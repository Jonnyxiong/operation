/**
 * Created by caosiyuan on 2017/7/13.
 */
$(function(){
    // $.ajax({
    //     url : '/accountInfo/client/add/data',
    //     type : "GET",
    //     success : function (res) {
    //         if(res.code != "0"){
    //             return;
    //         }
    //         for(var i = 0; i < res.data.agentInfoList.length; i++){
    //             var k = res.data.agentInfoList[i].agentId;
    //             var v = res.data.agentInfoList[i].agentName;
    //             $(".js-agent").append("<option value="+k+">"+v+"</option>");
    //         }
    //         for(var i = 0; i < res.data.saleList.length; i++){
    //             var k = res.data.saleList[i].id;
    //             var v = res.data.saleList[i].name;
    //             $(".js-sale").append("<option value="+k+">"+v+"</option>");
    //         }
    //     }
    // })
    var paytype = '';
    var val = $("#smsHttp").val();
    var smType = $("#smsfrom").val();
    if(smType != 6){
        $(".grid-smstype").removeClass("hide");
        $(".grid-smstype-http").addClass("hide");
    } else {
        $(".grid-smstype").addClass("hide");
        $(".grid-smstype-http").removeClass("hide");
    }
    if(val == 2){
        $(".grid-yz").hide();
        $(".grid-yx").hide();
        $(".grid-tz").hide();
        $(".grid-sms-http-type").removeClass("hide");
        priceShowHttp();
    }
    //选中后付费，出现单价
    $(".js-houfu").click(function(){
        $(".paytype").val($(this).val())
        if($("#smsfrom").val() == 6 && $("#smsHttp").val() != 2){
            $(".sms-price").show();
            $(".grid-price").show();
        } else {
            priceShow()
        }


    });
    //选中预付费，单价消失
    $(".js-yufu").click(function(){
        $(".sms-price").hide();
        $(".sms-price").find("input").val("");
        $(".paytype").val($(this).val())
    });
    //是否支持子账户
    $(".js-extValue").click(function(){
        $(".extValue").val($(this).val());
    })
    //是否自扩展
    $(".js-extend").click(function(){
        $(".needextend").val($(this).val())
        $(".grid-extend").removeClass('hide');
    })
    $(".js-notextend").click(function(){
        $(".needextend").val($(this).val());
        $(".grid-extend").addClass("hide");
        $(".grid-extend").val("");
    })

    //短信协议
    $("#smsfrom").change(function(){
        var value = $(this).val();

        //HTTPS不用选择短信类型
        if(value != 6){
            $(".grid-smstype").removeClass("hide");
            $(".grid-smstype-http").addClass("hide");

        } else {
            $(".grid-smstype").addClass("hide");
            $(".grid-smstype-http").removeClass("hide");

        }

        //http和sgip出现的东西不一样
        if(value == 6){
            $(".grid-sgip").addClass("hide");
            $(".grid-sgip").find("input").val("");

            $(".grid-https").removeClass("hide");
        } else if(value == 4){
            $(".grid-https").addClass("hide");
            $(".grid-https").find('input').val("");

            $(".grid-sgip").removeClass("hide");
        } else {
            $(".grid-https").addClass("hide");
            $(".grid-sgip").addClass("hide");

            $(".grid-https").find('input').val("");
            $(".grid-sgip").find("input").val("");
        }


        //显示单价 选中http 并且选中后付费
        var paytype = $(".paytype").val();
        if(paytype == 1 && value == 6){
            $(".sms-price").show();
            $(".grid-price").show();

            $(".js-price").val("");
        } else {
            priceShow();
        }
    })
    //http子协议
    $("#smsHttp").change(function(){
        var value = $(this).val();
        priceShowHttp();
        //HTTPS不用选择短信类型
        if(value != 2){
            $(".grid-sms-http-type").addClass('hide');
            $(".grid-yz").show();
            $(".grid-yx").show();
            $(".grid-tz").show();
        } else {
            $(".grid-sms-http-type").removeClass('hide');
        }

    })
    $(".js-smstype").change(function(){
        priceShow();
    })
    //选择http自协议出现的短信类型变化
    $(".jsmstype").change(function(){
        //先清空以前的值
        $(".js-price").val("");
        priceShowHttp()
    })
    //状态报告获取方式切换
    $(".js-needreport").change(function(){
        var needreport = $(this).val();

        if(needreport == 0 || needreport == 3){
            $(".grid-deliveryurl").addClass("hide");
            $(".grid-deliveryurl").find("input").val("");
        } else {
            $(".grid-deliveryurl").removeClass("hide");
        }
    });
    //上行获取方式切换
    $(".js-needmo").change(function(){
        var needmo = $(this).val();

        if(needmo == 0 || needmo == 3){
            $(".grid-mourl").addClass("hide");
            $(".grid-mourl").find("input").val("");
        } else {
            $(".grid-mourl").removeClass("hide");
        }
    })
    function priceShow(){
        var  val = $(".js-smstype").val();
        if($("#smsfrom").val() == 6){
            val = $(".jsmstype").val();
        }else {
            val = $(".js-smstype").val();
        }
        var paytype = $(".paytype").val();

        $(".grid-price").hide();
        $(".js-price").val("");
        if(val == "0" && paytype == 1){
            $(".sms-price").show();
            $(".grid-tz").show();
        } else if(val == 5 && paytype == 1){
            $(".sms-price").show();
            $(".grid-yx").show();
        } else if(val == 4 && paytype == 1){
            $(".sms-price").show();
            $(".grid-yz").show();
        }
    }

    function priceShowHttp(){
        var val = $(".jsmstype").val();
        var paytype = $(".paytype").val();

        $(".grid-price").hide();
        // $(".js-price").val("");
        if(val == "0" && paytype == 1){
            $(".sms-price").show();
            $(".grid-tz").show();
        } else if(val == 5 && paytype == 1){
            $(".sms-price").show();
            $(".grid-yx").show();
        } else if(val == 4 && paytype == 1){
            $(".sms-price").show();
            $(".grid-yz").show();
        }
    }

    //提交
    var submit_flag = false;
    $(".js-confirm").click(function(){
        // 获取旧的价格信息
        var oldUserPriceList = JSON.parse($("#oldUserPriceList").val());

        var params = {};
        $(".js-key").each(function(){
            var value = $(this).val();
            var key   = $(this).data("key");
            params[key] = value;
        })
        if(params.smsfrom == 6){
            params.smstype = $(".jsmstype").val()
        }else {
            params.smstype = $(".js-smstype").val()
        }


        if(_Auth.isNull(params.name)){
            layer.msg('客户名称不能为空', {icon: 2});
            return;
        }
        if(_Auth.isNull(params.name.trim())){
            layer.msg('客户名称不能为空字符', {icon: 2});
            return;
        }
        if(!_Auth.isUsername(params.name)){
            layer.msg('客户名称不能超过20个字符', {icon: 2});
            return;
        }
        /*

         */

        if(!_Auth.isNull(params.email) && !_Auth.isEmail(params.email)){
            layer.msg('邮箱格式不正确', {icon: 2});
            return;
        }
        if(!_Auth.isNull(params.mobile) && !_Auth.isMobile(params.mobile)){
            layer.msg('手机号码格式不正确', {icon: 2});
            return;
        }
        if(!_Auth.isNull(params.address) && !_Auth.isAddress(params.address)){
            layer.msg('联系地址长度不能大于50个字符', {icon: 2});
            return;
        }
        /*
        短信单价和类型的处理
         */
        var yz_price = $(".yz-price").val(),
            tz_price = $(".tz-price").val(),
            yx_price = $(".yx-price").val();
        //http
        if(params.smsfrom == 6 && params.httpProtocolType != 2){
            if(params.paytype == 1){
                var flag = false;
                $(".js-price").each(function(){
                    var price = $(this).val();
                    var tip   = $(this).data("str");
                    if(!_Auth.isPrice(price)){
                        flag = true;
                        layer.msg(tip + '格式为0-1之间的小数，最多保留四位小数', {icon: 2});
                        return false;
                    } else {
                        flag = false;
                    }
                })
                if(flag){
                    return;
                }
            }

            //清空短信类型 https默认所有短信类型
            params.smstype = "";
            //清空选择SGIP的三个必填字段
            params.moip   = "";
            params.moport = "";
            params.nodeid = "";
        } else {
            if(_Auth.isNull(params.smstype)){
                layer.msg('请选择短信类型', {icon: 2});
                return;
            }
            if(params.smstype == "0" && !_Auth.isPrice(tz_price) && params.paytype == 1){
                layer.msg('通知短信单价格式为0-1之间的小数，最多保留四位小数', {icon: 2});
                return;
            }
            if(params.smstype == 5 && !_Auth.isPrice(yx_price) && params.paytype == 1){
                layer.msg('营销短信单价格式为0-1之间的小数，最多保留四位小数', {icon: 2});
                return;
            }
            if(params.smstype == 4 && !_Auth.isPrice(yz_price) && params.paytype == 1){
                layer.msg('验证码短信单价格式为0-1之间的小数，最多保留四位小数', {icon: 2});
                return;
            }
        }

        /*
        SGIP 协议参数
         */
        if(params.smsfrom == 4){
            //清空https出现的字段
            params.mourl = "";
            params.deliveryurl = "";
            params.noticeurl = "";

            if(!_Auth.isIp(params.moip)){
                layer.msg('客户上行IP格式不正确', {icon: 2});
                return;
            }
            if(!_Auth.isPort(params.moport)){
                layer.msg('上行端口格式不正确', {icon: 2});
                return;
            }
            if(_Auth.isNull(params.nodeid)){
                layer.msg('节点编码不能为空', {icon: 2});
                return;
            }
            if(!_Auth.isNum(params.nodeid) ){
                layer.msg('节点编码格式不正确', {icon: 2});
                return;
            }
        }
        //ip 白名单 选填
        if(!_Auth.isNull(params.ip)){
            var flag = false;
            var arr = params.ip.split(",");
            for(var i = 0; i<arr.length; i++){
                if(!_Auth.isIp(arr[i])){
                    flag = true;
                    break;
                } else {
                    flag = false;
                }
            }
            if(flag){
                layer.msg('ip白名单格式不对', {icon: 2});
                return;
            }
        }
        //扩展位数处理
        if(params.needextend == 1 && !_Auth.isExtend(params.extendSize)){
            layer.msg('扩展位数为0-10的整数', {icon: 2});
            return;
        }
        //客户接入速度
        if(!_Auth.isNum(params.accessSpeed)){
            layer.msg('客户接入速度为0-3000的整数', {icon: 2});
            return;
        }
        if(params.accessSpeed > 3000){
            layer.msg('客户接入速度为0-3000的整数', {icon: 2});
            return;
        }
        //备注
        if(params.remarks.length > 50){
            layer.msg('备注长度不能超过50个字符', {icon: 2});
            return;
        }
        if((params.needreport == 1 || params.needreport == 2) && _Auth.isNull(params.deliveryurl) && params.smsfrom == 6){
            layer.msg('请填写状态报告回调地址', {icon: 2});
            return;
        }
        if(params.needmo == 1  &&  _Auth.isNull(params.mourl) && params.smsfrom == 6){
            layer.msg('请填写上行回调地址', {icon: 2});
            return;
        }
        //增加价格字段
        var userPriceList = [];
        var size= 0;
        $(".js-price").each(function(i){
            var obj = {};
            if($(this).val() != ""){
                size += 1;

                var old = $(this).attr("oldVal"), current = $(this).val();
                obj.smstype = $(this).attr("smstype");
                obj.userPrice = current;
                if(old != current){
                    userPriceList.push(obj);
                }
            }


        })
        params.size = size;
        params.userPriceList = userPriceList;

        // 比对旧的价格信息
        // var newUserPriceList = [];
        // if (oldUserPriceList){
        //     for(var i=0; i < userPriceList.length; i++){
        //         var obj = userPriceList[i];
        //
        //         var isFind = false;
        //         for(var j=0; j < oldUserPriceList.length; j++){
        //             var objOld = oldUserPriceList[j];
        //             if (objOld.smstype == obj.smstype){
        //                 isFind = true;
        //                 break;
        //             }
        //         }
        //
        //         if (!isFind){
        //             newUserPriceList.push(obj);
        //             continue;
        //         }
        //
        //         var isChange =false;
        //         var price = (obj.userPrice * 1).toFixed(4);
        //         for(var j=0; j < oldUserPriceList.length; j++){
        //             var objOld = oldUserPriceList[j];
        //             var priceOld = (objOld.userPrice * 1).toFixed(4);
        //             if (objOld.smstype == obj.smstype){
        //                 if (priceOld != price){
        //                     isChange = true;
        //                     break;
        //                 }
        //             }
        //         }
        //
        //         if (isChange){
        //             newUserPriceList.push(obj);
        //         }
        //     }
        //
        //     params.userPriceList = newUserPriceList;
        // }

        // params.userPriceList = JSON.stringify(userPriceList);

        if(submit_flag){
            return;
        }
        submit_flag = true;
        //好了 终于可以提交了， 蛋疼啊
        $.ajax({
            url :'/accountInfo/directclient/edit',
            type : "POST",
            contentType:"application/json",
            data : JSON.stringify(params),
            success : function(res){
                submit_flag = false;
                if(res.code !=0 ){
                    layer.msg(res.msg, {icon: 2});
                    return;
                }
                layer.msg(res.msg, {icon: 1,time:1500},function () {
                    window.location.href = history.back();
                    return;
                });
               /* var viewUrl = "/accountInfo/directclient/view";
                window.location.href = viewUrl+"?clientId="+params.clientid;*/
            }
        })
    })
})