package com.huawei.zxing.qrcode.encoder;

final class MaskUtil {
    private static final int N1 = 3;
    private static final int N2 = 3;
    private static final int N3 = 40;
    private static final int N4 = 10;

    private MaskUtil() {
    }

    static int applyMaskPenaltyRule1(ByteMatrix matrix) {
        return applyMaskPenaltyRule1Internal(matrix, true) + applyMaskPenaltyRule1Internal(matrix, false);
    }

    static int applyMaskPenaltyRule2(ByteMatrix matrix) {
        int penalty = 0;
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int y = 0;
        while (y < height - 1) {
            int x = 0;
            while (x < width - 1) {
                byte value = array[y][x];
                if (value == array[y][x + 1] && value == array[y + 1][x] && value == array[y + 1][x + 1]) {
                    penalty++;
                }
                x++;
            }
            y++;
        }
        return penalty * 3;
    }

    static int applyMaskPenaltyRule3(ByteMatrix matrix) {
        int numPenalties = 0;
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int y = 0;
        while (y < height) {
            int x = 0;
            while (x < width) {
                byte[] arrayY = array[y];
                if (x + 6 < width && arrayY[x] == (byte) 1 && arrayY[x + 1] == (byte) 0 && arrayY[x + 2] == (byte) 1 && arrayY[x + 3] == (byte) 1 && arrayY[x + 4] == (byte) 1 && arrayY[x + 5] == (byte) 0 && arrayY[x + 6] == (byte) 1 && (isWhiteHorizontal(arrayY, x - 4, x) || isWhiteHorizontal(arrayY, x + 7, x + 11))) {
                    numPenalties++;
                }
                if (y + 6 < height && array[y][x] == (byte) 1 && array[y + 1][x] == (byte) 0 && array[y + 2][x] == (byte) 1 && array[y + 3][x] == (byte) 1 && array[y + 4][x] == (byte) 1 && array[y + 5][x] == (byte) 0 && array[y + 6][x] == (byte) 1 && (isWhiteVertical(array, x, y - 4, y) || isWhiteVertical(array, x, y + 7, y + 11))) {
                    numPenalties++;
                }
                x++;
            }
            y++;
        }
        return numPenalties * 40;
    }

    private static boolean isWhiteHorizontal(byte[] rowArray, int from, int to) {
        int i = from;
        while (i < to) {
            if (i >= 0 && i < rowArray.length && rowArray[i] == (byte) 1) {
                return false;
            }
            i++;
        }
        return true;
    }

    private static boolean isWhiteVertical(byte[][] array, int col, int from, int to) {
        int i = from;
        while (i < to) {
            if (i >= 0 && i < array.length && array[i][col] == (byte) 1) {
                return false;
            }
            i++;
        }
        return true;
    }

    static int applyMaskPenaltyRule4(ByteMatrix matrix) {
        int numDarkCells = 0;
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        for (int y = 0; y < height; y++) {
            byte[] arrayY = array[y];
            for (int x = 0; x < width; x++) {
                if (arrayY[x] == (byte) 1) {
                    numDarkCells++;
                }
            }
        }
        int numTotalCells = matrix.getHeight() * matrix.getWidth();
        return ((Math.abs((numDarkCells * 2) - numTotalCells) * 10) / numTotalCells) * 10;
    }

    static boolean getDataMaskBit(int maskPattern, int x, int y) {
        int intermediate;
        int temp;
        switch (maskPattern) {
            case 0:
                intermediate = (y + x) & 1;
                break;
            case 1:
                intermediate = y & 1;
                break;
            case 2:
                intermediate = x % 3;
                break;
            case 3:
                intermediate = (y + x) % 3;
                break;
            case 4:
                intermediate = ((y >>> 1) + (x / 3)) & 1;
                break;
            case 5:
                temp = y * x;
                intermediate = (temp & 1) + (temp % 3);
                break;
            case 6:
                temp = y * x;
                intermediate = ((temp & 1) + (temp % 3)) & 1;
                break;
            case 7:
                intermediate = (((y * x) % 3) + ((y + x) & 1)) & 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid mask pattern: " + maskPattern);
        }
        if (intermediate == 0) {
            return true;
        }
        return false;
    }

    private static int applyMaskPenaltyRule1Internal(ByteMatrix matrix, boolean isHorizontal) {
        int penalty = 0;
        int iLimit = isHorizontal ? matrix.getHeight() : matrix.getWidth();
        int jLimit = isHorizontal ? matrix.getWidth() : matrix.getHeight();
        byte[][] array = matrix.getArray();
        int i = 0;
        while (i < iLimit) {
            int numSameBitCells = 0;
            int prevBit = -1;
            int j = 0;
            while (j < jLimit) {
                int bit = isHorizontal ? array[i][j] : array[j][i];
                if (bit == prevBit) {
                    numSameBitCells++;
                } else {
                    if (numSameBitCells >= 5) {
                        penalty += (numSameBitCells - 5) + 3;
                    }
                    numSameBitCells = 1;
                    prevBit = bit;
                }
                j++;
            }
            if (numSameBitCells >= 5) {
                penalty += (numSameBitCells - 5) + 3;
            }
            i++;
        }
        return penalty;
    }
}
