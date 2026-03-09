package klu;

import klu.model.*;
import klu.service.*;
import klu.service.VotingService.*;
import java.util.*;

/**
 * KLU Cloud Based College Voting Platform
 * Terminal Application
 *
 * DSA Concepts Applied (as per Course Outcomes):
 *   CO1 - Binary Search (candidates), Merge Sort (voter list)
 *   CO2 - Doubly Linked List (vote ledger)
 *   CO3 - Stack (audit), Circular Queue (rate-limiting), Max-Heap (rankings)
 *   CO4 - Custom HashMap with chaining (voter registry)
 *   CO5 - Practical voting application with full CRUD
 *   CO6 - Integrated Java OOP + DSA program
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final VotingService svc = new VotingService();
    private static Voter currentVoter = null;

    public static void main(String[] args) {
        Display.banner();
        mainMenu();
    }

    // ═══════════════════════════════════════
    //  MAIN MENU
    // ═══════════════════════════════════════
    static void mainMenu() {
        while (true) {
            Display.divider("MAIN MENU");
            System.out.println("  " + Display.CYAN + "[1]" + Display.RESET + " Student Login");
            System.out.println("  " + Display.PURPLE + "[2]" + Display.RESET + " Admin Login");
            System.out.println("  " + Display.YELLOW + "[3]" + Display.RESET + " View Candidates");
            System.out.println("  " + Display.RED + "[0]" + Display.RESET + " Exit");
            Display.prompt("Choose: ");
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> studentLoginFlow();
                case "2" -> adminLoginFlow();
                case "3" -> viewCandidatesPublic();
                case "0" -> { Display.success("Thank you for using KLU Voting Platform!"); System.exit(0); }
                default  -> Display.error("Invalid option.");
            }
        }
    }

    // ═══════════════════════════════════════
    //  STUDENT LOGIN FLOW
    // ═══════════════════════════════════════
    static void studentLoginFlow() {
        Display.header("Student Secure Login", "🎓");
        Display.info("Roll format: 25200 + (30=CSE|40=ECE|80=AI&DS|90=CS&IT) + 3 digits");
        Display.info("Password = your full name (case-insensitive)");
        System.out.println();

        Display.prompt("Enter Roll Number: ");
        String roll = sc.nextLine().trim();

        Display.prompt("Enter Password (your name): ");
        String pass = sc.nextLine().trim();

        LoginResult result = svc.studentLogin(roll, pass);

        switch (result) {
            case SUCCESS -> {
                currentVoter = svc.getVoter(roll);
                if (currentVoter == null) {
                    Display.error("Voter record not found.");
                    return;
                }
                Display.success("Welcome, " + currentVoter.name + " | " + currentVoter.dept);
                votingMenu();
            }
            case ALREADY_VOTED_ALL -> {
                Display.warn("You have already voted in all 4 categories. Thank you!");
                currentVoter = svc.getVoter(roll);
                if (currentVoter != null) viewMyVotes();
            }
            case INVALID_ROLL    -> Display.error("Invalid roll number format. Use: 25200 + dept code (30/40/80/90) + 3 digits.");
            case WRONG_CREDENTIALS -> Display.error("Incorrect password for this roll number.");
            case BLOCKED         -> Display.error("Your account has been blocked. Please contact the admin.");
            case RATE_LIMITED    -> Display.error("Too many failed attempts. Please wait 30 seconds.");
        }
    }

    // ═══════════════════════════════════════
    //  VOTING MENU
    // ═══════════════════════════════════════
    static void votingMenu() {
        while (true) {
            System.out.println();
            Display.statsStrip(svc.totalVoters(), svc.votedCount(), svc.pendingCount(), svc.blockedCount());
            Display.divider("VOTING MENU — " + currentVoter.name);
            Display.info("Categories voted: " + currentVoter.voteCount() + "/4");
            System.out.println();
            System.out.println("  " + Display.CYAN + "[1]" + Display.RESET + " Vote — College Captain");
            System.out.println("  " + Display.CYAN + "[2]" + Display.RESET + " Vote — Sports Captain");
            System.out.println("  " + Display.CYAN + "[3]" + Display.RESET + " Vote — Student Union Leader");
            System.out.println("  " + Display.CYAN + "[4]" + Display.RESET + " Vote — Club Leader");
            System.out.println("  " + Display.GREEN + "[5]" + Display.RESET + " My Votes");
            System.out.println("  " + Display.RED + "[0]" + Display.RESET + " Logout");
            Display.prompt("Choose: ");
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> castVoteFlow("College Captain");
                case "2" -> castVoteFlow("Sports Captain");
                case "3" -> castVoteFlow("Student Union Leader");
                case "4" -> castVoteFlow("Club Leader");
                case "5" -> viewMyVotes();
                case "0" -> { Display.success("Logged out."); currentVoter = null; return; }
                default  -> Display.error("Invalid option.");
            }
            if (currentVoter != null && currentVoter.hasVotedAll()) {
                System.out.println();
                Display.success("🎉 You have voted in all 4 categories! Thank you for participating!");
                viewMyVotes();
                currentVoter = null;
                return;
            }
        }
    }

    static void castVoteFlow(String category) {
        if (currentVoter.hasVotedIn(category)) {
            Display.warn("You already voted in: " + category + " → " + currentVoter.votes.get(category));
            return;
        }

        Display.header("Vote: " + category, "🗳");
        List<Candidate> candidates = svc.getCandidatesByCategory().get(category);
        if (candidates == null) { Display.error("Category not found."); return; }

        Display.candidateTable(candidates, currentVoter.votes);
        System.out.println();
        Display.prompt("Enter Candidate ID to vote (or 0 to cancel): ");
        String input = sc.nextLine().trim();
        if (input.equals("0")) return;

        int id;
        try { id = Integer.parseInt(input); }
        catch (NumberFormatException e) { Display.error("Invalid ID."); return; }

        VoteResult res = svc.castVote(currentVoter.roll, category, id);
        switch (res) {
            case SUCCESS         -> Display.success("Vote cast for: " + currentVoter.votes.get(category));
            case ALREADY_VOTED   -> Display.warn("Already voted in this category.");
            case INVALID_CANDIDATE -> Display.error("Invalid candidate ID for this category.");
            case INVALID_CATEGORY  -> Display.error("Invalid category.");
            case VOTER_BLOCKED     -> Display.error("Your account is blocked.");
            default                -> Display.error("Vote failed. Please try again.");
        }
    }

    static void viewMyVotes() {
        Display.header("My Votes", "📋");
        if (currentVoter.votes.isEmpty()) {
            Display.info("You have not voted in any category yet.");
            return;
        }
        for (Map.Entry<String, String> e : currentVoter.votes.entrySet())
            System.out.println("  " + Display.GREEN + "✔" + Display.RESET + " " +
                Display.BOLD + e.getKey() + Display.RESET + " → " + e.getValue());
    }

    static void viewCandidatesPublic() {
        Display.header("All Candidates", "🏛");
        for (Map.Entry<String, List<Candidate>> entry : svc.getCandidatesByCategory().entrySet()) {
            Display.divider(entry.getKey());
            Display.candidateTable(entry.getValue(), null);
        }
    }

    // ═══════════════════════════════════════
    //  ADMIN LOGIN
    // ═══════════════════════════════════════
    static void adminLoginFlow() {
        Display.header("Admin Secure Access", "🔐");
        Display.warn("This panel is STRICTLY restricted to election administrators.");
        Display.warn("All access attempts are logged via Audit Stack (DSA).");
        System.out.println();

        Display.prompt("Enter Admin PIN: ");
        String pin = sc.nextLine().trim();
        Display.prompt("Enter Your Full Name: ");
        String name = sc.nextLine().trim();

        if (svc.adminLogin(pin, name)) {
            Display.success("Admin access granted. Welcome, " + name + "!");
            adminMenu();
        } else {
            Display.error("Invalid PIN or name. Access denied.");
        }
    }

    // ═══════════════════════════════════════
    //  ADMIN MENU
    // ═══════════════════════════════════════
    static void adminMenu() {
        while (true) {
            System.out.println();
            Display.statsStrip(svc.totalVoters(), svc.votedCount(), svc.pendingCount(), svc.blockedCount());
            Display.divider("ADMIN DASHBOARD");
            System.out.println("  " + Display.CYAN   + "[1]" + Display.RESET + " Voter Management");
            System.out.println("  " + Display.GREEN  + "[2]" + Display.RESET + " Add Voter");
            System.out.println("  " + Display.YELLOW + "[3]" + Display.RESET + " Live Results (Heap Ranking)");
            System.out.println("  " + Display.PURPLE + "[4]" + Display.RESET + " Vote Ledger (Linked List)");
            System.out.println("  " + Display.WHITE  + "[5]" + Display.RESET + " Audit Log (Stack)");
            System.out.println("  " + Display.RED    + "[6]" + Display.RESET + " Reset All Votes");
            System.out.println("  " + Display.CYAN   + "[7]" + Display.RESET + " DSA Demos");
            System.out.println("  " + Display.RED    + "[0]" + Display.RESET + " Logout");
            Display.prompt("Choose: ");
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> voterManagement();
                case "2" -> addVoterFlow();
                case "3" -> liveResults();
                case "4" -> viewLedger();
                case "5" -> viewAuditLog();
                case "6" -> resetVotesFlow();
                case "7" -> dsaDemoMenu();
                case "0" -> { Display.success("Admin logged out."); return; }
                default  -> Display.error("Invalid option.");
            }
        }
    }

    static void voterManagement() {
        Display.header("Voter Management", "👥");
        System.out.println("  [1] View All Voters (Merge-Sorted)");
        System.out.println("  [2] Search Voter");
        System.out.println("  [3] Block Voter");
        System.out.println("  [4] Unblock Voter");
        System.out.println("  [5] Reset Voter Votes");
        System.out.println("  [0] Back");
        Display.prompt("Choose: ");
        String ch = sc.nextLine().trim();
        switch (ch) {
            case "1" -> { Display.voterTable(svc.getAllVotersSorted()); }
            case "2" -> {
                Display.prompt("Search (roll/name/dept): ");
                String q = sc.nextLine().trim();
                List<Voter> found = svc.searchVoters(q);
                if (found.isEmpty()) Display.info("No voters found.");
                else Display.voterTable(found);
            }
            case "3" -> {
                Display.prompt("Enter roll to block: ");
                String r = sc.nextLine().trim();
                if (svc.blockVoter(r)) Display.success("Voter " + r + " blocked.");
                else Display.error("Voter not found.");
            }
            case "4" -> {
                Display.prompt("Enter roll to unblock: ");
                String r = sc.nextLine().trim();
                if (svc.unblockVoter(r)) Display.success("Voter " + r + " unblocked.");
                else Display.error("Voter not found.");
            }
            case "5" -> {
                Display.prompt("Enter roll to reset votes: ");
                String r = sc.nextLine().trim();
                if (svc.resetVoterVotes(r)) Display.success("Votes reset for " + r + ".");
                else Display.error("Voter not found.");
            }
        }
    }

    static void addVoterFlow() {
        Display.header("Add New Voter", "➕");
        Display.info("Roll format: 25200 + (30=CSE|40=ECE|80=AI&DS|90=CS&IT) + 3 digits");
        Display.prompt("Roll Number: ");
        String roll = sc.nextLine().trim();
        Display.prompt("Full Name: ");
        String name = sc.nextLine().trim();

        String dept = svc.detectDept(roll);
        Display.info("Detected department: " + dept);

        if (svc.registerVoter(roll, name)) {
            Display.success("Voter registered: " + roll + " — " + name + " (" + dept + ")");
        } else {
            Display.error("Registration failed. Roll may be invalid or already registered.");
        }
    }

    static void liveResults() {
        Display.header("Live Results — Max-Heap Ranking (CO3)", "🏆");
        Display.info("Using CandidateHeap (max-heap) to extract top-ranked candidates.");
        for (String cat : svc.getCandidatesByCategory().keySet()) {
            List<Candidate> ranked = svc.getRankedCandidates(cat);
            Display.resultsBar(ranked, cat);
        }
        System.out.println();
        Display.info("Total votes recorded in ledger: " + svc.totalVotesInLedger());
    }

    static void viewLedger() {
        Display.header("Vote Ledger — Doubly Linked List (CO2)", "📜");
        Display.info("Each vote is appended as a node to the immutable vote ledger.");
        List<VoteRecord> records = svc.getLedger();
        if (records.isEmpty()) { Display.info("No votes cast yet."); return; }
        System.out.println();
        System.out.printf("  %-22s %-14s %-25s %-6s%n",
            Display.BOLD+"Time"+Display.RESET,
            Display.BOLD+"Roll"+Display.RESET,
            Display.BOLD+"Category"+Display.RESET,
            Display.BOLD+"Candidate"+Display.RESET);
        System.out.println("  " + "─".repeat(72));
        for (VoteRecord r : records)
            System.out.printf("  %-22s %-14s %-25s %s%n",
                r.formattedTime(), r.rollNumber, r.category, r.candidateName);
    }

    static void viewAuditLog() {
        Display.header("Admin Audit Log — Stack LIFO (CO3)", "📑");
        Display.info("Most recent actions shown first (stack top).");
        List<String> log = svc.getAuditLog();
        if (log.isEmpty()) { Display.info("Audit log is empty."); return; }
        for (int i = 0; i < log.size(); i++)
            System.out.println("  " + Display.CYAN + "[" + (i+1) + "]" + Display.RESET + " " + log.get(i));
    }

    static void resetVotesFlow() {
        Display.warn("WARNING: This will reset ALL votes for ALL voters!");
        Display.prompt("Type 'CONFIRM' to proceed: ");
        String c = sc.nextLine().trim();
        if (c.equals("CONFIRM")) {
            svc.resetAllVotes();
            Display.success("All votes have been reset.");
        } else {
            Display.info("Reset cancelled.");
        }
    }

    // ═══════════════════════════════════════
    //  DSA DEMOS
    // ═══════════════════════════════════════
    static void dsaDemoMenu() {
        while (true) {
            Display.divider("DSA DEMOS (Academic CO1–CO4)");
            System.out.println("  [1] CO1: Binary Search — Find Candidate by ID");
            System.out.println("  [1] CO1: Merge Sort — Sorted Voter List");
            System.out.println("  [2] CO2: Doubly Linked List Traversal (Ledger)");
            System.out.println("  [3] CO3: Heap — Manual Ranking Extraction");
            System.out.println("  [4] CO4: HashMap — Voter Lookup");
            System.out.println("  [0] Back");
            Display.prompt("Choose: ");
            String ch = sc.nextLine().trim();
            switch (ch) {
                case "1" -> demoSearchSort();
                case "2" -> demoLinkedList();
                case "3" -> demoHeap();
                case "4" -> demoHashMap();
                case "0" -> { return; }
                default  -> Display.error("Invalid option.");
            }
        }
    }

    static void demoSearchSort() {
        Display.header("CO1: Searching & Sorting", "🔍");

        // Binary Search
        Display.info("Binary Search (O(log n)) — searching sorted candidate list by ID");
        Display.prompt("Enter Candidate ID to search (1–12): ");
        String in = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(in);
            long t1 = System.nanoTime();
            Candidate c = svc.findCandidateByIdBinarySearch(id);
            long t2 = System.nanoTime();
            if (c != null)
                Display.success("Found: " + c + " | Time: " + (t2-t1) + " ns");
            else
                Display.error("Not found.");
        } catch (NumberFormatException e) { Display.error("Invalid number."); }

        // Merge Sort
        Display.info("\nMerge Sort (O(n log n)) — voters sorted alphabetically by name:");
        List<Voter> sorted = svc.getAllVotersSorted();
        Display.voterTable(sorted);
    }

    static void demoLinkedList() {
        Display.header("CO2: Doubly Linked List — Vote Ledger", "🔗");
        List<VoteRecord> recs = svc.getLedger();
        if (recs.isEmpty()) { Display.info("No votes in ledger yet. Cast some votes first!"); return; }
        Display.info("Forward traversal (head → tail):");
        for (VoteRecord r : recs)
            System.out.println("    " + Display.CYAN + "→" + Display.RESET + " " + r);
        Display.info("\nBackward traversal (tail → head):");
        for (int i = recs.size()-1; i >= 0; i--)
            System.out.println("    " + Display.PURPLE + "←" + Display.RESET + " " + recs.get(i));
    }

    static void demoHeap() {
        Display.header("CO3: Max-Heap Priority Queue — Ranking", "📊");
        Display.info("Extracting candidates by descending votes using Max-Heap:");
        for (String cat : svc.getCandidatesByCategory().keySet()) {
            List<Candidate> ranked = svc.getRankedCandidates(cat);
            System.out.println("\n  " + Display.BOLD + cat + Display.RESET);
            for (int i = 0; i < ranked.size(); i++) {
                Candidate c = ranked.get(i);
                System.out.printf("    Rank #%d: %-20s (%d votes)%n", i+1, c.name, c.votes);
            }
        }
    }

    static void demoHashMap() {
        Display.header("CO4: Custom HashMap — Voter Registry", "🗂");
        Display.info("Using VoterHashMap<String, Voter> with chaining:");
        Display.prompt("Enter a roll number to look up: ");
        String roll = sc.nextLine().trim();
        long t1 = System.nanoTime();
        Voter v = svc.getVoter(roll);
        long t2 = System.nanoTime();
        if (v != null) {
            Display.success("O(1) lookup: " + v);
            Display.info("Lookup time: " + (t2-t1) + " nanoseconds");
        } else {
            Display.error("Not found. Time: " + (t2-t1) + " ns");
        }
        Display.info("Total voters in HashMap: " + svc.totalVoters());
    }
}
