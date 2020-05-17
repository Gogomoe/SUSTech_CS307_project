_elpis.dashboard = function() {
    var dhxWindow = new dhx.Window({
        title: "Dashboard",
        modal: false,
        header: true,
        footer: false,
        closable: true,
        resizable: true,
        movable: true,
        width: 1200,
        height: 800
    });

    var tabbar = new dhx.Tabbar(null, {
        mode: "top",
        css: "dhx_widget--bordered",
        views:[
            { tab: "Overall", id: "overall" },
            { tab: "Fund", id: "fund"}
        ]

    });

    var config = {
        type:"splineArea",
        scales: {
            "bottom" : {
                text: "month"
            },
            "left" : {
                maxTicks: 10,
                max: 100,
                min: 0
            }
        },
        series: [
            {
                id: "A",
                value: "company A",
                color: "#81C4E8",
                strokeWidth: 2
            },
            {
                id: "B",
                value: "company B",
                color: "#74A2E7",
                strokeWidth: 3
            },
            {
                id: "C",
                value: "company C",
                color: "#5E83BA",
                strokeWidth: 4
            }
        ],
        legend: {
            series: ["A", "B", "C"],
            halign: "right",
            valign: "top"
        }
    };
    
    var chart = new dhx.Chart(null, config);

    var isFullScreen = false;
    var oldSize = null;
    var oldPos = null;
    var taskID;

    this.__init = function() {
        taskID = _elpis.taskbar.__add_task("Dashboard", "", dhxWindow);
        dhxWindow.header.data.add({ icon: "mdi mdi-fullscreen", id: "fullscreen" }, 2);
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

        var companiesData = [
            {month: "`02", "company A": 20, "company B": 52, "company C": 72, "company D": 34},
            {month: "`03", "company A": 5, "company B": 33, "company C": 90, "company D": 55},
            {month: "`04", "company A": 55, "company B": 30, "company C": 81, "company D": 66},
            {month: "`05", "company A": 30, "company B": 11, "company C": 62, "company D": 22},
            {month: "`06", "company A": 27, "company B": 14, "company C": 68, "company D": 70},
            {month: "`07", "company A": 32, "company B": 31, "company C": 64, "company D": 50},
            {month: "`08", "company A": 50, "company B": 22, "company C": 30, "company D": 80},
            {month: "`09", "company A": 12, "company B": 19, "company C": 65, "company D": 48},
            {month: "`10", "company A": 10, "company B": 24, "company C": 50, "company D": 66},
            {month: "`11", "company A": 17, "company B": 40, "company C": 78, "company D": 55}
        ];
        chart.data.parse(companiesData);

        dhxWindow.show();
        dhxWindow.attach(tabbar);
        tabbar.getCell("overall").attach(chart);
    }

    this.__init();
}