/* Global Namespace */
var _12307 = {};

_12307.modules = [
    'lib/elpis/12307/api.js',
    'lib/elpis/12307/train.js',
    'lib/elpis/12307/passenger.js',
    'lib/elpis/12307/ticket.js',
    'lib/elpis/12307/station.js'
];

_elpis.load_modules(_12307.modules);

_elpis.sidebar.__add_entry({
    type: "separator"
});

_elpis.sidebar.__add_entry({
    type: "title",
    value: "12307 Extension",
});

_elpis.sidebar.__add_entry({
    type: "button",
    id: "train_search",
    value: "火车票订票",
    icon: "mdi mdi-shopping"
}, () => { new _12307.train_search(); });

_elpis.sidebar.__add_entry({
    type: "button",
    id: "ticket",
    value: "已购买车票",
    icon: "mdi mdi-ticket-confirmation"
}, () => { new _12307.ticket(); });

_elpis.sidebar.__add_entry({
    type: "button",
    id: "station",
    value: "车站查询",
    icon: "mdi mdi-subway"
}, () => { new _12307.station_search(); });
