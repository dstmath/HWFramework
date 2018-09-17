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

    private static final class Entry implements java.util.Map.Entry {
        private final Object key;
        private final Object value;

        /* synthetic */ Entry(Object key, Object value, Entry -this2) {
            this(key, value);
        }

        private Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof java.util.Map.Entry)) {
                return false;
            }
            java.util.Map.Entry entry = (java.util.Map.Entry) o;
            if (!getKey() != null ? entry.getKey() == null : getKey().equals(entry.getKey())) {
                return false;
            }
            if (getValue() == null) {
                return entry.getValue() == null;
            } else {
                return getValue().equals(entry.getValue());
            }
        }

        public int hashCode() {
            int i = 0;
            int hashCode = getKey() == null ? 0 : getKey().hashCode();
            if (getValue() != null) {
                i = getValue().hashCode();
            }
            return hashCode ^ i;
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }

        public Object getValue() {
            return this.value;
        }

        public Object getKey() {
            return this.key;
        }
    }

    private static final class Referenced {
        private final int hashCode;
        private final WeakReference reference;

        /* synthetic */ Referenced(Object key, ReferenceQueue queue, Referenced -this2) {
            this(key, queue);
        }

        private Referenced(Object referant) {
            this.reference = new WeakReference(referant);
            this.hashCode = referant.hashCode();
        }

        private Referenced(Object key, ReferenceQueue queue) {
            this.reference = new WeakKey(key, queue, this, null);
            this.hashCode = key.hashCode();
        }

        public int hashCode() {
            return this.hashCode;
        }

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
            boolean result = otherKeyValue == null;
            if (!result) {
                return result;
            }
            if (hashCode() == otherKey.hashCode()) {
                return true;
            }
            return false;
        }
    }

    private static final class WeakKey extends WeakReference {
        private final Referenced referenced;

        /* synthetic */ WeakKey(Object key, ReferenceQueue queue, Referenced referenced, WeakKey -this3) {
            this(key, queue, referenced);
        }

        private WeakKey(Object key, ReferenceQueue queue, Referenced referenced) {
            super(key, queue);
            this.referenced = referenced;
        }

        private Referenced getReferenced() {
            return this.referenced;
        }
    }

    public boolean containsKey(Object key) {
        return super.containsKey(new Referenced(key, null));
    }

    public Enumeration elements() {
        purge();
        return super.elements();
    }

    public Set entrySet() {
        purge();
        Set<java.util.Map.Entry> referencedEntries = super.entrySet();
        Set unreferencedEntries = new HashSet();
        for (java.util.Map.Entry entry : referencedEntries) {
            Object key = ((Referenced) entry.getKey()).getValue();
            Object value = entry.getValue();
            if (key != null) {
                unreferencedEntries.add(new Entry(key, value, null));
            }
        }
        return unreferencedEntries;
    }

    public Object get(Object key) {
        return super.get(new Referenced(key, null));
    }

    public Enumeration keys() {
        purge();
        final Enumeration enumer = super.keys();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return enumer.hasMoreElements();
            }

            public Object nextElement() {
                return ((Referenced) enumer.nextElement()).getValue();
            }
        };
    }

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

    public Object put(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        } else if (value == null) {
            throw new NullPointerException("Null values are not allowed");
        } else {
            int i = this.changeCount;
            this.changeCount = i + 1;
            if (i > 100) {
                purge();
                this.changeCount = 0;
            } else if (this.changeCount % 10 == 0) {
                purgeOne();
            }
            return super.put(new Referenced(key, this.queue, null), value);
        }
    }

    public void putAll(Map t) {
        if (t != null) {
            for (java.util.Map.Entry entry : t.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Collection values() {
        purge();
        return super.values();
    }

    public Object remove(Object key) {
        int i = this.changeCount;
        this.changeCount = i + 1;
        if (i > 100) {
            purge();
            this.changeCount = 0;
        } else if (this.changeCount % 10 == 0) {
            purgeOne();
        }
        return super.remove(new Referenced(key, null));
    }

    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }

    public int size() {
        purge();
        return super.size();
    }

    public String toString() {
        purge();
        return super.toString();
    }

    protected void rehash() {
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
}
