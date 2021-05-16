var 
    AWS_SDK = require("aws-sdk"),
    STATE = new AWS_SDK.StepFunctions({
       "apiVersion": "2016-11-23",
       "region": "us-west-2"
    });
exports.handler = function(event, context, callback){
    var 
        decoded = {},
        return_me = {
            "message_str": "Report processing, check your phone shortly"
        };

   (function init(){
        if(event.params && event.params.header && event.params.header.Authorization){
            decoded = JSON.parse(new Buffer.from(event.params.header.Authorization.replace("Bearer ", "").trim().split(".")[1], "base64").toString("binary"));
                console.log(decoded);
            return_me.cellphone_str = decoded.phone_number;
            return_me.name_str = decoded["cognito:username"];
            return_me.ipv4_str = event.params.header["X-Forwarded-For"];
            return_me.message_str = "Report Processed";
            if(process.env && process.env.STEP_ARN){
              spawnStepFunction(return_me, {
                  cellphone_str: return_me.cellphone_str,
                  ipv4_str: return_me.ipv4_str
              }, process.env.STEP_ARN);
            }else{
              console.log("testing in the console without a STEP function");
               callback(null, return_me);
            }
        }else{
            console.log("testing in the console without AUTH");
            callback(null, return_me);
        }
    })();
   

    async function spawnStepFunction(return_me, payload, arn_str){
        var 
            payload_str = JSON.stringify(payload),
            params = {
                stateMachineArn: arn_str,
                input: payload_str
            };
        console.log(await STATE.startExecution(params).promise());
        callback(null, return_me);
    }
};
