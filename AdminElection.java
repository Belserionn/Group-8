package votingsystem;


public class AdminElection {

    public static void run(VotingSystem system) {
        while (true) {
            Election e = system.getElection();
            String header = "ELECTION CONTROL\n"
                + "Title : " + e.getTitle() + "\n"
                + "Status: " + e.getStatus() + "\n"
                + "Opened: " + Reports.fmt(e.getOpenedAt()) + "\n"
                + "Closed: " + Reports.fmt(e.getClosedAt());
            int c = Dialogs.menu("Election Control", header,
                "Open", "Close", "Title", "New election", "Back");
            try {
                if (c == 0) {
                    system.openElection();
                    Dialogs.info("Election is now OPEN. Voters can cast their votes.");
                } else if (c == 1) {
                    if (Dialogs.confirm("Close the election? Voting will stop.")) {
                        system.closeElection();
                        Dialogs.info("Election CLOSED. Results are now public.");
                    }
                } else if (c == 2) {
                    String title = Dialogs.input("Change title", "New election title:");
                    if (title != null) {
                        system.setElectionTitle(title);
                        Dialogs.info("Title updated.");
                    }
                } else if (c == 3) {
                    startNew(system);
                } else {
                    return;   
                }
            } catch (RuleException ex) {
                Dialogs.error(ex.getMessage());
            }
        }
    }

    private static void startNew(VotingSystem system) {
        if (!system.isClosed()) {
            Dialogs.error("Close the current election first.");
            return;
        }
        String title = Dialogs.input("New election", "Title of the new election:");
        if (title == null) {
            return;
        }
        if (Dialogs.confirm("This deletes ALL votes of the current election.\nContinue?")) {
            system.newElection(title);
            Dialogs.info("New election created (PENDING).\nVoters and candidates were kept.");
        }
    }
}
