package plugin.analyseTeamCooperation.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.form.ProjectInfoForm;
import ntut.csie.ezScrum.web.internal.IProjectSummaryEnum;
import ntut.csie.protocal.Action;

import org.codehaus.jettison.json.JSONException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.google.gson.Gson;

import plugin.analyseTeamCooperation.dataModel.ReleasePlanObject;
import plugin.analyseTeamCooperation.dataModel.SprintObject;
import plugin.analyseTeamCooperation.dataModel.StoryObject;
import plugin.analyseTeamCooperation.dataModel.TaskObject;
import plugin.analyseTeamCooperation.webservice.EzScrumWebServiceController;

public class AnalyseTeamCooperationAction implements Action {
	@Override
	public String getUrlName() {
		return "AnalyseTeamCooperation";
	}

	public void doGetStoryPage(StaplerRequest request, StaplerResponse response) {
		HttpSession session = request.getSession();
		ProjectInfoForm projectInfoForm = (ProjectInfoForm) session.getAttribute(IProjectSummaryEnum.PROJECT_INFO_FORM);
		IUserSession userSession = (IUserSession) session.getAttribute("UserSession");
		String userName = userSession.getAccount().getAccount();
		String encodedPassword = (String) session.getAttribute("passwordForPlugin");
		String projectId = projectInfoForm.getName();
		String releaseId = request.getParameter("releases");
		String sprintId = request.getParameter("sprints");
		try {
			System.out.println("releaseId : " + releaseId);
			System.out.println("sprintId : " + sprintId);
			
			AnalyseStory result = new AnalyseStory();
			String ezScrumURL = request.getServerName() + ":" + request.getLocalPort() + request.getContextPath();
			EzScrumWebServiceController service = new EzScrumWebServiceController(ezScrumURL);
			if (releaseId != null && !releaseId.isEmpty()) {
				System.out.println("release : in");
				List<ReleasePlanObject> releases = service.getReleasePlanList(userName, encodedPassword, projectId, releaseId.split(","));
				for (ReleasePlanObject release : releases) {
					AnalyseStory temp = getAnalysisResult(release.getSprintPlan());
					result.cooperation += temp.cooperation;
					result.nonCooperation += temp.nonCooperation;
					result.pairProgramming += temp.pairProgramming;
					result.totalTask += temp.totalTask;
				}
				result = getColorDepth(result);
			} else if (sprintId != null && !sprintId.isEmpty()) {
				System.out.println("sprint : in");
				List<SprintObject> sprints = service.getSprintList(userName, encodedPassword, projectId, sprintId.split(","));
				result = getColorDepth(getAnalysisResult(sprints));
			}
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(new Gson().toJson(result));
	    	response.getWriter().close();
	    	System.out.println("Gson : " + new Gson().toJson(result));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
	        e.printStackTrace();
        }
	}

	public void doGetPeoplePage(StaplerRequest request, StaplerResponse response) {
		HttpSession session = request.getSession();
		ProjectInfoForm projectInfoForm = (ProjectInfoForm) session.getAttribute(IProjectSummaryEnum.PROJECT_INFO_FORM);
		IUserSession userSession = (IUserSession) session.getAttribute("UserSession");
		String userName = userSession.getAccount().getName();
		String encodedPassword = (String) session.getAttribute("passwordForPlugin");
		String projectId = projectInfoForm.getName();
		InputStream in = null;
		PrintWriter writer = null;
		try {
			String ezScrumURL = request.getServerName() + ":" + request.getLocalPort() + request.getContextPath();
			EzScrumWebServiceController service = new EzScrumWebServiceController(ezScrumURL);
			List<SprintObject> sprints = service.getAllSprint(userName, encodedPassword, projectId);
			List<AnalyseTeam> teams = new ArrayList<AnalyseTeam>();
			
			for (SprintObject sprint : sprints) {
				AnalyseTeam team = new AnalyseTeam();
				List<StoryObject> stories = sprint.storyList;
				for (StoryObject story : stories) {
					System.out.println("story : task size " + story.taskList.size());
					List<TaskObject> tasks = story.taskList;
					boolean isCooperate = false;
					int sotryPoint = Integer.valueOf(story.estimation);
					String handler = null;
					for (TaskObject task : tasks) {
						/**
						 * 1. pair programming時進入
						 * 2. 沒有pair programming時，但是有cooperation時進入
						 */
						if (!task.handler.isEmpty() && !task.partners.isEmpty()) {
							isCooperate = true;
						} else if (!task.handler.isEmpty() && tasks.size() > 1) {
							if (handler == null) {
								handler = task.handler;
							} else if (!handler.contentEquals(task.handler)) {
								isCooperate = true;	
							}
						} 
					}
				}
				teams.add();
			}
			response.getWriter().write("");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
	        e.printStackTrace();
        } finally {
			if (writer != null)
				writer.close();
		}
	}
	
