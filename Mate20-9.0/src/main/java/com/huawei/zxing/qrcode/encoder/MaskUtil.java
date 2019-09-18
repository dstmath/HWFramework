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
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int penalty = 0;
        int y = 0;
        while (y < height - 1) {
            int penalty2 = penalty;
            for (int x = 0; x < width - 1; x++) {
                byte value = array[y][x];
                if (value == array[y][x + 1] && value == array[y + 1][x] && value == array[y + 1][x + 1]) {
                    penalty2++;
                }
            }
            y++;
            penalty = penalty2;
        }
        return 3 * penalty;
    }

    static int applyMaskPenaltyRule3(ByteMatrix matrix) {
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int numPenalties = 0;
        int y = 0;
        while (y < height) {
            int numPenalties2 = numPenalties;
            for (int x = 0; x < width; x++) {
                byte[] arrayY = array[y];
                if (x + 6 < width && arrayY[x] == 1 && arrayY[x + 1] == 0 && arrayY[x + 2] == 1 && arrayY[x + 3] == 1 && arrayY[x + 4] == 1 && arrayY[x + 5] == 0 && arrayY[x + 6] == 1 && (isWhiteHorizontal(arrayY, x - 4, x) || isWhiteHorizontal(arrayY, x + 7, x + 11))) {
                    numPenalties2++;
                }
                if (y + 6 < height && array[y][x] == 1 && array[y + 1][x] == 0 && array[y + 2][x] == 1 && array[y + 3][x] == 1 && array[y + 4][x] == 1 && array[y + 5][x] == 0 && array[y + 6][x] == 1 && (isWhiteVertical(array, x, y - 4, y) || isWhiteVertical(array, x, y + 7, y + 11))) {
                    numPenalties2++;
                }
            }
            y++;
            numPenalties = numPenalties2;
        }
        return numPenalties * 40;
    }

    private static boolean isWhiteHorizontal(byte[] rowArray, int from, int to) {
        for (int i = from; i < to; i++) {
            if (i >= 0 && i < rowArray.length && rowArray[i] == 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWhiteVertical(byte[][] array, int col, int from, int to) {
        for (int i = from; i < to; i++) {
            if (i >= 0 && i < array.length && array[i][col] == 1) {
                return false;
            }
        }
        return true;
    }

    static int applyMaskPenaltyRule4(ByteMatrix matrix) {
        byte[][] array = matrix.getArray();
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int numDarkCells = 0;
        int y = 0;
        while (y < height) {
            byte[] arrayY = array[y];
            int numDarkCells2 = numDarkCells;
            for (int x = 0; x < width; x++) {
                if (arrayY[x] == 1) {
                    numDarkCells2++;
                }
            }
            y++;
            numDarkCells = numDarkCells2;
        }
        int numTotalCells = matrix.getHeight() * matrix.getWidth();
        return ((Math.abs((numDarkCells * 2) - numTotalCells) * 10) / numTotalCells) * 10;
    }

    static boolean getDataMaskBit(int maskPattern, int x, int y) {
        int intermediate;
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
                int temp = y * x;
                intermediate = (temp & 1) + (temp % 3);
                break;
            case 6:
                int temp2 = y * x;
                intermediate = ((temp2 & 1) + (temp2 % 3)) & 1;
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
        int iLimit = isHorizontal ? matrix.getHeight() : matrix.getWidth();
        int jLimit = isHorizontal ? matrix.getWidth() : matrix.getHeight();
        byte[][] array = matrix.getArray();
        int penalty = 0;
        for (int i = 0; i < iLimit; i++) {
            int numSameBitCells = 0;
            int prevBit = -1;
            int penalty2 = penalty;
            for (int j = 0; j < jLimit; j++) {
                int bit = isHorizontal ? array[i][j] : array[j][i];
                if (bit == prevBit) {
                    numSameBitCells++;
                } else {
                    if (numSameBitCells >= 5) {
                        penalty2 += 3 + (numSameBitCells - 5);
                    }
                    numSameBitCells = 1;
                    prevBit = bit;
                }
            }
            if (numSameBitCells >= 5) {
                penalty2 += 3 + (numSameBitCells - 5);
            }
            penalty = penalty2;
        }
        return penalty;
    }
}
