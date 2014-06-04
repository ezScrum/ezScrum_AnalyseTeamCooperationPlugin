package plugin.analyseTeamCooperation.dataModel;

import java.io.Serializable;

public interface IIssueTag extends  Serializable{
	public long getTagId();
	public void setTagId(long tagId);
	public String getTagName();
	public void setTagName(String tagName);
}
