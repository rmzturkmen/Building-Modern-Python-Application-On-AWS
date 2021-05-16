import boto3
import json
import statistics
import re

def lambda_handler(event, context):
    PRODUCT = event['product_id_str']
    S3_BUCKET = 'aws-tc-largeobjects'
    S3_FILE = 'DEV-AWS-MO-Building_2.0/my_json_lines.jsonl'

    s3 = boto3.client('s3')

    r = s3.select_object_content(
            Bucket=S3_BUCKET,
            Key=S3_FILE,
            ExpressionType='SQL',
            Expression="select s.star_rating from s3object[*] s where s.product_id = '" + PRODUCT + "'",
            InputSerialization={'JSON': {"Type": "Lines"}},
            OutputSerialization={'JSON': {}}
    )

    for event in r['Payload']:
        if 'Records' in event:
            records = event['Records']['Payload'].decode('utf-8')
            n_records = (re.findall(r'\d+', records))
            results = list(map(float, n_records))
            f_results = statistics.mean(results)
            
    return {
        'product_id_str': PRODUCT,
        'average_star_review_float': f_results
    }