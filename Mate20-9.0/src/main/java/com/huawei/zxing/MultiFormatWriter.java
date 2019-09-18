package com.huawei.zxing;

import android.graphics.Bitmap;
import com.huawei.zxing.aztec.AztecWriter;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.datamatrix.DataMatrixWriter;
import com.huawei.zxing.oned.CodaBarWriter;
import com.huawei.zxing.oned.Code128Writer;
import com.huawei.zxing.oned.Code39Writer;
import com.huawei.zxing.oned.EAN13Writer;
import com.huawei.zxing.oned.EAN8Writer;
import com.huawei.zxing.oned.ITFWriter;
import com.huawei.zxing.oned.UPCAWriter;
import com.huawei.zxing.pdf417.PDF417Writer;
import com.huawei.zxing.qrcode.QRCodeWriter;
import java.util.Map;

public final class MultiFormatWriter implements Writer {
    private static final int BLACK = -16777216;
    private static final int WHITE = -1;

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
        return encode(contents, format, width, height, null);
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        Writer writer;
        switch (format) {
            case EAN_8:
                writer = new EAN8Writer();
                break;
            case EAN_13:
                writer = new EAN13Writer();
                break;
            case UPC_A:
                writer = new UPCAWriter();
                break;
            case QR_CODE:
                writer = new QRCodeWriter();
                break;
            case CODE_39:
                writer = new Code39Writer();
                break;
            case CODE_128:
                writer = new Code128Writer();
                break;
            case ITF:
                writer = new ITFWriter();
                break;
            case PDF_417:
                writer = new PDF417Writer();
                break;
            case CODABAR:
                writer = new CodaBarWriter();
                break;
            case DATA_MATRIX:
                writer = new DataMatrixWriter();
                break;
            case AZTEC:
                writer = new AztecWriter();
                break;
            default:
                throw new IllegalArgumentException("No encoder available for format " + format);
        }
        return writer.encode(contents, format, width, height, hints);
    }

    public Bitmap encodeAsBitmap(BitMatrix result) {
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[(width * height)];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? -16777216 : -1;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
