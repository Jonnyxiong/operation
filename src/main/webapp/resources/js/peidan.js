/**
 * Created by caosiyuan on 2017/6/29.
 */
$(function(){
    var orderId = $("#orderId").val();
    var height = $(window).height() - 80;
    $(".peidan-wrap").height(height);

    var ctx_height = height - 61;
    $(".grid-col").height(ctx_height + "px");

    //展开列表
    $(".grid-col").on('click', '.js-more' ,function(){
        $(this).closest(".js-grid-flex").removeClass("grid-small");
        $(this).addClass("hide");
        $(this).siblings(".js-less").removeClass("hide");
    })
    //收起列表
    $(".grid-col").on('click', '.js-less' ,function(){
        $(this).closest(".js-grid-flex").addClass("grid-small");
        $(this).addClass("hide");
        $(this).siblings(".js-more").removeClass("hide");
    })
    //tab切换
    $(".js-sel").click(function(){
        var index = $(this).index();
        $(".sel-ctx").hide();
        $(".sel-ctx").eq(index).fadeIn();

        $(".js-sel").removeClass("active");
        $(this).addClass("active");
    })


    $("#js-yijian").click(function(){
        var success_yijian = $("#success-yijian").val();
        var resource_arr = peidan_controller.choosedArray;
        var uninser_arr  = peidan_controller.uninserArray;
        var data = {};
        data.orderId = orderId;
        data.resourceIds = JSON.stringify(resource_arr);
        data.remark = success_yijian;
        if(uninser_arr.length > 0){
            layer.alert("待接入资源不能进行匹配");
            return;
        }
        if(success_yijian.length > 50){
            layer.alert("备注信息长度不能超过50");
            return;
        }
        if(resource_arr.length == 0){
            layer.alert("请先选择资源");
            return;
        }
        var index = layer.load(1, {
            shade: [0.1,'#fff'] //0.1透明度的白色背景
        });
        $.ajax({
            url :'/peidan/pipeichenggong',
            type : 'POST',
            data : JSON.stringify(data),
            dataType: 'json',
            contentType: 'application/json',
            success : function (res) {
                if(res.success){
                    layer.close(index);
                    layer.msg("匹配成功");
                    setTimeout(function(){
                        window.history.back();
                    }, 1500)
                } else {
                    layer.close(index);
                    layer.alert(res.msg);
                }
            }
        })
    })
    $("#js-daipipei").click(function(){
        var resource_arr = peidan_controller.choosedArray;
        var remark = $("#remark-daipipei").val();
        var data = {};
        data.orderId = orderId;
        data.resourceIds = JSON.stringify(resource_arr);
        data.remark = remark;
        if(remark == '' || remark === undefined || remark === null ){
            layer.alert("备注信息不能为空");
            return;
        }
        if(remark.length > 50){
            layer.alert("备注信息长度不能超过50");
            return;
        }
        if(resource_arr.length == 0){
            layer.alert("请先选择资源");
            return;
        }
        var index = layer.load(1, {
            shade: [0.1,'#fff'] //0.1透明度的白色背景
        });
        $.ajax({
            url :'/peidan/daipipei',
            type : 'POST',
            data : JSON.stringify(data),
            dataType: 'json',
            contentType: 'application/json',
            success : function (res) {
                if(res.success){
                    layer.close(index);
                    layer.msg(res.msg);
                    setTimeout(function(){
                        window.history.back();
                    }, 1500)
                } else {
                    layer.close(index);
                    layer.alert(res.msg);
                }
            }
        })
    })
    $("#js-huitui").click(function(){
        var remark = $("#remark-huitui").val();
        if(remark == '' || remark === undefined || remark === null ){
            layer.alert("备注信息不能为空");
            return;
        }
        if(remark.length > 50){
            layer.alert("备注信息长度不能超过50");
            return;
        }
        var data = {};
        data.orderId = orderId;
        data.remark = remark;
        var index = layer.load(1, {
            shade: [0.1,'#fff'] //0.1透明度的白色背景
        });
        $.ajax({
            url :'/peidan/huitui',
            type : 'POST',
            data : JSON.stringify(data),
            dataType: 'json',
            contentType: 'application/json',
            success : function (res) {
                if(res.success){
                    layer.close(index);
                    layer.msg(res.msg);
                    setTimeout(function(){
                        window.history.back();
                    }, 1500)
                } else {
                    layer.close(index);
                    layer.alert(res.msg);
                }
            }
        })
    })
    $("#js-faqixuqiu").click(function(){
        var remark = $("#remark-faqixuqiu").val();

        if(remark.length > 50){
            layer.alert("备注信息长度不能超过50");
            return;
        }
        var data = {};
        data.orderId = orderId;
        data.remark = remark;
        var index = layer.load(1, {
            shade: [0.1,'#fff'] //0.1透明度的白色背景
        });
        $.ajax({
            url :'/peidan/faqixuqiu',
            type : 'POST',
            data : JSON.stringify(data),
            dataType: 'json',
            contentType: 'application/json',
            success : function (res) {
                if(res.success){
                    layer.close(index);
                    layer.msg("发起需求成功");

                    setTimeout(function(){
                        window.history.back();
                    }, 1500)

                } else {
                    layer.close(index);
                    layer.alert(res.msg);
                }
            }
        })
    })


    peidan_controller.init();

})

