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
import java.util.List;
import java.util.Map;

public final class Detector {
    private final BitMatrix image;
    private final WhiteRectangleDetector rectangleDetector;

    public Detector(BitMatrix image2) throws NotFoundException {
        this.image = image2;
        this.rectangleDetector = new WhiteRectangleDetector(image2);
    }

    public DetectorResult detect() throws NotFoundException {
        ResultPoint topRight;
        ResultPoint bottomRight;
        ResultPoint topLeft;
        int i;
        BitMatrix bits;
        ResultPoint correctedTopRight;
        ResultPoint topRight2;
        int dimensionRight;
        ResultPoint bottomRight2;
        ResultPoint[] cornerPoints = this.rectangleDetector.detect();
        ResultPoint pointA = cornerPoints[0];
        ResultPoint pointB = cornerPoints[1];
        ResultPoint pointC = cornerPoints[2];
        ResultPoint pointD = cornerPoints[3];
        List<ResultPointsAndTransitions> transitions = new ArrayList<>(4);
        transitions.add(transitionsBetween(pointA, pointB));
        transitions.add(transitionsBetween(pointA, pointC));
        transitions.add(transitionsBetween(pointB, pointD));
        transitions.add(transitionsBetween(pointC, pointD));
        Collections.sort(transitions, new ResultPointsAndTransitionsComparator());
        ResultPointsAndTransitions lSideOne = transitions.get(0);
        ResultPointsAndTransitions lSideTwo = transitions.get(1);
        Map<ResultPoint, Integer> pointCount = new HashMap<>();
        increment(pointCount, lSideOne.getFrom());
        increment(pointCount, lSideOne.getTo());
        increment(pointCount, lSideTwo.getFrom());
        increment(pointCount, lSideTwo.getTo());
        ResultPoint bottomLeft = null;
        ResultPoint maybeTopLeft = null;
        ResultPoint maybeBottomRight = null;
        for (Map.Entry<ResultPoint, Integer> entry : pointCount.entrySet()) {
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
            throw NotFoundException.getNotFoundInstance();
        }
        ResultPoint[] corners = {maybeTopLeft, bottomLeft, maybeBottomRight};
        ResultPoint.orderBestPatterns(corners);
        ResultPoint bottomRight3 = corners[0];
        ResultPoint bottomLeft2 = corners[1];
        ResultPoint topLeft2 = corners[2];
        if (!pointCount.containsKey(pointA)) {
            topRight = pointA;
        } else if (!pointCount.containsKey(pointB)) {
            topRight = pointB;
        } else if (!pointCount.containsKey(pointC)) {
            topRight = pointC;
        } else {
            topRight = pointD;
        }
        int dimensionTop = transitionsBetween(topLeft2, topRight).getTransitions();
        int dimensionRight2 = transitionsBetween(bottomRight3, topRight).getTransitions();
        if ((dimensionTop & 1) == 1) {
            dimensionTop++;
        }
        int dimensionTop2 = dimensionTop + 2;
        if ((dimensionRight2 & 1) == 1) {
            dimensionRight2++;
        }
        int dimensionRight3 = dimensionRight2 + 2;
        if (dimensionTop2 * 4 >= dimensionRight3 * 7) {
            dimensionRight = dimensionRight3;
            topRight2 = topRight;
            bottomRight = bottomRight3;
            i = 4;
            bottomRight2 = topLeft2;
        } else if (dimensionRight3 * 4 >= dimensionTop2 * 7) {
            dimensionRight = dimensionRight3;
            topRight2 = topRight;
            bottomRight = bottomRight3;
            i = 4;
            bottomRight2 = topLeft2;
        } else {
            bottomRight = bottomRight3;
            i = 4;
            correctedTopRight = correctTopRight(bottomLeft2, bottomRight3, topLeft2, topRight, Math.min(dimensionRight3, dimensionTop2));
            if (correctedTopRight == null) {
                correctedTopRight = topRight;
            }
            int dimensionCorrected = Math.max(transitionsBetween(topLeft2, correctedTopRight).getTransitions(), transitionsBetween(bottomRight, correctedTopRight).getTransitions()) + 1;
            if ((dimensionCorrected & 1) == 1) {
                dimensionCorrected++;
            }
            bits = sampleGrid(this.image, topLeft2, bottomLeft2, bottomRight, correctedTopRight, dimensionCorrected, dimensionCorrected);
            topLeft = topLeft2;
            ResultPoint[] resultPointArr = new ResultPoint[i];
            resultPointArr[0] = topLeft;
            resultPointArr[1] = bottomLeft2;
            resultPointArr[2] = bottomRight;
            resultPointArr[3] = correctedTopRight;
            return new DetectorResult(bits, resultPointArr);
        }
        topLeft = bottomRight2;
        correctedTopRight = correctTopRightRectangular(bottomLeft2, bottomRight, bottomRight2, topRight2, dimensionTop2, dimensionRight);
        if (correctedTopRight == null) {
            correctedTopRight = topRight2;
        }
        int dimensionTop3 = transitionsBetween(topLeft, correctedTopRight).getTransitions();
        int dimensionRight4 = transitionsBetween(bottomRight, correctedTopRight).getTransitions();
        if ((dimensionTop3 & 1) == 1) {
            dimensionTop3++;
        }
        if ((dimensionRight4 & 1) == 1) {
            dimensionRight4++;
        }
        bits = sampleGrid(this.image, topLeft, bottomLeft2, bottomRight, correctedTopRight, dimensionTop3, dimensionRight4);
        ResultPoint[] resultPointArr2 = new ResultPoint[i];
        resultPointArr2[0] = topLeft;
        resultPointArr2[1] = bottomLeft2;
        resultPointArr2[2] = bottomRight;
        resultPointArr2[3] = correctedTopRight;
        return new DetectorResult(bits, resultPointArr2);
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
        return GridSampler.getInstance().sampleGrid(image2, dimensionX, dimensionY, 0.5f, 0.5f, ((float) dimensionX) - 0.5f, 0.5f, ((float) dimensionX) - 0.5f, ((float) dimensionY) - 0.5f, 0.5f, ((float) dimensionY) - 0.5f, topLeft.getX(), topLeft.getY(), topRight.getX(), topRight.getY(), bottomRight.getX(), bottomRight.getY(), bottomLeft.getX(), bottomLeft.getY());
    }

