package klu.ds;

/**
 * CO3: Max-Heap Priority Queue for candidate vote ranking.
 * Candidate with highest votes has highest priority.
 * Supports O(log n) insert, O(log n) extractMax.
 */
public class CandidateHeap {
    private int[][] heap;  // [candidateId, voteCount]
    private String[] names;
    private int size;
    private int capacity;

    public CandidateHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new int[capacity][2];
        this.names = new String[capacity];
        this.size = 0;
    }

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i)   { return 2 * i + 1; }
    private int right(int i)  { return 2 * i + 2; }

    public void insert(int id, String name, int votes) {
        if (size == capacity) throw new RuntimeException("Heap overflow");
        heap[size][0] = id;
        heap[size][1] = votes;
        names[size] = name;
        siftUp(size++);
    }

    private void siftUp(int i) {
        while (i > 0 && heap[i][1] > heap[parent(i)][1]) {
            swap(i, parent(i));
            i = parent(i);
        }
    }

    private void siftDown(int i) {
        int max = i;
        if (left(i) < size && heap[left(i)][1] > heap[max][1])  max = left(i);
        if (right(i) < size && heap[right(i)][1] > heap[max][1]) max = right(i);
        if (max != i) { swap(i, max); siftDown(max); }
    }

    private void swap(int a, int b) {
        int[] tmp = heap[a]; heap[a] = heap[b]; heap[b] = tmp;
        String t = names[a]; names[a] = names[b]; names[b] = t;
    }

    public int[] extractMax() {
        if (size == 0) return null;
        int[] top = heap[0].clone();
        heap[0] = heap[--size];
        names[0] = names[size];
        if (size > 0) siftDown(0);
        return top;  // [id, votes]
    }

    public String peekName() { return size > 0 ? names[0] : null; }
    public int peekVotes()   { return size > 0 ? heap[0][1] : 0; }
    public boolean isEmpty() { return size == 0; }
    public int size()        { return size; }

    /** Return sorted ranking (highest to lowest) — heap sort */
    public java.util.List<int[]> getSortedRanking(String[] allNames) {
        // Clone heap state
        int[][] hCopy = new int[size][2];
        String[] nCopy = new String[size];
        int sCopy = size;
        for (int i = 0; i < size; i++) { hCopy[i] = heap[i].clone(); nCopy[i] = names[i]; }

        java.util.List<int[]> result = new java.util.ArrayList<>();
        while (size > 0) result.add(extractMax());
        // Restore
        heap = hCopy; names = nCopy; size = sCopy;
        return result;
    }
}
