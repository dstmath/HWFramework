package com.huawei.zxing.aztec.detector;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.aztec.AztecDetectorResult;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.GridSampler;
import com.huawei.zxing.common.detector.MathUtils;
import com.huawei.zxing.common.detector.WhiteRectangleDetector;
import com.huawei.zxing.common.reedsolomon.GenericGF;
import com.huawei.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.huawei.zxing.common.reedsolomon.ReedSolomonException;

public final class Detector {
    private static final int[] EXPECTED_CORNER_BITS = null;
    private boolean compact;
    private final BitMatrix image;
    private int nbCenterLayers;
    private int nbDataBlocks;
    private int nbLayers;
    private int shift;

    static final class Point {
        private final int x;
        private final int y;

        ResultPoint toResultPoint() {
            return new ResultPoint((float) getX(), (float) getY());
        }

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getX() {
            return this.x;
        }

        int getY() {
            return this.y;
        }

        public String toString() {
            return "<" + this.x + ' ' + this.y + '>';
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.aztec.detector.Detector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.aztec.detector.Detector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.aztec.detector.Detector.<clinit>():void");
    }

    public Detector(BitMatrix image) {
        this.image = image;
    }

    public AztecDetectorResult detect() throws NotFoundException {
        return detect(false);
    }

    public AztecDetectorResult detect(boolean isMirror) throws NotFoundException {
        ResultPoint[] bullsEyeCorners = getBullsEyeCorners(getMatrixCenter());
        if (isMirror) {
            ResultPoint temp = bullsEyeCorners[0];
            bullsEyeCorners[0] = bullsEyeCorners[2];
            bullsEyeCorners[2] = temp;
        }
        extractParameters(bullsEyeCorners);
        return new AztecDetectorResult(sampleGrid(this.image, bullsEyeCorners[this.shift % 4], bullsEyeCorners[(this.shift + 1) % 4], bullsEyeCorners[(this.shift + 2) % 4], bullsEyeCorners[(this.shift + 3) % 4]), getMatrixCornerPoints(bullsEyeCorners), this.compact, this.nbDataBlocks, this.nbLayers);
    }

