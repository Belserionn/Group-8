package votingsystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


public class Reports {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String fmt(LocalDateTime t) {
        return t == null ? "-" : t.format(FMT);
    }

    private static String bar(char ch) {
        return new String(new char[50]).replace('\0', ch);
    }

    public static String electionLine(VotingSystem system) {
        Election e = system.getElection();
        return "Election: " + e.getTitle() + "  [" + e.getStatus() + "]";
    }

    public static String ballotLabel(Candidate c) {
        return c.getCandidateId() + " - " + c.getName() + " (" + c.getParty() + ")";
    }

    
    public static String ballot(VotingSystem system) {
        List<Candidate> list = system.approvedCandidates();
        if (list.isEmpty()) {
            return "No candidates on the ballot yet.";
        }
        StringBuilder sb = new StringBuilder("CANDIDATES ON THE BALLOT\n" + bar('-') + "\n");
        for (Candidate c : list) {
            sb.append(ballotLabel(c)).append('\n');
        }
        return sb.toString();
    }

    
    public static String results(VotingSystem system) {
        StringBuilder sb = new StringBuilder();
        sb.append(bar('=')).append("\n              ELECTION RESULTS\n").append(bar('=')).append('\n');
        int total = system.totalVotes();
        int rank = 1;
        for (VotingSystem.Ranked r : system.rankedCandidates()) {
            double pct = total == 0 ? 0 : r.votes * 100.0 / total;
            sb.append(String.format("%d. %-20s %-12s %3d vote(s)  %5.1f%%\n",
                rank++, r.candidate.getName(), r.candidate.getParty(), r.votes, pct));
        }
        if (rank == 1) {
            sb.append("No candidates on the ballot.\n");
        }
        sb.append(bar('-')).append('\n');
        List<VotingSystem.Ranked> winners = system.winners();
        if (winners.isEmpty()) {
            sb.append("Winner: none (no votes yet)\n");
        } else if (winners.size() == 1) {
            sb.append("Winner: ").append(winners.get(0).candidate.getName())
              .append(" (").append(winners.get(0).candidate.getParty()).append(")\n");
        } else {
            sb.append("Tie between: ");
            for (int i = 0; i < winners.size(); i++) {
                sb.append(i > 0 ? ", " : "").append(winners.get(i).candidate.getName());
            }
            sb.append('\n');
        }
        int voted = system.votersWhoVoted();
        int eligible = system.eligibleVoters();
        double pct = eligible == 0 ? 0 : voted * 100.0 / eligible;
        sb.append(String.format("Voter turnout: %d/%d (%.1f%%)\n", voted, eligible, pct));
        sb.append(bar('='));
        return sb.toString();
    }

    
    public static boolean resultsAvailable(VotingSystem system) {
        if (system.isClosed()) {
            return true;
        }
        Dialogs.info("Results will be available after the election closes.");
        return false;
    }

    
    public static String candidateTable(VotingSystem system) {
        StringBuilder sb = new StringBuilder("MANAGE CANDIDATES\n" + bar('-') + "\n");
        if (system.getAllCandidates().isEmpty()) {
            sb.append("(no candidates yet)\n");
        }
        for (Candidate c : system.getAllCandidates()) {
            sb.append(String.format("%-7s %-18s age %-3d %-10s %-9s %d vote(s)\n",
                c.getCandidateId(), c.getName(), c.getAge(), c.getParty(),
                c.isApproved() ? "APPROVED" : "PENDING", system.voteCount(c.getCandidateId())));
        }
        return sb.toString();
    }

    
    public static String voterTable(VotingSystem system) {
        StringBuilder sb = new StringBuilder("MANAGE VOTERS\n" + bar('-') + "\n");
        if (system.getAllVoters().isEmpty()) {
            sb.append("(no voters yet)\n");
        }
        for (Voter v : system.getAllVoters()) {
            sb.append(String.format("%-7s %-18s age %-3d %-8s voted: %s\n",
                v.getVoterId(), v.getName(), v.getAge(),
                v.isRegistered() ? "ACTIVE" : "BLOCKED",
                system.hasVoted(v.getVoterId()) ? "yes" : "no"));
        }
        return sb.toString();
    }

    
    public static String analytics(VotingSystem system) {
        Election e = system.getElection();
        StringBuilder sb = new StringBuilder();
        sb.append("Election: ").append(e.getTitle()).append("  [").append(e.getStatus()).append("]\n");
        sb.append("Opened  : ").append(fmt(e.getOpenedAt())).append('\n');
        sb.append("Closed  : ").append(fmt(e.getClosedAt())).append('\n');
        if (!system.isClosed()) {
            sb.append("(Live numbers - the election is not closed yet.)\n");
        }
        sb.append(results(system)).append('\n');

        int voted = system.votersWhoVoted();
        int eligible = system.eligibleVoters();
        sb.append("Voted: ").append(voted).append("   Did not vote: ").append(eligible - voted)
          .append("   Total votes cast: ").append(system.totalVotes()).append('\n');

        sb.append("\nVotes by party:\n");
        List<VotingSystem.Tally> parties = system.partyTallies();
        int total = system.totalVotes();
        if (parties.isEmpty()) {
            sb.append("  (none)\n");
        }
        for (VotingSystem.Tally t : parties) {
            double pct = total == 0 ? 0 : t.votes * 100.0 / total;
            sb.append(String.format("  %-15s %3d vote(s)  %5.1f%%\n", t.label, t.votes, pct));
        }

        sb.append("\nVoting activity by hour:\n");
        Map<Integer, Integer> perHour = system.votesPerHour();
        if (perHour.isEmpty()) {
            sb.append("  (no votes yet)\n");
        }
        for (Map.Entry<Integer, Integer> en : perHour.entrySet()) {
            StringBuilder hashes = new StringBuilder();
            for (int i = 0; i < en.getValue(); i++) {
                hashes.append('#');
            }
            sb.append(String.format("  %02d:00  %s %d\n", en.getKey(), hashes, en.getValue()));
        }
        return sb.toString();
    }
}
