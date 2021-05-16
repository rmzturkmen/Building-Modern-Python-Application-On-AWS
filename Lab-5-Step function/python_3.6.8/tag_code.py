import boto3
import json
import subprocess

_every_list = []
g_comp = boto3.client('comprehend')
g_s3 = boto3.client('s3')

def lambda_handler(event, context):
    g_bucket_name_str = get_bucket_name()
    S3_BUCKET = 'aws-tc-largeobjects'
    S3_FILE = 'DEV-AWS-MO-Building_2.0/my_json_lines.jsonl'
    all_records_list = s3_selection(S3_BUCKET, S3_FILE)
    safe_list = clean_dis(all_records_list)
    print(len(safe_list))
    comprehend_dis(0, 25, len(safe_list), safe_list)
    saveToS3(g_bucket_name_str)
    return "Done"

def get_bucket_name():
    return g_s3.list_buckets()["Buckets"][0]["Name"]

def s3_selection(S3_BUCKET, S3_FILE):
    all_records_list = []
    result_list = []
    helper_format_str = ""
    r = g_s3.select_object_content(
        Bucket=S3_BUCKET,
        Key=S3_FILE,
        ExpressionType='SQL',
        Expression="select s.product_id, s.review_headline, s.review_body, s.star_rating from s3object[*] s",
        InputSerialization={'JSON': {"Type": "Lines"}},
        OutputSerialization={'JSON': {}}
    )


    for event in r['Payload']:
        if 'Records' in event:
            helper_format_str = event['Records']['Payload'].decode('utf-8')
            for line in helper_format_str.splitlines():
                # print(line)
                all_records_list.append(line)
    for records_chunk_str in all_records_list:
        try:
            result_list.append(json.loads(records_chunk_str))
        except:
            print("problem with entry")
    return result_list


def clean_dis(all_records_list):
    result_list = []
    for record in all_records_list:
        try:
            if len(record["review_body"]) < 4800:
                result_list.append(record)
        except KeyError:
            print("Key issue")
    return result_list


def comprehend_dis(start_int, end_int, length_int, safe_list):
    print("Next 25 items")
    my_list = []
    for i_int in range(start_int, end_int):
        my_list.append(safe_list[i_int]["review_body"])
    entities = g_comp.batch_detect_entities(TextList=my_list,LanguageCode='en')
    for j_int in range(len(entities["ResultList"])):
        # print(start_int, end_int)

        every_item = {}
        every_item["Entities"] = entities["ResultList"][j_int]["Entities"]
        every_item["product_info"] = {}
        every_item["product_info"]["product_id"] = safe_list[start_int]["product_id"]
        every_item["product_info"]["star_rating"] = safe_list[start_int]["star_rating"]
        every_item["product_info"]["review_body"] = safe_list[start_int]["review_body"]
        every_item["product_info"]["review_headline"] = safe_list[start_int]["review_headline"]
        _every_list.append(every_item)
        start_int += 1
        end_int += 1
    # start_int += 25;
    # end_int += 25;

    if end_int > length_int:
        print("done")
    else:
        comprehend_dis(start_int, end_int, length_int, safe_list)


def saveToS3(BUCKET):
    client = boto3.client('s3')
    response = client.put_object(
        Bucket=BUCKET,
        Body=json.dumps(_every_list),
        CacheControl='max-age=0',
        ContentType='application/json',
        Key='tag.json',
    )
