package android_maps_conflict_avoidance.com.google.common.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

public class IntMap {
    private Hashtable higher;
    private Object[] lower;
    private int lowerCount;
    private int maxKey;
    private int maxLowerKey;

    public class KeyIterator {
        private int currentKey = Integer.MIN_VALUE;
        private Enumeration higherKeyEnumerator = null;
        private int oneAheadIndex = 0;

        public boolean hasNext() {
            if (this.currentKey != Integer.MIN_VALUE) {
                return true;
            }
            if (this.oneAheadIndex <= IntMap.this.maxLowerKey) {
                while (this.oneAheadIndex <= IntMap.this.maxLowerKey) {
                    if (IntMap.this.lower[this.oneAheadIndex] == null) {
                        this.oneAheadIndex++;
                    } else {
                        int i = this.oneAheadIndex;
                        this.oneAheadIndex = i + 1;
                        this.currentKey = i;
                        return true;
                    }
                }
            }
            if (IntMap.this.higher != null) {
                if (this.higherKeyEnumerator == null) {
                    this.higherKeyEnumerator = IntMap.this.higher.keys();
                }
                if (this.higherKeyEnumerator.hasMoreElements()) {
                    this.currentKey = ((Integer) this.higherKeyEnumerator.nextElement()).intValue();
                    return true;
                }
            }
            return false;
        }

        public int next() {
            if (hasNext()) {
                int key = this.currentKey;
                this.currentKey = Integer.MIN_VALUE;
                return key;
            }
            throw new NoSuchElementException();
        }
    }

    public IntMap() {
        this(16);
    }

    IntMap(int initialLowerBufferSize) {
        int lowerBufferSize = 16;
        if (initialLowerBufferSize > 0) {
            lowerBufferSize = Math.min(initialLowerBufferSize, 128);
        }
        this.lower = new Object[lowerBufferSize];
        this.lowerCount = 0;
        this.maxKey = Integer.MIN_VALUE;
        this.maxLowerKey = Integer.MIN_VALUE;
    }

    public KeyIterator keys() {
        return new KeyIterator();
    }

    public int size() {
        return this.higher != null ? this.lowerCount + this.higher.size() : this.lowerCount;
    }

    public void clear() {
        for (int i = 0; i < this.lower.length; i++) {
            this.lower[i] = null;
        }
        if (this.higher != null) {
            this.higher.clear();
        }
        this.maxKey = Integer.MIN_VALUE;
        this.maxLowerKey = Integer.MIN_VALUE;
        this.lowerCount = 0;
    }

    public Object get(int key) {
        if (key <= this.maxLowerKey && key >= 0) {
            return this.lower[key];
        }
        if (key > this.maxKey || this.higher == null) {
            return null;
        }
        return this.higher.get(Primitives.toInteger(key));
    }

    public void put(int key, Object value) {
        if (value != null) {
            if (key > this.maxKey) {
                this.maxKey = key;
            }
            if ((key >= 0 && key < this.lower.length) || expandLowerIfNecessary(key)) {
                if (key > this.maxLowerKey) {
                    this.maxLowerKey = key;
                    this.lowerCount++;
                } else if (this.lower[key] == null) {
                    this.lowerCount++;
                }
                this.lower[key] = value;
            } else {
                if (this.higher == null) {
                    this.higher = new Hashtable();
                }
                this.higher.put(Primitives.toInteger(key), value);
            }
            return;
        }
        remove(key);
    }

    public Object remove(int key) {
        Object deleted = null;
        if (key >= 0 && key < this.lower.length) {
            deleted = this.lower[key];
            if (deleted != null) {
                this.lowerCount--;
            }
            this.lower[key] = null;
        } else if (this.higher != null) {
            return this.higher.remove(Primitives.toInteger(key));
        }
        return deleted;
    }

    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < this.lower.length; i++) {
            Object value = this.lower[i];
            if (value != null) {
                hashCode = ((hashCode * 31) + value.hashCode()) + i;
            }
        }
        return this.higher != null ? hashCode + this.higher.size() : hashCode;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (object == null || !(object instanceof IntMap)) {
            return false;
        }
        IntMap peer = (IntMap) object;
        if (size() != peer.size()) {
            return false;
        }
        if (!(compareLowerBuffer(this.lower, peer.lower) && compareHashtable(this.higher, peer.higher))) {
            z = false;
        }
        return z;
    }

    private boolean compareLowerBuffer(Object[] lower1, Object[] lower2) {
        int min = Math.min(lower1.length, lower2.length);
        int i = 0;
        while (i < min) {
            if ((lower1[i] == null && lower2[i] != null) || (lower1[i] != null && !lower1[i].equals(lower2[i]))) {
                return false;
            }
            i++;
        }
        if (lower1.length > lower2.length) {
            for (i = min; i < lower1.length; i++) {
                if (lower1[i] != null) {
                    return false;
                }
            }
        } else if (lower1.length < lower2.length) {
            for (i = min; i < lower2.length; i++) {
                if (lower2[i] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:3:0x0006, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean compareHashtable(Hashtable h1, Hashtable h2) {
        if (h1 == h2) {
            return true;
        }
        if (h1 == null || h2 == null || h1.size() != h2.size()) {
            return false;
        }
        Enumeration h1Keys = h1.keys();
        while (h1Keys.hasMoreElements()) {
            Object key = h1Keys.nextElement();
            if (!h1.get(key).equals(h2.get(key))) {
                return false;
            }
        }
        return true;
    }

    private boolean expandLowerIfNecessary(int key) {
        if (key >= 128 || key < this.lower.length || key <= 0) {
            return false;
        }
        int size = this.lower.length;
        do {
            size <<= 1;
        } while (size <= key);
        Object[] newLower = new Object[Math.min(size, 128)];
        System.arraycopy(this.lower, 0, newLower, 0, this.lower.length);
        this.lower = newLower;
        return true;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("IntMap{lower:");
        for (int i = 0; i < this.lower.length; i++) {
            if (this.lower[i] != null) {
                buffer.append(i);
                buffer.append("=>");
                buffer.append(this.lower[i]);
                buffer.append(", ");
            }
        }
        buffer.append(", higher:" + this.higher + "}");
        return buffer.toString();
    }
}