//配单业务操作
var peidan_controller = {
    orderId          : $("#orderId").val(),
    orderInfo        : null,                //订单信息
    resourceAll      : null,                //全部资源，没有状态
    resourceInsert   : null,                //已接入的资源
    resourceUninsert : null,                //未接入的资源
    choosedResource  : null,                //已选中的资源
    choosedArray     : [],                  //已选中的资源的资源单号的数组
    uninserArray     : [],                  //待介入资源选中存入数组，判断数组长度来看是否选中待接入
    resourceAll_map  : null,                //序列化之后的全部资源，用于渲染界面
    init : function () {
        //初始化数据
        if(this.orderId != "" && this.orderId != undefined && this.orderId != null){
            this.initData();
        }


    },
    initEvent : function(){
         var that = this;
         $(".resourceList").on("click", ".ui-checkbox", function(){
             var target = $(this).attr("targetclass");
             if($(this).hasClass("checked")){
                 $(this).removeClass("checked");

                 $(".chooseResource ."+target).remove();
                 that.choosedArray.removeByValue(target);

             } else {
                 $(this).addClass("checked");

                 that.choosedArray.unshift(target);

                 var target_data = that.resourceAll_map[target];
                 var html = template('temp-choosed', target_data);
                 $("#grid-choosed").prepend(html);
             }
         })
        //待介入资源选中存入数组，判断数组长度来看是否选中待接入
         $(".unready").on("click", ".ui-checkbox", function(){
             var target = $(this).attr("targetclass");
             if($(this).hasClass("checked")){
                 that.uninserArray.removeByValue(target);
             } else {
                 that.uninserArray.unshift(target);
             }
         })
         $(".chooseResource").on("click", ".choose-close", function(){
             var target = $(this).attr("targetclass");
             //取消勾选
             $(".resourceList ." + target).removeClass("checked");
             //删除数组中的元素
             that.choosedArray.removeByValue(target);
             //从待接入数组
             that.uninserArray.removeByValue(target);
             //删除自己
             $(this).closest(".grid-choose").remove();
         })
    },
    initData : function(){
        this.getAllResource();              //资源列表无状态
        this.getOrderDesc();
        this.getResourceInsertInfo();       //已接入的资源列表
        this.getResourceUninsertInfo();     //未接入的资源列表
        this.getChoosedResource();          //已选资源列表
    },
    //序列化数据，用于勾选的时候渲染
    serialize : function () {
        var data = this.resourceAll;
        var obj  = {};
        for(var i=0; i<data.length; i++){
            var resourceId           = data[i].resourceId;
            obj[resourceId]          = {};
            obj[resourceId].data     = [];
            obj[resourceId].data[0]  = data[i];
        }
        this.resourceAll_map = obj;
    },
    getAllResource : function(){
        var params = {}, that = this;
        params.rows = "999999";
        params.page = "1";
        $.ajax({
            url : '/order/resource/query',
            data : params,
            type : 'POST',
            dataType:'json',
            success : function (res) {
                if(res.data.length == 0){
                    layer.alert("数据为空")
                    return;
                }
                that.resourceAll = res.data;
                that.serialize();
                that.initEvent();
            }
        })
    },
    getOrderDesc : function(){
        var that = this;
        $.ajax({
            url : '/demandOrder/' + that.orderId,
            type : 'GET',
            success : function(res){
                if(res.success){
                    that.orderInfo = res.data;
                    that.drawOrder();
                } else {
                    layer.alert("获取订单信息失败")
                }
            }
        })
    },

    getResourceInsertInfo : function(){
        var that = this;
        var data = {};
        data.rows = "9999999";
        data.page = "1";
        data.state = "1";
        //获取已接入资源
        $.ajax({
            url : '/order/resource/query',
            data : data,
            type : 'POST',
            dataType:'json',
            async: false,
            success : function (res) {
                that.resourceInsert = res;
                that.drawResourceInsert();
            }
        })
    },
    getResourceUninsertInfo : function () {
        var that = this;
        var data = {};
        data.rows = "9999999";
        data.page = "1";
        data.state = "0";
        //获取已接入资源
        $.ajax({
            url : '/order/resource/query',
            data : data,
            type : 'POST',
            dataType:'json',
            async: false,
            success : function (res) {
                that.resourceUninsert = res;
                that.drawResourceUninsert();
            }
        })
    },
    getChoosedResource : function(){
        var that = this;
        var data = {};
        data.orderId = this.orderId;
        data.rows = "9999999";
        data.page = "1";

        //获取已接入资源
        $.ajax({
            url : '/order/resource/query',
            data : data,
            type : 'POST',
            dataType:'json',
            async: false,
            success : function (res) {
                var data_ = res.data, arr = [];
                for(var i=0; i<data_.length; i++){
                    arr[i] = data_[i].resourceId;
                }
                that.choosedArray = arr;
                that.choosedResource = res;
                that.drawChoosedResource();
            }
        })
    },
    //绘制订单信息
    drawOrder : function(){
        var data = this.orderInfo;
        $(".js-key").each(function(){
            var key = $(this).attr("str");
            $(this).text(data[key]);
        })

        $(".order-loading").addClass("hide");
    },
    //绘制已接入订单信息
    drawResourceInsert : function(){
        var data = this.resourceInsert;
        var html = template('temp-resource', data);
        document.getElementById('ready').innerHTML = html;
    },
    //未接入的资源
    drawResourceUninsert : function(){
        var data = this.resourceUninsert;
        var html = template('temp-resource', data);
        document.getElementById('unready').innerHTML = html;
        $(".resource-loading").addClass("hide");
    },
    //以选择的资源列表
    drawChoosedResource : function (){
        var data = this.choosedResource;
        var html = template('temp-choosed', data);
        document.getElementById('grid-choosed').innerHTML = html;
        this.resourceHook();
        $(".choose-loading").addClass("hide");
    },
    resourceHook : function(){
        var choose_data = this.choosedResource.data;
        for(var i = 0; i < choose_data.length; i++){
            $(".resourceList ." + choose_data[i].resourceId).addClass("checked")
        }

    }
}