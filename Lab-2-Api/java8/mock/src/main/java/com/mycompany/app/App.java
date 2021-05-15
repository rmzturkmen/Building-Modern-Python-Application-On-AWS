package com.mycompany.app;


import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;


public class App {
    private static final AmazonApiGateway apiGatewayClient = AmazonApiGatewayClientBuilder.defaultClient();
    private static String apiId = "";
    private static String parentId = "";

    public static void main(String[] args) {
        createRestApi();
        System.out.println("Api created " +  apiId);
        getParentId();
        System.out.println("Root Id " + parentId);
        String reviewsResourceId = createApiResource("get_reviews");
        String starRatingResourceId = createApiResource("get_av_star_rating");
        String createReportResourceId = createApiResource("create_report");
        System.out.println("created all 3 resources "  + reviewsResourceId + ", " + starRatingResourceId + ", " + createReportResourceId);
        createMethod("GET", reviewsResourceId);
        createMethod("POST", createReportResourceId);
        createMethod("GET", starRatingResourceId);
        System.out.println("created all 3 methods");
        createIntegration("GET", reviewsResourceId);
        createIntegration("GET", starRatingResourceId);
        createIntegration("POST", createReportResourceId);
        System.out.println("created all 3 integrations");
        createMethodResponse("GET", reviewsResourceId);
        createMethodResponse("GET", starRatingResourceId);
        createMethodResponse("POST", createReportResourceId);
        System.out.println("created all 3 Method responses");
        createIntegrationResponse("GET",reviewsResourceId, "get_reviews");
        createIntegrationResponse("GET",starRatingResourceId, "get_av_star_rating");
        createIntegrationResponse("POST", createReportResourceId, "create_report");
        System.out.println("created all 3 integration responses");
        System.out.println("Done");
        exit(0);
    }

    private static void createRestApi() {
        CreateRestApiRequest request = new CreateRestApiRequest();
        request.setName("Fancy-Api");
        request.setDescription("test api");
        request.setMinimumCompressionSize(123);
        request.setEndpointConfiguration(new EndpointConfiguration().withTypes("REGIONAL"));
        CreateRestApiResult result = apiGatewayClient.createRestApi(request);
        apiId = result.getId();
    }

    private static String createApiResource(String path) {
        CreateResourceRequest request = new CreateResourceRequest();
        request.setRestApiId(apiId);
        request.setParentId(parentId);
        request.setPathPart(path);
        return apiGatewayClient.createResource(request).getId();
    }

    private static void getParentId() {
        GetResourcesRequest request = new GetResourcesRequest();
        request.setRestApiId(apiId);
        GetResourcesResult result = apiGatewayClient.getResources(request);
        for (Resource resource : result.getItems()) {
            if (resource.getPath().equals("/")) {
                System.out.println("caught the root" + resource.getId());
                parentId = resource.getId();
            }
        }
    }

    private static void createMethod(String httpMethod, String resourceId) {
        PutMethodRequest request = new PutMethodRequest();
        request.setAuthorizationType("NONE");
        request.setResourceId(resourceId);
        request.setRestApiId(apiId);
        request.setHttpMethod(httpMethod);
        if (httpMethod.equals("GET")) {
            Map<String, Boolean> requestParams = new HashMap<>();
            requestParams.put("method.request.querystring.product_id", false);
            request.setRequestParameters(requestParams);
        } else {
            Map<String, Boolean> requestParams = new HashMap<>();
            request.setRequestParameters(requestParams);
        }
        apiGatewayClient.putMethod(request);
    }

    private static void createCors(String resourceId) {
        //apiGatewayClient.
    }

    private static void createIntegration(String httpMethod, String resourceId) {
        PutIntegrationRequest request = new PutIntegrationRequest();
        request.setHttpMethod(httpMethod);
        request.setResourceId(resourceId);
        request.setRestApiId(apiId);
        request.setType("MOCK");
        Map<String, String> templates = new HashMap<>();
        templates.put("application/json", "{'statusCode': 200}");
        request.setRequestTemplates(templates);
        apiGatewayClient.putIntegration(request);
    }

