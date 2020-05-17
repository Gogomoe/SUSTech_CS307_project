var tree_data = [
    {
        "icon": {
            "folder": "fas fa-book",
            "openFolder": "fas fa-book-open",
            "file": "fas fa-file"
        },
    },
    {
        "value": "Department of CSE",
        "id": "Department of CSE",
        "opened": true,
        "items": [
            {
                "value": "Hao's Lab",
                "id": "Hao's Lab",
                "opened": true,
                "items": [
                    {
                        "value": "Project Elpis",
                        "id": "Project Elpis",
                        "opened": true,
                    }
                ]
            }
        ]
    }
]
_elpis.openWith = function () {
    var dhxWindow = new dhx.Window({
        title: "Open With",
        modal: false,
        header: true,
        footer: false,
        closable: true,
        resizable: true,
        movable: true,
        width: 320,
        height: 240
    });

    var form = new dhx.Form(null, {
        // css: "dhx_widget--bordered",
        css: "dhx_layout-cell--no-border",
        rows: [
            {
                align: "center",
                cols: [
                    {
                        gravity: false,
                        type: "button",
                        value: "Open with Spreadsheet",
                        id: "spreadsheet"
                    }
                ]
            },
            {
                align: "center",
                cols: [
                    {
                        gravity: false,
                        type: "button",
                        value: "Open with Notepad",
                        id: "notepad"
                    }
                ]
            },
            {
                align: "center",
                cols: [
                    {
                        gravity: false,
                        type: "button",
                        value: "Open with Dashboard",
                        id: "dashboard"
                    }
                ]
            }
        ]
    });

    this.__init = function () {

        form.events.on("ButtonClick", function(id,e) {
            dhxWindow.hide();
            if(id=="spreadsheet") {
                var spreadsheet = new _elpis.spreadsheet();
            } else if(id=="dashboard") {
                var dashboard = new _elpis.dashboard();
            } else if(id=="notepad") {
                var notepad = new _elpis.notepad();
            }
        });

        dhxWindow.show();
        dhxWindow.attach(form);
    }

    this.__init();
}

_elpis.browser = function () {
    var dhxWindow = new dhx.Window({
        title: "Browser",
        modal: false,
        header: true,
        footer: true,
        closable: true,
        resizable: true,
        movable: true,
        width: 500,
        height: 500
    });

    var tree = new dhx.Tree("tree");

    var isFullScreen = false;
    var oldSize = null;
    var oldPos = null;
    var taskID;

    this.__init = function() {
        taskID = _elpis.taskbar.__add_task("Browser", "", dhxWindow);
        dhxWindow.header.data.add({ icon: "mdi mdi-fullscreen", id: "fullscreen" }, 2);
        dhxWindow.header.events.on("click", function (id) {
            if (id === "fullscreen") {
                if (isFullScreen) {
                    dhxWindow.setSize(oldSize.width, oldSize.height);
                    dhxWindow.setPosition(oldPos.left, oldPos.top);
                } else {
                    oldSize = dhxWindow.getSize();
                    oldPos = dhxWindow.getPosition();
                    dhxWindow.setFullScreen();
                }
                isFullScreen = !isFullScreen;
            }
        });
        dhxWindow.footer.data.add({
            type: "spacer",
        });

        dhxWindow.footer.data.add({
            type: "button",
            view: "flat",
            size: "medium",
            color: "primary",
            value: "Open With",
            id: "openwith",
        });
        dhxWindow.footer.events.on("click", function (id) {
            dhxWindow.hide();
            openWith = new _elpis.openWith();
        });
        dhxWindow.show();
        dhxWindow.attach(tree);
        tree.data.parse(tree_data);
    }
    this.__init();
}






/*
var
var cfg = {
    id: "btn",
    type: "button",
    value: "BUTTON",
    tooltip: "Tooltip",
    icon: "dxi dxi-plus",
    count: "20",
    countColor: "success",
    view: "flat",
    size: "medium",
    color: "primary",
    full: false,
    circle: false,
    loading: false
};
ribbon.data.add(cfg);
ribbon.data.add({
    type: "button",
    view: "flat",
    size: "medium",
    color: "primary",
    value: "accept",
    id: "accept",
});

//
*/