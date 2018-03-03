/**
 * Created by csy on 2017/8/16.
 * 从页面搬运过来，统一处理
 */
var validate;
var msgTime = 1500;
$(function(){
    $.validator.defaults.ignore = "#unit_price";
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
                maxlength:30
            },
            product_type:{
                required:true
            },
            operator_code:{
                required:true
            },
            area_code:{
                required:true
            },
            status:{
                required:true
            },
            unit_price: {
                required: true,
                number:true,
                min:0,
                max:1000000,
                unitPriceVal: true
            },
            remark: {
                maxlength: 100
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
                required:"必须选择一项",
                maxlength:"请输入长度为0~30的字符"
            },
            product_type:{
                required:"必须选择一项"
            },
            operator_code:{
                required:"必须选择一项"
            },
            area_code:{
                required:"必须选择一项"
            },
            status:{
                required:"必须选择一项"
            },
            unit_price: {
                required: '请输入金额',
                number:"请输入数字",
                max:"请按规则输入范围（0-1,000,000）元金额",
                min:"请按规则输入范围（0-1,000,000）元金额"
            },
            remark: {
                maxlength:"请输入长度为0~100的字符"
            }
        }
    });

    // 域名校验
    jQuery.validator.addMethod("unitPriceVal", function(value, element) {
        var tel = /^([1-9]\d*|0)(\.\d{1,4})?$/;
        return this.optional(element) || (tel.test(value));
    }, "小数点后限制4位");

    var unit_price_flag = 1;
    var product_type_flag = 1;
    $("#product_type").change(function(){
        var product_type = $("#product_type").val();

        if(product_type == ''){
            $('#product_type-error').html("请选择产品类型");
            $('#product_type-error').show();
            product_type_flag = 0;
        }else{
            $('#product_type-error').hide();
            product_type_flag = 1;
        }

        // 如果产品类型是国际，则产品定价是1，并且不可编辑
        // 产品成本价是0~1的小数
        if(product_type == '2'){

            $("#unit_price").val("");
            $('#unit_price').attr("readonly","readonly");
            $('#unit_price-error').hide();
            $('#inter_discount_div').show();

            $("#operator_code").find("option[value=4]").prop('selected',true);
            $("#area_code").find("option[value=1]").prop('selected',true);

            $("input[name='operator_code']").val('4');
            $("input[name='area_code']").val('1');
        }else{
            $("#unit_price").removeAttr("readonly");
            $('#unit_price-error').show();
            $('#inter_discount_div').hide();

            $("#operator_code").find("option[value=0]").prop('selected',true);
            $("#area_code").find("option[value=0]").prop('selected',true);

            $("input[name='operator_code']").val('0');
            $("input[name='area_code']").val('0');
        }
    });


    $("#unit_price").keyup(function(){
        var product_type = $("#product_type").val();
        var unit_price = $("#unit_price").val();
        if(product_type != "2"){
            checkUnitPriceForNotInter(unit_price);
        }

    });
    $("#area_code").change(function(){
        areaCodeFun();
    })
    $("#operator_code").change(function(){
        operatorCodeFun();
    })
    function operatorCodeFun(){
        var product_type = $("#product_type").val();
        var operator_code = $("#operator_code").val();
        var area_code = $("#area_code").val();

        $("input[name='operator_code']").val(operator_code);

        //如果产品类型是国际，则产品定价是1，并且不可编辑，
        if(operator_code == '4'){
            $("#product_price").val("1");
            $("#product_price").attr("readonly","readonly");
            $("#unit_price").val("");
            $('#unit_price').attr("readonly","readonly");
            $('#inter_discount_div').show();

            //选择国际运营商 区域默认为国际，产品类型默认为国际
            $("#product_type").find("option[value=2]").prop('selected',true);

            $("#area_code").find("option[value=1]").prop('selected',true);
            $("input[name='area_code']").val('1');
        }

        if(product_type == '2' && operator_code != '4'){
            $("#product_type").find("option[value=3]").prop('selected',true);
            //如果产品类型非国际，恢复对产品定价的编辑
            $("#product_price").removeAttr("readonly");
            $("#unit_price").removeAttr("readonly");
            $('#inter_discount_div').hide();

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
            $("#unit_price").val("");
            $('#unit_price').attr("readonly","readonly");
            $('#inter_discount_div').show();
            //选择国际运营商 区域默认为国际，产品类型默认为国际
            $("#product_type").find("option[value=2]").prop('selected',true);

            $("#operator_code").find("option[value=4]").prop('selected',true);
            $("input[name='operator_code']").val('4');
        }


        if(product_type == '2' && area_code != '1'){
            $("#product_type").find("option[value=3]").prop('selected',true);
            //如果产品类型非国际，恢复对产品定价的编辑
            $("#product_price").removeAttr("readonly");
            $("#unit_price").removeAttr("readonly");

            $('#inter_discount_div').hide();

        }

        if(operator_code == '4' && area_code != '1'){
            $("#operator_code").find("option[value=0]").prop('selected',true);
            $("input[name='operator_code']").val('0');
        }
    }

})

