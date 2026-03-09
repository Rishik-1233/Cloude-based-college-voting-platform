package klu.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Voter {
    public enum Status { ACTIVE, BLOCKED }

    public final String roll;
    public String name;
    public String dept;
    public Status status;
    public Map<String, String> votes; // category -> candidateName
    public long registeredAt;
    public boolean isSpecial;

    public Voter(String roll, String name, String dept) {
        this.roll = roll;
        this.name = name;
        this.dept = dept;
        this.status = Status.ACTIVE;
        this.votes = new LinkedHashMap<>();
        this.registeredAt = System.currentTimeMillis();
        this.isSpecial = false;
    }

    public boolean hasVotedAll() {
        return votes.size() >= 4;
    }

    public boolean hasVotedIn(String category) {
        return votes.containsKey(category);
    }

    public int voteCount() { return votes.size(); }

    @Override
    public String toString() {
        return String.format("Voter[roll=%s, name=%s, dept=%s, status=%s, votes=%d/4]",
            roll, name, dept, status, votes.size());
    }
}
