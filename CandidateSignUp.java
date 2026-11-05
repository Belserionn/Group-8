package votingsystem;
 
public class CandidateSignUp {
 
    public static void run(VotingSystem system) {
        Forms.Result r = Forms.window("Candidate Sign Up",
            new String[] {"name", "age", "party (optional)", "password"},
            new boolean[] {false, false, false, true}, "save");
        if (r == null) {
            return;
        }
        String name = r.values[0].trim();
        Integer age = Dialogs.toInt(r.values[1]);
        if (age == null) {
            Dialogs.error("Age must be a whole number.");
            return;
        }
        String party = r.values[2].trim();
        if (name.isEmpty() || r.values[3].isEmpty()) {
            Dialogs.error("Name and password are required.");
            return;
        }
        try {
            Candidate c = system.registerCandidate(
                name, age, party.isEmpty() ? "Independent" : party, r.values[3]);
            Dialogs.info("Saved! Log in with \"" + c.getCandidateId()
                + "\" as your username.\nAn admin must approve you before you appear on the ballot.");
        } catch (RuleException e) {
            Dialogs.error(e.getMessage());
        }
    }
}
 