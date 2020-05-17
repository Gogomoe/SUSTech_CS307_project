_12307.train_search = class {

    reload() {

    }

    show_window() {
        var app = this;
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();

            this.form_by_keyword.getItem("depart_date_picker")._popup._popup.classList.add("dhx_popup--window_active");
        }, 100);
    }

    constructor(params) {
        var dhxWindow = new dhx.Window({
            title: "国内火车查询",
            modal: false,
            footer: false,
            resizable: true,
            movable: true,
            closable: true,
            width: 460,
            height: 540
        });

        var tabbar = new dhx.Tabbar(null, {
            mode: "top",
            css: "dhx_widget--bordered",
            views: [
                {tab: "按城市查找", id: "byKeyword"},
                {tab: "按车次查找", id: "byTrainNo"}
            ]
        });

        var form_by_keyword = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    name: "from",
                    gravity: false,
                    label: "出发地",
                    icon: "",
                    placeholder: "城市名或站名",
                    value: "深圳",
                    required: true
                },
                {
                    type: "input",
                    name: "to",
                    gravity: false,
                    label: "到达地",
                    placeholder: "城市名或站名",
                    value: "上海",
                    required: true
                },
                {
                    id: "depart_date_picker",
                    type: "datepicker",
                    name: "date",
                    gravity: true,
                    value: "2020-05-01",
                    dateFormat: "%Y-%m-%d",
                    label: "出发时间",
                    css: "force_on_top"
                },
                {
                    type: "checkbox",
                    name: "transship",
                    gravity: false,
                    label: "只搜索转乘",
                    labelInline:true
                },
                {
                    type: "button",
                    name: "search",
                    gravity: true,
                    full: true,
                    value: "查询",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                }
            ]
        });

        var form_by_trainno = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "input",
                    name: "trainno",
                    gravity: false,
                    label: "车次",
                    icon: "",
                    placeholder: "T6",
                    required: true
                },
                {
                    type: "datepicker",
                    name: "date",
                    gravity: false,
                    value: "2020-05-01",
                    dateFormat: "%Y-%m-%d",
                    label: "出发时间"
                },
                {
                    type: "button",
                    name: "search",
                    gravity: false,
                    full: true,
                    value: "查询",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                }
            ]
        });

        form_by_keyword.events.on("ButtonClick", function (id, e) {
            if (form_by_keyword.getValue()["transship"]) {
                _12307.api.search_train_transship_by_keyword(form_by_keyword.getValue(), data => {
                    new _12307.transship_info(data)
                })
            } else {
                _12307.api.search_train_by_keyword(form_by_keyword.getValue(), function (data) {
                    new _12307.train_info(data);
                });
            }
        });

        form_by_trainno.events.on("ButtonClick", function (id, e) {
            // console.log(form_by_trainno.getValue());
            _elpis.alert.__show_alert_no_implement();
        });

        tabbar.getCell("byKeyword").attach(form_by_keyword);
        tabbar.getCell("byTrainNo").attach(form_by_trainno);

        dhxWindow.attach(tabbar);
        // dhxWindow.show();
        this.show_window();

        this.dhxWindow = dhxWindow;
        this.tabbar = tabbar;
        this.form_by_keyword = form_by_keyword;
        this.form_by_trainno = form_by_trainno;
    }
}

