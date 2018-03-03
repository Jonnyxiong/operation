/**
 * Created by csy on 2018/1/24.
 * desc ： 发票申请
 */
$(function(){
    // oem代理商
    var agentList = [];
    // 发票信息配置
    var normal = {}, vata = {};

    // 获取OEM代理商
    $.ajax({
        url:"/finance/invoice/app/agentlist",
        type:'GET',
        success : function (res) {
            if(res.code != 0){
                layer.msg(res.msg, {icon:2});
                return;
            }
            var list = res.data;
            for (var i = 0; i < list.length; i++){
                agentList[i] = {};
                agentList[i].id = list[i].agentId;
                agentList[i].text = list[i].agentId + "-" +list[i].agentName;
            }
            $("#agentList").select2({
                data : agentList
            })

            //初始化select2
            $("#agentId").select2({
                data : agentList
            })

            //$("#agentList").val(agentList[0].id);
            $(".js-agent").val(agentList[0].id);
            $(".js-agentName").val(agentList[0].text);

            getCanAmount(agentList[0].id);

            normal = {}, vata = {};
            $.ajax({
                url:"/finance/invoice/app/invoiceconfig?agentId="+agentList[0].id+"&invoiceType=1",
                type:'GET',
                async: false,
                success : function (res) {
                    if(res.code != 0){
                        layer.msg(res.msg, {icon:2});
                        return;
                    }
                    normal = res.data;
                }
            });

            $.ajax({
                url:"/finance/invoice/app/invoiceconfig?agentId="+agentList[0].id+"&invoiceType=2",
                type:'GET',
                async: false,
                success : function (res) {
                    if(res.code != 0){
                        layer.msg(res.msg, {icon:2});
                        return;
                    }
                    vata = res.data;
                }
            });

            $("#invoiceType1").click();

            initNormalInvoiceInfo();
        }
    });

    // 获取可开票金额
    function getCanAmount(id){
        var id = id || $("#agentList").val();
        $.ajax({
            url:"/finance/invoice/app/canamount/" + id,
            type:"GET",
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg,{icon:2});
                    return;
                }
                $(".canamount").val(res.data);
            }
        })
    }

    // selec2 change
    // 这么写是为了 切换还要清空select2 ，切回来还要加上值 比较麻烦。
    $("#agentId").on("select2:select", function(evt){
        var agentId = evt.params.data.id;

        $(".js-agent").val(agentId);
        $(".js-agentName").val(evt.params.data.text);

        getCanAmount(agentId);

        normal = {}, vata = {};
        $.ajax({
            url:"/finance/invoice/app/invoiceconfig?agentId="+agentId+"&invoiceType=1",
            type:'GET',
            async: false,
            success : function (res) {
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return;
                }
                normal = res.data;
                if(normal.invoiceBody == 1){

                }
            }
        });

        $.ajax({
            url:"/finance/invoice/app/invoiceconfig?agentId="+agentId+"&invoiceType=2",
            type:'GET',
            async: false,
            success : function (res) {
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return;
                }
                vata = res.data;
            }
        });

        $("#invoiceType1").click();

        initNormalInvoiceInfo();
    });

    // 发票类型切换
    $(".invoiceType-radio").click(function(){
        var invoiceTypeRadio = $(this).val();
        if(invoiceTypeRadio == 1){
            // 普通发票
            $(".normalInvoice").removeClass("hide");
            $(".vataInvoice").addClass("hide");
            initNormalInvoiceInfo();
        } else {
            // 增值税发票
            $(".vataInvoice").removeClass("hide");
            $(".normalInvoice").addClass("hide");
            initVataInvoiceInfo();
        }
        $(".js-savaInvoice").removeClass("hide");
    });


    /**
     * 普通电子发票
     */
    // 发票主体切换后，个人不显示统一社会信用代码
    $(".invoiceType-body").click(function(){
        var invoiceTypeRadio = $(".invoiceType-body:checked").val();
        invoiceBodyChange(invoiceTypeRadio);
    });

    function invoiceBodyChange(invoiceBody){
        if (invoiceBody == 1)
        {
            $("#js-creditCode_normal").addClass("hide");
        } else {
            $("#js-creditCode_normal").removeClass("hide");
        }
    }

    // 初始化发票信息配置
    function initNormalInvoiceInfo() {
        var parentEle = $(".normalInvoice");

        // 默认为个人
        parentEle.find(".js-invoiceBodyStr").text("企业");
        parentEle.find(".js-invoiceBody").val(2);
        // parentEle.find(".invoiceType-body:checked").val(2);

        //赋值发票抬头
        parentEle.find(".js-invoiceHead").val("");
        parentEle.find(".js-invoiceHeadStr").text("");
        parentEle.find(".invoiceHead").val("");

        //赋值统一社会信用代码
        parentEle.find(".js-creditCode").val("");
        parentEle.find(".js-creditCodeStr").text("");
        parentEle.find(".creditCode").val("");

        //赋值电子邮箱
        parentEle.find(".js-email").val("");
        parentEle.find(".js-emailStr").text("");
        parentEle.find(".email").val("");
        parentEle.find(".js-clientid").text($(".js-agentName").val());
        if (normal && normal.id)
        {
            //赋值发票主体
            var invoiceBodyStr = normal.invoiceBody == 1 ? '个人' : '企业';
            parentEle.find(".js-invoiceBodyStr").text(invoiceBodyStr);
            parentEle.find(".js-invoiceBody").val(normal.invoiceBody);
            // parentEle.find(".invoiceType-body:checked").val(normal.invoiceBody)

            //赋值发票抬头
            parentEle.find(".js-invoiceHead").val(normal.invoiceHead);
            parentEle.find(".js-invoiceHeadStr").text(normal.invoiceHead);
            parentEle.find(".invoiceHead").val(normal.invoiceHead);

            //赋值统一社会信用代码
            parentEle.find(".js-creditCode").val(normal.creditCode);
            parentEle.find(".js-creditCodeStr").text(normal.creditCode);
            parentEle.find(".creditCode").val(normal.creditCode);

            //赋值电子邮箱
            parentEle.find(".js-email").val(normal.email);
            parentEle.find(".js-emailStr").text(normal.email);
            parentEle.find(".email").val(normal.email);

            //复制发票信息ID
            parentEle.find(".js-id").text(normal.id);
            parentEle.find(".invoiceMsg").addClass("hide");
            showNormalInvoiceInfo();
        } else {
            parentEle.find(".js-id").text("");
            parentEle.find(".invoiceMsg").addClass("hide");
            showAddNormalInvoice();
        }
    }

    // 展示普通电子发票信息
    function showNormalInvoiceInfo(){
        $(".normalInvoice").find(".grid-info").removeClass("hide");
        $(".normalInvoice").find(".grid-add").addClass("hide");
        $(".normalInvoice").find(".grid-edit").addClass("hide");
        $(".normalInvoice").find("h3").text("普通电子发票（电子）");
        $(".js-savaInvoice").removeClass("hide");

        var parentEle = $(".normalInvoice");
        var invoiceBody = parentEle.find(".js-invoiceBody").val();
        invoiceBodyChange(invoiceBody);
    }

    // 展示添加普通电子发票信息
    function showAddNormalInvoice(){
        $(".normalInvoice").find(".grid-add").removeClass("hide");
        $(".normalInvoice").find(".grid-info").addClass("hide");
        $(".normalInvoice").find(".grid-edit").addClass("hide");
        $(".normalInvoice").find("h3").text("新增普通电子发票（电子）");
        $(".js-savaInvoice").removeClass("hide");
        var parentEle = $(".normalInvoice");
        var invoiceBody = parentEle.find(".js-invoiceBody").val();
        invoiceBodyChange(invoiceBody);
    }

    // 展示编辑普通电子发票信息
    function showEditNormalInvoice(invoiceBody){
        $(".normalInvoice").find(".grid-add").removeClass("hide");
        $(".normalInvoice").find(".grid-info").addClass("hide");
        $(".normalInvoice").find(".grid-edit").removeClass("hide");
        $(".normalInvoice").find("h3").text("修改普通电子发票（电子）");
        $(".js-savaInvoice").addClass("hide");
        //展示发票信息ID
        $(".normalInvoice").find(".invoiceMsg").removeClass("hide");

        invoiceBodyChange(invoiceBody);
    }

    // 保存新增普通电子发票信息
    function addNormalInvoiceHandle(){
        console.log("ss");
        var parentEle = $(".normalInvoice");
        var invoiceBody = parentEle.find(".invoiceType-body:checked").val(),
            invoiceHead = parentEle.find(".invoiceHead").val(),
            creditCode  = parentEle.find(".creditCode").val(),
            email       = parentEle.find(".email").val();

        //赋值发票主体
        var invoiceBodyStr = invoiceBody == 1 ? '个人' : '企业';
        parentEle.find(".js-invoiceBodyStr").text(invoiceBodyStr);
        parentEle.find(".js-invoiceBody").val(invoiceBody);

        //赋值发票抬头
        parentEle.find(".js-invoiceHead").val(invoiceHead);
        parentEle.find(".js-invoiceHeadStr").text(invoiceHead);

        //赋值统一社会信用代码
        parentEle.find(".js-creditCode").val(creditCode);
        parentEle.find(".js-creditCodeStr").text(creditCode);

        //赋值电子邮箱
        parentEle.find(".js-email").val(email);
        parentEle.find(".js-emailStr").text(email);

        showNormalInvoiceInfo();
    };

    // 编辑普通发票信息
    $(".js-editNormal").click(function(){
        var parentEle = $(this).closest(".normalInvoice");
        var invoiceBody = parentEle.find(".js-invoiceBody").val(),
            invoiceHead = parentEle.find(".js-invoiceHead").val(),
            creditCode  = parentEle.find(".js-creditCode").val(),
            email       = parentEle.find(".js-email").val();
        //回显发票主体
        parentEle.find(".radio-" + invoiceBody).trigger("click");

        //回显发票抬头
        parentEle.find(".invoiceHead").val(invoiceHead);

        //回显统一社会信用代码
        parentEle.find(".creditCode").val(creditCode);

        //回显电子邮箱
        parentEle.find(".email").val(email);

        showEditNormalInvoice(invoiceBody);
    });

    // 取消编辑普通发票信息
    $(".js-cancelNormal").click(function(){
        $(".normalInvoice").find(".invoiceMsg").addClass("hide");
        showNormalInvoiceInfo();
    });

    /**
     * 增值税发票
     */
    function initVataInvoiceInfo(){
        var parentEle = $(".vataInvoice");

        //赋值
        //赋值发票抬头
        parentEle.find(".js-invoiceHead").val("");
        parentEle.find(".js-invoiceHeadStr").text("");
        parentEle.find(".invoiceHead").val("");

        //赋值统一社会信用代码
        parentEle.find(".js-creditCode").val("");
        parentEle.find(".js-creditCodeStr").text("");
        parentEle.find(".creditCode").val("");

        //赋值公司注册地址
        parentEle.find(".js-companyRegAddr").val("");
        parentEle.find(".js-companyRegAddrStr").text("");
        parentEle.find(".companyRegAddr").val("");

        //赋值开户银行
        parentEle.find(".js-bank").val("");
        parentEle.find(".js-bankStr").text("");
        parentEle.find(".bank").val("");

        //赋值开户账号
        parentEle.find(".js-bankAccount").val("");
        parentEle.find(".js-bankAccountStr").text("");
        parentEle.find(".bankAccount").val("");

        //赋值公司固定电话
        parentEle.find(".js-telphone").val("");
        parentEle.find(".js-telphoneStr").text("");
        parentEle.find(".telphone").val("");

        //赋值收件人
        parentEle.find(".js-toName").val("");
        parentEle.find(".js-toNameStr").text("");
        parentEle.find(".toName").val("");

        //赋值收件人地址
        parentEle.find(".js-toAddr").val("");
        parentEle.find(".js-toAddrStr").text("");
        parentEle.find(".toAddr").val("");

        //赋值收件人详细地址
        parentEle.find(".js-toAddrDetail").val("");
        parentEle.find(".js-toAddrDetailStr").text("");
        parentEle.find(".toAddrDetail").val("");

        //赋值收件人手机
        parentEle.find(".js-toPhone").val("");
        parentEle.find(".js-toPhoneStr").text("");
        parentEle.find(".toPhone").val("");

        //赋值收件人qq
        parentEle.find(".js-toQq").val("");
        parentEle.find(".js-toQqStr").text("");
        parentEle.find(".toQq").val("");

        parentEle.find(".js-clientid").text($(".js-agentName").val());

        if (vata && vata.id)
        {
            //赋值
            //赋值发票抬头
            parentEle.find(".js-invoiceHead").val(vata.invoiceHead);
            parentEle.find(".js-invoiceHeadStr").text(vata.invoiceHead);
            parentEle.find(".invoiceHead").val(vata.invoiceHead);

            //赋值统一社会信用代码
            parentEle.find(".js-creditCode").val(vata.creditCode);
            parentEle.find(".js-creditCodeStr").text(vata.creditCode);
            parentEle.find(".creditCode").val(vata.creditCode);

            //赋值公司注册地址
            parentEle.find(".js-companyRegAddr").val(vata.companyRegAddr);
            parentEle.find(".js-companyRegAddrStr").text(vata.companyRegAddr);
            parentEle.find(".companyRegAddr").val(vata.companyRegAddr);

            //赋值开户银行
            parentEle.find(".js-bank").val(vata.bank);
            parentEle.find(".js-bankStr").text(vata.bank);
            parentEle.find(".bank").val(vata.bank);

            //赋值开户账号
            parentEle.find(".js-bankAccount").val(vata.bankAccount);
            parentEle.find(".js-bankAccountStr").text(vata.bankAccount);
            parentEle.find(".bankAccount").val(vata.bankAccount);

            //赋值公司固定电话
            parentEle.find(".js-telphone").val(vata.telphone);
            parentEle.find(".js-telphoneStr").text(vata.telphone);
            parentEle.find(".telphone").val(vata.telphone);

            //赋值收件人
            parentEle.find(".js-toName").val(vata.toName);
            parentEle.find(".js-toNameStr").text(vata.toName);
            parentEle.find(".toName").val(vata.toName);

            //赋值收件人地址
            parentEle.find(".js-toAddr").val(vata.toAddr);
            parentEle.find(".js-toAddrStr").text(vata.toAddr);
            parentEle.find(".toAddr").val(vata.toAddr);

            //赋值收件人详细地址
            parentEle.find(".js-toAddrDetail").val(vata.toAddrDetail);
            parentEle.find(".js-toAddrDetailStr").text(vata.toAddrDetail);
            parentEle.find(".toAddrDetail").val(vata.toAddrDetail);

            //赋值收件人手机
            parentEle.find(".js-toPhone").val(vata.toPhone);
            parentEle.find(".js-toPhoneStr").text(vata.toPhone);
            parentEle.find(".toPhone").val(vata.toPhone);

            //赋值收件人qq
            parentEle.find(".js-toQq").val(vata.toQq);
            parentEle.find(".js-toQqStr").text(vata.toQq);
            parentEle.find(".toQq").val(vata.toQq);
            parentEle.find(".js-id").text(vata.id);
            parentEle.find(".invoiceMsg").addClass("hide");
            showVataInvoiceInfo();
        } else {
            parentEle.find(".js-id").text("");
            parentEle.find(".invoiceMsg").addClass("hide");
            showAddVataInvoice();
        }
    }

    //展示增值发票信息
    function showVataInvoiceInfo(){
        $(".vataInvoice").find(".grid-add").addClass("hide");
        $(".vataInvoice").find(".grid-info").removeClass("hide");
        $(".vataInvoice").find(".grid-edit").removeClass("hide");
        $(".vataInvoice").find("h3").text("增值税发票信息");
        $(".js-savaInvoice").removeClass("hide");

    }

    //展示添加增值发票信息
    function showAddVataInvoice(){
        $(".vataInvoice").find(".grid-add").removeClass("hide");
        $(".vataInvoice").find(".grid-info").addClass("hide");
        $(".vataInvoice").find(".grid-edit").addClass("hide");
        $(".vataInvoice").find("h3").text("新增增值税发票信息");
        $(".js-savaInvoice").removeClass("hide");
    }

    //展示编辑增值发票信息
    function showEditVataInvoice(){
        $(".vataInvoice").find(".grid-add").removeClass("hide");
        $(".vataInvoice").find(".grid-info").addClass("hide");
        $(".vataInvoice").find(".grid-edit").removeClass("hide");
        $(".vataInvoice").find("h3").text("修改增值税发票信息");
        $(".js-savaInvoice").addClass("hide");
        //展示发票信息ID
        $(".vataInvoice").find(".invoiceMsg").removeClass("hide");
    }

    // $(".js-addVata").click(function(){
    //     addVataInvoiceHandle();
    // })

    // 新增增值发票信息
    function addVataInvoiceHandle(){
        var parentEle = $(".vataInvoice");
        var invoiceHead     = parentEle.find(".invoiceHead").val(),
            creditCode      = parentEle.find(".creditCode").val(),
            companyRegAddr  = parentEle.find(".companyRegAddr").val(),
            bank            = parentEle.find(".bank").val(),
            bankAccount     = parentEle.find(".bankAccount").val(),
            telphone        = parentEle.find(".telphone").val(),
            toName          = parentEle.find(".toName").val(),
            toAddr          = parentEle.find(".toAddr").val(),
            toAddrDetail    = parentEle.find(".toAddrDetail").val(),
            toPhone         = parentEle.find(".toPhone").val(),
            toQq            = parentEle.find(".toQq").val();

        //赋值
        //赋值发票抬头
        parentEle.find(".js-invoiceHead").val(invoiceHead);
        parentEle.find(".js-invoiceHeadStr").text(invoiceHead);

        //赋值统一社会信用代码
        parentEle.find(".js-creditCode").val(creditCode);
        parentEle.find(".js-creditCodeStr").text(creditCode);

        //赋值公司注册地址
        parentEle.find(".js-companyRegAddr").val(companyRegAddr);
        parentEle.find(".js-companyRegAddrStr").text(companyRegAddr);

        //赋值开户银行
        parentEle.find(".js-bank").val(bank);
        parentEle.find(".js-bankStr").text(bank);

        //赋值开户账号
        parentEle.find(".js-bankAccount").val(bankAccount);
        parentEle.find(".js-bankAccountStr").text(bankAccount);

        //赋值公司固定电话
        parentEle.find(".js-telphone").val(telphone);
        parentEle.find(".js-telphoneStr").text(telphone);

        //赋值收件人
        parentEle.find(".js-toName").val(toName);
        parentEle.find(".js-toNameStr").text(toName);

        //赋值收件人地址
        parentEle.find(".js-toAddr").val(toAddr);
        parentEle.find(".js-toAddrStr").text(toAddr);

        //赋值收件人详细地址
        parentEle.find(".js-toAddrDetail").val(toAddrDetail);
        parentEle.find(".js-toAddrDetailStr").text(toAddrDetail);

        //赋值收件人手机
        parentEle.find(".js-toPhone").val(toPhone);
        parentEle.find(".js-toPhoneStr").text(toPhone);

        //赋值收件人qq
        parentEle.find(".js-toQq").val(toQq);
        parentEle.find(".js-toQqStr").text(toQq);

        showVataInvoiceInfo();
    };

    // 编辑增值发票信息
    $(".js-editVata").click(function(){

        var parentEle = $(this).closest(".vataInvoice");
        var invoiceHead     = parentEle.find(".js-invoiceHead").val(),
            creditCode      = parentEle.find(".js-creditCode").val(),
            companyRegAddr  = parentEle.find(".js-companyRegAddr").val(),
            bank            = parentEle.find(".js-bank").val(),
            bankAccount     = parentEle.find(".js-bankAccount").val(),
            telphone        = parentEle.find(".js-telphone").val(),
            toName          = parentEle.find(".js-toName").val(),
            toAddr          = parentEle.find(".js-toAddr").val(),
            toAddrDetail    = parentEle.find(".js-toAddrDetail").val(),
            toPhone         = parentEle.find(".js-toPhone").val(),
            toQq            = parentEle.find(".js-toQq").val();

        //回显发票抬头
        parentEle.find(".invoiceHead").val(invoiceHead);

        //回显统一社会信用代码
        parentEle.find(".creditCode").val(creditCode);

        //回显公司注册地址
        parentEle.find(".companyRegAddr").val(companyRegAddr);

        //回显开户银行
        parentEle.find(".bank").val(bank);

        //回显开户账号
        parentEle.find(".bankAccount").val(bankAccount);

        //回显公司固定电话
        parentEle.find(".telphone").val(telphone);

        //回显收件人
        parentEle.find(".toName").val(toName);

        //回显收件人地址
        parentEle.find(".toAddr").val(toAddr);

        //回显详细地址
        parentEle.find(".toAddrDetail").val(toAddrDetail);

        //回显收件人手机
        parentEle.find(".toPhone").val(toPhone);

        //回显收件人qq
        parentEle.find(".toQq").val(toQq);

        showEditVataInvoice();
    });

    //取消编辑取消发票
    $(".js-cancelVata").click(function(){
        $(".vataInvoice").find(".invoiceMsg").addClass("hide");
        showVataInvoiceInfo();
    })


    $("#normalInvoice").validate({
        rules: {
            invoiceHead: {
                required: true,
                maxlength: 50
            },
            creditCode: {
                required: true,
                maxlength: 18
            },
            email: {
                required: true,
                email: true
            }
        },
        submitHandler:function(form){
            $(".normalInvoice").find(".invoiceMsg").addClass("hide");
            addNormalInvoiceHandle();
        }
    });

    $("#vataInvoice").validate({
        rules: {
            invoiceHead2: {
                required: true,
                maxlength: 50
            },
            creditCode2: {
                required: true,
                maxlength: 18
            },
            companyRegAddr :{
                required: true,
                maxlength: 50
            },
            bank :{
                required: true,
                maxlength: 20
            },
            bankAccount :{
                required: true,
                maxlength: 20
            },
            telphone : {
                required: true
            },
            toName :{
                required:true,
                maxlength: 20
            },
            toAddr : {
                required:true,
                maxlength: 20
            },
            toAddrDetail : {
                required :true,
                maxlength: 50
            },
            toPhone : {
                required :true
            },
            toQq: {
                maxlength: 20
            }
        },
        submitHandler:function(form){
            $(".vataInvoice").find(".invoiceMsg").addClass("hide");
            addVataInvoiceHandle();
        }
    });


    $(".js-savaInvoice").click(function(){
        // 校验备注长度
        // 校验发票金额
        var root = $(".invoiceObject");

        var remark = root.find(".remark").val();
        if(remark.length > 50){
            layer.msg("备注长度必须介于 0 和 50 之间", {icon:2});
            return;
        }

        // 获取可开票金额
        var canAmount = $(".canamount").val();
        // 获取发票金额
        var invoiceAmount = $(".invoiceAmount").val();
        if (invoiceAmount == "" || Number(invoiceAmount) <= 0 || Number(invoiceAmount) >  Number(canAmount))
        {
            layer.msg("发票金额需大于0且小于可开票金额", {icon:2});
            return;
        }
        if(!_Auth.isTwodecimal(invoiceAmount)){
            layer.msg("发票金额最多保留两位小数", {icon:2});
            return;
        }

        // 构造参数
        var params = {};
        params.agentId = $(".js-agent").val();
        params.invoiceAmount = invoiceAmount;
        params.remark = remark;

        // 发票类型
        params.invoiceType = root.find(".invoiceType-radio:checked").val();
        if (params.invoiceType == 1) {
            var sub = $(".normalInvoice");
            var invoiceBody = sub.find(".js-invoiceBody").val(),
                invoiceHead = sub.find(".js-invoiceHead").val(),
                creditCode  = sub.find(".js-creditCode").val(),
                email       = sub.find(".js-email").val();

            params.invoiceBody = invoiceBody;
            params.invoiceHead = invoiceHead;
            params.email = email;

            // 校验一下
            if (invoiceBody == 2) {
                // 校验邮箱
                params.creditCode = creditCode;
            }
        } else {
            var sub = $(".vataInvoice");
            var invoiceHead     = sub.find(".js-invoiceHead").val(),
                creditCode      = sub.find(".js-creditCode").val(),
                companyRegAddr  = sub.find(".js-companyRegAddr").val(),
                bank            = sub.find(".js-bank").val(),
                bankAccount     = sub.find(".js-bankAccount").val(),
                telphone        = sub.find(".js-telphone").val(),
                toName          = sub.find(".js-toName").val(),
                toAddr          = sub.find(".js-toAddr").val(),
                toAddrDetail    = sub.find(".js-toAddrDetail").val(),
                toPhone         = sub.find(".js-toPhone").val(),
                toQq            = sub.find(".js-toQq").val();

            var i = /^[0][1-9]{2,3}-[0-9]{5,10}$/;
            if(!i.test(telphone)){
                layer.msg("公司固定电话格式不正确，例如：0755-78676756", {icon:2});
                return;
            }

            params.invoiceHead = invoiceHead;
            params.creditCode = creditCode;
            params.companyRegAddr = companyRegAddr;
            params.bank = bank;
            params.bankAccount = bankAccount;
            params.telphone = telphone;
            params.toName = toName;
            params.toAddr = toAddr;
            params.toAddrDetail = toAddrDetail;
            params.toPhone = toPhone;
            params.toQq = toQq;

        }

        $.ajax({
            url:"/finance/invoice/app/save",
            data : JSON.stringify(params),
            type: 'POST',
            contentType:"application/json",
            beforeSend:function () {
                $(".js-savaInvoice").attr({"disabled":"disabled"});
            },
            success : function (res) {
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    $(".js-savaInvoice").attr({"disabled":"false"});
                    return;
                } else {
                    layer.msg(res.msg, {icon: 1, time: 1500}, function () {
                        window.location.href = '/finance/invoice/record/list';
                    });
                }
            }
        });
    })
});
