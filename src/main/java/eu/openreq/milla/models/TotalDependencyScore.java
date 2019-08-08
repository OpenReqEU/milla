package eu.openreq.milla.models;

import java.util.Objects;

public class TotalDependencyScore implements Comparable<TotalDependencyScore> {

	String dependencyId;
	String fromid;
	String toid;
	double totalScore;
	
	public TotalDependencyScore(String dependencyId, String fromid, String toid, double totalScore) {
		this.dependencyId = dependencyId;
		this.fromid = fromid;
		this.toid = toid;
		this.totalScore = totalScore;
	}
	
	public String getFromid() {
		return fromid;
	}

	public String getToid() {
		return toid;
	}

	public String getDependencyId() {
		return dependencyId;
	}

	public double getTotalScore() {
		return totalScore;
	}

	@Override
    public int compareTo(TotalDependencyScore o) {
		if (this.totalScore>o.getTotalScore()) {
			return 1;
		} else if (this.totalScore<o.getTotalScore()) {
			return -1;
		}
        return 0;
    }
	
	@Override
	public int hashCode() {
		return Objects.hash(this.dependencyId, this.fromid, this.toid, this.totalScore);
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (!(o instanceof TotalDependencyScore)) {
	        	return false;
	    }
	        
		TotalDependencyScore score = (TotalDependencyScore) o;
		
		return (this.getFromid()==score.getFromid() && this.getToid()==score.getToid() 
				&& this.getTotalScore()==score.getTotalScore());	
	}
	
}
