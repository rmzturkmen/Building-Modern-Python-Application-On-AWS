exports.handler = function(event, context, callback){
	var 
		result_boo = false,
		safe_cellphone_str = "+1<FMI_1>",
		safe_ipv4_str = "<FMI_2>";

     if(event.cellphone_str && event.ipv4_str){
     	if(event.cellphone_str === safe_cellphone_str && event.ipv4_str === safe_ipv4_str){
     		result_boo = true;
     	}else{
     		result_boo = false;
     	}
     }else{
     	result_boo = false;
     }

    if(result_boo === true){
        callback(null, true);
    }else{
        callback("invalid", null);
    }
};
