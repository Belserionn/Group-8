package votingsystem;
 
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
 

public class VotingSystem {
    public static final int MIN_AGE = 18;         
    public static final int MIN_CANDIDATES = 2;    
    public static final String DEFAULT_ADMIN_ID = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "1234";
    private static final String OLD_DEFAULT_ADMIN_PASSWORD = "admin123";   
 
    
    public static class Ranked {
        public final int votes;
        public final Candidate candidate;
 
        public Ranked(int votes, Candidate candidate) {
            this.votes = votes;
            this.candidate = candidate;
        }
    }
 
    
    public static class Tally {
        public final String label;
        public final int votes;
 
        public Tally(String label, int votes) {
            this.label = label;
            this.votes = votes;
        }
    }
 
    private final Database db;
    private final Map<String, Admin> admins = new LinkedHashMap<>();         
    private final Map<String, Voter> voters = new LinkedHashMap<>();         
    private final Map<String, Candidate> candidates = new LinkedHashMap<>(); 
    private final Map<String, Set<String>> graph = new LinkedHashMap<>();    
    private final List<Vote> votes = new ArrayList<>();
    private Election election;
 
    public VotingSystem(Database db) {
        this.db = db;
        for (Admin a : db.loadAdmins()) {
            admins.put(a.getAdminId(), a);
        }
        if (!hasKeyIgnoreCase(admins.keySet(), DEFAULT_ADMIN_ID)) {   
            Admin a = Admin.create(DEFAULT_ADMIN_ID, "Administrator", DEFAULT_ADMIN_PASSWORD);
            db.insertAdmin(a);
            admins.put(a.getAdminId(), a);
        }
        
        for (Admin a : admins.values()) {
            if (a.getAdminId().equalsIgnoreCase(DEFAULT_ADMIN_ID)
                    && a.checkPassword(OLD_DEFAULT_ADMIN_PASSWORD)) {
                a.setPassword(DEFAULT_ADMIN_PASSWORD);
                db.updateAdminPassword(a);
            }
        }
        for (Voter v : db.loadVoters()) {
            voters.put(v.getVoterId(), v);
            graph.put(v.getVoterId(), new HashSet<>());
        }
        for (Candidate c : db.loadCandidates()) {
            candidates.put(c.getCandidateId(), c);
            graph.put(c.getCandidateId(), new HashSet<>());
        }
        for (Vote vote : db.loadVotes()) {
            votes.add(vote);
            graph.get(vote.getVoterId()).add(vote.getCandidateId());
            graph.get(vote.getCandidateId()).add(vote.getVoterId());
        }
        election = db.loadElection();
    }
 
   
    public Election getElection() {
        return election;
    }
 
    public boolean isOpen() {
        return election.getStatus() == Election.Status.OPEN;
    }
 
    public boolean isClosed() {
        return election.getStatus() == Election.Status.CLOSED;
    }
 
    private void requirePending(String what) {
        if (election.getStatus() != Election.Status.PENDING) {
            throw new RuleException("You can only " + what + " before the election opens.");
        }
    }
 
    public void openElection() {
        if (election.getStatus() != Election.Status.PENDING) {
            throw new RuleException("The election can only be opened from the PENDING state.");
        }
        if (approvedCandidates().size() < MIN_CANDIDATES) {
            throw new RuleException("You need at least " + MIN_CANDIDATES
                + " approved candidates to open the election.");
        }
        election.setStatus(Election.Status.OPEN);
        election.setOpenedAt(LocalDateTime.now());
        db.saveElection(election);
    }
 
    public void closeElection() {
        if (election.getStatus() != Election.Status.OPEN) {
            throw new RuleException("Only an OPEN election can be closed.");
        }
        election.setStatus(Election.Status.CLOSED);
        election.setClosedAt(LocalDateTime.now());
        db.saveElection(election);
    }
 
