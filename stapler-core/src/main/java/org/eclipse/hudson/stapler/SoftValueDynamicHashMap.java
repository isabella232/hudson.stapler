package org.eclipse.hudson.stapler;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A very simple implementation of a dynamic hash map that stores soft references to its values.
 * 
 * <p>To use, create a subclass that implements the <code>load</code> method.
 * This method is called by <code>get</code> whenever the underlying map has no value or the 
 * soft reference value has no value. <code>load</code> must not return null.
 * 
 * <p>Null values are not permitted.
 * 
 * @author Bob Foster
 *
 * @param <K>
 * @param <V>
 */
public abstract class SoftValueDynamicHashMap<K,V> implements Map<K,V> {
	private ConcurrentHashMap<K,SoftReference<V>> map = new ConcurrentHashMap<K,SoftReference<V>>();

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		SoftReference<V> softValue = map.get(key);
		V got = softValue == null ? null : softValue.get();
		if (got == null) {
			got = checkNull(load((K) key));
			map.put((K) key, new SoftReference<V>(got));
		}
		return got;
	}
	
	private V checkNull(V value) {
		if (value == null) {
			throw new NullPointerException("null values not supported");
		}
		return value;
	}
	
	public abstract V load(K key);

	@Override
	public V put(K key, V value) {
		checkNull(value);
		return deref(map.put(key, new SoftReference<V>(value)));
	}
	
	private V deref(SoftReference<V> softValue) {
		V value = softValue == null ? null : softValue.get();
		return value;
	}

	@Override
	public V remove(Object key) {
		return deref(map.remove(key));
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}
}
