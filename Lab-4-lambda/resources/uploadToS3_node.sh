#!/bin/bash
bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs`
#Test
#echo $bucket
aws s3 cp ~/environment/node_10.18.1/get_reviews.zip s3://$bucket/get_reviews.zip
#aws s3 cp ~/environment/node_10.18.1/get_ratings.zip s3://$bucket/get_ratings.zip
#aws s3 cp ~/environment/node_10.18.1/create_report.zip s3://$bucket/create_report.zip
