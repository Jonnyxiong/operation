/**
 * Created by csy on 17/7/28.
 * 组织架构页面业务逻辑
 */
$(function(){
    $("#jstree").jstree({ 'core': { data: null } });
    $("#adjust-tree").jstree({'core' : {data : null }});

    var tree = {},              //树
        parent_map = {},          //父部门字典  用于从parentid找部门,
        dept_map = {},          //部门字点
        users_map = {},         //成员字典
        department = null,  //部门信息
        roles = null;       //部门拥有的角色

    var deptId = '';   //当前部门ID
    getTree();

    //主tree事件
    $("#jstree").on('changed.jstree', function(e, data){
        if(data.selected.length > 0){
            var id = data.selected[0];
            getDept(id);
        }
    })
    //调整部门tree事件
    $("#adjust-tree").on('changed.jstree', function(e, data){
        var id = null;
        if(data.selected.length > 0){
            id = data.selected[0];
        } else {
            return
        }
        $.ajax({
            url : '/office/dept/' + id,
            type : 'GET',
            success : function(res){
                if(res.code != 0 ){
                    layer.msg(res.msg, {icon : 2});
                    return
                }
                var roles_now = null;
                if(res.data.department.level == '0'){
                    roles_now = res.data.roles;
                } else {
                    roles_now = res.data.department.roles;
                }
                $(".adjust-dept").val(res.data.department.departmentId);
                $(".adjust-now").text(res.data.department.departmentName);
                setSaleRole(roles_now, "adjust-role");
            }
        })
    })

    //编辑部门
    $(".js-editnow").click(function(){
        $.ajax({
            url : '/office/dept/roles?id=' + deptId,
            type : 'GET',
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon : 2});
                    return
                }
                setSaleRole(res.data, "now-rolelist");
                filter($("#now-rolelist"), roles);
                var parent = department.parentId;
                $(".parent-dept").text(parent_map[parent]);
                $(".top-name").val(department.departmentName);

                $("#editNowDept").modal('show');
            }
        })
    })
    //编辑部门保存
    $(".js-savenow").click(function(){
        var params = {};
        params.departmentName = $(".top-name").val();
        params.parentId = department.parentId;
        params.roles = checkRole($("#now-rolelist"));
        params.departmentId = department.departmentId;
        $.ajax({
            url : '/office/dept/edit',
            data : JSON.stringify(params),
            type: 'POST',
            contentType:"application/json",
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon : 2});
                    return
                }
                layer.msg(res.msg, {icon : 1});

                getTree(params.departmentId)
                $("#editNowDept").modal('hide');


            }
        })
    })

    //添加部门
    $(".js-saveDept").click(function(){
        var params = {};
        params.departmentName = $("#addDept .now-departmentName").val();
        params.parentId = deptId;
        params.roles = checkRole($("#dept-rolelist"));
        $.ajax({
            url : '/office/dept/add',
            type : 'POST',
            data : JSON.stringify(params),
            contentType:"application/json",
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return
                }
                layer.msg("添加成功", {icon : 1});
                $('#addDept').modal('hide');
                getTree(deptId);

                $("#addDept .now-departmentName").val("");
            }
        })
    })
    //添加角色
    $(".js-saveRole").click(function(){
        addRole(0)
    })
    $(".js-saveRoleAdd").click(function(){
        addRole(1)
    })
    //添加销售
    $(".js-saveSale").click(function(){
        addRole(2)
    })
    $(".js-saveSaleAdd").click(function(){
        addRole(3)
    })

    //编辑子部门
    $("#dept-cont").on("click", ".dept-edit", function(){
        var id = $(this).data("id");
        var current_dept = dept_map[id];

        console.log(dept_map);
        console.log(parent_map);

        $("#editDept #deptId").val(current_dept.departmentId)
        $("#editDept .now-departmentName").val(current_dept.departmentName)
        $("#editDept .top-dept").text(parent_map[current_dept.parentId])


        var role = current_dept.roles;

        var data_roles = {};
        data_roles.list = roles;
        var html_roles = template("tpl-rolelist-hassale", data_roles);
        $("#edit-dept-rolelist").html(html_roles);

        filter($("#edit-dept-rolelist"), role);
        $('#editDept').modal('show');
    })
    //保存编辑的子部门
    $(".js-editDept").click(function(){
        var params = {};
        params.departmentName = $("#editDept .now-departmentName").val();
        params.parentId = deptId;
        params.roles = checkRole($("#edit-dept-rolelist"));
        params.departmentId = $("#deptId").val();
        $.ajax({
            url : '/office/dept/edit',
            type : 'POST',
            data : JSON.stringify(params),
            contentType:"application/json",
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return
                }
                layer.msg("编辑成功", {icon : 1});
                $('#editDept').modal('hide')
                getDept(deptId);

                $("#editDept .now-departmentName").val("");
            }
        })
    })

    //删除部门
    $("#dept-cont").on("click", ".dept-del", function(){
        var depacrtment = $(this).data("id");
        var name = $(this).data("name");
        console.log("xxxx");
        console.log(swal);
        swal({
            title: "您确定要删除部门【"+name+"】",
            text: "",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定",
            cancelButtonText: "让我再考虑一下…",
            closeOnConfirm: false
        }, function () {
            $.ajax({
                url : '/office/dept/delete/' + depacrtment,
                type : 'POST',
                datatype : 'json',
                success : function(res){
                    swal.close();
                    if(res.code != 0){
                        layer.msg(res.msg, {icon : 2});
                        return;
                    }
                    layer.msg(res.msg, { icon : 1});
                    getTree(deptId);
                }
            })
        });

    })
    //用户操作
    //编辑用户
    $("#user-cont").on("click", ".role-edit", function(){
        var id = $(this).data("id");
        var role = $(this).data("role");
        var current_user = users_map[id];
        if(role.indexOf('商务人员') != -1){
            $.ajax({
                url : '/office/user/hasChannel',
                type : 'POST',
                data :{
                    userId:id
                },
                datatype : 'json',
                success : function(res){
                    if(res.data && res.code == '0'){
                        $(".isBusiness").attr('disabled',true);
                        $(".isBusiness").addClass('hasChannel');
                    }
                }
            })
        }
        $("#editUser #roleId").val(current_user.id)
        $("#editUser .email").val(current_user.email)
        $("#editUser .realname").val(current_user.realname)
        $("#editUser .mobile").val(current_user.mobile)
        $("#editUser .password").val(current_user.password)
        $("#editUser .role-auth").val(current_user.dataAuthority)

        //编辑销售
        $("#editSale #role_Id").val(current_user.id)
        $("#editSale .email").val(current_user.email)
        $("#editSale .realname").val(current_user.realname)
        $("#editSale .mobile").val(current_user.mobile)
        $("#editSale .password").val(current_user.password)
        $("#editSale .role-auth").val(current_user.dataAuthority)

        var role = current_user.roles;
        setRole(roles, "edit-user-rolelist");
        filter($("#edit-user-rolelist"), role);
        role.forEach(function (item,index,input) {
            if(item["roleName"] != "销售人员"){
                $('#editUser').modal('show')
            }else {
                $('#editSale').modal('show')
            }
        })
    });

    $("body").on("click",".hasChannel",function () {
        /*layer.tips("该商务人员名下含有通道资源，不能取消商务人员角色",$(this),{
            tips:[1,'3595CC'],
            time:2000
        });*/
        swal({
            title: "该商务名下含有通道资源，\n\r不能取消商务人员角色",
            text: "",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            closeOnConfirm: false
        })
    })
    //保存编辑的用户
    $(".js-editRole").click(function(){
        var params = getParams($("#editUser .js-key"));
        params.id = $("#editUser #roleId").val();
        params.departmentId = deptId;
        params.roles = checkRole($("#edit-user-rolelist"));
        $.ajax({
            url : '/office/user/edit',
            type : 'POST',
            dataType : 'json',
            contentType:"application/json",
            data : JSON.stringify(params),
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return
                }
                layer.msg(res.msg, {icon:1});
                getDept(deptId);
                $('#editUser').modal('hide');

                $("#editUser #roleId").val("");
            }
        })
    })
    //保存编辑的销售
    $(".js-editSale").click(function(){
        var params = getParams($("#editSale .js-key"));
        params.id = $("#editSale #role_Id").val();
        params.departmentId = deptId;
        params.roles = checkRole($("#edit-sale-rolelist"));
        //params.roles = [{id: "35"}];
        $.ajax({
            url : '/office/user/edit',
            type : 'POST',
            dataType : 'json',
            contentType:"application/json",
            data : JSON.stringify(params),
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return
                }
                layer.msg(res.msg, {icon:1});
                getDept(deptId);
                $('#editSale').modal('hide');

                $("#editUser #role_Id").val("");
            }
        })
    })

    //禁用用户
    $("#user-cont").on("click", ".role-no", function(){
        var id = $(this).data("id");
        var name = $(this).data("name");
        var role = $(this).data("role");
        debugger;
        if(role.indexOf('商务人员') != -1){
            $.ajax({
                url : '/office/user/hasChannel',
                type : 'POST',
                data :{
                    userId:id
                },
                datatype : 'json',
                success : function(res){
                    if(res.data && res.code == '0'){
                        swal({
                            title: "该商务名下含有通道资源，\n\r您确定要禁用成员【"+name+"】",
                            text: "",
                            type: "warning",
                            showCancelButton: true,
                            confirmButtonColor: "#DD6B55",
                            confirmButtonText: "确定",
                            cancelButtonText: "让我再考虑一下…",
                            closeOnConfirm: false
                        }, function () {
                            $.ajax({
                                url : '/office/user/disabled/' + id,
                                type : 'POST',
                                datatype : 'json',
                                success : function(res){
                                    swal.close();
                                    if(res.code != 0){
                                        layer.msg(res.msg, {icon : 2});
                                        return;
                                    }
                                    layer.msg(res.msg, { icon : 1});
                                    getDept(deptId);
                                }
                            })
                        });
                    }else {
                        swal({
                            title: "您确定要禁用成员【"+name+"】",
                            text: "",
                            type: "warning",
                            showCancelButton: true,
                            confirmButtonColor: "#DD6B55",
                            confirmButtonText: "确定",
                            cancelButtonText: "让我再考虑一下…",
                            closeOnConfirm: false
                        }, function () {
                            $.ajax({
                                url : '/office/user/disabled/' + id,
                                type : 'POST',
                                datatype : 'json',
                                success : function(res){
                                    swal.close();
                                    if(res.code != 0){
                                        layer.msg(res.msg, {icon : 2});
                                        return;
                                    }
                                    layer.msg(res.msg, { icon : 1});
                                    getDept(deptId);
                                }
                            })
                        });
                    }
                }
            })
        }else if(role.indexOf('销售人员') != -1){
            $.ajax({
                url : '/office/user/hasClient',
                type : 'POST',
                data :{
                    userId:id
                },
                datatype : 'json',
                success : function(res){
                    if(res.data && res.code == '0'){
                        swal({
                            title: "该销售名下含有客户，\n\r您确定要禁用成员【"+name+"】",
                            text: "",
                            type: "warning",
                            showCancelButton: true,
                            confirmButtonColor: "#DD6B55",
                            confirmButtonText: "确定",
                            cancelButtonText: "让我再考虑一下…",
                            closeOnConfirm: false
                        }, function () {
                            $.ajax({
                                url : '/office/user/disabled/' + id,
                                type : 'POST',
                                datatype : 'json',
                                success : function(res){
                                    swal.close();
                                    if(res.code != 0){
                                        layer.msg(res.msg, {icon : 2});
                                        return;
                                    }
                                    layer.msg(res.msg, { icon : 1});
                                    getDept(deptId);
                                }
                            })
                        });
                    }else {
                        swal({
                            title: "您确定要禁用成员【"+name+"】",
                            text: "",
                            type: "warning",
                            showCancelButton: true,
                            confirmButtonColor: "#DD6B55",
                            confirmButtonText: "确定",
                            cancelButtonText: "让我再考虑一下…",
                            closeOnConfirm: false
                        }, function () {
                            $.ajax({
                                url : '/office/user/disabled/' + id,
                                type : 'POST',
                                datatype : 'json',
                                success : function(res){
                                    swal.close();
                                    if(res.code != 0){
                                        layer.msg(res.msg, {icon : 2});
                                        return;
                                    }
                                    layer.msg(res.msg, { icon : 1});
                                    getDept(deptId);
                                }
                            })
                        });
                    }
                }
            })
        }else {
            swal({
                title: "您确定要禁用成员【"+name+"】",
                text: "",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "确定",
                cancelButtonText: "让我再考虑一下…",
                closeOnConfirm: false
            }, function () {
                $.ajax({
                    url : '/office/user/disabled/' + id,
                    type : 'POST',
                    datatype : 'json',
                    success : function(res){
                        swal.close();
                        if(res.code != 0){
                            layer.msg(res.msg, {icon : 2});
                            return;
                        }
                        layer.msg(res.msg, { icon : 1});
                        getDept(deptId);
                    }
                })
            });
        }

    })
    //启用用户
    $("#user-cont").on("click", ".role-start", function(){
        var id = $(this).data("id");
        var name = $(this).data("name");
        swal({
            title: "您确定要启用成员【"+name+"】",
            text: "",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定",
            cancelButtonText: "让我再考虑一下…",
            closeOnConfirm: false
        }, function () {
            $.ajax({
                url : '/office/user/enabled/' + id,
                type : 'POST',
                datatype : 'json',
                success : function(res){
                    swal.close();
                    if(res.code != 0){
                        layer.msg(res.msg, {icon : 2});
                        return;
                    }
                    layer.msg(res.msg, { icon : 1});
                    getDept(deptId);
                }
            })
        });
    })
    //调整部门
    $("#user-cont").on("click", ".role-dept", function(){
        var id = $(this).data("id");
        $("#adjust .adjust-user").val(id);

        $("#adjust").modal("show");
    })
    //保存调整
    $(".save-adjust").click(function(){
        var params = {};
        params.id = $(".adjust-user").val();
        params.departmentId = $(".adjust-dept").val();
        params.roles = checkRole($("#adjust-role"));

        $.ajax({
            url : '/office/user/adjustdept',
            type : 'POST',
            dataType : 'json',
            contentType:"application/json",
            data : JSON.stringify(params),
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return
                }
                layer.msg(res.msg, {icon:1});
                getDept(deptId);
                $('#adjust').modal('hide');
            }
        })

    })
    /*
    * 点击全选
    *
    * */
    $(".grid-role").on("click", ".js-checkall", function(){
        console.log($(this)[0].checked)
        if($(this)[0].checked === true){
            checkAll($(this));
        } else {
            unCheckAll($(this));

        }
    })
    /*
    * 点击选择一个
    *
    * */
    $(".grid-role").on("click", ".js-checkbox", function(){
        var checkbox = $(this);

        if(checkbox[0].checked === true){
            checkbox[0].checked = true;
            checkbox.addClass("checked");
        } else {
            checkbox[0].checked = false;
            checkbox.removeClass("checked")
        }

        isCheckAll($(this))
    })
    /*
    *  检查是否全选
    *  @params obj jQuery对象
    * */
    function isCheckAll(obj) {
        var length = obj.closest(".grid-role").find(".js-checkbox").size();
        console.log(obj.closest(".grid-role").find(".checked").size())
        if(length == obj.closest(".grid-role").find(".checked").size()){
            obj.closest(".grid-role").find(".js-checkall")[0].checked = true;
        } else{
            obj.closest(".grid-role").find(".js-checkall")[0].checked = false;
        }
    }
    /*
     *  选择全部
     *  @params obj jQuery对象
     * */
    function checkAll(obj) {
        console.log(obj.closest(".grid-role").find(".js-checkbox"))
        obj.closest(".grid-role").find(".js-checkbox").each(function(){

            $(this).addClass("checked");
            $(this)[0].checked = true;
        })
    }
    /*
     *  取消选择全部
     *  @params obj jQuery对象
     * */
    function unCheckAll(obj) {
        obj.closest(".grid-role").find(".js-checkbox").each(function(i){

            $(this).removeClass("checked");

            $(this)[0].checked = false;
        })
    }
    /*
    * 获取参数
    * @params jquery 对象
    * */
    function getParams(obj){
        var params = {};
        obj.each(function(){
            var value = $(this).val();
            var key   = $(this).data("key");
            params[key] = value;
        })
        return params;
    }
    //添加角色@params type 0 or 1  type = 0 保存 type = 1 保存并继续添加
    //添加销售@params type 2 or 3  type = 2 保存 type = 3 保存并继续添加
    function addRole(type){
        var params;
        if(type==0||type==1){
            params = getParams($("#addUser .js-key"));
            params.roles = checkRole($("#user-rolelist"));
        }else if(type==2||type==3){
            params = getParams($("#addSale .js-Sale"));
            params.roles =checkRole($("#add-sale-rolelist")) ;
            //params.roles=[{id: "35"}];
        }
        params.departmentId = deptId;
        $.ajax({
            url : '/office/user/add',
            type : 'POST',
            dataType : 'json',
            contentType:"application/json",
            data : JSON.stringify(params),
            success : function(res){
                if(res.code != 0){
                    layer.msg(res.msg, {icon:2});
                    return
                }
                layer.msg(res.msg, {icon:1});
                //type = 0 保存 type = 1 保存并继续添加
                if(type === 0){
                    $('#addUser').modal('hide');
                    //刷新部门内容
                    getDept(deptId);
                } else if(type === 2){
                    $('#addSale').modal('hide');
                    //刷新部门内容
                    getDept(deptId);
                } else if(type === 1){
                    $("#addUser .js-key").each(function(){
                        if($(this).is("[type=text]")){
                            $(this).val("");
                        }
                    })
                }else if(type === 3){
                    $("#addSale .js-Sale").each(function(){
                        if($(this).is("[type=text]")){
                            $(this).val("");
                        }
                    })
                }
            }
        })
    }
    function getTree(node){
        $.ajax({
            url : '/office/tree',
            type : 'POST',
            success : function(res){
                if(res.code != "0"){
                    layer.msg(res.msg,{icon:2});
                    return;
                }
                if(res.data === null || res.data.length == 1 || res.data === undefined){
                    $(".right-ctx").addClass("hide");
                    layer.msg("没有数据权限",{icon:2});
                } else {
                    $(".right-ctx").removeClass("hide");
                }
                var data = res.data,
                    arr  = res.data.subDepts;
                parent_map[data.deptId] = data.deptName; //存入字典
                //遍历对象， 以便达到jstree的格式
                tree.id = data.deptId + "";
                tree.text = data.deptName + "";
                tree.state = {};
                tree.state.opened = true;
                tree.children = recursionTree([], arr);



                $(".top-dept").text(parent_map[data.deptId])
                var tree_ = [];
                tree_[0] = tree;


                $("#jstree").jstree(true).settings.core.data = tree_;
                $("#jstree").jstree(true).refresh();

                $("#adjust-tree").jstree(true).settings.core.data = tree_;
                $("#adjust-tree").jstree(true).refresh();


                //如果有参数，刷新树打开指定节点
                if(node != undefined && node != null){
                    var tree_node = $('#jstree').jstree("get_node", node);
                    $('#jstree').jstree("open_node", tree_node);
                }
                else{
                    getDept(tree.id);
                }
            }
        })
    }
    //复制source到target
    function recursionTree(target, source){
        for(var i = 0; i < source.length; i++){
            parent_map[source[i].deptId] = source[i].deptName;
            target[i] = {};
            target[i].id = source[i].deptId + "";
            target[i].text = source[i].deptName + "";
            target[i].parent = source[i].parentId + "";

            if(source[i].subDepts.length > 0){
                target[i].children = recursionTree([], source[i].subDepts);
            } else {
                target[i].children = [];
            }
        }
        return target;
    }
    //获得部门信息
    function getDept(id){
        $.ajax({
            url : '/office/dept/' + id,
            type : 'GET',
            dataType : 'json',
            success : function(res){
                var users = res.data.users;
                var lowerDepartments = res.data.lowerDepartments;
                if(res.data.department.level == '0'){
                    roles = res.data.roles;
                } else {
                    roles = res.data.department.roles;
                }

                department = res.data.department;

                deptId = department.departmentId;
                //拿到user的map
                getUserMap(users);
                getDeptMap(lowerDepartments);
                $(".deptName").text(department.departmentName);
                $("#addDept .top-dept").text(department.departmentName);

                if(department.departmentId == '1'){
                    $(".js-editnow").addClass('hide');
                } else {
                    $(".js-editnow").removeClass('hide');

                }
                for(var i =0;i<roles.length;i++){
                    if(roles[i]['roleName'] == "销售人员"){
                        $('.openSale').show();
                        break;
                    }else {
                        $('.openSale').hide()
                    }
                }

                var data_lower = {};
                data_lower.list = lowerDepartments;
                var html = template("tpl-dept", data_lower);
                document.getElementById('dept-cont').innerHTML = html;

                var data_users = {};
                data_users.list = users;
                var html_ = template("tpl-role", data_users);
                document.getElementById('user-cont').innerHTML = html_;

                var data_roles = {};
                data_roles.list = roles;
                var html_roles = template("tpl-rolelist", data_roles);
                var html_roles_sale = template("tpl-rolelist-hassale", data_roles);
                var html_roles_onlySale = template("tpl-rolelist-onlysale", data_roles);
                $(".rolelist").html(html_roles);
                $(".rolelistSale").html(html_roles_sale);
                $("#add-sale-rolelist").html(html_roles_onlySale);
                $("#edit-sale-rolelist").html(html_roles_onlySale);
            }
        })
    }
    /*
    * 统计酸则的角色
    * @params jquery对象
    * */
    function checkRole(obj){
        var arr = [];
        obj.find("input[type='checkbox']").each(function(i){
            if($(this)[0].checked === true && $(this).hasClass("js-checkbox")){
                var len = arr.length;
                arr[len] = {};
                arr[len].id = $(this).val();
            }

        })

        return arr;
    }
    /*
    * 过滤角色 已选中的打钩
    * @params obj Jquery 对象
    * @params arr 待匹配的角色数组
    * */
    function filter(obj, arr){
        obj.find("input[type=checkbox]").each(function(i){

            for(var j = 0; j < arr.length; j++){
                if($(this).val() == arr[j].id){
                    $(this)[0].checked = true;
                    $(this).addClass("checked");
                }
            }
        })
        if(obj.find(".js-checkbox").size() == arr.length && arr.length > 0){
            obj.find(".js-checkall")[0].checked = true;
        }
    }
    /*
    * 拿到users的map
    * @params objext
    * */
    function getUserMap(users){
        for(var i=0; i < users.length; i++){
            users_map[users[i].id] = users[i];
        }
    }
    function getDeptMap(dept){
        for(var i=0; i < dept.length; i++){
            dept_map[dept[i].departmentId] = dept[i];
        }
    }
    /*
    *  设置角色 统一渲染
    *  @params list Array
    *  @params sel  Selector
    * */
    function setRole(list, sel){
        var data_role = {};
        data_role.list = list;
        var html_ = template("tpl-rolelist", data_role);
        document.getElementById(sel).innerHTML = html_;
    }

    function setSaleRole(list, sel){
        var data_role = {};
        data_role.list = list;
        var html_ = template("tpl-rolelist-hassale", data_role);
        document.getElementById(sel).innerHTML = html_;
    }
})


