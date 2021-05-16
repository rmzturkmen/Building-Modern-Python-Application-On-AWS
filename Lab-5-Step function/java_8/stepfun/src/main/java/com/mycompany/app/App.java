package com.mycompany.app;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.CreateStateMachineRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class App {
    private static final AmazonIdentityManagement iamClient = AmazonIdentityManagementClient.builder().build();
    private static final AWSStepFunctions stepClient = AWSStepFunctionsClientBuilder.standard().build();

    public static void main(String[] args) {
        String roleArn = getRoleArn();
        createStateMachine(roleArn);
        System.exit(0);
    }


    private static String getRoleArn() {
        ListRolesResult roles = iamClient.listRoles();
        String roleArn = "";
        for(Role r: roles.getRoles()) {
            if(r.getRoleName().contains("lab5-states-role")) {
                roleArn = r.getArn();
            }
        }
        return roleArn;
    }

    private static void createStateMachine(String roleArn) {

            CreateStateMachineRequest request = new CreateStateMachineRequest();
            request.setName("Fancy-StateMachine");
            request.setRoleArn(roleArn);
            request.setDefinition(getDefinition());
            stepClient.createStateMachine(request);

    }

    private static String getDefinition() {
        String definition = "";
        try{
            BufferedReader reader = new BufferedReader(new FileReader("../../resources/definition.json"));
            while(reader.ready()) {
                definition += reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return definition;
    }
}