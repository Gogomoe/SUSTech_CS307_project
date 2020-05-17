_elpis.loginform = class {
    
    show_window() {
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(params) {
        var dhxWindow = new dhx.Window({
            title: "请登录",
            modal: true,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 360
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "用户名",
                    icon: "",
                    placeholder: "Username",
                    name: "username",
                    required: true
                },
                {
                    type: "input",
                    gravity: false,
                    inputType: "password",
                    label: "密码",
                    placeholder: "Password",
                    name: "password",
                    required: true
                },
                {
                    gravity: true,
                    align: "between",
                    cols: [
                        {
                            gravity: false,
                            type: "button",
                            value: "注册",
                            size: "medium",
                            view: "link",
                            color: "primary",
                            id: "signin",
                            name: "signin",
                        },
                        {
                            
                            gravity: false,
                            type: "button",
                            value: "忘记密码",
                            size: "medium",
                            view: "link",
                            color: "primary",
                            id: "forgot",
                            name: "forgot",
                        }
                    ]
                },
                {
                    gravity: true,
                    type: "button",
                    value: "登录",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                    id: "login",
                    name: "login",
                    full: true,
                }
            ]
        });

        this.form = form;

        form.events.on("ButtonClick", function(id, e) {
            if(id == "login") {
                _elpis.api.login(form.getValue(), function(data) {
                    dhxWindow.hide();
                    _elpis.sidebar.__update_user_info();
                    _elpis.alert.__show_message("登录成功");
                });
            } else if(id == "signin") {
                new _elpis.signinform();
            } else {
                _elpis.alert.__show_alert_no_implement();
            }
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}

_elpis.signinform = class {
    
    show_window() {
        setTimeout( () => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(cb_ok = () => _elpis.alert.__show_message("注册成功，请登录")) {
        var dhxWindow = new dhx.Window({
            title: "注册",
            modal: false,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 320,
            height: 360
        });

        this.dhxWindow = dhxWindow;

        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    gravity: false,
                    label: "用户名",
                    icon: "",
                    placeholder: "Username",
                    name: "username",
                    required: true
                },
                {
                    type: "input",
                    gravity: false,
                    inputType: "password",
                    label: "密码",
                    placeholder: "Password",
                    name: "password",
                    required: true
                },
                {
                    type: "checkbox",
                    name: "agree",
                    id: "agree",
                    checked: true,
                    required: true,
                    label: "同意《12307用户服务协议》",
                    labelInline: true,
                    value: "同意《12307用户服务协议》"
                },
                {
                    gravity: true,
                    type: "button",
                    value: "注册",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                    id: "login",
                    name: "login",
                    full: true,
                }
            ]
        });

        this.form = form;

        form.events.on("ButtonClick", function(id, e) {
            if(id == "login") {
                _elpis.api.signin(form.getValue(), (data)=> {
                    dhxWindow.hide();
                    cb_ok();
                });
            } else {
                _elpis.alert.__show_alert_no_implement();
            }
        });

        dhxWindow.attach(form);
        this.show_window();
    }
}

