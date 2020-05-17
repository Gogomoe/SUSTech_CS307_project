_12307.admin.station_manager = class {

    reload(api_data) {
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
                    "value": "删除车站",
                    "icon": "mdi mdi-home-minus",
                }
            ]
        );

        this.context_menu = context_menu;

        context_menu.events.on("Click", function(id,e) {
            if(id == "remove") {
                var station = app.get_selected_station();
                _elpis.alert.__show_confirm("警告", "是否要删除车站: " + station.name, (confirm) => {
                    if(confirm) _12307.admin.api.remove_station(station.id, ()=>{ _elpis.alert.__show_message("车站增加成功"); app.refresh(); });
                });
            }
        });

        list.data.parse(api_data["stations"]);

        list.events.on("Click", function(id, e) {
            e.preventDefault();
            context_menu.showAt(e);
        });

        // this.dhxWindow.attach(list);
        this.layout.getCell("search_result").attach(list);
        this.dhxWindow.paint();
    }

    refresh() {
        var app = this;
        _12307.api.search_station_by_keyword(this.form.getValue().keyword, (data) => {
            Object.keys(data.stations).forEach((key) => data.stations[key]["id"] = String(data.stations[key]["id"]));
            app.reload(data);
        });
    }

    show_window() {
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_on_confirm = () => this.dhxWindow.hide()) {
        var app = this;
        var dhxWindow = new dhx.Window({
            title: "火车站管理",
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
                            label: "关键词",
                            labelInline: true,
                            icon: "",
                            placeholder: "车站或城市名",
                            value: "深圳",
                            required: true
                        },
                        {
                            type: "button",
                            name: "search",
                            gravity: false,
                            full: true,
                            value: "查询",
                            size: "medium",
                            view: "flat",
                            color: "primary",
                        }
                    ]
                }
            ]
        });

        this.form = form;

        form.events.on("ButtonClick", function(id,e) {
            app.refresh();
        });

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "增加车站",
            id: "add",
        }, 2);

        dhxWindow.header.events.on("click", function (id) {
            if(id == "add") {
                new _12307.admin.station_add(()=>{ _elpis.alert.__show_message("车站增加成功"); app.refresh(); });
            }
        });

        layout.getCell("search_form").attach(form);

        var template = function (item) {
            var template = "<div class='list_item'>";
            
            template += "<div class='item_name'>";
            template += "<span class='mdi mdi-subway'></span>";
            template += item.name;
            template += "</div>";

            template += "<div class='item_categories'>";
            template += "<span class='mdi mdi-city-variant'></span>";
            template += item.city;

            template += "<span class='mdi mdi-code-greater-than'></span>";
            template += item.code;
            template += "</div>";

            template += "</div>";
            return template;
        }

        this.template = template;

        dhxWindow.attach(layout);
        this.show_window();
    }

    get_selected_station() { return this.list.selection.getItem(); }
    
}

_12307.admin.station_add = class {
    
    show_window() {
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_ok = () => _elpis.alert.__show_message("车站增加成功")) {
        var dhxWindow = new dhx.Window({
            title: "增加车站",
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
                    label: "车站名",
                    icon: "",
                    placeholder: "请输入车站名",
                    name: "name",
                    required: true
                },
                {
                    type: "input",
                    gravity: false,
                    label: "城市名",
                    icon: "",
                    placeholder: "请输入城市名",
                    name: "city",
                    required: true
                },
                {
                    type: "input",
                    gravity: false,
                    label: "电报码",
                    icon: "",
                    placeholder: "请输入电报码",
                    name: "code",
                    required: true
                },
                {
                    gravity: true,
                    type: "button",
                    value: "增加车站",
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
            _12307.admin.api.add_station(form.getValue(), (data) => cb_ok() );
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}