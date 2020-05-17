_elpis.admin.api = {
    url: "http://cn.mylab.cc:24080/api/",

    default_cb_ok: _elpis.alert.__cb_show_ok_msg,
    default_cb_err: _elpis.alert.__cb_show_err_msg,

    get_all_user: function(cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "users";
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    get_all_role: function(cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "roles";
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    add_user_role: function(username, role, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "user/" + username + "/role";
        var request_form = { "role": role };
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    remove_user_role: function(username, role, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "user/" + username + "/role/" + role;
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },

}
