#!/bin/bash
bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs`
#Test
#echo $bucket
aws s3 cp ~/environment/java_8/get_reviews_code/build/deployment-package/get_reviews.zip s3://$bucket/get_reviews.zip
#aws s3 cp ~/environment/java_8/get_ratings_code/build/deployment-package/get_ratings.zip s3://$bucket/get_ratings.zip
#aws s3 cp ~/environment/java_8/create_report_code/build/deployment-package/create_report.zip s3://$bucket/create_report.zip
