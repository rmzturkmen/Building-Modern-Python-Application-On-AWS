exports.handler = async function(event, context){
  var 
      decoded = {},
      return_me = {
          "message_str": "Report processing, check your phone shortly"
      };
  if(event.params && event.params.header && event.params.header.Authorization){
    decoded = JSON.parse(new Buffer(event.params.header.Authorization.replace("Bearer ", "").trim().split(".")[1], "base64").toString("binary"));
    console.log(decoded);
    return_me.cell_str = decoded.phone_number;
    return_me.name_str = decoded["cognito:username"];
    return_me.ipv4_str = event.params.header["X-Forwarded-For"];
    return_me.message_str = "Report Processing";
  }else{
    console.log("testing in the console without AUTH");
  }
  return return_me;
};
