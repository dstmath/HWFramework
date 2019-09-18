package java.util;

import java.util.Map;

public interface NavigableMap<K, V> extends SortedMap<K, V> {
    Map.Entry<K, V> ceilingEntry(K k);

    K ceilingKey(K k);

    NavigableSet<K> descendingKeySet();

    NavigableMap<K, V> descendingMap();

    Map.Entry<K, V> firstEntry();

    Map.Entry<K, V> floorEntry(K k);

    K floorKey(K k);

    NavigableMap<K, V> headMap(K k, boolean z);

    SortedMap<K, V> headMap(K k);

    Map.Entry<K, V> higherEntry(K k);

    K higherKey(K k);

    Map.Entry<K, V> lastEntry();

    Map.Entry<K, V> lowerEntry(K k);

    K lowerKey(K k);

    NavigableSet<K> navigableKeySet();

    Map.Entry<K, V> pollFirstEntry();

    Map.Entry<K, V> pollLastEntry();

    NavigableMap<K, V> subMap(K k, boolean z, K k2, boolean z2);

    SortedMap<K, V> subMap(K k, K k2);

    NavigableMap<K, V> tailMap(K k, boolean z);

    SortedMap<K, V> tailMap(K k);
}
