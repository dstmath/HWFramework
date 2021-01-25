package com.huawei.zxing;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import com.huawei.zxing.aztec.AztecReader;
import com.huawei.zxing.common.HybridBinarizer;
import com.huawei.zxing.datamatrix.DataMatrixReader;
import com.huawei.zxing.maxicode.MaxiCodeReader;
import com.huawei.zxing.multi.GenericMultipleBarcodeReader;
import com.huawei.zxing.oned.MultiFormatOneDReader;
import com.huawei.zxing.pdf417.PDF417Reader;
import com.huawei.zxing.qrcode.QRCodeReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class MultiFormatReader implements Reader {
    private Map<DecodeHintType, ?> hints;
    private Reader[] readers;

    @Override // com.huawei.zxing.Reader
    public Result decode(BinaryBitmap image) throws NotFoundException {
        setHints(null);
        return decodeInternal(image);
    }

    @Override // com.huawei.zxing.Reader
    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints2) throws NotFoundException {
        setHints(hints2);
        return decodeInternal(image);
    }

    public Result decodeWithState(BinaryBitmap image) throws NotFoundException {
        if (this.readers == null) {
            setHints(null);
        }
        return decodeInternal(image);
    }

    public Result decode(byte[] data, int width, int height, Rect rect) {
        if (data == null || rect == null) {
            return null;
        }
        try {
            return decodeWithState(new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false))));
        } catch (ReaderException e) {
            return null;
        }
    }

    public Result[] decodeWithBitmap(Bitmap image) {
        if (image == null) {
            return null;
        }
        int lWidth = image.getWidth();
        int lHeight = image.getHeight();
        int[] lPixels = new int[(lWidth * lHeight)];
        image.getPixels(lPixels, 0, lWidth, 0, 0, lWidth, lHeight);
        try {
            return new GenericMultipleBarcodeReader(this).decodeMultiple(new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(lWidth, lHeight, lPixels))));
        } catch (NotFoundException e) {
            Log.e("MultiFormateReader", "not found, rawResult=null");
            return null;
        }
    }

    public void setHints(Map<DecodeHintType, ?> hints2) {
        this.hints = hints2;
        boolean addOneDReader = true;
        boolean tryHarder = hints2 != null && hints2.containsKey(DecodeHintType.TRY_HARDER);
        Collection<BarcodeFormat> formats = hints2 == null ? null : (Collection) hints2.get(DecodeHintType.POSSIBLE_FORMATS);
        Collection<Reader> readers2 = new ArrayList<>();
        if (formats != null) {
            if (!formats.contains(BarcodeFormat.UPC_A) && !formats.contains(BarcodeFormat.UPC_E) && !formats.contains(BarcodeFormat.EAN_13) && !formats.contains(BarcodeFormat.EAN_8) && !formats.contains(BarcodeFormat.CODABAR) && !formats.contains(BarcodeFormat.CODE_39) && !formats.contains(BarcodeFormat.CODE_93) && !formats.contains(BarcodeFormat.CODE_128) && !formats.contains(BarcodeFormat.ITF) && !formats.contains(BarcodeFormat.RSS_14) && !formats.contains(BarcodeFormat.RSS_EXPANDED)) {
                addOneDReader = false;
            }
            if (addOneDReader && !tryHarder) {
                readers2.add(new MultiFormatOneDReader(hints2));
            }
            if (formats.contains(BarcodeFormat.QR_CODE)) {
                readers2.add(new QRCodeReader());
            }
            if (formats.contains(BarcodeFormat.DATA_MATRIX)) {
                readers2.add(new DataMatrixReader());
            }
            if (formats.contains(BarcodeFormat.AZTEC)) {
                readers2.add(new AztecReader());
            }
            if (formats.contains(BarcodeFormat.PDF_417)) {
                readers2.add(new PDF417Reader());
            }
            if (formats.contains(BarcodeFormat.MAXICODE)) {
                readers2.add(new MaxiCodeReader());
            }
            if (addOneDReader && tryHarder) {
                readers2.add(new MultiFormatOneDReader(hints2));
            }
        }
        if (readers2.isEmpty()) {
            readers2.add(new QRCodeReader());
            readers2.add(new DataMatrixReader());
            readers2.add(new AztecReader());
            readers2.add(new PDF417Reader());
            readers2.add(new MaxiCodeReader());
            readers2.add(new MultiFormatOneDReader(hints2));
        }
        this.readers = (Reader[]) readers2.toArray(new Reader[readers2.size()]);
    }

    @Override // com.huawei.zxing.Reader
    public void reset() {
        Reader[] readerArr = this.readers;
        if (readerArr != null) {
            for (Reader reader : readerArr) {
                reader.reset();
            }
        }
    }

    private Result decodeInternal(BinaryBitmap image) throws NotFoundException {
        Reader[] readerArr = this.readers;
        if (readerArr != null) {
            int length = readerArr.length;
            for (int i = 0; i < length; i++) {
                Reader reader = readerArr[i];
                Log.e("MultiFormatReader", "MultFormatReader current reader=" + reader);
                try {
                    return reader.decode(image, this.hints);
                } catch (ReaderException e) {
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