    private void extractParameters(ResultPoint[] bullsEyeCorners) throws NotFoundException {
        if (isValid(bullsEyeCorners[0]) && isValid(bullsEyeCorners[1]) && isValid(bullsEyeCorners[2]) && isValid(bullsEyeCorners[3])) {
            int[] sides = new int[]{sampleLine(bullsEyeCorners[0], bullsEyeCorners[1], length), sampleLine(bullsEyeCorners[1], bullsEyeCorners[2], length), sampleLine(bullsEyeCorners[2], bullsEyeCorners[3], length), sampleLine(bullsEyeCorners[3], bullsEyeCorners[0], this.nbCenterLayers * 2)};
            this.shift = getRotation(sides, this.nbCenterLayers * 2);
            long parameterData = 0;
            for (int i = 0; i < 4; i++) {
                int side = sides[(this.shift + i) % 4];
                if (this.compact) {
                    parameterData = (parameterData << 7) + ((long) ((side >> 1) & 127));
                } else {
                    parameterData = (parameterData << 10) + ((long) (((side >> 2) & 992) + ((side >> 1) & 31)));
                }
            }
            int correctedData = getCorrectedParameterData(parameterData, this.compact);
            if (this.compact) {
                this.nbLayers = (correctedData >> 6) + 1;
                this.nbDataBlocks = (correctedData & 63) + 1;
                return;
            }
            this.nbLayers = (correctedData >> 11) + 1;
            this.nbDataBlocks = (correctedData & 2047) + 1;
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int getRotation(int[] sides, int length) throws NotFoundException {
        int cornerBits = 0;
        for (int side : sides) {
            cornerBits = (cornerBits << 3) + (((side >> (length - 2)) << 1) + (side & 1));
        }
        cornerBits = ((cornerBits & 1) << 11) + (cornerBits >> 1);
        for (int shift = 0; shift < 4; shift++) {
            if (Integer.bitCount(EXPECTED_CORNER_BITS[shift] ^ cornerBits) <= 2) {
                return shift;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int getCorrectedParameterData(long parameterData, boolean compact) throws NotFoundException {
        int numCodewords;
        int numDataCodewords;
        int i;
        if (compact) {
            numCodewords = 7;
            numDataCodewords = 2;
        } else {
            numCodewords = 10;
            numDataCodewords = 4;
        }
        int numECCodewords = numCodewords - numDataCodewords;
        int[] parameterWords = new int[numCodewords];
        for (i = numCodewords - 1; i >= 0; i--) {
            parameterWords[i] = ((int) parameterData) & 15;
            parameterData >>= 4;
        }
        try {
            new ReedSolomonDecoder(GenericGF.AZTEC_PARAM).decode(parameterWords, numECCodewords);
            int result = 0;
            for (i = 0; i < numDataCodewords; i++) {
                result = (result << 4) + parameterWords[i];
            }
            return result;
        } catch (ReedSolomonException e) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private ResultPoint[] getBullsEyeCorners(Point pCenter) throws NotFoundException {
        int i;
        Point pina = pCenter;
        Point pinb = pCenter;
        Point pinc = pCenter;
        Point pind = pCenter;
        boolean color = true;
        this.nbCenterLayers = 1;
        while (true) {
            i = this.nbCenterLayers;
            if (r0 >= 9) {
                break;
            }
            Point pouta = getFirstDifferent(pina, color, 1, -1);
            Point poutb = getFirstDifferent(pinb, color, 1, 1);
            Point poutc = getFirstDifferent(pinc, color, -1, 1);
            Point poutd = getFirstDifferent(pind, color, -1, -1);
            i = this.nbCenterLayers;
            if (r0 > 2) {
                float q = (distance(poutd, pouta) * ((float) this.nbCenterLayers)) / (distance(pind, pina) * ((float) (this.nbCenterLayers + 2)));
                if (((double) q) < 0.75d) {
                    break;
                }
                if (((double) q) <= 1.25d) {
                    if (!isWhiteOrBlackRectangle(pouta, poutb, poutc, poutd)) {
                        break;
                    }
                }
                break;
            }
            pina = pouta;
            pinb = poutb;
            pinc = poutc;
            pind = poutd;
            if (color) {
                color = false;
            } else {
                color = true;
            }
            this.nbCenterLayers++;
        }
        i = this.nbCenterLayers;
        if (r0 != 5) {
            i = this.nbCenterLayers;
            if (r0 != 7) {
                throw NotFoundException.getNotFoundInstance();
            }
        }
        i = this.nbCenterLayers;
        this.compact = r0 == 5;
        ResultPoint pinax = new ResultPoint(((float) pina.getX()) + 0.5f, ((float) pina.getY()) - 0.5f);
        ResultPoint pinbx = new ResultPoint(((float) pinb.getX()) + 0.5f, ((float) pinb.getY()) + 0.5f);
        ResultPoint pincx = new ResultPoint(((float) pinc.getX()) - 0.5f, ((float) pinc.getY()) + 0.5f);
        ResultPoint pindx = new ResultPoint(((float) pind.getX()) - 0.5f, ((float) pind.getY()) - 0.5f);
        return expandSquare(new ResultPoint[]{pinax, pinbx, pincx, pindx}, (float) ((this.nbCenterLayers * 2) - 3), (float) (this.nbCenterLayers * 2));
    }

    private Point getMatrixCenter() {
        ResultPoint pointA;
        ResultPoint pointB;
        ResultPoint pointC;
        ResultPoint pointD;
        int cx;
        int cy;
        try {
            ResultPoint[] cornerPoints = new WhiteRectangleDetector(this.image).detect();
            pointA = cornerPoints[0];
            pointB = cornerPoints[1];
            pointC = cornerPoints[2];
            pointD = cornerPoints[3];
        } catch (NotFoundException e) {
            cx = this.image.getWidth() / 2;
            cy = this.image.getHeight() / 2;
            pointA = getFirstDifferent(new Point(cx + 7, cy - 7), false, 1, -1).toResultPoint();
            pointB = getFirstDifferent(new Point(cx + 7, cy + 7), false, 1, 1).toResultPoint();
            pointC = getFirstDifferent(new Point(cx - 7, cy + 7), false, -1, 1).toResultPoint();
            pointD = getFirstDifferent(new Point(cx - 7, cy - 7), false, -1, -1).toResultPoint();
        }
        cx = MathUtils.round((((pointA.getX() + pointD.getX()) + pointB.getX()) + pointC.getX()) / 4.0f);
        cy = MathUtils.round((((pointA.getY() + pointD.getY()) + pointB.getY()) + pointC.getY()) / 4.0f);
        try {
            cornerPoints = new WhiteRectangleDetector(this.image, 15, cx, cy).detect();
            pointA = cornerPoints[0];
            pointB = cornerPoints[1];
            pointC = cornerPoints[2];
            pointD = cornerPoints[3];
        } catch (NotFoundException e2) {
            pointA = getFirstDifferent(new Point(cx + 7, cy - 7), false, 1, -1).toResultPoint();
            pointB = getFirstDifferent(new Point(cx + 7, cy + 7), false, 1, 1).toResultPoint();
            pointC = getFirstDifferent(new Point(cx - 7, cy + 7), false, -1, 1).toResultPoint();
            pointD = getFirstDifferent(new Point(cx - 7, cy - 7), false, -1, -1).toResultPoint();
        }
        return new Point(MathUtils.round((((pointA.getX() + pointD.getX()) + pointB.getX()) + pointC.getX()) / 4.0f), MathUtils.round((((pointA.getY() + pointD.getY()) + pointB.getY()) + pointC.getY()) / 4.0f));
    }

    private ResultPoint[] getMatrixCornerPoints(ResultPoint[] bullsEyeCorners) {
        return expandSquare(bullsEyeCorners, (float) (this.nbCenterLayers * 2), (float) getDimension());
    }

    private BitMatrix sampleGrid(BitMatrix image, ResultPoint topLeft, ResultPoint topRight, ResultPoint bottomRight, ResultPoint bottomLeft) throws NotFoundException {
        GridSampler sampler = GridSampler.getInstance();
        int dimension = getDimension();
        float low = (((float) dimension) / 2.0f) - ((float) this.nbCenterLayers);
        float high = (((float) dimension) / 2.0f) + ((float) this.nbCenterLayers);
        return sampler.sampleGrid(image, dimension, dimension, low, low, high, low, high, high, low, high, topLeft.getX(), topLeft.getY(), topRight.getX(), topRight.getY(), bottomRight.getX(), bottomRight.getY(), bottomLeft.getX(), bottomLeft.getY());
    }

    private int sampleLine(ResultPoint p1, ResultPoint p2, int size) {
        int result = 0;
        float d = distance(p1, p2);
        float moduleSize = d / ((float) size);
        float px = p1.getX();
        float py = p1.getY();
        float dx = ((p2.getX() - p1.getX()) * moduleSize) / d;
        float dy = ((p2.getY() - p1.getY()) * moduleSize) / d;
        for (int i = 0; i < size; i++) {
            if (this.image.get(MathUtils.round((((float) i) * dx) + px), MathUtils.round((((float) i) * dy) + py))) {
                result |= 1 << ((size - i) - 1);
            }
        }
        return result;
    }

    private boolean isWhiteOrBlackRectangle(Point p1, Point p2, Point p3, Point p4) {
        boolean z = false;
        Point p12 = new Point(p1.getX() - 3, p1.getY() + 3);
        Point p22 = new Point(p2.getX() - 3, p2.getY() - 3);
        Point p32 = new Point(p3.getX() + 3, p3.getY() - 3);
        Point p42 = new Point(p4.getX() + 3, p4.getY() + 3);
        int cInit = getColor(p42, p12);
        if (cInit == 0 || getColor(p12, p22) != cInit || getColor(p22, p32) != cInit) {
            return false;
        }
        if (getColor(p32, p42) == cInit) {
            z = true;
        }
        return z;
    }

    private int getColor(Point p1, Point p2) {
        float d = distance(p1, p2);
        float dx = ((float) (p2.getX() - p1.getX())) / d;
        float dy = ((float) (p2.getY() - p1.getY())) / d;
        int error = 0;
        float px = (float) p1.getX();
        float py = (float) p1.getY();
        boolean colorModel = this.image.get(p1.getX(), p1.getY());
        for (int i = 0; ((float) i) < d; i++) {
            px += dx;
            py += dy;
            if (this.image.get(MathUtils.round(px), MathUtils.round(py)) != colorModel) {
                error++;
            }
        }
        float errRatio = ((float) error) / d;
        if (errRatio > 0.1f && errRatio < 0.9f) {
            return 0;
        }
        return ((errRatio > 0.1f ? 1 : (errRatio == 0.1f ? 0 : -1)) <= 0) == colorModel ? 1 : -1;
    }

    private Point getFirstDifferent(Point init, boolean color, int dx, int dy) {
        int x = init.getX() + dx;
        int y = init.getY() + dy;
        while (isValid(x, y) && this.image.get(x, y) == color) {
            x += dx;
            y += dy;
        }
        x -= dx;
        y -= dy;
        while (isValid(x, y) && this.image.get(x, y) == color) {
            x += dx;
        }
        x -= dx;
        while (isValid(x, y) && this.image.get(x, y) == color) {
            y += dy;
        }
        return new Point(x, y - dy);
    }

    private static ResultPoint[] expandSquare(ResultPoint[] cornerPoints, float oldSide, float newSide) {
        float ratio = newSide / (2.0f * oldSide);
        float dx = cornerPoints[0].getX() - cornerPoints[2].getX();
        float dy = cornerPoints[0].getY() - cornerPoints[2].getY();
        float centerx = (cornerPoints[0].getX() + cornerPoints[2].getX()) / 2.0f;
        float centery = (cornerPoints[0].getY() + cornerPoints[2].getY()) / 2.0f;
        ResultPoint result0 = new ResultPoint((ratio * dx) + centerx, (ratio * dy) + centery);
        ResultPoint result2 = new ResultPoint(centerx - (ratio * dx), centery - (ratio * dy));
        dx = cornerPoints[1].getX() - cornerPoints[3].getX();
        dy = cornerPoints[1].getY() - cornerPoints[3].getY();
        centerx = (cornerPoints[1].getX() + cornerPoints[3].getX()) / 2.0f;
        centery = (cornerPoints[1].getY() + cornerPoints[3].getY()) / 2.0f;
        ResultPoint result1 = new ResultPoint((ratio * dx) + centerx, (ratio * dy) + centery);
        ResultPoint result3 = new ResultPoint(centerx - (ratio * dx), centery - (ratio * dy));
        return new ResultPoint[]{result0, result1, result2, result3};
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < this.image.getWidth() && y > 0 && y < this.image.getHeight();
    }

    private boolean isValid(ResultPoint point) {
        return isValid(MathUtils.round(point.getX()), MathUtils.round(point.getY()));
    }

    private static float distance(Point a, Point b) {
        return MathUtils.distance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    private static float distance(ResultPoint a, ResultPoint b) {
        return MathUtils.distance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    private int getDimension() {
        if (this.compact) {
            return (this.nbLayers * 4) + 11;
        }
        if (this.nbLayers <= 4) {
            return (this.nbLayers * 4) + 15;
        }
        return ((this.nbLayers * 4) + ((((this.nbLayers - 4) / 8) + 1) * 2)) + 15;
    }
}
