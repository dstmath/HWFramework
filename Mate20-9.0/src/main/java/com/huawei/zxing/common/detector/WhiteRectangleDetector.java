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

    public WhiteRectangleDetector(BitMatrix image2) throws NotFoundException {
        this.image = image2;
        this.height = image2.getHeight();
        this.width = image2.getWidth();
        this.leftInit = (this.width - 30) >> 1;
        this.rightInit = (this.width + 30) >> 1;
        this.upInit = (this.height - 30) >> 1;
        this.downInit = (this.height + 30) >> 1;
        if (this.upInit < 0 || this.leftInit < 0 || this.downInit >= this.height || this.rightInit >= this.width) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    public WhiteRectangleDetector(BitMatrix image2, int initSize, int x, int y) throws NotFoundException {
        this.image = image2;
        this.height = image2.getHeight();
        this.width = image2.getWidth();
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
        int down;
        int right;
        int left = this.leftInit;
        int right2 = this.rightInit;
        int up = this.upInit;
        int down2 = this.downInit;
        boolean sizeExceeded = false;
        boolean aBlackPointFoundOnBorder = true;
        int left2 = left;
        boolean atLeastOneBlackPointFoundOnBorder = false;
        while (true) {
            if (!aBlackPointFoundOnBorder) {
                down = down2;
                boolean z = aBlackPointFoundOnBorder;
                right = right2;
                break;
            }
            boolean aBlackPointFoundOnBorder2 = false;
            right = right2;
            boolean rightBorderNotWhite = true;
            while (rightBorderNotWhite && right < this.width) {
                rightBorderNotWhite = containsBlackPoint(up, down2, right, false);
                if (rightBorderNotWhite) {
                    right++;
                    aBlackPointFoundOnBorder2 = true;
                }
            }
            if (right >= this.width) {
                sizeExceeded = true;
                boolean z2 = aBlackPointFoundOnBorder2;
                down = down2;
                break;
            }
            boolean aBlackPointFoundOnBorder3 = aBlackPointFoundOnBorder2;
            down = down2;
            boolean bottomBorderNotWhite = true;
            while (bottomBorderNotWhite && down < this.height) {
                bottomBorderNotWhite = containsBlackPoint(left2, right, down, true);
                if (bottomBorderNotWhite) {
                    down++;
                    aBlackPointFoundOnBorder3 = true;
                }
            }
            if (down >= this.height) {
                sizeExceeded = true;
                boolean z3 = aBlackPointFoundOnBorder3;
                break;
            }
            boolean aBlackPointFoundOnBorder4 = aBlackPointFoundOnBorder3;
            int left3 = left2;
            boolean leftBorderNotWhite = true;
            while (leftBorderNotWhite && left3 >= 0) {
                leftBorderNotWhite = containsBlackPoint(up, down, left3, false);
                if (leftBorderNotWhite) {
                    left3--;
                    aBlackPointFoundOnBorder4 = true;
                }
            }
            if (left3 < 0) {
                sizeExceeded = true;
                left2 = left3;
                boolean z4 = aBlackPointFoundOnBorder4;
                break;
            }
            boolean aBlackPointFoundOnBorder5 = aBlackPointFoundOnBorder4;
            int up2 = up;
            boolean topBorderNotWhite = true;
            while (topBorderNotWhite && up2 >= 0) {
                topBorderNotWhite = containsBlackPoint(left3, right, up2, true);
                if (topBorderNotWhite) {
                    up2--;
                    aBlackPointFoundOnBorder5 = true;
                }
            }
            if (up2 < 0) {
                sizeExceeded = true;
                left2 = left3;
                up = up2;
                break;
            }
            if (aBlackPointFoundOnBorder5) {
                atLeastOneBlackPointFoundOnBorder = true;
            }
            right2 = right;
            down2 = down;
            left2 = left3;
            up = up2;
            aBlackPointFoundOnBorder = aBlackPointFoundOnBorder5;
        }
        if (sizeExceeded || !atLeastOneBlackPointFoundOnBorder) {
            throw NotFoundException.getNotFoundInstance();
        }
        int maxSize = right - left2;
        ResultPoint z5 = null;
        for (int i = 1; i < maxSize; i++) {
            z5 = getBlackPointOnSegment((float) left2, (float) (down - i), (float) (left2 + i), (float) down);
            if (z5 != null) {
                break;
            }
        }
        if (z5 != null) {
            ResultPoint t = null;
            for (int i2 = 1; i2 < maxSize; i2++) {
                t = getBlackPointOnSegment((float) left2, (float) (up + i2), (float) (left2 + i2), (float) up);
                if (t != null) {
                    break;
                }
            }
            if (t != null) {
                ResultPoint x = null;
                int i3 = 1;
                while (true) {
                    if (i3 >= maxSize) {
                        break;
                    }
                    boolean atLeastOneBlackPointFoundOnBorder2 = atLeastOneBlackPointFoundOnBorder;
                    x = getBlackPointOnSegment((float) right, (float) (up + i3), (float) (right - i3), (float) up);
                    if (x != null) {
                        break;
                    }
                    i3++;
                    atLeastOneBlackPointFoundOnBorder = atLeastOneBlackPointFoundOnBorder2;
                }
                if (x != null) {
                    ResultPoint y = null;
                    int i4 = 1;
                    while (true) {
                        int i5 = i4;
                        if (i5 >= maxSize) {
                            ResultPoint resultPoint = y;
                            break;
                        }
                        ResultPoint resultPoint2 = y;
                        y = getBlackPointOnSegment((float) right, (float) (down - i5), (float) (right - i5), (float) down);
                        if (y != null) {
                            break;
                        }
                        i4 = i5 + 1;
                    }
                    if (y != null) {
                        return centerEdges(y, z5, x, t);
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
                throw NotFoundException.getNotFoundInstance();
            }
            throw NotFoundException.getNotFoundInstance();
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
            return new ResultPoint[]{new ResultPoint(ti - 1.0f, tj + 1.0f), new ResultPoint(zi + 1.0f, zj + 1.0f), new ResultPoint(xi - 1.0f, xj - 1.0f), new ResultPoint(yi + 1.0f, yj - 1.0f)};
        }
        return new ResultPoint[]{new ResultPoint(ti + 1.0f, tj + 1.0f), new ResultPoint(zi + 1.0f, zj - 1.0f), new ResultPoint(xi - 1.0f, xj + 1.0f), new ResultPoint(yi - 1.0f, yj - 1.0f)};
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
