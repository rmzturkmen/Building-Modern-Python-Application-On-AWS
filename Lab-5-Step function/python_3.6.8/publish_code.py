
import boto3
import json
import subprocess

g_s3 = boto3.client("s3")


def lambda_handler(event, context):
    g_bucket_name_str = get_bucket_name()
    g_tag_data_arr = get_tag_data(g_bucket_name_str)
    g_sentiment_data_arr = get_sentiment_data(g_bucket_name_str)
    g_template_str = create_template()
    g_workable_data_arr = create_workable_data_structure(g_tag_data_arr, g_sentiment_data_arr)

    # print(g_sentiment_data_arr)
    # print(g_tag_data_arr)
    # print(g_template_str)
    # print(g_workable_data_arr)
    new_html_str = push_JSON_to_template(g_workable_data_arr)
    g_template_html = g_template_str.replace("REPLACE_ME", new_html_str)
    save_to_s3(g_bucket_name_str, g_template_html)
    return "done"

def get_bucket_name():
    return g_s3.list_buckets()["Buckets"][0]["Name"]

def get_tag_data(bucket_name_str):
    tag_data = g_s3.get_object(Bucket=bucket_name_str,Key="tag.json")
    return json.loads(tag_data["Body"].read())
    
def get_sentiment_data(bucket_name_str):
    sentiment_data = g_s3.get_object(Bucket=bucket_name_str,Key="sentiment.json")
    return json.loads(sentiment_data["Body"].read())

def create_template():
    html_str = ''
    html_str += '<!DOCTYPE html>'
    html_str += '<html>'
    html_str +=     '<head>'
    html_str +=         '<title>Report</title>'
    html_str +=         '<link rel="stylesheet" href="report.css" />'
    html_str +=     '</head>'
    html_str +=     '<body class="report">'
    html_str +=         '<main>'
    html_str +=             '<header>'
    html_str +=                 '<h1>Report</h1>'
    html_str +=             '</header>'
    html_str +=             '<section>'
    html_str +=                 "REPLACE_ME"
    html_str +=             '</section>'
    html_str +=         '</main>'
    html_str +=     '</body>'
    html_str += '</html>'
    return html_str

def create_workable_data_structure(g_tag_data_arr, g_sentiment_data_arr):
    final_arr = []
    merged_construct = {}
    # for i_int in range(0, len(g_tag_data_arr)):
    for i_int in range(len(g_tag_data_arr)):
        print(i_int)
        merged_construct = {};
        merged_construct["product_id_str"] =  g_sentiment_data_arr[i_int]["product_info"]["product_id"]
        merged_construct["review_headline_str"] =  g_sentiment_data_arr[i_int]["product_info"]["review_headline"]
        merged_construct["review_body_str"] =  g_sentiment_data_arr[i_int]["product_info"]["review_body"]
        merged_construct["star_rating_float"] = float(g_sentiment_data_arr[i_int]["product_info"]["star_rating"])
        #sentiment result
        merged_construct["sentiment_str"] = g_sentiment_data_arr[i_int]["Sentiment"].lower()
           
        if len(g_tag_data_arr[i_int]["Entities"]) == 0:
            print("no entities")
        else:
            # print("the else")
            merged_construct["entity_str_arr"] = []
        for j_int in range(len(g_tag_data_arr[i_int]["Entities"])): 
       
            # print(g_tag_data_arr[i_int]["Entities"][j_int])
            if g_tag_data_arr[i_int]["Entities"][j_int]["Type"].lower() in merged_construct["entity_str_arr"] :
            # if merged_construct["entity_str_arr"].index(g_tag_data_arr[i_int]["Entities"][j_int]["Type"].lower()) != -1 :
                print("skip no dups")
            else:
                print("unique")
                merged_construct["entity_str_arr"].append(g_tag_data_arr[i_int]["Entities"][j_int]["Type"].lower())
        final_arr.append(merged_construct);
    return final_arr

def push_JSON_to_template(g_workable_data_arr):
    html_str = ''
    for i_int in range(len(g_workable_data_arr)):
        html_str +=     '<div>';
        html_str +=         '<span>' + g_workable_data_arr[i_int]["product_id_str"] +'</span>'
        html_str +=         '<h2>' + g_workable_data_arr[i_int]["review_headline_str"] + '</h2>'
        html_str +=         '<h3>' + g_workable_data_arr[i_int]["sentiment_str"] + '</h3>'
        html_str +=         '<h4>' + str(g_workable_data_arr[i_int]["star_rating_float"]) + '</h4>'
        if "entity_str_arr" in g_workable_data_arr[i_int]:
            html_str +=         '<section>';
            for j_int in range(len(g_workable_data_arr[i_int]["entity_str_arr"])):
                html_str +=             '<span>'
                html_str +=                 g_workable_data_arr[i_int]["entity_str_arr"][j_int]
                html_str +=             '</span>'
            html_str +=         '</section>'
        html_str +=         '<p>' + g_workable_data_arr[i_int]["review_body_str"] + '</p>'
        html_str +=     '</div>'; 
    return html_str


def save_to_s3(bucket_name_str, html):
    response = g_s3.put_object(
        Bucket=bucket_name_str,
        Body=html,
        CacheControl='max-age=0',
        ContentType='text/html',
        Key='report.html',
    )
