package com.utils.bean;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@SuppressWarnings("rawtypes")
class WeakFastHashMap extends HashMap {
	private static final long serialVersionUID = 42L;
	private Map map = null;

	private boolean fast = false;

	public WeakFastHashMap() {
		this.map = createMap();
	}

	public WeakFastHashMap(int capacity) {
		this.map = createMap(capacity);
	}

	public WeakFastHashMap(int capacity, float factor) {
		this.map = createMap(capacity, factor);
	}

	public WeakFastHashMap(Map map) {
		this.map = createMap(map);
	}

	public boolean getFast() {
		return this.fast;
	}

	public void setFast(boolean fast) {
		this.fast = fast;
	}

	public Object get(Object key) {
		if (this.fast) {
			return this.map.get(key);
		}
		synchronized (this.map) {
			return this.map.get(key);
		}
	}

	public int size() {
		if (this.fast) {
			return this.map.size();
		}
		synchronized (this.map) {
			return this.map.size();
		}
	}

	public boolean isEmpty() {
		if (this.fast) {
			return this.map.isEmpty();
		}
		synchronized (this.map) {
			return this.map.isEmpty();
		}
	}

	public boolean containsKey(Object key) {
		if (this.fast) {
			return this.map.containsKey(key);
		}
		synchronized (this.map) {
			return this.map.containsKey(key);
		}
	}

	public boolean containsValue(Object value) {
		if (this.fast) {
			return this.map.containsValue(value);
		}
		synchronized (this.map) {
			return this.map.containsValue(value);
		}
	}

	@SuppressWarnings("unchecked")
	public Object put(Object key, Object value) {
		if (this.fast) {
			synchronized (this) {
				Map temp = cloneMap(this.map);
				Object result = temp.put(key, value);
				this.map = temp;
				return result;
			}
		}
		synchronized (this.map) {
			return this.map.put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	public void putAll(Map in) {
		if (this.fast)
			synchronized (this) {
				Map temp = cloneMap(this.map);
				temp.putAll(in);
				this.map = temp;
			}
		else
			synchronized (this.map) {
				this.map.putAll(in);
			}
	}

	public Object remove(Object key) {
		if (this.fast) {
			synchronized (this) {
				Map temp = cloneMap(this.map);
				Object result = temp.remove(key);
				this.map = temp;
				return result;
			}
		}
		synchronized (this.map) {
			return this.map.remove(key);
		}
	}

	public void clear() {
		if (this.fast)
			synchronized (this) {
				this.map = createMap();
			}
		else
			synchronized (this.map) {
				this.map.clear();
			}
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Map)) {
			return false;
		}
		Map mo = (Map) o;

		if (this.fast) {
			if (mo.size() != this.map.size()) {
				return false;
			}
			Iterator i = this.map.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				Object key = e.getKey();
				Object value = e.getValue();
				if (value == null) {
					if ((mo.get(key) != null) || (!mo.containsKey(key))) {
						return false;
					}
				} else if (!value.equals(mo.get(key))) {
					return false;
				}
			}

			return true;
		}

		synchronized (this.map) {
			if (mo.size() != this.map.size()) {
				return false;
			}
			Iterator i = this.map.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				Object key = e.getKey();
				Object value = e.getValue();
				if (value == null) {
					if ((mo.get(key) != null) || (!mo.containsKey(key))) {
						return false;
					}
				} else if (!value.equals(mo.get(key))) {
					return false;
				}
			}

