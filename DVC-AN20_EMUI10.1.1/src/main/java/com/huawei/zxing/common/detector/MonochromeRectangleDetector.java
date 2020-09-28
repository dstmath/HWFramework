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
        int top = ((int) findCornerFromCenter(halfWidth, 0, 0, width, halfHeight, -deltaY, 0, height, halfWidth >> 1).getY()) - 1;
        ResultPoint pointB = findCornerFromCenter(halfWidth, -deltaX, 0, width, halfHeight, 0, top, height, halfHeight >> 1);
        int left = ((int) pointB.getX()) - 1;
        ResultPoint pointC = findCornerFromCenter(halfWidth, deltaX, left, width, halfHeight, 0, top, height, halfHeight >> 1);
        int right = ((int) pointC.getX()) + 1;
        ResultPoint pointD = findCornerFromCenter(halfWidth, 0, left, right, halfHeight, deltaY, top, height, halfWidth >> 1);
        return new ResultPoint[]{findCornerFromCenter(halfWidth, 0, left, right, halfHeight, -deltaY, top, ((int) pointD.getY()) + 1, halfWidth >> 2), pointB, pointC, pointD};
    }

    /* JADX INFO: Multiple debug info for r7v0 int: [D('lastX' int), D('lastY' int)] */
    private ResultPoint findCornerFromCenter(int centerX, int deltaX, int left, int right, int centerY, int deltaY, int top, int bottom, int maxWhiteRun) throws NotFoundException {
        int[] range;
        int y = centerY;
        int[] lastRange = null;
        int x = centerX;
        while (true) {
            if (y < bottom) {
                if (y >= top) {
                    if (x < right) {
                        if (x < left) {
                            break;
                        }
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
                            int lastY2 = x - deltaX;
                            if (lastRange[0] >= centerY) {
                                return new ResultPoint((float) lastY2, (float) lastRange[1]);
                            }
                            if (lastRange[1] <= centerY) {
                                return new ResultPoint((float) lastY2, (float) lastRange[0]);
                            }
                            return new ResultPoint((float) lastY2, (float) (deltaX < 0 ? lastRange[0] : lastRange[1]));
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private int[] blackWhiteRange(int fixedDimension, int maxWhiteRun, int minDim, int maxDim, boolean horizontal) {
        int center = (minDim + maxDim) >> 1;
        int start = center;
        while (true) {
            if (start < minDim) {
                break;
            }
            BitMatrix bitMatrix = this.image;
            if (!horizontal ? !bitMatrix.get(fixedDimension, start) : !bitMatrix.get(start, fixedDimension)) {
                while (true) {
                    start--;
                    if (start < minDim) {
                        break;
                    }
                    BitMatrix bitMatrix2 = this.image;
                    if (horizontal) {
                        if (bitMatrix2.get(start, fixedDimension)) {
                            break;
                        }
                    } else if (bitMatrix2.get(fixedDimension, start)) {
                        break;
                    }
                }
                int whiteRunSize = start - start;
                if (start < minDim || whiteRunSize > maxWhiteRun) {
                    start = start;
                }
            } else {
                start--;
            }
        }
        start = start;
        int start2 = start + 1;
        int end = center;
        while (true) {
            if (end >= maxDim) {
                break;
            }
            BitMatrix bitMatrix3 = this.image;
            if (!horizontal ? !bitMatrix3.get(fixedDimension, end) : !bitMatrix3.get(end, fixedDimension)) {
                while (true) {
                    end++;
                    if (end >= maxDim) {
                        break;
                    }
                    BitMatrix bitMatrix4 = this.image;
                    if (horizontal) {
                        if (bitMatrix4.get(end, fixedDimension)) {
                            break;
                        }
                    } else if (bitMatrix4.get(fixedDimension, end)) {
                        break;
                    }
                }
                int whiteRunSize2 = end - end;
                if (end >= maxDim || whiteRunSize2 > maxWhiteRun) {
                    end = end;
                }
            } else {
                end++;
            }
        }
        int end2 = end - 1;
        if (end2 <= start2) {
            return null;
        }
        return new int[]{start2, end2};
    }
}
