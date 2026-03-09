package klu.service;

import klu.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Simple flat-file persistence for voters, votes, and ledger.
 * Saves to klu_data/ directory alongside the JAR.
 */
public class PersistenceManager {

    private static final String DATA_DIR  = "klu_data";
    private static final String VOTER_FILE  = DATA_DIR + "/voters.csv";
    private static final String VOTES_FILE  = DATA_DIR + "/votes.csv";
    private static final String LEDGER_FILE = DATA_DIR + "/ledger.csv";
    private static final String CAND_FILE   = DATA_DIR + "/candidates.csv";

    public static void ensureDir() {
        new File(DATA_DIR).mkdirs();
    }

    // ── SAVE ──────────────────────────────────────────────────────

    public static void saveVoters(List<Voter> voters) {
        ensureDir();
        try (PrintWriter pw = new PrintWriter(new FileWriter(VOTER_FILE))) {
            pw.println("roll,name,dept,status,special");
            for (Voter v : voters) {
                pw.printf("%s,%s,%s,%s,%b%n",
                    esc(v.roll), esc(v.name), esc(v.dept),
                    v.status.name(), v.isSpecial);
            }
        } catch (IOException e) { /* silent */ }
    }

    public static void saveVotes(List<Voter> voters) {
        ensureDir();
        try (PrintWriter pw = new PrintWriter(new FileWriter(VOTES_FILE))) {
            pw.println("roll,category,candidateName");
            for (Voter v : voters) {
                for (Map.Entry<String, String> e : v.votes.entrySet()) {
                    pw.printf("%s,%s,%s%n",
                        esc(v.roll), esc(e.getKey()), esc(e.getValue()));
                }
            }
        } catch (IOException e) { /* silent */ }
    }

    public static void saveLedger(List<VoteRecord> records) {
        ensureDir();
        try (PrintWriter pw = new PrintWriter(new FileWriter(LEDGER_FILE))) {
            pw.println("roll,category,candidateName,candidateId,timestamp");
            for (VoteRecord r : records) {
                pw.printf("%s,%s,%s,%d,%d%n",
                    esc(r.rollNumber), esc(r.category),
                    esc(r.candidateName), r.candidateId, r.timestamp);
            }
        } catch (IOException e) { /* silent */ }
    }

    public static void saveCandidateVotes(List<Candidate> candidates) {
        ensureDir();
        try (PrintWriter pw = new PrintWriter(new FileWriter(CAND_FILE))) {
            pw.println("id,votes");
            for (Candidate c : candidates) {
                pw.printf("%d,%d%n", c.id, c.votes);
            }
        } catch (IOException e) { /* silent */ }
    }

    // ── LOAD ──────────────────────────────────────────────────────

    public static List<String[]> loadVoters() {
        return loadCsv(VOTER_FILE, 5);
    }

    public static List<String[]> loadVotes() {
        return loadCsv(VOTES_FILE, 3);
    }

    public static List<String[]> loadLedger() {
        return loadCsv(LEDGER_FILE, 5);
    }

    public static Map<Integer, Integer> loadCandidateVotes() {
        Map<Integer, Integer> map = new HashMap<>();
        for (String[] row : loadCsv(CAND_FILE, 2)) {
            try { map.put(Integer.parseInt(row[0]), Integer.parseInt(row[1])); }
            catch (NumberFormatException ignored) {}
        }
        return map;
    }

    public static boolean dataExists() {
        return new File(VOTER_FILE).exists();
    }

    // ── HELPERS ───────────────────────────────────────────────────

    private static List<String[]> loadCsv(String path, int cols) {
        List<String[]> rows = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return rows;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] parts = splitCsv(line, cols);
                if (parts != null) rows.add(parts);
            }
        } catch (IOException e) { /* silent */ }
        return rows;
    }

    private static String[] splitCsv(String line, int expected) {
        // Simple CSV split respecting quoted fields
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (char c : line.toCharArray()) {
            if (c == '"') { inQuote = !inQuote; }
            else if (c == ',' && !inQuote) { fields.add(unescape(cur.toString())); cur.setLength(0); }
            else cur.append(c);
        }
        fields.add(unescape(cur.toString()));
        if (fields.size() < expected) return null;
        return fields.toArray(new String[0]);
    }

    private static String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private static String unescape(String s) {
        if (s.startsWith("\"") && s.endsWith("\""))
            return s.substring(1, s.length()-1).replace("\"\"", "\"");
        return s;
    }
}
