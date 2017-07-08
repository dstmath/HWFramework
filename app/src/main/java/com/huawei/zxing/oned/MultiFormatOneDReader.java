package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Reader;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.oned.rss.RSS14Reader;
import com.huawei.zxing.oned.rss.expanded.RSSExpandedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class MultiFormatOneDReader extends OneDReader {
    private final OneDReader[] readers;

    public MultiFormatOneDReader(Map<DecodeHintType, ?> hints) {
        Collection collection;
        if (hints == null) {
            collection = null;
        } else {
            collection = (Collection) hints.get(DecodeHintType.POSSIBLE_FORMATS);
        }
        boolean useCode39CheckDigit = hints != null ? hints.get(DecodeHintType.ASSUME_CODE_39_CHECK_DIGIT) != null : false;
        Collection<OneDReader> readers = new ArrayList();
        if (collection != null) {
            if (collection.contains(BarcodeFormat.EAN_13) || collection.contains(BarcodeFormat.UPC_A) || collection.contains(BarcodeFormat.EAN_8) || collection.contains(BarcodeFormat.UPC_E)) {
                readers.add(new MultiFormatUPCEANReader(hints));
            }
            if (collection.contains(BarcodeFormat.CODE_39)) {
                readers.add(new Code39Reader(useCode39CheckDigit));
            }
            if (collection.contains(BarcodeFormat.CODE_93)) {
                readers.add(new Code93Reader());
            }
            if (collection.contains(BarcodeFormat.CODE_128)) {
                readers.add(new Code128Reader());
            }
            if (collection.contains(BarcodeFormat.ITF)) {
                readers.add(new ITFReader());
            }
            if (collection.contains(BarcodeFormat.CODABAR)) {
                readers.add(new CodaBarReader());
            }
            if (collection.contains(BarcodeFormat.RSS_14)) {
                readers.add(new RSS14Reader());
            }
            if (collection.contains(BarcodeFormat.RSS_EXPANDED)) {
                readers.add(new RSSExpandedReader());
            }
        }
        if (readers.isEmpty()) {
            readers.add(new MultiFormatUPCEANReader(hints));
            readers.add(new Code39Reader());
            readers.add(new CodaBarReader());
            readers.add(new Code93Reader());
            readers.add(new Code128Reader());
            readers.add(new ITFReader());
            readers.add(new RSS14Reader());
            readers.add(new RSSExpandedReader());
        }
        this.readers = (OneDReader[]) readers.toArray(new OneDReader[readers.size()]);
    }

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        OneDReader[] oneDReaderArr = this.readers;
        int i = 0;
        while (i < oneDReaderArr.length) {
            try {
                return oneDReaderArr[i].decodeRow(rowNumber, row, hints);
            } catch (ReaderException e) {
                i++;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    public void reset() {
        for (Reader reader : this.readers) {
            reader.reset();
        }
    }
}