    private static void createMethodResponse(String httpMethod, String resourceId) {
        PutMethodResponseRequest request = new PutMethodResponseRequest();
        request.setHttpMethod(httpMethod);
        request.setRestApiId(apiId);
        request.setResourceId(resourceId);
        request.setStatusCode("200");
        Map<String, Boolean> parameters = new HashMap<>();
        parameters.put("method.response.header.Access-Control-Allow-Headers", false);
        parameters.put("method.response.header.Access-Control-Allow-Origin", false);
        parameters.put("method.response.header.Access-Control-Allow-Methods", false);
        request.setResponseParameters(parameters);
        Map<String, String> models = new HashMap<>();
        models.put("application/json", "Empty");
        request.setResponseModels(models);
        apiGatewayClient.putMethodResponse(request);
    }

    private static void createIntegrationResponse(String httpMethod, String resourceId, String methodName) {
        PutIntegrationResponseRequest request = new PutIntegrationResponseRequest();
        request.setHttpMethod(httpMethod);
        request.setResourceId(resourceId);
        request.setRestApiId(apiId);
        request.setStatusCode("200");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'");
        parameters.put("method.response.header.Access-Control-Allow-Methods", "'" + methodName + "'");
        parameters.put("method.response.header.Access-Control-Allow-Origin", "'*'");
        request.setResponseParameters(parameters);
        Map<String, String> templates = new HashMap<>();
        templates.put("application/json", "'message_str': 'report requested, check your phone shortly'");
        if (methodName.equals("get_reviews")) {
            templates = new HashMap<>();
            templates.put("application/json", "{ \"product_id_str\" : \"$input.params('product_id')\","
                            + "\"reviews_arr\":  [{"
                            + "\"review_body_str\": \"Both the dropcam and nest cam have an embarrassingly bad WIFI algorithm when there are multiple access points with the same name (SID) near it.  (I have a tall house and I need multiple WIFI access points)  When you have this situation, the cameras lose connectivity all the time. The obvious workaround is to dedicate a WIFI access point specifically for the Nest Cam, which is annoying.  Why Nest can\'t or won\'t fix this is beyond me.  I know of no other WIFI enabled device that is this dumb about WIFI connectivity.  Until this is fixed it stays a 3\","
                            + "\"rating_int\": 3.25"
                            + "},{"
                            + "\"review_body_str\": \"It was easy to setup with a small hiccup during the scanning of the barcode on the back.  I still have issues with the software not loading correctly on my phone which customer service has said they are working on fixing.  The app hangs quite often when loading it from a push notification where I either get single spinners or double spinners.<br /><br />I do wish the monthly/yearly fees for video retention were better or there was maybe a network based solution for video storage as I would like to buy more of these and use them as a whole house system but would get quite pricy\","
                            + "\"rating_int\": 2.25"
                            + "},{"
                            + "\"review_body_str\": \"I\'ve had this device for a few weeks now and I really like it.  It was easy to setup and it\'s easy to use.  I already have a Nest thermostat which I love and I now use the same app (on Android) to manage the camera.  It is really cool to be able to view the camera from my phone wherever I am.  There are some small kinks which seem to need work in the app.  For example, clicking on the notification will open the app and infinitely try to load the image from the camera history.  If you don\'t pay for the history it was just infinitely load... you could wait an hour it will never load an image.  You have to back out of the app and open it again to see the image.  Also, the camera should come with at least one day or a few hours of video history included for free.  It would be great to have the option to cache video history to my own computer or network device.  Without paying the subscription fee you have ZERO video history.  You will get a notification that the camera detected motion.... but you can\'t see it because it\'s usually over before you can open the app.  The camera is pretty much useless without video history... but the prices for history are not cheap.  If you don\'t mind paying a monthly fee... it\'s a great device with excellent build quality and image quality.\","
                            + "\"rating_int\":4.25 "
                    + "},{"
                    + "\"review_body_str\": \"I was hoping to use this for outdoor surveillance.  Proved to be too difficult to isolate zones where breezy plants wouldn\'t trigger unwanted alerts.  On one occasion, I received motion alerts when camera was allegedly off, which made me uncomfortable about when video was/wasn\'t being sent to cloud.  App had a bad habit of turning off my motion zones so my alerts were not useful.  Camera pours off heat.  Seems overall like an unrefined product not on par with the Nest thermostat which I own and like.\","
                    + "\"rating_int\": 3.50"
                    + "}]}");

        } else if (methodName.equals("get_av_star_rating")) {
            templates = new HashMap<>();
            templates.put("application/json", "{" +
                    "                \"product_id_str\" : \"$input.params('product_id')\",\n" +
                    "                        \"average_star_review_float\":  3.25 "+
                    "            }");
        }

        request.setResponseTemplates(templates);
        apiGatewayClient.putIntegrationResponse(request);
    }

}