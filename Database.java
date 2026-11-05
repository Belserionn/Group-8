package votingsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL lives here (JDBC + MySQL, driver: com.mysql.cj.jdbc.Driver).
 * The database and tables are created automatically on first run.
 */
public class Database {

    // ===== CHANGE THESE to match your MySQL server =====
    private static final String DB_URL = "jdbc:mysql://localhost:3306/voting_db"
        + "?createDatabaseIfNotExist=true&useSSL=false"
        + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "1234";   
    // ===================================================

    private Connection conn;

    public Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();
            migrate();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver (com.mysql.cj.jdbc.Driver) not found. "
                + "Check the mysql-connector-j dependency in pom.xml.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to MySQL. Is the server running, "
                + "and are DB_URL / DB_USER / DB_PASSWORD in Database.java correct?", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS admins ("
                + "admin_id VARCHAR(50) PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "salt VARCHAR(32) NOT NULL, "
                + "password_hash VARCHAR(64) NOT NULL"
                + ") ENGINE=InnoDB");
            st.execute("CREATE TABLE IF NOT EXISTS voters ("
                + "voter_id VARCHAR(50) PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "age INT NOT NULL DEFAULT 0, "
                + "registered TINYINT(1) NOT NULL DEFAULT 1, "
                + "salt VARCHAR(32) NOT NULL, "
                + "password_hash VARCHAR(64) NOT NULL"
                + ") ENGINE=InnoDB");
            st.execute("CREATE TABLE IF NOT EXISTS candidates ("
                + "candidate_id VARCHAR(50) PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "age INT NOT NULL DEFAULT 0, "
                + "party VARCHAR(100) NOT NULL, "
                + "approved TINYINT(1) NOT NULL DEFAULT 0, "
                + "salt VARCHAR(32) NOT NULL, "
                + "password_hash VARCHAR(64) NOT NULL"
                + ") ENGINE=InnoDB");
            st.execute("CREATE TABLE IF NOT EXISTS votes ("
                + "vote_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "voter_id VARCHAR(50) NOT NULL UNIQUE, "
                + "candidate_id VARCHAR(50) NOT NULL, "
                + "voted_at DATETIME(6) NOT NULL, "
                + "FOREIGN KEY (voter_id) REFERENCES voters(voter_id), "
                + "FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id)"
                + ") ENGINE=InnoDB");
            st.execute("CREATE TABLE IF NOT EXISTS election ("
                + "id INT PRIMARY KEY, "
                + "title VARCHAR(100) NOT NULL, "
                + "status VARCHAR(12) NOT NULL, "
                + "opened_at DATETIME(6) NULL, "
                + "closed_at DATETIME(6) NULL"
                + ") ENGINE=InnoDB");
            st.execute("INSERT IGNORE INTO election (id, title, status) "
                + "VALUES (1, 'Online Election', 'PENDING')");
        }
    }

    
    private void migrate() throws SQLException {
        boolean hasApproved;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW COLUMNS FROM candidates LIKE 'approved'")) {
            hasApproved = rs.next();
        }
        if (!hasApproved) {
            try (Statement st = conn.createStatement()) {
                
                st.execute("ALTER TABLE candidates "
                    + "ADD COLUMN approved TINYINT(1) NOT NULL DEFAULT 1 AFTER party");
            }
        }
    }

    private void run(String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

   
    public void insertAdmin(Admin a) {
        run("INSERT INTO admins (admin_id, name, salt, password_hash) VALUES (?, ?, ?, ?)",
            a.getAdminId(), a.getName(), a.getSalt(), a.getPasswordHash());
    }

    public void updateAdminPassword(Admin a) {
        run("UPDATE admins SET salt = ?, password_hash = ? WHERE admin_id = ?",
            a.getSalt(), a.getPasswordHash(), a.getAdminId());
    }

    public List<Admin> loadAdmins() {
        List<Admin> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT admin_id, name, salt, password_hash FROM admins")) {
            while (rs.next()) {
                list.add(new Admin(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    
    public void insertVoter(Voter v) {
        run("INSERT INTO voters (voter_id, name, age, registered, salt, password_hash) "
            + "VALUES (?, ?, ?, ?, ?, ?)",
            v.getVoterId(), v.getName(), v.getAge(), v.isRegistered() ? 1 : 0,
            v.getSalt(), v.getPasswordHash());
    }

    public void updateVoterPassword(Voter v) {
        run("UPDATE voters SET salt = ?, password_hash = ? WHERE voter_id = ?",
            v.getSalt(), v.getPasswordHash(), v.getVoterId());
    }

    public void updateVoterRegistered(Voter v) {
        run("UPDATE voters SET registered = ? WHERE voter_id = ?",
            v.isRegistered() ? 1 : 0, v.getVoterId());
    }

    public List<Voter> loadVoters() {
        List<Voter> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT voter_id, name, age, registered, salt, password_hash "
                + "FROM voters ORDER BY voter_id")) {
            while (rs.next()) {
                list.add(new Voter(rs.getString(1), rs.getString(2), rs.getInt(3),
                        rs.getInt(4) == 1, rs.getString(5), rs.getString(6)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    
    public void insertCandidate(Candidate c) {
        run("INSERT INTO candidates (candidate_id, name, age, party, approved, salt, "
            + "password_hash) VALUES (?, ?, ?, ?, ?, ?, ?)",
            c.getCandidateId(), c.getName(), c.getAge(), c.getParty(),
            c.isApproved() ? 1 : 0, c.getSalt(), c.getPasswordHash());
    }

    public void updateCandidatePassword(Candidate c) {
        run("UPDATE candidates SET salt = ?, password_hash = ? WHERE candidate_id = ?",
            c.getSalt(), c.getPasswordHash(), c.getCandidateId());
    }

    public void updateCandidateInfo(Candidate c) {
        run("UPDATE candidates SET name = ?, party = ? WHERE candidate_id = ?",
            c.getName(), c.getParty(), c.getCandidateId());
    }

    public void updateCandidateApproved(Candidate c) {
        run("UPDATE candidates SET approved = ? WHERE candidate_id = ?",
            c.isApproved() ? 1 : 0, c.getCandidateId());
    }

    public void deleteCandidate(String candidateId) {
        run("DELETE FROM candidates WHERE candidate_id = ?", candidateId);
    }

    public List<Candidate> loadCandidates() {
        List<Candidate> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT candidate_id, name, age, party, approved, salt, password_hash "
                + "FROM candidates ORDER BY candidate_id")) {
            while (rs.next()) {
                list.add(new Candidate(rs.getString(1), rs.getString(2), rs.getInt(3),
                        rs.getString(4), rs.getInt(5) == 1, rs.getString(6), rs.getString(7)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    
    public int insertVote(Vote vote) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO votes (voter_id, candidate_id, voted_at) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, vote.getVoterId());
            ps.setString(2, vote.getCandidateId());
            ps.setTimestamp(3, Timestamp.valueOf(vote.getTimestamp()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {   
                return -1;
            }
            throw new RuntimeException(e);
        }
    }

    public void deleteAllVotes() {
        run("DELETE FROM votes");
    }

    public List<Vote> loadVotes() {
        List<Vote> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT vote_id, voter_id, candidate_id, voted_at FROM votes ORDER BY vote_id")) {
            while (rs.next()) {
                list.add(new Vote(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4).toLocalDateTime()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Election loadElection() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT title, status, opened_at, closed_at FROM election WHERE id = 1")) {
            if (rs.next()) {
                Timestamp opened = rs.getTimestamp(3);
                Timestamp closed = rs.getTimestamp(4);
                return new Election(rs.getString(1), Election.Status.valueOf(rs.getString(2)),
                        opened == null ? null : opened.toLocalDateTime(),
                        closed == null ? null : closed.toLocalDateTime());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Election("Online Election", Election.Status.PENDING, null, null);
    }

    public void saveElection(Election e) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE election SET title = ?, status = ?, opened_at = ?, closed_at = ? "
                + "WHERE id = 1")) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getStatus().name());
            setTime(ps, 3, e.getOpenedAt());
            setTime(ps, 4, e.getClosedAt());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void setTime(PreparedStatement ps, int index, LocalDateTime t)
            throws SQLException {
        if (t == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(t));
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
