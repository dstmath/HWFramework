package java.util;

import java.io.Serializable;
import java.util.Map.Entry;

public abstract class AbstractMap<K, V> implements Map<K, V> {
    volatile transient Set<K> keySet;
    volatile transient Collection<V> values;

    public static class SimpleEntry<K, V> implements Entry<K, V>, Serializable {
        private static final long serialVersionUID = -8499721149061103585L;
        private final K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(Entry<? extends K, ? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) o;
            if (AbstractMap.eq(this.key, e.getKey())) {
                z = AbstractMap.eq(this.value, e.getValue());
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = this.key == null ? 0 : this.key.hashCode();
            if (this.value != null) {
                i = this.value.hashCode();
            }
            return hashCode ^ i;
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    public static class SimpleImmutableEntry<K, V> implements Entry<K, V>, Serializable {
        private static final long serialVersionUID = 7138329143949025153L;
        private final K key;
        private final V value;

        public SimpleImmutableEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleImmutableEntry(Entry<? extends K, ? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V v) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) o;
            if (AbstractMap.eq(this.key, e.getKey())) {
                z = AbstractMap.eq(this.value, e.getValue());
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = this.key == null ? 0 : this.key.hashCode();
            if (this.value != null) {
                i = this.value.hashCode();
            }
            return hashCode ^ i;
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    public abstract Set<Entry<K, V>> entrySet();

    protected AbstractMap() {
        this.keySet = null;
        this.values = null;
    }

    public int size() {
        return entrySet().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsValue(Object value) {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (value == null) {
            while (i.hasNext()) {
                if (((Entry) i.next()).getValue() == null) {
                    return true;
                }
            }
        }
        while (i.hasNext()) {
            if (value.equals(((Entry) i.next()).getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(Object key) {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (key == null) {
            while (i.hasNext()) {
                if (((Entry) i.next()).getKey() == null) {
                    return true;
                }
            }
        }
        while (i.hasNext()) {
            if (key.equals(((Entry) i.next()).getKey())) {
                return true;
            }
        }
        return false;
    }

    public V get(Object key) {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        Entry<K, V> e;
        if (key == null) {
            while (i.hasNext()) {
                e = (Entry) i.next();
                if (e.getKey() == null) {
                    return e.getValue();
                }
            }
        }
        while (i.hasNext()) {
            e = (Entry) i.next();
            if (key.equals(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    public V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    public V remove(Object key) {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        Entry entry = null;
        Entry<K, V> e;
        if (key == null) {
            while (entry == null && i.hasNext()) {
                e = (Entry) i.next();
                if (e.getKey() == null) {
                    entry = e;
                }
            }
        } else {
            while (entry == null && i.hasNext()) {
                e = (Entry) i.next();
                if (key.equals(e.getKey())) {
                    entry = e;
                }
            }
        }
        if (entry == null) {
            return null;
        }
        V oldValue = entry.getValue();
        i.remove();
        return oldValue;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public void clear() {
        entrySet().clear();
    }

    public Set<K> keySet() {
        if (this.keySet == null) {
            this.keySet = new AbstractSet<K>() {
                public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        private Iterator<Entry<K, V>> i;

                        {
                            this.i = AbstractMap.this.entrySet().iterator();
                        }

                        public boolean hasNext() {
                            return this.i.hasNext();
                        }

                        public K next() {
                            return ((Entry) this.i.next()).getKey();
                        }

                        public void remove() {
                            this.i.remove();
                        }
                    };
                }

                public int size() {
                    return AbstractMap.this.size();
                }

                public boolean isEmpty() {
                    return AbstractMap.this.isEmpty();
                }

                public void clear() {
                    AbstractMap.this.clear();
                }

                public boolean contains(Object k) {
                    return AbstractMap.this.containsKey(k);
                }
            };
        }
        return this.keySet;
    }

    public Collection<V> values() {
        if (this.values == null) {
            this.values = new AbstractCollection<V>() {
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        private Iterator<Entry<K, V>> i;

                        {
                            this.i = AbstractMap.this.entrySet().iterator();
                        }

                        public boolean hasNext() {
                            return this.i.hasNext();
                        }

                        public V next() {
                            return ((Entry) this.i.next()).getValue();
                        }

                        public void remove() {
                            this.i.remove();
                        }
                    };
                }

                public int size() {
                    return AbstractMap.this.size();
                }

                public boolean isEmpty() {
                    return AbstractMap.this.isEmpty();
                }

                public void clear() {
                    AbstractMap.this.clear();
                }

                public boolean contains(Object v) {
                    return AbstractMap.this.containsValue(v);
                }
            };
        }
        return this.values;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map)) {
            return false;
        }
        Map<K, V> m = (Map) o;
        if (m.size() != size()) {
            return false;
        }
        try {
            for (Entry<K, V> e : entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    boolean containsKey;
                    if (m.get(key) == null) {
                        containsKey = m.containsKey(key);
                    } else {
                        containsKey = false;
                    }
                    if (!containsKey) {
                        return false;
                    }
                } else if (!value.equals(m.get(key))) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e2) {
            return false;
        } catch (NullPointerException e3) {
            return false;
        }
    }

    public int hashCode() {
        int h = 0;
        for (Entry hashCode : entrySet()) {
            h += hashCode.hashCode();
        }
        return h;
    }

    public String toString() {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            Entry<K, V> e = (Entry) i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            if (key == this) {
                key = "(this Map)";
            }
            sb.append(key);
            sb.append('=');
            if (value == this) {
                value = "(this Map)";
            }
            sb.append(value);
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    protected Object clone() throws CloneNotSupportedException {
        AbstractMap<K, V> result = (AbstractMap) super.clone();
        result.keySet = null;
        result.values = null;
        return result;
    }

    private static boolean eq(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }
}
