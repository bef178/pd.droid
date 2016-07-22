package th.pd.common;

import java.util.LinkedList;
import java.util.List;

public class SlidingCache<E> {

    private List<E> cache;
    private int start = 0;

    public SlidingCache(int capcity) {
        cache = new LinkedList<>();
        for (int i = 0; i < capcity; ++i) {
            cache.add(null);
        }
    }

    public int capacity() {
        return cache.size();
    }

    public E get(int index) {
        return cache.get(index - this.start);
    }

    public boolean has(int index) {
        return index >= start && index < start + capacity();
    }

    public synchronized void move(int offset) {
        if (offset == 0) {
            return;
        }

        int capacity = capacity();

        if (offset >= capacity || offset <= -capacity) {
            cache.clear();
            for (int i = 0; i < capacity; ++i) {
                cache.add(null);
            }
            this.start += offset;
            return;
        }

        while (this.start < start) {
            cache.remove(0);
            cache.add(null);
        }
        while (this.start > start) {
            cache.remove(capacity - 1);
            cache.add(0, null);
        }
    }

    public void moveTo(int start) {
        move(start - this.start);
    }

    public synchronized void set(int index, E e) {
        cache.set(index - this.start, e);
    }
}
