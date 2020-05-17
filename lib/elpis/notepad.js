_elpis.notepad = function() {
    var dhxWindow = new dhx.Window({
        title: "Notepad",
        modal: false,
        header: true,
        footer: true,
        closable: true,
        resizable: true,
        movable: true,
        width: 1200,
        height: 800
    });

    var form = new dhx.Form("form_container", {
        rows: [
            {
                type: "textarea",
                //label: "textarea",
                //labelInline: true,
                //labelWidth: "70px",
                value: "Some nice text",
                // width: 1120
            }
        ]
    });

    var isFullScreen = false;
    var oldSize = null;
    var oldPos = null;
    var taskID;

    this.__init = function() {
        taskID = _elpis.taskbar.__add_task("Notepad", "", dhxWindow);
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
            value: "Button",
            id: "button",
        });
        dhxWindow.footer.events.on("click", function (id) {
            dhxWindow.hide();
        });

        dhxWindow.show();
        dhxWindow.attach(form);
    }
    this.__init();
}