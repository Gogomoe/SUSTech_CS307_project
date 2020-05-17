_elpis.admin.user_manager = class {

    reload(api_data, roles) {
        var app = this;
        var list = new dhx.List(null, {
            multiselection: false,
            css: "dhx_widget--bordered",
            template: this.template,
            keyNavigation: true,
            itemHeight: 64,
            height: 540
        });

        this.list = list;

        var context_menu = new dhx.ContextMenu(null, {css: "dhx_widget--bg_gray"});
        context_menu.data.parse(
            [
                {
                    "type": "menuItem",
                    "id": "remove",
                    "value": "删除用户",
                    "icon": "mdi mdi-account-minus",
                },
                {
                    "type": "menuItem",
                    "id": "role",
                    "value": "修改权限",
                    "icon": "mdi mdi-account-key",
                    "items": this.__get_menu_role_items(roles)
                }
            ]
        );

        this.context_menu = context_menu;

        context_menu.events.on("Click", function(id,e) {
            var user = app.get_selected_user();
            if(id == "remove") {
                _elpis.alert.__show_confirm("警告", "是否要删除用户: " + user.username, (confirm) => {
                    if(confirm) {
                        _elpis.alert.__show_alert_no_implement();
                    }
                });
            } else if(id.includes("role-")) {
                var selected_role = id.replace("role-", "");
                app.__switch_user_role(user, selected_role);
            } else {
                console.log(id);
            }
        });

        list.data.parse(api_data["users"]);

        list.events.on("Click", function(id, e) {
            e.preventDefault();
            context_menu.showAt(e);
        });

        this.layout.getCell("search_result").attach(list);
        this.dhxWindow.paint();
    }

    refresh() {
        var app = this;
        _elpis.admin.api.get_all_role((data)=>{
            var roles = data["roles"];
            Object.keys(roles).forEach((role)=>{
                roles[role] = JSON.stringify(roles[role]);
            });
            app.roles = roles;
            _elpis.admin.api.get_all_user((data)=>{
                app.api_data = data;
                app.reload(data, roles);
            });
        });
    }

    __get_menu_role_items(roles) {
        var items = [];
        Object.keys(roles).forEach((role)=>{
            items.push({
                "type": "menuItem",
                "id": "role-" + role,
                "value": "更改" + role + "身份",
                "icon": "mdi mdi-account-switch"
            });
        });
        return items;
    }

    __switch_user_role(user, selected_role) {
        var app = this;
        if(user.roles.includes(selected_role)) {
            var message = "是否删除用户" + user.username + "的" + selected_role + "身份，权限描述：" + this.roles[selected_role];
            _elpis.alert.__show_confirm("警告", message, (confirm) => {
                if(confirm) {
                    _elpis.admin.api.remove_user_role(user.username, selected_role, (data)=>{
                        _elpis.alert.__show_message("用户[" + user.username + "]权限[" + selected_role + "]删除成功");
                        app.refresh();
                    });
                }
            });
        } else {
            var message = "是否为用户" + user.username + "添加" + selected_role + "身份，权限描述：" + this.roles[selected_role];
            _elpis.alert.__show_confirm("警告", message, (confirm) => {
                if(confirm) {
                    _elpis.admin.api.add_user_role(user.username, selected_role, (data)=>{
                        _elpis.alert.__show_message("用户[" + user.username + "]权限[" + selected_role + "]添加成功");
                        app.refresh();
                    });
                }
            });
        }
    }

    filter() {
        var keyword = this.form.getValue().keyword;
        var filtered_data = [];
        this.api_data["users"].forEach((item)=>{
            if(item.username.includes(keyword)) filtered_data.push(item);
        });
        this.reload({users: filtered_data}, this.roles);
    }

    show_window() {
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(params, cb_on_confirm = () => this.dhxWindow.hide()) {
        var app = this;
        var dhxWindow = new dhx.Window({
            title: "用户管理",
            modal: false,
            footer: false,
            closable: false,
            resizable: true,
            movable: true,
            closable: true,
            width: 400, 
            height: 700
        });

        this.dhxWindow = dhxWindow;

        var layout = new dhx.Layout("layout", {
            css: "dhx_layout-cell--no-border",
            rows: [
                {
                    id: "search_form",
                    css: "dhx_layout-cell--no-border",
                    gravity: false,
                    height: 50
                },
                {
                    id: "search_result",
                    gravity: true,
                    css: "dhx_layout-cell--no-border"
                }
            ]
        });

        this.layout = layout;
    
        var form = new dhx.Form(null, {
            css: "dhx_widget--no-bordered",
            rows: [
                {
                    gravity: true,
                    align: "between",
                    cols: [
                        {
                            type: "input",
                            name: "keyword",
                            gravity: false,
                            label: "过滤器",
                            labelInline: true,
                            icon: "",
                            placeholder: "请输入用户名",
                        },
                        {
                            type: "button",
                            name: "filter",
                            gravity: false,
                            full: true,
                            value: "过滤",
                            size: "medium",
                            view: "flat",
                            color: "primary",
                        }
                    ]
                }
            ]
        });

        this.form = form;

        this.api_data = [];
        this.roles = {};

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "增加用户",
            id: "add",
        }, 2);

        dhxWindow.header.events.on("click", function (id) {
            if(id == "add") {
                new _elpis.signinform(()=>{ app.refresh(); app.filter(); });
            }
        });

        form.events.on("ButtonClick", function(id,e) {
            app.filter();
        });

        layout.getCell("search_form").attach(form);

        var template = function (item) {
            var template = "<div class='list_item'>";
            
            template += "<div class='item_name'>";
            template += "<span class='mdi mdi-account'></span>";
            template += item.username;
            template += "</div>";

            template += "<div class='item_categories'>";
            template += "<span class='mdi mdi-account-badge'></span>";
            template += item.roles.length==0?"普通用户":JSON.stringify(item.roles);
            
            template += "</div>";

            template += "</div>";
            return template;
        }

        this.template = template;

        dhxWindow.attach(layout);
        this.show_window();

        app.refresh();
    }

    get_selected_user() { return this.list.selection.getItem(); }
    
}
