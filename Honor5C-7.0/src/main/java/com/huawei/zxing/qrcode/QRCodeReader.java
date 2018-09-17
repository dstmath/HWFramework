package com.huawei.zxing.qrcode;

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
import com.huawei.zxing.qrcode.decoder.Decoder;
import com.huawei.zxing.qrcode.decoder.QRCodeDecoderMetaData;
import com.huawei.zxing.qrcode.detector.Detector;
import java.util.List;
import java.util.Map;

public class QRCodeReader implements Reader {
    private static final ResultPoint[] NO_POINTS = null;
    private final Decoder decoder;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.qrcode.QRCodeReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.qrcode.QRCodeReader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.qrcode.QRCodeReader.<clinit>():void");
    }

    public QRCodeReader() {
        this.decoder = new Decoder();
    }

    protected final Decoder getDecoder() {
        return this.decoder;
    }

    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return decode(image, null);
    }

    public final Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        if (hints == null || !hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(hints);
            decoderResult = this.decoder.decode(detectorResult.getBits(), (Map) hints);
            points = detectorResult.getPoints();
        } else {
            decoderResult = this.decoder.decode(extractPureBits(image.getBlackMatrix()), (Map) hints);
            points = NO_POINTS;
        }
        if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
            ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE);
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
        float moduleSize = moduleSize(leftTopBlack, image);
        int top = leftTopBlack[1];
        int bottom = rightBottomBlack[1];
        int left = leftTopBlack[0];
        int right = rightBottomBlack[0];
        if (left >= right || top >= bottom) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (bottom - top != right - left) {
            right = left + (bottom - top);
        }
        int matrixWidth = Math.round(((float) ((right - left) + 1)) / moduleSize);
        int matrixHeight = Math.round(((float) ((bottom - top) + 1)) / moduleSize);
        if (matrixWidth <= 0 || matrixHeight <= 0) {
            throw NotFoundException.getNotFoundInstance();
        } else if (matrixHeight != matrixWidth) {
            throw NotFoundException.getNotFoundInstance();
        } else {
            int nudge = (int) (moduleSize / 2.0f);
            top += nudge;
            left += nudge;
            int nudgedTooFarRight = (((int) (((float) (matrixWidth - 1)) * moduleSize)) + left) - (right - 1);
            if (nudgedTooFarRight > 0) {
                if (nudgedTooFarRight > nudge) {
                    throw NotFoundException.getNotFoundInstance();
                }
                left -= nudgedTooFarRight;
            }
            int nudgedTooFarDown = (((int) (((float) (matrixHeight - 1)) * moduleSize)) + top) - (bottom - 1);
            if (nudgedTooFarDown > 0) {
                if (nudgedTooFarDown > nudge) {
                    throw NotFoundException.getNotFoundInstance();
                }
                top -= nudgedTooFarDown;
            }
            BitMatrix bits = new BitMatrix(matrixWidth, matrixHeight);
            for (int y = 0; y < matrixHeight; y++) {
                int iOffset = top + ((int) (((float) y) * moduleSize));
                for (int x = 0; x < matrixWidth; x++) {
                    if (image.get(((int) (((float) x) * moduleSize)) + left, iOffset)) {
                        bits.set(x, y);
                    }
                }
            }
            return bits;
        }
    }

    private static float moduleSize(int[] leftTopBlack, BitMatrix image) throws NotFoundException {
        int height = image.getHeight();
        int width = image.getWidth();
        int x = leftTopBlack[0];
        int y = leftTopBlack[1];
        boolean inBlack = true;
        int transitions = 0;
        while (x < width && y < height) {
            if (inBlack != image.get(x, y)) {
                transitions++;
                if (transitions == 5) {
                    break;
                }
                inBlack = !inBlack;
            }
            x++;
            y++;
        }
        if (x != width && y != height) {
            return ((float) (x - leftTopBlack[0])) / 7.0f;
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
