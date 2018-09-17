package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat384;
import java.math.BigInteger;

public class SecP384R1Field {
    private static final long M = 4294967295L;
    static final int[] P = null;
    private static final int P11 = -1;
    static final int[] PExt = null;
    private static final int PExt23 = -1;
    private static final int[] PExtInv = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP384R1Field.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP384R1Field.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.custom.sec.SecP384R1Field.<clinit>():void");
    }

    public static void reduce(int[] r1, int[] r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP384R1Field.reduce(int[], int[]):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP384R1Field.reduce(int[], int[]):void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.custom.sec.SecP384R1Field.reduce(int[], int[]):void");
    }

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat.add(12, x, y, z) != 0 || (z[11] == PExt23 && Nat.gte(12, z, P))) {
            addPInvTo(z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(24, xx, yy, zz) != 0 || (zz[23] == PExt23 && Nat.gte(24, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(24, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(12, x, z) != 0 || (z[11] == PExt23 && Nat.gte(12, z, P))) {
            addPInvTo(z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat.fromBigInteger(384, x);
        if (z[11] == PExt23 && Nat.gte(12, z, P)) {
            Nat.subFrom(12, P, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        if ((x[0] & 1) == 0) {
            Nat.shiftDownBit(12, x, 0, z);
        } else {
            Nat.shiftDownBit(12, z, Nat.add(12, x, P, z));
        }
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat.create(24);
        Nat384.mul(x, y, tt);
        reduce(tt, z);
    }

    public static void negate(int[] x, int[] z) {
        if (Nat.isZero(12, x)) {
            Nat.zero(12, z);
        } else {
            Nat.sub(12, P, x, z);
        }
    }

    public static void reduce32(int x, int[] z) {
        long cc = 0;
        if (x != 0) {
            long xx12 = ((long) x) & M;
            cc = 0 + ((((long) z[0]) & M) + xx12);
            z[0] = (int) cc;
            cc = (cc >> 32) + ((((long) z[1]) & M) - xx12);
            z[1] = (int) cc;
            cc >>= 32;
            if (cc != 0) {
                cc += ((long) z[2]) & M;
                z[2] = (int) cc;
                cc >>= 32;
            }
            cc += (((long) z[3]) & M) + xx12;
            z[3] = (int) cc;
            cc = (cc >> 32) + ((((long) z[4]) & M) + xx12);
            z[4] = (int) cc;
            cc >>= 32;
        }
        if (cc == 0 || Nat.incAt(12, z, 5) == 0) {
            if (z[11] != PExt23 || !Nat.gte(12, z, P)) {
                return;
            }
        }
        addPInvTo(z);
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat.create(24);
        Nat384.square(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
        int[] tt = Nat.create(24);
        Nat384.square(x, tt);
        reduce(tt, z);
        while (true) {
            n += PExt23;
            if (n > 0) {
                Nat384.square(z, tt);
                reduce(tt, z);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        if (Nat.sub(12, x, y, z) != 0) {
            subPInvFrom(z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(24, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(24, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(12, x, 0, z) != 0 || (z[11] == PExt23 && Nat.gte(12, z, P))) {
            addPInvTo(z);
        }
    }

    private static void addPInvTo(int[] z) {
        long c = (((long) z[0]) & M) + 1;
        z[0] = (int) c;
        c = (c >> 32) + ((((long) z[1]) & M) - 1);
        z[1] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += ((long) z[2]) & M;
            z[2] = (int) c;
            c >>= 32;
        }
        c += (((long) z[3]) & M) + 1;
        z[3] = (int) c;
        c = (c >> 32) + ((((long) z[4]) & M) + 1);
        z[4] = (int) c;
        if ((c >> 32) != 0) {
            Nat.incAt(12, z, 5);
        }
    }

    private static void subPInvFrom(int[] z) {
        long c = (((long) z[0]) & M) - 1;
        z[0] = (int) c;
        c = (c >> 32) + ((((long) z[1]) & M) + 1);
        z[1] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += ((long) z[2]) & M;
            z[2] = (int) c;
            c >>= 32;
        }
        c += (((long) z[3]) & M) - 1;
        z[3] = (int) c;
        c = (c >> 32) + ((((long) z[4]) & M) - 1);
        z[4] = (int) c;
        if ((c >> 32) != 0) {
            Nat.decAt(12, z, 5);
        }
    }
}
