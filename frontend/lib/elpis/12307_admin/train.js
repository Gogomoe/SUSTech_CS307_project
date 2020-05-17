_12307.admin.train_add = class {

    reload(api_data, roles) {

    }

    show_window() {
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
            this.form.getItem("date")._popup._popup.classList.add("dhx_popup--window_active");
        }, 100);
    }

    constructor(params, cb_on_confirm = () => this.dhxWindow.hide()) {
        var app = this;
        var dhxWindow = new dhx.Window({
            title: "添加列车",
            modal: false,
            footer: true,
            resizable: true,
            movable: true,
            closable: true,
            width: 800,
            height: 580
        });

        this.dhxWindow = dhxWindow;

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "加载模板",
            id: "template_add",
        }, 2);

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "删除车次",
            id: "train_remove",
        }, 2);

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "删除线路",
            id: "train_static_remove",
        }, 2);

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "创建车次",
            id: "train_new",
        }, 2);

        var layout = new dhx.Layout("layout", {
            css: "dhx_layout-cell--no-border",
            cols: [
                {
                    id: "info_form",
                    css: "dhx_layout-cell--no-border",
                    header: "列车基本信息",
                    gravity: false,
                    weight: 100
                },
                {
                    id: "station_list",
                    gravity: true,
                    header: "列车线路 (请拖拽'车站查询'中的站点到此处)",
                    css: "dhx_layout-cell--no-border"
                }
            ]
        });

        this.layout = layout;

        var form = new dhx.Form(null, {
            css: "dhx_widget--no-bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "车次",
                    labelInline: true,
                    icon: "",
                    placeholder: "请输入车次",
                    id: "code",
                    name: "code",
                    required: true
                },
                {
                    type: "input",
                    gravity: false,
                    label: "列车类型",
                    labelInline: true,
                    icon: "",
                    placeholder: "请输入列车类型",
                    id: "type",
                    name: "type",
                    required: true
                },
                {
                    id: "date",
                    type: "datepicker",
                    name: "date",
                    gravity: true,
                    value: "2020-05-01",
                    dateFormat: "%Y-%m-%d",
                    label: "开行日期",
                    labelInline: true,
                },
                {
                    gravity: false,
                    align: "between",
                    cols: [
                        {
                            type: "checkbox",
                            name: "seat1",
                            id: "seat1",
                            gravity: false,
                            checked: false,
                            label: "二等座",
                            labelInline: true,
                            value: "",
                        },
                        {
                            type: "input",
                            name: "seat1_cnt",
                            id: "seat1_cnt",
                            gravity: false,
                            label: "座位数",
                            labelInline: true,
                            icon: "",
                            value: "0",
                            disabled: true
                        }
                    ]
                },
                {
                    gravity: false,
                    align: "between",
                    cols: [
                        {
                            type: "checkbox",
                            name: "seat2",
                            id: "seat2",
                            gravity: false,
                            checked: false,
                            label: "一等座",
                            labelInline: true,
                            value: "",
                        },
                        {
                            type: "input",
                            name: "seat2_cnt",
                            id: "seat2_cnt",
                            gravity: false,
                            label: "座位数",
                            labelInline: true,
                            icon: "",
                            value: "0",
                            disabled: true
                        }
                    ]
                },
                {
                    gravity: false,
                    align: "between",
                    cols: [
                        {
                            type: "checkbox",
                            name: "seat3",
                            id: "seat3",
                            gravity: false,
                            checked: false,
                            label: "硬卧",
                            labelInline: true,
                            value: "",
                        },
                        {
                            type: "input",
                            name: "seat3_cnt",
                            id: "seat3_cnt",
                            gravity: false,
                            label: "座位数",
                            labelInline: true,
                            icon: "",
                            value: "0",
                            disabled: true
                        }
                    ]
                },
                {
                    gravity: false,
                    align: "between",
                    cols: [
                        {
                            type: "checkbox",
                            name: "seat4",
                            id: "seat4",
                            gravity: false,
                            checked: false,
                            label: "软卧",
                            labelInline: true,
                            value: "",
                        },
                        {
                            type: "input",
                            name: "seat4_cnt",
                            id: "seat4_cnt",
                            gravity: false,
                            label: "座位数",
                            labelInline: true,
                            icon: "",
                            value: "0",
                            disabled: true
                        }
                    ]
                },
            ]
        });

        this.form = form;

        var template = function (item) {
            var template = "<div class='list_item'>";

            template += "<div class='item_name'>";
            template += "<span class='mdi mdi-subway'></span>";
            template += item.name;


            template += "</div>";


            template += "<div class='item_categories'>";
            template += "<span class='mdi mdi-import'></span>";
            template += item.arriveTime ? item.arriveTime : "未设置";
            template += " ";
            template += "<span class='mdi mdi-export'></span>";
            template += item.departTime ? item.departTime : "未设置";
            template += "</div>";

            template += "</div>";
            return template;
        }

        this.template = template;

        var list = new dhx.List(null, {
            multiselection: false,
            css: "dhx_widget--bordered",
            template: template,
            keyNavigation: true,
            itemHeight: 64,
            height: 540,
            dragMode: "target",
        });

        this.list = list;

        var context_menu = new dhx.ContextMenu(null, {css: "dhx_widget--bg_gray"});
        context_menu.data.parse(
            [
                {
                    "type": "menuItem",
                    "id": "edit",
                    "value": "编辑设置",
                    "icon": "mdi mdi-calendar-edit",
                },
                {
                    "type": "menuItem",
                    "id": "remove",
                    "value": "删除路径点",
                    "icon": "mdi mdi-calendar-minus",
                }
            ]
        );

        this.context_menu = context_menu;

        context_menu.events.on("Click", function (id, e) {
            var node = app.get_selected_station();
            if (id == "remove") {
                list.data.remove(node.id);

            } else if (id.includes("edit")) {
                new _12307.admin.node_edit(node, form.getValue(), (node_form) => {
                    // console.log(node_form);
                    list.data.update(node_form.id, node_form);
                    dhxWindow.paint();
                })
            }
        });

        list.events.on("Click", function (id, e) {
            e.preventDefault();
            context_menu.showAt(e);
        });

        dhxWindow.footer.data.add({
            type: "spacer"
        });

        dhxWindow.footer.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "确认添加",
            id: "ok",
        });

        dhxWindow.header.events.on("click", function (id) {
            if (id === "template_add") {
                new _12307.admin.template_load((data) => {
                    var trainline = data['trainline'];

                    form.getItem("type").setValue(trainline.static.type);
                    var code = trainline.static.code;
                    if (code.includes("$")) {
                        var codeArr = code.split("$");
                        var version = parseInt(codeArr[1]);
                        code = codeArr[0] + '$' + (version + 1);
                    } else {
                        code += "$1";
                    }
                    form.getItem("code").setValue(code);

                    ["1", "2", "3", "4"].forEach(key => {
                        if (trainline.seat[key]) {
                            form.getItem(`seat${key}_cnt`).setValue(trainline.seat[key]);
                            form.getItem(`seat${key}_cnt`).config.disabled = false;
                            form.getItem(`seat${key}`).setValue(true);
                        } else {
                            form.getItem(`seat${key}_cnt`).setValue(0);
                            form.getItem(`seat${key}_cnt`).config.disabled = true;
                            form.getItem(`seat${key}`).setValue(false);
                        }
                    });
                    list.data.removeAll();
                    for (var item of trainline.stations) {
                        var it = {};
                        it.id = item.station.id;
                        it.name = item.station.name;
                        it.city = item.station.city;
                        it.code = item.station.code;
                        it.arriveTime = item.arriveTime;
                        it.departTime = item.departTime;
                        for (var key in item.prices) {
                            it[`seat${key}_price`] = item.prices[key];
                        }
                        list.data.add(it);
                    }

                });
            }

            if (id === "train_remove") {
                new _12307.admin.train_remove();
            }

            if (id === "train_static_remove") {
                new _12307.admin.train_static_remove();
            }

            if (id === "train_new") {
                new _12307.admin.train_new((data) => {
                    _elpis.alert.__show_message(`添加车次成功, 车次id:${data.train}`);
                });
            }
        });

        dhxWindow.footer.events.on("click", function (id) {
            var request_form = form.getValue();
            request_form["seat"] = {};
            var stations = app.get_all_station();

            for (let i = 0; i < stations.length; ++i) {
                stations[i]["station"] = parseInt(stations[i]["id"]);
                stations[i]["prices"] = {};
            }

            ["1", "2", "3", "4"].forEach((key) => {
                if (request_form["seat" + key]) {
                    request_form["seat"][key] = parseInt(request_form["seat" + key + "_cnt"]);
                    for (let i = 0; i < stations.length; ++i) {
                        stations[i]["prices"][key] = parseInt(stations[i]["seat" + key + "_price"]);
                    }
                }
            });
            request_form["stations"] = stations;


            _12307.admin.api.add_train_static(request_form, (data) => {
                _elpis.alert.__show_message(`添加线路成功, 线路id:${data.static}`);
                _12307.admin.api.add_train({date: request_form["date"], static: data["static"]}, (data2) => {
                    _elpis.alert.__show_message(`添加车次成功, 车次id:${data2.train}`);
                })
            })

        });

        form.events.on("Change", function (name, new_value) {
            if (name.includes("seat") && !name.includes("cnt")) {
                var config = form.getItem(name + "_cnt").config;
                config["disabled"] = !new_value;
                form.getItem(name + "_cnt").setConfig(config);
            }
        });

        layout.getCell("info_form").attach(form);
        layout.getCell("station_list").attach(list);

        dhxWindow.attach(layout);
        this.show_window();

        // app.refresh();
    }

    get_selected_station() {
        return this.list.selection.getItem();
    }

    get_all_station() {
        return this.list.data._order;
    }
}

