package th.pd.glry;

import java.util.LinkedList;
import java.util.List;

public class PivotCache<E> {

    private static class Entry<E> {

        private int key = 0;
        private E value = null;

        public int getKey() {
            return key;
        }

        public E getValue() {
            return value;
        }

        public Entry<E> setKey(int key) {
            this.key = key;
            return this;
        }

        public Entry<E> setValue(E value) {
            this.value = value;
            return this;
        }
    }

    public final int CAPACITY;
    public final int RADIUS;

    List<Entry<E>> cache;

    public PivotCache() {
        this(7, 0);
    }

    public PivotCache(int capcity, int pivotId) {
        CAPACITY = (capcity - 1) | 0x01;
        RADIUS = CAPACITY / 2;
        cache = new LinkedList<Entry<E>>();
        for (int i = 0; i < CAPACITY; ++i) {
            cache.add(new Entry<E>().setKey(i));
        }
        focus(pivotId);
    }

    public E get(int id) {
        Entry<E> entry = getEntry(id);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    private Entry<E> getEntry(int id) {
        int i = id - cache.get(0).getKey();
        if (i >= 0 && i < CAPACITY) {
            return cache.get(i);
        }
        return null;
    }

    public void focus(int pivot) {
        int start = cache.get(0).getKey();
        int newStart = pivot - RADIUS;
        if (start == newStart) {
            return;
        }

        if (newStart >= start - RADIUS * 2 && newStart <= start + RADIUS * 2) {
            // overlap
            while (start < newStart) {
                Entry<E> e = cache.remove(0);
                e.setKey(start++ + CAPACITY).setValue(null);
                cache.add(e);
            }
            while (start > newStart) {
                Entry<E> e = cache.remove(CAPACITY - 1);
                e.setKey(--start).setValue(null);
                cache.add(0, e);
            }
        } else {
            for (Entry<E> e : cache) {
                e.setKey(newStart++).setValue(null);
            }
        }
    }

    public void set(int id, E element) {
        getEntry(id).setValue(element);
    }
}
