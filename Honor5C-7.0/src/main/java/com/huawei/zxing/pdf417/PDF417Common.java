package com.huawei.zxing.pdf417;

import java.util.Collection;

public final class PDF417Common {
    public static final int BARS_IN_MODULE = 8;
    private static final int[] CODEWORD_TABLE = null;
    private static final int[] EMPTY_INT_ARRAY = null;
    public static final int MAX_CODEWORDS_IN_BARCODE = 928;
    public static final int MAX_ROWS_IN_BARCODE = 90;
    public static final int MIN_ROWS_IN_BARCODE = 3;
    public static final int MODULES_IN_CODEWORD = 17;
    public static final int MODULES_IN_STOP_PATTERN = 18;
    public static final int NUMBER_OF_CODEWORDS = 929;
    public static final int[] SYMBOL_TABLE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.PDF417Common.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.PDF417Common.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.PDF417Common.<clinit>():void");
    }

    private PDF417Common() {
    }

    public static int getBitCountSum(int[] moduleBitCount) {
        int bitCountSum = 0;
        for (int count : moduleBitCount) {
            bitCountSum += count;
        }
        return bitCountSum;
    }

    public static int[] toIntArray(Collection<Integer> list) {
        if (list == null || list.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        int[] result = new int[list.size()];
        int i = 0;
        for (Integer integer : list) {
            int i2 = i + 1;
            result[i] = integer.intValue();
            i = i2;
        }
        return result;
    }

    public static int getCodeword(long symbol) {
        int i = findCodewordIndex(symbol & 262143);
        if (i == -1) {
            return -1;
        }
        return (CODEWORD_TABLE[i] - 1) % NUMBER_OF_CODEWORDS;
    }

    private static int findCodewordIndex(long symbol) {
        int first = 0;
        int upto = SYMBOL_TABLE.length;
        while (first < upto) {
            int mid = (first + upto) >>> 1;
            if (symbol < ((long) SYMBOL_TABLE[mid])) {
                upto = mid;
            } else if (symbol <= ((long) SYMBOL_TABLE[mid])) {
                return mid;
            } else {
                first = mid + 1;
            }
        }
        return -1;
    }
}
