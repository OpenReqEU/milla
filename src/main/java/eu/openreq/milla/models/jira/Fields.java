
package eu.openreq.milla.models.jira;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "issuetype",
    "customfield_10190",
    "timespent",
    "project",
    "customfield_11000",
    "fixVersions",
    "aggregatetimespent",
    "resolution",
    "customfield_10027",
    "customfield_10302",
    "resolutiondate",
    "workratio",
    "customfield_10906",
    "customfield_10907",
    "customfield_10908",
    "customfield_10909",
    "lastViewed",
    "watches",
    "customfield_10180",
    "customfield_10181",
    "created",
    "customfield_10142",
    "priority",
    "customfield_10023",
    "customfield_10300",
    "labels",
    "customfield_10301",
    "timeestimate",
    "aggregatetimeoriginalestimate",
    "versions",
    "issuelinks",
    "assignee",
    "updated",
    "status",
    "components",
    "timeoriginalestimate",
    "description",
    "customfield_10600",
    "customfield_10601",
    "customfield_10800",
    "aggregatetimeestimate",
    "summary",
    "creator",
    "subtasks",
    "reporter",
    "aggregateprogress",
    "customfield_10200",
    "customfield_10400",
    "environment",
    "customfield_10910",
    "customfield_10911",
    "progress",
    "comment",
    "votes",
    "customfield_10030",
    "customfield_10500",
    "customfield_10501",
    "customfield_10183",
    "customfield_10184",
    "customfield_10401",
    "customfield_10402",
    "customfield_10403",
    "customfield_10404",
    "duedate",
    "customfield_11100"
})
public class Fields {

    @JsonProperty("issuetype")
    private Issuetype issuetype;
    @JsonProperty("customfield_10190")
    private Object customfield10190;
    @JsonProperty("timespent")
    private Object timespent;
    @JsonProperty("project")
    private Project project;
    @JsonProperty("customfield_11000")
    private Object customfield11000;
    @JsonProperty("fixVersions")
    private List<FixVersion> fixVersions = null;
    @JsonProperty("aggregatetimespent")
    private Object aggregatetimespent;
    @JsonProperty("resolution")
    private Resolution resolution;
    @JsonProperty("customfield_10027")
    private CustomField customfield10027;
    @JsonProperty("customfield_10302")
    private Object customfield10302;
    @JsonProperty("resolutiondate")
    private Object resolutiondate;
    @JsonProperty("workratio")
    private Integer workratio;
    @JsonProperty("customfield_10906")
    private Object customfield10906;
    @JsonProperty("customfield_10907")
    private Object customfield10907;
    @JsonProperty("customfield_10908")
    private Object customfield10908;
    @JsonProperty("customfield_10909")
    private Object customfield10909;
    @JsonProperty("lastViewed")
    private String lastViewed;
    @JsonProperty("watches")
    private Watches watches;
    @JsonProperty("customfield_10180")
    private Object customfield10180;
    @JsonProperty("customfield_10181")
    private Object customfield10181;
    @JsonProperty("created")
    private String created;
    @JsonProperty("customfield_10142")
    private Object customfield10142;
    @JsonProperty("priority")
    private Priority priority;
    @JsonProperty("customfield_10023")
    private Object customfield10023;
    @JsonProperty("customfield_10300")
    private String customfield10300;
    @JsonProperty("labels")
    private List<Object> labels = null;
    @JsonProperty("customfield_10301")
    private String customfield10301;
    @JsonProperty("timeestimate")
    private Object timeestimate;
    @JsonProperty("aggregatetimeoriginalestimate")
    private Object aggregatetimeoriginalestimate;
    @JsonProperty("versions")
    private List<Version> versions = null;
    @JsonProperty("issuelinks")
    private List<Issuelink> issuelinks = null;
    @JsonProperty("assignee")
    private Assignee assignee;
    @JsonProperty("updated")
    private String updated;
    @JsonProperty("status")
    private Status status;
    @JsonProperty("components")
    private List<Component> components = null;
    @JsonProperty("timeoriginalestimate")
    private Object timeoriginalestimate;
    @JsonProperty("description")
    private String description;
    @JsonProperty("customfield_10600")
    private Object customfield10600;
    @JsonProperty("customfield_10601")
    private Object customfield10601;
    @JsonProperty("customfield_10800")
    private String customfield10800;
    @JsonProperty("aggregatetimeestimate")
    private Object aggregatetimeestimate;
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("creator")
    private Creator creator;
    @JsonProperty("subtasks")
    private List<Subtask> subtasks = null;
    @JsonProperty("reporter")
    private Reporter reporter;
    @JsonProperty("customfield_10200")
    private String customfield10200;
    @JsonProperty("customfield_10400")
    private Object customfield10400;
    @JsonProperty("environment")
    private Object environment;
    @JsonProperty("customfield_10910")
    private Object customfield10910;
    @JsonProperty("customfield_10911")
    private Object customfield10911;
    @JsonProperty("aggregateprogress")
    private Progress aggregateprogress;
    @JsonProperty("progress")
    private Progress progress;
    @JsonProperty("comment")
    private Comment comment;
    @JsonProperty("votes")
    private Votes votes;
    @JsonProperty("customfield_10030")
    private Object customfield10030;
    @JsonProperty("customfield_10500")
    private String customfield10500;
    @JsonProperty("customfield_10501")
    private String customfield10501;
    @JsonProperty("customfield_10183")
    private Object customfield10183;
    @JsonProperty("customfield_10184")
    private Object customfield10184;
    @JsonProperty("customfield_10401")
    private CustomField customfield10401;
    @JsonProperty("customfield_10402")
    private Object customfield10402;
    @JsonProperty("customfield_10403")
    private Object customfield10403;
    @JsonProperty("customfield_10404")
    private Object customfield10404;
    @JsonProperty("duedate")
    private Object duedate;
    @JsonProperty("customfield_11100")
    private Platforms customfield11100;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("issuetype")
    public Issuetype getIssuetype() {
        return issuetype;
    }

