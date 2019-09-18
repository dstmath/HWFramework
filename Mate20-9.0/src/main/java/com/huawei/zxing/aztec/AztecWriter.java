package com.huawei.zxing.aztec;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.Writer;
import com.huawei.zxing.aztec.encoder.AztecCode;
import com.huawei.zxing.aztec.encoder.Encoder;
import com.huawei.zxing.common.BitMatrix;
import java.nio.charset.Charset;
import java.util.Map;

public final class AztecWriter implements Writer {
    private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) {
        return encode(contents, format, width, height, null);
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) {
        int intValue;
        Map<EncodeHintType, ?> map = hints;
        Number layers = null;
        String charset = map == null ? null : (String) map.get(EncodeHintType.CHARACTER_SET);
        Number eccPercent = map == null ? null : (Number) map.get(EncodeHintType.ERROR_CORRECTION);
        if (map != null) {
            layers = (Number) map.get(EncodeHintType.AZTEC_LAYERS);
        }
        Charset forName = charset == null ? DEFAULT_CHARSET : Charset.forName(charset);
        int intValue2 = eccPercent == null ? 33 : eccPercent.intValue();
        if (layers == null) {
            intValue = 0;
        } else {
            intValue = layers.intValue();
        }
        return encode(contents, format, width, height, forName, intValue2, intValue);
    }

    private static BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Charset charset, int eccPercent, int layers) {
        if (format == BarcodeFormat.AZTEC) {
            return renderResult(Encoder.encode(contents.getBytes(charset), eccPercent, layers), width, height);
        }
        throw new IllegalArgumentException("Can only encode AZTEC, but got " + format);
    }

    private static BitMatrix renderResult(AztecCode code, int width, int height) {
        BitMatrix input = code.getMatrix();
        if (input != null) {
            int inputWidth = input.getWidth();
            int inputHeight = input.getHeight();
            int outputWidth = Math.max(width, inputWidth);
            int outputHeight = Math.max(height, inputHeight);
            int multiple = Math.min(outputWidth / inputWidth, outputHeight / inputHeight);
            int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
            BitMatrix output = new BitMatrix(outputWidth, outputHeight);
            int inputY = 0;
            int outputY = (outputHeight - (inputHeight * multiple)) / 2;
            while (inputY < inputHeight) {
                int inputX = 0;
                int outputX = leftPadding;
                while (inputX < inputWidth) {
                    if (input.get(inputX, inputY)) {
                        output.setRegion(outputX, outputY, multiple, multiple);
                    }
                    inputX++;
                    outputX += multiple;
                }
                inputY++;
                outputY += multiple;
            }
            return output;
        }
        int i = width;
        int i2 = height;
        throw new IllegalStateException();
    }
}
