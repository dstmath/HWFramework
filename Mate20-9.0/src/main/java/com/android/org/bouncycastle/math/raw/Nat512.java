package com.android.org.bouncycastle.math.raw;

public abstract class Nat512 {
    public static void mul(int[] x, int[] y, int[] zz) {
        int[] iArr = zz;
        Nat256.mul(x, y, zz);
        Nat256.mul(x, 8, y, 8, iArr, 16);
        int c24 = Nat256.addToEachOther(iArr, 8, iArr, 16);
        int c242 = c24 + Nat256.addTo(iArr, 24, iArr, 16, Nat256.addTo(iArr, 0, iArr, 8, 0) + c24);
        int[] dx = Nat256.create();
        int[] dy = Nat256.create();
        boolean neg = Nat256.diff(x, 8, x, 0, dx, 0) != Nat256.diff(y, 8, y, 0, dy, 0);
        int[] tt = Nat256.createExt();
        Nat256.mul(dx, dy, tt);
        Nat.addWordAt(32, c242 + (neg ? Nat.addTo(16, tt, 0, iArr, 8) : Nat.subFrom(16, tt, 0, iArr, 8)), iArr, 24);
    }

    public static void square(int[] x, int[] zz) {
        Nat256.square(x, zz);
        Nat256.square(x, 8, zz, 16);
        int c24 = Nat256.addToEachOther(zz, 8, zz, 16);
        int c242 = c24 + Nat256.addTo(zz, 24, zz, 16, Nat256.addTo(zz, 0, zz, 8, 0) + c24);
        int[] dx = Nat256.create();
        Nat256.diff(x, 8, x, 0, dx, 0);
        int[] tt = Nat256.createExt();
        Nat256.square(dx, tt);
        Nat.addWordAt(32, c242 + Nat.subFrom(16, tt, 0, zz, 8), zz, 24);
    }
}
