
(async () => {
	var 
		definition = require("../resources/definition.json"),
		FS_PROMISE = require("fs").promises, //as we have v10
		AWS_SDK = require("aws-sdk"),
		STATE = new AWS_SDK.StepFunctions({
			apiVersion: "2016-11-23",
			region: "us-west-2"
		}),
		IAM = new AWS_SDK.IAM({}),
		ROLE_ARN_STR = "";
	
	async function getRoleName(){
		var roles_arr = (await IAM.listRoles().promise()).Roles;
		for(var i_int = 0; i_int < roles_arr.length; i_int += 1){
			if(roles_arr[i_int].RoleName === "lab5-states-role"){
				ROLE_ARN_STR = roles_arr[i_int].Arn;
				break;
			}
		}
	}
	function createStateMachine(){
		var 
			params = {
  				name: "Fancy-StateMachine", 
  				definition: JSON.stringify(definition),
  				roleArn: ROLE_ARN_STR
 			};
 		return STATE.createStateMachine(params).promise();
	}
	(async function init(){
		await getRoleName();
		console.log(ROLE_ARN_STR);
		await createStateMachine();
		console.log("Done");
	})();
})();