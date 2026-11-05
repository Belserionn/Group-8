package votingsystem;
 
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
 
 
public class Main {
 
    private static void loginScreen(VotingSystem system, boolean isVoter) {
        String label = isVoter ? "Voter" : "Candidate";
        while (true) {
            Forms.Result r = Forms.window(label + " Login",
                new String[] {"username", "password"}, new boolean[] {false, true},
                "log in", "sign up", "reset password");
            if (r == null) {
                return;   
            }
            if (r.button == 0) {
                String username = r.values[0].trim();
                if (isVoter) {
                    Voter v = system.loginVoter(username, r.values[1]);
                    if (v == null) {
                        Dialogs.error("Invalid username or password.");
                    } else {
                        VoterGUI.voterMenu(system, v);
                    }
                } else {
                    Candidate cand = system.loginCandidate(username, r.values[1]);
                    if (cand == null) {
                        Dialogs.error("Invalid username or password.");
                    } else {
                        CandidateGUI.candidateMenu(system, cand);
                    }
                }
            } else if (r.button == 1) {
                if (isVoter) {
                    VoterSignUp.run(system);
                } else {
                    CandidateSignUp.run(system);
                }
            } else {
                if (isVoter) {
                    ResetPassVoter.run(system);
                } else {
                    ResetPassCandidate.run(system);
                }
            }
        }
    }
 
    private static void adminLoginScreen(VotingSystem system) {
        while (true) {
            Forms.Result r = Forms.window("Admin Login",
                new String[] {"username", "password"}, new boolean[] {false, true},
                "log in");
            if (r == null) {
                return;   
            }
            Admin a = system.loginAdmin(r.values[0].trim(), r.values[1]);
            if (a == null) {
                Dialogs.error("Invalid username or password.");
            } else {
                AdminGUI.adminMenu(system, a);
            }
        }
    }
 
    private static void start() {
        Database db;
        try {
            db = new Database();
        } catch (RuntimeException e) {
            String why = e.getCause() != null ? "\n\nDetails: " + e.getCause().getMessage() : "";
            Dialogs.error(e.getMessage() + why);
            System.exit(1);
            return;
        }
        VotingSystem system = new VotingSystem(db);
        try {
            while (true) {
                int c = Forms.menu(Dialogs.TITLE + " - " + Reports.electionLine(system),
                    null, "voters", "candidates", "admin");
                if (c == 0) {
                    loginScreen(system, true);
                } else if (c == 1) {
                    loginScreen(system, false);
                } else if (c == 2) {
                    adminLoginScreen(system);
                } else {
                    break;   
                }
            }
        } catch (RuntimeException e) {
            Dialogs.error("Unexpected error: " + e.getMessage());
        }
        db.close();
        System.exit(0);
    }
 
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }
}