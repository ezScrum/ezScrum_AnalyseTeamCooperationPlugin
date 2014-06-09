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
	
	public void doGetStoryPage(StaplerRequest request, StaplerResponse response) {
		init(request);
		String releaseId = request.getParameter("releases");
		String sprintId = request.getParameter("sprints");
		try {
			AnalyseStory result = new AnalyseStory();
			EzScrumWebServiceController service = new EzScrumWebServiceController(mEzScrumURL);
			if (releaseId != null && !releaseId.isEmpty()) {
				List<ReleasePlanObject> releases = service.getReleasePlanList(mUserName, mEncodedPassword, mProjectId, releaseId.split(","));
				for (ReleasePlanObject release : releases) {
					AnalyseStory temp = getAnalysisResult(release.getSprintPlan());
					result.cooperation += temp.cooperation;
					result.nonCooperation += temp.nonCooperation;
					result.pairProgramming += temp.pairProgramming;
					result.totalTask += temp.totalTask;
				}
				result = getColorDepth(result);
			} else if (sprintId != null && !sprintId.isEmpty()) {
				List<SprintObject> sprints = service.getSprintList(mUserName, mEncodedPassword, mProjectId, sprintId.split(","));
				result = getColorDepth(getAnalysisResult(sprints));
			}
			response.setCharacterEncoding("utf-8");
			response.getWriter().write(new Gson().toJson(result));
	    	response.getWriter().close();
	    	System.out.println(new Gson().toJson(result));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
	        e.printStackTrace();
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
//			List<TeamObject> teamObjects = new ArrayList<TeamObject>();
			List<Node> nodes = new ArrayList<Node>();
			List<Link> links = new ArrayList<Link>();
			int sprintCount = 0;
			int nodeCount = 0;
			for (SprintObject sprint : sprints) {
//				TeamObject teamObject = new TeamObject();
				List<StoryObject> stories = sprint.storyList;
//				List<Node> nodes = new ArrayList<Node>();
//				List<Link> links = new ArrayList<Link>();
				HashMap<String, Integer> workerMap = new HashMap<String, Integer>();
				for (StoryObject story : stories) {
					List<TaskObject> tasks = story.taskList;
					
					// 解析任務合作狀態
					String handler = null, partners = null, nonCollaborativeHandler = null;
					boolean isCollaboration = false;
					for (TaskObject task : tasks) {
						handler = task.handler;
						if (handler != null && !handler.isEmpty()) {
							partners = task.partners;
							updateTotalMember(members, totalMemberMap, handler);
							nodeCount = updateWorkers(nodeCount, workerMap, handler);
							updateNode(nodes, handler, totalMemberMap.get(handler), Integer.valueOf(task.estimation), sprintCount, false, !partners.isEmpty());
							
							// 解析是否為獨立完成Story
							if (nonCollaborativeHandler == null) {
								nonCollaborativeHandler = task.handler;
							} else if (!nonCollaborativeHandler.contentEquals(task.handler)) {
								isCollaboration = true;	
							}
							
							// 解析partners
							if (partners!= null && !partners.isEmpty()) {
								String[] partnerList = partners.split(";");
								for (String partner : partnerList) {
									updateTotalMember(members, totalMemberMap, partner);
									// 新增partners為node
									updateNode(nodes, partner, totalMemberMap.get(partner), Integer.valueOf(task.estimation), sprintCount, false, true);
									// 新增handler與partner之間的關係
//									updateLink(links, totalMemberMap.get(handler), totalMemberMap.get(partner));
									nodeCount = updateWorkers(nodeCount, workerMap, partner);
									updateLink(links, workerMap.get(handler), workerMap.get(partner));
								}
								updatePartnerAndPartnerLink(links, nodes, workerMap, partnerList, sprintCount);
							}
						}
					}
					if (!isCollaboration && tasks.size() > 1 && nonCollaborativeHandler != null) {
						updateNode(nodes, nonCollaborativeHandler, totalMemberMap.get(nonCollaborativeHandler), 0, sprintCount, true, false);
					}
				}
//				teamObject.nodes = nodes;
//				teamObject.links = links;
//				teamObjects.add(teamObject);
				sprintCount++;
			}
			team.nodes = nodes;
			team.links = links;
			team.sprints = sprintCount;
//			team.sprints = teamObjects;
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
	
	private void updatePartnerAndPartnerLink(List<Link> links, List<Node> nodes, HashMap<String, Integer> workerMap, String[] partnerList, int sprintCount) {
		for (int i = 0; i < partnerList.length; i++) {
			for (int j = i + 1; j < partnerList.length; j++) {
				updateNode(nodes, partnerList[i], 0, 0, sprintCount, false, true);
				updateLink(links, workerMap.get(partnerList[i]), workerMap.get(partnerList[j]));
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

	private void updateNode(List<Node> nodes, String handler, Integer color, Integer taskHours, int sprintCount, boolean isNonCollaborative, boolean isPair) {
		boolean isAdded = false;
		for (Node node : nodes) {
			if (node.name.contentEquals(handler) && node.group == sprintCount) {
				node.value += taskHours;
				node.collaborationValue += isNonCollaborative ? -2 : 0;
				node.collaborationValue += isPair ? 1 : 0;
				isAdded = true;
				break;
			}
		}
		if (!isAdded) {
			Node node = new Node(handler, color, taskHours, sprintCount);
			node.collaborationValue += isNonCollaborative ? -2 : 0;
			node.collaborationValue += isPair ? 1 : 0;
			nodes.add(node);
			
		}
    }

	private void updateTotalMember(List<String> members, HashMap<String, Integer> memberGroupMap, String handler) {
		if (!members.contains(handler)) {
			memberGroupMap.put(handler, memberGroupMap.size());
			members.add(handler);		// add all memebers
		}
    }

	private void updateLink(List<Link> links, Integer source, Integer target) {
		boolean isAdded = false;
	    for (Link link : links) {
	    	if (link.source == source) {
	    		if (link.target == target) {
					link.value++;
		    		isAdded = true;
					break;
				} 
	    	}
	    }

		if (!isAdded) {
			Link temp = new Link();
			temp.source = source;
			temp.target = target;
			temp.value++;
			links.add(temp);
		}
    }

	/**
	 * class定義
	 */
	
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

//	public class TeamObject {
//		public int cooperationValue;
//		public LinkedHashMap<String, Integer> cooperationCount;
//		public List<Node> nodes;
//		public List<Link> links;
//	}
	
	public class Node {
		public String name;				// 名字
		public int color;				// 顏色
		public int value;				// 大小
		public int group;				// 哪個sprint
		public int collaborationValue;	// 合作度
		
		public Node(String nodeName, int nodeGroup, int nodeValue, int sprintCount) {
			name = nodeName;
			color = nodeGroup;
			value = nodeValue;
			group = sprintCount;
			collaborationValue = 0;
        }
	}
	
	public class Link {
		public int source;	// 來源
		public int target;	// 目標
		public int value;	// 粗細
		public int story;	// 合作的story數量
		public int task;	// 合作的task數量
		
		public Link() {
			value = 0;
		}
	}
	
	public class AnalyseTeam {
//		public List<TeamObject> sprints;
		public List<Node> nodes;
		public List<Link> links;
		public List<String> members;
		int sprints;
		
		public AnalyseTeam() {
		}
	}
}