	private AnalyseStory getColorDepth(AnalyseStory analyseTeam) {
		AnalyseStory result = new AnalyseStory();
		final int colorDepth = 50;
		result.cooperation = (int) ((analyseTeam.cooperation / (double) (analyseTeam.cooperation + analyseTeam.nonCooperation)) * colorDepth);
		result.nonCooperation = (int) ((analyseTeam.nonCooperation / (double) (analyseTeam.cooperation + analyseTeam.nonCooperation)) * colorDepth);
		Long pairColorDepth = Math.round(colorDepth * 2 * (Math.abs((analyseTeam.pairProgramming / (double) analyseTeam.totalTask) - 0.5)));
		result.pairProgramming = Integer.valueOf(pairColorDepth.toString());
	    return result;
    }
	
	private AnalyseStory getAnalysisResult(List<SprintObject> sprints) {
		int nonCooperation = 0, cooperation = 0, pairProgramming = 0, totalTask = 0;
		for (SprintObject sprint : sprints) {
			List<StoryObject> stories = sprint.storyList;
			for (StoryObject story : stories) {
				System.out.println("story : task size " + story.taskList.size());
				List<TaskObject> tasks = story.taskList;
				boolean isCooperate = false;
				int sotryPoint = Integer.valueOf(story.estimation);
				totalTask += tasks.size();
				String handler = null;
				for (TaskObject task : tasks) {
					/**
					 * 1. pair programming時進入
					 * 2. 沒有pair programming時，但是有cooperation時進入
					 */
					if (!task.handler.isEmpty() && !task.partners.isEmpty()) {
						pairProgramming++;
						isCooperate = true;
					} else if (!task.handler.isEmpty() && tasks.size() > 1) {
						if (handler == null) {
							handler = task.handler;
						} else if (!handler.contentEquals(task.handler)) {
							isCooperate = true;	
						}
					} 
				}
				if (isCooperate) {
					cooperation += sotryPoint;
				} else if (tasks.size() > 1) {
					nonCooperation += sotryPoint;
				}
			}
		}
		return new AnalyseStory(cooperation, nonCooperation, pairProgramming, totalTask);
	}
	
	public class AnalyseStory {
		public int cooperation;
		public int nonCooperation;
		public int pairProgramming;
		public int totalTask;
		
		public AnalyseStory(int cooperation, int nonCooperation, int pairProgramming, int totalTask) {
			this.cooperation = cooperation;
			this.nonCooperation = nonCooperation;
			this.pairProgramming = pairProgramming;
			this.totalTask = totalTask;
		}
		
		public AnalyseStory() {
			cooperation = 0;
			nonCooperation = 0;
			pairProgramming = 0;
			totalTask = 0;
        }
	}

	public class TeamObject {
		public int cooperationValue;
		public LinkedHashMap<String, Integer> cooperationCount;
	}
	
	public class TeamObject {
		public int cooperationValue;
		public LinkedHashMap<String, Integer> cooperationCount;
	}
	
	public class AnalyseTeam {
		public List<TeamObject> users;
		public List<>
		
		public AnalyseTeam() {
        }
	}
}
