package com.huawei.zxing.oned;

import com.huawei.telephony.HuaweiTelephonyManager;
import java.util.Arrays;

public final class CodaBarWriter extends OneDimensionalCodeWriter {
    private static final char[] ALT_START_END_CHARS = null;
    private static final char[] START_END_CHARS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.oned.CodaBarWriter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.oned.CodaBarWriter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.oned.CodaBarWriter.<clinit>():void");
    }

    public boolean[] encode(String contents) {
        if (contents.length() < 2) {
            throw new IllegalArgumentException("Codabar should start/end with start/stop symbols");
        }
        boolean arrayContains;
        boolean arrayContains2;
        char firstChar = Character.toUpperCase(contents.charAt(0));
        char lastChar = Character.toUpperCase(contents.charAt(contents.length() - 1));
        if (CodaBarReader.arrayContains(START_END_CHARS, firstChar)) {
            arrayContains = CodaBarReader.arrayContains(START_END_CHARS, lastChar);
        } else {
            arrayContains = false;
        }
        if (CodaBarReader.arrayContains(ALT_START_END_CHARS, firstChar)) {
            arrayContains2 = CodaBarReader.arrayContains(ALT_START_END_CHARS, lastChar);
        } else {
            arrayContains2 = false;
        }
        if (arrayContains) {
            arrayContains2 = true;
        }
        if (arrayContains2) {
            int resultLength = 20;
            int i = 4;
            char[] charsWhichAreTenLengthEachAfterDecoded = new char[]{'/', ':', '+', '.'};
            int i2 = 1;
            while (i2 < contents.length() - 1) {
                if (Character.isDigit(contents.charAt(i2)) || contents.charAt(i2) == '-' || contents.charAt(i2) == '$') {
                    resultLength += 9;
                } else {
                    if (CodaBarReader.arrayContains(charsWhichAreTenLengthEachAfterDecoded, contents.charAt(i2))) {
                        resultLength += 10;
                    } else {
                        throw new IllegalArgumentException("Cannot encode : '" + contents.charAt(i2) + '\'');
                    }
                }
                i2++;
            }
            boolean[] result = new boolean[(resultLength + (contents.length() - 1))];
            int position = 0;
            for (int index = 0; index < contents.length(); index++) {
                char c = Character.toUpperCase(contents.charAt(index));
                if (index == contents.length() - 1) {
                    switch (c) {
                        case HuaweiTelephonyManager.CU_DUAL_MODE_CARD /*42*/:
                            c = 'C';
                            break;
                        case 'E':
                            c = 'D';
                            break;
                        case 'N':
                            c = 'B';
                            break;
                        case 'T':
                            c = 'A';
                            break;
                    }
                }
                int code = 0;
                i2 = 0;
                while (true) {
                    int length = CodaBarReader.ALPHABET.length;
                    if (i2 < i) {
                        if (c == CodaBarReader.ALPHABET[i2]) {
                            code = CodaBarReader.CHARACTER_ENCODINGS[i2];
                        } else {
                            i2++;
                        }
                    }
                    boolean color = true;
                    int counter = 0;
                    int bit = 0;
                    while (bit < 7) {
                        result[position] = color;
                        position++;
                        if (((code >> (6 - bit)) & 1) == 0 || counter == 1) {
                            color = !color;
                            bit++;
                            counter = 0;
                        } else {
                            counter++;
                        }
                    }
                    if (index < contents.length() - 1) {
                        result[position] = false;
                        position++;
                    }
                }
            }
            return result;
        }
        throw new IllegalArgumentException("Codabar should start/end with " + Arrays.toString(START_END_CHARS) + ", or start/end with " + Arrays.toString(ALT_START_END_CHARS));
    }
}
