# KLU Cloud Based College Voting Platform
## Java + DSA Terminal Application

**K L University — Student Elections 2025**

---

## 🚀 How to Run

### Option 1 — Pre-compiled JAR (Fastest)
```bash
java -jar KLUVoting.jar
```

### Option 2 — Shell Script
```bash
chmod +x run.sh
./run.sh
```

### Option 3 — Compile & Run Manually
```bash
# With standard JDK (javac available)
mkdir out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out klu.Main

# With JRE only (OpenJDK with jdk.compiler module)
java -m jdk.compiler/com.sun.tools.javac.Main -d out @sources.txt
java -cp out klu.Main
```

---

## 📋 Login Credentials

### Student Login
| Roll Number   | Password (Name)        | Department |
|---------------|------------------------|------------|
| 2520030333    | ramagiri rishik rao    | CSE        |
| 2520030366    | nimma lokesh reddy     | CSE        |
| 2520030169    | v bharath kumar        | CSE        |
| Any valid roll| Your name (any text)   | Auto-detect|

**Roll Format:** `25200` + dept code (`30`=CSE, `40`=ECE, `80`=AI&DS, `90`=CS&IT) + 3 digits

### Admin Login
- **PIN:** `12353`
- **Name:** `ramagiri rishik rao`

---

## 🏛 DSA Concepts Applied

| CO  | Concept                  | Implementation                                | Location                    |
|-----|--------------------------|-----------------------------------------------|-----------------------------|
| CO1 | Binary Search O(log n)   | Find candidate by ID in sorted array          | `VotingService.findCandidateByIdBinarySearch()` |
| CO1 | Merge Sort O(n log n)    | Sort voter list alphabetically               | `VotingService.getAllVotersSorted()` |
| CO1 | Linear Search O(n)       | Find candidate by name (fallback)            | `VotingService.findCandidateByNameLinear()` |
| CO2 | Doubly Linked List       | Immutable vote ledger (forward+backward traversal) | `VoteLedger<T>` |
| CO3 | Stack (Array-based)      | Admin audit trail with LIFO access           | `AuditStack<T>` |
| CO3 | Circular Queue           | Login rate-limiting (last N attempts)        | `LoginThrottleQueue` |
| CO3 | Max-Heap Priority Queue  | Real-time candidate ranking by votes          | `CandidateHeap` |
| CO4 | HashMap with Chaining    | O(1) voter registry lookup/insert/delete     | `VoterHashMap<K,V>` |
| CO5 | Practical Application    | Full voting system with CRUD, admin panel     | `VotingService`, `Main` |
| CO6 | Java OOP + DSA           | Integrated program with models, services, UI  | All classes                 |

---

## 📁 Project Structure

```
KLUVoting/
├── src/klu/
│   ├── Main.java                    # Entry point, full terminal UI
│   ├── ds/
│   │   ├── VoterHashMap.java        # CO4: Custom HashMap with chaining
│   │   ├── VoteLedger.java          # CO2: Doubly linked list ledger
│   │   ├── CandidateHeap.java       # CO3: Max-heap for rankings
│   │   ├── AuditStack.java          # CO3: Array stack for audit log
│   │   └── LoginThrottleQueue.java  # CO3: Circular queue rate-limiter
│   ├── model/
│   │   ├── Voter.java               # Voter entity
│   │   ├── Candidate.java           # Candidate entity
│   │   └── VoteRecord.java          # Ledger entry
│   └── service/
│       ├── VotingService.java       # Core business logic + DSA
│       └── Display.java             # ANSI terminal UI helpers
├── out/                             # Compiled .class files
├── KLUVoting.jar                    # Runnable JAR
├── run.sh                           # Convenience run script
└── README.md                        # This file
```

---

## 🎯 Features

- **Student Portal:** Login with roll number + name, vote in 4 categories (one-time)
- **Admin Panel:** Voter management, live results, audit log, reset votes
- **Security:** Rate-limiting (Circular Queue), lockout on repeated failures
- **Live Results:** Max-heap extracts top-ranked candidates in O(log n)
- **Audit Trail:** Every action pushed to Stack (LIFO audit log)
- **Vote Ledger:** Immutable doubly-linked list — forward & backward traversal
- **DSA Demo Mode:** Interactive demos for Binary Search, Heap, HashMap, etc.

---

**Developed by Ramagiri Rishik Rao | K L University | 2025**
