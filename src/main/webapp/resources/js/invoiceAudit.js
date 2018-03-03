/**
 * Created by keyhuang on 2018/01/25.
 */
// 查看详情
function view(invoiceId) {
    $.ajax({
        type: 'post',
        url:'/finance/invoice/audit/view',
        data: {
            invoiceId: invoiceId
        },
        cache: false,
        dataType: 'json',
        success: function (data) {
            if (data.code != 0) {
                layer.msg(data.msg);
                return;
            } else {
                var params = data.data;
                $(".js-key").each(function(){
                    var key   = $(this).data("key");
                    var text = params[key];
                    if (key == "expressCompany")
                    {
                        if (text == 0)
                        {
                            text = "顺丰速运";
                        }else if (text == 1) {
                            text = "EMS快递";
                        }
                    }
                    $(this).text(text)
                })
            }
        }
    });
}
function auditNormal(invoiceId) {
    layer.open({
        type: 1,
        shadeClose: true,
        title: "审核发票申请记录",
        content: $("#normal_msg"),
        area: ['1100px', '700px']
    });
    view(invoiceId);
    $("#backInvoiceId").val(invoiceId);
    $("#form_normal_audit").show();
    $("#form_check_normal").hide();

    $(".isFail").hide();
}
function audit(invoiceId) {
    layer.open({
        type: 1,
        shadeClose: true,
        title: "审核发票申请记录",
        content: $("#increment_msg"),
        area: ['1100px', '90%']
    });
    view(invoiceId);
    $("#backInvoiceId").val(invoiceId);
    $(".isSend").hide()
    $(".isFail").hide()
    $("#form_audit").show();
    $("#form_check").hide();
}

function checkNormal(invoiceId, status) {
    layer.open({
        type: 1,
        shadeClose: true,
        title: "查看发票申请记录",
        content: $("#normal_msg"),
        area: ['1100px', '600px']
    });
    view(invoiceId);
    $("#form_normal_audit").hide();
    $("#form_check_normal").show();

    if (status == 2)
    {
        $(".isFail").show();
    }else {
        $(".isFail").hide();
    }
}
function check(invoiceId) {
    layer.open({
        type: 1,
        shadeClose: true,
        title: "查看发票申请记录",
        content: $("#increment_msg"),
        area: ['1100px', '90%']
    });
    view(invoiceId);
    $(".isSend").hide()
    $(".isFail").hide()
    $("#form_audit").hide();
    $("#form_check").show();
}
function checkSend(invoiceId) {
    layer.open({
        type: 1,
        shadeClose: true,
        title: "查看发票申请记录",
        content: $("#increment_msg"),
        area: ['1100px', '90%']
    });
    view(invoiceId);
    $(".isSend").show();
    $(".isFail").hide();
    $("#form_audit").hide();
    $("#form_check").show();
}
function checkFail(invoiceId) {
    layer.open({
        type: 1,
        shadeClose: true,
        title: "查看发票申请记录",
        content: $("#increment_msg"),
        area: ['1100px', '90%']
    });
    view(invoiceId);
    $(".isFail").show();
    $(".isSend").hide();
    $("#form_audit").hide();
    $("#form_check").show();
}

function auditBack(invoiceId) {
    // 还原默认值
    $("#otherReason").val("");
    $('input:radio[name="reason"]').prop("checked",false);
    $('input:radio[name="reason"]').eq(0).prop("checked",true);

    var title = '审核驳回原因';
    if (invoiceId == null) {
        title = '审核不通过原因';
    }
    layer.open({
        type: 1,
        shadeClose: true,
        title: '<p style="text-align: center;margin-left: 60px;">'+title+'</p>',
        content: $("#audit_back"),
        area: ['500px', '250px']
    });
    if (invoiceId != null) {
        $("#backInvoiceId").val(invoiceId);
        // 为1是审核驳回
        $("#backType").val(1);
    } else {
        // 为2是审核不通过
        $("#backType").val(2);
    }
    $(".isFail").show();
    $(".isSend").hide();
}

function mail(invoiceId) {
    layer.open({
        type: 1,
        shadeClose: true,
        title: "邮寄",
        content: $("#mailing"),
        area: ['500px', '350px']
    });
    view(invoiceId);
}

$("input[name='reason']").click(function(){
    var val = $(this).val();
    if(val==0){
        $("#other").hide();
    }
    if(val==1){
        $("#other").show();
    }
})
//审核不通过
function makeSure() {
    var auditFailCause = $('input[name="reason"]:checked ').val();
    var val = $('input[name="reason"]:checked ').val();
    if(val==0){
        var auditFailCause = "发票抬头有误";
    }else if(val==1){
        var auditFailCause = $("#otherReason").val();
        if(auditFailCause == ''){
            layer.msg('审核不通过原因为空', {icon: 2});
            return;
        }
    }
    var url = '/finance/invoice/audit/doaudit';
    var backType = $("#backType").val();
    if (backType == 1) {
        url = '/finance/invoice/audit/audittoback';
    }
    $.ajax({
        type: 'post',
        url: url,
        data: {
            invoiceId: $("#backInvoiceId").val(),
            auditFailCause: auditFailCause || '',
            status: 2
        },
        cache: false,
        dataType: 'json',
        success: function (data) {
            if (data.success != true) {
                layer.msg(data.msg);
                reload();
                return;
            }
            layer.msg(data.msg, {icon: 1});
        }
    });
    layer.closeAll();
}

//邮寄
function ismail() {
    $.ajax({
        type: 'post',
        url: '/finance/invoice/audit/express',
        data: {
            invoiceId: $("#invoiceId_2").text(),
            expressCompany: $("#ismailExpressCompany").val(),
            expressOrder: $("#ismailExpressOrder").val()
        },
        cache: false,
        dataType: 'json',
        success: function (data) {
            layer.msg(data.msg);
            reload();
        }
    });
    layer.closeAll();
}

//普票审核通过
function auditPassNormal() {
    layer.confirm("您确定审核通过发票申请？",{title:'审核通过确认'},function () {
           $.ajax({
               type:'post',
               url:'/finance/invoice/audit/doaudit',
               data:{
                   invoiceId : $("#invoiceId_1").text(),
                   status : 3
               },
               cache:false,
               dataType:'json',
               success:function(data){
                       layer.msg(data.msg);
                       reload();
               }
           });
        layer.closeAll();
    })
}
//增票审核通过
function auditPass() {
    layer.confirm("您确定审核通过发票申请？",{title:'审核通过确认'},function () {
           $.ajax({
               type:'post',
               url:'/finance/invoice/audit/doaudit',
               data:{
                   invoiceId : $("#invoiceId_2").text(),
                   status : 3
               },
               cache:false,
               dataType:'json',
               success:function(data){
                   layer.msg(data.msg);
                   reload();
               }
           });
        layer.closeAll();
    })
}
function closemsg() {
    layer.closeAll();
}