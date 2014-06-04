package plugin.analyseTeamCooperation.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import plugin.analyseTeamCooperation.dataModel.ReleasePlanObject;
import plugin.analyseTeamCooperation.dataModel.SprintObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.Base64;

public class EzScrumWebServiceController {
	private String mEzScrumURL;

	public EzScrumWebServiceController(String ezScrumURL) {
		mEzScrumURL = ezScrumURL;
	}

	public List<ReleasePlanObject> getReleasePlanList(String account, String encodedPassword, String projectId, String[] releaseIds) throws JSONException {
		String encodedUserName = new String(Base64.encode(account.getBytes()));
		// 需要的帳密為暗碼
		StringBuilder releaseWebServiceUrl = new StringBuilder();
		releaseWebServiceUrl.append("http://")
					        .append(mEzScrumURL)
					        .append("/web-service/").append(projectId)
					        .append("/release-plan/all/all/")
					        .append("?userName=").append(encodedUserName).append("&password=").append(encodedPassword);

		Client client = Client.create();
		WebResource webResource = client.resource(releaseWebServiceUrl.toString());
		Builder result = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		JSONArray json = result.get(JSONArray.class);
		Gson gson = new Gson();
		List<ReleasePlanObject> releases = gson.fromJson(json.toString(), new TypeToken<List<ReleasePlanObject>>() {}.getType());
		List<ReleasePlanObject> resultList = new ArrayList<ReleasePlanObject>();
		for (String releaseId : releaseIds) {
			for (ReleasePlanObject release : releases) {
				if (release.getId().contentEquals(releaseId)) {
					resultList.add(release);
					break;
				}
			}
		}
		return resultList;
	}
	
	public List<SprintObject> getSprintList(String account, String encodedPassword, String projectId, String[] sprintIds) throws JSONException {
		String encodedUserName = new String(Base64.encode(account.getBytes()));
		List<SprintObject> sprints, resultList = new ArrayList<SprintObject>();
		// 需要的帳密為暗碼
		StringBuilder releaseWebServiceUrl = new StringBuilder();
		releaseWebServiceUrl.append("http://")
							.append(mEzScrumURL)
							.append("/web-service/").append(projectId)
							.append("/sprint/all/all")
							.append("?userName=").append(encodedUserName).append("&password=").append(encodedPassword);
		
		Client client = Client.create();
		WebResource webResource = client.resource(releaseWebServiceUrl.toString());
		Builder result = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		JSONArray json = result.get(JSONArray.class);
		sprints = new Gson().fromJson(json.toString(), new TypeToken<List<SprintObject>>() {}.getType());
		for (String sprintId : sprintIds) {
			for (SprintObject sprint : sprints) {
				if (sprint.id.contentEquals(sprintId)) {
					resultList.add(sprint);
					break;
				}
			}
		}
		return resultList;
	}
	
	public List<SprintObject> getAllSprint(String account, String encodedPassword, String projectId) throws JSONException {
		String encodedUserName = new String(Base64.encode(account.getBytes()));
		List<SprintObject> sprints = new ArrayList<SprintObject>();
		// 需要的帳密為暗碼
		StringBuilder releaseWebServiceUrl = new StringBuilder();
		releaseWebServiceUrl.append("http://")
							.append(mEzScrumURL)
							.append("/web-service/").append(projectId)
							.append("/sprint/all/all")
							.append("?userName=").append(encodedUserName).append("&password=").append(encodedPassword);
		
		Client client = Client.create();
		WebResource webResource = client.resource(releaseWebServiceUrl.toString());
		Builder result = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		JSONArray json = result.get(JSONArray.class);
		sprints = new Gson().fromJson(json.toString(), new TypeToken<List<SprintObject>>() {}.getType());
		return sprints;
	}
}
