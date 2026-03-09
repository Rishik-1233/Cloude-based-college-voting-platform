package klu.ds;

/**
 * CO2: Doubly Linked List used as an immutable Vote Ledger.
 * Each vote cast is appended (tamper-proof chain).
 * Supports traverse, reverse-traverse, and search.
 */
public class VoteLedger<T> {
    private Node<T> head, tail;
    private int size;

    static class Node<T> {
        T data;
        Node<T> prev, next;
        Node(T data) { this.data = data; }
    }

    public void append(T data) {
        Node<T> n = new Node<>(data);
        if (tail == null) { head = tail = n; }
        else { n.prev = tail; tail.next = n; tail = n; }
        size++;
    }

    public void traverseForward(java.util.function.Consumer<T> action) {
        Node<T> cur = head;
        while (cur != null) { action.accept(cur.data); cur = cur.next; }
    }

    public void traverseBackward(java.util.function.Consumer<T> action) {
        Node<T> cur = tail;
        while (cur != null) { action.accept(cur.data); cur = cur.prev; }
    }

    public T search(java.util.function.Predicate<T> pred) {
        Node<T> cur = head;
        while (cur != null) { if (pred.test(cur.data)) return cur.data; cur = cur.next; }
        return null;
    }

    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>();
        traverseForward(list::add);
        return list;
    }

    public int size() { return size; }
    public T getHead() { return head != null ? head.data : null; }
    public T getTail() { return tail != null ? tail.data : null; }
}
