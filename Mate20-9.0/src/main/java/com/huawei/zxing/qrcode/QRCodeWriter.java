package com.huawei.zxing.qrcode;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.Writer;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.huawei.zxing.qrcode.encoder.ByteMatrix;
import com.huawei.zxing.qrcode.encoder.Encoder;
import com.huawei.zxing.qrcode.encoder.QRCode;
import java.util.Map;

public final class QRCodeWriter implements Writer {
    private static final int QUIET_ZONE_SIZE = 4;

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
        return encode(contents, format, width, height, null);
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        } else if (format != BarcodeFormat.QR_CODE) {
            throw new IllegalArgumentException("Can only encode QR_CODE, but got " + format);
        } else if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' + height);
        } else {
            ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
            int quietZone = 4;
            if (hints != null) {
                ErrorCorrectionLevel requestedECLevel = (ErrorCorrectionLevel) hints.get(EncodeHintType.ERROR_CORRECTION);
                if (requestedECLevel != null) {
                    errorCorrectionLevel = requestedECLevel;
                }
                Integer quietZoneInt = (Integer) hints.get(EncodeHintType.MARGIN);
                if (quietZoneInt != null) {
                    quietZone = quietZoneInt.intValue();
                }
            }
            return renderResult(Encoder.encode(contents, errorCorrectionLevel, hints), width, height, quietZone);
        }
    }

    private static BitMatrix renderResult(QRCode code, int width, int height, int quietZone) {
        int outputX;
        ByteMatrix input = code.getMatrix();
        if (input != null) {
            int inputWidth = input.getWidth();
            int inputHeight = input.getHeight();
            int qrWidth = (quietZone << 1) + inputWidth;
            int qrHeight = (quietZone << 1) + inputHeight;
            int outputWidth = Math.max(width, qrWidth);
            int outputHeight = Math.max(height, qrHeight);
            int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
            int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
            BitMatrix output = new BitMatrix(outputWidth, outputHeight);
            int inputY = 0;
            int outputY = (outputHeight - (inputHeight * multiple)) / 2;
            while (inputY < inputHeight) {
                int inputX = 0;
                int inputX2 = leftPadding;
                while (true) {
                    int outputX2 = inputX2;
                    if (inputX >= inputWidth) {
                        break;
                    }
                    int inputWidth2 = inputWidth;
                    ByteMatrix input2 = input;
                    if (input.get(inputX, inputY) == 1) {
                        outputX = outputX2;
                        output.setRegion(outputX, outputY, multiple, multiple);
                    } else {
                        outputX = outputX2;
                    }
                    inputX++;
                    inputX2 = outputX + multiple;
                    inputWidth = inputWidth2;
                    input = input2;
                }
                int i = inputWidth;
                inputY++;
                outputY += multiple;
            }
            int i2 = inputWidth;
            return output;
        }
        int i3 = width;
        int i4 = height;
        ByteMatrix byteMatrix = input;
        throw new IllegalStateException();
    }
}
