_elpis.spreadsheet = function() {
    var dataset = [
        {
            "name": "Argentina",
            "year": 2015,
            "continent": "South America",
            "form": "Republic",
            "gdp": 181.357,
            "oil": 1.545,
            "balance": 4.699,
            "when": "4/2/2015"
        }
    ];

    var dhxWindow = new dhx.Window({
        title: "Spreadsheet",
        modal: false,
        header: true,
        footer: true,
        closable: true,
        resizable: true,
        movable: true,
        width: 1200,
        height: 800
    });
    
    var pivot = new dhx.Pivot(null, {
        data: dataset,
        fields: {
            rows: ["form", "name"],
            columns: ["year"],
            values: [{ id: "oil", method: "count" }, { id: "oil", method: "sum" }],
        },
        fieldList: [
            { id: "name", label: "Name" },
            { id: "year", label: "Year" },
            { id: "continent", label: "Continent" },
            { id: "form", label: "Form" },
            { id: "gdp", label: "GDP" },
            { id: "oil", label: "Oil" },
            { id: "balance", label: "Balance" },
            { id: "when", label: "When", type: "date", format: "%d/%m/%Y" }
        ]
    });

    var isFullScreen = false;
    var oldSize = null;
    var oldPos = null;
    var taskID;

    this.__init = function() {
        taskID = _elpis.taskbar.__add_task("SpreadSheet", "", dhxWindow);
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

        var events = [
			"fieldClick",
			"applyButtonClick",
			"change",
			"filterApply",
        ];
        
        events.forEach(function (event) {
			pivot.events.on(event, function () {
				dhxWindow.show();
			});
		});

        //dhxWindow.events.on("AfterShow", function(){
        //    setTimeout(function() { console.log("Patch for spreadsheet is activated."); dhxWindow.show(); }, 1000);
        //});

        dhxWindow.show();
        dhxWindow.attach(pivot);

        

        
    }

    this.__init();
}