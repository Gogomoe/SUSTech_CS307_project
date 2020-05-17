_elpis.layout = {
    layout: new dhx.Layout("layout", {
        css: "dhx_layout-cell--no-border",
        rows: [
            {
                id: "taskbar",
                css: "dhx_layout-cell--no-border",
                gravity: true,
                height: "60px"
            },
            {
                id: "sidebar",
                gravity: true,
                css: "dhx_layout-cell--no-border",
                width: "200px",
                height: String(window.innerHeight-60)+"px"
            }
        ]
    }),
    __init: function () {
        this.layout.getCell("taskbar").attach(_elpis.taskbar.ribbon);
        this.layout.getCell("sidebar").attach(_elpis.sidebar.sidebar);
    }
}

_elpis.layout.__init();