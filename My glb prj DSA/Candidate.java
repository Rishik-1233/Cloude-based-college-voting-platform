package klu.model;

public class Candidate {
    public final int id;
    public final String name;
    public final String dept;
    public final String category;
    public final String slogan;
    public int votes;

    public Candidate(int id, String name, String dept, String category, String slogan) {
        this.id = id;
        this.name = name;
        this.dept = dept;
        this.category = category;
        this.slogan = slogan;
        this.votes = 0;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) — %d vote(s)", id, name, dept, votes);
    }
}
