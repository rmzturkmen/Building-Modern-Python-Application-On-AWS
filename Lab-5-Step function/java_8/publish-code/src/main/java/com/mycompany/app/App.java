package com.mycompany.app;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.reflect.TypeToken;

public class App implements RequestHandler<List<Boolean>, Boolean> {
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final String S3_BUCKET_NAME = getS3BucketName();
    private static final String SENTIMENT_FILE_KEY = "sentiment.json";
    private static final String TAG_FILE_KEY = "tag.json";
    private static final String REPORT_FILE_KEY = "report.html";

    @Override
    public Boolean handleRequest(List<Boolean> event, Context context) {
        publish();
        return true;
    }

    private static void publish() {
        // get product review sentiment tag list
        List<ProductReviewSentimentTag> productReviewSentimentTags = getProductSentimentTagList();

        // create HTML report
        String htmlTemplate = generateHTMLTemplate();
        String fullHtml = addDataToHTML(htmlTemplate, productReviewSentimentTags);

        // upload report
        uploadToS3(fullHtml);
    }

    private static List<ProductReviewSentimentTag> getProductSentimentTagList() {
        List<SentimentResult> productReviewSentiments = getProductReviewSentimentList();
        List<TagResult> productReviewTags = getProductReviewTagList();
        return mergeLists(productReviewSentiments, productReviewTags);
    }

    private static List<SentimentResult> getProductReviewSentimentList() {
        InputStream s3Object = downloadObjectFromS3(SENTIMENT_FILE_KEY);
        String s3ObjectString = convertTextInputStreamToString(s3Object);
        return convertStringToProductReviewSentiment(s3ObjectString);
    }

    private static List<TagResult> getProductReviewTagList() {
        InputStream s3Object = downloadObjectFromS3(TAG_FILE_KEY);
        String s3ObjectString = convertTextInputStreamToString(s3Object);
        return convertStringToProductReviewTag(s3ObjectString);
    }

    private static InputStream downloadObjectFromS3(String key) {
        // get object from S3
        S3Object object = s3Client.getObject(new GetObjectRequest(S3_BUCKET_NAME, key));
        // get object contents as input stream
        return object.getObjectContent();
    }

    private static String convertTextInputStreamToString(InputStream input) {
        // Read the text input stream one line at a time and display each line.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        String objectContent = "";
        try {
            while ((line = reader.readLine()) != null) {
                objectContent += line;
            }
        } catch (IOException e) {
            // in the real world please do something with errors
            // do not just log them
            // do as I say not as I do
            System.out.println(e.getMessage());
        }
        objectContent = objectContent.replace("{\"results_arr\": ", "");
        objectContent = objectContent.substring(0, objectContent.length() - 1);
        return objectContent;
    }

    private static List<SentimentResult> convertStringToProductReviewSentiment(String data) {
        Type type = new TypeToken<List<SentimentResult>>(){}.getType();
        return (List<SentimentResult>) gson.fromJson(data, type);
    }

    private static List<TagResult> convertStringToProductReviewTag(String data) {
        Type type = new TypeToken<List<TagResult>>(){}.getType();
        return (List<TagResult>) gson.fromJson(data, type);
    }

    private static String getS3BucketName() {
        List<Bucket> buckets = s3Client.listBuckets();
        String bucketName = "";
        for (Bucket b : buckets) {
            if(b.getName().contains("s3bucket")) {
                bucketName = b.getName();
            }
        }
        return bucketName;
    }

    private static List<ProductReviewSentimentTag> mergeLists(List<SentimentResult> sentiment, List<TagResult> tags) {
        List<ProductReviewSentimentTag> mergedList = new ArrayList<>();

        for (int i = 0; i < tags.size(); i++) {
            ProductReviewSentimentTag sentimentTag = new ProductReviewSentimentTag(sentiment.get(i), tags.get(i));
            mergedList.add(sentimentTag);
        }
        return mergedList;
    }

    private static String generateHTMLTemplate() {
        String html = "";
        html += "<!DOCTYPE html>";
        html += "<html>";
        html +=     "<head>";
        html +=         "<title>Report</title>";
        html +=         "<link rel=\"stylesheet\" href=\"report.css\" />";
        html +=     "</head>";
        html +=     "<body class=\"report\">";
        html +=         "<main>";
        html +=             "<header>";
        html +=                 "<h1>Report</h1>";
        html +=             "</header>";
        html +=             "<section>";
        html +=                 "REPLACE_ME";
        html +=             "</section>";
        html +=         "</main>";
        html +=     "</body>";
        html += "</html>";
        return html;
    }

    private static String addDataToHTML(String template, List<ProductReviewSentimentTag> productReviewSentimentTags) {

        String html = "";
        for(ProductReviewSentimentTag p : productReviewSentimentTags){
            html +=     "<div>";
            html +=         "<span>" + p.getProductId() +"</span>";
            html +=         "<h2>" + p.getReviewHeadline() + "</h2>";
            html +=         "<h3>" + p.getSentiment() + "</h3>";
            html +=         "<h4>" + p.getStarRating() + "</h4>";
            
            html +=         "<section>";
            if(p.getEntities() != null){
                if(!p.getEntities().isEmpty()) {
                    for(Entity entity: p.getEntities()) {
                        html +=             "<span>";
                        html +=                 "<p> Text: " + entity.getText() + "</p>";
                        html +=                 "<p> Type: " + entity.getType() + "</p>";
                        html +=                 "<p> Begin Offset: " + entity.getBeginOffset() + "</p>";
                        html +=                 "<p> End Offset: " + entity.getEndOffset() + "</p>";
                        html +=                 "<p> Score: " + entity.getScore() + "</p>";
                        html +=             "</span>";
                    }
                    html +=         "</section>";
                }
            }
            html +=         "<p>" + p.getReviewBody() + "</p>";
            html +=     "</div>";
        }

        template = template.replace("REPLACE_ME", html);
        return template;
    }

    private static void uploadToS3(String data) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        metadata.setCacheControl("max-age=0");
        metadata.setContentLength(data.getBytes().length);
        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        PutObjectRequest request = new PutObjectRequest(S3_BUCKET_NAME, REPORT_FILE_KEY, stream, metadata);
        s3Client.putObject(request);
    }

}