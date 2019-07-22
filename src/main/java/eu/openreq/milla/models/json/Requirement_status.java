package eu.openreq.milla.models.json;

import com.google.gson.annotations.SerializedName;

public enum Requirement_status {

	@SerializedName(value="submitted", alternate= {"SUBMITTED"})
	SUBMITTED,
	@SerializedName(value="pending", alternate= {"PENDING"})
	PENDING,
	@SerializedName(value="accepted", alternate= {"ACCEPTED"})
	ACCEPTED,
	@SerializedName(value="draft", alternate= {"DRAFT"})
	DRAFT,
	@SerializedName(value="deferred", alternate= {"DEFERRED"})
	DEFERRED,
	@SerializedName(value="open", alternate= {"OPEN"})
	OPEN,
	@SerializedName(value="planned", alternate= {"PLANNED"})
	PLANNED,
	@SerializedName(value="completed", alternate= {"COMPLETED"})
	COMPLETED,
	@SerializedName(value="rejected", alternate= {"REJECTED"})
	REJECTED,
	@SerializedName(value="recommended", alternate= {"RECOMMENDED"})
	RECOMMENDED,
	@SerializedName(value="in_progress", alternate= {"IN_PROGRESS"})
	IN_PROGRESS
	
}
