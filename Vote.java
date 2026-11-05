package votingsystem;

import java.time.LocalDateTime;

public class Vote {
    private int voteId;
    private final String voterId;
    private final String candidateId;
    private final LocalDateTime timestamp;

    public Vote(int voteId, String voterId, String candidateId, LocalDateTime timestamp) {
        this.voteId = voteId;
        this.voterId = voterId;
        this.candidateId = candidateId;
        this.timestamp = timestamp;
    }

    /** A new vote that is not saved yet (the database assigns voteId). */
    public Vote(String voterId, String candidateId) {
        this(0, voterId, candidateId, LocalDateTime.now());
    }

    public int getVoteId() { return voteId; }
    public void setVoteId(int voteId) { this.voteId = voteId; }
    public String getVoterId() { return voterId; }
    public String getCandidateId() { return candidateId; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
