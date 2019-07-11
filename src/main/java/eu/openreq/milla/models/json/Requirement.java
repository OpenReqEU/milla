package eu.openreq.milla.models.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
* Requirement
* A requirement within the OpenReq framework
* 
*/
public class Requirement {

	/**
	* The unique identifier for a requirement
	* (Required)
	* 
	*/
	@SerializedName("id")
	@Expose
	private String id;
	/**
	* The name of the requirement
	* 
	*/
	@SerializedName("name")
	@Expose
	private String name;
	/**
	* The textual description of the requirement
	* 
	*/
	@SerializedName("text")
	@Expose
	private String text;
	/**
	* The comments to the requirement
	* 
	*/
	@SerializedName("comments")
	@Expose
	private List<Comment> comments = null;
	/**
	* Creation timestamp
	* (Required)
	* 
	*/
	@SerializedName("created_at")
	@Expose
	private long created_at;
	/**
	* Last modification time
	* 
	*/
	@SerializedName("modified_at")
	@Expose
	private long modified_at;
	/**
	* The calculated priority of a requirement
	* 
	*/
	@SerializedName("priority")
	@Expose
	private int priority;
	/**
	* The type of a requirement
	* 
	*/
	@SerializedName("requirement_type")
	@Expose
	private Requirement_type requirement_type;
	/**
	* The current status of a requirement
	* (Required)
	* 
	*/
	@SerializedName("status")
	@Expose
	private Requirement_status status;
	/**
	* The requirements belonging to this requirement
	* 
	*/
	@SerializedName("children")
	//@Expose
	private List<Requirement> children = null;
	
	/**
	* RequirementParts of a requirement
	* 
	*/
	@SerializedName("requirementParts")
	@Expose
	private List<RequirementPart> requirementParts = null;
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public List<Comment> getComments() {
		if (this.comments==null) {
			this.comments = new ArrayList<Comment>();
		}
		return comments;
	}
	
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	public long getCreated_at() {
		return created_at;
	}
	
	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}
	
	public long getModified_at() {
		return modified_at;
	}
	
	public void setModified_at(long modified_at) {
		this.modified_at = modified_at;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public Requirement_type getRequirement_type() {
		return requirement_type;
	}
	
	public void setRequirement_type(Requirement_type requirement_type) {
		this.requirement_type = requirement_type;
	}
	
	public Requirement_status getStatus() {
		return status;
	}
	
	public void setStatus(Requirement_status status) {
		this.status = status;
	}
	
	public List<Requirement> getChildren() {
		if(children==null) {
			children = new ArrayList<Requirement>();
		}
		return children;
	}
	
	public void setChildren(List<Requirement> children) {
		this.children = children;
	}

	public List<RequirementPart> getRequirementParts() {
		if(requirementParts==null) {
			requirementParts = new ArrayList<RequirementPart>();
		}
		return requirementParts;
	}

	public void setRequirementParts(List<RequirementPart> requirementParts) {
		this.requirementParts = requirementParts;
	}
}