_12307.admin.node_edit = class {

    show_window() {
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(node_form, train_form, cb_ok = (form) => {
    }) {
        var dhxWindow = new dhx.Window({
            title: "路径点设置",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 600
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "到达时间",
                    placeholder: "请输入时间，如23:00:00",
                    icon: "",
                    name: "arriveTime",
                    required: true,
                },
                {
                    type: "input",
                    gravity: false,
                    label: "出发时间",
                    placeholder: "请输入时间，如46:30:00",
                    icon: "",
                    name: "departTime",
                    required: true,
                },
                {
                    type: "input",
                    gravity: false,
                    label: "二等座价格",
                    placeholder: "请输入价格",
                    icon: "",
                    name: "seat1_price",
                    required: true,
                    disabled: !train_form.seat1
                },
                {
                    type: "input",
                    gravity: false,
                    label: "一等座价格",
                    placeholder: "请输入价格",
                    icon: "",
                    name: "seat2_price",
                    required: true,
                    disabled: !train_form.seat2
                },
                {
                    type: "input",
                    gravity: false,
                    label: "硬卧价格",
                    placeholder: "请输入价格",
                    icon: "",
                    name: "seat3_price",
                    required: true,
                    disabled: !train_form.seat3
                },
                {
                    type: "input",
                    gravity: false,
                    label: "软卧价格",
                    placeholder: "请输入价格",
                    icon: "",
                    name: "seat4_price",
                    required: true,
                    disabled: !train_form.seat4
                },
                {
                    gravity: false,
                    type: "button",
                    value: "确认",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                    id: "confirm",
                    name: "confirm",
                    full: true,
                }
            ]
        });

        this.form = form;

        form.setValue(node_form);

        form.events.on("ButtonClick", function (id, e) {
            var new_form = form.getValue();
            Object.keys(new_form).forEach((key) => {
                node_form[key] = new_form[key];
            })
            cb_ok(node_form);
            dhxWindow.hide();
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}

_12307.admin.template_load = class {

    show_window() {
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_ok) {
        var dhxWindow = new dhx.Window({
            title: " 加载模板",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 380
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "线路id",
                    icon: "",
                    placeholder: "请输入线路id",
                    id: "static_id",
                    name: "static_id",
                    required: true
                },
                {
                    gravity: true,
                    type: "button",
                    value: "加载线路",
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

        form.events.on("ButtonClick", function (id, e) {
            _12307.admin.api.get_train_static(form.getValue()['static_id'], (data) => cb_ok(data));
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}

_12307.admin.train_remove = class {

    show_window() {
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_ok) {
        var dhxWindow = new dhx.Window({
            title: "删除车次",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 380
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "车次id",
                    icon: "",
                    placeholder: "请输入车次id",
                    id: "train_id",
                    name: "train_id",
                    required: true
                },
                {
                    gravity: true,
                    type: "button",
                    value: "删除车次",
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

        form.events.on("ButtonClick", function (id, e) {
            _12307.admin.api.remove_train(form.getValue()['train_id'], cb_ok);
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}
_12307.admin.train_new = class {

    show_window() {
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
            this.form.getItem("date")._popup._popup.classList.add("dhx_popup--window_active");
        }, 100);
    }

    constructor(cb_ok) {
        var dhxWindow = new dhx.Window({
            title: "添加车次",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 380
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "线路id",
                    icon: "",
                    placeholder: "请输入线路id",
                    id: "static_id",
                    name: "static_id",
                    required: true
                },
                {
                    id: "date",
                    type: "datepicker",
                    name: "date",
                    gravity: true,
                    value: "2020-05-01",
                    dateFormat: "%Y-%m-%d",
                    label: "开行日期",
                    labelInline: true,
                },
                {
                    gravity: true,
                    type: "button",
                    value: "添加车次",
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

        form.events.on("ButtonClick", function (id, e) {
            _12307.admin.api.add_train({
                static: parseInt(form.getValue()['static_id']),
                date: form.getValue()['date']
            }, cb_ok);
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}

_12307.admin.train_static_remove = class {

    show_window() {
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_ok) {
        var dhxWindow = new dhx.Window({
            title: "删除线路",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 380
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "线路id",
                    icon: "",
                    placeholder: "请输入线路id",
                    id: "static_id",
                    name: "static_id",
                    required: true
                },
                {
                    gravity: true,
                    type: "button",
                    value: "删除线路",
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

        form.events.on("ButtonClick", function (id, e) {
            _12307.admin.api.remove_train_static(form.getValue()['static_id'], cb_ok);
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}