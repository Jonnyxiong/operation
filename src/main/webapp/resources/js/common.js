
$(function () {
    // seesion超时情况下ajax请求跳转到登陆页面
    $.ajaxSetup({
        contentType : "application/x-www-form-urlencoded;charset=utf-8",
        complete : function(XMLHttpRequest, textStatus) {
            /*通过XMLHttpRequest取得响应头，sessionstatus*/
            var sessionstatus = XMLHttpRequest.getResponseHeader("sessionstatus");
            var status = XMLHttpRequest.status;
            if (sessionstatus == "TIMEOUT") {
                parent.layer.open({
                    type: 2,
                    title: false,
                    area: ['600px', '500px'],
                    offset: '120px',
                    fixed: true, //固定
                    maxmin: false,
                    loading: 1,
                    closeBtn: 0,
                    content: XMLHttpRequest.getResponseHeader("CONTEXTPATH")
                });
            } else if (sessionstatus == "timeout") {
                layer.open({
                    type: 2,
                    title: false,
                    area: ['600px', '500px'],
                    offset: '120px',
                    fixed: true, //固定
                    maxmin: false,
                    loading: 1,
                    closeBtn: 0,
                    content: XMLHttpRequest.getResponseHeader("CONTEXTPATH")
                });
            }
        }
    });
});



// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
// 例子： 
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
Date.prototype.Format = function (fmt) { //author: meizz 
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "h+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds(), //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

//删除数组中的制定元素
Array.prototype.removeByValue = function(val) {
    for(var i=0; i<this.length; i++) {
        if(this[i] == val) {
            this.splice(i, 1);
            break;
        }
    }
};

function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var url = decodeURI(window.location.search);
    var r = url.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return "";
}
//解决合计精度问题
// Math.prototype.add = function(v1, v2)
// {
//     var r1, r2, m;
//     try
//     {
//         r1 = v1.toString().split(".")[1].length;
//     }
//     catch (e)
//     {
//         r1 = 0;
//     }
//     try
//     {
//         r2 = v2.toString().split(".")[1].length;
//     }
//     catch (e)
//     {
//         r2 = 0;
//     }
//     m = Math.pow(10, Math.max(r1, r2));
//
//     return (v1 * m + v2 * m) / m;
// };


function halfYearAgo(fmt){
	var d = new Date();
	d.setMonth(d.getMonth() - 6); 
	return d.Format(fmt);
}
function getNextDay(fmt){
    var d = new Date();
    d.setDate(d.getDate() + 1);
    return d.Format(fmt);
}
function monthsAgo(fmt,num){
	var d = new Date();
	d.setMonth(d.getMonth() - num);
	return d.Format(fmt);
}




function isSessionValid(){
    var url = "/index/isSessionValid";
    var valid = false;
    $.ajax({
        type: "post",
        async:false,
        url:url,
        success:function (data) {
            if(data.success == true){
                valid = true;
            }else{
                valid = false;
            }
        }
    });
    return valid;
}
function checkMenu (id){
    $.ajax({
        url : "/index/menuright/"+ id +"/button",
        type : "GET",
        success : function(res) {
            if(res.code != 0){
                layer.msg(res.msg, {icon:2});
                return;
            }
            var menulist = res.data;
            for(var i = 0; i < menulist.length; i++){
                var item = menulist[i],
                    ele  = $("[data-menuId='"+ item.menuId +"']");
                if(item.authority){
                    ele.removeClass("hide");
                } else {
                    ele.addClass("js-authority");
                }
                $("js-authority").remove();
            }
        }
    })
}

function ityzl_SHOW_LOAD_LAYER(msg){
    return layer.msg('<div style="color:#506470;font-family: "microsoft yahei","Arial Narrow",HELVETICA;">'+msg+'</div>', {icon: 16,shade: [0.5, '#f5f5f5'],scrollbar: false,offset: 'auto',time: 600000}) ;
}

$(".nav li").on("click", "a" ,function (e) {
    var href= $(this).attr("href") || '';
    var ishref = href.split('/').length > 1 ? true : false;
    if(ishref && !isSessionValid()){
        e.preventDefault();
        e.stopepropagation();
        return false;
    }
});

$(".J_menuItem").on("click" ,function (e) {
    var href= $(this).attr("href") || '';
    var ishref = href.split('/').length > 1 ? true : false;
    if(ishref && !isSessionValid()){
        e.preventDefault();
        e.stopepropagation();
        return false;
    }
});