package plugin.analyseTeamCooperation.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.form.ProjectInfoForm;
import ntut.csie.ezScrum.web.internal.IProjectSummaryEnum;
import ntut.csie.protocal.Action;

import org.codehaus.jettison.json.JSONException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import plugin.analyseTeamCooperation.dataModel.HistoryObject;
import plugin.analyseTeamCooperation.dataModel.ReleasePlanObject;
import plugin.analyseTeamCooperation.dataModel.SprintObject;
import plugin.analyseTeamCooperation.dataModel.StoryObject;
import plugin.analyseTeamCooperation.dataModel.TaskObject;
import plugin.analyseTeamCooperation.webservice.EzScrumWebServiceController;

import com.google.gson.Gson;

public class AnalyseTeamCooperationAction implements Action {
	private String mUserName;
	private String mEncodedPassword;
	private String mProjectId;
	private String mEzScrumURL;

	@Override
	public String getUrlName() {
		return "AnalyseTeamCooperation";
	}

	private void init(StaplerRequest request) {
		HttpSession session = request.getSession();
		ProjectInfoForm projectInfoForm = (ProjectInfoForm) session.getAttribute(IProjectSummaryEnum.PROJECT_INFO_FORM);
		IUserSession userSession = (IUserSession) session.getAttribute("UserSession");
		mUserName = userSession.getAccount().getAccount();
		mEncodedPassword = (String) session.getAttribute("passwordForPlugin");
		mProjectId = projectInfoForm.getName();
		mEzScrumURL = request.getServerName() + ":" + request.getLocalPort() + request.getContextPath();
	}