    @JsonProperty("issuetype")
    public void setIssuetype(Issuetype issuetype) {
        this.issuetype = issuetype;
    }

    @JsonProperty("customfield_10190")
    public Object getCustomfield10190() {
        return customfield10190;
    }

    @JsonProperty("customfield_10190")
    public void setCustomfield10190(Object customfield10190) {
        this.customfield10190 = customfield10190;
    }

    @JsonProperty("timespent")
    public Object getTimespent() {
        return timespent;
    }

    @JsonProperty("timespent")
    public void setTimespent(Object timespent) {
        this.timespent = timespent;
    }

    @JsonProperty("project")
    public Project getProject() {
        return project;
    }

    @JsonProperty("project")
    public void setProject(Project project) {
        this.project = project;
    }

    @JsonProperty("customfield_11000")
    public Object getCustomfield11000() {
        return customfield11000;
    }

    @JsonProperty("customfield_11000")
    public void setCustomfield11000(Object customfield11000) {
        this.customfield11000 = customfield11000;
    }

    @JsonProperty("fixVersions")
    public List<FixVersion> getFixVersions() {
        return fixVersions;
    }

    @JsonProperty("fixVersions")
    public void setFixVersions(List<FixVersion> fixVersions) {
        this.fixVersions = fixVersions;
    }

    @JsonProperty("aggregatetimespent")
    public Object getAggregatetimespent() {
        return aggregatetimespent;
    }

    @JsonProperty("aggregatetimespent")
    public void setAggregatetimespent(Object aggregatetimespent) {
        this.aggregatetimespent = aggregatetimespent;
    }

    @JsonProperty("resolution")
    public Resolution getResolution() {
        return resolution;
    }

    @JsonProperty("resolution")
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    @JsonProperty("customfield_10027")
    public CustomField getCustomfield10027() {
        return customfield10027;
    }

    @JsonProperty("customfield_10027")
    public void setCustomfield10027(CustomField customfield10027) {
        this.customfield10027 = customfield10027;
    }

    @JsonProperty("customfield_10302")
    public Object getCustomfield10302() {
        return customfield10302;
    }

