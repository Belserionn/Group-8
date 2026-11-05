package votingsystem;
 
public class ResetPassCandidate {
 
    public static void run(VotingSystem system) {
        Forms.Result r = Forms.window("Reset Password (Candidate)",
            new String[] {"username", "new password"}, new boolean[] {false, true}, "save");
        if (r == null) {
            return;
        }
        if (r.values[1].isEmpty()) {
            Dialogs.error("Password cannot be empty.");
        } else if (system.resetCandidatePassword(r.values[0].trim(), r.values[1])) {
            Dialogs.info("Password saved.");
        } else {
            Dialogs.error("Username not found.");
        }
    }
}
 