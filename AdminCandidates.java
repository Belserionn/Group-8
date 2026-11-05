package votingsystem;
 
import java.util.ArrayList;
import java.util.List;
 

public class AdminCandidates {
 
    public static void run(VotingSystem system) {
        while (true) {
            int c = Dialogs.menu("Manage Candidates", Reports.candidateTable(system),
                "Add", "Approve", "Edit", "Remove", "Back");
            try {
                if (c == 0) {
                    add(system);
                } else if (c == 1) {
                    Candidate cand = pick(system, "Approve candidate");
                    if (cand != null) {
                        system.approveCandidate(cand.getCandidateId());
                        Dialogs.info(cand.getName() + " is now on the ballot.");
                    }
                } else if (c == 2) {
                    edit(system);
                } else if (c == 3) {
                    remove(system);
                } else {
                    return;   
                }
            } catch (RuleException e) {
                Dialogs.error(e.getMessage());
            }
        }
    }
 
    private static Candidate pick(VotingSystem system, String title) {
        List<Candidate> list = new ArrayList<>(system.getAllCandidates());
        if (list.isEmpty()) {
            Dialogs.info("There are no candidates yet.");
            return null;
        }
        String[] labels = new String[list.size()];
        for (int i = 0; i < labels.length; i++) {
            Candidate c = list.get(i);
            labels[i] = Reports.ballotLabel(c) + "  [" + (c.isApproved() ? "APPROVED" : "PENDING") + "]";
        }
        int i = Dialogs.choose(title, "Select a candidate:", labels);
        return i < 0 ? null : list.get(i);
    }
 
    private static void add(VotingSystem system) {
        String[] f = Dialogs.form("Add Candidate", null,
            new String[] {"name", "age", "party", "starting password"},
            new boolean[] {false, false, false, true});
        if (f == null) {
            return;
        }
        String name = f[0].trim();
        Integer age = Dialogs.toInt(f[1]);
        if (age == null) {
            Dialogs.error("Age must be a whole number.");
            return;
        }
        String party = f[2].trim();
        if (name.isEmpty() || f[3].isEmpty()) {
            Dialogs.error("Name and password are required.");
            return;
        }
        Candidate c = system.addCandidateByAdmin(
            name, age, party.isEmpty() ? "Independent" : party, f[3]);
        Dialogs.info("Added " + c.getName() + ". Username: " + c.getCandidateId() + ".");
    }
 
    private static void edit(VotingSystem system) {
        Candidate c = pick(system, "Edit candidate");
        if (c == null) {
            return;
        }
        String[] f = Dialogs.form("Edit Candidate", "Leave a field blank to keep it.",
            new String[] {"Name", "Party"}, new boolean[] {false, false},
            new String[] {c.getName(), c.getParty()});
        if (f == null) {
            return;
        }
        system.editCandidate(c.getCandidateId(), f[0], f[1]);
        Dialogs.info("Candidate updated.");
    }
 
    private static void remove(VotingSystem system) {
        Candidate c = pick(system, "Remove candidate");
        if (c == null) {
            return;
        }
        if (Dialogs.confirm("Remove " + c.getName() + " permanently?")) {
            system.removeCandidate(c.getCandidateId());
            Dialogs.info("Candidate removed.");
        }
    }
}
 