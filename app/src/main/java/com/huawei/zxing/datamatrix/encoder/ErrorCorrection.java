package com.huawei.zxing.datamatrix.encoder;

import com.huawei.internal.telephony.uicc.IccConstantsEx;

public final class ErrorCorrection {
    private static final int[] ALOG = null;
    private static final int[][] FACTORS = null;
    private static final int[] FACTOR_SETS = null;
    private static final int[] LOG = null;
    private static final int MODULO_VALUE = 301;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.datamatrix.encoder.ErrorCorrection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.datamatrix.encoder.ErrorCorrection.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.datamatrix.encoder.ErrorCorrection.<clinit>():void");
    }

    private ErrorCorrection() {
    }

    public static String encodeECC200(String codewords, SymbolInfo symbolInfo) {
        if (codewords.length() != symbolInfo.getDataCapacity()) {
            throw new IllegalArgumentException("The number of codewords does not match the selected symbol");
        }
        StringBuilder sb = new StringBuilder(symbolInfo.getDataCapacity() + symbolInfo.getErrorCodewords());
        sb.append(codewords);
        int blockCount = symbolInfo.getInterleavedBlockCount();
        if (blockCount == 1) {
            sb.append(createECCBlock(codewords, symbolInfo.getErrorCodewords()));
        } else {
            sb.setLength(sb.capacity());
            int[] dataSizes = new int[blockCount];
            int[] errorSizes = new int[blockCount];
            int[] startPos = new int[blockCount];
            for (int i = 0; i < blockCount; i++) {
                dataSizes[i] = symbolInfo.getDataLengthForInterleavedBlock(i + 1);
                errorSizes[i] = symbolInfo.getErrorLengthForInterleavedBlock(i + 1);
                startPos[i] = 0;
                if (i > 0) {
                    startPos[i] = startPos[i - 1] + dataSizes[i];
                }
            }
            for (int block = 0; block < blockCount; block++) {
                StringBuilder temp = new StringBuilder(dataSizes[block]);
                for (int d = block; d < symbolInfo.getDataCapacity(); d += blockCount) {
                    temp.append(codewords.charAt(d));
                }
                String ecc = createECCBlock(temp.toString(), errorSizes[block]);
                int pos = 0;
                int e = block;
                while (e < errorSizes[block] * blockCount) {
                    int pos2 = pos + 1;
                    sb.setCharAt(symbolInfo.getDataCapacity() + e, ecc.charAt(pos));
                    e += blockCount;
                    pos = pos2;
                }
            }
        }
        return sb.toString();
    }

    private static String createECCBlock(CharSequence codewords, int numECWords) {
        return createECCBlock(codewords, 0, codewords.length(), numECWords);
    }

    private static String createECCBlock(CharSequence codewords, int start, int len, int numECWords) {
        int i;
        int table = -1;
        for (i = 0; i < FACTOR_SETS.length; i++) {
            if (FACTOR_SETS[i] == numECWords) {
                table = i;
                break;
            }
        }
        if (table < 0) {
            throw new IllegalArgumentException("Illegal number of error correction codewords specified: " + numECWords);
        }
        int[] poly = FACTORS[table];
        char[] ecc = new char[numECWords];
        for (i = 0; i < numECWords; i++) {
            ecc[i] = '\u0000';
        }
        for (i = start; i < start + len; i++) {
            int m = ecc[numECWords - 1] ^ codewords.charAt(i);
            int k = numECWords - 1;
            while (k > 0) {
                if (m == 0 || poly[k] == 0) {
                    ecc[k] = ecc[k - 1];
                } else {
                    ecc[k] = (char) (ecc[k - 1] ^ ALOG[(LOG[m] + LOG[poly[k]]) % IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN]);
                }
                k--;
            }
            if (m == 0 || poly[0] == 0) {
                ecc[0] = '\u0000';
            } else {
                ecc[0] = (char) ALOG[(LOG[m] + LOG[poly[0]]) % IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN];
            }
        }
        char[] eccReversed = new char[numECWords];
        for (i = 0; i < numECWords; i++) {
            eccReversed[i] = ecc[(numECWords - i) - 1];
        }
        return String.valueOf(eccReversed);
    }
}
