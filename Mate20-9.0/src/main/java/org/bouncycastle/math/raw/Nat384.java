package org.bouncycastle.math.raw;

public abstract class Nat384 {
    public static void mul(int[] iArr, int[] iArr2, int[] iArr3) {
        Nat192.mul(iArr, iArr2, iArr3);
        Nat192.mul(iArr, 6, iArr2, 6, iArr3, 12);
        int addToEachOther = Nat192.addToEachOther(iArr3, 6, iArr3, 12);
        int addTo = addToEachOther + Nat192.addTo(iArr3, 18, iArr3, 12, Nat192.addTo(iArr3, 0, iArr3, 6, 0) + addToEachOther);
        int[] create = Nat192.create();
        int[] create2 = Nat192.create();
        boolean z = Nat192.diff(iArr, 6, iArr, 0, create, 0) != Nat192.diff(iArr2, 6, iArr2, 0, create2, 0);
        int[] createExt = Nat192.createExt();
        Nat192.mul(create, create2, createExt);
        Nat.addWordAt(24, addTo + (z ? Nat.addTo(12, createExt, 0, iArr3, 6) : Nat.subFrom(12, createExt, 0, iArr3, 6)), iArr3, 18);
    }

    public static void square(int[] iArr, int[] iArr2) {
        Nat192.square(iArr, iArr2);
        Nat192.square(iArr, 6, iArr2, 12);
        int addToEachOther = Nat192.addToEachOther(iArr2, 6, iArr2, 12);
        int addTo = addToEachOther + Nat192.addTo(iArr2, 18, iArr2, 12, Nat192.addTo(iArr2, 0, iArr2, 6, 0) + addToEachOther);
        int[] create = Nat192.create();
        Nat192.diff(iArr, 6, iArr, 0, create, 0);
        int[] createExt = Nat192.createExt();
        Nat192.square(create, createExt);
        Nat.addWordAt(24, addTo + Nat.subFrom(12, createExt, 0, iArr2, 6), iArr2, 18);
    }
}
