package com.huawei.zxing.datamatrix.detector;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DetectorResult;
import com.huawei.zxing.common.GridSampler;
import com.huawei.zxing.common.detector.MathUtils;
import com.huawei.zxing.common.detector.WhiteRectangleDetector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class Detector {
    private final BitMatrix image;
    private final WhiteRectangleDetector rectangleDetector;

    private static final class ResultPointsAndTransitions {
        private final ResultPoint from;
        private final ResultPoint to;
        private final int transitions;

        private ResultPointsAndTransitions(ResultPoint from2, ResultPoint to2, int transitions2) {
            this.from = from2;
            this.to = to2;
            this.transitions = transitions2;
        }

        /* access modifiers changed from: package-private */
        public ResultPoint getFrom() {
            return this.from;
        }

        /* access modifiers changed from: package-private */
        public ResultPoint getTo() {
            return this.to;
        }

        public int getTransitions() {
            return this.transitions;
        }

        public String toString() {
            return this.from + "/" + this.to + '/' + this.transitions;
        }
    }

    private static final class ResultPointsAndTransitionsComparator implements Comparator<ResultPointsAndTransitions>, Serializable {
        private ResultPointsAndTransitionsComparator() {
        }

        public int compare(ResultPointsAndTransitions o1, ResultPointsAndTransitions o2) {
            return o1.getTransitions() - o2.getTransitions();
        }
    }

    public Detector(BitMatrix image2) throws NotFoundException {
        this.image = image2;
        this.rectangleDetector = new WhiteRectangleDetector(image2);
    }

    public DetectorResult detect() throws NotFoundException {
        ResultPoint topRight;
        int i;
        BitMatrix bits;
        ResultPoint correctedTopRight;
        int dimensionTop;
        ResultPoint topRight2;
        ResultPoint[] cornerPoints = this.rectangleDetector.detect();
        ResultPoint pointA = cornerPoints[0];
        ResultPoint pointB = cornerPoints[1];
        ResultPoint pointC = cornerPoints[2];
        ResultPoint pointD = cornerPoints[3];
        ArrayList arrayList = new ArrayList(4);
        arrayList.add(transitionsBetween(pointA, pointB));
        arrayList.add(transitionsBetween(pointA, pointC));
        arrayList.add(transitionsBetween(pointB, pointD));
        arrayList.add(transitionsBetween(pointC, pointD));
        Collections.sort(arrayList, new ResultPointsAndTransitionsComparator());
        ResultPointsAndTransitions lSideOne = (ResultPointsAndTransitions) arrayList.get(0);
        ResultPointsAndTransitions lSideTwo = (ResultPointsAndTransitions) arrayList.get(1);
        Map<ResultPoint, Integer> hashMap = new HashMap<>();
        increment(hashMap, lSideOne.getFrom());
        increment(hashMap, lSideOne.getTo());
        increment(hashMap, lSideTwo.getFrom());
        increment(hashMap, lSideTwo.getTo());
        ResultPoint maybeBottomRight = null;
        ResultPoint maybeTopLeft = null;
        ResultPoint bottomLeft = null;
        for (Map.Entry<ResultPoint, Integer> entry : hashMap.entrySet()) {
            ResultPoint point = entry.getKey();
            if (entry.getValue().intValue() == 2) {
                bottomLeft = point;
            } else if (maybeTopLeft == null) {
                maybeTopLeft = point;
            } else {
                maybeBottomRight = point;
            }
        }
        if (maybeTopLeft == null || bottomLeft == null || maybeBottomRight == null) {
            Map<ResultPoint, Integer> pointCount = hashMap;
            ResultPointsAndTransitions resultPointsAndTransitions = lSideTwo;
            ResultPointsAndTransitions resultPointsAndTransitions2 = lSideOne;
            ArrayList arrayList2 = arrayList;
            ResultPoint resultPoint = pointD;
            ResultPoint[] resultPointArr = cornerPoints;
            throw NotFoundException.getNotFoundInstance();
        }
        ResultPoint[] corners = {maybeTopLeft, bottomLeft, maybeBottomRight};
        ResultPoint.orderBestPatterns(corners);
        ResultPoint bottomRight = corners[0];
        ResultPoint bottomLeft2 = corners[1];
        ResultPoint topLeft = corners[2];
        if (!hashMap.containsKey(pointA)) {
            topRight = pointA;
        } else if (!hashMap.containsKey(pointB)) {
            topRight = pointB;
        } else if (!hashMap.containsKey(pointC)) {
            topRight = pointC;
        } else {
            topRight = pointD;
        }
        ResultPoint topRight3 = topRight;
        int dimensionTop2 = transitionsBetween(topLeft, topRight3).getTransitions();
        int dimensionRight = transitionsBetween(bottomRight, topRight3).getTransitions();
        HashMap hashMap2 = hashMap;
        ResultPointsAndTransitions resultPointsAndTransitions3 = lSideTwo;
        if ((dimensionTop2 & 1) == 1) {
            dimensionTop2++;
        }
        int dimensionTop3 = dimensionTop2 + 2;
        if ((dimensionRight & 1) == 1) {
            dimensionRight++;
        }
        int dimensionRight2 = dimensionRight + 2;
        ResultPointsAndTransitions resultPointsAndTransitions4 = lSideOne;
        if (4 * dimensionTop3 >= 7 * dimensionRight2) {
            dimensionTop = dimensionTop3;
            ArrayList arrayList3 = arrayList;
            ResultPoint[] resultPointArr2 = cornerPoints;
            i = 4;
            topRight2 = topRight3;
        } else if (4 * dimensionRight2 >= 7 * dimensionTop3) {
            dimensionTop = dimensionTop3;
            ArrayList arrayList4 = arrayList;
            ResultPoint[] resultPointArr3 = cornerPoints;
            i = 4;
            topRight2 = topRight3;
        } else {
            int i2 = dimensionTop3;
            ArrayList arrayList5 = arrayList;
            ResultPoint[] resultPointArr4 = cornerPoints;
            i = 4;
            ResultPoint topRight4 = topRight3;
            correctedTopRight = correctTopRight(bottomLeft2, bottomRight, topLeft, topRight3, Math.min(dimensionRight2, dimensionTop3));
            if (correctedTopRight == null) {
                correctedTopRight = topRight4;
            }
            int dimensionCorrected = Math.max(transitionsBetween(topLeft, correctedTopRight).getTransitions(), transitionsBetween(bottomRight, correctedTopRight).getTransitions()) + 1;
            if ((dimensionCorrected & 1) == 1) {
                dimensionCorrected++;
            }
            bits = sampleGrid(this.image, topLeft, bottomLeft2, bottomRight, correctedTopRight, dimensionCorrected, dimensionCorrected);
            ResultPoint resultPoint2 = pointD;
            ResultPoint[] resultPointArr5 = new ResultPoint[i];
            resultPointArr5[0] = topLeft;
            resultPointArr5[1] = bottomLeft2;
            resultPointArr5[2] = bottomRight;
            resultPointArr5[3] = correctedTopRight;
            return new DetectorResult(bits, resultPointArr5);
        }
        ResultPoint resultPoint3 = pointD;
        ResultPoint correctedTopRight2 = correctTopRightRectangular(bottomLeft2, bottomRight, topLeft, topRight2, dimensionTop, dimensionRight2);
        if (correctedTopRight2 == null) {
            correctedTopRight2 = topRight2;
        }
        int dimensionTop4 = transitionsBetween(topLeft, correctedTopRight).getTransitions();
        int dimensionRight3 = transitionsBetween(bottomRight, correctedTopRight).getTransitions();
        if ((dimensionTop4 & 1) == 1) {
            dimensionTop4++;
        }
        if ((dimensionRight3 & 1) == 1) {
            dimensionRight3++;
        }
        int i3 = dimensionTop4;
        int i4 = dimensionRight3;
        bits = sampleGrid(this.image, topLeft, bottomLeft2, bottomRight, correctedTopRight, dimensionTop4, dimensionRight3);
        ResultPoint[] resultPointArr52 = new ResultPoint[i];
        resultPointArr52[0] = topLeft;
        resultPointArr52[1] = bottomLeft2;
        resultPointArr52[2] = bottomRight;
        resultPointArr52[3] = correctedTopRight;
        return new DetectorResult(bits, resultPointArr52);
    }

    private ResultPoint correctTopRightRectangular(ResultPoint bottomLeft, ResultPoint bottomRight, ResultPoint topLeft, ResultPoint topRight, int dimensionTop, int dimensionRight) {
        float corr = ((float) distance(bottomLeft, bottomRight)) / ((float) dimensionTop);
        int norm = distance(topLeft, topRight);
        ResultPoint c1 = new ResultPoint(topRight.getX() + (corr * ((topRight.getX() - topLeft.getX()) / ((float) norm))), topRight.getY() + (corr * ((topRight.getY() - topLeft.getY()) / ((float) norm))));
        float corr2 = ((float) distance(bottomLeft, topLeft)) / ((float) dimensionRight);
        int norm2 = distance(bottomRight, topRight);
        ResultPoint c2 = new ResultPoint(topRight.getX() + (corr2 * ((topRight.getX() - bottomRight.getX()) / ((float) norm2))), topRight.getY() + (corr2 * ((topRight.getY() - bottomRight.getY()) / ((float) norm2))));
        if (!isValid(c1)) {
            if (isValid(c2)) {
                return c2;
            }
            return null;
        } else if (isValid(c2) && Math.abs(dimensionTop - transitionsBetween(topLeft, c1).getTransitions()) + Math.abs(dimensionRight - transitionsBetween(bottomRight, c1).getTransitions()) > Math.abs(dimensionTop - transitionsBetween(topLeft, c2).getTransitions()) + Math.abs(dimensionRight - transitionsBetween(bottomRight, c2).getTransitions())) {
            return c2;
        } else {
            return c1;
        }
    }

    private ResultPoint correctTopRight(ResultPoint bottomLeft, ResultPoint bottomRight, ResultPoint topLeft, ResultPoint topRight, int dimension) {
        float corr = ((float) distance(bottomLeft, bottomRight)) / ((float) dimension);
        int norm = distance(topLeft, topRight);
        ResultPoint c1 = new ResultPoint(topRight.getX() + (corr * ((topRight.getX() - topLeft.getX()) / ((float) norm))), topRight.getY() + (corr * ((topRight.getY() - topLeft.getY()) / ((float) norm))));
        float corr2 = ((float) distance(bottomLeft, topLeft)) / ((float) dimension);
        int norm2 = distance(bottomRight, topRight);
        ResultPoint c2 = new ResultPoint(topRight.getX() + (corr2 * ((topRight.getX() - bottomRight.getX()) / ((float) norm2))), topRight.getY() + (corr2 * ((topRight.getY() - bottomRight.getY()) / ((float) norm2))));
        if (!isValid(c1)) {
            if (isValid(c2)) {
                return c2;
            }
            return null;
        } else if (!isValid(c2)) {
            return c1;
        } else {
            return Math.abs(transitionsBetween(topLeft, c1).getTransitions() - transitionsBetween(bottomRight, c1).getTransitions()) <= Math.abs(transitionsBetween(topLeft, c2).getTransitions() - transitionsBetween(bottomRight, c2).getTransitions()) ? c1 : c2;
        }
    }

    private boolean isValid(ResultPoint p) {
        return p.getX() >= 0.0f && p.getX() < ((float) this.image.getWidth()) && p.getY() > 0.0f && p.getY() < ((float) this.image.getHeight());
    }

    private static int distance(ResultPoint a, ResultPoint b) {
        return MathUtils.round(ResultPoint.distance(a, b));
    }

    private static void increment(Map<ResultPoint, Integer> table, ResultPoint key) {
        Integer value = table.get(key);
        int i = 1;
        if (value != null) {
            i = 1 + value.intValue();
        }
        table.put(key, Integer.valueOf(i));
    }

    private static BitMatrix sampleGrid(BitMatrix image2, ResultPoint topLeft, ResultPoint bottomLeft, ResultPoint bottomRight, ResultPoint topRight, int dimensionX, int dimensionY) throws NotFoundException {
        int i = dimensionX;
        int i2 = dimensionY;
        return GridSampler.getInstance().sampleGrid(image2, i, i2, 0.5f, 0.5f, ((float) i) - 0.5f, 0.5f, ((float) i) - 0.5f, ((float) i2) - 0.5f, 0.5f, ((float) i2) - 0.5f, topLeft.getX(), topLeft.getY(), topRight.getX(), topRight.getY(), bottomRight.getX(), bottomRight.getY(), bottomLeft.getX(), bottomLeft.getY());
    }

    private ResultPointsAndTransitions transitionsBetween(ResultPoint from, ResultPoint to) {
        int fromX;
        int fromX2;
        Detector detector = this;
        int fromX3 = (int) from.getX();
        int fromY = (int) from.getY();
        int toX = (int) to.getX();
        int toY = (int) to.getY();
        int xstep = 1;
        boolean steep = Math.abs(toY - fromY) > Math.abs(toX - fromX3);
        if (steep) {
            int temp = fromX3;
            fromX3 = fromY;
            fromY = temp;
            int temp2 = toX;
            toX = toY;
            toY = temp2;
        }
        int dx = Math.abs(toX - fromX3);
        int dy = Math.abs(toY - fromY);
        int error = (-dx) >> 1;
        int ystep = fromY < toY ? 1 : -1;
        if (fromX3 >= toX) {
            xstep = -1;
        }
        int transitions = 0;
        boolean inBlack = detector.image.get(steep ? fromY : fromX3, steep ? fromX3 : fromY);
        int x = fromX3;
        int error2 = error;
        int y = fromY;
        while (true) {
            if (x == toX) {
                break;
            }
            BitMatrix bitMatrix = detector.image;
            int i = steep ? y : x;
            if (steep) {
                fromX = fromX3;
                fromX2 = x;
            } else {
                fromX = fromX3;
                fromX2 = y;
            }
            boolean isBlack = bitMatrix.get(i, fromX2);
            if (isBlack != inBlack) {
                transitions++;
                inBlack = isBlack;
            }
            error2 += dy;
            if (error2 > 0) {
                if (y == toY) {
                    break;
                }
                y += ystep;
                error2 -= dx;
            }
            x += xstep;
            fromX3 = fromX;
            detector = this;
        }
        return new ResultPointsAndTransitions(from, to, transitions);
    }
}
