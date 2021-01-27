package ohos.com.sun.org.apache.xerces.internal.util;

public class SymbolTable {
    protected static final int MAX_HASH_COLLISIONS = 40;
    protected static final int MULTIPLIERS_MASK = 31;
    protected static final int MULTIPLIERS_SIZE = 32;
    protected static final int TABLE_SIZE = 101;
    protected Entry[] fBuckets;
    protected final int fCollisionThreshold;
    protected transient int fCount;
    protected int[] fHashMultipliers;
    protected float fLoadFactor;
    protected int fTableSize;
    protected int fThreshold;

    public SymbolTable(int i, float f) {
        this.fBuckets = null;
        if (i < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + i);
        } else if (f <= 0.0f || Float.isNaN(f)) {
            throw new IllegalArgumentException("Illegal Load: " + f);
        } else {
            i = i == 0 ? 1 : i;
            this.fLoadFactor = f;
            this.fTableSize = i;
            int i2 = this.fTableSize;
            this.fBuckets = new Entry[i2];
            this.fThreshold = (int) (((float) i2) * f);
            this.fCollisionThreshold = (int) (f * 40.0f);
            this.fCount = 0;
        }
    }

    public SymbolTable(int i) {
        this(i, 0.75f);
    }

    public SymbolTable() {
        this(101, 0.75f);
    }

    public String addSymbol(String str) {
        int hash = hash(str) % this.fTableSize;
        int i = 0;
        for (Entry entry = this.fBuckets[hash]; entry != null; entry = entry.next) {
            if (entry.symbol.equals(str)) {
                return entry.symbol;
            }
            i++;
        }
        return addSymbol0(str, hash, i);
    }

    private String addSymbol0(String str, int i, int i2) {
        if (this.fCount >= this.fThreshold) {
            rehash();
            i = hash(str) % this.fTableSize;
        } else if (i2 >= this.fCollisionThreshold) {
            rebalance();
            i = hash(str) % this.fTableSize;
        }
        Entry entry = new Entry(str, this.fBuckets[i]);
        this.fBuckets[i] = entry;
        this.fCount++;
        return entry.symbol;
    }

    public String addSymbol(char[] cArr, int i, int i2) {
        int hash = hash(cArr, i, i2) % this.fTableSize;
        int i3 = 0;
        for (Entry entry = this.fBuckets[hash]; entry != null; entry = entry.next) {
            if (i2 == entry.characters.length) {
                for (int i4 = 0; i4 < i2; i4++) {
                    if (cArr[i + i4] == entry.characters[i4]) {
                    }
                }
                return entry.symbol;
            }
            i3++;
        }
        return addSymbol0(cArr, i, i2, hash, i3);
    }

    private String addSymbol0(char[] cArr, int i, int i2, int i3, int i4) {
        if (this.fCount >= this.fThreshold) {
            rehash();
            i3 = hash(cArr, i, i2) % this.fTableSize;
        } else if (i4 >= this.fCollisionThreshold) {
            rebalance();
            i3 = hash(cArr, i, i2) % this.fTableSize;
        }
        Entry entry = new Entry(cArr, i, i2, this.fBuckets[i3]);
        this.fBuckets[i3] = entry;
        this.fCount++;
        return entry.symbol;
    }

    public int hash(String str) {
        if (this.fHashMultipliers == null) {
            return str.hashCode() & Integer.MAX_VALUE;
        }
        return hash0(str);
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

    public int hash(char[] cArr, int i, int i2) {
        if (this.fHashMultipliers != null) {
            return hash0(cArr, i, i2);
        }
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            i3 = (i3 * 31) + cArr[i + i4];
        }
        return Integer.MAX_VALUE & i3;
    }

    private int hash0(char[] cArr, int i, int i2) {
        int[] iArr = this.fHashMultipliers;
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            i3 = (i3 * iArr[i4 & 31]) + cArr[i + i4];
        }
        return Integer.MAX_VALUE & i3;
    }

    /* access modifiers changed from: protected */
    public void rehash() {
        rehashCommon((this.fBuckets.length * 2) + 1);
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
        this.fThreshold = (int) (((float) i) * this.fLoadFactor);
        this.fBuckets = entryArr2;
        this.fTableSize = this.fBuckets.length;
        while (true) {
            int i2 = length - 1;
            if (length > 0) {
                Entry entry = entryArr[i2];
                while (entry != null) {
                    Entry entry2 = entry.next;
                    int hash = hash(entry.symbol) % i;
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

    public boolean containsSymbol(String str) {
        int hash = hash(str) % this.fTableSize;
        int length = str.length();
        Entry entry = this.fBuckets[hash];
        while (true) {
            if (entry == null) {
                return false;
            }
            if (length == entry.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (str.charAt(i) == entry.characters[i]) {
                    }
                }
                return true;
            }
            entry = entry.next;
        }
    }

    public boolean containsSymbol(char[] cArr, int i, int i2) {
        Entry entry = this.fBuckets[hash(cArr, i, i2) % this.fTableSize];
        while (true) {
            if (entry == null) {
                return false;
            }
            if (i2 == entry.characters.length) {
                for (int i3 = 0; i3 < i2; i3++) {
                    if (cArr[i + i3] == entry.characters[i3]) {
                    }
                }
                return true;
            }
            entry = entry.next;
        }
    }

    /* access modifiers changed from: protected */
    public static final class Entry {
        public final char[] characters;
        public Entry next;
        public final String symbol;

        public Entry(String str, Entry entry) {
            this.symbol = str.intern();
            this.characters = new char[str.length()];
            char[] cArr = this.characters;
            str.getChars(0, cArr.length, cArr, 0);
            this.next = entry;
        }

        public Entry(char[] cArr, int i, int i2, Entry entry) {
            this.characters = new char[i2];
            System.arraycopy(cArr, i, this.characters, 0, i2);
            this.symbol = new String(this.characters).intern();
            this.next = entry;
        }
    }
}
