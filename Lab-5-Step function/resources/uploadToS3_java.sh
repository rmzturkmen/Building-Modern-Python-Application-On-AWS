#!/bin/bash
bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs`
aws s3 cp ~/environment/java_8/sentiment-code/build/deployment-package/sentiment.zip s3://$bucket/sentiment.zip
#aws s3 cp ~/environment/java_8/tag-code/build/deployment-package/tag.zip s3://$bucket/tag.zip
#aws s3 cp ~/environment/java_8/publish-code/build/deployment-package/publish.zip s3://$bucket/publish.zip
