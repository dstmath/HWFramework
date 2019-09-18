package com.huawei.zxing.common.detector;

import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;

public final class MonochromeRectangleDetector {
    private static final int MAX_MODULES = 32;
    private final BitMatrix image;

    public MonochromeRectangleDetector(BitMatrix image2) {
        this.image = image2;
    }

    public ResultPoint[] detect() throws NotFoundException {
        int height = this.image.getHeight();
        int width = this.image.getWidth();
        int halfHeight = height >> 1;
        int halfWidth = width >> 1;
        int deltaY = Math.max(1, height / 256);
        int deltaX = Math.max(1, width / 256);
        int bottom = height;
        ResultPoint pointA = findCornerFromCenter(halfWidth, 0, 0, width, halfHeight, -deltaY, 0, bottom, halfWidth >> 1);
        int top = ((int) pointA.getY()) - 1;
        int deltaY2 = -deltaX;
        int deltaX2 = deltaX;
        int deltaX3 = halfWidth;
        int deltaY3 = deltaY;
        int i = width;
        int i2 = height;
        int height2 = halfHeight;
        int i3 = top;
        int i4 = bottom;
        ResultPoint pointB = findCornerFromCenter(deltaX3, deltaY2, 0, width, height2, 0, i3, i4, halfHeight >> 1);
        int left = ((int) pointB.getX()) - 1;
        int i5 = left;
        ResultPoint pointC = findCornerFromCenter(deltaX3, deltaX2, i5, width, height2, 0, i3, i4, halfHeight >> 1);
        int right = ((int) pointC.getX()) + 1;
        ResultPoint pointD = findCornerFromCenter(deltaX3, 0, i5, right, height2, deltaY3, i3, i4, halfWidth >> 1);
        int i6 = deltaY3;
        ResultPoint resultPoint = pointA;
        return new ResultPoint[]{findCornerFromCenter(halfWidth, 0, left, right, halfHeight, -deltaY3, top, ((int) pointD.getY()) + 1, halfWidth >> 2), pointB, pointC, pointD};
    }

    private ResultPoint findCornerFromCenter(int centerX, int deltaX, int left, int right, int centerY, int deltaY, int top, int bottom, int maxWhiteRun) throws NotFoundException {
        int[] range;
        int i = centerX;
        int y = centerY;
        int[] lastRange = null;
        int y2 = y;
        int x = i;
        while (true) {
            int i2 = bottom;
            if (y2 >= i2) {
                int i3 = right;
                int i4 = top;
                break;
            }
            int i5 = top;
            if (y2 < i5) {
                int i6 = right;
                break;
            }
            int i7 = right;
            if (x >= i7) {
                break;
            }
            int i8 = left;
            if (x < i8) {
                break;
            }
            if (deltaX == 0) {
                range = blackWhiteRange(y2, maxWhiteRun, i8, i7, true);
            } else {
                range = blackWhiteRange(x, maxWhiteRun, i5, i2, false);
            }
            if (range != null) {
                lastRange = range;
                y2 += deltaY;
                x += deltaX;
            } else if (lastRange == null) {
                throw NotFoundException.getNotFoundInstance();
            } else if (deltaX == 0) {
                int lastY = y2 - deltaY;
                if (lastRange[0] >= i) {
                    return new ResultPoint((float) lastRange[1], (float) lastY);
                }
                if (lastRange[1] <= i) {
                    return new ResultPoint((float) lastRange[0], (float) lastY);
                }
                return new ResultPoint((float) (deltaY > 0 ? lastRange[0] : lastRange[1]), (float) lastY);
            } else {
                int lastY2 = x - deltaX;
                if (lastRange[0] >= y) {
                    return new ResultPoint((float) lastY2, (float) lastRange[1]);
                }
                if (lastRange[1] <= y) {
                    return new ResultPoint((float) lastY2, (float) lastRange[0]);
                }
                return new ResultPoint((float) lastY2, (float) (deltaX < 0 ? lastRange[0] : lastRange[1]));
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0022  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0035 A[EDGE_INSN: B:53:0x0035->B:16:0x0035 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0072 A[EDGE_INSN: B:67:0x0072->B:37:0x0072 ?: BREAK  , SYNTHETIC] */
    private int[] blackWhiteRange(int fixedDimension, int maxWhiteRun, int minDim, int maxDim, boolean horizontal) {
        int end;
        int start;
        int center = (minDim + maxDim) >> 1;
        int start2 = center;
        while (true) {
            if (start2 < minDim) {
                break;
            }
            if (!horizontal) {
                start = start2;
                while (true) {
                    start--;
                    if (start < minDim) {
                    }
                }
                int whiteRunSize = start2 - start;
                if (start < minDim) {
                    break;
                }
                break;
            }
            start = start2;
            while (true) {
                start--;
                if (start < minDim) {
                    break;
                } else if (horizontal) {
                    if (this.image.get(start, fixedDimension)) {
                        break;
                    }
                } else if (this.image.get(fixedDimension, start)) {
                    break;
                }
            }
            int whiteRunSize2 = start2 - start;
            if (start < minDim || whiteRunSize2 > maxWhiteRun) {
            } else {
                start2 = start;
            }
            start2--;
        }
        int start3 = start2 + 1;
        int end2 = center;
        while (true) {
            if (end2 >= maxDim) {
                break;
            }
            if (!horizontal) {
                end = end2;
                while (true) {
                    end++;
                    if (end >= maxDim) {
                    }
                }
                int whiteRunSize3 = end - end2;
                if (end >= maxDim) {
                    break;
                }
                break;
            }
            end = end2;
            while (true) {
                end++;
                if (end >= maxDim) {
                    break;
                } else if (horizontal) {
                    if (this.image.get(end, fixedDimension)) {
                        break;
                    }
                } else if (this.image.get(fixedDimension, end)) {
                    break;
                }
            }
            int whiteRunSize32 = end - end2;
            if (end >= maxDim || whiteRunSize32 > maxWhiteRun) {
            } else {
                end2 = end;
            }
            end2++;
        }
        int end3 = end2 - 1;
        if (end3 <= start3) {
            return null;
        }
        return new int[]{start3, end3};
    }
}
