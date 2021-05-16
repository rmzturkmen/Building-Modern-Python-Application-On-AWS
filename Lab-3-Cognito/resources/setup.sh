#!/bin/bash 
sudo pip3 install boto3
echo ''
echo Please enter a valid IP address:
read ip_address
echo IP address:$ip_address
bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs`
echo export bucket=`aws s3api list-buckets --query "Buckets[].Name" | grep s3bucket | tr -d ',' | sed -e 's/"//g' | xargs` >> /home/ec2-user/.bashrc
echo export bucket_url="https://${bucket}.s3-us-west-2.amazonaws.com/index.html" >> /home/ec2-user/.bashrc
FILE_PATH="/home/ec2-user/environment/resources/public_policy.json"
FILE_PATH_2="/home/ec2-user/environment/resources/permissions.py"
aws s3 cp --cache-control max-age=0 --content-type image/jpg ~/environment/resources/website/backdrop_camera.jpg s3://$bucket/backdrop_camera.jpg 
aws s3 cp --cache-control max-age=0 --content-type text/html ~/environment/resources/website/callback.html s3://$bucket/callback.html
aws s3 cp --cache-control max-age=0 --content-type application/javascript ~/environment/resources/website/config.js s3://$bucket/config.js
aws s3 cp --cache-control max-age=0 --content-type text/css ~/environment/resources/website/core.css s3://$bucket/core.css
aws s3 cp --cache-control max-age=0 --content-type application/javascript ~/environment/resources/website/flex_search.js s3://$bucket/flex_search.js
aws s3 cp --cache-control max-age=0 --content-type text/html ~/environment/resources/website/index.html s3://$bucket/index.html
aws s3 cp --cache-control max-age=0 --content-type application/javascript ~/environment/resources/website/jquery.js s3://$bucket/jquery.js
aws s3 cp --cache-control max-age=0 --content-type image/png ~/environment/resources/website/kiosk.png s3://$bucket/kiosk.png
aws s3 cp --cache-control max-age=0 --content-type image/png ~/environment/resources/website/kiosk_bottom.png s3://$bucket/kiosk_bottom.png
aws s3 cp --cache-control max-age=0 --content-type image/png ~/environment/resources/website/kiosk_left.png s3://$bucket/kiosk_left.png
aws s3 cp --cache-control max-age=0 --content-type image/png ~/environment/resources/website/kiosk_right.png s3://$bucket/kiosk_right.png
aws s3 cp --cache-control max-age=0 --content-type image/png ~/environment/resources/website/kiosk_top.png s3://$bucket/kiosk_top.png
aws s3 cp --cache-control max-age=0 --content-type text/css ~/environment/resources/website/main.css s3://$bucket/main.css
aws s3 cp --cache-control max-age=0 --content-type application/javascript ~/environment/resources/website/main.js s3://$bucket/main.js
aws s3 cp --cache-control max-age=0 --content-type application/javascript ~/environment/resources/website/products.js s3://$bucket/products.js
aws s3 cp --cache-control max-age=0 --content-type text/html ~/environment/resources/website/report.html s3://$bucket/report.html
aws s3 cp --cache-control max-age=0 --content-type text/css ~/environment/resources/website/reset.css s3://$bucket/reset.css
aws s3 cp --cache-control max-age=0 --content-type text/css ~/environment/resources/website/search.css s3://$bucket/search.css
aws s3 cp --cache-control max-age=0 --content-type application/javascript ~/environment/resources/website/search.js s3://$bucket/search.js
sed -i "s/<FMI_1>/$bucket/g" $FILE_PATH
sed -i "s/<FMI_2>/$ip_address/g" $FILE_PATH
sed -i "s/<FMI_3>/$ip_address/g" $FILE_PATH
sed -i "s/<FMI>/$bucket/g" $FILE_PATH_2
python3 /home/ec2-user/environment/resources/permissions.py