/**
 * Created by caosiyuan on 2017/7/13.
 */
$(function(){
    //加载代理商和销售

    var agent_list = [], sale_list = [];
    $.ajax({
        url : '/accountInfo/client/add/data2',
        type : "GET",
        success : function (res) {
            if(res.code != "0"){
                return;
            }
            for(var i = 0; i < res.data.agentInfoList.length; i++){
                agent_list[i] = {};
                agent_list[i].id = res.data.agentInfoList[i].agentId;
                agent_list[i].text = res.data.agentInfoList[i].agentName;
                agent_list[i].sale = res.data.agentInfoList[i].belongSale;
            }
            for(var i = 0; i < res.data.saleList.length; i++){
                sale_list[i] = {};
                sale_list[i].id = res.data.saleList[i].id;
                sale_list[i].text = res.data.saleList[i].name;
            }
            //初始化select2
            $("#agentId").select2({
                data : agent_list
            })
            $("#belongSale").select2({
                data : sale_list
            })
            $(".js-agent").val(agent_list[0].id);
            $(".js-sale").val(sale_list[0].id);

            layer.closeAll();
        }
    })
    //selec2 change
    //这么写是为了 切换还要清空select2 ，切回来还要加上值 比较麻烦。
    $("#agentId").on("select2:select", function(evt){
        var agentId = evt.params.data.id;
        var sale = evt.params.data.sale;
        if(sale != null){
            $(".js-sale").attr("value",sale);
            $("#belongSale").val(sale).trigger("change");
        }
        $(".js-agent").val(agentId);
    })
    $("#belongSale").on("select2:select", function(evt){
        var saleId = evt.params.data.id;
        $(".js-sale").val(saleId);
    })
    var paytype = '';
    //是否支持子账户
    $(".js-extValue").click(function(){
        $(".extValue").val($(this).val());
    })
    //切换添加客户类型
    $(".js-chooseType").click(function(){
        if($(this).hasClass("agentshow")){
            $("#grid-agent").show();
        } else {
            $("#grid-agent").hide();
        }
    })
    //选中后付费，出现单价，自动返还消失
    $(".js-houfu").click(function(){
        $(".paytype").val($(this).val())
        if($("#smsfrom").val() == 6 && $("#smsHttp").val() != 2){
            $(".sms-price").show();
            $(".grid-price").show();
        } else {
            priceShow()
        }
        authStatus()
        toggleRule()
    });
    //选中预付费，单价消失，判断计费规则 是否出现自动返还
    $(".js-yufu").click(function(){
        $(".sms-price").hide();
        $(".sms-price").find("input").val("");
        $(".paytype").val($(this).val())
        authStatus()

        toggleRule();
    });

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

    //选择代理商判断认证状态
    //预付费+http+选择代理商 带认证 其余都是已认证
    //5.14去除这个判断，没有认证状态
    function authStatus (){
        // var agent = $(".js-agent").val();
        // var smsfrom = $("#smsfrom").val();
        // var paytype = $(".paytype").val();
        // if(!_Auth.isNull(agent) && smsfrom == 6 && paytype == "0"){
        //     $(".js-auth").val("2");
        // } else {
        //     $(".js-auth").val("3");
        // }
    }
    //修改计费规则
    function toggleRule(){
        var val = $("#chargeRule").val();
        var paytype = $(".paytype").val();
        if(val != 0 && paytype == 0){
            $(".js-return").show();
        } else{
            $(".js-return").hide();
        }
    }
    $(".js-agent").change(function () {
        authStatus ()

    })
    //短信协议
    $("#smsfrom").change(function(){
        var value = $(this).val();

        //HTTPS不用选择短信类型
        if(value != 6){
            $(".grid-smstype").removeClass("hide");
            $(".js-auth").val("3");
            $(".grid-smstype-http").addClass("hide");
        } else {
            $(".grid-smstype").addClass("hide");
            $(".grid-smstype-http").removeClass("hide");
        }
        //认证状态
        authStatus()
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
    //状态报告获取方式切换
    $(".js-needreport").change(function(){
        var needreport = $(this).val();

        if(needreport == 0 || needreport == 3){
            $(".grid-deliveryurl").hide();
            $(".grid-deliveryurl").find("input").val("");
        } else {
            $(".grid-deliveryurl").show();
        }
    });
    //上行获取方式切换
    $(".js-needmo").change(function(){
        var needmo = $(this).val();

        if(needmo == 0 || needmo == 3){
            $(".grid-mourl").hide();
            $(".grid-mourl").find("input").val("");
        } else {
            $(".grid-mourl").show();
        }
    })
    $(".js-smstype").change(function(){
        priceShow()
    })
    $(".jsmstype").change(function(){
        priceShowHttp()
    })

    //计费规则
    $("#chargeRule").change(function(){
        toggleRule()

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

    //提交
    var submit_flag = false;
    $(".js-confirm").click(function(){
        var params = {};
        $(".js-key").each(function(){
            var value = $(this).val();
            var key   = $(this).data("key");
            params[key] = value;
        })
        //等于0 不加是否自动返还字段
        if(params.chargeRule == 0){
            params.clientInfoExt = {
                extValue : $(".extValue").val()
            }
        } else {
            params.clientInfoExt = {
                extValue : parseInt($(".extValue").val()) + parseInt($("#autoReturn").val())
            };
        }

        if(_Auth.isNull(params.agentId) && params.paytype == 0){
            layer.msg('直客不能选择预付费', {icon: 2});
            return;
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
        if(_Auth.isNull(params.password)){
            layer.msg('请填写登录密码', {icon: 2});
            return;
        }
        if(_Auth.isNull($(".re-pwd").val())){
            layer.msg('请填写确认密码', {icon: 2});
            return;
        }
        if(!_Auth.isPassword(params.password)){
            layer.msg('登录密码为8-12之间的字符，不包括特殊字符', {icon: 2});
            return;
        }
        if(params.password != $(".re-pwd").val()){
            layer.msg('两次密码不一致', {icon: 2});
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
        /*
         如果选择了代理商，则邮箱、手机号码、联系地址为必填项，
         如果未选择代理商，则邮箱、手机号,联系地址为选填项。
         */
        /*
        * 5.14修改 邮箱和手机号码为选填项 by 曹思远
        * */
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
            if(params.smsfrom == 6){
                params.smstype = $(".jsmstype").val();
            }else {
                params.smstype = $(".js-smstype").val();
            }
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
            if(params.smstype == 4 && !_Auth.isPrice(yz_price ) && params.paytype == 1){
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
                    break
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
        // if(!_Auth.isNum(params.accessSpeed)){
        //     layer.msg('客户接入速度为0-3000的整数', {icon: 2});
        //     return;
        // }
        if(params.accessSpeed > 3000){
            layer.msg('客户接入速度为0-3000的整数', {icon: 2});
            return;
        }
        //备注
        if(params.remarks.length > 200){
            layer.msg('备注长度不能超过200个字符', {icon: 2});
            return;
        }
        //增加价格字段
        var userPriceList = [];
        $(".js-price").each(function(i){
            var obj = {};
            if($(this).val() != ""){
               obj.smstype = $(this).attr("smstype");
               obj.userPrice = $(this).val();
               userPriceList.push(obj);
            }
        })
        params.userPriceList = userPriceList;
        // params.userPriceList = JSON.stringify(userPriceList);

        //好了 终于可以提交了， 蛋疼啊
        if(submit_flag){
            return;
        }
        submit_flag = true;
        var agentOwned = $("#agentOwned").val()
        $.ajax({
            url :'/accountInfo/client/add',
            type : "POST",
            contentType:"application/json",
            data : JSON.stringify(params),
            success : function(res){
                submit_flag = false;
                if(res.code !=0 ){
                    layer.msg(res.msg, {icon: 2});
                    return;
                }
                //填充数据

                layer.open({
                    type: 1,
                    title:'开户信息',
                    skin: 'layui-layer-rim', //加上边框
                    area: ['420px', 'auto'], //宽高
                    content: $(".pop"),
                    cancel: function(index, layero){
                        if(params.agentId != ""){
                            //代理商子客户
                            window.location.href = '/accountInfo/client/query'
                        } else {
                            //直客
                            window.location.href = '/accountInfo/directclient/query'
                        }
                    }
                });
                $(".agentLoginUrl").html(res.data.clientUrl);
                $(".text-clientid").html(res.data.clientId);
                $(".text-email").html(res.data.email);
                $(".text-mobile").html(res.data.mobile);
                $(".text-pwd").html(res.data.password);
                $(".interfaceClientId").html(res.data.interfaceClientId);
                $(".interfacePassword").html(res.data.interfacePassword);
                if(agentOwned == '1'){
                    $(".isagentOwned").hide()
                }else{
                    $(".isagentOwned").show()
                }
                if(params.agentId != ""){
                    //代理商子客户
                    var viewUrl = "/accountInfo/qualification/save?clientid=" + res.data.clientId;
                } else {
                    //直客
                    var viewUrl = "/accountInfo/qualification/save?clientid=" + res.data.clientId + "&type=1";
                }
                $(".redirect").attr("href", viewUrl);


            }
        })
    })
})