    @JsonProperty("customfield_10302")
    public void setCustomfield10302(Object customfield10302) {
        this.customfield10302 = customfield10302;
    }

    @JsonProperty("resolutiondate")
    public Object getResolutiondate() {
        return resolutiondate;
    }

    @JsonProperty("resolutiondate")
    public void setResolutiondate(Object resolutiondate) {
        this.resolutiondate = resolutiondate;
    }

    @JsonProperty("workratio")
    public Integer getWorkratio() {
        return workratio;
    }

    @JsonProperty("workratio")
    public void setWorkratio(Integer workratio) {
        this.workratio = workratio;
    }

    @JsonProperty("customfield_10906")
    public Object getCustomfield10906() {
        return customfield10906;
    }

    @JsonProperty("customfield_10906")
    public void setCustomfield10906(Object customfield10906) {
        this.customfield10906 = customfield10906;
    }

    @JsonProperty("customfield_10907")
    public Object getCustomfield10907() {
        return customfield10907;
    }

    @JsonProperty("customfield_10907")
    public void setCustomfield10907(Object customfield10907) {
        this.customfield10907 = customfield10907;
    }

    @JsonProperty("customfield_10908")
    public Object getCustomfield10908() {
        return customfield10908;
    }

    @JsonProperty("customfield_10908")
    public void setCustomfield10908(Object customfield10908) {
        this.customfield10908 = customfield10908;
    }

    @JsonProperty("customfield_10909")
    public Object getCustomfield10909() {
        return customfield10909;
    }

    @JsonProperty("customfield_10909")
    public void setCustomfield10909(Object customfield10909) {
        this.customfield10909 = customfield10909;
    }

    @JsonProperty("lastViewed")
    public String getLastViewed() {
        return lastViewed;
    }

    @JsonProperty("lastViewed")
    public void setLastViewed(String lastViewed) {
        this.lastViewed = lastViewed;
    }

    @JsonProperty("watches")
    public Watches getWatches() {
        return watches;
    }

    @JsonProperty("watches")
    public void setWatches(Watches watches) {
        this.watches = watches;
    }

    @JsonProperty("customfield_10180")
    public Object getCustomfield10180() {
        return customfield10180;
    }

    @JsonProperty("customfield_10180")
    public void setCustomfield10180(Object customfield10180) {
        this.customfield10180 = customfield10180;
    }

    @JsonProperty("customfield_10181")
    public Object getCustomfield10181() {
        return customfield10181;
    }

    @JsonProperty("customfield_10181")
    public void setCustomfield10181(Object customfield10181) {
        this.customfield10181 = customfield10181;
    }

    @JsonProperty("created")
    public String getCreated() {
        return created;
    }

    @JsonProperty("created")
    public void setCreated(String created) {
        this.created = created;
    }

    @JsonProperty("customfield_10142")
    public Object getCustomfield10142() {
        return customfield10142;
    }

    @JsonProperty("customfield_10142")
    public void setCustomfield10142(Object customfield10142) {
        this.customfield10142 = customfield10142;
    }

    @JsonProperty("priority")
    public Priority getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @JsonProperty("customfield_10023")
    public Object getCustomfield10023() {
        return customfield10023;
    }

    @JsonProperty("customfield_10023")
    public void setCustomfield10023(Object customfield10023) {
        this.customfield10023 = customfield10023;
    }

    @JsonProperty("customfield_10300")
    public String getCustomfield10300() {
        return customfield10300;
    }

    @JsonProperty("customfield_10300")
    public void setCustomfield10300(String customfield10300) {
        this.customfield10300 = customfield10300;
    }

    @JsonProperty("labels")
    public List<Object> getLabels() {
        return labels;
    }

    @JsonProperty("labels")
    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    @JsonProperty("customfield_10301")
    public String getCustomfield10301() {
        return customfield10301;
    }

    @JsonProperty("customfield_10301")
    public void setCustomfield10301(String customfield10301) {
        this.customfield10301 = customfield10301;
    }

