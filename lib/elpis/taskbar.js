_elpis.taskbar = {
    taskNum: 0,
    taskList: {},
    ribbon: new dhx.Ribbon(null, {
        css: "dhx_widget--bordered dhx_widget--bg_gray"
    }),

    __add_task: function(taskName, taskIcon, window) {
        var taskID = String( this.taskNum++ );
        var buttonConfig = {
            id: taskID,
            value: taskName,
            icon: taskIcon,
        }
        this.ribbon.data.add(buttonConfig);
        this.taskNum++;
        this.taskList[taskID] = {
            window: window,
            taskID: taskID,
            taskName: taskName
        };
        return taskID;
    },

    __del_task: function(taskID) {
        this.ribbon.hide(taskID);
    },

    __init: function() {
        this.ribbon.events.on("Click", function(id,e) {
            _elpis.taskbar.taskList[id].window.show();
        });
    }

}

_elpis.taskbar.__init();
