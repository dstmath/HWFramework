package ohos.global.icu.impl;

import ohos.com.sun.org.apache.xerces.internal.parsers.XMLGrammarCachingConfiguration;

public class CalendarCache {
    public static long EMPTY = Long.MIN_VALUE;
    private static final int[] primes = {61, 127, 509, 1021, XMLGrammarCachingConfiguration.BIG_PRIME, 4093, 8191, 16381, 32749, 65521, 131071, 262139};
    private int arraySize = primes[this.pIndex];
    private long[] keys;
    private int pIndex = 0;
    private int size = 0;
    private int threshold;
    private long[] values;

    public CalendarCache() {
        int i = this.arraySize;
        this.threshold = (i * 3) / 4;
        this.keys = new long[i];
        this.values = new long[i];
        makeArrays(i);
    }

    private void makeArrays(int i) {
        this.keys = new long[i];
        this.values = new long[i];
        for (int i2 = 0; i2 < i; i2++) {
            this.values[i2] = EMPTY;
        }
        this.arraySize = i;
        this.threshold = (int) (((double) this.arraySize) * 0.75d);
        this.size = 0;
    }

    public synchronized long get(long j) {
        return this.values[findIndex(j)];
    }

    public synchronized void put(long j, long j2) {
        if (this.size >= this.threshold) {
            rehash();
        }
        int findIndex = findIndex(j);
        this.keys[findIndex] = j;
        this.values[findIndex] = j2;
        this.size++;
    }

    private final int findIndex(long j) {
        int hash = hash(j);
        int i = 0;
        while (this.values[hash] != EMPTY && this.keys[hash] != j) {
            if (i == 0) {
                i = hash2(j);
            }
            hash = (hash + i) % this.arraySize;
        }
        return hash;
    }

    private void rehash() {
        int i = this.arraySize;
        long[] jArr = this.keys;
        long[] jArr2 = this.values;
        int i2 = this.pIndex;
        int[] iArr = primes;
        if (i2 < iArr.length - 1) {
            int i3 = i2 + 1;
            this.pIndex = i3;
            this.arraySize = iArr[i3];
        } else {
            this.arraySize = (i * 2) + 1;
        }
        this.size = 0;
        makeArrays(this.arraySize);
        for (int i4 = 0; i4 < i; i4++) {
            if (jArr2[i4] != EMPTY) {
                put(jArr[i4], jArr2[i4]);
            }
        }
    }

    private final int hash(long j) {
        int i = this.arraySize;
        int i2 = (int) (((j * 15821) + 1) % ((long) i));
        return i2 < 0 ? i2 + i : i2;
    }

    private final int hash2(long j) {
        int i = this.arraySize;
        return (i - 2) - ((int) (j % ((long) (i - 2))));
    }
}
