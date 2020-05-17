_12307.api = {
    url: "http://cn.mylab.cc:24080/api/",

    default_cb_ok: _elpis.alert.__cb_show_ok_msg,
    default_cb_err: _elpis.alert.__cb_show_err_msg,

    // https://github.com/Gogomoe/SUSTech_CS307_project/blob/backend/backend/src/test/http/ticket.http

    search_train_by_keyword: function(request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train/from/" + request_form["from"] + "/to/" + request_form["to"] + "/date/" + request_form["date"];
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    get_active_ticket: function(cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "tickets/active";
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    get_all_passenger: function(cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "user/" + _elpis.api.session.user.username + "/passenger";
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    get_train_static_timetable: function(static_train_id=1234, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train/static/" + static_train_id + "/timetable";
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    get_train_static_trainline: function(static_train_id=1234, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train/static/" + static_train_id + "/trainline";
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    get_train_timetable: function(train_id=1234, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "train/" + train_id + "/timetable";
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    add_passenger: function(request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "passenger";
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    modify_passenger: function(passenger_id, request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "passenger/" + passenger_id;
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    remove_passenger: function(passenger_id, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "passenger/" + passenger_id;
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },

    purchase_ticket: function(request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "ticket";
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    return_ticket: function(ticket_id, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "ticket/" + ticket_id;
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },

    search_station_by_keyword: function(keyword, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "station/search/" + keyword;
        dhx.ajax.get(request_url).then(cb_ok).catch(cb_err);
    },

    add_station: function(request_form, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "station";
        dhx.ajax.post(request_url, request_form).then(cb_ok).catch(cb_err);
    },

    remove_station: function(station_id, cb_ok=this.default_cb_ok, cb_err=this.default_cb_err) {
        var request_url = this.url + "station/" + station_id;
        dhx.ajax.delete(request_url).then(cb_ok).catch(cb_err);
    },

};
