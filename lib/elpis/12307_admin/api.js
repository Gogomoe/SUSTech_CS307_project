_12307.admin.api = {
    url: "http://cn.mylab.cc:24080/api/",

    default_cb_ok: _elpis.alert.__cb_show_ok_msg,
    default_cb_err: _elpis.alert.__cb_show_err_msg,

    add_station: function(request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "station";
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    remove_station: function(station_id, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "station/" + station_id;
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },

    add_train_static: function(request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train/static";
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    add_train: function(request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train";
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    remove_train_static: function(train_static_id, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train/" + train_static_id;
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },

    remove_train: function(train_id, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train/" + train_id;
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },
}
