_elpis.sidebar = {
    sidebar: new dhx.Sidebar(null, {
        css: "dhx_widget--border_right"
    }),

    callback: {},

    __add_entry: function(config, cb_func = ()=>{}) {
        this.callback[ config["id"] ] = cb_func;
        this.sidebar.data.add(config);
    },

    __update_entry: function(config, cb_func = ()=>{}) {
        this.callback[ config["id"] ] = cb_func;
        this.sidebar.data.update(config);
    },

    __update_user_info: function(title, subtitle, avatar) {
        if(title && subtitle && avatar) {
            this.sidebar.data.update("userInfo", {
                html: "<div class='user-info_container'>" +
                    "<img class='user-info_avatar' src='" + avatar + "'/>" +
                    "<div class='user-info_title'>" +
                    title +
                    "</div>" +
                    "<div class='user-info_contact'>" +
                    subtitle +
                    "</div>" +
                    "</div>"
            });
            if(subtitle == "普通用户" || subtitle.includes("登录")) {
                this.sidebar.disable("user_manager");
                this.sidebar.disable("station_manager");
                this.sidebar.disable("train_add");
            } else {
                this.sidebar.enable("user_manager");
                this.sidebar.enable("station_manager");
                this.sidebar.enable("train_add");
            }
        } else {
            _elpis.api.get_session(function(data) {
                console.log(data);
                if(!data["session"]) {
                    _elpis.sidebar.__update_user_info("未登录", "点击此处登录", "resource/avatar.jpg");
//                    _elpis.sidebar.__update_user_info(" ^|  ^y   ^u", " ^b  ^g      ^d ^y   ^u", "assets/avatar.jpg");
                } else {
                    url = "http://cn.mylab.cc:28080"; // TODO: Replace this
                    user = data["user"];
                    if(user["roles"].length==0) {
                        _elpis.sidebar.__update_user_info(user["username"], "普通用户", url + user["avatar"]);
                    } else { 
                        _elpis.sidebar.__update_user_info(user["username"], JSON.stringify(user["roles"]), url + user["avatar"]);
                    }
                }
            }, function(data) {
                _elpis.alert.__show_alert("错误", JSON.stringify(data));
            });
        }
    },

    __init: function () {
        var data = [
            {
                // type: "customHTML",
                type: "button",
                id: "userInfo",
                css: "user-info_item",
                html: ""
            }
        ];
        this.callback["userInfo"] = function() {
            if(_elpis.api.is_logged_in()) {
                _elpis.alert.__show_confirm("警告", "是否确认注销？", (confirm) => {
                    if(confirm) _elpis.api.logout(()=>_elpis.sidebar.__update_user_info());
                });
            } else new _elpis.loginform();
        }

        this.sidebar.data.parse(data);
        this.sidebar.events.on("Click", function(id, e) {
            console.log(id);
            if(id in _elpis.sidebar.callback) _elpis.sidebar.callback[id]();
        });
        this.__update_user_info();
    }
};

_elpis.sidebar.__init();
