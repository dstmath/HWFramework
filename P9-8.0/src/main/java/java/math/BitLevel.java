package java.math;

class BitLevel {
    private BitLevel() {
    }

    static int bitLength(BigInteger val) {
        val.prepareJavaRepresentation();
        if (val.sign == 0) {
            return 0;
        }
        int bLength = val.numberLength << 5;
        int highDigit = val.digits[val.numberLength - 1];
        if (val.sign < 0 && val.getFirstNonzeroDigit() == val.numberLength - 1) {
            highDigit--;
        }
        return bLength - Integer.numberOfLeadingZeros(highDigit);
    }

    static int bitCount(BigInteger val) {
        val.prepareJavaRepresentation();
        int bCount = 0;
        if (val.sign == 0) {
            return 0;
        }
        int i = val.getFirstNonzeroDigit();
        if (val.sign > 0) {
            while (i < val.numberLength) {
                bCount += Integer.bitCount(val.digits[i]);
                i++;
            }
        } else {
            bCount = Integer.bitCount(-val.digits[i]) + 0;
            while (true) {
                i++;
                if (i >= val.numberLength) {
                    break;
                }
                bCount += Integer.bitCount(~val.digits[i]);
            }
            bCount = (val.numberLength << 5) - bCount;
        }
        return bCount;
    }

    static boolean testBit(BigInteger val, int n) {
        val.prepareJavaRepresentation();
        if ((val.digits[n >> 5] & (1 << (n & 31))) != 0) {
            return true;
        }
        return false;
    }

    static boolean nonZeroDroppedBits(int numberOfBits, int[] digits) {
        int intCount = numberOfBits >> 5;
        int bitCount = numberOfBits & 31;
        int i = 0;
        while (i < intCount && digits[i] == 0) {
            i++;
        }
        if (i == intCount && (digits[i] << (32 - bitCount)) == 0) {
            return false;
        }
        return true;
    }

    static void shiftLeftOneBit(int[] result, int[] source, int srcLen) {
        int carry = 0;
        for (int i = 0; i < srcLen; i++) {
            int val = source[i];
            result[i] = (val << 1) | carry;
            carry = val >>> 31;
        }
        if (carry != 0) {
            result[srcLen] = carry;
        }
    }

    static BigInteger shiftLeftOneBit(BigInteger source) {
        source.prepareJavaRepresentation();
        int srcLen = source.numberLength;
        int resLen = srcLen + 1;
        int[] resDigits = new int[resLen];
        shiftLeftOneBit(resDigits, source.digits, srcLen);
        return new BigInteger(source.sign, resLen, resDigits);
    }

    static BigInteger shiftRight(BigInteger source, int count) {
        source.prepareJavaRepresentation();
        int intCount = count >> 5;
        count &= 31;
        if (intCount >= source.numberLength) {
            return source.sign < 0 ? BigInteger.MINUS_ONE : BigInteger.ZERO;
        }
        int resLength = source.numberLength - intCount;
        int[] resDigits = new int[(resLength + 1)];
        shiftRight(resDigits, resLength, source.digits, intCount, count);
        if (source.sign < 0) {
            int i = 0;
            while (i < intCount && source.digits[i] == 0) {
                i++;
            }
            if (i < intCount || (count > 0 && (source.digits[i] << (32 - count)) != 0)) {
                i = 0;
                while (i < resLength && resDigits[i] == -1) {
                    resDigits[i] = 0;
                    i++;
                }
                if (i == resLength) {
                    resLength++;
                }
                resDigits[i] = resDigits[i] + 1;
            }
        }
        return new BigInteger(source.sign, resLength, resDigits);
    }

    static boolean shiftRight(int[] result, int resultLen, int[] source, int intCount, int count) {
        int i = 1;
        boolean allZero = true;
        int i2 = 0;
        while (i2 < intCount) {
            int i3;
            if (source[i2] == 0) {
                i3 = 1;
            } else {
                i3 = 0;
            }
            allZero &= i3;
            i2++;
        }
        if (count == 0) {
            System.arraycopy(source, intCount, result, 0, resultLen);
            i2 = resultLen;
        } else {
            int leftShiftCount = 32 - count;
            if ((source[i2] << leftShiftCount) != 0) {
                i = 0;
            }
            allZero &= i;
            i2 = 0;
            while (i2 < resultLen - 1) {
                result[i2] = (source[i2 + intCount] >>> count) | (source[(i2 + intCount) + 1] << leftShiftCount);
                i2++;
            }
            result[i2] = source[i2 + intCount] >>> count;
            i2++;
        }
        return allZero;
    }

    static BigInteger flipBit(BigInteger val, int n) {
        val.prepareJavaRepresentation();
        int resSign = val.sign == 0 ? 1 : val.sign;
        int intCount = n >> 5;
        int resLength = Math.max(intCount + 1, val.numberLength) + 1;
        int[] resDigits = new int[resLength];
        int bitNumber = 1 << (n & 31);
        System.arraycopy(val.digits, 0, resDigits, 0, val.numberLength);
        if (val.sign >= 0) {
            resDigits[intCount] = resDigits[intCount] ^ bitNumber;
        } else if (intCount >= val.numberLength) {
            resDigits[intCount] = bitNumber;
        } else {
            int firstNonZeroDigit = val.getFirstNonzeroDigit();
            int i;
            if (intCount > firstNonZeroDigit) {
                resDigits[intCount] = resDigits[intCount] ^ bitNumber;
            } else if (intCount < firstNonZeroDigit) {
                resDigits[intCount] = -bitNumber;
                i = intCount + 1;
                while (i < firstNonZeroDigit) {
                    resDigits[i] = -1;
                    i++;
                }
                int i2 = resDigits[i];
                resDigits[i] = i2 - 1;
                resDigits[i] = i2;
            } else {
                i = intCount;
                resDigits[intCount] = -((-resDigits[intCount]) ^ bitNumber);
                if (resDigits[intCount] == 0) {
                    i = intCount + 1;
                    while (resDigits[i] == -1) {
                        resDigits[i] = 0;
                        i++;
                    }
                    resDigits[i] = resDigits[i] + 1;
                }
            }
        }
        return new BigInteger(resSign, resLength, resDigits);
    }
}