    private ResultPointsAndTransitions transitionsBetween(ResultPoint from, ResultPoint to) {
        Detector detector = this;
        int fromX = (int) from.getX();
        int fromY = (int) from.getY();
        int toX = (int) to.getX();
        int toY = (int) to.getY();
        int xstep = 1;
        boolean steep = Math.abs(toY - fromY) > Math.abs(toX - fromX);
        if (steep) {
            fromX = fromY;
            fromY = fromX;
            toX = toY;
            toY = toX;
        }
        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);
        int error = (-dx) >> 1;
        int ystep = fromY < toY ? 1 : -1;
        if (fromX >= toX) {
            xstep = -1;
        }
        int transitions = 0;
        boolean inBlack = detector.image.get(steep ? fromY : fromX, steep ? fromX : fromY);
        int x = fromX;
        int y = fromY;
        while (true) {
            if (x == toX) {
                break;
            }
            boolean isBlack = detector.image.get(steep ? y : x, steep ? x : y);
            if (isBlack != inBlack) {
                transitions++;
                inBlack = isBlack;
            }
            error += dy;
            if (error > 0) {
                if (y == toY) {
                    break;
                }
                y += ystep;
                error -= dx;
            }
            x += xstep;
            detector = this;
            fromX = fromX;
        }
        return new ResultPointsAndTransitions(from, to, transitions);
    }

    /* access modifiers changed from: private */
    public static final class ResultPointsAndTransitions {
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

    /* access modifiers changed from: private */
    public static final class ResultPointsAndTransitionsComparator implements Comparator<ResultPointsAndTransitions>, Serializable {
        private ResultPointsAndTransitionsComparator() {
        }

        public int compare(ResultPointsAndTransitions o1, ResultPointsAndTransitions o2) {
            return o1.getTransitions() - o2.getTransitions();
        }
    }
}
