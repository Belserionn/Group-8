package votingsystem;


public class AdminGUI {

    public static void adminMenu(VotingSystem system, Admin admin) {
        while (true) {
            String header = "ADMIN PANEL - " + admin.getName() + "\n"
                + Reports.electionLine(system);
            if (admin.checkPassword(VotingSystem.DEFAULT_ADMIN_PASSWORD)) {
                header += "\nWARNING: default password in use - please change it.";
            }
            int c = Dialogs.menu("Admin Panel", header,
                "Candidates", "Voters", "Election", "Results", "Password", "Log out");
            if (c == 0) {
                AdminCandidates.run(system);
            } else if (c == 1) {
                AdminVoters.run(system);
            } else if (c == 2) {
                AdminElection.run(system);
            } else if (c == 3) {
                AdminAnalytics.run(system);
            } else if (c == 4) {
                changePassword(system, admin);
            } else {
                return;   
            }
        }
    }

    private static void changePassword(VotingSystem system, Admin admin) {
        String[] f = Dialogs.form("Change Password", null,
            new String[] {"Current password", "New password"}, new boolean[] {true, true});
        if (f == null) {
            return;
        }
        if (!admin.checkPassword(f[0])) {
            Dialogs.error("Current password is wrong.");
            return;
        }
        try {
            system.changeAdminPassword(admin, f[1]);
            Dialogs.info("Password changed.");
        } catch (RuleException e) {
            Dialogs.error(e.getMessage());
        }
    }
}
