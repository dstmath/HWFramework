package sun.misc;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class SoftCache extends AbstractMap implements Map {
    private Set entrySet;
    private Map hash;
    private ReferenceQueue queue;

    private class Entry implements java.util.Map.Entry {
        private java.util.Map.Entry ent;
        private Object value;

        Entry(java.util.Map.Entry ent, Object value) {
            this.ent = ent;
            this.value = value;
        }

        public Object getKey() {
            return this.ent.getKey();
        }

        public Object getValue() {
            return this.value;
        }

        public Object setValue(Object value) {
            return this.ent.setValue(ValueCell.create(this.ent.getKey(), value, SoftCache.this.queue));
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof java.util.Map.Entry)) {
                return false;
            }
            java.util.Map.Entry e = (java.util.Map.Entry) o;
            if (SoftCache.valEquals(this.ent.getKey(), e.getKey())) {
                z = SoftCache.valEquals(this.value, e.getValue());
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            Object k = getKey();
            int hashCode = k == null ? 0 : k.hashCode();
            if (this.value != null) {
                i = this.value.hashCode();
            }
            return hashCode ^ i;
        }
    }

    private class EntrySet extends AbstractSet {
        Set hashEntries;

        private EntrySet() {
            this.hashEntries = SoftCache.this.hash.entrySet();
        }

        public Iterator iterator() {
            return new Iterator() {
                Iterator hashIterator;
                Entry next;

                {
                    this.hashIterator = EntrySet.this.hashEntries.iterator();
                    this.next = null;
                }

                public boolean hasNext() {
                    while (this.hashIterator.hasNext()) {
                        java.util.Map.Entry ent = (java.util.Map.Entry) this.hashIterator.next();
                        ValueCell vc = (ValueCell) ent.getValue();
                        Object v = null;
                        if (vc != null) {
                            v = vc.get();
                            if (v != null) {
                            }
                        }
                        this.next = new Entry(ent, v);
                        return true;
                    }
                    return false;
                }

                public Object next() {
                    if (this.next != null || hasNext()) {
                        Entry e = this.next;
                        this.next = null;
                        return e;
                    }
                    throw new NoSuchElementException();
                }

                public void remove() {
                    this.hashIterator.remove();
                }
            };
        }

        public boolean isEmpty() {
            return !iterator().hasNext();
        }

        public int size() {
            int j = 0;
            Iterator i = iterator();
            while (i.hasNext()) {
                j++;
                i.next();
            }
            return j;
        }

        public boolean remove(Object o) {
            SoftCache.this.processQueue();
            if (o instanceof Entry) {
                return this.hashEntries.remove(((Entry) o).ent);
            }
            return false;
        }
    }

    private static class ValueCell extends SoftReference {
        private static Object INVALID_KEY;
        private static int dropped;
        private Object key;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.SoftCache.ValueCell.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.SoftCache.ValueCell.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.misc.SoftCache.ValueCell.<clinit>():void");
        }

        private ValueCell(Object key, Object value, ReferenceQueue queue) {
            super(value, queue);
            this.key = key;
        }

        private static ValueCell create(Object key, Object value, ReferenceQueue queue) {
            if (value == null) {
                return null;
            }
            return new ValueCell(key, value, queue);
        }

        private static Object strip(Object val, boolean drop) {
            if (val == null) {
                return null;
            }
            ValueCell vc = (ValueCell) val;
            Object o = vc.get();
            if (drop) {
                vc.drop();
            }
            return o;
        }

        private boolean isValid() {
            return this.key != INVALID_KEY;
        }

        private void drop() {
            super.clear();
            this.key = INVALID_KEY;
            dropped++;
        }
    }

    private void processQueue() {
        while (true) {
            ValueCell vc = (ValueCell) this.queue.poll();
            if (vc == null) {
                return;
            }
            if (vc.isValid()) {
                this.hash.remove(vc.key);
            } else {
                ValueCell.dropped = ValueCell.dropped - 1;
            }
        }
    }

    public SoftCache(int initialCapacity, float loadFactor) {
        this.queue = new ReferenceQueue();
        this.entrySet = null;
        this.hash = new HashMap(initialCapacity, loadFactor);
    }

    public SoftCache(int initialCapacity) {
        this.queue = new ReferenceQueue();
        this.entrySet = null;
        this.hash = new HashMap(initialCapacity);
    }

    public SoftCache() {
        this.queue = new ReferenceQueue();
        this.entrySet = null;
        this.hash = new HashMap();
    }

    public int size() {
        return entrySet().size();
    }

    public boolean isEmpty() {
        return entrySet().isEmpty();
    }

    public boolean containsKey(Object key) {
        return ValueCell.strip(this.hash.get(key), false) != null;
    }

    protected Object fill(Object key) {
        return null;
    }

    public Object get(Object key) {
        processQueue();
        Object v = this.hash.get(key);
        if (v == null) {
            v = fill(key);
            if (v != null) {
                this.hash.put(key, ValueCell.create(key, v, this.queue));
                return v;
            }
        }
        return ValueCell.strip(v, false);
    }

    public Object put(Object key, Object value) {
        processQueue();
        return ValueCell.strip(this.hash.put(key, ValueCell.create(key, value, this.queue)), true);
    }

    public Object remove(Object key) {
        processQueue();
        return ValueCell.strip(this.hash.remove(key), true);
    }

    public void clear() {
        processQueue();
        this.hash.clear();
    }

    private static boolean valEquals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    public Set entrySet() {
        if (this.entrySet == null) {
            this.entrySet = new EntrySet();
        }
        return this.entrySet;
    }
}
