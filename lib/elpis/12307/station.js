_12307.station_search = class {

    reload(api_data) {
        var app = this;
        var list = new dhx.List(null, {
            multiselection: false,
            css: "dhx_widget--bordered",
            template: this.template,
            keyNavigation: true,
            itemHeight: 64,
            height: 540,
            dragMode: "source",
            dragCopy: true
        });

        this.list = list;

        list.data.parse(api_data["stations"]);

        list.events.on("click", function(id) {
            var selected_items = list.selection.getItem();
            // if (selected_items) app.dhxWindow.header.data.update("return", { disabled: false });
            // else app.dhxWindow.header.data.update("return", { disabled: true });
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
            title: "火车站查询",
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
