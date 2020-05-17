_12307.admin = {};

_12307.admin.modules = [
    'lib/elpis/12307_admin/api.js',
    'lib/elpis/12307_admin/station.js',
    'lib/elpis/12307_admin/train.js'
];

_elpis.load_modules(_12307.admin.modules);

_elpis.sidebar.__add_entry({
    type: "separator"
});

_elpis.sidebar.__add_entry({
    type: "title",
    value: "12307 Admin Extension",
});

_elpis.sidebar.__add_entry({
    type: "button",
    id: "station_manager",
    value: "车站管理",
    icon: "mdi mdi-home-plus"
}, () => { new _12307.admin.station_manager(); });

_elpis.sidebar.__add_entry({
    type: "button",
    id: "train_add",
    value: "添加列车",
    icon: "mdi mdi-train"
}, () => { new _12307.admin.train_add(); });

