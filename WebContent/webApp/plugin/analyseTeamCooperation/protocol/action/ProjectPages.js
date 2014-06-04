Ext.ns('Plugin.analyseTeamCooperation');

/**
 * -----------------------------
 * Story page panel code
 * -----------------------------
 */

Plugin.analyseTeamCooperation.rangePanel = Ext.extend(Ext.Panel, {
	title		: 'Select Analysis Range',
	height		: 300,
	width		: '25%',
	autoScroll	: true,
	bodyStyle	: 'padding: 10px;',
	range	: [],
	initComponent: function() {
		var config = {
			tbar	: [storyAnalysisComboBox]
		}
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Plugin.analyseTeamCooperation.rangePanel.superclass.initComponent.apply(this, arguments);
		
		this.createReleaseCheckboxs();
	},
	createReleaseCheckboxs: function() {
		var obj = this;
		obj.removeAll();
		Ext.Ajax.request({
			url		: 'ajaxGetReleasePlan.do',
			success	: function(response) {
				obj.range = Ext.decode(response.responseText).Releases;
				for (var i = 0; i < obj.range.length; i++) {
					obj.add({
						xtype		: 'checkbox',
						boxLabel	: obj.range[i].Name,
						releaseId	: obj.range[i].ID,
						listeners	: {
							check: function(checkbox, value) {
								obj.checkChange();
							}
						}
					});
				}
				var rangePanel = Ext.getCmp('analyseTeamCooperation_rangePanel_ID');
				rangePanel.doLayout();
			}
		});
	},
	createSprintCheckboxs: function() {
		var obj = this;
		obj.removeAll();
		Ext.Ajax.request({
			url		: 'showAllSprintForSprintPlan.do',
			success	: function(response) {
				obj.range = Ext.decode(response.responseText).Sprints;
				for (var i = 0; i < obj.range.length; i++) {
					obj.add({
						xtype		: 'checkbox',
						boxLabel	: 'Sprint #' + obj.range[i].Id + ', ' + obj.range[i].Goal,
						sprintId	: obj.range[i].Id,
						listeners	: {
							check: function(checkbox, value) {
								obj.checkChange();
							}
						}
					});
				}
				var rangePanel = Ext.getCmp('analyseTeamCooperation_rangePanel_ID');
				rangePanel.doLayout();
			}
		});
	}, 
	refreshCheckboxs: function() {
		var selectedIndex = storyAnalysisComboBox.selectedIndex;
		var selectedCombo = storyAnalysisFieldStore.getAt(selectedIndex).data.dataValue;
		if (selectedCombo == 'Release') {
			this.createReleaseCheckboxs();
		} else if (selectedCombo == 'Sprint') {
			this.createSprintCheckboxs();
		}
		var selectedPanel = Ext.getCmp('analyseTeamCooperation_selectedPanel_ID');
		selectedPanel.removeAll();
	},
	checkChange: function() {
		var obj = this;
		var selectedPanel = Ext.getCmp('analyseTeamCooperation_selectedPanel_ID');
		
		selectedPanel.removeAll();
		for (var i = 0; i < obj.items.length; i++) { 
			if (obj.get(i).checked) {
				selectedPanel.add({
					html: obj.get(i).boxLabel,
					style: 'margin: 0px 0px 3px 0px;',
					border: false
				});
			}
		}
		selectedPanel.doLayout();
	}
});
Ext.reg('analyseTeamCooperation_rangePanel', Plugin.analyseTeamCooperation.rangePanel);

Plugin.analyseTeamCooperation.selectedPanel = Ext.extend(Ext.Panel, {
	title		: 'Selected',
	height		: 300,
	width		: '25%',
	autoScroll	: true,
	bodyStyle	: 'padding: 10px;'
});
Ext.reg('analyseTeamCooperation_selectedPanel', Plugin.analyseTeamCooperation.selectedPanel);

Plugin.analyseTeamCooperation.controlPanel = Ext.extend(Ext.Panel, {
	border		: false,
	layout		: {
		type: 'hbox',
		pack: 'center',
		align: 'top'
	},
	height		: 350,
	style		: 'padding: 15px;',
	initComponent: function() {
		var config = {
				items: [{
				    	xtype	: 'analyseTeamCooperation_rangePanel', 
				    	ref		: 'analyseTeamCooperation_rangePanel_ID',
				    	id		: 'analyseTeamCooperation_rangePanel_ID'
				    }, {
				    	html		: '>>',
				    	border		: false,
				    	bodyStyle	: 'margin:140px 50px 0px 50px'
				    }, {
				    	xtype	: 'analyseTeamCooperation_selectedPanel',
				    	ref		: 'analyseTeamCooperation_selectedPanel_ID',
				    	id		: 'analyseTeamCooperation_selectedPanel_ID'
				    }, {
				    	xtype	: 'button',
				    	text	: 'Analysis',
				    	handler	: this.doAnalysis,
				    	style	: 'margin: 275px 0px 0px 50px;'
				    }
				]
			}
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Plugin.analyseTeamCooperation.controlPanel.superclass.initComponent.apply(this, arguments);
	},
	doAnalysis: function() {
		var chartPanel = Ext.getCmp('analyseTeamCooperation_chartPanel_ID');
		var rangePanel = Ext.getCmp('analyseTeamCooperation_rangePanel_ID');
		
		// 組合query string
		var checked = [];
		var queryString = "PID=" + getURLParameter("PID");
		
		var selectedIndex = storyAnalysisComboBox.selectedIndex;
		selectedIndex = selectedIndex == -1 ? 0 : selectedIndex;
		var selectedCombo = storyAnalysisFieldStore.getAt(selectedIndex).data.dataValue;
		if (selectedCombo == 'Release') {
			queryString += "&releases=";
			for (var i = 0; i < rangePanel.items.length; i++) {
				if (rangePanel.get(i).checked) {
					checked.push(rangePanel.get(i).releaseId);
				}
			}
			for (var i = 0; i < checked.length; i++) {
				queryString += checked[i];
				if (i != checked.length - 1) {
					queryString += ",";
				}
			};
		} else if (selectedCombo == 'Sprint') {
			queryString += "&sprints=";
			for (var i = 0; i < rangePanel.items.length; i++) {
				if (rangePanel.get(i).checked) {
					checked.push(rangePanel.get(i).sprintId);
				}
			}
			for (var i = 0; i < checked.length; i++) {
				queryString += checked[i];
				if (i != checked.length - 1) {
					queryString += ",";
				}
			};
		}
		
		if (checked.length === 0) {
			alert('Please select one release at least.');
			return;
		}
		
		chartPanel.removeAll();
		chartPanel.add({
				html: '<iframe src="pluginWorkspace/AnalyseTeamCooperationPlugin/webApp/story.html?'+ queryString + '" width="820" height="650" frameborder="0" scrolling="auto"></iframe>',
				border: false
			}
		);
		chartPanel.doLayout();
	}
});
Ext.reg('analyseTeamCooperation_controlPanel', Plugin.analyseTeamCooperation.controlPanel);

