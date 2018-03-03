/**
 * Created by csy on 2017/8/16.
 */

var validate;

function goback(){
    window.history.back();
}
$(function(){
    $.validator.defaults.ignore = "";
    //表单验证规则
    validate = $("#mainForm").validate({
        rules: {
            product_code:{
                required:true,
                minlength:1,
                maxlength:30
            },
            product_name:{
                required:true,
                minlength:1,
                maxlength:30
            },
            product_desc: {
                required:true,
                minlength:1,
                maxlength:30
            },
            active_period:{
                required:true,
                digits:true,
                min:0,
                max:365
            },
            quantity:{
                required:true,
                digits:true,
                min:1,
                max:1000000
            },
            product_price:{
                required:true
            },
            product_price:{
                required:true
            },
            product_cost:{
                required:true
            }
        },
        messages: {
            product_code:{
                required:"请输入产品代码",
                minlength:"请输入长度为1~30的字符",
                maxlength:"请输入长度为1~30的字符"
            },
            product_name:{
                required:"请输入产品名字",
                minlength:"请输入长度为1~30的字符",
                maxlength:"请输入长度为1~30的字符"
            },
            product_desc:{
                required:"请输入产品描述",
                minlength:"请输入长度为1~30的字符",
                maxlength:"请输入长度为1~30的字符"
            },
            active_period: {
                required:"请输入有效期",
                digits:"请输入整数，且有效期范围0~365天",
                max:"有效期范围0~365天",
                min:"有效期范围0~365天"
            },
            quantity: {
                required:"请输入数量",
                digits:"请输入整数，且数量范围1~1000000",
                max:"数量范围1~1000000",
                min:"数量范围1~1000000"
            },
            product_price:{
                required:"请输入产品定价"
            },
            product_cost:{
                required:"请输入产品成本"
            }
        }
})

    // 产品定价校验
    jQuery.validator.addMethod("isPrice", function(value, element) {
        var product_type = $("#product_type").val();

        if(product_type != "2"){
            if(isNaN(value)){
                $(element).data('error-msg',"请输入范围为0~100000数值");
                return false;
            }else if((value < 0) || (value > 100000)){
                $(element).data('error-msg',"请请输入范围为0~100000数值");
                return false;
            }else if(!checkDecimalPlace(value)){
                $(element).data('error-msg',"请小数点后不能超过4位");
                return false
            }

        }
        return true;
    }, function(params, element) {
        return $(element).data('error-msg');
    });
    jQuery.validator.addMethod("isCost", function(value, element) {
        var product_type = $("#product_type").val();
        if(product_type == "2"){
            if(value == "" || isNaN(value) ){
                $(element).data('error-msg',"请输入范围为0~1的小数");
                return false;
            }else if((value <= 0) || (value > 1)){
                $(element).data('error-msg',"请输入范围为0~1的小数");
                return false;
            }else if(!checkDecimalPlace(value)){
                $(element).data('error-msg',"小数点后不能超过4位");
                return false;
            }
        }else{
            if(isNaN(value)){
                $(element).data('error-msg',"请输入范围为0~100000数值");
                return false;
            }else if((value < 0) || (value > 100000)){
                $(element).data('error-msg',"请输入范围为0~100000数值");
                return false;
            }else if(!checkDecimalPlace(value)){
                $(element).data('error-msg',"小数点后不能超过4位");
                return false;
            }
        }

        return true;
    }, function(params, element) {
        return $(element).data('error-msg');
    });

    productTypeFun();
});


var validate_flag = 1;
var operator_code_flag = 1;
var product_type_flag = 1;

var product_price_flag = 1;
var product_cost_flag = 1;


$("#operator_code").change(function(){
    var operator_code = $("#operator_code").val();

    if(operator_code == ''){
        $('#operator_code-error').html("请选择运营商");
        $('#operator_code-error').show();
        operator_code_flag = 0;
    }else{
        $('#operator_code-error').hide();
        product_type_flag = 1;
    }
});
$("#operator_code").change(function(){
    operatorCodeFun();
})

$("#product_type").change(function(){
    productTypeFun();
});

$("#area_code").change(function(){
    areaCodeFun();
})
function productTypeFun(){
    var product_type = $("#product_type").val();
    var operator_code = $("#operator_code").val();
    var area_code = $("#area_code").val();

    if(product_type == ''){
        $('#product_type-error').html("请选择产品类型");
        $('#product_type-error').show();
        product_type_flag = 0;
    }else{
        $('#product_type-error').hide();
        product_type_flag = 1;
    }

    //如果产品类型是国际，则产品定价是1，并且不可编辑，
    //产品成本价是0~1的小数
    if(product_type == '2'){
        $("#product_price").val("1");
        $("#product_price").attr("readonly","readonly");

        //选择国际短信 区域默认为国际，运营商默认为国际
        $("#operator_code").find("option[value=4]").prop('selected',true);
        $("#area_code").find("option[value=1]").prop('selected',true);

        $("input[name='operator_code']").val('4');
        $("input[name='area_code']").val('1');

    }else {
        //如果产品类型非国际，恢复对产品定价的编辑
        $("#product_price").removeAttr("readonly");
    }
    if(operator_code == '4' && product_type != '2'){

        $("#operator_code").find("option[value=0]").prop('selected',true);
        $("input[name='operator_code']").val('0');

    }
    if(area_code == '1' && product_type != '2'){
        $("#area_code").find("option[value=0]").prop('selected',true);
        $("input[name='area_code']").val('0');
    }
}

