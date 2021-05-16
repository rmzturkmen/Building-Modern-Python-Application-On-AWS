package com.mycompany.app;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import software.amazon.awssdk.awscore.exception.*;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionCode;

import java.util.List;

public class App {
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static final AmazonIdentityManagement iamClient = AmazonIdentityManagementClient.builder().build();
    private static final LambdaClient awsLambda = LambdaClient.create();

    public static void main(String[] args) {
        String bucketName = getBucketName();
        String roleArn = getRoleArn();
        createLambdaFunction(bucketName, roleArn);
        System.exit(0);
    }

    private static String getBucketName() {
        List<Bucket> buckets = s3Client.listBuckets();
        String bucketName = "";
        for (Bucket b : buckets) {
            if(b.getName().contains("s3bucket")) {
                bucketName = b.getName();
            }
        }
        return bucketName;
    }

    private static String getRoleArn() {
        ListRolesResult roles = iamClient.listRoles();
        String roleArn = "";
        for(Role r: roles.getRoles()) {
            if(r.getRoleName().contains("lab5-lambda-role")) {
                roleArn = r.getArn();
            }
        }
        return roleArn;
    }

    private static void createLambdaFunction(String bucketName, String roleArn) {

        try {

            awsLambda.createFunction(CreateFunctionRequest.builder()
                    .code(FunctionCode.builder()
                            .s3Bucket(bucketName)
                            .s3Key("publish.zip").build())
                    .functionName("publish")
                    .handler("com.mycompany.app.App::handleRequest")
                    .memorySize(448)
                    .timeout(90)
                    .runtime("java8")
                    .role(roleArn)
                    .publish(true)
                    .build());

        } catch (AwsServiceException e) {
            System.out.println(e.getMessage());
        }
    }
}