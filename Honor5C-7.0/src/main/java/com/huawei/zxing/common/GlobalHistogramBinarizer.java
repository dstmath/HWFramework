package com.huawei.zxing.common;

import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.zxing.Binarizer;
import com.huawei.zxing.LuminanceSource;
import com.huawei.zxing.NotFoundException;

public class GlobalHistogramBinarizer extends Binarizer {
    private static final byte[] EMPTY = null;
    private static final int LUMINANCE_BITS = 5;
    private static final int LUMINANCE_BUCKETS = 32;
    private static final int LUMINANCE_SHIFT = 3;
    private final int[] buckets;
    private byte[] luminances;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.common.GlobalHistogramBinarizer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.common.GlobalHistogramBinarizer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.common.GlobalHistogramBinarizer.<clinit>():void");
    }

    public GlobalHistogramBinarizer(LuminanceSource source) {
        super(source);
        this.luminances = EMPTY;
        this.buckets = new int[LUMINANCE_BUCKETS];
    }

    public BitArray getBlackRow(int y, BitArray row) throws NotFoundException {
        int x;
        LuminanceSource source = getLuminanceSource();
        int width = source.getWidth();
        if (row == null || row.getSize() < width) {
            row = new BitArray(width);
        } else {
            row.clear();
        }
        initArrays(width);
        byte[] localLuminances = source.getRow(y, this.luminances);
        int[] localBuckets = this.buckets;
        for (x = 0; x < width; x++) {
            int i = (localLuminances[x] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN) >> LUMINANCE_SHIFT;
            localBuckets[i] = localBuckets[i] + 1;
        }
        int blackPoint = estimateBlackPoint(localBuckets);
        int left = localLuminances[0] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN;
        int center = localLuminances[1] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN;
        for (x = 1; x < width - 1; x++) {
            int right = localLuminances[x + 1] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN;
            if (((((center << 2) - left) - right) >> 1) < blackPoint) {
                row.set(x);
            }
            left = center;
            center = right;
        }
        return row;
    }

    public BitMatrix getBlackMatrix() throws NotFoundException {
        int y;
        byte[] localLuminances;
        LuminanceSource source = getLuminanceSource();
        int width = source.getWidth();
        int height = source.getHeight();
        BitMatrix matrix = new BitMatrix(width, height);
        initArrays(width);
        int[] localBuckets = this.buckets;
        for (y = 1; y < LUMINANCE_BITS; y++) {
            int x;
            localLuminances = source.getRow((height * y) / LUMINANCE_BITS, this.luminances);
            int right = (width << 2) / LUMINANCE_BITS;
            for (x = width / LUMINANCE_BITS; x < right; x++) {
                int i = (localLuminances[x] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN) >> LUMINANCE_SHIFT;
                localBuckets[i] = localBuckets[i] + 1;
            }
        }
        int blackPoint = estimateBlackPoint(localBuckets);
        localLuminances = source.getMatrix();
        for (y = 0; y < height; y++) {
            int offset = y * width;
            for (x = 0; x < width; x++) {
                if ((localLuminances[offset + x] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN) < blackPoint) {
                    matrix.set(x, y);
                }
            }
        }
        return matrix;
    }

    public Binarizer createBinarizer(LuminanceSource source) {
        return new GlobalHistogramBinarizer(source);
    }

    private void initArrays(int luminanceSize) {
        if (this.luminances.length < luminanceSize) {
            this.luminances = new byte[luminanceSize];
        }
        for (int x = 0; x < LUMINANCE_BUCKETS; x++) {
            this.buckets[x] = 0;
        }
    }

    private static int estimateBlackPoint(int[] buckets) throws NotFoundException {
        int x;
        int numBuckets = buckets.length;
        int maxBucketCount = 0;
        int firstPeak = 0;
        int firstPeakSize = 0;
        for (x = 0; x < numBuckets; x++) {
            if (buckets[x] > firstPeakSize) {
                firstPeak = x;
                firstPeakSize = buckets[x];
            }
            if (buckets[x] > maxBucketCount) {
                maxBucketCount = buckets[x];
            }
        }
        int secondPeak = 0;
        int secondPeakScore = 0;
        for (x = 0; x < numBuckets; x++) {
            int distanceToBiggest = x - firstPeak;
            int score = (buckets[x] * distanceToBiggest) * distanceToBiggest;
            if (score > secondPeakScore) {
                secondPeak = x;
                secondPeakScore = score;
            }
        }
        if (firstPeak > secondPeak) {
            int temp = firstPeak;
            firstPeak = secondPeak;
            secondPeak = temp;
        }
        if (secondPeak - firstPeak <= (numBuckets >> 4)) {
            throw NotFoundException.getNotFoundInstance();
        }
        int bestValley = secondPeak - 1;
        int bestValleyScore = -1;
        for (x = secondPeak - 1; x > firstPeak; x--) {
            int fromFirst = x - firstPeak;
            score = ((fromFirst * fromFirst) * (secondPeak - x)) * (maxBucketCount - buckets[x]);
            if (score > bestValleyScore) {
                bestValley = x;
                bestValleyScore = score;
            }
        }
        return bestValley << LUMINANCE_SHIFT;
    }
}
