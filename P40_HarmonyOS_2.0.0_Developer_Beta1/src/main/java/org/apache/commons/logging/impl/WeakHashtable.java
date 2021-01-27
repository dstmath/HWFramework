package org.apache.commons.logging.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

@Deprecated
public final class WeakHashtable extends Hashtable {
    private static final int MAX_CHANGES_BEFORE_PURGE = 100;
    private static final int PARTIAL_PURGE_COUNT = 10;
    private int changeCount = 0;
    private ReferenceQueue queue = new ReferenceQueue();

    @Override // java.util.Hashtable, java.util.Map
    public boolean containsKey(Object key) {
        return super.containsKey(new Referenced(key));
    }

    @Override // java.util.Hashtable, java.util.Dictionary
    public Enumeration elements() {
        purge();
        return super.elements();
    }

    @Override // java.util.Hashtable, java.util.Map
    public Set entrySet() {
        purge();
        Set<Map.Entry> referencedEntries = super.entrySet();
        Set unreferencedEntries = new HashSet();
        for (Map.Entry entry : referencedEntries) {
            Object key = ((Referenced) entry.getKey()).getValue();
            Object value = entry.getValue();
            if (key != null) {
                unreferencedEntries.add(new Entry(key, value));
            }
        }
        return unreferencedEntries;
    }

    @Override // java.util.Hashtable, java.util.Map, java.util.Dictionary
    public Object get(Object key) {
        return super.get(new Referenced(key));
    }

    @Override // java.util.Hashtable, java.util.Dictionary
    public Enumeration keys() {
        purge();
        final Enumeration enumer = super.keys();
        return new Enumeration() {
            /* class org.apache.commons.logging.impl.WeakHashtable.AnonymousClass1 */

            @Override // java.util.Enumeration
            public boolean hasMoreElements() {
                return enumer.hasMoreElements();
            }

            @Override // java.util.Enumeration
            public Object nextElement() {
                return ((Referenced) enumer.nextElement()).getValue();
            }
        };
    }

    @Override // java.util.Hashtable, java.util.Map
    public Set keySet() {
        purge();
        Set<Referenced> referencedKeys = super.keySet();
        Set unreferencedKeys = new HashSet();
        for (Referenced referenceKey : referencedKeys) {
            Object keyValue = referenceKey.getValue();
            if (keyValue != null) {
                unreferencedKeys.add(keyValue);
            }
        }
        return unreferencedKeys;
    }

    @Override // java.util.Hashtable, java.util.Map, java.util.Dictionary
    public Object put(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        } else if (value != null) {
            int i = this.changeCount;
            this.changeCount = i + 1;
            if (i > 100) {
                purge();
                this.changeCount = 0;
            } else if (this.changeCount % 10 == 0) {
                purgeOne();
            }
            return super.put(new Referenced(key, this.queue), value);
        } else {
            throw new NullPointerException("Null values are not allowed");
        }
    }

    @Override // java.util.Hashtable, java.util.Map
    public void putAll(Map t) {
        if (t != null) {
            for (Map.Entry entry : t.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override // java.util.Hashtable, java.util.Map
    public Collection values() {
        purge();
        return super.values();
    }

    @Override // java.util.Hashtable, java.util.Map, java.util.Dictionary
    public Object remove(Object key) {
        int i = this.changeCount;
        this.changeCount = i + 1;
        if (i > 100) {
            purge();
            this.changeCount = 0;
        } else if (this.changeCount % 10 == 0) {
            purgeOne();
        }
        return super.remove(new Referenced(key));
    }

    @Override // java.util.Hashtable, java.util.Map, java.util.Dictionary
    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }

    @Override // java.util.Hashtable, java.util.Map, java.util.Dictionary
    public int size() {
        purge();
        return super.size();
    }

    @Override // java.util.Hashtable, java.lang.Object
    public String toString() {
        purge();
        return super.toString();
    }

    /* access modifiers changed from: protected */
    @Override // java.util.Hashtable
    public void rehash() {
        purge();
        super.rehash();
    }

    private void purge() {
        synchronized (this.queue) {
            while (true) {
                WeakKey key = (WeakKey) this.queue.poll();
                if (key != null) {
                    super.remove(key.getReferenced());
                }
            }
        }
    }

    private void purgeOne() {
        synchronized (this.queue) {
            WeakKey key = (WeakKey) this.queue.poll();
            if (key != null) {
                super.remove(key.getReferenced());
            }
        }
    }

    private static final class Entry implements Map.Entry {
        private final Object key;
        private final Object value;

        private Entry(Object key2, Object value2) {
            this.key = key2;
            this.value = value2;
        }

        @Override // java.util.Map.Entry, java.lang.Object
        public boolean equals(Object o) {
            boolean result;
            if (o == null || !(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry) o;
            if (getKey() != null ? getKey().equals(entry.getKey()) : entry.getKey() == null) {
                if (getValue() != null ? getValue().equals(entry.getValue()) : entry.getValue() == null) {
                    result = true;
                    return result;
                }
            }
            result = false;
            return result;
        }

        @Override // java.util.Map.Entry, java.lang.Object
        public int hashCode() {
            int i = 0;
            int hashCode = getKey() == null ? 0 : getKey().hashCode();
            if (getValue() != null) {
                i = getValue().hashCode();
            }
            return hashCode ^ i;
        }

        @Override // java.util.Map.Entry
        public Object setValue(Object value2) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }

        @Override // java.util.Map.Entry
        public Object getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public Object getKey() {
            return this.key;
        }
    }

    /* access modifiers changed from: private */
    public static final class Referenced {
        private final int hashCode;
        private final WeakReference reference;

        private Referenced(Object referant) {
            this.reference = new WeakReference(referant);
            this.hashCode = referant.hashCode();
        }

        private Referenced(Object key, ReferenceQueue queue) {
            this.reference = new WeakKey(key, queue, this);
            this.hashCode = key.hashCode();
        }

        public int hashCode() {
            return this.hashCode;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Object getValue() {
            return this.reference.get();
        }

        public boolean equals(Object o) {
            if (!(o instanceof Referenced)) {
                return false;
            }
            Referenced otherKey = (Referenced) o;
            Object thisKeyValue = getValue();
            Object otherKeyValue = otherKey.getValue();
            if (thisKeyValue != null) {
                return thisKeyValue.equals(otherKeyValue);
            }
            boolean result = false;
            boolean result2 = otherKeyValue == null;
            if (!result2) {
                return result2;
            }
            if (hashCode() == otherKey.hashCode()) {
                result = true;
            }
            return result;
        }
    }

    /* access modifiers changed from: private */
    public static final class WeakKey extends WeakReference {
        private final Referenced referenced;

        private WeakKey(Object key, ReferenceQueue queue, Referenced referenced2) {
            super(key, queue);
            this.referenced = referenced2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Referenced getReferenced() {
            return this.referenced;
        }
    }
}
