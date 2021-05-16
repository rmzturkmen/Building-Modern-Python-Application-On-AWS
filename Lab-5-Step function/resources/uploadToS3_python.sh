#!/bin/bash
bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs`
aws s3 cp ~/environment/python_3.6.8/sentiment.zip s3://$bucket/sentiment.zip
#aws s3 cp ~/environment/python_3.6.8/tag.zip s3://$bucket/tag.zip
#aws s3 cp ~/environment/python_3.6.8/publish.zip s3://$bucket/publish.zip
