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

}
