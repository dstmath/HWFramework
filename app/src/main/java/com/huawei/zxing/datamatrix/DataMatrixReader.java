package com.huawei.zxing.datamatrix;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.BinaryBitmap;
import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Reader;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.common.DetectorResult;
import com.huawei.zxing.datamatrix.decoder.Decoder;
import com.huawei.zxing.datamatrix.detector.Detector;
import java.util.List;
import java.util.Map;

public final class DataMatrixReader implements Reader {
    private static final ResultPoint[] NO_POINTS = null;
    private final Decoder decoder;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.datamatrix.DataMatrixReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.datamatrix.DataMatrixReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.datamatrix.DataMatrixReader.<clinit>():void");
    }

    public DataMatrixReader() {
        this.decoder = new Decoder();
    }

    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return decode(image, null);
    }

    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        if (hints == null || !hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect();
            decoderResult = this.decoder.decode(detectorResult.getBits());
            points = detectorResult.getPoints();
        } else {
            decoderResult = this.decoder.decode(extractPureBits(image.getBlackMatrix()));
            points = NO_POINTS;
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.DATA_MATRIX);
        List<byte[]> byteSegments = decoderResult.getByteSegments();
        if (byteSegments != null) {
            result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
        }
        String ecLevel = decoderResult.getECLevel();
        if (ecLevel != null) {
            result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
        }
        return result;
    }

    public void reset() {
    }

    private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
        int[] leftTopBlack = image.getTopLeftOnBit();
        int[] rightBottomBlack = image.getBottomRightOnBit();
        if (leftTopBlack == null || rightBottomBlack == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        int moduleSize = moduleSize(leftTopBlack, image);
        int top = leftTopBlack[1];
        int bottom = rightBottomBlack[1];
        int left = leftTopBlack[0];
        int matrixWidth = ((rightBottomBlack[0] - left) + 1) / moduleSize;
        int matrixHeight = ((bottom - top) + 1) / moduleSize;
        if (matrixWidth <= 0 || matrixHeight <= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        int nudge = moduleSize >> 1;
        top += nudge;
        left += nudge;
        BitMatrix bits = new BitMatrix(matrixWidth, matrixHeight);
        for (int y = 0; y < matrixHeight; y++) {
            int iOffset = top + (y * moduleSize);
            for (int x = 0; x < matrixWidth; x++) {
                if (image.get((x * moduleSize) + left, iOffset)) {
                    bits.set(x, y);
                }
            }
        }
        return bits;
    }

    private static int moduleSize(int[] leftTopBlack, BitMatrix image) throws NotFoundException {
        int width = image.getWidth();
        int x = leftTopBlack[0];
        int y = leftTopBlack[1];
        while (x < width && image.get(x, y)) {
            x++;
        }
        if (x == width) {
            throw NotFoundException.getNotFoundInstance();
        }
        int moduleSize = x - leftTopBlack[0];
        if (moduleSize != 0) {
            return moduleSize;
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
