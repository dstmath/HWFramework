package com.huawei.zxing.common;

import com.huawei.zxing.Binarizer;
import com.huawei.zxing.LuminanceSource;
import com.huawei.zxing.NotFoundException;
import java.lang.reflect.Array;

public final class HybridBinarizer extends GlobalHistogramBinarizer {
    private static final int BLOCK_SIZE = 8;
    private static final int BLOCK_SIZE_MASK = 7;
    private static final int BLOCK_SIZE_POWER = 3;
    private static final int MINIMUM_DIMENSION = 40;
    private static final int MIN_DYNAMIC_RANGE = 24;
    private BitMatrix matrix;

    public HybridBinarizer(LuminanceSource source) {
        super(source);
    }

    public BitMatrix getBlackMatrix() throws NotFoundException {
        if (this.matrix != null) {
            return this.matrix;
        }
        LuminanceSource source = getLuminanceSource();
        int width = source.getWidth();
        int height = source.getHeight();
        if (width < 40 || height < 40) {
            this.matrix = super.getBlackMatrix();
        } else {
            byte[] luminances = source.getMatrix();
            int subWidth = width >> 3;
            if ((width & 7) != 0) {
                subWidth++;
            }
            int subWidth2 = subWidth;
            int subHeight = height >> 3;
            if ((height & 7) != 0) {
                subHeight++;
            }
            int subHeight2 = subHeight;
            int[][] blackPoints = calculateBlackPoints(luminances, subWidth2, subHeight2, width, height);
            BitMatrix newMatrix = new BitMatrix(width, height);
            calculateThresholdForBlock(luminances, subWidth2, subHeight2, width, height, blackPoints, newMatrix);
            this.matrix = newMatrix;
        }
        return this.matrix;
    }

    public Binarizer createBinarizer(LuminanceSource source) {
        return new HybridBinarizer(source);
    }

    private static void calculateThresholdForBlock(byte[] luminances, int subWidth, int subHeight, int width, int height, int[][] blackPoints, BitMatrix matrix2) {
        int i = subWidth;
        int i2 = subHeight;
        for (int y = 0; y < i2; y++) {
            int yoffset = y << 3;
            int maxYOffset = height - 8;
            if (yoffset > maxYOffset) {
                yoffset = maxYOffset;
            }
            int x = 0;
            while (true) {
                int x2 = x;
                if (x2 >= i) {
                    break;
                }
                int xoffset = x2 << 3;
                int maxXOffset = width - 8;
                if (xoffset > maxXOffset) {
                    xoffset = maxXOffset;
                }
                int xoffset2 = xoffset;
                int left = cap(x2, 2, i - 3);
                int top = cap(y, 2, i2 - 3);
                int z = -2;
                int sum = 0;
                while (true) {
                    int sum2 = z;
                    if (sum2 > 2) {
                        break;
                    }
                    int[] blackRow = blackPoints[top + sum2];
                    sum += blackRow[left - 2] + blackRow[left - 1] + blackRow[left] + blackRow[left + 1] + blackRow[left + 2];
                    z = sum2 + 1;
                }
                thresholdBlock(luminances, xoffset2, yoffset, sum / 25, width, matrix2);
                x = x2 + 1;
            }
        }
    }

    private static int cap(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    private static void thresholdBlock(byte[] luminances, int xoffset, int yoffset, int threshold, int stride, BitMatrix matrix2) {
        int y = 0;
        int offset = (yoffset * stride) + xoffset;
        while (y < 8) {
            for (int x = 0; x < 8; x++) {
                if ((luminances[offset + x] & 255) <= threshold) {
                    matrix2.set(xoffset + x, yoffset + y);
                }
            }
            y++;
            offset += stride;
        }
    }

    private static int[][] calculateBlackPoints(byte[] luminances, int subWidth, int subHeight, int width, int height) {
        int i = subWidth;
        int i2 = subHeight;
        int[][] blackPoints = (int[][]) Array.newInstance(int.class, new int[]{i2, i});
        int y = 0;
        while (y < i2) {
            int yoffset = y << 3;
            int maxYOffset = height - 8;
            if (yoffset > maxYOffset) {
                yoffset = maxYOffset;
            }
            int x = 0;
            while (x < i) {
                int xoffset = x << 3;
                int maxXOffset = width - 8;
                if (xoffset > maxXOffset) {
                    xoffset = maxXOffset;
                }
                int min = 0;
                int offset = (yoffset * width) + xoffset;
                int min2 = 255;
                int sum = 0;
                int yy = 0;
                while (true) {
                    int offset2 = offset;
                    int i3 = 8;
                    if (yy >= 8) {
                        break;
                    }
                    int max = min;
                    int min3 = min2;
                    int xx = 0;
                    while (xx < i3) {
                        int pixel = luminances[offset2 + xx] & 255;
                        sum += pixel;
                        if (pixel < min3) {
                            min3 = pixel;
                        }
                        int max2 = max;
                        if (pixel > max2) {
                            max = pixel;
                        } else {
                            max = max2;
                        }
                        xx++;
                        i3 = 8;
                        int i4 = subHeight;
                    }
                    int max3 = max;
                    if (max3 - min3 <= 24) {
                        yy++;
                        offset = offset2 + width;
                        min2 = min3;
                        int i5 = subWidth;
                        min = max3;
                        int max4 = subHeight;
                    }
                    while (true) {
                        yy++;
                        offset2 += width;
                        if (yy >= 8) {
                            break;
                        }
                        int xx2 = 0;
                        for (int i6 = 8; xx2 < i6; i6 = 8) {
                            sum += luminances[offset2 + xx2] & 255;
                            xx2++;
                        }
                    }
                    yy++;
                    offset = offset2 + width;
                    min2 = min3;
                    int i52 = subWidth;
                    min = max3;
                    int max42 = subHeight;
                }
                int average = sum >> 6;
                if (min - min2 <= 24) {
                    average = min2 >> 1;
                    if (y > 0 && x > 0) {
                        int averageNeighborBlackPoint = ((blackPoints[y - 1][x] + (blackPoints[y][x - 1] * 2)) + blackPoints[y - 1][x - 1]) >> 2;
                        if (min2 < averageNeighborBlackPoint) {
                            average = averageNeighborBlackPoint;
                        }
                    }
                }
                blackPoints[y][x] = average;
                x++;
                i = subWidth;
                int i7 = subHeight;
            }
            y++;
            i = subWidth;
            i2 = subHeight;
        }
        return blackPoints;
    }
}
