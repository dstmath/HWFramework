package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat224;
import java.math.BigInteger;

public class SecP224K1Field {
    static final int[] P = null;
    private static final int P6 = -1;
    static final int[] PExt = null;
    private static final int PExt13 = -1;
    private static final int[] PExtInv = null;
    private static final int PInv33 = 6803;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP224K1Field.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP224K1Field.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.custom.sec.SecP224K1Field.<clinit>():void");
    }

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat224.add(x, y, z) != 0 || (z[6] == PExt13 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(14, xx, yy, zz) != 0 || (zz[13] == PExt13 && Nat.gte(14, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(14, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(7, x, z) != 0 || (z[6] == PExt13 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat224.fromBigInteger(x);
        if (z[6] == PExt13 && Nat224.gte(z, P)) {
            Nat.add33To(7, PInv33, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        if ((x[0] & 1) == 0) {
            Nat.shiftDownBit(7, x, 0, z);
        } else {
            Nat.shiftDownBit(7, z, Nat224.add(x, P, z));
        }
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat224.createExt();
        Nat224.mul(x, y, tt);
        reduce(tt, z);
    }

    public static void multiplyAddToExt(int[] x, int[] y, int[] zz) {
        if ((Nat224.mulAddTo(x, y, zz) != 0 || (zz[13] == PExt13 && Nat.gte(14, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(14, zz, PExtInv.length);
        }
    }

    public static void negate(int[] x, int[] z) {
        if (Nat224.isZero(x)) {
            Nat224.zero(z);
        } else {
            Nat224.sub(P, x, z);
        }
    }

    public static void reduce(int[] xx, int[] z) {
        if (Nat224.mul33DWordAdd(PInv33, Nat224.mul33Add(PInv33, xx, 7, xx, 0, z, 0), z, 0) != 0 || (z[6] == PExt13 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }

    public static void reduce32(int x, int[] z) {
        if (x == 0 || Nat224.mul33WordAdd(PInv33, x, z, 0) == 0) {
            if (z[6] != PExt13 || !Nat224.gte(z, P)) {
                return;
            }
        }
        Nat.add33To(7, PInv33, z);
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat224.createExt();
        Nat224.square(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
        int[] tt = Nat224.createExt();
        Nat224.square(x, tt);
        reduce(tt, z);
        while (true) {
            n += PExt13;
            if (n > 0) {
                Nat224.square(z, tt);
                reduce(tt, z);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        if (Nat224.sub(x, y, z) != 0) {
            Nat.sub33From(7, PInv33, z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(14, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(14, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(7, x, 0, z) != 0 || (z[6] == PExt13 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }
}
