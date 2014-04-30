package plugin.analyseTeamCooperation.protocol;

import java.util.ArrayList;
import java.util.List;

import ntut.csie.ui.protocol.EzScrumUI;
import ntut.csie.ui.protocol.PluginUI;
import ntut.csie.ui.protocol.ProjectUI;
import ntut.csie.ui.protocol.UIConfig;

public class PluginImp extends UIConfig {
	@Override
	public void setEzScrumUIList(List<EzScrumUI> ezScrumUIList) {
		/**
		 * add PluginUI to  ezScrumUIList for DoD Plug-in
		 */
		final PluginUI pluginUI = new PluginUI() {
			public String getPluginID() {
				return "AnalyseTeamCooperationPlugin";
			}
		};
		ezScrumUIList.add(pluginUI);
		
		/**
		 * add ReleasePlanUI to ezScrumUIList for ReleasePlan Pages view
		 */
		ProjectUI projectUI = new ProjectUI() {
			// 定義要放在左方Tree的node
			@Override
			public List<String> getProjectLeftTreeIDList() {
				List<String> projectLeftTreeIDList = new ArrayList<String>();
				projectLeftTreeIDList.add("analyseTeamCooperation");
				return projectLeftTreeIDList;
			}

			// 定義要左方Tree的node要連結到的頁面
			@Override
            public List<String> getProjectPageIDList() {
				List<String> projectPageIDList = new ArrayList<String>();
				projectPageIDList.add("analyseTeamCooperation_storyPage");
				projectPageIDList.add("analyseTeamCooperation_peoplePage");
				return projectPageIDList;
            }

			@Override
			public PluginUI getPluginUI() {
				return pluginUI;
			}
		};
		ezScrumUIList.add(projectUI);
	}
}
