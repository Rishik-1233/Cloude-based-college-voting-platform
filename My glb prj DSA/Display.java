package klu.service;

import klu.model.*;
import java.util.*;

public class Display {
    // ANSI colours
    public static final String RESET  = "\u001B[0m";
    public static final String BOLD   = "\u001B[1m";
    public static final String BLUE   = "\u001B[34m";
    public static final String CYAN   = "\u001B[36m";
    public static final String GREEN  = "\u001B[32m";
    public static final String RED    = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String WHITE  = "\u001B[37m";
    public static final String PURPLE = "\u001B[35m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_NAV = "\u001B[48;5;17m";

    public static void banner() {
        System.out.println();
        System.out.println(BG_NAV + BOLD + WHITE);
        System.out.println("  ╔══════════════════════════════════════════════════════════════╗  ");
        System.out.println("  ║          KLU CLOUD-BASED COLLEGE VOTING PLATFORM             ║  ");
        System.out.println("  ║              K L University — Student Elections 2025          ║  ");
        System.out.println("  ║        Secure  ●  Transparent  ●  Democratic                 ║  ");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝  ");
        System.out.println(RESET);
    }

    public static void header(String title, String icon) {
        System.out.println("\n" + BOLD + BLUE + "  " + icon + " " + title + RESET);
        System.out.println(CYAN + "  " + "─".repeat(55) + RESET);
    }

    public static void success(String msg) {
        System.out.println(GREEN + "  ✔  " + msg + RESET);
    }

    public static void error(String msg) {
        System.out.println(RED + "  ✘  " + msg + RESET);
    }

    public static void info(String msg) {
        System.out.println(CYAN + "  ℹ  " + msg + RESET);
    }

    public static void warn(String msg) {
        System.out.println(YELLOW + "  ⚠  " + msg + RESET);
    }

    public static void line() {
        System.out.println(CYAN + "  " + "─".repeat(55) + RESET);
    }

    public static void statsStrip(int total, int voted, int pending, int blocked) {
        System.out.println("\n" + BOLD + WHITE + "  ┌──────────┬──────────┬──────────┬──────────┐");
        System.out.printf("  │ %sTOTAL%s    │ %sVOTED%s    │ %sPENDING%s  │ %sBLOCKED%s  │%n",
            CYAN,WHITE, GREEN,WHITE, YELLOW,WHITE, RED,WHITE);
        System.out.printf("  │ %s%-8d%s │ %s%-8d%s │ %s%-8d%s │ %s%-8d%s │%n",
            CYAN,total,WHITE, GREEN,voted,WHITE, YELLOW,pending,WHITE, RED,blocked,WHITE);
        System.out.println("  └──────────┴──────────┴──────────┴──────────┘" + RESET);
    }

    public static void candidateTable(List<Candidate> candidates, Map<String, String> votedFor) {
        System.out.printf("%n  %-4s %-20s %-12s %-6s %s%n",
            BOLD+"ID"+RESET, BOLD+"Name"+RESET, BOLD+"Dept"+RESET, BOLD+"Votes"+RESET, BOLD+"Status"+RESET);
        System.out.println("  " + "─".repeat(60));
        for (Candidate c : candidates) {
            boolean isVoted = votedFor != null && c.name.equals(votedFor.get(c.category));
            String mark = isVoted ? GREEN + " ✔ Voted" + RESET : "";
            System.out.printf("  %-4d %-20s %-12s %-6d %s%n",
                c.id, c.name, c.dept, c.votes, mark);
        }
    }

    public static void voterTable(List<Voter> voters) {
        System.out.printf("%n  %-12s %-25s %-10s %-8s %-8s%n",
            BOLD+"Roll"+RESET, BOLD+"Name"+RESET, BOLD+"Dept"+RESET, BOLD+"Status"+RESET, BOLD+"Votes"+RESET);
        System.out.println("  " + "─".repeat(70));
        for (Voter v : voters) {
            String statusStr = v.status == Voter.Status.BLOCKED
                ? RED+"BLOCKED"+RESET : GREEN+"ACTIVE"+RESET;
            System.out.printf("  %-12s %-25s %-10s %-16s %d/4%n",
                v.roll, v.name, v.dept, statusStr, v.voteCount());
        }
    }

    public static void resultsBar(List<Candidate> ranked, String category) {
        System.out.println("\n" + BOLD + PURPLE + "  🏆 " + category + " Results" + RESET);
        int maxVotes = ranked.isEmpty() ? 1 : Math.max(1, ranked.get(0).votes);
        for (int i = 0; i < ranked.size(); i++) {
            Candidate c = ranked.get(i);
            int barLen = maxVotes == 0 ? 0 : (int)((double)c.votes / maxVotes * 20);
            String bar = "█".repeat(barLen) + "░".repeat(20 - barLen);
            String prefix = i == 0 ? YELLOW + "  🥇 " : (i == 1 ? WHITE + "  🥈 " : "  🥉 ");
            System.out.printf("%s%-20s%s %s%s%s %d vote(s)%n",
                prefix, c.name, RESET, GREEN, bar, RESET, c.votes);
        }
    }

    public static void prompt(String msg) {
        System.out.print(YELLOW + "  ► " + msg + RESET);
    }

    public static void divider(String label) {
        int pad = (55 - label.length()) / 2;
        System.out.println("\n" + CYAN + "  " + "─".repeat(pad) + " " + label + " " + "─".repeat(pad) + RESET);
    }
}
