_elpis.api = {
    url: "http://cn.mylab.cc:24080/api/",

    default_cb_ok: _elpis.alert.__cb_show_ok_msg,
    default_cb_err: _elpis.alert.__cb_show_err_msg,

    session: {
        session: false
    },

    rand_int: function(lower=1, upper=1E8) {
        return Math.floor( Math.random()*(upper-lower)+lower );
    },

    is_logged_in: function() {
        return this.session.session
    },
    
    warp_require_login: function(func) {
        return function(...params) {
            if(_elpis.api.is_logged_in()) func(...params);
            else {
                _elpis.alert.__show_message("请先登录后操作");
                new _elpis.loginform();
            }
        }
    },

    get_session: function(cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "session";
        dhx.ajax.get(request_url).then((data)=>{ cb_ok(data); _elpis.api.session = data; }).catch(cb_err);
    },

    login: function(login_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "session";
        dhx.ajax.post(request_url, login_form).then(cb_ok).catch(cb_err);
    },

    logout: function(cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "session";
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },

    get_user_info: function(username, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "user/" + username;
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    signin:  function(signin_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "user";
        dhx.ajax.post(request_url, signin_form).then(cb_ok).catch(cb_err);
    },

    

    /*test: function() {
        var request_url = this.url + "user/user";
        dhx.ajax.get(request_url).then(function (data) {
            console.log(data);
        }).catch(function (err) {
            if(cb_err) cb_err(err);
        });
    }*/

}
