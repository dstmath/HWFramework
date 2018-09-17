package java.math;

import dalvik.system.VMDebug;

class Conversion {
    static final int[] bigRadices = new int[]{Integer.MIN_VALUE, 1162261467, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS, 1220703125, 362797056, 1977326743, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS, 387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056, 170859375, VMDebug.KIND_THREAD_EXT_ALLOCATED_OBJECTS, 410338673, 612220032, 893871739, 1280000000, 1801088541, 113379904, 148035889, 191102976, 244140625, 308915776, 387420489, 481890304, 594823321, 729000000, 887503681, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS, 1291467969, 1544804416, 1838265625, 60466176};
    static final int[] digitFitInInt = new int[]{-1, -1, 31, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};

    private Conversion() {
    }

    static String bigInteger2String(BigInteger val, int radix) {
        val.prepareJavaRepresentation();
        int sign = val.sign;
        int numberLength = val.numberLength;
        int[] digits = val.digits;
        if (sign == 0) {
            return AndroidHardcodedSystemProperties.JAVA_VERSION;
        }
        if (numberLength == 1) {
            long v = ((long) digits[numberLength - 1]) & 4294967295L;
            if (sign < 0) {
                v = -v;
            }
            return Long.toString(v, radix);
        } else if (radix == 10 || radix < 2 || radix > 36) {
            return val.toString();
        } else {
            int i;
            double bitLength = ((double) val.abs().bitLength()) / (Math.log((double) radix) / Math.log(2.0d));
            if (sign < 0) {
                i = 1;
            } else {
                i = 0;
            }
            int resLengthInChars = ((int) (((double) i) + bitLength)) + 1;
            char[] result = new char[resLengthInChars];
            int currentChar = resLengthInChars;
            int i2;
            if (radix != 16) {
                int[] temp = new int[numberLength];
                System.arraycopy(digits, 0, temp, 0, numberLength);
                int tempLen = numberLength;
                int charsPerInt = digitFitInInt[radix];
                int bigRadix = bigRadices[radix - 2];
                while (true) {
                    int resDigit = Division.divideArrayByInt(temp, temp, tempLen, bigRadix);
                    int previous = currentChar;
                    do {
                        currentChar--;
                        result[currentChar] = Character.forDigit(resDigit % radix, radix);
                        resDigit /= radix;
                        if (resDigit == 0) {
                            break;
                        }
                    } while (currentChar != 0);
                    int delta = (charsPerInt - previous) + currentChar;
                    for (i2 = 0; i2 < delta && currentChar > 0; i2++) {
                        currentChar--;
                        result[currentChar] = '0';
                    }
                    i2 = tempLen - 1;
                    while (i2 > 0 && temp[i2] == 0) {
                        i2--;
                    }
                    tempLen = i2 + 1;
                    if (tempLen == 1 && temp[0] == 0) {
                        break;
                    }
                }
            } else {
                for (i2 = 0; i2 < numberLength; i2++) {
                    for (int j = 0; j < 8 && currentChar > 0; j++) {
                        currentChar--;
                        result[currentChar] = Character.forDigit((digits[i2] >> (j << 2)) & 15, 16);
                    }
                }
            }
            while (result[currentChar] == '0') {
                currentChar++;
            }
            if (sign == -1) {
                currentChar--;
                result[currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars - currentChar);
        }
    }

    static String toDecimalScaledString(BigInteger val, int scale) {
        val.prepareJavaRepresentation();
        int sign = val.sign;
        int numberLength = val.numberLength;
        int[] digits = val.digits;
        if (sign == 0) {
            switch (scale) {
                case 0:
                    return AndroidHardcodedSystemProperties.JAVA_VERSION;
                case 1:
                    return "0.0";
                case 2:
                    return "0.00";
                case 3:
                    return "0.000";
                case 4:
                    return "0.0000";
                case 5:
                    return "0.00000";
                case 6:
                    return "0.000000";
                default:
                    StringBuilder result1 = new StringBuilder();
                    if (scale < 0) {
                        result1.append("0E+");
                    } else {
                        result1.append("0E");
                    }
                    result1.append(-scale);
                    return result1.toString();
            }
        }
        int j;
        int resLengthInChars = ((numberLength * 10) + 1) + 7;
        char[] result = new char[(resLengthInChars + 1)];
        int currentChar = resLengthInChars;
        if (numberLength == 1) {
            int highDigit = digits[0];
            if (highDigit >= 0) {
                int v = highDigit;
                while (true) {
                    int prev = v;
                    v /= 10;
                    currentChar--;
                    result[currentChar] = (char) ((prev - (v * 10)) + 48);
                    if (v == 0) {
                        break;
                    }
                }
            } else {
                long v2 = ((long) highDigit) & 4294967295L;
                do {
                    long prev2 = v2;
                    v2 /= 10;
                    currentChar--;
                    result[currentChar] = (char) (((int) (prev2 - (10 * v2))) + 48);
                } while (v2 != 0);
            }
        } else {
            int[] temp = new int[numberLength];
            int tempLen = numberLength;
            System.arraycopy(digits, 0, temp, 0, numberLength);
            while (true) {
                long result11 = 0;
                for (int i1 = tempLen - 1; i1 >= 0; i1--) {
                    long res = divideLongByBillion((result11 << 32) + (((long) temp[i1]) & 4294967295L));
                    temp[i1] = (int) res;
                    result11 = (long) ((int) (res >> 32));
                }
                int resDigit = (int) result11;
                int previous = currentChar;
                do {
                    currentChar--;
                    result[currentChar] = (char) ((resDigit % 10) + 48);
                    resDigit /= 10;
                    if (resDigit == 0) {
                        break;
                    }
                } while (currentChar != 0);
                int delta = (9 - previous) + currentChar;
                for (int i = 0; i < delta && currentChar > 0; i++) {
                    currentChar--;
                    result[currentChar] = '0';
                }
                j = tempLen - 1;
                while (temp[j] == 0) {
                    if (j == 0) {
                        break;
                    }
                    j--;
                }
                tempLen = j + 1;
            }
            while (result[currentChar] == '0') {
                currentChar++;
            }
        }
        boolean negNumber = sign < 0;
        int exponent = ((resLengthInChars - currentChar) - scale) - 1;
        if (scale == 0) {
            if (negNumber) {
                currentChar--;
                result[currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars - currentChar);
        } else if (scale <= 0 || exponent < -6) {
            int startPoint = currentChar + 1;
            int endPoint = resLengthInChars;
            StringBuilder stringBuilder = new StringBuilder((resLengthInChars + 16) - startPoint);
            if (negNumber) {
                stringBuilder.append('-');
            }
            if (resLengthInChars - startPoint >= 1) {
                stringBuilder.append(result[currentChar]);
                stringBuilder.append('.');
                stringBuilder.append(result, currentChar + 1, (resLengthInChars - currentChar) - 1);
            } else {
                stringBuilder.append(result, currentChar, resLengthInChars - currentChar);
            }
            stringBuilder.append('E');
            if (exponent > 0) {
                stringBuilder.append('+');
            }
            stringBuilder.append(Integer.toString(exponent));
            return stringBuilder.toString();
        } else if (exponent >= 0) {
            int insertPoint = currentChar + exponent;
            for (j = resLengthInChars - 1; j >= insertPoint; j--) {
                result[j + 1] = result[j];
            }
            result[insertPoint + 1] = '.';
            if (negNumber) {
                currentChar--;
                result[currentChar] = '-';
            }
            return new String(result, currentChar, (resLengthInChars - currentChar) + 1);
        } else {
            for (j = 2; j < (-exponent) + 1; j++) {
                currentChar--;
                result[currentChar] = '0';
            }
            currentChar--;
            result[currentChar] = '.';
            currentChar--;
            result[currentChar] = '0';
            if (negNumber) {
                currentChar--;
                result[currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars - currentChar);
        }
    }

    static String toDecimalScaledString(long value, int scale) {
        boolean negNumber = value < 0;
        if (negNumber) {
            value = -value;
        }
        StringBuilder result1;
        if (value == 0) {
            switch (scale) {
                case 0:
                    return AndroidHardcodedSystemProperties.JAVA_VERSION;
                case 1:
                    return "0.0";
                case 2:
                    return "0.00";
                case 3:
                    return "0.000";
                case 4:
                    return "0.0000";
                case 5:
                    return "0.00000";
                case 6:
                    return "0.000000";
                default:
                    result1 = new StringBuilder();
                    if (scale < 0) {
                        result1.append("0E+");
                    } else {
                        result1.append("0E");
                    }
                    result1.append(scale == Integer.MIN_VALUE ? "2147483648" : Integer.toString(-scale));
                    return result1.toString();
            }
        }
        char[] result = new char[19];
        int currentChar = 18;
        long v = value;
        do {
            long prev = v;
            v /= 10;
            currentChar--;
            result[currentChar] = (char) ((int) ((prev - (10 * v)) + 48));
        } while (v != 0);
        long exponent = ((18 - ((long) currentChar)) - ((long) scale)) - 1;
        int j;
        if (scale == 0) {
            if (negNumber) {
                currentChar--;
                result[currentChar] = '-';
            }
            return new String(result, currentChar, 18 - currentChar);
        } else if (scale <= 0 || exponent < -6) {
            int startPoint = currentChar + 1;
            result1 = new StringBuilder(34 - startPoint);
            if (negNumber) {
                result1.append('-');
            }
            if (18 - startPoint >= 1) {
                result1.append(result[currentChar]);
                result1.append('.');
                result1.append(result, currentChar + 1, (18 - currentChar) - 1);
            } else {
                result1.append(result, currentChar, 18 - currentChar);
            }
            result1.append('E');
            if (exponent > 0) {
                result1.append('+');
            }
            result1.append(Long.toString(exponent));
            return result1.toString();
        } else if (exponent >= 0) {
            int insertPoint = currentChar + ((int) exponent);
            for (j = 17; j >= insertPoint; j--) {
                result[j + 1] = result[j];
            }
            result[insertPoint + 1] = '.';
            if (negNumber) {
                currentChar--;
                result[currentChar] = '-';
            }
            return new String(result, currentChar, (18 - currentChar) + 1);
        } else {
            for (j = 2; ((long) j) < (-exponent) + 1; j++) {
                currentChar--;
                result[currentChar] = '0';
            }
            currentChar--;
            result[currentChar] = '.';
            currentChar--;
            result[currentChar] = '0';
            if (negNumber) {
                currentChar--;
                result[currentChar] = '-';
            }
            return new String(result, currentChar, 18 - currentChar);
        }
    }

    static long divideLongByBillion(long a) {
        long quot;
        long rem;
        if (a >= 0) {
            quot = a / 1000000000;
            rem = a % 1000000000;
        } else {
            long aPos = a >>> 1;
            quot = aPos / 500000000;
            rem = ((aPos % 500000000) << 1) + (1 & a);
        }
        return (rem << 32) | (4294967295L & quot);
    }

    static double bigInteger2Double(BigInteger val) {
        val.prepareJavaRepresentation();
        if (val.numberLength < 2 || (val.numberLength == 2 && val.digits[1] > 0)) {
            return (double) val.longValue();
        }
        double d;
        if (val.numberLength > 32) {
            if (val.sign > 0) {
                d = Double.POSITIVE_INFINITY;
            } else {
                d = Double.NEGATIVE_INFINITY;
            }
            return d;
        }
        int bitLen = val.abs().bitLength();
        long exponent = (long) (bitLen - 1);
        int delta = bitLen - 54;
        long mantissa = val.abs().shiftRight(delta).longValue() & 9007199254740991L;
        if (exponent == 1023) {
            if (mantissa == 9007199254740991L) {
                if (val.sign > 0) {
                    d = Double.POSITIVE_INFINITY;
                } else {
                    d = Double.NEGATIVE_INFINITY;
                }
                return d;
            } else if (mantissa == 9007199254740990L) {
                return val.sign > 0 ? Double.MAX_VALUE : -1.7976931348623157E308d;
            }
        }
        if ((1 & mantissa) == 1 && ((2 & mantissa) == 2 || BitLevel.nonZeroDroppedBits(delta, val.digits))) {
            mantissa += 2;
        }
        return Double.longBitsToDouble(((val.sign < 0 ? Long.MIN_VALUE : 0) | (((1023 + exponent) << 52) & 9218868437227405312L)) | (mantissa >> 1));
    }
}
