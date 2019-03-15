package eu.openreq.milla.models.json;

import com.google.gson.annotations.SerializedName;

public enum Dependency_type {

	
	@SerializedName(value="contributes", alternate= {"CONTRIBUTES"})
	CONTRIBUTES,
	@SerializedName(value="damages", alternate= {"DAMAGES"})
	DAMAGES,
	@SerializedName(value="refines", alternate= {"REFINES"})
	REFINES,
	@SerializedName(value="requires", alternate= {"REQUIRES"})
	REQUIRES,
	@SerializedName(value="incompatible", alternate= {"INCOMPATIBLE"})
	INCOMPATIBLE,
	@SerializedName(value="decomposition", alternate= {"DECOMPOSITION"})
	DECOMPOSITION,
	@SerializedName(value="similar", alternate= {"SIMILAR"})
	SIMILAR,
	@SerializedName(value="duplicates", alternate= {"DUPLICATES"})
	DUPLICATES,
	@SerializedName(value="replaces", alternate= {"REPLACES"})
	REPLACES
	
//	@SerializedName("contributes")
//	CONTRIBUTES,
//	@SerializedName("damages")
//	DAMAGES,
//	@SerializedName("refines")
//	REFINES,
//	@SerializedName("requires")
//	REQUIRES,
//	@SerializedName("incompatible")
//	INCOMPATIBLE,
//	@SerializedName("decomposition")
//	DECOMPOSITION,
//	@SerializedName("similar")
//	SIMILAR,
//	@SerializedName("duplicates")
//	DUPLICATES,
//	@SerializedName("replaces")
//	REPLACES
	
//	@JsonProperty("contributes")
//	CONTRIBUTES,
//	@JsonProperty("damages")
//	DAMAGES,
//	@JsonProperty("refines")
//	REFINES,
//	@JsonProperty("requires")
//	REQUIRES,
//	@JsonProperty("incompatible")
//	INCOMPATIBLE,
//	@JsonProperty("decomposition")
//	DECOMPOSITION,
//	@JsonProperty("similar")
//	SIMILAR,
//	@JsonProperty("duplicates")
//	DUPLICATES,
//	@JsonProperty("replaces")
//	REPLACES

}
