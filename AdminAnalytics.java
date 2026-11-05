package votingsystem;

/** Admin window: full results and analytics (available at any time, even live). */
public class AdminAnalytics {

    public static void run(VotingSystem system) {
        Dialogs.showText("Results & Analytics", Reports.analytics(system));
    }
}
