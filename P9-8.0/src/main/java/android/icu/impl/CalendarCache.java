package android.icu.impl;

import dalvik.bytecode.Opcodes;

public class CalendarCache {
    public static long EMPTY = Long.MIN_VALUE;
    private static final int[] primes = new int[]{61, 127, 509, 1021, 2039, 4093, Opcodes.OP_SPUT_BYTE_JUMBO, 16381, 32749, 65521, 131071, 262139};
    private int arraySize = primes[this.pIndex];
    private long[] keys = new long[this.arraySize];
    private int pIndex = 0;
    private int size = 0;
    private int threshold = ((this.arraySize * 3) / 4);
    private long[] values = new long[this.arraySize];

    public CalendarCache() {
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
