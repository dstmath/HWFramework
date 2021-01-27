package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitMatrix;
import java.util.Map;

public final class Code39Writer extends OneDimensionalCodeWriter {
    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter, com.huawei.zxing.Writer
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format == BarcodeFormat.CODE_39) {
            return super.encode(contents, format, width, height, hints);
        }
        throw new IllegalArgumentException("Can only encode CODE_39, but got " + format);
    }

    /* JADX INFO: Multiple debug info for r3v3 boolean[]: [D('i' int), D('result' boolean[])] */
    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter
    public boolean[] encode(String contents) {
        int length = contents.length();
        if (length <= 80) {
            int[] widths = new int[9];
            int codeWidth = length + 25;
            int i = 0;
            while (true) {
                if (i < length) {
                    int indexInString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(contents.charAt(i));
                    if (indexInString >= 0) {
                        toIntArray(Code39Reader.CHARACTER_ENCODINGS[indexInString], widths);
                        for (int width : widths) {
                            codeWidth += width;
                        }
                        i++;
                    } else {
                        throw new IllegalArgumentException("Bad contents: " + contents);
                    }
                } else {
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
            }
        } else {
            throw new IllegalArgumentException("Requested contents should be less than 80 digits long, but got " + length);
        }
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
