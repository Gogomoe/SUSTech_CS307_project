/* Global Namespace */
var _elpis = {};

_elpis.modules = [
    'lib/elpis/alert.js',
    'lib/elpis/api.js',
    'lib/elpis/taskbar.js',
    'lib/elpis/sidebar.js',
    'lib/elpis/user.js',
//    'lib/elpis/browser.js',
//    'lib/elpis/spreadsheet.js',
//    'lib/elpis/dashboard.js',
//    'lib/elpis/notepad.js',
//    'lib/elpis/journal.js',
    'lib/elpis/elpis.js',
];

_elpis.user_modules = [
    'lib/elpis/12307/include.js',
];

_elpis.admin_modules = [
    'lib/elpis/admin/include.js',
    'lib/elpis/12307_admin/include.js'
];

_elpis.load_modules = function(module_list) {
    for(i=0; i<module_list.length; ++i) {
        document.write("<script type='text/javascript' src='" + module_list[i] + "'></script>");
    }
}

_elpis.load_modules(_elpis.modules);
_elpis.load_modules(_elpis.user_modules);
_elpis.load_modules(_elpis.admin_modules);
