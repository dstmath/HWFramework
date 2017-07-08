package android.icu.impl;

public class CalendarCache {
    public static long EMPTY;
    private static final int[] primes = null;
    private int arraySize;
    private long[] keys;
    private int pIndex;
    private int size;
    private int threshold;
    private long[] values;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CalendarCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CalendarCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CalendarCache.<clinit>():void");
    }

    public CalendarCache() {
        this.pIndex = 0;
        this.size = 0;
        this.arraySize = primes[this.pIndex];
        this.threshold = (this.arraySize * 3) / 4;
        this.keys = new long[this.arraySize];
        this.values = new long[this.arraySize];
        makeArrays(this.arraySize);
    }

    private void makeArrays(int newSize) {
        this.keys = new long[newSize];
        this.values = new long[newSize];
        for (int i = 0; i < newSize; i++) {
            this.values[i] = EMPTY;
        }
        this.arraySize = newSize;
        this.threshold = (int) (((double) this.arraySize) * 0.75d);
        this.size = 0;
    }

    public synchronized long get(long key) {
        return this.values[findIndex(key)];
    }

    public synchronized void put(long key, long value) {
        if (this.size >= this.threshold) {
            rehash();
        }
        int index = findIndex(key);
        this.keys[index] = key;
        this.values[index] = value;
        this.size++;
    }

    private final int findIndex(long key) {
        int index = hash(key);
        int delta = 0;
        while (this.values[index] != EMPTY && this.keys[index] != key) {
            if (delta == 0) {
                delta = hash2(key);
            }
            index = (index + delta) % this.arraySize;
        }
        return index;
    }

    private void rehash() {
        int oldSize = this.arraySize;
        long[] oldKeys = this.keys;
        long[] oldValues = this.values;
        if (this.pIndex < primes.length - 1) {
            int[] iArr = primes;
            int i = this.pIndex + 1;
            this.pIndex = i;
            this.arraySize = iArr[i];
        } else {
            this.arraySize = (this.arraySize * 2) + 1;
        }
        this.size = 0;
        makeArrays(this.arraySize);
        for (int i2 = 0; i2 < oldSize; i2++) {
            if (oldValues[i2] != EMPTY) {
                put(oldKeys[i2], oldValues[i2]);
            }
        }
    }

    private final int hash(long key) {
        int h = (int) (((15821 * key) + 1) % ((long) this.arraySize));
        if (h < 0) {
            return h + this.arraySize;
        }
        return h;
    }

    private final int hash2(long key) {
        return (this.arraySize - 2) - ((int) (key % ((long) (this.arraySize - 2))));
    }
}
