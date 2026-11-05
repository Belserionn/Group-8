package votingsystem;
 
public class VoterSignUp {
 
    public static void run(VotingSystem system) {
        Forms.Result r = Forms.window("Voter Sign Up",
            new String[] {"name", "age", "password"}, new boolean[] {false, false, true},
            "save");
        if (r == null) {
            return;
        }
        String name = r.values[0].trim();
        Integer age = Dialogs.toInt(r.values[1]);
        if (age == null) {
            Dialogs.error("Age must be a whole number.");
            return;
        }
        if (name.isEmpty() || r.values[2].isEmpty()) {
            Dialogs.error("Name and password are required.");
            return;
        }
        try {
            Voter voter = system.registerVoter(name, age, r.values[2]);
            Dialogs.info("Saved! Log in with \"" + voter.getVoterId()
                + "\" as your username.");
        } catch (RuleException e) {
            Dialogs.error(e.getMessage());
        }
    }
}