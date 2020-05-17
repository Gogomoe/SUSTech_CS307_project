_elpis.test =  class {
    show_window() {
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor() {
        var dhxWindow = new dhx.Window({
            title: "测试",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 360
        });

        this.dhxWindow = dhxWindow;

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

        var list = new dhx.List(null, {
            multiselection: false,
            css: "dhx_widget--bordered",
            template: template,
            keyNavigation: true,
            itemHeight: 64,
            height: 320,
            dragMode: "target"
        });

        this.list = list;

        list.events.on("AfterEditEnd", function(value, id) {
            console.log(value);
            console.log(id);
        });

        

        dhxWindow.attach(list);
        this.show_window();
    }
}