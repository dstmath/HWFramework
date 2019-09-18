package java.math;

import dalvik.system.VMDebug;

class Conversion {
    static final int[] bigRadices = {Integer.MIN_VALUE, 1162261467, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS, 1220703125, 362797056, 1977326743, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS, 387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056, 170859375, VMDebug.KIND_THREAD_EXT_ALLOCATED_OBJECTS, 410338673, 612220032, 893871739, 1280000000, 1801088541, 113379904, 148035889, 191102976, 244140625, 308915776, 387420489, 481890304, 594823321, 729000000, 887503681, VMDebug.KIND_THREAD_EXT_FREED_OBJECTS, 1291467969, 1544804416, 1838265625, 60466176};
    static final int[] digitFitInInt = {-1, -1, 31, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};

    private Conversion() {
    }

    static String bigInteger2String(BigInteger val, int radix) {
        int currentChar;
        boolean z;
        BigInteger bigInteger = val;
        int i = radix;
        val.prepareJavaRepresentation();
        int sign = bigInteger.sign;
        int numberLength = bigInteger.numberLength;
        int[] digits = bigInteger.digits;
        if (sign == 0) {
            return AndroidHardcodedSystemProperties.JAVA_VERSION;
        }
        if (numberLength == 1) {
            long v = ((long) digits[numberLength - 1]) & 4294967295L;
            if (sign < 0) {
                v = -v;
            }
            return Long.toString(v, i);
        } else if (i == 10 || i < 2 || i > 36) {
            return val.toString();
        } else {
            int resLengthInChars = ((int) ((((double) val.abs().bitLength()) / (Math.log((double) i) / Math.log(2.0d))) + ((double) (sign < 0 ? 1 : 0)))) + 1;
            char[] result = new char[resLengthInChars];
            int currentChar2 = resLengthInChars;
            char c = '0';
            if (i != 16) {
                int[] temp = new int[numberLength];
                System.arraycopy(digits, 0, temp, 0, numberLength);
                int tempLen = numberLength;
                int charsPerInt = digitFitInInt[i];
                int bigRadix = bigRadices[i - 2];
                while (true) {
                    int bigRadix2 = bigRadix;
                    int resDigit = Division.divideArrayByInt(temp, temp, tempLen, bigRadix2);
                    int currentChar3 = currentChar2;
                    while (true) {
                        currentChar3--;
                        result[currentChar3] = Character.forDigit(resDigit % i, i);
                        int i2 = resDigit / i;
                        resDigit = i2;
                        if (i2 == 0 || currentChar3 == 0) {
                            int delta = (charsPerInt - currentChar2) + currentChar3;
                            currentChar = currentChar3;
                            int currentChar4 = 0;
                        }
                    }
                    int delta2 = (charsPerInt - currentChar2) + currentChar3;
                    currentChar = currentChar3;
                    int currentChar42 = 0;
                    while (true) {
                        int i3 = currentChar42;
                        if (i3 >= delta2 || currentChar <= 0) {
                            int i4 = tempLen - 1;
                        } else {
                            currentChar--;
                            result[currentChar] = c;
                            currentChar42 = i3 + 1;
                        }
                    }
                    int i42 = tempLen - 1;
                    while (i42 > 0 && temp[i42] == 0) {
                        i42--;
                    }
                    tempLen = i42 + 1;
                    if (tempLen == 1) {
                        z = false;
                        if (temp[0] == 0) {
                            break;
                        }
                    } else {
                        z = false;
                    }
                    currentChar2 = currentChar;
                    c = '0';
                    int i5 = z;
                    bigRadix = bigRadix2;
                    int bigRadix3 = i5;
                }
            } else {
                currentChar = currentChar2;
                for (int i6 = 0; i6 < numberLength; i6++) {
                    for (int j = 0; j < 8 && currentChar > 0; j++) {
                        currentChar--;
                        result[currentChar] = Character.forDigit((digits[i6] >> (j << 2)) & 15, 16);
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
            int currentChar5 = currentChar;
            return new String(result, currentChar5, resLengthInChars - currentChar5);
        }
    }

    static String toDecimalScaledString(BigInteger val, int scale) {
        char[] result;
        int currentChar;
        BigInteger bigInteger = val;
        int i = scale;
        val.prepareJavaRepresentation();
        int sign = bigInteger.sign;
        int numberLength = bigInteger.numberLength;
        int[] digits = bigInteger.digits;
        if (sign == 0) {
            switch (i) {
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
                    if (i < 0) {
                        result1.append("0E+");
                    } else {
                        result1.append("0E");
                    }
                    result1.append(-i);
                    return result1.toString();
            }
        } else {
            int resLengthInChars = (numberLength * 10) + 1 + 7;
            char[] result2 = new char[(resLengthInChars + 1)];
            int currentChar2 = resLengthInChars;
            long j = 4294967295L;
            if (numberLength == 1) {
                int highDigit = digits[0];
                if (highDigit < 0) {
                    long v = 4294967295L & ((long) highDigit);
                    while (true) {
                        long prev = v;
                        v /= 10;
                        currentChar2--;
                        result = result2;
                        result[currentChar2] = (char) (((int) (prev - (10 * v))) + 48);
                        if (v == 0) {
                            break;
                        }
                        result2 = result;
                    }
                } else {
                    result = result2;
                    int v2 = highDigit;
                    do {
                        int prev2 = v2;
                        v2 /= 10;
                        currentChar2--;
                        result[currentChar2] = (char) ((prev2 - (v2 * 10)) + 48);
                    } while (v2 != 0);
                }
            } else {
                result = result2;
                int[] temp = new int[numberLength];
                int tempLen = numberLength;
                System.arraycopy(digits, 0, temp, 0, tempLen);
                loop2:
                while (true) {
                    long res = 0;
                    int i1 = tempLen - 1;
                    while (i1 >= 0) {
                        long j2 = res;
                        long res2 = divideLongByBillion((res << 32) + (((long) temp[i1]) & j));
                        temp[i1] = (int) res2;
                        res = (long) ((int) (res2 >> 32));
                        i1--;
                        j = 4294967295L;
                    }
                    long j3 = res;
                    int resDigit = (int) res;
                    int currentChar3 = currentChar2;
                    do {
                        currentChar3--;
                        result[currentChar3] = (char) ((resDigit % 10) + 48);
                        int i2 = resDigit / 10;
                        resDigit = i2;
                        if (i2 == 0) {
                            break;
                        }
                    } while (currentChar3 != 0);
                    int delta = (9 - currentChar2) + currentChar3;
                    currentChar = currentChar3;
                    for (int i3 = 0; i3 < delta && currentChar > 0; i3++) {
                        currentChar--;
                        result[currentChar] = '0';
                    }
                    int j4 = tempLen - 1;
                    while (temp[j4] == 0) {
                        if (j4 == 0) {
                            break loop2;
                        }
                        j4--;
                    }
                    tempLen = j4 + 1;
                    currentChar2 = currentChar;
                    j = 4294967295L;
                }
                currentChar2 = currentChar;
                while (result[currentChar2] == '0') {
                    currentChar2++;
                }
            }
            boolean negNumber = sign < 0;
            int exponent = ((resLengthInChars - currentChar2) - i) - 1;
            if (i == 0) {
                if (negNumber) {
                    currentChar2--;
                    result[currentChar2] = '-';
                }
                return new String(result, currentChar2, resLengthInChars - currentChar2);
            }
            char[] result3 = result;
            if (i <= 0 || exponent < -6) {
                int startPoint = currentChar2 + 1;
                int endPoint = resLengthInChars;
                StringBuilder result12 = new StringBuilder((16 + endPoint) - startPoint);
                if (negNumber) {
                    result12.append('-');
                }
                if (endPoint - startPoint >= 1) {
                    result12.append(result3[currentChar2]);
                    result12.append('.');
                    result12.append(result3, currentChar2 + 1, (resLengthInChars - currentChar2) - 1);
                } else {
                    result12.append(result3, currentChar2, resLengthInChars - currentChar2);
                }
                result12.append('E');
                if (exponent > 0) {
                    result12.append('+');
                }
                result12.append(Integer.toString(exponent));
                return result12.toString();
            } else if (exponent >= 0) {
                int insertPoint = currentChar2 + exponent;
                for (int j5 = resLengthInChars - 1; j5 >= insertPoint; j5--) {
                    result3[j5 + 1] = result3[j5];
                }
                result3[insertPoint + 1] = '.';
                if (negNumber) {
                    currentChar2--;
                    result3[currentChar2] = '-';
                }
                return new String(result3, currentChar2, (resLengthInChars - currentChar2) + 1);
            } else {
                int j6 = 2;
                for (int i4 = 1; j6 < (-exponent) + i4; i4 = 1) {
                    currentChar2--;
                    result3[currentChar2] = '0';
                    j6++;
                }
                int currentChar4 = currentChar2 - 1;
                result3[currentChar4] = '.';
                int currentChar5 = currentChar4 - 1;
                result3[currentChar5] = '0';
                if (negNumber) {
                    currentChar5--;
                    result3[currentChar5] = '-';
                }
                return new String(result3, currentChar5, resLengthInChars - currentChar5);
            }
        }
    }

    static String toDecimalScaledString(long value, int scale) {
        long value2 = value;
        int i = scale;
        boolean negNumber = value2 < 0;
        if (negNumber) {
            value2 = -value2;
        }
        if (value2 == 0) {
            switch (i) {
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
                    if (i < 0) {
                        result1.append("0E+");
                    } else {
                        result1.append("0E");
                    }
                    result1.append(i == Integer.MIN_VALUE ? "2147483648" : Integer.toString(-i));
                    return result1.toString();
            }
        } else {
            char[] result = new char[(18 + 1)];
            int currentChar = 18;
            long v = value2;
            do {
                long prev = v;
                v /= 10;
                currentChar--;
                result[currentChar] = (char) ((int) (48 + (prev - (10 * v))));
            } while (v != 0);
            long exponent = ((((long) 18) - ((long) currentChar)) - ((long) i)) - 1;
            if (i == 0) {
                if (negNumber) {
                    currentChar--;
                    result[currentChar] = '-';
                }
                return new String(result, currentChar, 18 - currentChar);
            } else if (i <= 0 || exponent < -6) {
                int startPoint = currentChar + 1;
                StringBuilder result12 = new StringBuilder((16 + 18) - startPoint);
                if (negNumber) {
                    result12.append('-');
                }
                if (18 - startPoint >= 1) {
                    result12.append(result[currentChar]);
                    result12.append('.');
                    result12.append(result, currentChar + 1, (18 - currentChar) - 1);
                } else {
                    result12.append(result, currentChar, 18 - currentChar);
                }
                result12.append('E');
                if (exponent > 0) {
                    result12.append('+');
                }
                result12.append(Long.toString(exponent));
                return result12.toString();
            } else if (exponent >= 0) {
                int insertPoint = ((int) exponent) + currentChar;
                for (int j = 18 - 1; j >= insertPoint; j--) {
                    result[j + 1] = result[j];
                }
                result[insertPoint + 1] = '.';
                if (negNumber) {
                    currentChar--;
                    result[currentChar] = '-';
                }
                return new String(result, currentChar, (18 - currentChar) + 1);
            } else {
                int j2 = 2;
                while (true) {
                    long value3 = value2;
                    if (((long) j2) >= (-exponent) + 1) {
                        break;
                    }
                    currentChar--;
                    result[currentChar] = '0';
                    j2++;
                    value2 = value3;
                }
                int currentChar2 = currentChar - 1;
                result[currentChar2] = '.';
                int currentChar3 = currentChar2 - 1;
                result[currentChar3] = '0';
                if (negNumber) {
                    currentChar3--;
                    result[currentChar3] = '-';
                }
                return new String(result, currentChar3, 18 - currentChar3);
            }
        }
    }

    static long divideLongByBillion(long a) {
        long quot;
        long bLong;
        if (a >= 0) {
            quot = a / 1000000000;
            bLong = a % 1000000000;
        } else {
            long aPos = a >>> 1;
            bLong = ((aPos % 500000000) << 1) + (1 & a);
            quot = aPos / 500000000;
        }
        return (bLong << 32) | (4294967295L & quot);
    }

    static double bigInteger2Double(BigInteger val) {
        BigInteger bigInteger = val;
        val.prepareJavaRepresentation();
        if (bigInteger.numberLength < 2 || (bigInteger.numberLength == 2 && bigInteger.digits[1] > 0)) {
            return (double) val.longValue();
        }
        double d = Double.NEGATIVE_INFINITY;
        if (bigInteger.numberLength > 32) {
            if (bigInteger.sign > 0) {
                d = Double.POSITIVE_INFINITY;
            }
            return d;
        }
        long exponent = (long) (val.abs().bitLength() - 1);
        int delta = val.abs().bitLength() - 54;
        long mantissa = val.abs().shiftRight(delta).longValue() & 9007199254740991L;
        if (exponent == 1023) {
            if (mantissa == 9007199254740991L) {
                if (bigInteger.sign > 0) {
                    d = Double.POSITIVE_INFINITY;
                }
                return d;
            } else if (mantissa == 9007199254740990L) {
                return bigInteger.sign > 0 ? Double.MAX_VALUE : -1.7976931348623157E308d;
            }
        }
        if ((mantissa & 1) == 1 && ((mantissa & 2) == 2 || BitLevel.nonZeroDroppedBits(delta, bigInteger.digits))) {
            mantissa += 2;
        }
        return Double.longBitsToDouble((bigInteger.sign < 0 ? Long.MIN_VALUE : 0) | (((1023 + exponent) << 52) & 9218868437227405312L) | (mantissa >> 1));
    }
}
