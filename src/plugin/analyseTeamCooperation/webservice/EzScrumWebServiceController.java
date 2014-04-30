package plugin.analyseTeamCooperation.webservice;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.Gson;
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

	/**
	 * use web-service to get release info and return release plan file
	 * 
	 * @param account - the user account(not encoded)
	 * @param encodedPassword - the user password(encoded)
	 * @param projectId - the release of project
	 * @param releaseId - the release docx you wnat
	 * @return release docx file
	 * @throws JSONException
	 */
	public File getReleaseDocx(String account, String encodedPassword, String projectId, String releaseId) throws JSONException {
		String encodedUserName = new String(Base64.encode(account.getBytes()));
		// 需要的帳密為暗碼
		StringBuilder releaseWebServiceUrl = new StringBuilder();
		releaseWebServiceUrl.append("http://")
					        .append(mEzScrumURL)
					        .append("/web-service/").append(projectId)
					        .append("/release-plan/").append(releaseId)
					        .append("/all/").append("?userName=").append(encodedUserName).append("&password=").append(encodedPassword);

		Client client = Client.create();
		WebResource webResource = client.resource(releaseWebServiceUrl.toString());
		Builder result = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		JSONObject resultJSONObject = result.get(JSONObject.class);
		Gson gson = new Gson();
		return null;
	}
}
