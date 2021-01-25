package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitMatrix;
import java.util.Map;

public final class EAN8Writer extends UPCEANWriter {
    private static final int CODE_WIDTH = 67;

    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter, com.huawei.zxing.Writer
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format == BarcodeFormat.EAN_8) {
            return super.encode(contents, format, width, height, hints);
        }
        throw new IllegalArgumentException("Can only encode EAN_8, but got " + format);
    }

    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter
    public boolean[] encode(String contents) {
        if (contents.length() == 8) {
            boolean[] result = new boolean[67];
            int pos = 0 + appendPattern(result, 0, UPCEANReader.START_END_PATTERN, true);
            for (int i = 0; i <= 3; i++) {
                pos += appendPattern(result, pos, UPCEANReader.L_PATTERNS[Integer.parseInt(contents.substring(i, i + 1))], false);
            }
            int pos2 = pos + appendPattern(result, pos, UPCEANReader.MIDDLE_PATTERN, false);
            for (int i2 = 4; i2 <= 7; i2++) {
                pos2 += appendPattern(result, pos2, UPCEANReader.L_PATTERNS[Integer.parseInt(contents.substring(i2, i2 + 1))], true);
            }
            int pos3 = pos2 + appendPattern(result, pos2, UPCEANReader.START_END_PATTERN, true);
            return result;
        }
        throw new IllegalArgumentException("Requested contents should be 8 digits long, but got " + contents.length());
    }
}
