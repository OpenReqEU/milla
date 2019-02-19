package eu.openreq.milla.models.json;

import java.util.Date;
import java.util.List;

public class RequestParams {

	    private String projectId;
	    private List<String> requirementIds;
	    private Date created_at;
	    private Date modified_at;
	    private String type;
	    private String status;
	    private String resolution;
	    
	    
		public String getProjectId() {
			return projectId;
		}
		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}
		public List<String> getRequirementIds() {
			return requirementIds;
		}
		public void setRequirementIds(List<String> requirementIds) {
			this.requirementIds = requirementIds;
		}
		public Date getCreated_at() {
			return created_at;
		}
		public void setCreated_at(Date created_at) {
			this.created_at = created_at;
		}
		public Date getModified_at() {
			return modified_at;
		}
		public void setModified_at(Date modified_at) {
			this.modified_at = modified_at;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getResolution() {
			return resolution;
		}
		public void setResolution(String resolution) {
			this.resolution = resolution;
		}
	    
	    
	
}