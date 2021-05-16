#!/bin/bash
bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs`
aws s3 cp ~/environment/node_10.18.1/sentiment.zip s3://$bucket/sentiment.zip
#aws s3 cp ~/environment/node_10.18.1/tag.zip s3://$bucket/tag.zip
#aws s3 cp ~/environment/node_10.18.1/publish.zip s3://$bucket/publish.zip
