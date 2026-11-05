package votingsystem;

import java.util.List;


public class VoterGUI {

    public static void voterMenu(VotingSystem system, Voter voter) {
        while (true) {
            int c = Dialogs.menu("Voter", "Voter: " + voter.getName() + "\n"
                + Reports.electionLine(system),
                "Status", "Candidates", "Cast vote", "Results", "Log out");
            if (c == 0) {
                Dialogs.info(Reports.electionLine(system) + "\n"
                    + (system.hasVoted(voter.getVoterId())
                        ? "You have already voted." : "You have not voted yet."));
            } else if (c == 1) {
                Dialogs.showText("Candidates", Reports.ballot(system));
            } else if (c == 2) {
                castVote(system, voter);
            } else if (c == 3) {
                if (Reports.resultsAvailable(system)) {
                    Dialogs.showText("Election Results", Reports.results(system));
                }
            } else {
                return;   
            }
        }
    }

    private static void castVote(VotingSystem system, Voter voter) {
        if (!system.isOpen()) {
            
            Dialogs.info(system.castVote(voter.getVoterId(), ""));
            return;
        }
        if (system.hasVoted(voter.getVoterId())) {
            Dialogs.info("You have already voted.");
            return;
        }
        List<Candidate> ballot = system.approvedCandidates();
        if (ballot.isEmpty()) {
            Dialogs.info("No candidates on the ballot.");
            return;
        }
        String[] labels = new String[ballot.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Reports.ballotLabel(ballot.get(i));
        }
        int pick = Dialogs.choose("Cast vote", "Choose your candidate:", labels);
        if (pick < 0) {
            return;
        }
        Candidate chosen = ballot.get(pick);
        if (Dialogs.confirm("Vote for " + chosen.getName() + "?\nThis cannot be changed.")) {
            Dialogs.info(system.castVote(voter.getVoterId(), chosen.getCandidateId()));
        }
    }
}
