package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Reader;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.common.BitArray;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class MultiFormatUPCEANReader extends OneDReader {
    private final UPCEANReader[] readers;

    public MultiFormatUPCEANReader(Map<DecodeHintType, ?> hints) {
        Collection<BarcodeFormat> possibleFormats;
        if (hints == null) {
            possibleFormats = null;
        } else {
            possibleFormats = (Collection) hints.get(DecodeHintType.POSSIBLE_FORMATS);
        }
        Collection<UPCEANReader> readers2 = new ArrayList<>();
        if (possibleFormats != null) {
            if (possibleFormats.contains(BarcodeFormat.EAN_13)) {
                readers2.add(new EAN13Reader());
            } else if (possibleFormats.contains(BarcodeFormat.UPC_A)) {
                readers2.add(new UPCAReader());
            }
            if (possibleFormats.contains(BarcodeFormat.EAN_8)) {
                readers2.add(new EAN8Reader());
            }
            if (possibleFormats.contains(BarcodeFormat.UPC_E)) {
                readers2.add(new UPCEReader());
            }
        }
        if (readers2.isEmpty()) {
            readers2.add(new EAN13Reader());
            readers2.add(new EAN8Reader());
            readers2.add(new UPCEReader());
        }
        this.readers = (UPCEANReader[]) readers2.toArray(new UPCEANReader[readers2.size()]);
    }

    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        int[] startGuardPattern = UPCEANReader.findStartGuardPattern(row);
        UPCEANReader[] uPCEANReaderArr = this.readers;
        boolean canReturnUPCA = false;
        int i = 0;
        while (i < uPCEANReaderArr.length) {
            try {
                Result result = uPCEANReaderArr[i].decodeRow(rowNumber, row, startGuardPattern, hints);
                boolean ean13MayBeUPCA = result.getBarcodeFormat() == BarcodeFormat.EAN_13 && result.getText().charAt(0) == '0';
                Collection<BarcodeFormat> possibleFormats = hints == null ? null : (Collection) hints.get(DecodeHintType.POSSIBLE_FORMATS);
                if (possibleFormats == null || possibleFormats.contains(BarcodeFormat.UPC_A)) {
                    canReturnUPCA = true;
                }
                if (!ean13MayBeUPCA || !canReturnUPCA) {
                    return result;
                }
                Result resultUPCA = new Result(result.getText().substring(1), result.getRawBytes(), result.getResultPoints(), BarcodeFormat.UPC_A);
                resultUPCA.putAllMetadata(result.getResultMetadata());
                return resultUPCA;
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
