package votingsystem;

import java.util.ArrayList;
import java.util.List;

/** Admin window: view voters and block / unblock them. */
public class AdminVoters {

    public static void run(VotingSystem system) {
        while (true) {
            int c = Dialogs.menu("Manage Voters", Reports.voterTable(system),
                "Block / Unblock", "Back");
            if (c == 0) {
                toggle(system);
            } else {
                return;   // Back, or the window was closed
            }
        }
    }

    private static void toggle(VotingSystem system) {
        List<Voter> list = new ArrayList<>(system.getAllVoters());
        if (list.isEmpty()) {
            Dialogs.info("There are no voters yet.");
            return;
        }
        String[] labels = new String[list.size()];
        for (int i = 0; i < labels.length; i++) {
            Voter v = list.get(i);
            labels[i] = v.getVoterId() + " - " + v.getName()
                + "  [" + (v.isRegistered() ? "ACTIVE" : "BLOCKED") + "]";
        }
        int i = Dialogs.choose("Block / Unblock", "Select a voter:", labels);
        if (i < 0) {
            return;
        }
        Voter v = list.get(i);
        boolean nowRegistered = !v.isRegistered();
        system.setVoterRegistered(v.getVoterId(), nowRegistered);
        Dialogs.info(v.getName() + (nowRegistered ? " was unblocked." : " was blocked."));
    }
}
