(async () => {
	var 
		FS_PROMISE = require("fs").promises, //as we have v10
		AWS_SDK = require("aws-sdk"),
		S3_API = new AWS_SDK.S3({
			apiVersion: "2006-03-01",
			region: "us-west-2"
		}),
		LAMBDA = new AWS_SDK.Lambda({
			apiVersion: "2015-03-31",
			region: "us-west-2"
		}),
		IAM = new AWS_SDK.IAM({
			apiVersion: "2010-05-08"
		}),
		BUCKET_NAME_STR = "",
		ROLE_NAME_STR = "";

	async function getBucketName(){
		var 
			buckets_arr = (await S3_API.listBuckets().promise()).Buckets;
		for(var i_int = 0; i_int < buckets_arr.length; i_int += 1){
			if(buckets_arr[i_int].Name.indexOf("s3bucket") !== -1){
				BUCKET_NAME_STR = buckets_arr[i_int].Name;
				break;
			}
		}
	}
	
	async function getRoleName(){
		var roles_arr = (await IAM.listRoles().promise()).Roles;
		// console.log(roles_arr);
		for(var i_int = 0; i_int < roles_arr.length; i_int += 1){
			// console.log(roles_arr[i_int].Arn);
			if(roles_arr[i_int].RoleName === "lab4-lambda-role"){
				ROLE_NAME_STR = roles_arr[i_int].Arn;
				break;
			}
		}
	}

	function createLambdaFunction(){
		var 
			params = {
				Code: {
					S3Bucket: BUCKET_NAME_STR, 
					S3Key: "get_reviews.zip"
				},
  				FunctionName: "get_reviews", 
  				Handler: "get_reviews_code.handler",
  				Publish: true, 
  				Role: ROLE_NAME_STR, 
  				Runtime: "nodejs10.x"
 			};
 		return LAMBDA.createFunction(params).promise();
	}

	(async function init(){
		await getBucketName();
		await getRoleName();
		await createLambdaFunction(); //throw away response
		console.log("Done");
	})();

})();