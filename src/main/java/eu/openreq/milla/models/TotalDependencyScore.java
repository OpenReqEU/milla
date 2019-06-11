package eu.openreq.milla.models;

public class TotalDependencyScore implements Comparable<TotalDependencyScore> {

	String dependencyId;
	
	double totalScore;
	
	public TotalDependencyScore(String dependencyId, double totalScore) {
		this.dependencyId = dependencyId;
		this.totalScore = totalScore;
	}

	public String getDependencyId() {
		return dependencyId;
	}

	public void setDependencyId(String dependencyId) {
		this.dependencyId = dependencyId;
	}

	public double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(double totalScore) {
		this.totalScore = totalScore;
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
}
