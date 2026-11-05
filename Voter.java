package votingsystem;


public class Voter {
    private final String voterId;
    private final String name;
    private final int age;
    private boolean registered;
    private String salt;
    private String passwordHash;

    public Voter(String voterId, String name, int age, boolean registered,
                 String salt, String passwordHash) {
        this.voterId = voterId;
        this.name = name;
        this.age = age;
        this.registered = registered;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public static Voter create(String voterId, String name, int age, String password) {
        String salt = Security.newSalt();
        return new Voter(voterId, name, age, true, salt, Security.hashPassword(password, salt));
    }

    public boolean checkPassword(String password) {
        return Security.hashPassword(password, salt).equals(passwordHash);
    }

    public void setPassword(String password) {
        this.salt = Security.newSalt();
        this.passwordHash = Security.hashPassword(password, salt);
    }

    public String getVoterId() { return voterId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
}
