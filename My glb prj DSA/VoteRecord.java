package klu.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VoteRecord {
    public final String rollNumber;
    public final String category;
    public final String candidateName;
    public final int candidateId;
    public final long timestamp;

    private static final DateTimeFormatter FMT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault());

    public VoteRecord(String rollNumber, String category, String candidateName, int candidateId) {
        this.rollNumber = rollNumber;
        this.category = category;
        this.candidateName = candidateName;
        this.candidateId = candidateId;
        this.timestamp = System.currentTimeMillis();
    }

    public String formattedTime() {
        return FMT.format(Instant.ofEpochMilli(timestamp));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s -> %s", formattedTime(), rollNumber, category, candidateName);
    }
}
