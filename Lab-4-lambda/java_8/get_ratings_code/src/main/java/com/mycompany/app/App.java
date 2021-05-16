package com.mycompany.app;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.JSONInput;
import com.amazonaws.services.s3.model.JSONOutput;
import com.amazonaws.services.s3.model.CompressionType;
import com.amazonaws.services.s3.model.ExpressionType;
import com.amazonaws.services.s3.model.InputSerialization;
import com.amazonaws.services.s3.model.OutputSerialization;
import com.amazonaws.services.s3.model.SelectObjectContentRequest;
import com.amazonaws.services.s3.model.SelectObjectContentResult;
import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;


public class App implements RequestHandler<Map<String,String>, APIGatewayProxyResponseEvent> {
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static final String S3_BUCKET = "aws-tc-largeobjects";
    private static final String S3_FILE = "DEV-AWS-MO-Building_2.0/my_json_lines.jsonl";
    private static final Gson gson = new Gson();
    private static final String QUERY_PARAM_NAME = "product_id_str";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String,String> event, Context context) {
        double averageRating = readData(event);
        return generateResponse(averageRating, event.get(QUERY_PARAM_NAME));
    }

    protected static double readData(Map<String,String> event) {
        String productId = event.get(QUERY_PARAM_NAME);
        SelectObjectContentRequest request = generateJSONRequest(productId);
        List<Integer> ratings = queryS3(request);
        return calculateAverageRating(ratings);
    }

    private static List<Integer> queryS3(SelectObjectContentRequest request) {
        // Call S3 Select
        SelectObjectContentResult result = s3Client.selectObjectContent(request);
        JsonArray array = new JsonArray();
        List<Integer> ratings = new ArrayList<Integer>();
        try {

            InputStream resultInputStream = result.getPayload().getRecordsInputStream();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(resultInputStream, "UTF-8"));

            String inputStr;

            while (true) {
                if (!((inputStr = streamReader.readLine()) != null)) break;
                String dataJson = gson.toJson(inputStr);
                dataJson = dataJson.replace("\\", "");
                dataJson = dataJson.substring(1, dataJson.length()-1);
                JsonObject obj = new JsonParser().parse(dataJson).getAsJsonObject();
                String rating = obj.get("star_rating").getAsString();
                ratings.add(Integer.parseInt(rating));
            }


        } catch (IOException e) {
            // Implement more robust error handling
            // as necessary
            System.out.println(e.getMessage());
        }

        return ratings;
    }

    private static SelectObjectContentRequest generateJSONRequest(String productId) {
        SelectObjectContentRequest request = new SelectObjectContentRequest();
        request.setBucketName(S3_BUCKET);
        request.setKey(S3_FILE);
        request.setExpression("select s.star_rating from s3object[*] s where s.product_id = '" + productId + "'");
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

    private static double calculateAverageRating(List<Integer> ratings) {
        OptionalDouble average = ratings
                .stream()
                .mapToDouble(a -> a)
                .average();

        return average.isPresent() ? average.getAsDouble() : 0;
    }

    private static APIGatewayProxyResponseEvent generateResponse(double averageRating, String productId) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("{\"product_id_str\": \"" + productId + "\"" + ",\"average_star_review_float\": " + averageRating + "}");
        return response;
    }
}