			return true;
		}
	}

	public int hashCode() {
		if (this.fast) {
			int h = 0;
			Iterator i = this.map.entrySet().iterator();
			while (i.hasNext()) {
				h += i.next().hashCode();
			}
			return h;
		}
		synchronized (this.map) {
			int h = 0;
			Iterator i = this.map.entrySet().iterator();
			while (i.hasNext()) {
				h += i.next().hashCode();
			}
			return h;
		}
	}

	public Object clone() {
		WeakFastHashMap results = null;
		if (this.fast)
			results = new WeakFastHashMap(this.map);
		else {
			synchronized (this.map) {
				results = new WeakFastHashMap(this.map);
			}
		}
		results.setFast(getFast());
		return results;
	}

	public Set entrySet() {
		// return new EntrySet(null);
		return new EntrySet();
	}

	public Set keySet() {
		// return new KeySet(null);
		return new KeySet();
	}

	public Collection values() {
		// return new Values(null);
		return new Values();
	}

	protected Map createMap() {
		return new WeakHashMap();
	}

	protected Map createMap(int capacity) {
		return new WeakHashMap(capacity);
	}

	protected Map createMap(int capacity, float factor) {
		return new WeakHashMap(capacity, factor);
	}

	@SuppressWarnings("unchecked")
	protected Map createMap(Map map) {
		return new WeakHashMap(map);
	}

	protected Map cloneMap(Map map) {
		return createMap(map);
	}

	private class EntrySet extends WeakFastHashMap.CollectionView implements Set {
		private EntrySet() {
			super();
		}

		protected Collection get(Map map) {
			return map.entrySet();
		}

		protected Object iteratorNext(Map.Entry entry) {
			return entry;
		}
	}

	private class Values extends WeakFastHashMap.CollectionView {
		private Values() {
			super();
		}

		protected Collection get(Map map) {
			return map.values();
		}

		protected Object iteratorNext(Map.Entry entry) {
			return entry.getValue();
		}
	}

	private class KeySet extends WeakFastHashMap.CollectionView implements Set {
		private KeySet() {
			super();
		}

		protected Collection get(Map map) {
			return map.keySet();
		}

		protected Object iteratorNext(Map.Entry entry) {
			return entry.getKey();
		}
	}

	private abstract class CollectionView implements Collection {
		public CollectionView() {
		}

		protected abstract Collection get(Map paramMap);

		protected abstract Object iteratorNext(Map.Entry paramEntry);

		public void clear() {
			if (WeakFastHashMap.this.fast)
				synchronized (WeakFastHashMap.this) {
					WeakFastHashMap.this.map = WeakFastHashMap.this.createMap();
				}
			else
				synchronized (WeakFastHashMap.this.map) {
					get(WeakFastHashMap.this.map).clear();
				}
		}

		public boolean remove(Object o) {
			if (WeakFastHashMap.this.fast) {
				synchronized (WeakFastHashMap.this) {
					Map temp = WeakFastHashMap.this.cloneMap(WeakFastHashMap.this.map);
					boolean r = get(temp).remove(o);
					WeakFastHashMap.this.map = temp;
					return r;
				}
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).remove(o);
			}
		}

		@SuppressWarnings("unchecked")
		public boolean removeAll(Collection o) {
			if (WeakFastHashMap.this.fast) {
				synchronized (WeakFastHashMap.this) {
					Map temp = WeakFastHashMap.this.cloneMap(WeakFastHashMap.this.map);
					boolean r = get(temp).removeAll(o);
					WeakFastHashMap.this.map = temp;
					return r;
				}
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).removeAll(o);
			}
		}

		@SuppressWarnings("unchecked")
		public boolean retainAll(Collection o) {
			if (WeakFastHashMap.this.fast) {
				synchronized (WeakFastHashMap.this) {
					Map temp = WeakFastHashMap.this.cloneMap(WeakFastHashMap.this.map);
					boolean r = get(temp).retainAll(o);
					WeakFastHashMap.this.map = temp;
					return r;
				}
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).retainAll(o);
			}
		}

		public int size() {
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).size();
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).size();
			}
		}

		public boolean isEmpty() {
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).isEmpty();
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).isEmpty();
			}
		}

		public boolean contains(Object o) {
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).contains(o);
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).contains(o);
			}
		}

		@SuppressWarnings("unchecked")
		public boolean containsAll(Collection o) {
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).containsAll(o);
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).containsAll(o);
			}
		}

		@SuppressWarnings("unchecked")
		public Object[] toArray(Object[] o) {
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).toArray(o);
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).toArray(o);
			}
		}

		public Object[] toArray() {
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).toArray();
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).toArray();
			}
		}

		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).equals(o);
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).equals(o);
			}
		}

		public int hashCode() {
			if (WeakFastHashMap.this.fast) {
				return get(WeakFastHashMap.this.map).hashCode();
			}
			synchronized (WeakFastHashMap.this.map) {
				return get(WeakFastHashMap.this.map).hashCode();
			}
		}

		public boolean add(Object o) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection c) {
			throw new UnsupportedOperationException();
		}

		public Iterator iterator() {
			return new CollectionViewIterator();
		}

		private class CollectionViewIterator implements Iterator {
			private Map expected;
			private Map.Entry lastReturned = null;
			private Iterator iterator;

			public CollectionViewIterator() {
				this.expected = WeakFastHashMap.this.map;
				this.iterator = this.expected.entrySet().iterator();
			}

			public boolean hasNext() {
				if (this.expected != WeakFastHashMap.this.map) {
					throw new ConcurrentModificationException();
				}
				return this.iterator.hasNext();
			}

			public Object next() {
				if (this.expected != WeakFastHashMap.this.map) {
					throw new ConcurrentModificationException();
				}
				this.lastReturned = ((Map.Entry) this.iterator.next());
				return WeakFastHashMap.CollectionView.this.iteratorNext(this.lastReturned);
			}

			public void remove() {
				if (this.lastReturned == null) {
					throw new IllegalStateException();
				}
				if (WeakFastHashMap.this.fast) {
					synchronized (WeakFastHashMap.this) {
						if (this.expected != WeakFastHashMap.this.map) {
							throw new ConcurrentModificationException();
						}
						WeakFastHashMap.this.remove(this.lastReturned.getKey());
						this.lastReturned = null;
						this.expected = WeakFastHashMap.this.map;
					}
				} else {
					this.iterator.remove();
					this.lastReturned = null;
				}
			}
		}
	}
}