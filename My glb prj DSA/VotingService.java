package klu.service;

import klu.ds.*;
import klu.model.*;
import java.util.*;
import java.util.regex.Pattern;

public class VotingService {

    private static final Pattern ROLL_RE = Pattern.compile("^25200(30|40|80|90)\\d{3}$");
    private static final Map<String, String> DEPT_MAP = Map.of(
        "30", "CSE", "40", "ECE", "80", "AI & DS", "90", "CS & IT"
    );

    private final VoterHashMap<String, Voter> voterMap = new VoterHashMap<>();
    private final VoteLedger<VoteRecord> ledger = new VoteLedger<>();
    private final AuditStack<String> auditStack = new AuditStack<>(512);
    private final VoterHashMap<String, LoginThrottleQueue> throttleMap = new VoterHashMap<>();
    private final Map<String, List<Candidate>> candidatesByCategory = new LinkedHashMap<>();
    private final List<Candidate> allCandidates = new ArrayList<>();

    private static final String ADMIN_PIN  = "12353";
    private static final String ADMIN_NAME = "ramagiri rishik rao";

    public VotingService() {
        seedCandidates();
        if (PersistenceManager.dataExists()) {
            loadFromDisk();
        } else {
            seedFreshData();
            persist();
        }
    }

    // ── Candidates ─────────────────────────────────────────────────
    private void seedCandidates() {
        addCand(1,  "Arjun Sharma",   "CSE",    "College Captain",     "Building a bridge between students and administration.");
        addCand(2,  "Priya Reddy",    "AI & DS","College Captain",     "Empowering every student's dream and potential.");
        addCand(3,  "Karthik Naidu",  "ECE",    "College Captain",     "Together we rise, together we shine.");
        addCand(4,  "Ravi Kumar",     "CS & IT","Sports Captain",      "Sports build character - I'll build champions.");
        addCand(5,  "Sneha Patel",    "CSE",    "Sports Captain",      "Every athlete deserves an equal chance.");
        addCand(6,  "Vikram Singh",   "ECE",    "Sports Captain",      "Fitness, teamwork, victory - that's my promise.");
        addCand(7,  "Meera Iyer",     "AI & DS","Student Union Leader","Your grievances are my agenda. Always.");
        addCand(8,  "Suresh Babu",    "CS & IT","Student Union Leader","A student's voice should never go unheard.");
        addCand(9,  "Divya Rao",      "CSE",    "Student Union Leader","Unity, equity and student welfare - my mission.");
        addCand(10, "Ananya Krishnan","ECE",    "Club Leader",         "Culture, arts and creativity for every student.");
        addCand(11, "Rahul Menon",    "AI & DS","Club Leader",         "Tech clubs that open doors to real-world skills.");
        addCand(12, "Lakshmi Devi",   "CS & IT","Club Leader",         "Inclusion, passion and campus life redefined.");
        allCandidates.sort(Comparator.comparingInt(c -> c.id));
    }

    private void addCand(int id, String name, String dept, String cat, String slogan) {
        Candidate c = new Candidate(id, name, dept, cat, slogan);
        allCandidates.add(c);
        candidatesByCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(c);
    }

