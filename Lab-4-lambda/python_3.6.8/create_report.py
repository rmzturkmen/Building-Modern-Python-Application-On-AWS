import boto3
import subprocess

client = boto3.client('lambda')
ROLE = 'arn:' + subprocess.getoutput('aws iam list-roles | grep role/lab4 | cut -f3- -d : | cut  --complement -c 44,45')
BUCKET = subprocess.getoutput('aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d "," | xargs')

response = client.create_function(
    FunctionName='create_report',
    Runtime='python3.8',
    Role=ROLE,
    Handler='create_report_code.lambda_handler',
    Code={
        'S3Bucket': BUCKET,
        'S3Key': 'create_report.zip'
    }
   
)

print ("DONE")