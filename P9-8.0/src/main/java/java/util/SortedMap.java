package java.util;

import java.util.Map.Entry;

public interface SortedMap<K, V> extends Map<K, V> {
    Comparator<? super K> comparator();

    Set<Entry<K, V>> entrySet();

    K firstKey();

    SortedMap<K, V> headMap(K k);

    Set<K> keySet();

    K lastKey();

    SortedMap<K, V> subMap(K k, K k2);

    SortedMap<K, V> tailMap(K k);

    Collection<V> values();
}