    // ── Seed fresh demo data ────────────────────────────────────────
    private void seedFreshData() {
        // Special pre-registered voters
        addVoterRaw("2520030333", "Ramagiri Rishik Rao", "CSE", true);
        addVoterRaw("2520030366", "Nimma Lokesh Reddy",  "CSE", true);
        addVoterRaw("2520030169", "V Bharath Kumar",     "CSE", true);

        // Demo voters
        String[][] demo = {
            {"2520030101","Aditya Verma","CSE"},   {"2520030102","Bhavana Singh","CSE"},
            {"2520030103","Charan Kumar","CSE"},   {"2520040101","Deepak Reddy","ECE"},
            {"2520040102","Esha Patel","ECE"},     {"2520040103","Farhan Ali","ECE"},
            {"2520080101","Geetha Lakshmi","AI & DS"}, {"2520080102","Harish Rao","AI & DS"},
            {"2520090101","Isha Kumari","CS & IT"},{"2520090102","Jagadeesh Babu","CS & IT"},
        };
        for (String[] d : demo) addVoterRaw(d[0], d[1], d[2], false);

        // College Captain: Arjun=5, Priya=3, Karthik=2
        int[][] cc = {{101,1},{102,1},{103,2},{104,1},{105,2},{106,3},{107,1},{108,2},{109,3},{110,1}};
        String[] demRolls = {"2520030101","2520030102","2520030103","2520040101","2520040102",
                             "2520040103","2520080101","2520080102","2520090101","2520090102"};
        for (int i = 0; i < demRolls.length; i++) castDemoVote(demRolls[i], "College Captain", cc[i][1]);

        // Sports Captain: Ravi=4, Sneha=4, Vikram=2
        int[] sp = {4,5,4,6,5,4,5,6,5,4};
        for (int i = 0; i < demRolls.length; i++) castDemoVote(demRolls[i], "Sports Captain", sp[i]);

        // Union Leader: Meera=5, Divya=3, Suresh=2
        int[] ul = {7,9,7,8,7,9,7,8,7,9};
        for (int i = 0; i < demRolls.length; i++) castDemoVote(demRolls[i], "Student Union Leader", ul[i]);

        // Club Leader: Ananya=4, Rahul=4, Lakshmi=2
        int[] cl = {10,11,10,12,11,10,11,12,11,10};
        for (int i = 0; i < demRolls.length; i++) castDemoVote(demRolls[i], "Club Leader", cl[i]);

        auditStack.push("DEMO DATA SEEDED — " + voterMap.size() + " voters, " + ledger.size() + " votes");
    }

    private void addVoterRaw(String roll, String name, String dept, boolean special) {
        Voter v = new Voter(roll, name, dept);
        v.isSpecial = special;
        voterMap.put(roll, v);
        auditStack.push("REGISTER: " + roll + " (" + name + ")");
    }

    private void castDemoVote(String roll, String category, int candidateId) {
        Voter voter = voterMap.get(roll);
        if (voter == null || voter.hasVotedIn(category)) return;
        Candidate c = findCandidateByIdBinarySearch(candidateId);
        if (c == null) return;
        c.votes++;
        voter.votes.put(category, c.name);
        ledger.append(new VoteRecord(roll, category, c.name, candidateId));
    }

    // ── Load from disk ──────────────────────────────────────────────
    private void loadFromDisk() {
        for (String[] row : PersistenceManager.loadVoters()) {
            Voter v = new Voter(row[0], row[1], row[2]);
            v.status  = "BLOCKED".equals(row[3]) ? Voter.Status.BLOCKED : Voter.Status.ACTIVE;
            v.isSpecial = Boolean.parseBoolean(row[4]);
            voterMap.put(row[0], v);
        }
        Map<Integer, Integer> cv = PersistenceManager.loadCandidateVotes();
        for (Candidate c : allCandidates) if (cv.containsKey(c.id)) c.votes = cv.get(c.id);
        for (String[] row : PersistenceManager.loadVotes()) {
            Voter v = voterMap.get(row[0]);
            if (v != null) v.votes.put(row[1], row[2]);
        }
        for (String[] row : PersistenceManager.loadLedger()) {
            try { ledger.append(new VoteRecord(row[0], row[1], row[2], Integer.parseInt(row[3]))); }
            catch (NumberFormatException ignored) {}
        }
        auditStack.push("DATA LOADED — " + voterMap.size() + " voters, " + ledger.size() + " ledger entries");
    }

    public void persist() {
        List<Voter> voters = voterMap.values();
        PersistenceManager.saveVoters(voters);
        PersistenceManager.saveVotes(voters);
        PersistenceManager.saveLedger(ledger.toList());
        PersistenceManager.saveCandidateVotes(allCandidates);
    }

