package com.huawei.zxing.common.reedsolomon;

public final class GenericGF {
    public static final GenericGF AZTEC_DATA_10 = null;
    public static final GenericGF AZTEC_DATA_12 = null;
    public static final GenericGF AZTEC_DATA_6 = null;
    public static final GenericGF AZTEC_DATA_8 = null;
    public static final GenericGF AZTEC_PARAM = null;
    public static final GenericGF DATA_MATRIX_FIELD_256 = null;
    private static final int INITIALIZATION_THRESHOLD = 0;
    public static final GenericGF MAXICODE_FIELD_64 = null;
    public static final GenericGF QR_CODE_FIELD_256 = null;
    private int[] expTable;
    private final int generatorBase;
    private boolean initialized;
    private int[] logTable;
    private GenericGFPoly one;
    private final int primitive;
    private final int size;
    private GenericGFPoly zero;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.common.reedsolomon.GenericGF.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.common.reedsolomon.GenericGF.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.common.reedsolomon.GenericGF.<clinit>():void");
    }

    public GenericGF(int primitive, int size, int b) {
        this.initialized = false;
        this.primitive = primitive;
        this.size = size;
        this.generatorBase = b;
        if (size <= 0) {
            initialize();
        }
    }

    private void initialize() {
        int i;
        this.expTable = new int[this.size];
        this.logTable = new int[this.size];
        int x = 1;
        for (i = 0; i < this.size; i++) {
            this.expTable[i] = x;
            x <<= 1;
            if (x >= this.size) {
                x = (x ^ this.primitive) & (this.size - 1);
            }
        }
        for (i = 0; i < this.size - 1; i++) {
            this.logTable[this.expTable[i]] = i;
        }
        this.zero = new GenericGFPoly(this, new int[]{0});
        this.one = new GenericGFPoly(this, new int[]{1});
        this.initialized = true;
    }

    private void checkInit() {
        if (!this.initialized) {
            initialize();
        }
    }

    GenericGFPoly getZero() {
        checkInit();
        return this.zero;
    }

    GenericGFPoly getOne() {
        checkInit();
        return this.one;
    }

    GenericGFPoly buildMonomial(int degree, int coefficient) {
        checkInit();
        if (degree < 0) {
            throw new IllegalArgumentException();
        } else if (coefficient == 0) {
            return this.zero;
        } else {
            int[] coefficients = new int[(degree + 1)];
            coefficients[0] = coefficient;
            return new GenericGFPoly(this, coefficients);
        }
    }

    static int addOrSubtract(int a, int b) {
        return a ^ b;
    }

    int exp(int a) {
        checkInit();
        return this.expTable[a];
    }

    int log(int a) {
        checkInit();
        if (a != 0) {
            return this.logTable[a];
        }
        throw new IllegalArgumentException();
    }

    int inverse(int a) {
        checkInit();
        if (a != 0) {
            return this.expTable[(this.size - this.logTable[a]) - 1];
        }
        throw new ArithmeticException();
    }

    int multiply(int a, int b) {
        checkInit();
        if (a == 0 || b == 0) {
            return 0;
        }
        return this.expTable[(this.logTable[a] + this.logTable[b]) % (this.size - 1)];
    }

    public int getSize() {
        return this.size;
    }

    public int getGeneratorBase() {
        return this.generatorBase;
    }

    public String toString() {
        return "GF(0x" + Integer.toHexString(this.primitive) + ',' + this.size + ')';
    }
}
