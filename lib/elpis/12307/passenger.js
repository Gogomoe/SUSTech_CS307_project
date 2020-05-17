_12307.passenger = class {
    reload(api_data) {
        var app = this;
        var list = new dhx.List(null, {
            multiselection: true,
            css: "dhx_widget--bordered",
            template: this.template,
            keyNavigation: true,
            itemHeight: 64,
            height: 6400
        });

        this.list = list;

        list.data.parse(api_data["passengers"]);

        list.events.on("click", function(id) {
            var selected_items = list.selection.getItem();
            if (selected_items) app.dhxWindow.header.data.update("remove", { disabled: false });
            else app.dhxWindow.header.data.update("remove", { disabled: true });
            app.cb_on_select(selected_items);
        });

        this.dhxWindow.attach(list);
        this.dhxWindow.paint();
    }

    refresh() {
        var app = this;
        _12307.api.get_all_passenger((data) => app.reload(data));
    }

    show_window() {
        var app = this;
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_on_select=()=>{}) {
        var app = this;
        var dhxWindow = new dhx.Window({
            title: "选择同行人",
            modal: false,
            header: true,
            footer: true,
            closable: true,
            resizable: true,
            movable: true,
            width: 600,
            height: 400
        });

        this.dhxWindow = dhxWindow;
        this.cb_on_select = cb_on_select;

        var template = function (item) {
            var template = "<div class='list_item'>";
            
            template += "<div class='item_name'>";
            template += "<span class='mdi mdi-account'></span>";
            template += item.passenger_name;
            template += "</div>";

            template += "<div class='item_categories'>";
            template += "<span class='mdi mdi-phone'></span>";
            template += item.phone;
            template += " ";
            template += "<span class='mdi mdi-card-bulleted'></span>";
            template += item.id_number;
            template += "</div>";

            template += "</div>";
            return template;
        }

        this.template = template;

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "添加同行人信息",
            id: "add",
        }, 2);

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "删除同行人信息",
            id: "remove",
            disabled: true
        }, 2);

        dhxWindow.footer.data.add({
            type: "spacer"
        });

        dhxWindow.footer.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "确认",
            id: "ok",
        });

        dhxWindow.header.events.on("click", function (id) {
            if (id == "remove") {
                app.__remove_selected_passengers();
            } else if (id == "add") {
                // (() => new _12307.passenger_add(app.refresh))();
                // setTimeout( () => new _12307.passenger_add((data) => app.refresh(data))._handlers.setActive(), 100 );
                new _12307.passenger_add((data) => app.refresh(data))
            }
        });

        dhxWindow.footer.events.on("click", function (id) {
            dhxWindow.hide();
        });

        // dhxWindow.show();

        this.refresh();
    }

    get_selected_passengers() { return this.list.selection.getItem(); }

    __remove_selected_passengers() {
        var names = "";
        var app = this;
        var selected_items = this.get_selected_passengers();
        selected_items.forEach((item) => names += item["passenger_name"] + " " );
        _elpis.alert.__show_confirm("警告", "是否确认删除同行人 " + names, (confirm) => {
            if(confirm) selected_items.forEach((item) => {
                _12307.api.remove_passenger(item["passenger_id"], (data) => app.refresh(data));
            });
        });
    }
}

_12307.passenger_add = class {

    show_window() {
        var app = this;
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_ok=()=>{}) {
        this.cb_ok = cb_ok;

        var dhxWindow = new dhx.Window({
            title: "添加同行人信息",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 400
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "同行人姓名",
                    icon: "",
                    placeholder: "请输入姓名",
                    name: "passenger_name",
                    required: true
                },
                {
                    type: "input",
                    gravity: false,
                    label: "同行人身份证号",
                    icon: "",
                    placeholder: "请输入有效身份证号",
                    name: "id_number",
                    required: true
                },
                {
                    type: "input",
                    gravity: false,
                    label: "同行人手机号码",
                    icon: "",
                    placeholder: "请输入手机号码",
                    name: "phone",
                    required: true
                },
                {
                    gravity: true,
                    type: "button",
                    value: "添加",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                    id: "add",
                    name: "add",
                    full: true,
                }
            ]
        });

        this.form = form;

        form.events.on("ButtonClick", function(id, e) {
            _12307.api.add_passenger(form.getValue(), function(data) {
                dhxWindow.hide();
                cb_ok(data);
            });
        });

        // dhxWindow.show();
        this.show_window();
        dhxWindow.attach(form);
    }
}