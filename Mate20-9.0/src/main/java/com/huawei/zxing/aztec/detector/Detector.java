package com.huawei.zxing.aztec.detector;

import com.huawei.android.hishow.AlarmInfoEx;
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
    private static final int[] EXPECTED_CORNER_BITS = {3808, 476, 2107, 1799};
    private boolean compact;
    private final BitMatrix image;
    private int nbCenterLayers;
    private int nbDataBlocks;
    private int nbLayers;
    private int shift;

    static final class Point {
        private final int x;
        private final int y;

        /* access modifiers changed from: package-private */
        public ResultPoint toResultPoint() {
            return new ResultPoint((float) getX(), (float) getY());
        }

        Point(int x2, int y2) {
            this.x = x2;
            this.y = y2;
        }

        /* access modifiers changed from: package-private */
        public int getX() {
            return this.x;
        }

        /* access modifiers changed from: package-private */
        public int getY() {
            return this.y;
        }

        public String toString() {
            return "<" + this.x + ' ' + this.y + '>';
        }
    }

    public Detector(BitMatrix image2) {
        this.image = image2;
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
        BitMatrix bits = sampleGrid(this.image, bullsEyeCorners[this.shift % 4], bullsEyeCorners[(this.shift + 1) % 4], bullsEyeCorners[(this.shift + 2) % 4], bullsEyeCorners[(this.shift + 3) % 4]);
        AztecDetectorResult aztecDetectorResult = new AztecDetectorResult(bits, getMatrixCornerPoints(bullsEyeCorners), this.compact, this.nbDataBlocks, this.nbLayers);
        return aztecDetectorResult;
    }

    private void extractParameters(ResultPoint[] bullsEyeCorners) throws NotFoundException {
        int i;
        long parameterData;
        if (!isValid(bullsEyeCorners[0]) || !isValid(bullsEyeCorners[1]) || !isValid(bullsEyeCorners[2]) || !isValid(bullsEyeCorners[3])) {
            throw NotFoundException.getNotFoundInstance();
        }
        int length = this.nbCenterLayers * 2;
        int[] sides = {sampleLine(bullsEyeCorners[0], bullsEyeCorners[1], length), sampleLine(bullsEyeCorners[1], bullsEyeCorners[2], length), sampleLine(bullsEyeCorners[2], bullsEyeCorners[3], length), sampleLine(bullsEyeCorners[3], bullsEyeCorners[0], length)};
        this.shift = getRotation(sides, length);
        long parameterData2 = 0;
        for (int i2 = 0; i2 < 4; i2++) {
            int side = sides[(this.shift + i2) % 4];
            if (this.compact) {
                parameterData = parameterData2 << 7;
                i = (side >> 1) & AlarmInfoEx.EVERYDAY_CODE;
            } else {
                parameterData = parameterData2 << 10;
                i = ((side >> 2) & 992) + ((side >> 1) & 31);
            }
            parameterData2 = parameterData + ((long) i);
        }
        int correctedData = getCorrectedParameterData(parameterData2, this.compact);
        if (this.compact) {
            this.nbLayers = (correctedData >> 6) + 1;
            this.nbDataBlocks = (correctedData & 63) + 1;
            return;
        }
        this.nbLayers = (correctedData >> 11) + 1;
        this.nbDataBlocks = (correctedData & 2047) + 1;
    }

    private static int getRotation(int[] sides, int length) throws NotFoundException {
        int shift2 = 0;
        int cornerBits = 0;
        for (int side : sides) {
            cornerBits = (cornerBits << 3) + ((side >> (length - 2)) << 1) + (side & 1);
        }
        int cornerBits2 = ((cornerBits & 1) << 11) + (cornerBits >> 1);
        while (true) {
            int shift3 = shift2;
            if (shift3 >= 4) {
                throw NotFoundException.getNotFoundInstance();
            } else if (Integer.bitCount(EXPECTED_CORNER_BITS[shift3] ^ cornerBits2) <= 2) {
                return shift3;
            } else {
                shift2 = shift3 + 1;
            }
        }
    }

    private static int getCorrectedParameterData(long parameterData, boolean compact2) throws NotFoundException {
        int numDataCodewords;
        int numCodewords;
        if (compact2) {
            numCodewords = 7;
            numDataCodewords = 2;
        } else {
            numCodewords = 10;
            numDataCodewords = 4;
        }
        int numECCodewords = numCodewords - numDataCodewords;
        int[] parameterWords = new int[numCodewords];
        for (int i = numCodewords - 1; i >= 0; i--) {
            parameterWords[i] = ((int) parameterData) & 15;
            parameterData >>= 4;
        }
        try {
            new ReedSolomonDecoder(GenericGF.AZTEC_PARAM).decode(parameterWords, numECCodewords);
            int result = 0;
            for (int i2 = 0; i2 < numDataCodewords; i2++) {
                result = (result << 4) + parameterWords[i2];
            }
            return result;
        } catch (ReedSolomonException e) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private ResultPoint[] getBullsEyeCorners(Point pCenter) throws NotFoundException {
        Point pina = pCenter;
        Point pinb = pCenter;
        Point pinc = pCenter;
        Point pind = pCenter;
        boolean color = true;
        this.nbCenterLayers = 1;
        while (true) {
            boolean z = false;
            if (this.nbCenterLayers >= 9) {
                break;
            }
            Point pouta = getFirstDifferent(pina, color, 1, -1);
            Point poutb = getFirstDifferent(pinb, color, 1, 1);
            Point poutc = getFirstDifferent(pinc, color, -1, 1);
            Point poutd = getFirstDifferent(pind, color, -1, -1);
            if (this.nbCenterLayers > 2) {
                float q = (distance(poutd, pouta) * ((float) this.nbCenterLayers)) / (distance(pind, pina) * ((float) (this.nbCenterLayers + 2)));
                if (((double) q) >= 0.75d) {
                    if (((double) q) <= 1.25d) {
                        if (!isWhiteOrBlackRectangle(pouta, poutb, poutc, poutd)) {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            pina = pouta;
            pinb = poutb;
            pinc = poutc;
            pind = poutd;
            if (!color) {
                z = true;
            }
            color = z;
            this.nbCenterLayers++;
        }
        if (this.nbCenterLayers == 5 || this.nbCenterLayers == 7) {
            this.compact = this.nbCenterLayers == 5;
            return expandSquare(new ResultPoint[]{new ResultPoint(((float) pina.getX()) + 0.5f, ((float) pina.getY()) - 0.5f), new ResultPoint(((float) pinb.getX()) + 0.5f, ((float) pinb.getY()) + 0.5f), new ResultPoint(((float) pinc.getX()) - 0.5f, ((float) pinc.getY()) + 0.5f), new ResultPoint(((float) pind.getX()) - 0.5f, ((float) pind.getY()) - 0.5f)}, (float) ((this.nbCenterLayers * 2) - 3), (float) (2 * this.nbCenterLayers));
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private Point getMatrixCenter() {
        ResultPoint pointA;
        ResultPoint pointB;
        ResultPoint pointA2;
        ResultPoint pointD;
        ResultPoint pointC;
        ResultPoint pointB2;
        ResultPoint pointA3;
        ResultPoint pointD2;
        try {
            ResultPoint[] cornerPoints = new WhiteRectangleDetector(this.image).detect();
            pointA2 = cornerPoints[0];
            pointB = cornerPoints[1];
            pointA = cornerPoints[2];
            pointD = cornerPoints[3];
        } catch (NotFoundException e) {
            int cx = this.image.getWidth() / 2;
            int cy = this.image.getHeight() / 2;
            ResultPoint pointA4 = getFirstDifferent(new Point(cx + 7, cy - 7), false, 1, -1).toResultPoint();
            ResultPoint pointB3 = getFirstDifferent(new Point(cx + 7, cy + 7), false, 1, 1).toResultPoint();
            ResultPoint pointC2 = getFirstDifferent(new Point(cx - 7, cy + 7), false, -1, 1).toResultPoint();
            pointD = getFirstDifferent(new Point(cx - 7, cy - 7), false, -1, -1).toResultPoint();
            pointA2 = pointA4;
            pointB = pointB3;
            pointA = pointC2;
        }
        int cx2 = MathUtils.round((((pointA2.getX() + pointD.getX()) + pointB.getX()) + pointA.getX()) / 4.0f);
        int cy2 = MathUtils.round((((pointA2.getY() + pointD.getY()) + pointB.getY()) + pointA.getY()) / 4.0f);
        try {
            ResultPoint[] cornerPoints2 = new WhiteRectangleDetector(this.image, 15, cx2, cy2).detect();
            pointA3 = cornerPoints2[0];
            pointB2 = cornerPoints2[1];
            pointC = cornerPoints2[2];
            pointD2 = cornerPoints2[3];
        } catch (NotFoundException e2) {
            pointA3 = getFirstDifferent(new Point(cx2 + 7, cy2 - 7), false, 1, -1).toResultPoint();
            pointB2 = getFirstDifferent(new Point(cx2 + 7, cy2 + 7), false, 1, 1).toResultPoint();
            pointC = getFirstDifferent(new Point(cx2 - 7, cy2 + 7), false, -1, 1).toResultPoint();
            pointD2 = getFirstDifferent(new Point(cx2 - 7, cy2 - 7), false, -1, -1).toResultPoint();
        }
        return new Point(MathUtils.round((((pointA3.getX() + pointD2.getX()) + pointB2.getX()) + pointC.getX()) / 4.0f), MathUtils.round((((pointA3.getY() + pointD2.getY()) + pointB2.getY()) + pointC.getY()) / 4.0f));
    }

    private ResultPoint[] getMatrixCornerPoints(ResultPoint[] bullsEyeCorners) {
        return expandSquare(bullsEyeCorners, (float) (2 * this.nbCenterLayers), (float) getDimension());
    }

    private BitMatrix sampleGrid(BitMatrix image2, ResultPoint topLeft, ResultPoint topRight, ResultPoint bottomRight, ResultPoint bottomLeft) throws NotFoundException {
        GridSampler sampler = GridSampler.getInstance();
        int dimension = getDimension();
        float low = (((float) dimension) / 2.0f) - ((float) this.nbCenterLayers);
        float high = (((float) dimension) / 2.0f) + ((float) this.nbCenterLayers);
        int i = dimension;
        return sampler.sampleGrid(image2, dimension, dimension, low, low, high, low, high, high, low, high, topLeft.getX(), topLeft.getY(), topRight.getX(), topRight.getY(), bottomRight.getX(), bottomRight.getY(), bottomLeft.getX(), bottomLeft.getY());
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
        Point p12 = new Point(p1.getX() - 3, p1.getY() + 3);
        Point p22 = new Point(p2.getX() - 3, p2.getY() - 3);
        Point p32 = new Point(p3.getX() + 3, p3.getY() - 3);
        Point p42 = new Point(p4.getX() + 3, p4.getY() + 3);
        int cInit = getColor(p42, p12);
        boolean z = false;
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
        float px = (float) p1.getX();
        float py = (float) p1.getY();
        boolean colorModel = this.image.get(p1.getX(), p1.getY());
        boolean z = false;
        int error = 0;
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
        int i2 = 1;
        if (errRatio <= 0.1f) {
            z = true;
        }
        if (z != colorModel) {
            i2 = -1;
        }
        return i2;
    }

    private Point getFirstDifferent(Point init, boolean color, int dx, int dy) {
        int x = init.getX() + dx;
        int y = init.getY();
        while (true) {
            y += dy;
            if (!isValid(x, y) || this.image.get(x, y) != color) {
                int x2 = x - dx;
                int y2 = y - dy;
            } else {
                x += dx;
            }
        }
        int x22 = x - dx;
        int y22 = y - dy;
        while (isValid(x22, y22) && this.image.get(x22, y22) == color) {
            x22 += dx;
        }
        int x3 = x22 - dx;
        while (isValid(x3, y22) && this.image.get(x3, y22) == color) {
            y22 += dy;
        }
        return new Point(x3, y22 - dy);
    }

    private static ResultPoint[] expandSquare(ResultPoint[] cornerPoints, float oldSide, float newSide) {
        float ratio = newSide / (2.0f * oldSide);
        float dx = cornerPoints[0].getX() - cornerPoints[2].getX();
        float dy = cornerPoints[0].getY() - cornerPoints[2].getY();
        float centerx = (cornerPoints[0].getX() + cornerPoints[2].getX()) / 2.0f;
        float centery = (cornerPoints[0].getY() + cornerPoints[2].getY()) / 2.0f;
        ResultPoint result0 = new ResultPoint((ratio * dx) + centerx, (ratio * dy) + centery);
        ResultPoint result2 = new ResultPoint(centerx - (ratio * dx), centery - (ratio * dy));
        float dx2 = cornerPoints[1].getX() - cornerPoints[3].getX();
        float dy2 = cornerPoints[1].getY() - cornerPoints[3].getY();
        float centerx2 = (cornerPoints[1].getX() + cornerPoints[3].getX()) / 2.0f;
        float centery2 = (cornerPoints[1].getY() + cornerPoints[3].getY()) / 2.0f;
        return new ResultPoint[]{result0, new ResultPoint((ratio * dx2) + centerx2, (ratio * dy2) + centery2), result2, new ResultPoint(centerx2 - (ratio * dx2), centery2 - (ratio * dy2))};
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
            return (4 * this.nbLayers) + 11;
        }
        if (this.nbLayers <= 4) {
            return (4 * this.nbLayers) + 15;
        }
        return (this.nbLayers * 4) + (2 * (((this.nbLayers - 4) / 8) + 1)) + 15;
    }
}
