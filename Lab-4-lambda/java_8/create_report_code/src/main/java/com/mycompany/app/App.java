package com.mycompany.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Map;


public class App implements RequestHandler<Map<String,Object>, String> {

    @Override
    public String handleRequest(Map<String,Object> event, Context context) {
        return generateResponse(event);
    }
    private static String generateResponse(Map<String,Object> event) {
        String ipv4 = "";
        String user = "";
        String phone = "";
        if(event != null && event.get("params") != null) {
            String[] params = String.valueOf(event.get("params")).split(",");
            if(params.length > 0) {
                for (String param : params) {
                    if (param.contains("Authorization=Bearer")) {
                        String extract = param.replaceAll("Authorization=Bearer", "").trim();
                        byte[] byteArray = Base64.decodeBase64(extract.getBytes());
                        String decodedString = null; // for UTF-8 encoding
                        try {
                            decodedString = new String(byteArray, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            System.out.println("Error decoding auth");
                        }
                        JsonObject jsonObject = new JsonParser().parse(decodedString.split("}")[1] + "}").getAsJsonObject();
                        phone = String.valueOf(jsonObject.get("phone_number"));
                        user = String.valueOf(jsonObject.get("cognito:username"));
                    }

                    if (param.contains("X-Forwarded-For")) {
                        ipv4 = param.replace("X-Forwarded-For=", "");
                    }
                }
            }

        }

        if( ! ipv4.isEmpty() &&  ! user.isEmpty() && ! phone.isEmpty()) {
            String response = ("{ \"statusCode\": 200, \"ipv4\":\"" + ipv4 + "\",\"phone\":" + phone +
                    ",\"name\":" +  user + ",\"message_str\": \"Report Processing\"}");
            return response;
        }

        String response = ("{ \"statusCode\": 200, \"message_str\": \"Report processing, check your phone shortly\"}");
        return response;
    }
}