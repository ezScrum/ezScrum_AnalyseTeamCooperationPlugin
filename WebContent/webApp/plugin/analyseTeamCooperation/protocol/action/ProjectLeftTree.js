Ext.ns('Plugin.analyseTeamCooperation.projectLeftTree');

Plugin.analyseTeamCooperation.projectLeftTree.TreePlugin = Ext.extend(Object, {
	init: function(cmp) {
		this.hostCmp = cmp;
		this.hostCmp.on('render', this.onRender, this, {delay: 200});
	},

	onRender: function() {
		var node = new Ext.tree.AsyncTreeNode({
			text: 'Team Analysis',
	    	id 	: 'teamAnalysis',
			expanded : true,
			iconCls:'None',
        	cls:'treepanel-parent',
			children : [
		    {
		    	id:'analysisByStory',
				text : '<u>Analysis by Story</u>', 
		    	cls:'treepanel-leaf',
            	iconCls:'leaf-icon',
				leaf:true,
				listeners: {
					click: function(node, event) {
						var index = Ext.getCmp('content_panel').items.keys.indexOf("storyPage");
						Ext.getCmp('content_panel').layout.setActiveItem(index);
						Ext.getCmp('left_panel').Plugin_Clicked = true;
					}
				}
			}, 
			{
				id:'analysisByTeam',
				text : '<u>Analysis by Team</u>',
				cls:'treepanel-leaf',
            	iconCls:'leaf-icon',
				leaf:true,
				listeners: {
					click: function(node, event) {
						var index = Ext.getCmp('content_panel').items.keys.indexOf("teamPage");
						Ext.getCmp('content_panel').layout.setActiveItem(index);
						Ext.getCmp('left_panel').Plugin_Clicked = true;
					}
				}
			}]
		});

		this.hostCmp.getRootNode().appendChild(node);
		this.hostCmp.doLayout();
	}
});

Ext.preg('analyseTeamCooperation', Plugin.analyseTeamCooperation.projectLeftTree.TreePlugin);
