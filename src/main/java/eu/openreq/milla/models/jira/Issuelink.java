
package eu.openreq.milla.models.jira;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "self",
    "type",
    "outwardIssue",
    "inwardIssue"
})
public class Issuelink {

    @JsonProperty("id")
    private String id;
    @JsonProperty("self")
    private String self;
    @JsonProperty("type")
    private Type type;
    @JsonProperty("outwardIssue")
    private OutwardIssue outwardIssue;
    @JsonProperty("inwardIssue")
    private InwardIssue inwardIssue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("self")
    public String getSelf() {
        return self;
    }

    @JsonProperty("self")
    public void setSelf(String self) {
        this.self = self;
    }

    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    @JsonProperty("outwardIssue")
    public OutwardIssue getOutwardIssue() {
        return outwardIssue;
    }

    @JsonProperty("outwardIssue")
    public void setOutwardIssue(OutwardIssue outwardIssue) {
        this.outwardIssue = outwardIssue;
    }

    @JsonProperty("inwardIssue")
    public InwardIssue getInwardIssue() {
        return inwardIssue;
    }

    @JsonProperty("inwardIssue")
    public void setInwardIssue(InwardIssue inwardIssue) {
        this.inwardIssue = inwardIssue;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
