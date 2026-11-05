package votingsystem;


public class Admin {
    private final String adminId;
    private final String name;
    private String salt;
    private String passwordHash;

    public Admin(String adminId, String name, String salt, String passwordHash) {
        this.adminId = adminId;
        this.name = name;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public static Admin create(String adminId, String name, String password) {
        String salt = Security.newSalt();
        return new Admin(adminId, name, salt, Security.hashPassword(password, salt));
    }

    public boolean checkPassword(String password) {
        return Security.hashPassword(password, salt).equals(passwordHash);
    }

    public void setPassword(String password) {
        this.salt = Security.newSalt();
        this.passwordHash = Security.hashPassword(password, salt);
    }

    public String getAdminId() { return adminId; }
    public String getName() { return name; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
}
