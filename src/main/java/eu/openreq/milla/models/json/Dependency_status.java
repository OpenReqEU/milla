package eu.openreq.milla.models.json;

import com.google.gson.annotations.SerializedName;

public enum Dependency_status {
	
	@SerializedName(value="proposed", alternate= {"PROPOSED"})
	PROPOSED,
	@SerializedName(value="accepted", alternate= {"ACCEPTED"})
	ACCEPTED,
	@SerializedName(value="rejected", alternate= {"REJECTED"})
	REJECTED
	
//	@SerializedName("proposed")
//	PROPOSED,
//	@SerializedName("accepted")
//	ACCEPTED,
//	@SerializedName("rejected")
//	REJECTED
	
//	@JsonProperty("proposed")
//	PROPOSED,
//	@JsonProperty("accepted")
//	ACCEPTED,
//	@JsonProperty("rejected")
//	REJECTED
	
}
