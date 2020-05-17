_elpis.admin = {};

_elpis.admin.modules = [
    'lib/elpis/admin/api.js',
    'lib/elpis/admin/user.js',
    // 'lib/elpis/admin/usermgr.js'
];

_elpis.load_modules(_elpis.admin.modules);

_elpis.sidebar.__add_entry({
    type: "separator"
});

_elpis.sidebar.__add_entry({
    type: "title",
    value: "Admin Extension",
});

_elpis.sidebar.__add_entry({
    type: "button",
    id: "user_manager",
    value: "用户管理",
    icon: "mdi mdi-account-edit"
}, () => { new _elpis.admin.user_manager(); });