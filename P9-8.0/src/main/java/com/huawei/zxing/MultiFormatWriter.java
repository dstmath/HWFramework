package com.huawei.zxing;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
    private static final /* synthetic */ int[] -com-huawei-zxing-BarcodeFormatSwitchesValues = null;
    private static final int BLACK = -16777216;
    private static final int WHITE = -1;

    private static /* synthetic */ int[] -getcom-huawei-zxing-BarcodeFormatSwitchesValues() {
        if (-com-huawei-zxing-BarcodeFormatSwitchesValues != null) {
            return -com-huawei-zxing-BarcodeFormatSwitchesValues;
        }
        int[] iArr = new int[BarcodeFormat.values().length];
        try {
            iArr[BarcodeFormat.AZTEC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[BarcodeFormat.CODABAR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[BarcodeFormat.CODE_128.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[BarcodeFormat.CODE_39.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[BarcodeFormat.CODE_93.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[BarcodeFormat.DATA_MATRIX.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[BarcodeFormat.EAN_13.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[BarcodeFormat.EAN_8.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[BarcodeFormat.ITF.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[BarcodeFormat.MAXICODE.ordinal()] = 13;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[BarcodeFormat.PDF_417.ordinal()] = 9;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[BarcodeFormat.QR_CODE.ordinal()] = 10;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[BarcodeFormat.RSS_14.ordinal()] = 14;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[BarcodeFormat.RSS_EXPANDED.ordinal()] = 15;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[BarcodeFormat.UPC_A.ordinal()] = 11;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[BarcodeFormat.UPC_E.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[BarcodeFormat.UPC_EAN_EXTENSION.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        -com-huawei-zxing-BarcodeFormatSwitchesValues = iArr;
        return iArr;
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
        return encode(contents, format, width, height, null);
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        Writer writer;
        switch (-getcom-huawei-zxing-BarcodeFormatSwitchesValues()[format.ordinal()]) {
            case 1:
                writer = new AztecWriter();
                break;
            case 2:
                writer = new CodaBarWriter();
                break;
            case 3:
                writer = new Code128Writer();
                break;
            case 4:
                writer = new Code39Writer();
                break;
            case 5:
                writer = new DataMatrixWriter();
                break;
            case 6:
                writer = new EAN13Writer();
                break;
            case 7:
                writer = new EAN8Writer();
                break;
            case 8:
                writer = new ITFWriter();
                break;
            case 9:
                writer = new PDF417Writer();
                break;
            case 10:
                writer = new QRCodeWriter();
                break;
            case 11:
                writer = new UPCAWriter();
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
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
