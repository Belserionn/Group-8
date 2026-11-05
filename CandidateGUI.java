package votingsystem;


public class CandidateGUI {

    public static void candidateMenu(VotingSystem system, Candidate cand) {
        while (true) {
            int c = Dialogs.menu("Candidate", "Candidate: " + cand.getName()
                + " (" + cand.getParty() + ")\n" + Reports.electionLine(system),
                "My status", "My votes", "Results", "Log out");
            if (c == 0) {
                Dialogs.info(cand.isApproved()
                    ? "Status: APPROVED - you are on the ballot."
                    : "Status: PENDING - waiting for admin approval.");
            } else if (c == 1) {
                if (Reports.resultsAvailable(system)) {
                    Dialogs.info("You have " + system.voteCount(cand.getCandidateId())
                        + " vote(s).");
                }
            } else if (c == 2) {
                if (Reports.resultsAvailable(system)) {
                    Dialogs.showText("Election Results", Reports.results(system));
                }
            } else {
                return;   
            }
        }
    }
}