    // ── CO1: Binary Search ──────────────────────────────────────────
    public Candidate findCandidateByIdBinarySearch(int targetId) {
        int lo = 0, hi = allCandidates.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int midId = allCandidates.get(mid).id;
            if (midId == targetId) return allCandidates.get(mid);
            else if (midId < targetId) lo = mid + 1;
            else hi = mid - 1;
        }
        return null;
    }

    public Candidate findCandidateByNameLinear(String name) {
        for (Candidate c : allCandidates) if (c.name.equalsIgnoreCase(name)) return c;
        return null;
    }

    // ── Voter Registration ──────────────────────────────────────────
    public boolean registerVoter(String roll, String name, String dept, boolean special) {
        if (!ROLL_RE.matcher(roll).matches() || voterMap.containsKey(roll)) return false;
        addVoterRaw(roll, name, dept, special);
        persist();
        return true;
    }

    public boolean registerVoter(String roll, String name) {
        if (!ROLL_RE.matcher(roll).matches()) return false;
        String dept = DEPT_MAP.getOrDefault(roll.substring(5, 7), "KLU");
        return registerVoter(roll, name, dept, false);
    }

    public String detectDept(String roll) {
        if (roll.length() < 7) return "Unknown";
        return DEPT_MAP.getOrDefault(roll.substring(5, 7), "Unknown");
    }

    // ── Login ───────────────────────────────────────────────────────
    public enum LoginResult { SUCCESS, ALREADY_VOTED_ALL, WRONG_CREDENTIALS, INVALID_ROLL, BLOCKED, RATE_LIMITED }

    public LoginResult studentLogin(String roll, String password) {
        if (!ROLL_RE.matcher(roll).matches()) return LoginResult.INVALID_ROLL;
        LoginThrottleQueue q = throttleMap.get(roll);
        if (q == null) { q = new LoginThrottleQueue(3); throttleMap.put(roll, q); }
        if (q.isRateLimited(3, 30_000)) return LoginResult.RATE_LIMITED;

        Voter v = voterMap.get(roll);
        if (v == null) {
            registerVoter(roll, titleCase(password.trim()), DEPT_MAP.getOrDefault(roll.substring(5,7),"KLU"), false);
            v = voterMap.get(roll);
        }
        if (v.status == Voter.Status.BLOCKED) return LoginResult.BLOCKED;
        if (v.isSpecial && !password.trim().equalsIgnoreCase(v.name)) {
            q.enqueue(System.currentTimeMillis());
            return LoginResult.WRONG_CREDENTIALS;
        }
        if (v.hasVotedAll()) return LoginResult.ALREADY_VOTED_ALL;
        return LoginResult.SUCCESS;
    }

    public boolean adminLogin(String pin, String name) {
        return ADMIN_PIN.equals(pin.trim()) && ADMIN_NAME.equalsIgnoreCase(name.trim());
    }

    // ── Voting ──────────────────────────────────────────────────────
    public enum VoteResult { SUCCESS, ALREADY_VOTED, INVALID_CATEGORY, INVALID_CANDIDATE, VOTER_NOT_FOUND, VOTER_BLOCKED }

    public VoteResult castVote(String roll, String category, int candidateId) {
        Voter voter = voterMap.get(roll);
        if (voter == null) return VoteResult.VOTER_NOT_FOUND;
        if (voter.status == Voter.Status.BLOCKED) return VoteResult.VOTER_BLOCKED;
        if (voter.hasVotedIn(category)) return VoteResult.ALREADY_VOTED;
        if (!candidatesByCategory.containsKey(category)) return VoteResult.INVALID_CATEGORY;
        Candidate c = findCandidateByIdBinarySearch(candidateId);
        if (c == null || !c.category.equals(category)) return VoteResult.INVALID_CANDIDATE;
        c.votes++;
        voter.votes.put(category, c.name);
        ledger.append(new VoteRecord(roll, category, c.name, candidateId));
        auditStack.push("VOTE: " + roll + " -> " + category + " -> " + c.name);
        persist();
        return VoteResult.SUCCESS;
    }

    // ── Admin ops ───────────────────────────────────────────────────
    public boolean blockVoter(String roll) {
        Voter v = voterMap.get(roll);
        if (v == null) return false;
        v.status = Voter.Status.BLOCKED;
        auditStack.push("BLOCK: " + roll);
        persist(); return true;
    }

    public boolean unblockVoter(String roll) {
        Voter v = voterMap.get(roll);
        if (v == null) return false;
        v.status = Voter.Status.ACTIVE;
        auditStack.push("UNBLOCK: " + roll);
        persist(); return true;
    }

    public boolean resetVoterVotes(String roll) {
        Voter v = voterMap.get(roll);
        if (v == null) return false;
        for (Map.Entry<String, String> e : v.votes.entrySet()) {
            Candidate c = findCandidateByNameLinear(e.getValue());
            if (c != null && c.votes > 0) c.votes--;
        }
        v.votes.clear();
        auditStack.push("RESET_VOTES: " + roll);
        persist(); return true;
    }

    public void resetAllVotes() {
        for (Candidate c : allCandidates) c.votes = 0;
        voterMap.values().forEach(v -> v.votes.clear());
        auditStack.push("RESET_ALL_VOTES by admin");
        persist();
    }

    // ── CO3: Max-Heap ranking ───────────────────────────────────────
    public List<Candidate> getRankedCandidates(String category) {
        List<Candidate> cats = candidatesByCategory.get(category);
        if (cats == null) return Collections.emptyList();
        CandidateHeap heap = new CandidateHeap(cats.size());
        for (Candidate c : cats) heap.insert(c.id, c.name, c.votes);
        List<Candidate> ranked = new ArrayList<>();
        while (!heap.isEmpty()) {
            int[] top = heap.extractMax();
            Candidate found = findCandidateByIdBinarySearch(top[0]);
            if (found != null) ranked.add(found);
        }
        return ranked;
    }

    // ── CO1: Merge Sort voters ──────────────────────────────────────
    public List<Voter> getAllVotersSorted() {
        List<Voter> list = new ArrayList<>(voterMap.values());
        mergeSort(list, 0, list.size() - 1);
        return list;
    }

    private void mergeSort(List<Voter> arr, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        mergeSort(arr, lo, mid); mergeSort(arr, mid + 1, hi); merge(arr, lo, mid, hi);
    }

    private void merge(List<Voter> arr, int lo, int mid, int hi) {
        List<Voter> left  = new ArrayList<>(arr.subList(lo, mid + 1));
        List<Voter> right = new ArrayList<>(arr.subList(mid + 1, hi + 1));
        int i = 0, j = 0, k = lo;
        while (i < left.size() && j < right.size())
            arr.set(k++, left.get(i).name.compareToIgnoreCase(right.get(j).name) <= 0 ? left.get(i++) : right.get(j++));
        while (i < left.size()) arr.set(k++, left.get(i++));
        while (j < right.size()) arr.set(k++, right.get(j++));
    }

    // ── Stats ────────────────────────────────────────────────────────
    public int totalVoters()        { return voterMap.size(); }
    public int votedCount()         { return (int) voterMap.values().stream().filter(Voter::hasVotedAll).count(); }
    public int pendingCount()       { return totalVoters() - votedCount(); }
    public int blockedCount()       { return (int) voterMap.values().stream().filter(v -> v.status == Voter.Status.BLOCKED).count(); }
    public int totalVotesInLedger() { return ledger.size(); }
    public List<String> getAuditLog()                      { return auditStack.toList(); }
    public List<VoteRecord> getLedger()                    { return ledger.toList(); }
    public Map<String, List<Candidate>> getCandidatesByCategory() { return candidatesByCategory; }
    public List<Candidate> getAllCandidates()               { return allCandidates; }
    public Voter getVoter(String roll)                      { return voterMap.get(roll); }

    public List<Voter> searchVoters(String query) {
        String q = query.toLowerCase();
        List<Voter> res = new ArrayList<>();
        for (Voter v : voterMap.values())
            if (v.roll.contains(q) || v.name.toLowerCase().contains(q) || v.dept.toLowerCase().contains(q)) res.add(v);
        return res;
    }

    private String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (String p : s.trim().split("\\s+"))
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
        return sb.toString().trim();
    }
}