	//	public void doGetStoryPage(StaplerRequest request, StaplerResponse response) {
	//		init(request);
	//		String releaseId = request.getParameter("releases");
	//		String sprintId = request.getParameter("sprints");
	//		try {
	//			AnalyseStory result = new AnalyseStory();
	//			EzScrumWebServiceController service = new EzScrumWebServiceController(mEzScrumURL);
	//			
	//			if (releaseId != null && !releaseId.isEmpty()) {
	//				List<ReleasePlanObject> releases = service.getReleasePlanList(mUserName, mEncodedPassword, mProjectId, releaseId.split(","));
	//				for (ReleasePlanObject release : releases) {
	//					for (SprintObject sprint : release.getSprintPlan()) {
	//						List<StoryObject> stories = sprint.storyList;
	//						
	//						for (StoryObject story : stories) {
	//							List<TaskObject> tasks = story.taskList;
	//							
	//						}
	//					}
	//				}
	////				result = getColorDepth(result);
	//			} else if (sprintId != null && !sprintId.isEmpty()) {
	//				List<SprintObject> sprints = service.getSprintList(mUserName, mEncodedPassword, mProjectId, sprintId.split(","));
	//				result = getColorDepth(getAnalysisResult(sprints));
	//			}
	//			response.setCharacterEncoding("utf-8");
	//			response.getWriter().write(new Gson().toJson(result));
	//	    	response.getWriter().close();
	//	    	System.out.println(new Gson().toJson(result));
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} catch (JSONException e) {
	//	        e.printStackTrace();
	//        }
	//	}
	//	
	//	private AnalyseStory getColorDepth(AnalyseStory analyseTeam) {
	//		AnalyseStory result = new AnalyseStory();
	//		final int colorDepth = 50;
	//		result.cooperation = (int) ((analyseTeam.cooperation / (double) (analyseTeam.cooperation + analyseTeam.nonCooperation)) * colorDepth);
	//		result.nonCooperation = (int) ((analyseTeam.nonCooperation / (double) (analyseTeam.cooperation + analyseTeam.nonCooperation)) * colorDepth);
	//		Long pairColorDepth = Math.round(colorDepth * 2 * (Math.abs((analyseTeam.pairProgramming / (double) analyseTeam.totalTask) - 0.5)));
	//		result.pairProgramming = Integer.valueOf(pairColorDepth.toString());
	//	    return result;
	//    }
	//	
	//	private AnalyseStory getAnalysisResult(List<SprintObject> sprints) {
	//		int nonCooperation = 0, cooperation = 0, pairProgramming = 0, totalTask = 0;
	//		for (SprintObject sprint : sprints) {
	//			List<StoryObject> stories = sprint.storyList;
	//			for (StoryObject story : stories) {
	//				List<TaskObject> tasks = story.taskList;
	//				boolean isCooperate = false;
	//				int sotryPoint = Integer.valueOf(story.estimation);
	//				totalTask += tasks.size();
	//				String handler = null;
	//				for (TaskObject task : tasks) {
	//					/**
	//					 * 1. pair programming時進入
	//					 * 2. 沒有pair programming時，但是有cooperation時進入
	//					 */
	//					if (!task.handler.isEmpty() && !task.partners.isEmpty()) {
	//						pairProgramming++;
	//						isCooperate = true;
	//					} else if (!task.handler.isEmpty() && tasks.size() > 1) {
	//						if (handler == null) {
	//							handler = task.handler;
	//						} else if (!handler.contentEquals(task.handler)) {
	//							isCooperate = true;	
	//						}
	//					} 
	//				}
	//				if (isCooperate) {
	//					cooperation += sotryPoint;
	//				} else if (tasks.size() > 1) {
	//					nonCooperation += sotryPoint;
	//				}
	//			}
	//		}
	//		return new AnalyseStory(cooperation, nonCooperation, pairProgramming, totalTask);
	//	}

	//TODO
	public void doGetTeamPage(StaplerRequest request, StaplerResponse response) {
		init(request);
		try {
			// 取資料
			EzScrumWebServiceController service = new EzScrumWebServiceController(mEzScrumURL);
			List<SprintObject> sprints = service.getAllSprint(mUserName, mEncodedPassword, mProjectId);
			// 分析資料
			AnalyseTeam team = new AnalyseTeam();
			List<String> members = new ArrayList<String>();
			HashMap<String, Integer> totalMemberMap = new HashMap<String, Integer>();
			List<Node> nodes = new ArrayList<Node>();
			List<Link> links = new ArrayList<Link>();
			List<TeamObject> teamObjects = new ArrayList<TeamObject>();
			int currentSprint = 0, nodeCount = 0;
			int[] linkCount = {0};
			int[] dropCount = new int[sprints.size()];
			calculateDropCount(sprints, dropCount);
			for (SprintObject sprint : sprints) {
				List<StoryObject> stories = sprint.storyList;
				HashMap<String, Integer> workerMap = new HashMap<String, Integer>();

				for (StoryObject story : stories) {
					List<TaskObject> tasks = story.taskList;
					// 解析任務合作狀態
					String handler = null, partners = null, nonCollaborativeHandler = null;
					boolean isCollaboration = false;
//					analyseTaskForTeam();
					for (TaskObject task : tasks) {
						handler = task.handler;
						if (handler != null && !handler.isEmpty()) {
							List<Integer> nodeIds = new ArrayList<Integer>();
							List<Integer> linkIds = new ArrayList<Integer>();
							partners = task.partners;
							updateTotalMember(members, totalMemberMap, handler);
							nodeCount = updateWorkers(nodeCount, workerMap, handler);
							nodeIds.add(updateNode(nodes, workerMap.get(handler), handler, totalMemberMap.get(handler), Double.valueOf(task.estimation), currentSprint, false, !partners.isEmpty()));

							// 解析是否為獨立完成Story
							if (nonCollaborativeHandler == null) {
								nonCollaborativeHandler = task.handler;
							} else if (!nonCollaborativeHandler.contentEquals(task.handler)) {
								isCollaboration = true;
							}

							// 解析partners
							if (partners != null && !partners.isEmpty()) {
								String[] partnerList = partners.split(";");
								for (String partner : partnerList) {
									updateTotalMember(members, totalMemberMap, partner);

									nodeCount = updateWorkers(nodeCount, workerMap, partner);
									// 新增partners為node
									nodeIds.add(updateNode(nodes, workerMap.get(partner), partner, totalMemberMap.get(partner), Double.valueOf(task.estimation), currentSprint, false, true));
									// 新增handler與partner之間的關係
									linkIds.add(updateLink(links, linkCount, workerMap.get(handler), workerMap.get(partner), story.id, currentSprint));
								}
								updatePartnerAndPartnerLink(links, nodes, nodeIds, linkIds, linkCount, workerMap, partnerList, currentSprint, story.id);
							}
							teamObjects.add(new TeamObject(nodeIds, linkIds, Double.valueOf(task.estimation), task.doneTime, currentSprint, dropCount[currentSprint]));
						}
					}
					if (!isCollaboration && tasks.size() > 1 && nonCollaborativeHandler != null) {
						updateNode(nodes, workerMap.get(nonCollaborativeHandler), nonCollaborativeHandler, totalMemberMap.get(nonCollaborativeHandler), 0.0, currentSprint, true, false);
					}
				}
				currentSprint++;
			}
			team.nodes = nodes;
			team.links = links;
			team.sprints = currentSprint;
			team.doneTask = sortTeamObject(teamObjects);
			team.members = members;		// add all memebers
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(new Gson().toJson(team));
			response.getWriter().close();
			System.out.println(new Gson().toJson(team));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void analyseTaskForTeam() {
	    // TODO Auto-generated method stub
	    
    }

	private void calculateDropCount(List<SprintObject> sprints, int[] dropCount) {
		int currentSprint = 0;
		for (SprintObject sprint : sprints) {
			//			System.out.println("current " + currentSprint);
			dropCount[currentSprint] = 0;
			List<StoryObject> stories = sprint.storyList;
			// history
			for (StoryObject story : stories) {
				for (HistoryObject history : story.historyList) {
					String desc = history.description;
					String[] token = desc.split(":");
					if (token.length == 2) {
						if (token[0].trim().contentEquals("Sprint")) {
							String[] value = token[1].split("=>");
							//							System.out.println("current " + currentSprint + " = " + value[0].trim() + " , " + value[1].trim());
							if (value[1].trim().contentEquals("0")) {	// drop from sprint
								dropCount[Integer.valueOf(value[0].trim()) - 1]++;
							}
						}
					}
				}
			}
			currentSprint++;
		}
	}

	private List<TeamObject> sortTeamObject(List<TeamObject> teamObjects) {
		TeamObject temp = new TeamObject();
		for (int i = 0; i < teamObjects.size(); i++) {
			int currentSprint = teamObjects.get(i).sprints;
			for (int j = i + 1; j < teamObjects.size(); j++) {
				if (currentSprint != teamObjects.get(j).sprints)
					break;
				if (teamObjects.get(i).doneTime > teamObjects.get(j).doneTime) {
					temp = teamObjects.get(i);
					teamObjects.set(i, teamObjects.get(j));
					teamObjects.set(j, temp);
				}
			}
		}
		return teamObjects;
	}

	private void updatePartnerAndPartnerLink(List<Link> links, List<Node> nodes, List<Integer> nodeIds, List<Integer> linkIds, int[] linkCount, HashMap<String, Integer> workerMap,
	        String[] partnerList, int sprintCount, String story) {
		for (int i = 0; i < partnerList.length; i++) {
			for (int j = i + 1; j < partnerList.length; j++) {
				nodeIds.add(updateNode(nodes, workerMap.get(partnerList[i]), partnerList[i], 0, 0.0, sprintCount, false, true));
				linkIds.add(updateLink(links, linkCount, workerMap.get(partnerList[i]), workerMap.get(partnerList[j]), story, sprintCount));
			}
		}
	}

	private int updateWorkers(int nodeCount, HashMap<String, Integer> workerMap, String handler) {
		if (!workerMap.containsKey(handler)) {
			workerMap.put(handler, nodeCount);
			nodeCount++;
		}
		return nodeCount;
	}

	private int updateNode(List<Node> nodes, int id, String handler, Integer color, Double taskHours, int sprintCount, boolean isNonCollaborative, boolean isPair) {
		boolean isAdded = false;
		int nodeId = 0;
		for (Node node : nodes) {
			if (node.name.contentEquals(handler) && node.group == sprintCount) {
				node.value += taskHours;
				node.task++;
				nodeId = node.id;
				isAdded = true;
				break;
			}
		}
		if (!isAdded) {
			Node node = new Node(id, handler, color, taskHours, sprintCount);
			node.task++;
			nodes.add(node);
			nodeId = node.id;
		}
		return nodeId;
	}

	private void updateTotalMember(List<String> members, HashMap<String, Integer> memberGroupMap, String handler) {
		if (!members.contains(handler)) {
			memberGroupMap.put(handler, memberGroupMap.size());
			members.add(handler);		// add all memebers
		}
	}

	private int updateLink(List<Link> links, int[] id, Integer source, Integer target, String story, int currentSprint) {
		boolean isAdded = false;
		int linkId = 0;
		for (Link link : links) {
			if (link.source == source) {
				if (link.target == target) {
					link.value++;
					if (!link.currentStoryId.contentEquals(story)) {
						link.currentStoryId = story;
						link.story++;
					}
					link.task++;
					isAdded = true;
					linkId = link.id;
					break;
				}
			} else if (link.source == target) {
				if (link.target == source) {
					if (!link.currentStoryId.contentEquals(story)) {
						link.currentStoryId = story;
						link.story++;
					}
					link.task++;
				}
			}
		}

		if (!isAdded) {
			Link temp = new Link();
			temp.id = id[0];
			id[0]++;
			temp.source = source;
			temp.target = target;
			temp.group = currentSprint;
			temp.value++;
			temp.currentStoryId = story;
			temp.story++;
			temp.task++;
			links.add(temp);
			linkId = temp.id;
		}
		return linkId;
	}

	/**
	 * class定義
	 */

	public class AnalyseStory {
		public int storyPoint;
		public int value; 		// story有幾個人參與
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
		public double value;	// task hours
		public long doneTime;	// task done time
		public int sprints;		// which sprint
		public int dropCount;	// sprint有drop的story時為true
		public List<Integer> nodeId;	// node id
		public List<Integer> linkId;	// link id

		public TeamObject() {}

		public TeamObject(List<Integer> nodeIdList, List<Integer> linkIdList, Double taskHours, long taskDoneTime, int currentSprint, int storyDropCount) {
			value = taskHours;
			doneTime = taskDoneTime;
			nodeId = nodeIdList;
			linkId = linkIdList;
			sprints = currentSprint;
			dropCount = storyDropCount;
		}
	}

	public class Node {
		public int id;
		public String name;				// 名字
		public int color;				// 顏色
		public double value = 10;		// 大小
		public int group;				// 哪個sprint
		public int task;				// 實作task數量

		public Node(int nodeId, String nodeName, int nodeGroup, double nodeValue, int sprintCount) {
			id = nodeId;
			name = nodeName;
			color = nodeGroup;
			value += nodeValue;
			group = sprintCount;
			task = 0;
		}
	}

	public class Link {
		public int id;
		public int source;	// 來源
		public int target;	// 目標
		public int value;	// 粗細
		public int story;	// 合作的story數量
		public int task;	// 合作的task數量
		public int group;	// 哪個sprint
		public String currentStoryId;

		public Link() {
			value = 5;
		}
	}

	public class AnalyseTeam {
		public List<Node> nodes;
		public List<Link> links;
		public List<String> members;
		int sprints;
		public List<TeamObject> doneTask;
		public List<TeamObject> sprintDuration;

		public AnalyseTeam() {}
	}
}
