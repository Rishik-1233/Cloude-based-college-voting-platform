package klu.ds;

/**
 * CO3: Array-based Stack for Admin Audit Trail.
 * Each admin action is pushed. Supports peek/pop for undo.
 */
public class AuditStack<T> {
    private Object[] data;
    private int top;
    private int capacity;

    public AuditStack(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.top = -1;
    }

    public void push(T item) {
        if (top == capacity - 1) {
            // Grow the stack
            Object[] grown = new Object[capacity * 2];
            System.arraycopy(data, 0, grown, 0, capacity);
            data = grown;
            capacity *= 2;
        }
        data[++top] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new RuntimeException("Stack underflow");
        return (T) data[top--];
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) return null;
        return (T) data[top];
    }

    public boolean isEmpty() { return top == -1; }
    public int size()        { return top + 1; }

    @SuppressWarnings("unchecked")
    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>();
        for (int i = top; i >= 0; i--) list.add((T) data[i]);
        return list;
    }
}
