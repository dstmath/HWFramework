package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class Code128Writer extends OneDimensionalCodeWriter {
    private static final int CODE_CODE_B = 100;
    private static final int CODE_CODE_C = 99;
    private static final int CODE_FNC_1 = 102;
    private static final int CODE_FNC_2 = 97;
    private static final int CODE_FNC_3 = 96;
    private static final int CODE_FNC_4_B = 100;
    private static final int CODE_START_B = 104;
    private static final int CODE_START_C = 105;
    private static final int CODE_STOP = 106;
    private static final char ESCAPE_FNC_1 = 241;
    private static final char ESCAPE_FNC_2 = 242;
    private static final char ESCAPE_FNC_3 = 243;
    private static final char ESCAPE_FNC_4 = 244;

    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter, com.huawei.zxing.Writer
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format == BarcodeFormat.CODE_128) {
            return super.encode(contents, format, width, height, hints);
        }
        throw new IllegalArgumentException("Can only encode CODE_128, but got " + format);
    }

    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter
    public boolean[] encode(String contents) {
        int newCodeSet;
        int patternIndex;
        int length = contents.length();
        if (length < 1 || length > 80) {
            throw new IllegalArgumentException("Contents length should be between 1 and 80 characters, but got " + length);
        }
        for (int i = 0; i < length; i++) {
            char c = contents.charAt(i);
            if (c < ' ' || c > '~') {
                switch (c) {
                    default:
                        throw new IllegalArgumentException("Bad character in input: " + c);
                    case 241:
                    case 242:
                    case 243:
                    case 244:
                        break;
                }
            }
        }
        Collection<int[]> patterns = new ArrayList<>();
        int checkSum = 0;
        int checkWeight = 1;
        int codeSet = 0;
        int position = 0;
        while (position < length) {
            if (isDigits(contents, position, codeSet == 99 ? 2 : 4)) {
                newCodeSet = 99;
            } else {
                newCodeSet = 100;
            }
            if (newCodeSet != codeSet) {
                if (codeSet != 0) {
                    patternIndex = newCodeSet;
                } else if (newCodeSet == 100) {
                    patternIndex = 104;
                } else {
                    patternIndex = 105;
                }
                codeSet = newCodeSet;
            } else if (codeSet != 100) {
                switch (contents.charAt(position)) {
                    case 241:
                        patternIndex = 102;
                        position++;
                        break;
                    case 242:
                        patternIndex = CODE_FNC_2;
                        position++;
                        break;
                    case 243:
                        patternIndex = CODE_FNC_3;
                        position++;
                        break;
                    case 244:
                        patternIndex = 100;
                        position++;
                        break;
                    default:
                        patternIndex = Integer.parseInt(contents.substring(position, position + 2));
                        position += 2;
                        break;
                }
            } else {
                patternIndex = contents.charAt(position) - ' ';
                position++;
            }
            patterns.add(Code128Reader.CODE_PATTERNS[patternIndex]);
            checkSum += patternIndex * checkWeight;
            if (position != 0) {
                checkWeight++;
            }
        }
        patterns.add(Code128Reader.CODE_PATTERNS[checkSum % 103]);
        patterns.add(Code128Reader.CODE_PATTERNS[106]);
        int codeWidth = 0;
        for (int[] pattern : patterns) {
            for (int width : pattern) {
                codeWidth += width;
            }
        }
        boolean[] result = new boolean[codeWidth];
        int pos = 0;
        for (int[] pattern2 : patterns) {
            pos += appendPattern(result, pos, pattern2, true);
        }
        return result;
    }

    private static boolean isDigits(CharSequence value, int start, int length) {
        int end = start + length;
        int last = value.length();
        int i = start;
        while (i < end && i < last) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                if (c != 241) {
                    return false;
                }
                end++;
            }
            i++;
        }
        if (end <= last) {
            return true;
        }
        return false;
    }
}
