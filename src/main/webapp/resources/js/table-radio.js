/**
 * Created by csy on 2017/9/18.
 * 基于表格内选择的jquery扩展
 */

;(function(window, $, undefined){
    function countTable() {
        var that = this,
            tag  = $(this);

        //绑定事件

        //radio
        tag.find(".js-table-radio").change(function(){
            total()
        })

        //加
        tag.find(".js-table-add").click(function(){
            var input = getInput($(this)),
                num  = input.val(),
                old  = input.attr("old"),
                remain = getRemain($(this));

            checkRadio($(this));
            if(!/^[0-9]+$/.test(num)){
                input.val(old);     //如果不符合规范，直接使用旧值
            } else if(++num > remain){
                input.val(remain);
            } else {
                input.val(num + 1);
            }
            rowTotal($(this));
            total();
        })
        //减
        tag.find(".js-table-sub").click(function(){
            var input = getInput($(this)),
                num  = input.val(),
                old  = input.attr("old");

            checkRadio($(this));
            if(!/^[0-9]+$/.test(num)){
                input.val(old);     //如果不符合规范，直接使用旧值
            } else if(--num <= 0){
                input.val("1");
            } else {
                input.val(num - 1);
            }

            rowTotal($(this));
            total();
        })
        //输入框
        tag.find(".js-table-input").keyup(function(){
            var input = $(this),
                num  = input.val(),
                old  = input.attr("old"),
                remain = getRemain($(this));

            checkRadio($(this));

            if(!/^[0-9]+$/.test(num)){
                input.val(old);     //如果不符合规范，直接使用旧值，不需要重新计算
            } else if(num > remain){
                input.val(remain);
            } else if(num <= 0){
                input.val("1");
            }

            input.val(num);
            rowTotal($(this));
            total();
        })
    }
    // 行合计
    function rowTotal(obj){
        var num = obj.closest("tr").find(".num").val(),
            unit = obj.closest("tr").find(".unit").data("price");

        var row_t = num * unit;
        obj.closest("tr").find(".price").text(row_t.toFixed(4))
    }
    //合计
    function total(){
        var total = 0;
        tag.find(".js-table-radio:checked").each(function(){
            var num = $(this).closest("tr").find(".num").val(),
                unit = $(this).closest("tr").find(".unit").data("price");

            var t = num * unit;
            total += t;
        })
        $(".js-table-total").text(total.toFixed(4));
    }

    function getInput(obj){
        return obj.closest("tr").find(".js-table-input");

    }
    function getRemain(obj){
        return obj.data("remain");
    }
    function checkRadio(obj){
        obj.closest("tr").find(".js-table-radio")[0].checked = true;
    }


    $.fn.extend({
        countTable : countTable
    })
})(window, jQuery, undefined)
