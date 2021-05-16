
var 
    AWS_SDK = require("aws-sdk"),
    S3_API = new AWS_SDK.S3({
        region: "us-east-1",
        apiVersion: "2006-03-01"
    }),
    COMPREHEND = new AWS_SDK.Comprehend({
        region: "us-west-2",
        apiVersion: "2017-11-27"
    }),
    SOURCE_S3_BUCKET_STR = "aws-tc-largeobjects",
    SOURCE_KEY_STR = "DEV-AWS-MO-Building_2.0/my_json_lines.jsonl";

exports.handler = function(event, context, callback){
    var
        G_PRODUCT_ID_STR = event.product_id,
        SEARCH_EXPRESSION_STR = "SELECT s.product_id, s.review_body, s.review_headline, s.star_rating FROM s3object[*] s",
        g_search_results_arr = [],
        g_collectable_obj_arr = [],
        g_products_arr = [],
        g_bucket_name_str = "";

    (function init(){
        doSearch(async function(err, chunk_str){
            if(err){
                return callback(err, null);
            }
            await getBucketName();
            processChunk(chunk_str);
            await getEntity();
            await pushRawDataToS3();
            callback(null, true);
        });
    })();

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

    async function getEntity(){
        var 
            i_int = 0,
            start_int = 0,
            end_int = 25,
            drift_int = 0,
            response_promise_arr = [];
        (function _doLoop(){
            var done_boo = false,
                params = {
                    LanguageCode: "en",
                    TextList: []
                };
            for(i_int = start_int; i_int < end_int; i_int += 1){
                if(g_search_results_arr[i_int] === undefined){
                    done_boo = true;
                    break;
                }

                var temp = JSON.parse(g_search_results_arr[i_int]);
                if(Number.isNaN(Number(temp.star_rating))){
                    console.log("Skipping junk unparsable reviews");
                    continue;
                }

                if(g_search_results_arr[i_int].length > 4800){
                    console.log("Text too long, skipping, ", g_search_results_arr[i_int].length, i_int);
                    continue;
                }
                if(g_search_results_arr[i_int] === ""){
                    console.log("Empty review, skipping, ", g_search_results_arr[i_int].length, i_int);
                    continue;
                }
                try{
                    g_products_arr[drift_int] = JSON.parse(g_search_results_arr[i_int]);//title??
                    drift_int += 1;
                    params.TextList.push(JSON.parse(g_search_results_arr[i_int]).review_body);
                     // + i_int.toString());
                }catch(e){
                    console.log(e);
                }
            }
            response_promise_arr.push(COMPREHEND.batchDetectEntities(params).promise());
            start_int += 25;
            end_int += 25;
            if(done_boo === true){
                //
            }else{
                _doLoop();
            }
        })();

       await Promise.all(response_promise_arr).then((values_arr) => {
            var counter_int = 0;
            for(var i_int = 0; i_int < values_arr.length; i_int += 1){
                for(var j_int = 0; j_int < values_arr[i_int].ResultList.length; j_int += 1){
                    // values_arr[i_int].ResultList[j_int].Index;
                    values_arr[i_int].ResultList[j_int].product_info = g_products_arr[counter_int];
                    //values_arr[i_int].ResultList[j_int].rick_counter_int = counter_int;

                    g_collectable_obj_arr.push(values_arr[i_int].ResultList[j_int]);
                    counter_int += 1;
                }
            }
        });
    }

    async function pushRawDataToS3(){
        var
            params = {
                Bucket: g_bucket_name_str,
                Key: "tag.json",
                Body: JSON.stringify(g_collectable_obj_arr),
                ContentType: "application/json",
                CacheControl: "max-age=0"
            };

        await S3_API.putObject(params).promise();
    }


    function processChunk(chunk_str){
        //this is not working well

        g_search_results_arr = chunk_str.split("\n").filter(Boolean);

    }

    function doSearch(cb){
        var 
            params = {
                Bucket: SOURCE_S3_BUCKET_STR,
                Key: SOURCE_KEY_STR,
                Expression: SEARCH_EXPRESSION_STR,
                ExpressionType: "SQL",
                InputSerialization: {
                    JSON: {
                        Type: "LINES"
                    },
                },
                OutputSerialization: {
                    JSON: {

                    }
                }
            };
        S3_API.selectObjectContent(params, function(err, data){
            var 
                chunk_str = "";
            if(err){
                console.log("err", err);
                return;
            }
            var event = data.Payload;
            event.on("data", function(event) {
                if(event.Records){
                    chunk_str += event.Records.Payload.toString();
                }
            });
            event.on("error", function(err){
                cb(err, null);
            });
            event.on("end", function(){ 
                cb(null, chunk_str);
            });
        });
    }
};