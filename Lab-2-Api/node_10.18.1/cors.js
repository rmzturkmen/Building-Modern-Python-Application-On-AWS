(async function(){
    
    var 
        AWS_SDK = require("aws-sdk"),
        API_GW = new AWS_SDK.APIGateway({
            apiVersion: "2015-07-09",
            region: "us-west-2"
        }),
        S3_API = new AWS_SDK.S3({
            region: "us-east-1",
            apiVersion: "2006-03-01"
        }),
        g_bucket_name_str = "",
        g_api_id_str = "",
        g_post_id_str = "",
        g_reviews_resource_id_str = "",
        g_av_star_rating_resource_id_str = "",
        g_create_report_resource_id_str = "";

    async function getBucketName(){
        var 
            buckets_arr = (await S3_API.listBuckets().promise()).Buckets;
        for(var i_int = 0; i_int < buckets_arr.length; i_int += 1){
            if(buckets_arr[i_int].Name.indexOf("s3bucket") !== -1){
                g_bucket_name_str = buckets_arr[i_int].Name;
                break;
            }
        }
    }

    async function createCORS(){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: g_post_id_str,
                httpMethod: "OPTIONS",
                authorizationType: "NONE" //for now until we build the authorizer in lab 3
            };
        await API_GW.putMethod(params).promise();
    }
    
    async function createIntegrationCORS(){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: g_post_id_str,
                httpMethod: "OPTIONS",
                type: "MOCK",
                requestTemplates: {
                    "application/json": "{'statusCode': 200}"
                }
            };
        await API_GW.putIntegration(params).promise();
    }
    
    async function createMethodResponseCORS(){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: g_post_id_str,
                httpMethod: "OPTIONS",
                statusCode: "200",
                responseParameters: {
                    "method.response.header.Access-Control-Allow-Headers": true,
                    "method.response.header.Access-Control-Allow-Origin": true,
                    "method.response.header.Access-Control-Allow-Methods": true,
                    "method.response.header.Access-Control-Allow-Credentials": true
                },
                responseModels: {
                    "application/json": "Empty"
                }
            };
        await API_GW.putMethodResponse(params).promise();
    }


    async function createIntegrationResponseCORS(){
        var 
            params = {
                restApiId: g_api_id_str,
                resourceId: g_post_id_str,
                httpMethod: "OPTIONS",
                statusCode: "200",
                responseParameters: { //always wrap in quotes
                    "method.response.header.Access-Control-Allow-Headers": "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'",
                    "method.response.header.Access-Control-Allow-Methods": "'POST,OPTIONS'",
                    "method.response.header.Access-Control-Allow-Origin": "'https://" + g_bucket_name_str + ".s3-us-west-2.amazonaws.com'",
                    "method.response.header.Access-Control-Allow-Credentials": "'true'"
                },
                responseTemplates: {
                     "application/json": ""
                }
            };
        await API_GW.putIntegrationResponse(params).promise();
    }

    async function getRestApiId(){
        g_api_id_str = (await API_GW.getRestApis().promise()).items[0].id;
    }

    async function getPostId(){
        var 
            params = {
                restApiId: g_api_id_str
            },
            response = {};
       
        response = await API_GW.getResources(params).promise();
        for(var i_int = 0; i_int < response.items.length; i_int += 1){
            if(response.items[i_int].pathPart === "create_report"){
                g_post_id_str = response.items[i_int].id;
                break;
            }
        }
    }

    (async function(){
        await getBucketName();
        await getRestApiId();
        await getPostId();
        console.log("got our variables from API GW");
        await createCORS();
        console.log("created a MOCK OPTION resource for create_report");
        await createIntegrationCORS();
        console.log("created the basci mock integration request settings");
        await createMethodResponseCORS();
        console.log("created all the method CORS responses, and told it to allow Credentials (for later)");
        await createIntegrationResponseCORS();
        console.log("created all Integration CORS responses, to lock it down to the website's origin");
        console.log("DONE");
    })();

})();






