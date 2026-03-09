package klu.ds;

/**
 * CO3: Circular Queue for rate-limiting login attempts.
 * Tracks the last N login timestamps per roll number.
 */
public class LoginThrottleQueue {
    private long[] timestamps;
    private int front, rear, size, capacity;

    public LoginThrottleQueue(int capacity) {
        this.capacity = capacity;
        this.timestamps = new long[capacity];
        this.front = 0; this.rear = -1; this.size = 0;
    }

    public void enqueue(long ts) {
        rear = (rear + 1) % capacity;
        if (size == capacity) front = (front + 1) % capacity; // overwrite oldest
        else size++;
        timestamps[rear] = ts;
    }

    public long peek() { return size > 0 ? timestamps[front] : -1; }
    public int size()  { return size; }
    public boolean isFull() { return size == capacity; }

    /** True if more than `max` attempts in last `windowMs` milliseconds */
    public boolean isRateLimited(int max, long windowMs) {
        if (size < max) return false;
        long oldest = timestamps[front];
        return (System.currentTimeMillis() - oldest) < windowMs;
    }
}
