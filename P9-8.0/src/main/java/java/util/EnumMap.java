package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class EnumMap<K extends Enum<K>, V> extends AbstractMap<K, V> implements Serializable, Cloneable {
    private static final Object NULL = new Object() {
        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "java.util.EnumMap.NULL";
        }
    };
    private static final Enum<?>[] ZERO_LENGTH_ENUM_ARRAY = new Enum[0];
    private static final long serialVersionUID = 458661240069192865L;
    private transient Set<Entry<K, V>> entrySet;
    private final Class<K> keyType;
    private transient K[] keyUniverse;
    private transient int size = 0;
    private transient Object[] vals;

    private abstract class EnumMapIterator<T> implements Iterator<T> {
        int index;
        int lastReturnedIndex;

        /* synthetic */ EnumMapIterator(EnumMap this$0, EnumMapIterator -this1) {
            this();
        }

        private EnumMapIterator() {
            this.index = 0;
            this.lastReturnedIndex = -1;
        }

        public boolean hasNext() {
            while (this.index < EnumMap.this.vals.length && EnumMap.this.vals[this.index] == null) {
                this.index++;
            }
            return this.index != EnumMap.this.vals.length;
        }

        public void remove() {
            checkLastReturnedIndex();
            if (EnumMap.this.vals[this.lastReturnedIndex] != null) {
                EnumMap.this.vals[this.lastReturnedIndex] = null;
                EnumMap enumMap = EnumMap.this;
                enumMap.size = enumMap.size - 1;
            }
            this.lastReturnedIndex = -1;
        }

        private void checkLastReturnedIndex() {
            if (this.lastReturnedIndex < 0) {
                throw new IllegalStateException();
            }
        }
    }

    private class EntryIterator extends EnumMapIterator<java.util.Map.Entry<K, V>> {
        private java.util.EnumMap$EntryIterator.Entry lastReturnedEntry;

        private class Entry implements java.util.Map.Entry<K, V> {
            private int index;

            /* synthetic */ Entry(EntryIterator this$1, int index, Entry -this2) {
                this(index);
            }

            private Entry(int index) {
                this.index = index;
            }

            public K getKey() {
                checkIndexForEntryUse();
                return EnumMap.this.keyUniverse[this.index];
            }

            public V getValue() {
                checkIndexForEntryUse();
                return EnumMap.this.unmaskNull(EnumMap.this.vals[this.index]);
            }

            public V setValue(V value) {
                checkIndexForEntryUse();
                V oldValue = EnumMap.this.unmaskNull(EnumMap.this.vals[this.index]);
                EnumMap.this.vals[this.index] = EnumMap.this.maskNull(value);
                return oldValue;
            }

            public boolean equals(Object o) {
                boolean z = true;
                boolean z2 = false;
                if (this.index < 0) {
                    if (o != this) {
                        z = false;
                    }
                    return z;
                } else if (!(o instanceof java.util.Map.Entry)) {
                    return false;
                } else {
                    java.util.Map.Entry<?, ?> e = (java.util.Map.Entry) o;
                    V ourValue = EnumMap.this.unmaskNull(EnumMap.this.vals[this.index]);
                    V hisValue = e.getValue();
                    if (e.getKey() == EnumMap.this.keyUniverse[this.index]) {
                        if (ourValue == hisValue) {
                            z2 = true;
                        } else if (ourValue != null) {
                            z2 = ourValue.lambda$-java_util_function_Predicate_4628(hisValue);
                        }
                    }
                    return z2;
                }
            }

            public int hashCode() {
                if (this.index < 0) {
                    return super.hashCode();
                }
                return EnumMap.this.entryHashCode(this.index);
            }

            public String toString() {
                if (this.index < 0) {
                    return super.toString();
                }
                return EnumMap.this.keyUniverse[this.index] + "=" + EnumMap.this.unmaskNull(EnumMap.this.vals[this.index]);
            }

            private void checkIndexForEntryUse() {
                if (this.index < 0) {
                    throw new IllegalStateException("Entry was removed");
                }
            }
        }

        /* synthetic */ EntryIterator(EnumMap this$0, EntryIterator -this1) {
            this();
        }

        private EntryIterator() {
            super(EnumMap.this, null);
        }

        public java.util.Map.Entry<K, V> next() {
            if (hasNext()) {
                int i = this.index;
                this.index = i + 1;
                this.lastReturnedEntry = new Entry(this, i, null);
                return this.lastReturnedEntry;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            this.lastReturnedIndex = this.lastReturnedEntry == null ? -1 : this.lastReturnedEntry.index;
            super.remove();
            this.lastReturnedEntry.index = this.lastReturnedIndex;
            this.lastReturnedEntry = null;
        }
    }

    private class EntrySet extends AbstractSet<Entry<K, V>> {
        /* synthetic */ EntrySet(EnumMap this$0, EntrySet -this1) {
            this();
        }

        private EntrySet() {
        }

        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator(EnumMap.this, null);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry) o;
            return EnumMap.this.containsMapping(entry.getKey(), entry.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry) o;
            return EnumMap.this.removeMapping(entry.getKey(), entry.getValue());
        }

        public int size() {
            return EnumMap.this.size;
        }

        public void clear() {
            EnumMap.this.clear();
        }

        public Object[] toArray() {
            return fillEntryArray(new Object[EnumMap.this.size]);
        }

        public <T> T[] toArray(T[] a) {
            int size = size();
            if (a.length < size) {
                a = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            if (a.length > size) {
                a[size] = null;
            }
            return fillEntryArray(a);
        }

        private Object[] fillEntryArray(Object[] a) {
            int j = 0;
            for (int i = 0; i < EnumMap.this.vals.length; i++) {
                if (EnumMap.this.vals[i] != null) {
                    int j2 = j + 1;
                    a[j] = new SimpleEntry(EnumMap.this.keyUniverse[i], EnumMap.this.unmaskNull(EnumMap.this.vals[i]));
                    j = j2;
                }
            }
            return a;
        }
    }

    private class KeyIterator extends EnumMapIterator<K> {
        /* synthetic */ KeyIterator(EnumMap this$0, KeyIterator -this1) {
            this();
        }

        private KeyIterator() {
            super(EnumMap.this, null);
        }

        public K next() {
            if (hasNext()) {
                int i = this.index;
                this.index = i + 1;
                this.lastReturnedIndex = i;
                return EnumMap.this.keyUniverse[this.lastReturnedIndex];
            }
            throw new NoSuchElementException();
        }
    }

    private class KeySet extends AbstractSet<K> {
        /* synthetic */ KeySet(EnumMap this$0, KeySet -this1) {
            this();
        }

        private KeySet() {
        }

        public Iterator<K> iterator() {
            return new KeyIterator(EnumMap.this, null);
        }

        public int size() {
            return EnumMap.this.size;
        }

        public boolean contains(Object o) {
            return EnumMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            int oldSize = EnumMap.this.size;
            EnumMap.this.remove(o);
            return EnumMap.this.size != oldSize;
        }

        public void clear() {
            EnumMap.this.clear();
        }
    }

    private class ValueIterator extends EnumMapIterator<V> {
        /* synthetic */ ValueIterator(EnumMap this$0, ValueIterator -this1) {
            this();
        }

        private ValueIterator() {
            super(EnumMap.this, null);
        }

        public V next() {
            if (hasNext()) {
                int i = this.index;
                this.index = i + 1;
                this.lastReturnedIndex = i;
                return EnumMap.this.unmaskNull(EnumMap.this.vals[this.lastReturnedIndex]);
            }
            throw new NoSuchElementException();
        }
    }

    private class Values extends AbstractCollection<V> {
        /* synthetic */ Values(EnumMap this$0, Values -this1) {
            this();
        }

        private Values() {
        }

        public Iterator<V> iterator() {
            return new ValueIterator(EnumMap.this, null);
        }

        public int size() {
            return EnumMap.this.size;
        }

        public boolean contains(Object o) {
            return EnumMap.this.containsValue(o);
        }

        public boolean remove(Object o) {
            o = EnumMap.this.maskNull(o);
            for (int i = 0; i < EnumMap.this.vals.length; i++) {
                if (o.lambda$-java_util_function_Predicate_4628(EnumMap.this.vals[i])) {
                    EnumMap.this.vals[i] = null;
                    EnumMap enumMap = EnumMap.this;
                    enumMap.size = enumMap.size - 1;
                    return true;
                }
            }
            return false;
        }

        public void clear() {
            EnumMap.this.clear();
        }
    }

    private Object maskNull(Object value) {
        return value == null ? NULL : value;
    }

    private V unmaskNull(Object value) {
        return value == NULL ? null : value;
    }

    public EnumMap(Class<K> keyType) {
        this.keyType = keyType;
        this.keyUniverse = getKeyUniverse(keyType);
        this.vals = new Object[this.keyUniverse.length];
    }

    public EnumMap(EnumMap<K, ? extends V> m) {
        this.keyType = m.keyType;
        this.keyUniverse = m.keyUniverse;
        this.vals = (Object[]) m.vals.clone();
        this.size = m.size;
    }

    public EnumMap(Map<K, ? extends V> m) {
        if (m instanceof EnumMap) {
            EnumMap<K, ? extends V> em = (EnumMap) m;
            this.keyType = em.keyType;
            this.keyUniverse = em.keyUniverse;
            this.vals = (Object[]) em.vals.clone();
            this.size = em.size;
        } else if (m.isEmpty()) {
            throw new IllegalArgumentException("Specified map is empty");
        } else {
            this.keyType = ((Enum) m.keySet().iterator().next()).getDeclaringClass();
            this.keyUniverse = getKeyUniverse(this.keyType);
            this.vals = new Object[this.keyUniverse.length];
            putAll(m);
        }
    }

    public int size() {
        return this.size;
    }

    public boolean containsValue(Object value) {
        value = maskNull(value);
        for (Object val : this.vals) {
            if (value.lambda$-java_util_function_Predicate_4628(val)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(Object key) {
        return isValidKey(key) && this.vals[((Enum) key).ordinal()] != null;
    }

    private boolean containsMapping(Object key, Object value) {
        if (isValidKey(key)) {
            return maskNull(value).lambda$-java_util_function_Predicate_4628(this.vals[((Enum) key).ordinal()]);
        }
        return false;
    }

    public V get(Object key) {
        return isValidKey(key) ? unmaskNull(this.vals[((Enum) key).ordinal()]) : null;
    }

    public V put(K key, V value) {
        typeCheck(key);
        int index = key.ordinal();
        Object oldValue = this.vals[index];
        this.vals[index] = maskNull(value);
        if (oldValue == null) {
            this.size++;
        }
        return unmaskNull(oldValue);
    }

    public V remove(Object key) {
        if (!isValidKey(key)) {
            return null;
        }
        int index = ((Enum) key).ordinal();
        Object oldValue = this.vals[index];
        this.vals[index] = null;
        if (oldValue != null) {
            this.size--;
        }
        return unmaskNull(oldValue);
    }

    private boolean removeMapping(Object key, Object value) {
        if (!isValidKey(key)) {
            return false;
        }
        int index = ((Enum) key).ordinal();
        if (!maskNull(value).lambda$-java_util_function_Predicate_4628(this.vals[index])) {
            return false;
        }
        this.vals[index] = null;
        this.size--;
        return true;
    }

    private boolean isValidKey(Object key) {
        boolean z = true;
        if (key == null) {
            return false;
        }
        Class<?> keyClass = key.getClass();
        if (!(keyClass == this.keyType || keyClass.getSuperclass() == this.keyType)) {
            z = false;
        }
        return z;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        if (m instanceof EnumMap) {
            EnumMap<?, ?> em = (EnumMap) m;
            if (em.keyType == this.keyType) {
                for (int i = 0; i < this.keyUniverse.length; i++) {
                    Object emValue = em.vals[i];
                    if (emValue != null) {
                        if (this.vals[i] == null) {
                            this.size++;
                        }
                        this.vals[i] = emValue;
                    }
                }
            } else if (!em.isEmpty()) {
                throw new ClassCastException(em.keyType + " != " + this.keyType);
            } else {
                return;
            }
        }
        super.putAll(m);
    }

    public void clear() {
        Arrays.fill(this.vals, null);
        this.size = 0;
    }

    public Set<K> keySet() {
        Set<K> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        ks = new KeySet(this, null);
        this.keySet = ks;
        return ks;
    }

    public Collection<V> values() {
        Collection<V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        vs = new Values(this, null);
        this.values = vs;
        return vs;
    }

    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        Set<Entry<K, V>> entrySet = new EntrySet(this, null);
        this.entrySet = entrySet;
        return entrySet;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EnumMap) {
            return equals((EnumMap) o);
        }
        if (!(o instanceof Map)) {
            return false;
        }
        Map<?, ?> m = (Map) o;
        if (this.size != m.size()) {
            return false;
        }
        for (int i = 0; i < this.keyUniverse.length; i++) {
            if (this.vals[i] != null) {
                K key = this.keyUniverse[i];
                V value = unmaskNull(this.vals[i]);
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
                } else if (!value.lambda$-java_util_function_Predicate_4628(m.get(key))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean equals(EnumMap<?, ?> em) {
        boolean z = true;
        if (em.keyType != this.keyType) {
            if (!(this.size == 0 && em.size == 0)) {
                z = false;
            }
            return z;
        }
        for (int i = 0; i < this.keyUniverse.length; i++) {
            Object ourValue = this.vals[i];
            Object hisValue = em.vals[i];
            if (hisValue != ourValue && (hisValue == null || (hisValue.lambda$-java_util_function_Predicate_4628(ourValue) ^ 1) != 0)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int h = 0;
        for (int i = 0; i < this.keyUniverse.length; i++) {
            if (this.vals[i] != null) {
                h += entryHashCode(i);
            }
        }
        return h;
    }

    private int entryHashCode(int index) {
        return this.keyUniverse[index].hashCode() ^ this.vals[index].hashCode();
    }

    public EnumMap<K, V> clone() {
        try {
            EnumMap<K, V> result = (EnumMap) super.clone();
            result.vals = (Object[]) result.vals.clone();
            result.entrySet = null;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private void typeCheck(K key) {
        Object keyClass = key.getClass();
        if (keyClass != this.keyType && keyClass.getSuperclass() != this.keyType) {
            throw new ClassCastException(keyClass + " != " + this.keyType);
        }
    }

    private static <K extends Enum<K>> K[] getKeyUniverse(Class<K> keyType) {
        return JavaLangAccess.getEnumConstantsShared(keyType);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.size);
        int entriesToBeWritten = this.size;
        int i = 0;
        while (entriesToBeWritten > 0) {
            if (this.vals[i] != null) {
                s.writeObject(this.keyUniverse[i]);
                s.writeObject(unmaskNull(this.vals[i]));
                entriesToBeWritten--;
            }
            i++;
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.keyUniverse = getKeyUniverse(this.keyType);
        this.vals = new Object[this.keyUniverse.length];
        int size = s.readInt();
        for (int i = 0; i < size; i++) {
            put((Enum) s.readObject(), s.readObject());
        }
    }
}
