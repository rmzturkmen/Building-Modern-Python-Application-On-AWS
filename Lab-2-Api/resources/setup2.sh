#!/bin/bash
bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs`
#echo $bucket
aws s3 cp ~/environment/resources/website/config.js s3://$bucket/config.js
