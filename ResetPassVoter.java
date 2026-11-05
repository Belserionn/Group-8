package votingsystem;
 
public class ResetPassVoter {
 
    public static void run(VotingSystem system) {
        String[] f = Dialogs.form("Reset Password (Voter)", null,
            new String[] {"Username", "New password"}, new boolean[] {false, true});
        if (f == null) {
            return;
        }
        if (f[1].isEmpty()) {
            Dialogs.error("Password cannot be empty.");
        } else if (system.resetVoterPassword(f[0].trim(), f[1])) {
            Dialogs.info("Password saved.");
        } else {
            Dialogs.error("Username not found.");
        }
    }
}