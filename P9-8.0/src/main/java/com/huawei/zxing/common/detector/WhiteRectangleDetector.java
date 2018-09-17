package com.huawei.zxing.common.detector;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;

public final class WhiteRectangleDetector {
    private static final int CORR = 1;
    private static final int INIT_SIZE = 30;
    private final int downInit;
    private final int height;
    private final BitMatrix image;
    private final int leftInit;
    private final int rightInit;
    private final int upInit;
    private final int width;

    public WhiteRectangleDetector(BitMatrix image) throws NotFoundException {
        this.image = image;
        this.height = image.getHeight();
        this.width = image.getWidth();
        this.leftInit = (this.width - 30) >> 1;
        this.rightInit = (this.width + 30) >> 1;
        this.upInit = (this.height - 30) >> 1;
        this.downInit = (this.height + 30) >> 1;
        if (this.upInit < 0 || this.leftInit < 0 || this.downInit >= this.height || this.rightInit >= this.width) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    public WhiteRectangleDetector(BitMatrix image, int initSize, int x, int y) throws NotFoundException {
        this.image = image;
        this.height = image.getHeight();
        this.width = image.getWidth();
        int halfsize = initSize >> 1;
        this.leftInit = x - halfsize;
        this.rightInit = x + halfsize;
        this.upInit = y - halfsize;
        this.downInit = y + halfsize;
        if (this.upInit < 0 || this.leftInit < 0 || this.downInit >= this.height || this.rightInit >= this.width) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    public ResultPoint[] detect() throws NotFoundException {
        int left = this.leftInit;
        int right = this.rightInit;
        int up = this.upInit;
        int down = this.downInit;
        boolean sizeExceeded = false;
        boolean aBlackPointFoundOnBorder = true;
        boolean atLeastOneBlackPointFoundOnBorder = false;
        while (aBlackPointFoundOnBorder) {
            aBlackPointFoundOnBorder = false;
            boolean rightBorderNotWhite = true;
            while (rightBorderNotWhite && right < this.width) {
                rightBorderNotWhite = containsBlackPoint(up, down, right, false);
                if (rightBorderNotWhite) {
                    right++;
                    aBlackPointFoundOnBorder = true;
                }
            }
            if (right >= this.width) {
                sizeExceeded = true;
                break;
            }
            boolean bottomBorderNotWhite = true;
            while (bottomBorderNotWhite && down < this.height) {
                bottomBorderNotWhite = containsBlackPoint(left, right, down, true);
                if (bottomBorderNotWhite) {
                    down++;
                    aBlackPointFoundOnBorder = true;
                }
            }
            if (down >= this.height) {
                sizeExceeded = true;
                break;
            }
            boolean leftBorderNotWhite = true;
            while (leftBorderNotWhite && left >= 0) {
                leftBorderNotWhite = containsBlackPoint(up, down, left, false);
                if (leftBorderNotWhite) {
                    left--;
                    aBlackPointFoundOnBorder = true;
                }
            }
            if (left < 0) {
                sizeExceeded = true;
                break;
            }
            boolean topBorderNotWhite = true;
            while (topBorderNotWhite && up >= 0) {
                topBorderNotWhite = containsBlackPoint(left, right, up, true);
                if (topBorderNotWhite) {
                    up--;
                    aBlackPointFoundOnBorder = true;
                }
            }
            if (up < 0) {
                sizeExceeded = true;
                break;
            } else if (aBlackPointFoundOnBorder) {
                atLeastOneBlackPointFoundOnBorder = true;
            }
        }
        if (sizeExceeded || !atLeastOneBlackPointFoundOnBorder) {
            throw NotFoundException.getNotFoundInstance();
        }
        int i;
        int maxSize = right - left;
        ResultPoint z = null;
        for (i = 1; i < maxSize; i++) {
            z = getBlackPointOnSegment((float) left, (float) (down - i), (float) (left + i), (float) down);
            if (z != null) {
                break;
            }
        }
        if (z == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        ResultPoint t = null;
        for (i = 1; i < maxSize; i++) {
            t = getBlackPointOnSegment((float) left, (float) (up + i), (float) (left + i), (float) up);
            if (t != null) {
                break;
            }
        }
        if (t == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        ResultPoint x = null;
        for (i = 1; i < maxSize; i++) {
            x = getBlackPointOnSegment((float) right, (float) (up + i), (float) (right - i), (float) up);
            if (x != null) {
                break;
            }
        }
        if (x == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        ResultPoint y = null;
        for (i = 1; i < maxSize; i++) {
            y = getBlackPointOnSegment((float) right, (float) (down - i), (float) (right - i), (float) down);
            if (y != null) {
                break;
            }
        }
        if (y != null) {
            return centerEdges(y, z, x, t);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private ResultPoint getBlackPointOnSegment(float aX, float aY, float bX, float bY) {
        int dist = MathUtils.round(MathUtils.distance(aX, aY, bX, bY));
        float xStep = (bX - aX) / ((float) dist);
        float yStep = (bY - aY) / ((float) dist);
        for (int i = 0; i < dist; i++) {
            int x = MathUtils.round((((float) i) * xStep) + aX);
            int y = MathUtils.round((((float) i) * yStep) + aY);
            if (this.image.get(x, y)) {
                return new ResultPoint((float) x, (float) y);
            }
        }
        return null;
    }

    private ResultPoint[] centerEdges(ResultPoint y, ResultPoint z, ResultPoint x, ResultPoint t) {
        float yi = y.getX();
        float yj = y.getY();
        float zi = z.getX();
        float zj = z.getY();
        float xi = x.getX();
        float xj = x.getY();
        float ti = t.getX();
        float tj = t.getY();
        if (yi < ((float) this.width) / 2.0f) {
            return new ResultPoint[]{new ResultPoint(ti - 1.0f, 1.0f + tj), new ResultPoint(1.0f + zi, 1.0f + zj), new ResultPoint(xi - 1.0f, xj - 1.0f), new ResultPoint(1.0f + yi, yj - 1.0f)};
        }
        return new ResultPoint[]{new ResultPoint(1.0f + ti, 1.0f + tj), new ResultPoint(1.0f + zi, zj - 1.0f), new ResultPoint(xi - 1.0f, 1.0f + xj), new ResultPoint(yi - 1.0f, yj - 1.0f)};
    }

    private boolean containsBlackPoint(int a, int b, int fixed, boolean horizontal) {
        if (horizontal) {
            for (int x = a; x <= b; x++) {
                if (this.image.get(x, fixed)) {
                    return true;
                }
            }
        } else {
            for (int y = a; y <= b; y++) {
                if (this.image.get(fixed, y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
