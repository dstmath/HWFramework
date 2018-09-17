package com.huawei.zxing.qrcode.decoder;

import com.huawei.zxing.common.BitMatrix;

abstract class DataMask {
    private static final DataMask[] DATA_MASKS = null;

    private static final class DataMask000 extends DataMask {
        private DataMask000() {
            super();
        }

        boolean isMasked(int i, int j) {
            return ((i + j) & 1) == 0;
        }
    }

    private static final class DataMask001 extends DataMask {
        private DataMask001() {
            super();
        }

        boolean isMasked(int i, int j) {
            return (i & 1) == 0;
        }
    }

    private static final class DataMask010 extends DataMask {
        private DataMask010() {
            super();
        }

        boolean isMasked(int i, int j) {
            return j % 3 == 0;
        }
    }

    private static final class DataMask011 extends DataMask {
        private DataMask011() {
            super();
        }

        boolean isMasked(int i, int j) {
            return (i + j) % 3 == 0;
        }
    }

    private static final class DataMask100 extends DataMask {
        private DataMask100() {
            super();
        }

        boolean isMasked(int i, int j) {
            return (((i >>> 1) + (j / 3)) & 1) == 0;
        }
    }

    private static final class DataMask101 extends DataMask {
        private DataMask101() {
            super();
        }

        boolean isMasked(int i, int j) {
            int temp = i * j;
            if ((temp & 1) + (temp % 3) == 0) {
                return true;
            }
            return false;
        }
    }

    private static final class DataMask110 extends DataMask {
        private DataMask110() {
            super();
        }

        boolean isMasked(int i, int j) {
            int temp = i * j;
            if ((((temp & 1) + (temp % 3)) & 1) == 0) {
                return true;
            }
            return false;
        }
    }

    private static final class DataMask111 extends DataMask {
        private DataMask111() {
            super();
        }

        boolean isMasked(int i, int j) {
            return ((((i + j) & 1) + ((i * j) % 3)) & 1) == 0;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.qrcode.decoder.DataMask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.qrcode.decoder.DataMask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.qrcode.decoder.DataMask.<clinit>():void");
    }

    abstract boolean isMasked(int i, int i2);

    private DataMask() {
    }

    final void unmaskBitMatrix(BitMatrix bits, int dimension) {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (isMasked(i, j)) {
                    bits.flip(j, i);
                }
            }
        }
    }

    static DataMask forReference(int reference) {
        if (reference >= 0 && reference <= 7) {
            return DATA_MASKS[reference];
        }
        throw new IllegalArgumentException();
    }
}
