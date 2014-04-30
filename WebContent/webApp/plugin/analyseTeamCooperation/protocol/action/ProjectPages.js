Ext.ns('Plugin.analyseTeamCooperation');

Plugin.analyseTeamCooperation.storyPage = Ext.extend(Object, {
	init: function(cmp) {
		this.hostCmp = cmp;
		this.hostCmp.on('render', this.onRender, this, {delay: 200});
	},

	onRender: function() {
		panel = new Ext.Panel({
			id			: 'storyPage',
			title		: 'storyPage',
			height		: 300,
			width		: '25%',
			autoScroll	: true,
			bodyStyle	: 'padding: 10px;',
			releases	: [],
			initComponent: function() {
				ezScrum.VelocityReleasePanel.superclass.initComponent.apply(this, arguments);
				
			}
		});

		this.hostCmp.add(panel);
		this.hostCmp.doLayout();
	}
});

Ext.preg('analyseTeamCooperation_storyPage', Plugin.analyseTeamCooperation.storyPage);

Plugin.analyseTeamCooperation.peoplePage = Ext.extend(Object, {
	init: function(cmp) {
		this.hostCmp = cmp;
		this.hostCmp.on('render', this.onRender, this, {delay: 200});
	},

	onRender: function() {
		panel = new Ext.Panel({
			id			: 'peoplePage',
			title		: 'peoplePage',
			height		: 300,
			width		: '25%',
			autoScroll	: true,
			bodyStyle	: 'padding: 10px;',
			releases	: [],
			initComponent: function() {
				ezScrum.VelocityReleasePanel.superclass.initComponent.apply(this, arguments);
			},
		});

		this.hostCmp.add(panel);
		this.hostCmp.doLayout();
	}
});

Ext.preg('analyseTeamCooperation_peoplePage', Plugin.analyseTeamCooperation.peoplePage);
