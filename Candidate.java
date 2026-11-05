package votingsystem;

public class Candidate {
    private final String candidateId;
    private String name;
    private final int age;
    private String party;
    private boolean approved;
    private String salt;
    private String passwordHash;

    public Candidate(String candidateId, String name, int age, String party,
                     boolean approved, String salt, String passwordHash) {
        this.candidateId = candidateId;
        this.name = name;
        this.age = age;
        this.party = party;
        this.approved = approved;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public static Candidate create(String candidateId, String name, int age,
                                   String party, boolean approved, String password) {
        String salt = Security.newSalt();
        return new Candidate(candidateId, name, age, party, approved, salt,
                Security.hashPassword(password, salt));
    }

    public boolean checkPassword(String password) {
        return Security.hashPassword(password, salt).equals(passwordHash);
    }

    public void setPassword(String password) {
        this.salt = Security.newSalt();
        this.passwordHash = Security.hashPassword(password, salt);
    }

    public String getCandidateId() { return candidateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
}