    @JsonProperty("timeestimate")
    public Object getTimeestimate() {
        return timeestimate;
    }

    @JsonProperty("timeestimate")
    public void setTimeestimate(Object timeestimate) {
        this.timeestimate = timeestimate;
    }

    @JsonProperty("aggregatetimeoriginalestimate")
    public Object getAggregatetimeoriginalestimate() {
        return aggregatetimeoriginalestimate;
    }

    @JsonProperty("aggregatetimeoriginalestimate")
    public void setAggregatetimeoriginalestimate(Object aggregatetimeoriginalestimate) {
        this.aggregatetimeoriginalestimate = aggregatetimeoriginalestimate;
    }

    @JsonProperty("versions")
    public List<Version> getVersions() {
        return versions;
    }

    @JsonProperty("versions")
    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    @JsonProperty("issuelinks")
    public List<Issuelink> getIssuelinks() {
        return issuelinks;
    }

    @JsonProperty("issuelinks")
    public void setIssuelinks(List<Issuelink> issuelinks) {
        this.issuelinks = issuelinks;
    }

    @JsonProperty("assignee")
    public Assignee getAssignee() {
        return assignee;
    }

    @JsonProperty("assignee")
    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    @JsonProperty("updated")
    public String getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonProperty("components")
    public List<Component> getComponents() {
        return components;
    }

    @JsonProperty("components")
    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @JsonProperty("timeoriginalestimate")
    public Object getTimeoriginalestimate() {
        return timeoriginalestimate;
    }

