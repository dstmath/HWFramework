package ohos.com.sun.org.apache.xerces.internal.util;

public class SymbolHash {
    protected static final int MAX_HASH_COLLISIONS = 40;
    protected static final int MULTIPLIERS_MASK = 31;
    protected static final int MULTIPLIERS_SIZE = 32;
    protected static final int TABLE_SIZE = 101;
    protected Entry[] fBuckets;
    protected int[] fHashMultipliers;
    protected int fNum;
    protected int fTableSize;

    public SymbolHash() {
        this(101);
    }

    public SymbolHash(int i) {
        this.fNum = 0;
        this.fTableSize = i;
        this.fBuckets = new Entry[this.fTableSize];
    }

    public void put(Object obj, Object obj2) {
        int hash = hash(obj);
        int i = hash % this.fTableSize;
        int i2 = 0;
        for (Entry entry = this.fBuckets[i]; entry != null; entry = entry.next) {
            if (obj.equals(entry.key)) {
                entry.value = obj2;
                return;
            } else {
                i2++;
            }
        }
        if (this.fNum >= this.fTableSize) {
            rehash();
            i = hash % this.fTableSize;
        } else if (i2 >= 40 && (obj instanceof String)) {
            rebalance();
            i = hash(obj) % this.fTableSize;
        }
        this.fBuckets[i] = new Entry(obj, obj2, this.fBuckets[i]);
        this.fNum++;
    }

    public Object get(Object obj) {
        Entry search = search(obj, hash(obj) % this.fTableSize);
        if (search != null) {
            return search.value;
        }
        return null;
    }

    public int getLength() {
        return this.fNum;
    }

    public int getValues(Object[] objArr, int i) {
        int i2 = 0;
        for (int i3 = 0; i3 < this.fTableSize && i2 < this.fNum; i3++) {
            for (Entry entry = this.fBuckets[i3]; entry != null; entry = entry.next) {
                objArr[i + i2] = entry.value;
                i2++;
            }
        }
        return this.fNum;
    }

    public Object[] getEntries() {
        Object[] objArr = new Object[(this.fNum << 1)];
        int i = 0;
        for (int i2 = 0; i2 < this.fTableSize && i < (this.fNum << 1); i2++) {
            for (Entry entry = this.fBuckets[i2]; entry != null; entry = entry.next) {
                objArr[i] = entry.key;
                int i3 = i + 1;
                objArr[i3] = entry.value;
                i = i3 + 1;
            }
        }
        return objArr;
    }

    public SymbolHash makeClone() {
        SymbolHash symbolHash = new SymbolHash(this.fTableSize);
        symbolHash.fNum = this.fNum;
        int[] iArr = this.fHashMultipliers;
        symbolHash.fHashMultipliers = iArr != null ? (int[]) iArr.clone() : null;
        for (int i = 0; i < this.fTableSize; i++) {
            Entry[] entryArr = this.fBuckets;
            if (entryArr[i] != null) {
                symbolHash.fBuckets[i] = entryArr[i].makeClone();
            }
        }
        return symbolHash;
    }

    public void clear() {
        for (int i = 0; i < this.fTableSize; i++) {
            this.fBuckets[i] = null;
        }
        this.fNum = 0;
        this.fHashMultipliers = null;
    }

    /* access modifiers changed from: protected */
    public Entry search(Object obj, int i) {
        for (Entry entry = this.fBuckets[i]; entry != null; entry = entry.next) {
            if (obj.equals(entry.key)) {
                return entry;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int hash(Object obj) {
        if (this.fHashMultipliers == null || !(obj instanceof String)) {
            return obj.hashCode() & Integer.MAX_VALUE;
        }
        return hash0((String) obj);
    }

    private int hash0(String str) {
        int length = str.length();
        int[] iArr = this.fHashMultipliers;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            i = (i * iArr[i2 & 31]) + str.charAt(i2);
        }
        return Integer.MAX_VALUE & i;
    }

    /* access modifiers changed from: protected */
    public void rehash() {
        rehashCommon((this.fBuckets.length << 1) + 1);
    }

    /* access modifiers changed from: protected */
    public void rebalance() {
        if (this.fHashMultipliers == null) {
            this.fHashMultipliers = new int[32];
        }
        PrimeNumberSequenceGenerator.generateSequence(this.fHashMultipliers);
        rehashCommon(this.fBuckets.length);
    }

    private void rehashCommon(int i) {
        Entry[] entryArr = this.fBuckets;
        int length = entryArr.length;
        Entry[] entryArr2 = new Entry[i];
        this.fBuckets = entryArr2;
        this.fTableSize = this.fBuckets.length;
        while (true) {
            int i2 = length - 1;
            if (length > 0) {
                Entry entry = entryArr[i2];
                while (entry != null) {
                    Entry entry2 = entry.next;
                    int hash = hash(entry.key) % i;
                    entry.next = entryArr2[hash];
                    entryArr2[hash] = entry;
                    entry = entry2;
                }
                length = i2;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public static final class Entry {
        public Object key;
        public Entry next;
        public Object value;

        public Entry() {
            this.key = null;
            this.value = null;
            this.next = null;
        }

        public Entry(Object obj, Object obj2, Entry entry) {
            this.key = obj;
            this.value = obj2;
            this.next = entry;
        }

        public Entry makeClone() {
            Entry entry = new Entry();
            entry.key = this.key;
            entry.value = this.value;
            Entry entry2 = this.next;
            if (entry2 != null) {
                entry.next = entry2.makeClone();
            }
            return entry;
        }
    }
}