    public void setElectionTitle(String title) {
        requirePending("change the election title");
        if (title.trim().isEmpty()) {
            throw new RuleException("Title cannot be empty.");
        }
        election.setTitle(title.trim());
        db.saveElection(election);
    }
 
    
    public void newElection(String title) {
        if (election.getStatus() != Election.Status.CLOSED) {
            throw new RuleException("Close the current election first.");
        }
        if (title.trim().isEmpty()) {
            throw new RuleException("Title cannot be empty.");
        }
        db.deleteAllVotes();
        votes.clear();
        for (Set<String> edges : graph.values()) {
            edges.clear();
        }
        election = new Election(title.trim(), Election.Status.PENDING, null, null);
        db.saveElection(election);
    }
 
    
    private static boolean hasKeyIgnoreCase(Collection<String> keys, String id) {
        for (String k : keys) {
            if (k.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
 
    private boolean idTaken(String id) {
        return hasKeyIgnoreCase(voters.keySet(), id)
            || hasKeyIgnoreCase(candidates.keySet(), id)
            || hasKeyIgnoreCase(admins.keySet(), id);
    }
 
    
    private String checkUsername(String name) {
        String u = name == null ? "" : name.trim();
        if (u.isEmpty()) {
            throw new RuleException("Name is required.");
        }
        if (u.length() > 50) {
            throw new RuleException("Name is too long (max 50 characters).");
        }
        if (idTaken(u)) {
            throw new RuleException("That name is already taken. Please choose another.");
        }
        return u;
    }
 
    private void checkAge(int age, String action) {
        if (age < MIN_AGE) {
            throw new RuleException("You are not qualified. You must be " + MIN_AGE
                + " years old or above to " + action + ".");
        }
    }
 
    public Voter registerVoter(String name, int age, String password) {
        if (isClosed()) {
            throw new RuleException("Registration is closed. Please wait for the next election.");
        }
        checkAge(age, "register");
        String username = checkUsername(name);
        Voter voter = Voter.create(username, username, age, password);
        db.insertVoter(voter);
        voters.put(voter.getVoterId(), voter);
        graph.put(voter.getVoterId(), new HashSet<>());
        return voter;
    }
 
   
    public Candidate registerCandidate(String name, int age, String party, String password) {
        requirePending("sign up as a candidate");
        checkAge(age, "run");
        return saveNewCandidate(name, age, party, password, false);
    }
 
    
    public Candidate addCandidateByAdmin(String name, int age, String party, String password) {
        requirePending("add candidates");
        checkAge(age, "run");
        return saveNewCandidate(name, age, party, password, true);
    }
 
    private Candidate saveNewCandidate(String name, int age, String party,
                                       String password, boolean approved) {
        String username = checkUsername(name);
        Candidate cand = Candidate.create(
            username, username, age, party, approved, password);
        db.insertCandidate(cand);
        candidates.put(cand.getCandidateId(), cand);
        graph.put(cand.getCandidateId(), new HashSet<>());
        return cand;
    }
 
    
    private Candidate findCandidate(String id) {
        Candidate c = candidates.get(id);
        if (c == null) {
            throw new RuleException("Candidate ID not found.");
        }
        return c;
    }
 
    public void approveCandidate(String id) {
        requirePending("approve candidates");
        Candidate c = findCandidate(id);
        if (c.isApproved()) {
            throw new RuleException("That candidate is already approved.");
        }
        c.setApproved(true);
        db.updateCandidateApproved(c);
    }
 
    public void editCandidate(String id, String newName, String newParty) {
        requirePending("edit candidates");
        Candidate c = findCandidate(id);
        if (!newName.trim().isEmpty()) {
            c.setName(newName.trim());
        }
        if (!newParty.trim().isEmpty()) {
            c.setParty(newParty.trim());
        }
        db.updateCandidateInfo(c);
    }
 
    public void removeCandidate(String id) {
        requirePending("remove candidates");
        findCandidate(id);
        candidates.remove(id);
        graph.remove(id);
        db.deleteCandidate(id);
    }
 
    public void setVoterRegistered(String voterId, boolean registered) {
        Voter v = voters.get(voterId);
        if (v == null) {
            throw new RuleException("Voter ID not found.");
        }
        v.setRegistered(registered);
        db.updateVoterRegistered(v);
    }
 
    public Collection<Voter> getAllVoters() {
        return voters.values();
    }
 
    public Collection<Candidate> getAllCandidates() {
        return candidates.values();
    }
 
    
    public List<Candidate> approvedCandidates() {
        List<Candidate> list = new ArrayList<>();
        for (Candidate c : candidates.values()) {
            if (c.isApproved()) {
                list.add(c);
            }
        }
        return list;
    }
 
    
    public Admin loginAdmin(String id, String password) {
        for (Admin a : admins.values()) {
            if (a.getAdminId().equalsIgnoreCase(id) && a.checkPassword(password)) {
                return a;
            }
        }
        return null;
    }
 
    public Voter loginVoter(String id, String password) {
        Voter v = voters.get(id);
        return (v != null && v.checkPassword(password)) ? v : null;
    }
 
    public Candidate loginCandidate(String id, String password) {
        Candidate c = candidates.get(id);
        return (c != null && c.checkPassword(password)) ? c : null;
    }
 
    public void changeAdminPassword(Admin admin, String newPassword) {
        if (newPassword.isEmpty()) {
            throw new RuleException("Password cannot be empty.");
        }
        admin.setPassword(newPassword);
        db.updateAdminPassword(admin);
    }
 
    public boolean resetVoterPassword(String voterId, String newPassword) {
        Voter v = voters.get(voterId);
        if (v == null) {
            return false;
        }
        v.setPassword(newPassword);
        db.updateVoterPassword(v);
        return true;
    }
 
    public boolean resetCandidatePassword(String candidateId, String newPassword) {
        Candidate c = candidates.get(candidateId);
        if (c == null) {
            return false;
        }
        c.setPassword(newPassword);
        db.updateCandidatePassword(c);
        return true;
    }
 
    
    public boolean hasVoted(String voterId) {
        return !graph.get(voterId).isEmpty();
    }
 
    public String castVote(String voterId, String candidateId) {
        if (election.getStatus() == Election.Status.PENDING) {
            return "The election has not started yet.";
        }
        if (election.getStatus() == Election.Status.CLOSED) {
            return "The election is closed.";
        }
        Voter voter = voters.get(voterId);
        if (voter == null) {
            return "Voter not found.";
        }
        if (!voter.isRegistered()) {
            return "Your voter account is blocked. Please contact the admin.";
        }
        Candidate cand = candidates.get(candidateId);
        if (cand == null || !cand.isApproved()) {
            return "Candidate not found on the ballot.";
        }
        if (hasVoted(voterId)) {
            return "You have already voted.";
        }
        Vote vote = new Vote(voterId, candidateId);
        int id = db.insertVote(vote);
        if (id < 0) {
            return "You have already voted.";
        }
        vote.setVoteId(id);
        votes.add(vote);
        graph.get(voterId).add(candidateId);
        graph.get(candidateId).add(voterId);
        return "Vote #" + id + " recorded at " + Reports.fmt(vote.getTimestamp()) + ".";
    }
 
    
    public int voteCount(String candidateId) {
        return graph.get(candidateId).size();
    }
 
    public int totalVotes() {
        return votes.size();
    }
 
    
    public List<Ranked> rankedCandidates() {
        List<Ranked> items = new ArrayList<>();
        for (Candidate c : approvedCandidates()) {
            items.add(new Ranked(voteCount(c.getCandidateId()), c));
        }
        return Sorter.mergeSort(items, new Comparator<Ranked>() {
            @Override
            public int compare(Ranked a, Ranked b) {
                if (a.votes != b.votes) {
                    return Integer.compare(b.votes, a.votes);
                }
                return a.candidate.getName().compareToIgnoreCase(b.candidate.getName());
            }
        });
    }
 
    
    public List<Ranked> winners() {
        List<Ranked> ranked = rankedCandidates();
        List<Ranked> top = new ArrayList<>();
        if (ranked.isEmpty() || ranked.get(0).votes == 0) {
            return top;
        }
        for (Ranked r : ranked) {
            if (r.votes == ranked.get(0).votes) {
                top.add(r);
            }
        }
        return top;
    }
 
   
    public List<Tally> partyTallies() {
        Map<String, Integer> byParty = new LinkedHashMap<>();
        for (Ranked r : rankedCandidates()) {
            String party = r.candidate.getParty();
            byParty.put(party, byParty.getOrDefault(party, 0) + r.votes);
        }
        List<Tally> list = new ArrayList<>();
        for (Map.Entry<String, Integer> e : byParty.entrySet()) {
            list.add(new Tally(e.getKey(), e.getValue()));
        }
        return Sorter.mergeSort(list, new Comparator<Tally>() {
            @Override
            public int compare(Tally a, Tally b) {
                if (a.votes != b.votes) {
                    return Integer.compare(b.votes, a.votes);
                }
                return a.label.compareToIgnoreCase(b.label);
            }
        });
    }
 
    
    public TreeMap<Integer, Integer> votesPerHour() {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        for (Vote v : votes) {
            int hour = v.getTimestamp().getHour();
            map.put(hour, map.getOrDefault(hour, 0) + 1);
        }
        return map;
    }
 
    public int votersWhoVoted() {
        int count = 0;
        for (String id : voters.keySet()) {
            if (hasVoted(id)) {
                count++;
            }
        }
        return count;
    }
 
    public int eligibleVoters() {
        int count = 0;
        for (Voter v : voters.values()) {
            if (v.isRegistered() || hasVoted(v.getVoterId())) {
                count++;
            }
        }
        return count;
    }
}
 