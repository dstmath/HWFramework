package java.util.concurrent;

import java.util.NavigableMap;
import java.util.NavigableSet;

public interface ConcurrentNavigableMap<K, V> extends ConcurrentMap<K, V>, NavigableMap<K, V> {
    NavigableSet<K> descendingKeySet();

    ConcurrentNavigableMap<K, V> descendingMap();

    ConcurrentNavigableMap<K, V> headMap(K k);

    ConcurrentNavigableMap<K, V> headMap(K k, boolean z);

    NavigableSet<K> keySet();

    NavigableSet<K> navigableKeySet();

    ConcurrentNavigableMap<K, V> subMap(K k, K k2);

    ConcurrentNavigableMap<K, V> subMap(K k, boolean z, K k2, boolean z2);

    ConcurrentNavigableMap<K, V> tailMap(K k);

    ConcurrentNavigableMap<K, V> tailMap(K k, boolean z);
}
