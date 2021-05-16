
var 
    AWS_SDK = require("aws-sdk"),
    S3_API = new AWS_SDK.S3({
        region: "us-east-1",
        apiVersion: "2006-03-01"
    });

exports.handler = function(event, context, callback){

    var 
        g_bucket_name_str = "",
        g_template_html = '',
        g_sentiment_data_arr = {},
        g_tag_data_arr = {},
        g_workable_data_arr = {};

    (async function init(){
        await getBucketName();
        await getTagData();
        await getSentimentData();
        createTemplate();
        createWorkableDataStructure();
        pushJSONToTemplate();
        await publishToS3();
        console.log("Done with publishing to s3");
        callback(null, true);
    })();

    async function getSentimentData(){
        var
            params = {
                Bucket: g_bucket_name_str,
                Key: "sentiment.json"
            },
            sentiment_data = await S3_API.getObject(params).promise();
        g_sentiment_data_arr = JSON.parse(sentiment_data.Body.toString());
    }
    
    async function getTagData(){
        var
            params = {
                Bucket: g_bucket_name_str,
                Key: "tag.json"
            },
            tag_data = await S3_API.getObject(params).promise();
        g_tag_data_arr = JSON.parse(tag_data.Body.toString());
    }

    async function getBucketName(){
        var 
            buckets_arr = (await S3_API.listBuckets().promise()).Buckets;
        for(var i_int = 0; i_int < buckets_arr.length; i_int += 1){
            if(buckets_arr[i_int].Name.indexOf("s3bucket") !== -1){
                g_bucket_name_str = buckets_arr[i_int].Name;
                break;
            }
        }
        return 
    }

    function createTemplate(){
        var 
            html_str = '';
        html_str += '<!DOCTYPE html>';
        html_str += '<html>';
        html_str +=     '<head>';
        html_str +=         '<title>Report</title>';
        html_str +=         '<link rel="stylesheet" href="report.css" />';
        html_str +=     '</head>';
        html_str +=     '<body class="report">';
        html_str +=         '<main>';
        html_str +=             '<header>';
        html_str +=                 '<h1>Report</h1>';
        html_str +=             '</header>';
        html_str +=             '<section>';
        html_str +=                 "REPLACE_ME";
        html_str +=             '</section>';
        html_str +=         '</main>';
        html_str +=     '</body>';
        html_str += '</html>';
        g_template_html = html_str;
    }

    async function publishToS3(){
        // push to s3
        var 
            params = {
                Bucket: g_bucket_name_str,
                Key: "report.html",
                Body: g_template_html,//convert???
                ContentType: "text/html",
                CacheControl: "max-age=0"
            };

        return S3_API.putObject(params).promise();
    }

    function createWorkableDataStructure(){
        var 
            final_arr = [],
            merged_construct = {};
        for(var i_int = 0; i_int < g_tag_data_arr.length; i_int += 1){
            merged_construct = {};//reset
            //product info
            merged_construct.product_id_str =  g_sentiment_data_arr[i_int].product_info.product_id;
            merged_construct.review_headline_str =  g_sentiment_data_arr[i_int].product_info.review_headline;
            merged_construct.review_body_str =  g_sentiment_data_arr[i_int].product_info.review_body;
            merged_construct.star_rating_float = Number(g_sentiment_data_arr[i_int].product_info.star_rating);
            //sentiment result
            merged_construct.sentiment_str = g_sentiment_data_arr[i_int].Sentiment.toLowerCase();

            // mrege in entitiy If there is one
           
            if(g_tag_data_arr[i_int].Entities.length === 0){
                //
            }else{
                 merged_construct.entity_str_arr = [];
            }
            for(var j_int = 0; j_int < g_tag_data_arr[i_int].Entities.length; j_int += 1){
                // console.log(g_tag_data_arr[i_int].Entities[j_int].Type);
                if(merged_construct.entity_str_arr.indexOf(g_tag_data_arr[i_int].Entities[j_int].Type.toLowerCase()) !== -1){
                    //skip no dups
                }else{
                    merged_construct.entity_str_arr.push(g_tag_data_arr[i_int].Entities[j_int].Type.toLowerCase());
                }
                //could do unique ?? maybe leave to JS front end
            }
            //everyone has one
            final_arr.push(merged_construct);
            // console.log(final_arr);
        }
        //as we added and recieved them in order
        //sentiment_data;
        g_workable_data_arr = final_arr;
    }

    function pushJSONToTemplate(){
 
        var 
            html_str = '';
        for(var i_int = 0; i_int < g_workable_data_arr.length; i_int += 1){      
            html_str +=     '<div>';
            html_str +=         '<span>' + g_workable_data_arr[i_int].product_id_str +'</span>';
            html_str +=         '<h2>' + g_workable_data_arr[i_int].review_headline_str + '</h2>';
            html_str +=         '<h3>' + g_workable_data_arr[i_int].sentiment_str + '</h3>';
            html_str +=         '<h4>' + g_workable_data_arr[i_int].star_rating_float.toString() + '</h4>';
            if(g_workable_data_arr[i_int].entity_str_arr){
                html_str +=         '<section>';
                for(var j_int = 0; j_int < g_workable_data_arr[i_int].entity_str_arr.length; j_int += 1){      
                    html_str +=             '<span>';
                    html_str +=                 g_workable_data_arr[i_int].entity_str_arr[j_int];
                    html_str +=             '</span>';
                }
                html_str +=         '</section>';
            }
            html_str +=         '<p>' + g_workable_data_arr[i_int].review_body_str + '</p>';
            html_str +=     '</div>'; 
        }
    
        // console.log("make html out of this please!", g_workable_data_arr);//, g_workable_data_arr);
        g_template_html = g_template_html.replace("REPLACE_ME", html_str);
    }
};




