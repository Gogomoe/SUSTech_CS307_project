_12307.ticket = class {
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

        list.data.parse(api_data["tickets"]);

        list.events.on("click", function(id) {
            var selected_items = list.selection.getItem();
            if (selected_items) app.dhxWindow.header.data.update("return", { disabled: false });
            else app.dhxWindow.header.data.update("return", { disabled: true });
        });

        this.dhxWindow.attach(list);
        this.dhxWindow.paint();
    }

    refresh() {
        var app = this;
        _12307.api.get_active_ticket((data) => {
            Object.keys(data.tickets).forEach((key) => data.tickets[key]["id"] = String(data.tickets[key]["id"]));
            app.reload(data);
        });
        // _12307.api.get_all_passenger((data)=> app.reload(data) );
    }

    show_window() {
        var app = this;
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor() {
        var app = this;
        var dhxWindow = new dhx.Window({
            title: "已购买车票",
            modal: false,
            header: true,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 400,
            height: 400
        });

        this.dhxWindow = dhxWindow;

        var template = function (item) {
            var depart_time = moment(item["departTime"], moment.ISO_8601).format("YYYY-MM-DD HH:mm");
            var arrive_time = moment(item["arriveTime"], moment.ISO_8601).format("YYYY-MM-DD HH:mm");
            var template = "";
            if(!item.valid) template += "<s>"
            template += "<div class='list_item'>";
            template += "<div class='item_name'>"
            template += "<span class='mdi mdi-seat-passenger'></span>";
            template += item.passenger.passenger_name + " ";
            template += "<span class='mdi mdi-train'></span>";
            template += item.train.static.code;
            template += "</div>";
            template += "<div class='item_categories'>";
            template += "<span class='mdi mdi-export'></span>";
            template += item.departStation.name + " @ " + depart_time;
            template += "<br>"
            template += "<span class='mdi mdi-import'></span>";
            template += item.arriveStation.name + " @ " + arrive_time;
            template += "</div>";
            template += "</div>";
            if(!item.valid) template += "</s>"
            
            return template;
        }

        this.template = template;

        dhxWindow.header.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "已选定票退款",
            id: "return",
            disabled: true
        }, 2);

        dhxWindow.header.events.on("click", function (id) {
            if (id == "return") {
                app.__return_selected_tickets();
            }
        });

        this.show_window();
        this.refresh();
    }

    get_selected_tickets() { return this.list.selection.getItem(); }

    __return_selected_tickets() {
        var names = "";
        var app = this;
        var selected_items = this.get_selected_tickets();
        selected_items.forEach((item) => names += item.passenger.passenger_name + "去往" + item.arriveStation.name + " " );
        names += "的车票";
        _elpis.alert.__show_confirm("警告", "是否确认退票 " + names, (confirm) => {
            if(confirm) selected_items.forEach((item) => {
                _12307.api.return_ticket(parseInt(item.id), ()=>app.refresh());
            });
        });
    }
}