_12307.transship_info = class {
    reload(api_data) {
        var grid = new dhx.Grid("grid" + _elpis.api.rand_int(), {
            columns: [
                {id: "trainNo", header: [{text: "车次"}]},
                {id: "type", header: [{text: "列车类型"}]},
                {id: "departStation", header: [{text: "出发站"}]},
                {id: "arriveStation", header: [{text: "到达站"}]},
                {id: "departTime", header: [{text: "出发时间"}]},
                {id: "arriveTime", header: [{text: "到达时间"}]},
                {id: "durationTime", header: [{text: "运行时间"}]},
                {id: "price2", header: [{text: "二等座 / 硬座"}]},
                {id: "price1", header: [{text: "一等座 / 软座"}]},
                {id: "priceh", header: [{text: "硬卧"}]},
                {id: "prices", header: [{text: "软卧"}]},
                {id: "operation", header: [{text: "操作"}], htmlEnable: true, width: 64}
            ],
            autoWidth: true,
            // data: dataset
        });

        grid.events.on("CellClick", function (row, column, e) {
            if (row.trainNo === "") {
                return;
            }
            if (column.id !== "operation") {
                new _12307.train_timetable(row);
            } else {
                _elpis.api.warp_require_login(() => {
                    new _12307.train_order(row)
                    if (row.ordinal % 3 == 1)
                        new _12307.train_order(grid._currentData[row.ordinal - 1])
                    else
                        new _12307.train_order(grid._currentData[row.ordinal + 1])
                })();
            }
        });

        var dataset = this.__convert_transship_to_dataset(api_data)
        grid.data.parse(dataset);

        this.dhxWindow.attach(grid);
        this.dhxWindow.paint();
        this.grid = grid;

    }

    show_window() {
        var app = this;
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(api_data) {
        var dhxWindow = new dhx.Window({
            title: "查询结果",
            modal: false,
            header: true,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 1400,
            height: 800
        });

        this.dhxWindow = dhxWindow;

        var isFullScreen = false;
        var oldSize = null;
        var oldPos = null;

        dhxWindow.header.data.add({icon: "mdi mdi-fullscreen", id: "fullscreen"}, 2);
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

        // dhxWindow.show();
        this.show_window();
        this.reload(api_data);

    }

    __seats_toString(seats, id) {
        if (seats[id]) {
            if (seats[id]["count"] > 0) return String(seats[id]["price"]) + "￥";
            else return "暂无余票";
        } else return "";
    }

    __datetime_toString(from_time, to_time, is_duration) {
        var from = moment(from_time, moment.ISO_8601);
        if (!is_duration) from.hours(0).minutes(0).seconds(0);
        var to = moment(to_time, moment.ISO_8601);
        var diff = moment.utc(to.diff(from));
        var day = diff.date() - 1;

        if (day >= 1) {
            if (is_duration) {
                var hours = 24 * day + parseInt(diff.format("HH"));
                return String(hours) + diff.format("时mm分");
            } else return "(" + day + "日后) " + diff.format("HH:mm");
        } else {
            if (is_duration) return diff.format("HH时mm分");
            else return diff.format("HH:mm");
        }
    }

    __convert_transship_to_dataset(data) {
        var dataset = [];
        var transships = data["transships"];
        for (let i = 0; i < transships.length; i++) {
            var entry = transships[i]
            var firstTrain = entry["first_train"];
            var secondTrain = entry["second_train"];

            var departTime = firstTrain["depart_time"];
            var arriveTime = firstTrain["arrive_time"];
            dataset.push({
                "trainNo": firstTrain["train"]["static"]["code"],
                "departStation": firstTrain["depart_station"]["name"],
                "arriveStation": firstTrain["arrive_station"]["name"],
                "departTime": this.__datetime_toString(departTime, departTime, false),
                "arriveTime": this.__datetime_toString(departTime, arriveTime, false),
                "durationTime": this.__datetime_toString(departTime, arriveTime, true),
                "price2": this.__seats_toString(firstTrain["seats"], "1"),
                "price1": this.__seats_toString(firstTrain["seats"], "2"),
                "priceh": this.__seats_toString(firstTrain["seats"], "3"),
                "prices": this.__seats_toString(firstTrain["seats"], "4"),
                "type": firstTrain["train"]["static"]["type"],
                "operation": "",
                "originData": firstTrain,
                "ordinal": i * 3
            });


            departTime = secondTrain["depart_time"];
            arriveTime = secondTrain["arrive_time"];
            dataset.push({
                "trainNo": secondTrain["train"]["static"]["code"],
                "departStation": secondTrain["depart_station"]["name"],
                "arriveStation": secondTrain["arrive_station"]["name"],
                "departTime": this.__datetime_toString(departTime, departTime, false),
                "arriveTime": this.__datetime_toString(departTime, arriveTime, false),
                "durationTime": this.__datetime_toString(departTime, arriveTime, true),
                "price2": this.__seats_toString(secondTrain["seats"], "1"),
                "price1": this.__seats_toString(secondTrain["seats"], "2"),
                "priceh": this.__seats_toString(secondTrain["seats"], "3"),
                "prices": this.__seats_toString(secondTrain["seats"], "4"),
                "type": secondTrain["train"]["static"]["type"],
                "operation": "<div class='purchase-button'><span class='mdi mdi-shopping'></span>订票</div>",
                "originData": secondTrain,
                "ordinal": i * 3 + 1
            });

            dataset.push({
                "trainNo": '',
                "departStation": '',
                "arriveStation": '',
                "departTime": '',
                "arriveTime": '',
                "durationTime": '',
                "price2": '',
                "price1": '',
                "priceh": '',
                "prices": '',
                "type": '',
                "operation": '',
                "originData": '',
                "ordinal": i * 3 + 2
            })
        }
        data["transships"].forEach(entry => {

        })
        return dataset;

    }
}

_12307.train_info = class {
    reload(api_data) {
        var grid = new dhx.Grid("grid" + _elpis.api.rand_int(), {
            columns: [
                {id: "trainNo", header: [{text: "车次"}]},
                {id: "type", header: [{text: "列车类型"}]},
                {id: "departStation", header: [{text: "出发站"}]},
                {id: "arriveStation", header: [{text: "到达站"}]},
                {id: "departTime", header: [{text: "出发时间"}]},
                {id: "arriveTime", header: [{text: "到达时间"}]},
                {id: "durationTime", header: [{text: "运行时间"}]},
                {id: "price2", header: [{text: "二等座 / 硬座"}]},
                {id: "price1", header: [{text: "一等座 / 软座"}]},
                {id: "priceh", header: [{text: "硬卧"}]},
                {id: "prices", header: [{text: "软卧"}]},
                {id: "operation", header: [{text: "操作"}], htmlEnable: true, width: 64}
            ],
            autoWidth: true,
            // data: dataset
        });

        grid.events.on("CellClick", function (row, column, e) {
            if (column.id != "operation") {
                new _12307.train_timetable(row);
            } else {
                _elpis.api.warp_require_login(() => new _12307.train_order(row))();
            }
        });

        var dataset = this.__convert_to_dataset(api_data);
        grid.data.parse(dataset);

        if(dataset.length<=3){
            _elpis.alert.__show_message("直达线路少，可以尝试搜索转乘")
        }

        this.dhxWindow.attach(grid);
        this.dhxWindow.paint();
        this.grid = grid;

    }

    show_window() {
        var app = this;
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(api_data) {
        var dhxWindow = new dhx.Window({
            title: "查询结果",
            modal: false,
            header: true,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 1400,
            height: 800
        });

        this.dhxWindow = dhxWindow;

        var isFullScreen = false;
        var oldSize = null;
        var oldPos = null;

        dhxWindow.header.data.add({icon: "mdi mdi-fullscreen", id: "fullscreen"}, 2);
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

        // dhxWindow.show();
        this.show_window();
        this.reload(api_data);

    }

    __seats_toString(seats, id) {
        if (seats[id]) {
            if (seats[id]["count"] > 0) return String(seats[id]["price"]) + "￥";
            else return "暂无余票";
        } else return "";
    }

    __datetime_toString(from_time, to_time, is_duration) {
        var from = moment(from_time, moment.ISO_8601);
        if (!is_duration) from.hours(0).minutes(0).seconds(0);
        var to = moment(to_time, moment.ISO_8601);
        var diff = moment.utc(to.diff(from));
        var day = diff.date() - 1;

        if (day >= 1) {
            if (is_duration) {
                var hours = 24 * day + parseInt(diff.format("HH"));
                return String(hours) + diff.format("时mm分");
            } else return "(" + day + "日后) " + diff.format("HH:mm");
        } else {
            if (is_duration) return diff.format("HH时mm分");
            else return diff.format("HH:mm");
        }
    }

    __convert_to_dataset(api_data) {
        var dataset = [];
        api_data["trains"].forEach((entry) => {
            var departTime = entry["depart_time"];
            var arriveTime = entry["arrive_time"];
            dataset.push({
                "trainNo": entry["train"]["static"]["code"],
                "departStation": entry["depart_station"]["name"],
                "arriveStation": entry["arrive_station"]["name"],
                "departTime": this.__datetime_toString(departTime, departTime, false),
                "arriveTime": this.__datetime_toString(departTime, arriveTime, false),
                "durationTime": this.__datetime_toString(departTime, arriveTime, true),
                "price2": this.__seats_toString(entry["seats"], "1"),
                "price1": this.__seats_toString(entry["seats"], "2"),
                "priceh": this.__seats_toString(entry["seats"], "3"),
                "prices": this.__seats_toString(entry["seats"], "4"),
                "type": entry["train"]["static"]["type"],
                "operation": "<div class='purchase-button'><span class='mdi mdi-shopping'></span>订票</div>",
                "originData": entry
            });
        });

        return dataset;
    }

}
// _12307.train(null);

_12307.train_order = class {

    reload(row_data) {
        var app = this;
        this.origin_data = row_data["originData"];

        var radio_btn_cfg = this.__get_radio_button_config(row_data);
        var form = new dhx.Form(null, {
            css: "dhx_widget--bordered",
            rows: [
                {
                    type: "text",
                    name: "trainNo",
                    gravity: false,
                    label: "车次",
                    labelInline: true,
                    icon: "",
                    value: row_data["trainNo"],
                    readOnly: true,
                },
                {
                    type: "datepicker",
                    name: "depart_date",
                    gravity: false,
                    value: row_data["originData"]["depart_time"]
                        .substring(0,row_data["originData"]["depart_time"].indexOf('T')),
                    dateFormat: "%Y-%m-%d",
                    label: "出发日",
                    labelInline: true,
                },
                {
                    type: "text",
                    name: "departStation",
                    gravity: false,
                    label: "出发站",
                    labelInline: true,
                    icon: "",
                    value: row_data["departStation"],
                    readOnly: true,
                },
                {
                    type: "text",
                    name: "arriveStation",
                    gravity: false,
                    label: "到达站",
                    labelInline: true,
                    value: row_data["arriveStation"],
                    readOnly: true,
                },
                {
                    type: "text",
                    name: "depart_time",
                    gravity: false,
                    label: "出发时间",
                    labelInline: true,
                    icon: "",
                    value: row_data["departTime"],
                    readOnly: true,
                },
                {
                    type: "text",
                    name: "arrive_time",
                    gravity: false,
                    label: "到达时间",
                    labelInline: true,
                    value: row_data["arriveTime"],
                    readOnly: true,
                },
                {
                    type: "radioGroup",
                    name: "seat_type",
                    id: "seat_type",
                    disabled: false,
                    required: true,
                    gravity: false,
                    options: {
                        padding: "5px",
                        rows: radio_btn_cfg
                    }
                },
                {
                    type: "button",
                    name: "addPassenger",
                    id: "addPassenger",
                    gravity: true,
                    full: true,
                    value: "添加同行人",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                },
                {
                    type: "text",
                    name: "price",
                    id: "price",
                    label: "价格",
                    labelInline: true,
                    gravity: false,
                    value: "0￥"
                },
                {
                    type: "button",
                    name: "order",
                    id: "order",
                    gravity: false,
                    full: true,
                    value: "提交订单",
                    size: "medium",
                    view: "flat",
                    color: "primary",
                }
            ]
        });

        this.form = form;

        form.events.on("ButtonClick", function (id, e) {
            if (id == "addPassenger") {
                // (() => app.passenger.show_window())();
                // setTimeout( () => app.passenger.show_window(), 100 );
                app.passenger.show_window();
            } else if (id == "order") {
                app.__book_tickets();
            }
        });

        form.events.on("Change", () => {
            app.__update_price();
        });

        this.dhxWindow.attach(form);
        this.dhxWindow.paint();
    }

    show_window() {
        var app = this;
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(row_data) {
        var app = this;
        var dhxWindow = new dhx.Window({
            title: "订单",
            modal: false,
            footer: false,
            resizable: true,
            movable: true,
            closable: true,
            width: 400,
            height: 680
        });

        this.dhxWindow = dhxWindow;
        this.passenger = new _12307.passenger(() => {
            app.__update_price();
        });

        // dhxWindow.show();
        this.show_window();
        this.reload(row_data);
    }

    __get_radio_button_config(row_data) {
        var seat_type = ["", "二等座 / 硬座", "一等座 / 软座", "硬卧", "软卧"];
        var dhx_config = [];
        for (var i = 1; i < seat_type.length; ++i) {
            var seat = row_data["originData"]["seats"][String(i)];
            if (seat && seat.count > 0) {
                dhx_config.push({
                    type: "radioButton",
                    label: seat_type[i] + " [余票:" + seat.count + " 单价:" + seat.price + "￥]",
                    labelInline: true,
                    value: i
                });
            }
        }
        return dhx_config;
    }

    __update_price() {
        var seat = String(this.form.getItem("seat_type").getValue());
        var passengers = this.passenger.get_selected_passengers();
        if (seat && passengers) {
            var price = this.origin_data["seats"][seat]["price"] * passengers.length;
            this.form.getItem("price").setValue(String(price) + "￥");
        } else this.form.getItem("price").setValue("0￥");
    }

    __book_tickets() {
        var seat = this.form.getItem("seat_type").getValue();
        var origin_data = this.origin_data;
        var passengers = this.passenger.get_selected_passengers();
        this.dhxWindow.hide();
        _elpis.alert.__show_message("正在提交购票订单，请稍后");
        if (!passengers) {
            _elpis.alert.__show_alert("错误", "请选择至少一个同行人");
            return;
        }
        passengers.forEach((p) => {
            _12307.api.purchase_ticket({
                "train": origin_data.train.id,
                "seat": seat,
                "from": origin_data.depart_station.id,
                "to": origin_data.arrive_station.id,
                "passenger": p["passenger_id"]
            }, function () {
                var name = p["passenger_name"];
                return () => _elpis.alert.__show_message(p["passenger_name"] + "的车票购买成功");
            }());
        });
    }
}

_12307.train_timetable = class {
    reload(api_data) {
        var grid = new dhx.Grid("grid" + _elpis.api.rand_int(), {
            columns: [
                {id: "stationName", header: [{text: "站名"}]},
                {id: "cityName", header: [{text: "城市"}]},
                {id: "departTime", header: [{text: "出发时间"}]},
                {id: "arriveTime", header: [{text: "到达时间"}]},
            ],
            autoWidth: true,
            // data: dataset
        });

        var dataset = this.__convert_to_dataset(api_data);
        grid.data.parse(dataset);

        this.dhxWindow.attach(grid);
        this.dhxWindow.paint();
        this.grid = grid;
    }

    show_window() {
        var app = this;
        setTimeout(() => {
            this.dhxWindow.show();
            this.dhxWindow._handlers.setActive();
        }, 100);
    }

    constructor(row_data) {
        var app = this;
        var dhxWindow = new dhx.Window({
            title: row_data["trainNo"] + "次时刻表",
            modal: false,
            header: true,
            footer: false,
            closable: true,
            resizable: true,
            movable: true,
            width: 480,
            height: 600
        });

        this.dhxWindow = dhxWindow;

        var isFullScreen = false;
        var oldSize = null;
        var oldPos = null;

        dhxWindow.header.data.add({icon: "mdi mdi-fullscreen", id: "fullscreen"}, 2);
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

        // dhxWindow.show();
        this.show_window();

        var train_id = row_data["originData"]["train"]["id"];
        _12307.api.get_train_timetable(train_id, (data) => app.reload(data));
    }

    __convert_to_dataset(api_data) {
        var dataset = [];
        api_data["timetable"]["stations"].forEach((item) => {
            var station = item["station"];
            var depart_time = moment(item["departTime"], moment.ISO_8601).format("MM-DD HH:mm");
            var arrive_time = moment(item["arriveTime"], moment.ISO_8601).format("MM-DD HH:mm");
            dataset.push({
                "stationName": station["name"],
                "cityName": station["city"],
                "departTime": depart_time,
                "arriveTime": arrive_time
            });
        })
        return dataset;
    }
}