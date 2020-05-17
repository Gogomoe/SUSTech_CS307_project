_elpis.alert = {
    __show_alert: function(title, message) {
        dhx.alert({
            header: title,
            text: message,
            buttonsAlignment: "center",
            buttons: ["ok"],
        });
    },

    __show_confirm: function(title, message, cb_func) {
        dhx.confirm({
            header: title,
            text: message,
        }).then( function(i) {
            if(cb_func) cb_func(i);
        });
    },

    __show_message: function(message) {
        dhx.message({text: message, icon: "dxi-close"});
    },

    __show_alert_no_implement: function() {
        this.__show_alert("错误", "该功能施工中");
    },

    __cb_show_err_msg: function(err) {
        if(typeof err == "object") {
            try {
                msg_json = JSON.parse(err["message"])
                _elpis.alert.__show_alert("错误", msg_json["error"]["message"]);
            } catch (error) {
                _elpis.alert.__show_alert("错误", JSON.stringify(err) );
            }
        }
        else _elpis.alert.__show_alert("错误", err);
    },

    __cb_show_ok_msg: function(data) {
        if(typeof data == "object") _elpis.alert.__show_alert("信息", JSON.stringify(data) );
        else _elpis.alert.__show_alert("信息", data);
    },
}