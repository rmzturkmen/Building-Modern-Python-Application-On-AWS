(async function(){
    
    var 
        AWS_SDK = require("aws-sdk"),
        API_GW = new AWS_SDK.APIGateway({
            apiVersion: "2015-07-09",
            region: "us-west-2"
        }),
        g_api_id_str = "",
        g_root_id_str = "",
        g_reviews_resource_id_str = "",
        g_av_star_rating_resource_id_str = "",
        g_create_report_resource_id_str = "";

    function createRestApi(){
        var 
            params = {
                name: "Fancy-Api",
                description: "test api",
                minimumCompressionSize: 123,
                endpointConfiguration: {
                    "types": ["REGIONAL"]
                }
            };
        return API_GW.createRestApi(params).promise();
    }

   function createResource(path_str){
        var 
            params = {
                restApiId: g_api_id_str,
                parentId: g_root_id_str,
                pathPart: path_str
            };
        return API_GW.createResource(params).promise();
    }
    
    function createMethod(method_str, resource_id_str){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: resource_id_str,
                httpMethod: method_str,
                authorizationType: "NONE",
                requestParameters: {
                    "method.request.querystring.product_id": false 
                }
            };
        if(method_str === "POST"){ //where we don't pass a product id
            delete params.requestParameters;
        }
        return API_GW.putMethod(params).promise();
    }
    
    function createIntegration(method_str, resource_id_str){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: resource_id_str,
                httpMethod: method_str,
                type: "MOCK",
                requestTemplates: {
                    "application/json": "{'statusCode': 200}"
                }
            };
        return API_GW.putIntegration(params).promise();
    }
    
    function createMethodResponse(method_str, resource_id_str){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: resource_id_str,
                httpMethod: method_str,
                statusCode: "200", //needs to be a string
                responseParameters: { //we will add CORS later
                    "method.response.header.Access-Control-Allow-Headers": false,
                    "method.response.header.Access-Control-Allow-Origin": false,
                    "method.response.header.Access-Control-Allow-Methods": false
                },
                responseModels: {"application/json": "Empty"}
            };
        return API_GW.putMethodResponse(params).promise();
    }

    function createIntegrationResponse(method_str, resource_id_str, route_name_str){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: resource_id_str,
                httpMethod: method_str,
                statusCode: "200",  //needs to be a string
                responseParameters: {
                    'method.response.header.Access-Control-Allow-Headers': "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'",
                    'method.response.header.Access-Control-Allow-Methods': "'" + method_str + "'",
                    'method.response.header.Access-Control-Allow-Origin': "'*'"
                }
            };
        if(route_name_str === "get_reviews"){
            params.responseTemplates = {
                "application/json": JSON.stringify({
                    "product_id_str" : "$input.params('product_id')",
                    "reviews_arr": [{
                        "review_body_str": "Both the dropcam and nest cam have an embarrassingly bad WIFI algorithm when there are multiple access points with the same name (SID) near it.  (I have a tall house and I need multiple WIFI access points)  When you have this situation, the cameras lose connectivity all the time. The obvious workaround is to dedicate a WIFI access point specifically for the Nest Cam, which is annoying.  Why Nest can't or won't fix this is beyond me.  I know of no other WIFI enabled device that is this dumb about WIFI connectivity.  Until this is fixed it stays a 3",
                        "rating_float": 3.25
                    }, {
                        "review_body_str": "It was easy to setup with a small hiccup during the scanning of the barcode on the back.  I still have issues with the software not loading correctly on my phone which customer service has said they are working on fixing.  The app hangs quite often when loading it from a push notification where I either get single spinners or double spinners.<br /><br />I do wish the monthly/yearly fees for video retention were better or there was maybe a network based solution for video storage as I would like to buy more of these and use them as a whole house system but would get quite pricy",
                        "rating_float": 2.25
                    }, {
                        "review_body_str": "I've had this device for a few weeks now and I really like it.  It was easy to setup and it's easy to use.  I already have a Nest thermostat which I love and I now use the same app (on Android) to manage the camera.  It is really cool to be able to view the camera from my phone wherever I am.  There are some small kinks which seem to need work in the app.  For example, clicking on the notification will open the app and infinitely try to load the image from the camera history.  If you don't pay for the history it was just infinitely load... you could wait an hour it will never load an image.  You have to back out of the app and open it again to see the image.  Also, the camera should come with at least one day or a few hours of video history included for free.  It would be great to have the option to cache video history to my own computer or network device.  Without paying the subscription fee you have ZERO video history.  You will get a notification that the camera detected motion.... but you can't see it because it's usually over before you can open the app.  The camera is pretty much useless without video history... but the prices for history are not cheap.  If you don't mind paying a monthly fee... it's a great device with excellent build quality and image quality.",
                        "rating_float": 4.25
                    }, {
                        "review_body_str": "I was hoping to use this for outdoor surveillance.  Proved to be too difficult to isolate zones where breezy plants wouldn't trigger unwanted alerts.  On one occasion, I received motion alerts when camera was allegedly off, which made me uncomfortable about when video was/wasn't being sent to cloud.  App had a bad habit of turning off my motion zones so my alerts were not useful.  Camera pours off heat.  Seems overall like an unrefined product not on par with the Nest thermostat which I own and like.",
                        "rating_float": 3.50
                    }]
                })
            };
        }else if(route_name_str === "get_av_star_rating"){
            params.responseTemplates = {
                "application/json": JSON.stringify({
                    "product_id_str" : "$input.params('product_id')",
                    "average_star_review_float":  3.25
                })
            };
        }else{ //must be create_report
            params.responseTemplates = {
                "application/json": JSON.stringify({
                     "message_str": "report requested, check your phone shortly"
                })
            };
        }
        // console.log(params);
        return API_GW.putIntegrationResponse(params).promise();
    }

    async function getRootId(){
        var 
            params = {
                restApiId: g_api_id_str
            },
            response = {};
       
        response = await API_GW.getResources(params).promise();
        for(var i_int = 0; i_int < response.items.length; i_int += 1){
            if(response.items[i_int]["path"] === "/"){
                console.log("caught the root", response.items[i_int]);
                g_root_id_str = response.items[i_int].id;
                break;
            }
        }
    }
    // init
    (async function(){
        //no error handling yet
        g_api_id_str = (await createRestApi()).id;
        console.log("Api created", g_api_id_str);
        await getRootId();
        console.log(g_root_id_str);
        g_reviews_resource_id_str = (await createResource("get_reviews")).id;
        g_av_star_rating_resource_id_str = (await createResource("get_av_star_rating")).id;
        g_create_report_resource_id_str = (await createResource("create_report")).id;
        console.log("created all 3 resources", 
                g_reviews_resource_id_str, 
                g_av_star_rating_resource_id_str, 
                g_create_report_resource_id_str);

        await createMethod("GET", g_reviews_resource_id_str);
        await createMethod("GET", g_av_star_rating_resource_id_str);
        await createMethod("POST", g_create_report_resource_id_str);
        console.log("created all 3 methods");
        //needed to wait

        await createIntegration("GET", g_reviews_resource_id_str);
        await createIntegration("GET", g_av_star_rating_resource_id_str);
        await createIntegration("POST", g_create_report_resource_id_str);
        console.log("created all 3 integrations");
       

        await createMethodResponse("GET", g_reviews_resource_id_str);
        await createMethodResponse("GET", g_av_star_rating_resource_id_str);
        await createMethodResponse("POST", g_create_report_resource_id_str);
        console.log("created all 3 Method responses");

        await createIntegrationResponse("GET", g_reviews_resource_id_str, "get_reviews");
        await createIntegrationResponse("GET", g_av_star_rating_resource_id_str, "get_av_star_rating");
        await createIntegrationResponse("POST", g_create_report_resource_id_str, "create_report");
        console.log("created all 3 integration responses");


        console.log("DONE");
    })();

})();