//非国际商品，校验成本价
function checkUnitPriceForNotInter(unit_price){
    unit_price = parseFloat(unit_price);
    if(isNaN(unit_price)){
        $('#unit_price-error').html("请输入数字");
        $('#unit_price-error').show();
        unit_price_flag = 0;
    }
    else if(unit_price < 0){
        $('#unit_price-error').html("数值不能为负");
        $('#unit_price-error').show();
        unit_price_flag = 0;
    }else if(unit_price > 1000000){
        $('#unit_price-error').html("请按规则输入范围（0-1,000,000）元金额");
        $('#unit_price-error').show();
        unit_price_flag = 0;
    }
    else{
        var reg = /^([1-9]\d*|0)(\.\d{1,4})?$/;
        if(!reg.test(unit_price)){
            $('#unit_price-error').html("小数点后限制4位");
            $('#unit_price-error').show();
            unit_price_flag = 0;
        }else{
            $('#unit_price-error').hide();
            unit_price_flag = 1;
        }
    }
}
function save(btn) {
    if (!validate.form()) {
        return;
    }
    var unit_price_flag = 1;
    //验证产品定价和成本价
    var product_type = $("#product_type").val();
    var product_price = $("#product_price").val();
    var unit_price = $("#unit_price").val();
    // 非国际
    if(product_type != "2"){ checkUnitPriceForNotInter(unit_price);}

    if(unit_price_flag == 0){ return;}
    var saveUrl =  $("#ajaxurl").val();
    var options = {
        beforeSubmit: function () {
            $(btn).attr("disabled", true);
        },
        success: function (data) {
            layer.closeAll(); //疯狂模式，关闭所有层
            $(btn).attr("disabled", false);
            if (data == "" || data == null) {
                return; //session超时
            }
            if (data.success) {
                layer.msg(data.msg, {icon: 1, time: msgTime}, function () {
                    var mainUrl = $("#backurl").val();
                    window.location.href = mainUrl;
                });
            } else {
                layer.msg(data.msg, {
                    icon: 2
                });
            }
        },
        url: saveUrl,
        type: "post"
    };

    layer.confirm("确定创建产品包？", function(index){
        $("#mainForm").ajaxSubmit(options);
        layer.close(index);
    });
}
function edit(btn) {
    if (!validate.form()) {
        return;
    }

    //验证产品定价和成本价
    var product_type = $("#product_type").val();
    var product_price = $("#product_price").val();
    var unit_price = $("#unit_price").val();
    // 非国际
    if(product_type != "2"){
        checkUnitPriceForNotInter(unit_price);
        if(unit_price_flag == 0){ return;}
    }


    var saveUrl = $("#ajaxurl").val();
    var options = {
        beforeSubmit: function () {
            $(btn).attr("disabled", true);
        },
        success: function (data) {
            layer.closeAll(); //疯狂模式，关闭所有层
            $(btn).attr("disabled", false);
            if (data == "" || data == null) {
                return; //session超时
            }
            if (data.success) {
                layer.msg(data.msg, {icon: 1, time: msgTime}, function () {
                    var mainUrl = $("#backurl").val();
                    window.location.href = mainUrl;
                });
            } else {
                layer.msg(data.msg, {
                    icon: 2
                });
            }
        },
        url: saveUrl,
        type: "post"
    };

    layer.confirm("确定修改产品包？", function(index){
        $("#mainForm").ajaxSubmit(options);
        layer.close(index);
    });
}