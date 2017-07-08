package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.zxing.NotFoundException;

final class FieldParser {
    private static final Object[][] FOUR_DIGIT_DATA_LENGTH = null;
    private static final Object[][] THREE_DIGIT_DATA_LENGTH = null;
    private static final Object[][] THREE_DIGIT_PLUS_DIGIT_DATA_LENGTH = null;
    private static final Object[][] TWO_DIGIT_DATA_LENGTH = null;
    private static final Object VARIABLE_LENGTH = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.rss.expanded.decoders.FieldParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.rss.expanded.decoders.FieldParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.rss.expanded.decoders.FieldParser.<clinit>():void");
    }

    private FieldParser() {
    }

    static String parseFieldsInGeneralPurpose(String rawInformation) throws NotFoundException {
        if (rawInformation.isEmpty()) {
            return null;
        }
        if (rawInformation.length() < 2) {
            throw NotFoundException.getNotFoundInstance();
        }
        String firstTwoDigits = rawInformation.substring(0, 2);
        Object[][] objArr = TWO_DIGIT_DATA_LENGTH;
        int length = objArr.length;
        int i = 0;
        while (i < length) {
            Object[] dataLength = objArr[i];
            if (!dataLength[0].equals(firstTwoDigits)) {
                i++;
            } else if (dataLength[1] == VARIABLE_LENGTH) {
                return processVariableAI(2, ((Integer) dataLength[2]).intValue(), rawInformation);
            } else {
                return processFixedAI(2, ((Integer) dataLength[1]).intValue(), rawInformation);
            }
        }
        if (rawInformation.length() < 3) {
            throw NotFoundException.getNotFoundInstance();
        }
        String firstThreeDigits = rawInformation.substring(0, 3);
        objArr = THREE_DIGIT_DATA_LENGTH;
        length = objArr.length;
        i = 0;
        while (i < length) {
            dataLength = objArr[i];
            if (!dataLength[0].equals(firstThreeDigits)) {
                i++;
            } else if (dataLength[1] == VARIABLE_LENGTH) {
                return processVariableAI(3, ((Integer) dataLength[2]).intValue(), rawInformation);
            } else {
                return processFixedAI(3, ((Integer) dataLength[1]).intValue(), rawInformation);
            }
        }
        objArr = THREE_DIGIT_PLUS_DIGIT_DATA_LENGTH;
        length = objArr.length;
        i = 0;
        while (i < length) {
            dataLength = objArr[i];
            if (!dataLength[0].equals(firstThreeDigits)) {
                i++;
            } else if (dataLength[1] == VARIABLE_LENGTH) {
                return processVariableAI(4, ((Integer) dataLength[2]).intValue(), rawInformation);
            } else {
                return processFixedAI(4, ((Integer) dataLength[1]).intValue(), rawInformation);
            }
        }
        if (rawInformation.length() < 4) {
            throw NotFoundException.getNotFoundInstance();
        }
        String firstFourDigits = rawInformation.substring(0, 4);
        objArr = FOUR_DIGIT_DATA_LENGTH;
        length = objArr.length;
        i = 0;
        while (i < length) {
            dataLength = objArr[i];
            if (!dataLength[0].equals(firstFourDigits)) {
                i++;
            } else if (dataLength[1] == VARIABLE_LENGTH) {
                return processVariableAI(4, ((Integer) dataLength[2]).intValue(), rawInformation);
            } else {
                return processFixedAI(4, ((Integer) dataLength[1]).intValue(), rawInformation);
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static String processFixedAI(int aiSize, int fieldSize, String rawInformation) throws NotFoundException {
        if (rawInformation.length() < aiSize) {
            throw NotFoundException.getNotFoundInstance();
        }
        String ai = rawInformation.substring(0, aiSize);
        if (rawInformation.length() < aiSize + fieldSize) {
            throw NotFoundException.getNotFoundInstance();
        }
        String field = rawInformation.substring(aiSize, aiSize + fieldSize);
        String remaining = rawInformation.substring(aiSize + fieldSize);
        String result = '(' + ai + ')' + field;
        String parsedAI = parseFieldsInGeneralPurpose(remaining);
        return parsedAI == null ? result : result + parsedAI;
    }

    private static String processVariableAI(int aiSize, int variableFieldSize, String rawInformation) throws NotFoundException {
        int maxSize;
        String ai = rawInformation.substring(0, aiSize);
        if (rawInformation.length() < aiSize + variableFieldSize) {
            maxSize = rawInformation.length();
        } else {
            maxSize = aiSize + variableFieldSize;
        }
        String field = rawInformation.substring(aiSize, maxSize);
        String remaining = rawInformation.substring(maxSize);
        String result = '(' + ai + ')' + field;
        String parsedAI = parseFieldsInGeneralPurpose(remaining);
        return parsedAI == null ? result : result + parsedAI;
    }
}
