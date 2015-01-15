package th.pd;

import java.util.LinkedList;
import java.util.List;

public class Cache<E> {

	private static class Entry<E> {
		private int key = 0;
		private E value = null;

		public int getK() {
			return key;
		}

		public E getV() {
			return value;
		}

		public Entry<E> setK(int k) {
			this.key = k;
			return this;
		}

		public Entry<E> setV(E v) {
			this.value = v;
			return this;
		}
	}

	private static final int DEFAULT_CAPACITY = 15;
	private static final int DEFAULT_RADIUS = 3;

	public final int CAPACITY;
	public final int RADIUS;

	List<Entry<E>> cache;

	public Cache() {
		this(DEFAULT_CAPACITY, DEFAULT_RADIUS);
	}

	public Cache(int capcity, int radius) {
		CAPACITY = capcity | 0x01;
		RADIUS = radius | 0x01;
		refresh(0);
	}

	public E get(int id) {
		Entry<E> entry = getEntry(id);
		if (entry != null) {
			return entry.getV();
		}
		return null;
	}

	public int getCount() {
		return cache.size();
	}

	private Entry<E> getEntry(int id) {
		int count = getCount();
		if (count > 0) {
			int i = id - cache.get(0).getK();
			if (i >= 0 && i < count) {
				return cache.get(i);
			}
		}
		return null;
	}

	private void refresh(int id) {
		id -= CAPACITY / 2;
		if (cache == null) {
			cache = new LinkedList<Entry<E>>();
			for (int i = 0; i < CAPACITY; ++i) {
				cache.add(new Entry<E>().setK(id++));
			}
		} else {
			for (Entry<E> entry : cache) {
				entry.setK(id++).setV(null);
			}
		}
	}

	public boolean set(int id, E element) {
		Entry<E> entry = getEntry(id);
		if (entry != null) {
			entry.setV(element);
			return true;
		}
		return false;
	}

	// update the center of cache as pivot
	public void update(int id, E element) {
		Entry<E> entry = getEntry(id);
		if (entry == null) {
			refresh(id);
			set(id, element);
		} else {
			// partial in range
			int newStart = id - RADIUS;
			while (cache.get(0).getK() < newStart) {
				Entry<E> e = cache.remove(0);
				e.setK(cache.get(cache.size() - 1).getK() + 1).setV(null);
				cache.add(e);
			}
			int newEnd = id + RADIUS;
			while (cache.get(cache.size() - 1).getK() > newEnd) {
				Entry<E> e = cache.remove(cache.size() - 1);
				e.setK(cache.get(0).getK() - 1).setV(null);
				cache.add(0, e);
			}
		}
	}
}
