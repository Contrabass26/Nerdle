package main.java.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OrderedMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>> {

    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();
    private final Comparator<Entry<K, V>> comparator;

    public OrderedMap(Comparator<Entry<K, V>> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return values.stream().anyMatch(v -> v.equals(value));
    }

    @Override
    public V get(Object key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(key)) {
                return values.get(i);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        keys.add(key);
        values.add(value);
        return value;
    }

    @Override
    public V remove(Object key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(key)) {
                keys.remove(i);
                V value = values.get(i);
                values.remove(i);
                return value;
            }
        }
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return new HashSet<>(keys);
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return new HashSet<>(values);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new HashSet<>();
        for (int i = 0; i < keys.size(); i++) {
            entries.add(Map.entry(keys.get(i), values.get(i)));
        }
        return entries;
    }

    public List<Entry<K, V>> sortedEntryList() {
        List<Entry<K, V>> entries = new ArrayList<>(entrySet());
        entries.sort(comparator);
        return entries;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map<?, ?> m))
            return false;
        if (m.size() != size())
            return false;
        try {
            for (Entry<K, V> e : entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (Entry<K, V> entry : entrySet())
            hashCode += entry.hashCode();
        return hashCode;
    }

    public void put(Entry<K, V> entry) {
        put(entry.getKey(), entry.getValue());
    }

    @NotNull
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i == keys.size();
            }

            @Override
            public Entry<K, V> next() {
                i++;
                return Map.entry(keys.get(i - 1), values.get(i - 1));
            }
        };
    }

    public static <T> Comparator<T> reverseComparator(Comparator<T> comparator) {
        return (t1, t2) -> comparator.compare(t2, t1);
    }
}
