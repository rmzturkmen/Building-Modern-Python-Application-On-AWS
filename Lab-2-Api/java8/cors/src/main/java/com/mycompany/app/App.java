package com.mycompany.app;


import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;


public class App {
    private static final AmazonApiGateway apiGatewayClient = AmazonApiGatewayClientBuilder.defaultClient();
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static String apiId = "";
    private static String bucketName = "";
    private static String postId = "";

    public static void main(String[] args) {
        getRestAPIId();
        getBucketName();
        getPostId();
        System.out.println("got our variables from API GW");
        createCors();
        System.out.println("created a MOCK OPTION resource for create_report");
        createIntegrationCors();
        System.out.println("created the basci mock integration request settings");
        createMethodResponseCors();
        System.out.println("created all the method CORS responses, and told it to allow Credentials (for later)");
        createIntegrationResponseCORS();
        System.out.println("created all Integration CORS responses, to lock it down to the website's origin");
        System.out.println("DONE");
        exit(0);
    }

    private static void getRestAPIId() {
        GetRestApisRequest request = new GetRestApisRequest();
        GetRestApisResult result = apiGatewayClient.getRestApis(request);
        apiId = result.getItems().get(0).getId();
    }

    private static void getBucketName() {
        List<Bucket> buckets = s3Client.listBuckets();
        String s3BucketName = "";
        for (Bucket b : buckets) {
            if(b.getName().contains("s3bucket")) {
                s3BucketName = b.getName();
            }
        }
        bucketName = s3BucketName;
    }

    private static void getPostId() {
        GetResourcesRequest request = new GetResourcesRequest();
        request.setRestApiId(apiId);
        GetResourcesResult result = apiGatewayClient.getResources(request);
        for (Resource resource : result.getItems()) {
            if (resource.getPath().contains("create_report")) {
                postId = resource.getId();
            }
        }
    }

    private static void createCors() {
        PutMethodRequest request = new PutMethodRequest();
        request.setAuthorizationType("NONE");
        request.setResourceId(postId);
        request.setRestApiId(apiId);
        request.setHttpMethod("OPTIONS");
        apiGatewayClient.putMethod(request);
    }

    private static void createIntegrationCors() {
        PutIntegrationRequest request = new PutIntegrationRequest();
        request.setHttpMethod("OPTIONS");
        request.setResourceId(postId);
        request.setRestApiId(apiId);
        request.setType("MOCK");
        Map<String, String> templates = new HashMap<>();
        templates.put("application/json", "{'statusCode': 200}");
        request.setRequestTemplates(templates);
        apiGatewayClient.putIntegration(request);
    }

    private static void createMethodResponseCors() {
        PutMethodResponseRequest request = new PutMethodResponseRequest();
        request.setHttpMethod("OPTIONS");
        request.setRestApiId(apiId);
        request.setResourceId(postId);
        request.setStatusCode("200");
        Map<String, Boolean> parameters = new HashMap<>();
        parameters.put("method.response.header.Access-Control-Allow-Headers", true);
        parameters.put("method.response.header.Access-Control-Allow-Origin", true);
        parameters.put("method.response.header.Access-Control-Allow-Methods", true);
        parameters.put("method.response.header.Access-Control-Allow-Credentials", true);
        request.setResponseParameters(parameters);
        Map<String, String> models = new HashMap<>();
        models.put("application/json", "Empty");
        request.setResponseModels(models);
        apiGatewayClient.putMethodResponse(request);
    }

    private static void createIntegrationResponseCORS() {
        PutIntegrationResponseRequest request = new PutIntegrationResponseRequest();
        request.setHttpMethod("OPTIONS");
        request.setResourceId(postId);
        request.setRestApiId(apiId);
        request.setStatusCode("200");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'");
        parameters.put("method.response.header.Access-Control-Allow-Methods", "'POST,OPTIONS'");
        parameters.put("method.response.header.Access-Control-Allow-Origin", "'https://" + bucketName + ".s3-us-west-2.amazonaws.com'");
        parameters.put("method.response.header.Access-Control-Allow-Credentials", "'true'");
        request.setResponseParameters(parameters);
        Map<String, String> templates = new HashMap<>();
        templates.put("application/json", "");
        request.setResponseTemplates(templates);
        apiGatewayClient.putIntegrationResponse(request);
    }

}