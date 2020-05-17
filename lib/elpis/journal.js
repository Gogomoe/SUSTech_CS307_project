/*
_elpis.console = class {
    constructor(text = "") {
        this.dhxWindow = new dhx.Window({
            title: "Console",
            modal: false,
            header: true,
            closable: true,
            resizable: true,
            movable: true,
            width: 1200,
            height: 800
        });
    
        this.form = new dhx.Form("form_container", {
            rows: [
                {
                    type: "textarea",
                    id: "text",
                    value: text
                }
            ]
        });
        this.text_buffer = text;
        dhxWindow.show();
        dhxWindow.attach(form);
    }

    append_text(text) {
        this.text_buffer += text;
        this.form.getItem("text").setValue(this.text_buffer);
    }
}
*/

_elpis.journal = {
    text_buffer: "",

    dhxWindow: new dhx.Window({
        title: "日志",
        modal: false,
        header: false,
        closable: true,
        resizable: true,
        movable: true,
        width: 600,
        height: 400
    }),

    form: new dhx.Form("form_container", {
        rows: [
            {
                type: "textarea",
                id: "text",
                value: ""
            }
        ]
    }),

    __append_text: function(text) {
        this.text_buffer += text;
        this.form.getItem("text").setValue(this.text_buffer);
    },

    __show_window: function() {
        this.dhxWindow.show();
    },

    __init: function() {
        this.dhxWindow.attach(this.form);

        _elpis.sidebar.__add_entry({
            type: "button",
            id: "console",
            value: "日志"
        }, () => { _elpis.journal.__show_window(); });
    }
}

_elpis.journal.__init();