Plugin.analyseTeamCooperation.chartPanel = Ext.extend(Ext.Panel, {
	border		: true,
	layout		: {
		type: 'hbox',
		pack: 'center',
		align: 'top'
	},
	autoHeight	: true,
	width		: '100%',
	autoScroll	: true
});
Ext.reg('analyseTeamCooperation_chartPanel', Plugin.analyseTeamCooperation.chartPanel);

var storyAnalysisFieldStore = new Ext.data.SimpleStore({ // 由於comboBox所需要的內容為靜態而不是來自資料庫，所以使用SimpleStore
	fields: ['dataValue'],
	data: [['Release'], ['Sprint']]
});

var storyAnalysisComboBox = new Ext.form.ComboBox({
	typeAhead: true,
	triggerAction: 'all',
	lazyRender: true,
	editable: false,
	mode: 'local',
	store: storyAnalysisFieldStore,
	fieldLabel: 'storyAnalysisComboBox',
	blankText: storyAnalysisFieldStore.getAt(0).data['dataValue'],
	emptyText: storyAnalysisFieldStore.getAt(0).data['dataValue'],
	valueField: 'dataValue', 	// 選擇display項目相對的賦與值。
	displayField: 'dataValue', 	// 選擇store中field裡的一個欄位名稱。
	id: 'storyAnalysisComboBoxID',
	listeners: {
		select: function() {
			var rangePanel = Ext.getCmp('analyseTeamCooperation_rangePanel_ID');
			rangePanel.refreshCheckboxs();
		}
	}
});

/**
 * -----------------------------
 * Story page code
 * -----------------------------
 */

Plugin.analyseTeamCooperation.storyPage = Ext.extend(Ext.Panel, {
	init: function(cmp) {
		this.hostCmp = cmp;
		this.hostCmp.on('render', this.onRender, this, {delay: 200});
	},

	onRender: function() {
		panel = new Ext.Panel({
			id			: 'storyPage',
			title		: 'Story Analysis',
			height		: 300,
			width		: '25%',
			autoScroll	: true,
			bodyStyle	: 'padding: 10px;',
			releases	: [],
			initComponent: function() {
				var config = {
					items: [{
							xtype	: 'analyseTeamCooperation_controlPanel', 
							ref		: 'analyseTeamCooperation_controlPanel_ID',
							id		: 'analyseTeamCooperation_controlPanel_ID'
						}, {
							xtype	: 'analyseTeamCooperation_chartPanel',
							ref		: 'analyseTeamCooperation_chartPanel_ID',
							id		: 'analyseTeamCooperation_chartPanel_ID'
						}
					]
				}
				Ext.apply(this, Ext.apply(this.initialConfig, config));
				Plugin.analyseTeamCooperation.storyPage.superclass.initComponent.apply(this, arguments);
			}
		});

		this.hostCmp.add(panel);
		this.hostCmp.doLayout();
	}
});

Ext.preg('analyseTeamCooperation_storyPage', Plugin.analyseTeamCooperation.storyPage);

/**
 * -----------------------------
 * Team page panel code
 * -----------------------------
 */



/**
 * -----------------------------
 * Team page code
 * -----------------------------
 */
Plugin.analyseTeamCooperation.teamPage = Ext.extend(Ext.Panel, {
	init: function(cmp) {
		this.hostCmp = cmp;
		this.hostCmp.on('render', this.onRender, this, {delay: 200});
	},

	onRender: function() {
		panel = new Ext.Panel({
			id			: 'teamPage',
			title		: 'Team Analysis',
			height		: 300,
			width		: '25%',
			autoScroll	: true,
			bodyStyle	: 'padding: 10px;',
			releases	: [],
			initComponent: function() {
				var config = {
						items: [{
								xtype	: '', 
								ref		: '',
								id		: ''
							}, {
								xtype	: '',
								ref		: '',
								id		: ''
							}
						]
					}
				Ext.apply(this, Ext.apply(this.initialConfig, config));
				Plugin.analyseTeamCooperation.teamPage.superclass.initComponent.apply(this, arguments);
			}
		});

		this.hostCmp.add(panel);
		this.hostCmp.doLayout();
	}
});

Ext.preg('analyseTeamCooperation_teamPage', Plugin.analyseTeamCooperation.teamPage);
