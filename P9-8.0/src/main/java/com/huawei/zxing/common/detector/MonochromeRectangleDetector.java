package com.huawei.zxing.common.detector;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;

public final class MonochromeRectangleDetector {
    private static final int MAX_MODULES = 32;
    private final BitMatrix image;

    public MonochromeRectangleDetector(BitMatrix image) {
        this.image = image;
    }

    public ResultPoint[] detect() throws NotFoundException {
        int height = this.image.getHeight();
        int width = this.image.getWidth();
        int halfHeight = height >> 1;
        int halfWidth = width >> 1;
        int deltaY = Math.max(1, height / 256);
        int deltaX = Math.max(1, width / 256);
        int bottom = height;
        int right = width;
        int top = ((int) findCornerFromCenter(halfWidth, 0, 0, width, halfHeight, -deltaY, 0, height, halfWidth >> 1).getY()) - 1;
        int left = ((int) findCornerFromCenter(halfWidth, -deltaX, 0, width, halfHeight, 0, top, height, halfHeight >> 1).getX()) - 1;
        right = ((int) findCornerFromCenter(halfWidth, deltaX, left, width, halfHeight, 0, top, height, halfHeight >> 1).getX()) + 1;
        ResultPoint pointD = findCornerFromCenter(halfWidth, 0, left, right, halfHeight, deltaY, top, height, halfWidth >> 1);
        int i = -deltaY;
        int i2 = halfWidth;
        int i3 = left;
        int i4 = right;
        int i5 = halfHeight;
        int i6 = top;
        return new ResultPoint[]{findCornerFromCenter(i2, 0, i3, i4, i5, i, i6, ((int) pointD.getY()) + 1, halfWidth >> 2), pointB, pointC, pointD};
    }

    private ResultPoint findCornerFromCenter(int centerX, int deltaX, int left, int right, int centerY, int deltaY, int top, int bottom, int maxWhiteRun) throws NotFoundException {
        int[] lastRange = null;
        int y = centerY;
        int x = centerX;
        while (y < bottom && y >= top && x < right && x >= left) {
            int[] range;
            if (deltaX == 0) {
                range = blackWhiteRange(y, maxWhiteRun, left, right, true);
            } else {
                range = blackWhiteRange(x, maxWhiteRun, top, bottom, false);
            }
            if (range != null) {
                lastRange = range;
                y += deltaY;
                x += deltaX;
            } else if (lastRange == null) {
                throw NotFoundException.getNotFoundInstance();
            } else if (deltaX == 0) {
                int lastY = y - deltaY;
                if (lastRange[0] >= centerX) {
                    return new ResultPoint((float) lastRange[1], (float) lastY);
                }
                if (lastRange[1] <= centerX) {
                    return new ResultPoint((float) lastRange[0], (float) lastY);
                }
                return new ResultPoint((float) (deltaY > 0 ? lastRange[0] : lastRange[1]), (float) lastY);
            } else {
                int lastX = x - deltaX;
                if (lastRange[0] >= centerY) {
                    return new ResultPoint((float) lastX, (float) lastRange[1]);
                }
                if (lastRange[1] <= centerY) {
                    return new ResultPoint((float) lastX, (float) lastRange[0]);
                }
                return new ResultPoint((float) lastX, (float) (deltaX < 0 ? lastRange[0] : lastRange[1]));
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private int[] blackWhiteRange(int fixedDimension, int maxWhiteRun, int minDim, int maxDim, boolean horizontal) {
        int whiteRunStart;
        int i;
        int whiteRunSize;
        int center = (minDim + maxDim) >> 1;
        int start = center;
        while (start >= minDim) {
            if (horizontal ? this.image.get(start, fixedDimension) : this.image.get(fixedDimension, start)) {
                start--;
            } else {
                whiteRunStart = start;
                do {
                    start--;
                    if (start < minDim) {
                        break;
                    } else if (horizontal) {
                        i = this.image.get(start, fixedDimension);
                    } else {
                        i = this.image.get(fixedDimension, start);
                    }
                } while ((i ^ 1) != 0);
                whiteRunSize = whiteRunStart - start;
                if (start < minDim || whiteRunSize > maxWhiteRun) {
                    start = whiteRunStart;
                    break;
                }
            }
        }
        start++;
        int end = center;
        while (end < maxDim) {
            if (horizontal ? this.image.get(end, fixedDimension) : this.image.get(fixedDimension, end)) {
                end++;
            } else {
                whiteRunStart = end;
                do {
                    end++;
                    if (end >= maxDim) {
                        break;
                    } else if (horizontal) {
                        i = this.image.get(end, fixedDimension);
                    } else {
                        i = this.image.get(fixedDimension, end);
                    }
                } while ((i ^ 1) != 0);
                whiteRunSize = end - whiteRunStart;
                if (end >= maxDim || whiteRunSize > maxWhiteRun) {
                    end = whiteRunStart;
                    break;
                }
            }
        }
        if (end - 1 <= start) {
            return null;
        }
        return new int[]{start, end - 1};
    }
}
