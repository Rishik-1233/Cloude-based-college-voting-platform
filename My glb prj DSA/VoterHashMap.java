package klu.ds;

/**
 * CO4: Custom Hash Table with Chaining for fast voter lookup.
 * Supports O(1) average insert, delete, search.
 */
public class VoterHashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 64;
    private static final double LOAD_FACTOR = 0.75;

    @SuppressWarnings("unchecked")
    private Node<K, V>[] table;
    private int size;
    private int capacity;

    static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    @SuppressWarnings("unchecked")
    public VoterHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.table = new Node[capacity];
        this.size = 0;
    }

    private int hash(K key) {
        int h = key.hashCode();
        // Spread bits (like Java's HashMap)
        h = h ^ (h >>> 16);
        return Math.abs(h % capacity);
    }

    public void put(K key, V value) {
        if ((double) size / capacity >= LOAD_FACTOR) rehash();
        int idx = hash(key);
        Node<K, V> head = table[idx];
        while (head != null) {
            if (head.key.equals(key)) { head.value = value; return; }
            head = head.next;
        }
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = table[idx];
        table[idx] = newNode;
        size++;
    }

    public V get(K key) {
        int idx = hash(key);
        Node<K, V> cur = table[idx];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    public boolean containsKey(K key) { return get(key) != null; }

    public void remove(K key) {
        int idx = hash(key);
        Node<K, V> cur = table[idx], prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) table[idx] = cur.next;
                else prev.next = cur.next;
                size--;
                return;
            }
            prev = cur; cur = cur.next;
        }
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        capacity *= 2;
        Node<K, V>[] old = table;
        table = new Node[capacity];
        size = 0;
        for (Node<K, V> head : old)
            for (Node<K, V> cur = head; cur != null; cur = cur.next)
                put(cur.key, cur.value);
    }

    public int size() { return size; }

    public java.util.List<K> keys() {
        java.util.List<K> list = new java.util.ArrayList<>();
        for (Node<K, V> head : table)
            for (Node<K, V> cur = head; cur != null; cur = cur.next)
                list.add(cur.key);
        return list;
    }

    public java.util.List<V> values() {
        java.util.List<V> list = new java.util.ArrayList<>();
        for (Node<K, V> head : table)
            for (Node<K, V> cur = head; cur != null; cur = cur.next)
                list.add(cur.value);
        return list;
    }
}