    @JsonProperty("timeoriginalestimate")
    public void setTimeoriginalestimate(Object timeoriginalestimate) {
        this.timeoriginalestimate = timeoriginalestimate;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("customfield_10600")
    public Object getCustomfield10600() {
        return customfield10600;
    }

    @JsonProperty("customfield_10600")
    public void setCustomfield10600(Object customfield10600) {
        this.customfield10600 = customfield10600;
    }

    @JsonProperty("customfield_10601")
    public Object getCustomfield10601() {
        return customfield10601;
    }

    @JsonProperty("customfield_10601")
    public void setCustomfield10601(Object customfield10601) {
        this.customfield10601 = customfield10601;
    }

    @JsonProperty("customfield_10800")
    public String getCustomfield10800() {
        return customfield10800;
    }

    @JsonProperty("customfield_10800")
    public void setCustomfield10800(String customfield10800) {
        this.customfield10800 = customfield10800;
    }

    @JsonProperty("aggregatetimeestimate")
    public Object getAggregatetimeestimate() {
        return aggregatetimeestimate;
    }

    @JsonProperty("aggregatetimeestimate")
    public void setAggregatetimeestimate(Object aggregatetimeestimate) {
        this.aggregatetimeestimate = aggregatetimeestimate;
    }

    @JsonProperty("summary")
    public String getSummary() {
        return summary;
    }

    @JsonProperty("summary")
    public void setSummary(String summary) {
        this.summary = summary;
    }

    @JsonProperty("creator")
    public Creator getCreator() {
        return creator;
    }

    @JsonProperty("creator")
    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    @JsonProperty("subtasks")
    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    @JsonProperty("subtasks")
    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @JsonProperty("reporter")
    public Reporter getReporter() {
        return reporter;
    }

    @JsonProperty("reporter")
    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    @JsonProperty("aggregateprogress")
    public Progress getAggregateprogress() {
        return aggregateprogress;
    }

    @JsonProperty("aggregateprogress")
    public void setAggregateprogress(Progress aggregateprogress) {
        this.aggregateprogress = aggregateprogress;
    }

    @JsonProperty("customfield_10200")
    public String getCustomfield10200() {
        return customfield10200;
    }

    @JsonProperty("customfield_10200")
    public void setCustomfield10200(String customfield10200) {
        this.customfield10200 = customfield10200;
    }

    @JsonProperty("customfield_10400")
    public Object getCustomfield10400() {
        return customfield10400;
    }

    @JsonProperty("customfield_10400")
    public void setCustomfield10400(Object customfield10400) {
        this.customfield10400 = customfield10400;
    }

    @JsonProperty("environment")
    public Object getEnvironment() {
        return environment;
    }

    @JsonProperty("environment")
    public void setEnvironment(Object environment) {
        this.environment = environment;
    }

    @JsonProperty("customfield_10910")
    public Object getCustomfield10910() {
        return customfield10910;
    }

    @JsonProperty("customfield_10910")
    public void setCustomfield10910(Object customfield10910) {
        this.customfield10910 = customfield10910;
    }

    @JsonProperty("customfield_10911")
    public Object getCustomfield10911() {
        return customfield10911;
    }

    @JsonProperty("customfield_10911")
    public void setCustomfield10911(Object customfield10911) {
        this.customfield10911 = customfield10911;
    }

    @JsonProperty("progress")
    public Progress getProgress() {
        return progress;
    }

    @JsonProperty("progress")
    public void setProgress(Progress progress) {
        this.progress = progress;
    }
    
    @JsonProperty("comment")
    public Comment getComment() {
    
        return comment;
    }

    @JsonProperty("commet")
    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @JsonProperty("votes")
    public Votes getVotes() {
        return votes;
    }

    @JsonProperty("votes")
    public void setVotes(Votes votes) {
        this.votes = votes;
    }

    @JsonProperty("customfield_10030")
    public Object getCustomfield10030() {
        return customfield10030;
    }

    @JsonProperty("customfield_10030")
    public void setCustomfield10030(Object customfield10030) {
        this.customfield10030 = customfield10030;
    }

    @JsonProperty("customfield_10500")
    public String getCustomfield10500() {
        return customfield10500;
    }

    @JsonProperty("customfield_10500")
    public void setCustomfield10500(String customfield10500) {
        this.customfield10500 = customfield10500;
    }

    @JsonProperty("customfield_10501")
    public String getCustomfield10501() {
        return customfield10501;
    }

    @JsonProperty("customfield_10501")
    public void setCustomfield10501(String customfield10501) {
        this.customfield10501 = customfield10501;
    }

    @JsonProperty("customfield_10183")
    public Object getCustomfield10183() {
        return customfield10183;
    }

    @JsonProperty("customfield_10183")
    public void setCustomfield10183(Object customfield10183) {
        this.customfield10183 = customfield10183;
    }

    @JsonProperty("customfield_10184")
    public Object getCustomfield10184() {
        return customfield10184;
    }

    @JsonProperty("customfield_10184")
    public void setCustomfield10184(Object customfield10184) {
        this.customfield10184 = customfield10184;
    }

    @JsonProperty("customfield_10401")
    public CustomField getCustomfield10401() {
        return customfield10401;
    }

    @JsonProperty("customfield_10401")
    public void setCustomfield10401(CustomField customfield10401) {
        this.customfield10401 = customfield10401;
    }

    @JsonProperty("customfield_10402")
    public Object getCustomfield10402() {
        return customfield10402;
    }

    @JsonProperty("customfield_10402")
    public void setCustomfield10402(Object customfield10402) {
        this.customfield10402 = customfield10402;
    }

    @JsonProperty("customfield_10403")
    public Object getCustomfield10403() {
        return customfield10403;
    }

    @JsonProperty("customfield_10403")
    public void setCustomfield10403(Object customfield10403) {
        this.customfield10403 = customfield10403;
    }

    @JsonProperty("customfield_10404")
    public Object getCustomfield10404() {
        return customfield10404;
    }

    @JsonProperty("customfield_10404")
    public void setCustomfield10404(Object customfield10404) {
        this.customfield10404 = customfield10404;
    }

    @JsonProperty("duedate")
    public Object getDuedate() {
        return duedate;
    }

    @JsonProperty("duedate")
    public void setDuedate(Object duedate) {
        this.duedate = duedate;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("customfield_11100")
	public Platforms getCustomfield11100() {
		return customfield11100;
	}

    @JsonProperty("customfield_11100")
	public void setCustomfield11100(Platforms customfield11100) {
		this.customfield11100 = customfield11100;
	}

}
