package votingsystem;
 

public class Dialogs {
    public static final String TITLE = "Online Voting System";
 
    
    public static int menu(String title, String header, String... options) {
        return Forms.menu(title, header, options);
    }
 
    
    public static int choose(String title, String header, String[] options) {
        return Forms.choose(title, header, options);
    }
 
    
    public static String[] form(String title, String header, String[] labels, boolean[] secret) {
        return form(title, header, labels, secret, null);
    }
 
    public static String[] form(String title, String header, String[] labels,
                                boolean[] secret, String[] initial) {
        Forms.Result r = Forms.window(title, header, labels, secret, initial,
            "save", "cancel");
        return (r == null || r.button != 0) ? null : r.values;
    }
 
    
    public static String input(String title, String message) {
        Forms.Result r = Forms.window(title, message, new String[] {"type here"},
            new boolean[] {false}, null, "ok", "cancel");
        return (r == null || r.button != 0) ? null : r.values[0];
    }
 
    public static void info(String message) {
        Forms.message(TITLE, message);
    }
 
    public static void error(String message) {
        Forms.message("Error", message);
    }
 
    public static void warn(String message) {
        Forms.message("Warning", message);
    }
 
    public static boolean confirm(String question) {
        return Forms.confirm(TITLE, question);
    }
 
    /** Scrollable monospaced report (results, tables...). */
    public static void showText(String title, String text) {
        Forms.showText(title, text);
    }
 
    
    public static Integer toInt(String text) {
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
 