package com.huawei.zxing.pdf417.decoder.ec;

import com.huawei.zxing.pdf417.PDF417Common;

public final class ModulusGF {
    public static final ModulusGF PDF417_GF = new ModulusGF(PDF417Common.NUMBER_OF_CODEWORDS, 3);
    private final int[] expTable;
    private final int[] logTable;
    private final int modulus;
    private final ModulusPoly one;
    private final ModulusPoly zero;

    private ModulusGF(int modulus2, int generator) {
        this.modulus = modulus2;
        this.expTable = new int[modulus2];
        this.logTable = new int[modulus2];
        int x = 1;
        for (int i = 0; i < modulus2; i++) {
            this.expTable[i] = x;
            x = (x * generator) % modulus2;
        }
        for (int i2 = 0; i2 < modulus2 - 1; i2++) {
            this.logTable[this.expTable[i2]] = i2;
        }
        this.zero = new ModulusPoly(this, new int[]{0});
        this.one = new ModulusPoly(this, new int[]{1});
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly getZero() {
        return this.zero;
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly getOne() {
        return this.one;
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly buildMonomial(int degree, int coefficient) {
        if (degree < 0) {
            throw new IllegalArgumentException();
        } else if (coefficient == 0) {
            return this.zero;
        } else {
            int[] coefficients = new int[(degree + 1)];
            coefficients[0] = coefficient;
            return new ModulusPoly(this, coefficients);
        }
    }

    /* access modifiers changed from: package-private */
    public int add(int a, int b) {
        return (a + b) % this.modulus;
    }

    /* access modifiers changed from: package-private */
    public int subtract(int a, int b) {
        return ((this.modulus + a) - b) % this.modulus;
    }

    /* access modifiers changed from: package-private */
    public int exp(int a) {
        return this.expTable[a];
    }

    /* access modifiers changed from: package-private */
    public int log(int a) {
        if (a != 0) {
            return this.logTable[a];
        }
        throw new IllegalArgumentException();
    }

    /* access modifiers changed from: package-private */
    public int inverse(int a) {
        if (a != 0) {
            return this.expTable[(this.modulus - this.logTable[a]) - 1];
        }
        throw new ArithmeticException();
    }

    /* access modifiers changed from: package-private */
    public int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return this.expTable[(this.logTable[a] + this.logTable[b]) % (this.modulus - 1)];
    }

    /* access modifiers changed from: package-private */
    public int getSize() {
        return this.modulus;
    }
}
