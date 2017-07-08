package com.huawei.zxing.pdf417.decoder.ec;

public final class ModulusGF {
    public static final ModulusGF PDF417_GF = null;
    private final int[] expTable;
    private final int[] logTable;
    private final int modulus;
    private final ModulusPoly one;
    private final ModulusPoly zero;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.decoder.ec.ModulusGF.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.decoder.ec.ModulusGF.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.decoder.ec.ModulusGF.<clinit>():void");
    }

    private ModulusGF(int modulus, int generator) {
        int i;
        this.modulus = modulus;
        this.expTable = new int[modulus];
        this.logTable = new int[modulus];
        int x = 1;
        for (i = 0; i < modulus; i++) {
            this.expTable[i] = x;
            x = (x * generator) % modulus;
        }
        for (i = 0; i < modulus - 1; i++) {
            this.logTable[this.expTable[i]] = i;
        }
        this.zero = new ModulusPoly(this, new int[]{0});
        this.one = new ModulusPoly(this, new int[]{1});
    }

    ModulusPoly getZero() {
        return this.zero;
    }

    ModulusPoly getOne() {
        return this.one;
    }

    ModulusPoly buildMonomial(int degree, int coefficient) {
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

    int add(int a, int b) {
        return (a + b) % this.modulus;
    }

    int subtract(int a, int b) {
        return ((this.modulus + a) - b) % this.modulus;
    }

    int exp(int a) {
        return this.expTable[a];
    }

    int log(int a) {
        if (a != 0) {
            return this.logTable[a];
        }
        throw new IllegalArgumentException();
    }

    int inverse(int a) {
        if (a != 0) {
            return this.expTable[(this.modulus - this.logTable[a]) - 1];
        }
        throw new ArithmeticException();
    }

    int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return this.expTable[(this.logTable[a] + this.logTable[b]) % (this.modulus - 1)];
    }

    int getSize() {
        return this.modulus;
    }
}
