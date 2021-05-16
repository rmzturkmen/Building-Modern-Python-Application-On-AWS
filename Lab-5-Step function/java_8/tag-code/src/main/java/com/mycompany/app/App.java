package com.mycompany.app;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.BatchDetectEntitiesItemResult;
import com.amazonaws.services.comprehend.model.BatchDetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.BatchDetectEntitiesResult;
import com.amazonaws.services.comprehend.model.BatchItemError;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App implements RequestHandler<Boolean, Boolean> {
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static final String S3_BUCKET = "aws-tc-largeobjects";
    private static final String S3_FILE = "DEV-AWS-MO-Building_2.0/my_json_lines.jsonl";
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String S3_SELECT_QUERY = "SELECT s.product_id, s.review_body, s.review_headline, s.star_rating FROM s3object[*] s";
    private static final AmazonComprehend comprehendClient = AmazonComprehendClientBuilder.defaultClient();

    @Override
    public Boolean handleRequest(Boolean event, Context context) {
       processReviews();
       return true;
    }

    protected static void processReviews() {
        List<ProductReviewTag> products = queryS3();
        products = determineEntites(products);
        String data = generateResponse(products);
        uploadToS3(data);
    }

    private static List<ProductReviewTag> queryS3() {
        // Call S3 Select
        SelectObjectContentRequest request = generateJSONRequest();
        SelectObjectContentResult result = s3Client.selectObjectContent(request);
        List<ProductReviewTag> products = new ArrayList<>();

        try {

            InputStream resultInputStream = result.getPayload().getRecordsInputStream();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(resultInputStream, "UTF-8"));

            String inputStr;
            int i = 0;
            while (true) {
                if (!((inputStr = streamReader.readLine()) != null)) break;
                String dataJson = gson.toJson(inputStr);
                dataJson = dataJson.replace("\\", "");
                dataJson = dataJson.substring(1, dataJson.length() - 1);
                try {
                    JsonObject obj = new JsonParser().parse(dataJson).getAsJsonObject();
                    ProductReviewTag product = new ProductReviewTag();
                    product.setReviewBody(obj.get("review_body").getAsString());
                    product.setProductId(obj.get("product_id").getAsString());
                    product.setReviewHeadline(obj.get("review_headline").getAsString());
                    product.setStarRating((obj.get("star_rating").getAsString()));
                    // Strings must be less than 5000 bytes for comprehend
                    if (product.getReviewBody().getBytes().length < 5000) {
                        // Star rating must be numeric
                        if (product.getStarRating().chars().allMatch(Character::isDigit)) {
                            products.add(product);
                            i++;
                        } else {
                            System.out.println("Skipping due to star rating");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("malformed json, skipping review");
                }

            }

        } catch (IOException e) {
            // Implement more robust error handling
            // as necessary
            System.out.println(e.getMessage());
        }

        return products;
    }

    private static SelectObjectContentRequest generateJSONRequest() {
        SelectObjectContentRequest request = new SelectObjectContentRequest();
        request.setBucketName(S3_BUCKET);
        request.setKey(S3_FILE);
        request.setExpression(S3_SELECT_QUERY);
        request.setExpressionType(ExpressionType.SQL);

        InputSerialization inputSerialization = new InputSerialization();
        inputSerialization.setJson(new JSONInput().withType("Lines"));
        inputSerialization.setCompressionType(CompressionType.NONE);
        request.setInputSerialization(inputSerialization);

        OutputSerialization outputSerialization = new OutputSerialization();
        outputSerialization.setJson(new JSONOutput());
        request.setOutputSerialization(outputSerialization);

        return request;
    }

    private static List<ProductReviewTag> determineEntites(List<ProductReviewTag> products) {
        String[] reviews = new String[25];
        int j = 0;
        for (int i = 0; i < products.size(); i++) {
            reviews[j] = products.get(i).getReviewBody();
            if (i % 24 == 0 && i != 0) {
                callComprehend(reviews, i - 24, products);
                j = 0;
            } else if(i == products.size() - 1) {
                int finalIndex = products.size() % 24;
                // copy items into a smaller array for the last batch
                String[] smallerReviewsArray = new String[finalIndex];
                System.arraycopy(reviews, 0, smallerReviewsArray, 0, finalIndex);
                callComprehend(smallerReviewsArray, i - (finalIndex-1), products);
            }
            j++;
        }

        return products;
    }

    private static void callComprehend(String[] reviews, int i, List<ProductReviewTag> resultObj) {
        // Call detectEntities API
        BatchDetectEntitiesRequest batchDetectEntitiesRequest = new BatchDetectEntitiesRequest().withTextList(reviews)
                .withLanguageCode("en");
        BatchDetectEntitiesResult batchDetectEntitiesResult = comprehendClient.batchDetectEntities(batchDetectEntitiesRequest);

        for (int j = 0; j < batchDetectEntitiesResult.getResultList().size(); j++) {
            resultObj.get(i).setEntities(batchDetectEntitiesResult.getResultList().get(j).getEntities());
            i++;
        }

        // check if we need to retry failed requests
        if (batchDetectEntitiesResult.getErrorList().size() != 0) {
            System.out.println("Retrying Failed Requests");
            ArrayList<String> textToRetry = new ArrayList<String>();
            for (BatchItemError errorItem : batchDetectEntitiesResult.getErrorList()) {
                textToRetry.add(reviews[errorItem.getIndex()]);
            }

            batchDetectEntitiesRequest = new BatchDetectEntitiesRequest().withTextList(textToRetry).withLanguageCode("en");
            batchDetectEntitiesResult = comprehendClient.batchDetectEntities(batchDetectEntitiesRequest);

            for (BatchDetectEntitiesItemResult item : batchDetectEntitiesResult.getResultList()) {
                System.out.println(item);
            }
        }
    }

    private static String generateResponse(List<ProductReviewTag> results) {

        String responseBody = "{\"results_arr\": [";
        for (int i = 0; i < results.size(); i++) {
            responseBody += results.get(i).toString();
            if (i != results.size() - 1) {
                responseBody += ",";
            }
        }
        responseBody = responseBody.replace("\\", "");
        responseBody += "]}";
        System.out.println(responseBody);
        return responseBody;
    }

    private static void uploadToS3(String data) {

        List<Bucket> buckets = s3Client.listBuckets();
        String bucketName = "";
        for (Bucket b : buckets) {
            if (b.getName().contains("s3bucket")) {
                bucketName = b.getName();
            }
        }
        s3Client.putObject(bucketName, "tag.json", data);

    }
}