/**
 * Created by csy on 2017/9/19.
 */
/**
 * Created by csy on 2017/9/18.
 * 基于表格内选择的jquery扩展
 */

;(function(window, $, undefined){
    /*
    * @params tables jQuery Obj
    * */
    function countTable(table) {
        var that = this,
            tag  = $(this),
            table = $(table);

        //绑定事件
        table.on("click",".js-table-number",function(e){
            e.stopPropagation();
        })
        //radio
        table.on("change","input[type=checkbox]",function(){
            total()
        })



        //加
        table.on("click",".js-table-add",function(){
            var input = getInput($(this)),
                num  = input.val(),
                old  = input.attr("old"),
                remain = getRemain($(this));

            checkRadio($(this));
            if(!/^[0-9]+$/.test(num)){
                input.val(old);     //如果不符合规范，直接使用旧值
                return;
            } else if(++num > remain){
                input.val(remain);
                input.attr("old", remain);
                rowTotal($(this));
                total();
                return;
            } else {
                input.val(num);
                input.attr("old", num);
            }

            input.attr("old", num);
            rowTotal($(this));
            total();
        })
        //减
        table.on("click",".js-table-sub",function(){
            var input = getInput($(this)),
                num  = input.val(),
                old  = input.attr("old");


            if(!/^[0-9]+$/.test(num)){
                input.val(old);     //如果不符合规范，直接使用旧值
                return;
            } else if(--num <= 0){
                input.val(old);
                input.attr("old", old);
                rowTotal($(this));
                total();
                return;
            } else {
                input.val(num);
                input.attr("old", num);
                checkRadio($(this));
            }

            input.attr("old", num);
            rowTotal($(this));
            total();
        })
        //输入框
        table.on("keyup",".js-table-input",function(){
            var input = $(this),
                num  = input.val(),
                old  = input.attr("old"),
                remain = getRemain($(this));


            if(num == ""){
                return;
            }
            checkRadio($(this));
            if(!/^[0-9]*$/.test(num)){
                input.val(old);     //如果不符合规范，直接使用旧值，不需要重新计算
                return;
            } else if(num > remain){
                input.val(remain);
                input.attr("old", remain);
                rowTotal($(this));
                total();
                return;
            } else if(num <= 0){
                input.val("1");
                input.attr("old", 1);
                rowTotal($(this));
                total();
                return;
            }

            input.val(num);
            input.attr("old", num);
            rowTotal($(this));
            total();
        })
        table.on("blur",".js-table-input",function(){
            var input = $(this),
                num  = input.val(),
                old  = input.attr("old"),
                remain = getRemain($(this));


            if(num == ""){
                input.val("1");
                input.attr("old", 1);
            }

        })
        function checkRadio(obj){
            //obj.closest("tr").find("input[type=checkbox]")[0].checked = true;
            var rowId = obj.closest("tr").attr("id");
            table.resetSelection(rowId)
            table.setSelection(rowId);
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
            table.find("input[type=checkbox]:checked").each(function(){
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
    }




    $.fn.extend({
        countTable : countTable
    })
})(window, jQuery, undefined)
