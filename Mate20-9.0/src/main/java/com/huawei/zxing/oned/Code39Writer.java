package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitMatrix;
import java.util.Map;

public final class Code39Writer extends OneDimensionalCodeWriter {
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format == BarcodeFormat.CODE_39) {
            return super.encode(contents, format, width, height, hints);
        }
        throw new IllegalArgumentException("Can only encode CODE_39, but got " + format);
    }

    public boolean[] encode(String contents) {
        int length = contents.length();
        if (length <= 80) {
            int[] widths = new int[9];
            int codeWidth = 25 + length;
            int i = 0;
            while (i < length) {
                int indexInString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(contents.charAt(i));
                if (indexInString >= 0) {
                    toIntArray(Code39Reader.CHARACTER_ENCODINGS[indexInString], widths);
                    int codeWidth2 = codeWidth;
                    for (int width : widths) {
                        codeWidth2 += width;
                    }
                    i++;
                    codeWidth = codeWidth2;
                } else {
                    throw new IllegalArgumentException("Bad contents: " + contents);
                }
            }
            boolean[] result = new boolean[codeWidth];
            toIntArray(Code39Reader.CHARACTER_ENCODINGS[39], widths);
            int pos = appendPattern(result, 0, widths, true);
            int[] narrowWhite = {1};
            int pos2 = pos + appendPattern(result, pos, narrowWhite, false);
            for (int i2 = length - 1; i2 >= 0; i2--) {
                toIntArray(Code39Reader.CHARACTER_ENCODINGS["0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(contents.charAt(i2))], widths);
                int pos3 = pos2 + appendPattern(result, pos2, widths, true);
                pos2 = pos3 + appendPattern(result, pos3, narrowWhite, false);
            }
            toIntArray(Code39Reader.CHARACTER_ENCODINGS[39], widths);
            int pos4 = pos2 + appendPattern(result, pos2, widths, true);
            return result;
        }
        throw new IllegalArgumentException("Requested contents should be less than 80 digits long, but got " + length);
    }

    private static void toIntArray(int a, int[] toReturn) {
        for (int i = 0; i < 9; i++) {
            int i2 = 1;
            if (((1 << i) & a) != 0) {
                i2 = 2;
            }
            toReturn[i] = i2;
        }
    }
}
