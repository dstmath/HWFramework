package com.huawei.zxing.qrcode.decoder;

final class FormatInformation {
    private static final int[] BITS_SET_IN_HALF_BYTE = null;
    private static final int[][] FORMAT_INFO_DECODE_LOOKUP = null;
    private static final int FORMAT_INFO_MASK_QR = 21522;
    private final byte dataMask;
    private final ErrorCorrectionLevel errorCorrectionLevel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.qrcode.decoder.FormatInformation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.qrcode.decoder.FormatInformation.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.qrcode.decoder.FormatInformation.<clinit>():void");
    }

    private FormatInformation(int formatInfo) {
        this.errorCorrectionLevel = ErrorCorrectionLevel.forBits((formatInfo >> 3) & 3);
        this.dataMask = (byte) (formatInfo & 7);
    }

    static int numBitsDiffering(int a, int b) {
        a ^= b;
        return ((((((BITS_SET_IN_HALF_BYTE[a & 15] + BITS_SET_IN_HALF_BYTE[(a >>> 4) & 15]) + BITS_SET_IN_HALF_BYTE[(a >>> 8) & 15]) + BITS_SET_IN_HALF_BYTE[(a >>> 12) & 15]) + BITS_SET_IN_HALF_BYTE[(a >>> 16) & 15]) + BITS_SET_IN_HALF_BYTE[(a >>> 20) & 15]) + BITS_SET_IN_HALF_BYTE[(a >>> 24) & 15]) + BITS_SET_IN_HALF_BYTE[(a >>> 28) & 15];
    }

    static FormatInformation decodeFormatInformation(int maskedFormatInfo1, int maskedFormatInfo2) {
        FormatInformation formatInfo = doDecodeFormatInformation(maskedFormatInfo1, maskedFormatInfo2);
        if (formatInfo != null) {
            return formatInfo;
        }
        return doDecodeFormatInformation(maskedFormatInfo1 ^ FORMAT_INFO_MASK_QR, maskedFormatInfo2 ^ FORMAT_INFO_MASK_QR);
    }

    private static FormatInformation doDecodeFormatInformation(int maskedFormatInfo1, int maskedFormatInfo2) {
        int bestDifference = Integer.MAX_VALUE;
        int bestFormatInfo = 0;
        for (int[] decodeInfo : FORMAT_INFO_DECODE_LOOKUP) {
            int targetInfo = decodeInfo[0];
            if (targetInfo == maskedFormatInfo1 || targetInfo == maskedFormatInfo2) {
                return new FormatInformation(decodeInfo[1]);
            }
            int bitsDifference = numBitsDiffering(maskedFormatInfo1, targetInfo);
            if (bitsDifference < bestDifference) {
                bestFormatInfo = decodeInfo[1];
                bestDifference = bitsDifference;
            }
            if (maskedFormatInfo1 != maskedFormatInfo2) {
                bitsDifference = numBitsDiffering(maskedFormatInfo2, targetInfo);
                if (bitsDifference < bestDifference) {
                    bestFormatInfo = decodeInfo[1];
                    bestDifference = bitsDifference;
                }
            }
        }
        if (bestDifference <= 3) {
            return new FormatInformation(bestFormatInfo);
        }
        return null;
    }

    ErrorCorrectionLevel getErrorCorrectionLevel() {
        return this.errorCorrectionLevel;
    }

    byte getDataMask() {
        return this.dataMask;
    }

    public int hashCode() {
        return (this.errorCorrectionLevel.ordinal() << 3) | this.dataMask;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof FormatInformation)) {
            return false;
        }
        FormatInformation other = (FormatInformation) o;
        if (this.errorCorrectionLevel == other.errorCorrectionLevel && this.dataMask == other.dataMask) {
            z = true;
        }
        return z;
    }
}