function operatorCodeFun(){
    var product_type = $("#product_type").val();
    var operator_code = $("#operator_code").val();
    var area_code = $("#area_code").val();

    $("input[name='operator_code']").val(operator_code);

    //如果产品类型是国际，则产品定价是1，并且不可编辑，
    if(operator_code == '4'){
        $("#product_price").val("1");
        $("#product_price").attr("readonly","readonly");
        //选择国际运营商 区域默认为国际，产品类型默认为国际
        $("#product_type").find("option[value=2]").prop('selected',true);

        $("#area_code").find("option[value=1]").prop('selected',true);
        $("input[name='area_code']").val('1');
    }

    if(product_type == '2' && operator_code != '4'){
        $("#product_type").find("option[value=3]").prop('selected',true);
        //如果产品类型非国际，恢复对产品定价的编辑
        $("#product_price").removeAttr("readonly");
    }

    if(area_code == '1' && operator_code != '4'){
        $("#area_code").find("option[value=0]").prop('selected',true);
        $("input[name='area_code']").val('0');
    }
}
function areaCodeFun(){
    var product_type = $("#product_type").val();
    var operator_code = $("#operator_code").val();
    var area_code = $("#area_code").val();

    //如果产品类型是国际，则产品定价是1，并且不可编辑，
    if(area_code == '1'){
        $("#product_price").val("1");
        $("#product_price").attr("readonly","readonly");
        //选择国际运营商 区域默认为国际，产品类型默认为国际
        $("#product_type").find("option[value=2]").prop('selected',true);

        $("#operator_code").find("option[value=4]").prop('selected',true);
        $("input[name='operator_code']").val('4');
    }


    if(product_type == '2' && area_code != '1'){
        $("#product_type").find("option[value=3]").prop('selected',true);
        //如果产品类型非国际，恢复对产品定价的编辑
        $("#product_price").removeAttr("readonly");
    }

    if(operator_code == '4' && area_code != '1'){
        $("#operator_code").find("option[value=0]").prop('selected',true);
        $("input[name='operator_code']").val('0');
    }
}
function checkDecimalPlace(input_val){
    //判断小数位数是否超过3位
    var num = input_val.toString();
    if(num.split(".").length > 1){
        var dec = num.split(".")[1];
        var decimalPlace = new Number(dec.length);
        if(decimalPlace > 4){
            return false;
        }
    }
    return true;
}





var msgTime = 1500;

function edit(btn){
    $("#msg").hide();

    if(!validate.form()){
        validate_flag = 0;
    }else{
        validate_flag = 1;
    }



    if(validate_flag == 0 || product_price_flag ==0 || product_cost_flag == 0){
        return;
    }

    var options = {
        beforeSubmit : function() {
            $(btn).attr("disabled", true);
        },
        success : function(data) {
            $(btn).attr("disabled", false);
            if(data.success){
                layer.msg(data.msg, {icon: 1,time: msgTime});
                window.setTimeout(function(){
                    location.href = $("#url").val();
                }, msgTime);

            }else{
                layer.msg(data.msg, {icon: 2,time: msgTime});
            }
        },
        url : $("#ajaxurl").val(),
        type : "post",
        timeout:30000
    };


    layer.confirm("确定修改产品包？", function(index){
        $("#mainForm").ajaxSubmit(options);
        layer.close(index);
    });

};

function save(btn){
    $("#msg").hide();

    if(!validate.form()){
        validate_flag = 0;
    }else{
        validate_flag = 1;
    }


    if(validate_flag == 0 || product_price_flag ==0 || product_cost_flag == 0){
        return;
    }

    var options = {
        beforeSubmit : function() {
            $(btn).attr("disabled", true);
        },
        success : function(data) {
            $(btn).attr("disabled", false);
            if(data.success){
                layer.msg(data.msg, {icon: 1,time: msgTime});
                window.setTimeout(function(){
                    location.href = $("#url").val();
                }, msgTime);

            }else{
                layer.msg(data.msg, {icon: 2,time: msgTime});
            }
        },
        url : $("#ajaxurl").val(),
        type : "post",
        timeout:30000
    };


    layer.confirm("确定创建产品包？", function(index){
        $("#mainForm").ajaxSubmit(options);
        layer.close(index);
    });

};