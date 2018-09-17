package java.math;

class Logical {
    private Logical() {
    }

    static BigInteger not(BigInteger val) {
        if (val.sign == 0) {
            return BigInteger.MINUS_ONE;
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return BigInteger.ZERO;
        }
        int i;
        int[] resDigits = new int[(val.numberLength + 1)];
        if (val.sign <= 0) {
            i = 0;
            while (val.digits[i] == 0) {
                resDigits[i] = -1;
                i++;
            }
        } else if (val.digits[val.numberLength - 1] != -1) {
            i = 0;
            while (val.digits[i] == -1) {
                i++;
            }
        } else {
            i = 0;
            while (i < val.numberLength && val.digits[i] == -1) {
                i++;
            }
            if (i == val.numberLength) {
                resDigits[i] = 1;
                return new BigInteger(-val.sign, i + 1, resDigits);
            }
        }
        resDigits[i] = val.digits[i] + val.sign;
        while (true) {
            i++;
            if (i >= val.numberLength) {
                return new BigInteger(-val.sign, i, resDigits);
            }
            resDigits[i] = val.digits[i];
        }
    }

    static BigInteger and(BigInteger val, BigInteger that) {
        if (that.sign == 0 || val.sign == 0) {
            return BigInteger.ZERO;
        }
        if (that.equals(BigInteger.MINUS_ONE)) {
            return val;
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return that;
        }
        if (val.sign > 0) {
            if (that.sign > 0) {
                return andPositive(val, that);
            }
            return andDiffSigns(val, that);
        } else if (that.sign > 0) {
            return andDiffSigns(that, val);
        } else {
            if (val.numberLength > that.numberLength) {
                return andNegative(val, that);
            }
            return andNegative(that, val);
        }
    }

    static BigInteger andPositive(BigInteger val, BigInteger that) {
        int resLength = Math.min(val.numberLength, that.numberLength);
        int i = Math.max(val.getFirstNonzeroDigit(), that.getFirstNonzeroDigit());
        if (i >= resLength) {
            return BigInteger.ZERO;
        }
        int[] resDigits = new int[resLength];
        while (i < resLength) {
            resDigits[i] = val.digits[i] & that.digits[i];
            i++;
        }
        return new BigInteger(1, resLength, resDigits);
    }

    static BigInteger andDiffSigns(BigInteger positive, BigInteger negative) {
        int iPos = positive.getFirstNonzeroDigit();
        int iNeg = negative.getFirstNonzeroDigit();
        if (iNeg >= positive.numberLength) {
            return BigInteger.ZERO;
        }
        int resLength = positive.numberLength;
        int[] resDigits = new int[resLength];
        int i = Math.max(iPos, iNeg);
        if (i == iNeg) {
            resDigits[i] = (-negative.digits[i]) & positive.digits[i];
            i++;
        }
        int limit = Math.min(negative.numberLength, positive.numberLength);
        while (i < limit) {
            resDigits[i] = (~negative.digits[i]) & positive.digits[i];
            i++;
        }
        if (i >= negative.numberLength) {
            while (i < positive.numberLength) {
                resDigits[i] = positive.digits[i];
                i++;
            }
        }
        return new BigInteger(1, resLength, resDigits);
    }

    static BigInteger andNegative(BigInteger longer, BigInteger shorter) {
        int iLonger = longer.getFirstNonzeroDigit();
        int iShorter = shorter.getFirstNonzeroDigit();
        if (iLonger >= shorter.numberLength) {
            return longer;
        }
        int digit;
        int resLength;
        int[] resDigits;
        int i = Math.max(iShorter, iLonger);
        if (iShorter > iLonger) {
            digit = (-shorter.digits[i]) & (~longer.digits[i]);
        } else if (iShorter < iLonger) {
            digit = (~shorter.digits[i]) & (-longer.digits[i]);
        } else {
            digit = (-shorter.digits[i]) & (-longer.digits[i]);
        }
        if (digit == 0) {
            while (true) {
                i++;
                if (i >= shorter.numberLength) {
                    break;
                }
                digit = ~(longer.digits[i] | shorter.digits[i]);
                if (digit != 0) {
                    break;
                }
            }
            if (digit == 0) {
                while (i < longer.numberLength) {
                    digit = ~longer.digits[i];
                    if (digit != 0) {
                        break;
                    }
                    i++;
                }
                if (digit == 0) {
                    resLength = longer.numberLength + 1;
                    resDigits = new int[resLength];
                    resDigits[resLength - 1] = 1;
                    return new BigInteger(-1, resLength, resDigits);
                }
            }
        }
        resLength = longer.numberLength;
        resDigits = new int[resLength];
        resDigits[i] = -digit;
        while (true) {
            i++;
            if (i >= shorter.numberLength) {
                break;
            }
            resDigits[i] = longer.digits[i] | shorter.digits[i];
        }
        while (i < longer.numberLength) {
            resDigits[i] = longer.digits[i];
            i++;
        }
        return new BigInteger(-1, resLength, resDigits);
    }

    static BigInteger andNot(BigInteger val, BigInteger that) {
        if (that.sign == 0) {
            return val;
        }
        if (val.sign == 0) {
            return BigInteger.ZERO;
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return that.not();
        }
        if (that.equals(BigInteger.MINUS_ONE)) {
            return BigInteger.ZERO;
        }
        if (val.sign > 0) {
            if (that.sign > 0) {
                return andNotPositive(val, that);
            }
            return andNotPositiveNegative(val, that);
        } else if (that.sign > 0) {
            return andNotNegativePositive(val, that);
        } else {
            return andNotNegative(val, that);
        }
    }

    static BigInteger andNotPositive(BigInteger val, BigInteger that) {
        int[] resDigits = new int[val.numberLength];
        int limit = Math.min(val.numberLength, that.numberLength);
        int i = val.getFirstNonzeroDigit();
        while (i < limit) {
            resDigits[i] = val.digits[i] & (~that.digits[i]);
            i++;
        }
        while (i < val.numberLength) {
            resDigits[i] = val.digits[i];
            i++;
        }
        return new BigInteger(1, val.numberLength, resDigits);
    }

    static BigInteger andNotPositiveNegative(BigInteger positive, BigInteger negative) {
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
        if (iNeg >= positive.numberLength) {
            return positive;
        }
        int resLength = Math.min(positive.numberLength, negative.numberLength);
        int[] resDigits = new int[resLength];
        int i = iPos;
        while (i < iNeg) {
            resDigits[i] = positive.digits[i];
            i++;
        }
        if (i == iNeg) {
            resDigits[i] = positive.digits[i] & (negative.digits[i] - 1);
            i++;
        }
        while (i < resLength) {
            resDigits[i] = positive.digits[i] & negative.digits[i];
            i++;
        }
        return new BigInteger(1, resLength, resDigits);
    }

    static BigInteger andNotNegativePositive(BigInteger negative, BigInteger positive) {
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
        if (iNeg >= positive.numberLength) {
            return negative;
        }
        int[] resDigits;
        int limit;
        int resLength = Math.max(negative.numberLength, positive.numberLength);
        int i = iNeg;
        if (iPos > iNeg) {
            resDigits = new int[resLength];
            limit = Math.min(negative.numberLength, iPos);
            while (i < limit) {
                resDigits[i] = negative.digits[i];
                i++;
            }
            if (i == negative.numberLength) {
                i = iPos;
                while (i < positive.numberLength) {
                    resDigits[i] = positive.digits[i];
                    i++;
                }
            }
        } else {
            int digit = (-negative.digits[iNeg]) & (~positive.digits[iNeg]);
            if (digit == 0) {
                limit = Math.min(positive.numberLength, negative.numberLength);
                i = iNeg + 1;
                while (i < limit) {
                    digit = ~(negative.digits[i] | positive.digits[i]);
                    if (digit != 0) {
                        break;
                    }
                    i++;
                }
                if (digit == 0) {
                    while (i < positive.numberLength) {
                        digit = ~positive.digits[i];
                        if (digit != 0) {
                            break;
                        }
                        i++;
                    }
                    while (i < negative.numberLength) {
                        digit = ~negative.digits[i];
                        if (digit != 0) {
                            break;
                        }
                        i++;
                    }
                    if (digit == 0) {
                        resLength++;
                        resDigits = new int[resLength];
                        resDigits[resLength - 1] = 1;
                        return new BigInteger(-1, resLength, resDigits);
                    }
                }
            }
            resDigits = new int[resLength];
            resDigits[i] = -digit;
            i++;
        }
        limit = Math.min(positive.numberLength, negative.numberLength);
        while (i < limit) {
            resDigits[i] = negative.digits[i] | positive.digits[i];
            i++;
        }
        while (i < negative.numberLength) {
            resDigits[i] = negative.digits[i];
            i++;
        }
        while (i < positive.numberLength) {
            resDigits[i] = positive.digits[i];
            i++;
        }
        return new BigInteger(-1, resLength, resDigits);
    }

    static BigInteger andNotNegative(BigInteger val, BigInteger that) {
        int iVal = val.getFirstNonzeroDigit();
        int iThat = that.getFirstNonzeroDigit();
        if (iVal >= that.numberLength) {
            return BigInteger.ZERO;
        }
        int limit;
        int resLength = that.numberLength;
        int[] resDigits = new int[resLength];
        int i = iVal;
        if (iVal < iThat) {
            resDigits[iVal] = -val.digits[iVal];
            limit = Math.min(val.numberLength, iThat);
            i = iVal + 1;
            while (i < limit) {
                resDigits[i] = ~val.digits[i];
                i++;
            }
            if (i == val.numberLength) {
                while (i < iThat) {
                    resDigits[i] = -1;
                    i++;
                }
                resDigits[i] = that.digits[i] - 1;
            } else {
                resDigits[i] = (~val.digits[i]) & (that.digits[i] - 1);
            }
        } else if (iThat < iVal) {
            resDigits[iVal] = (-val.digits[iVal]) & that.digits[iVal];
        } else {
            resDigits[iVal] = (-val.digits[iVal]) & (that.digits[iVal] - 1);
        }
        limit = Math.min(val.numberLength, that.numberLength);
        i++;
        while (i < limit) {
            resDigits[i] = (~val.digits[i]) & that.digits[i];
            i++;
        }
        while (i < that.numberLength) {
            resDigits[i] = that.digits[i];
            i++;
        }
        return new BigInteger(1, resLength, resDigits);
    }

    static BigInteger or(BigInteger val, BigInteger that) {
        if (that.equals(BigInteger.MINUS_ONE) || val.equals(BigInteger.MINUS_ONE)) {
            return BigInteger.MINUS_ONE;
        }
        if (that.sign == 0) {
            return val;
        }
        if (val.sign == 0) {
            return that;
        }
        if (val.sign > 0) {
            if (that.sign <= 0) {
                return orDiffSigns(val, that);
            }
            if (val.numberLength > that.numberLength) {
                return orPositive(val, that);
            }
            return orPositive(that, val);
        } else if (that.sign > 0) {
            return orDiffSigns(that, val);
        } else {
            if (that.getFirstNonzeroDigit() > val.getFirstNonzeroDigit()) {
                return orNegative(that, val);
            }
            return orNegative(val, that);
        }
    }

    static BigInteger orPositive(BigInteger longer, BigInteger shorter) {
        int resLength = longer.numberLength;
        int[] resDigits = new int[resLength];
        int i = 0;
        while (i < shorter.numberLength) {
            resDigits[i] = longer.digits[i] | shorter.digits[i];
            i++;
        }
        while (i < resLength) {
            resDigits[i] = longer.digits[i];
            i++;
        }
        return new BigInteger(1, resLength, resDigits);
    }

    static BigInteger orNegative(BigInteger val, BigInteger that) {
        int iThat = that.getFirstNonzeroDigit();
        int iVal = val.getFirstNonzeroDigit();
        if (iVal >= that.numberLength) {
            return that;
        }
        if (iThat >= val.numberLength) {
            return val;
        }
        int i;
        int resLength = Math.min(val.numberLength, that.numberLength);
        int[] resDigits = new int[resLength];
        if (iThat == iVal) {
            resDigits[iVal] = -((-val.digits[iVal]) | (-that.digits[iVal]));
            i = iVal;
        } else {
            i = iThat;
            while (i < iVal) {
                resDigits[i] = that.digits[i];
                i++;
            }
            resDigits[i] = that.digits[i] & (val.digits[i] - 1);
        }
        for (i++; i < resLength; i++) {
            resDigits[i] = val.digits[i] & that.digits[i];
        }
        return new BigInteger(-1, resLength, resDigits);
    }

    static BigInteger orDiffSigns(BigInteger positive, BigInteger negative) {
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
        if (iPos >= negative.numberLength) {
            return negative;
        }
        int i;
        int limit;
        int resLength = negative.numberLength;
        int[] resDigits = new int[resLength];
        if (iNeg < iPos) {
            i = iNeg;
            while (i < iPos) {
                resDigits[i] = negative.digits[i];
                i++;
            }
        } else if (iPos < iNeg) {
            i = iPos;
            resDigits[iPos] = -positive.digits[iPos];
            limit = Math.min(positive.numberLength, iNeg);
            i = iPos + 1;
            while (i < limit) {
                resDigits[i] = ~positive.digits[i];
                i++;
            }
            if (i != positive.numberLength) {
                resDigits[i] = ~((-negative.digits[i]) | positive.digits[i]);
            } else {
                while (i < iNeg) {
                    resDigits[i] = -1;
                    i++;
                }
                resDigits[i] = negative.digits[i] - 1;
            }
            i++;
        } else {
            i = iPos;
            resDigits[iPos] = -((-negative.digits[iPos]) | positive.digits[iPos]);
            i = iPos + 1;
        }
        limit = Math.min(negative.numberLength, positive.numberLength);
        while (i < limit) {
            resDigits[i] = negative.digits[i] & (~positive.digits[i]);
            i++;
        }
        while (i < negative.numberLength) {
            resDigits[i] = negative.digits[i];
            i++;
        }
        return new BigInteger(-1, resLength, resDigits);
    }

    static BigInteger xor(BigInteger val, BigInteger that) {
        if (that.sign == 0) {
            return val;
        }
        if (val.sign == 0) {
            return that;
        }
        if (that.equals(BigInteger.MINUS_ONE)) {
            return val.not();
        }
        if (val.equals(BigInteger.MINUS_ONE)) {
            return that.not();
        }
        if (val.sign > 0) {
            if (that.sign <= 0) {
                return xorDiffSigns(val, that);
            }
            if (val.numberLength > that.numberLength) {
                return xorPositive(val, that);
            }
            return xorPositive(that, val);
        } else if (that.sign > 0) {
            return xorDiffSigns(that, val);
        } else {
            if (that.getFirstNonzeroDigit() > val.getFirstNonzeroDigit()) {
                return xorNegative(that, val);
            }
            return xorNegative(val, that);
        }
    }

    static BigInteger xorPositive(BigInteger longer, BigInteger shorter) {
        int resLength = longer.numberLength;
        int[] resDigits = new int[resLength];
        int i = Math.min(longer.getFirstNonzeroDigit(), shorter.getFirstNonzeroDigit());
        while (i < shorter.numberLength) {
            resDigits[i] = longer.digits[i] ^ shorter.digits[i];
            i++;
        }
        while (i < longer.numberLength) {
            resDigits[i] = longer.digits[i];
            i++;
        }
        return new BigInteger(1, resLength, resDigits);
    }

    static BigInteger xorNegative(BigInteger val, BigInteger that) {
        int limit;
        int resLength = Math.max(val.numberLength, that.numberLength);
        int[] resDigits = new int[resLength];
        int iVal = val.getFirstNonzeroDigit();
        int iThat = that.getFirstNonzeroDigit();
        int i = iThat;
        if (iVal == iThat) {
            resDigits[iThat] = (-val.digits[iThat]) ^ (-that.digits[iThat]);
        } else {
            resDigits[iThat] = -that.digits[iThat];
            limit = Math.min(that.numberLength, iVal);
            i = iThat + 1;
            while (i < limit) {
                resDigits[i] = ~that.digits[i];
                i++;
            }
            if (i == that.numberLength) {
                while (i < iVal) {
                    resDigits[i] = -1;
                    i++;
                }
                resDigits[i] = val.digits[i] - 1;
            } else {
                resDigits[i] = (-val.digits[i]) ^ (~that.digits[i]);
            }
        }
        limit = Math.min(val.numberLength, that.numberLength);
        i++;
        while (i < limit) {
            resDigits[i] = val.digits[i] ^ that.digits[i];
            i++;
        }
        while (i < val.numberLength) {
            resDigits[i] = val.digits[i];
            i++;
        }
        while (i < that.numberLength) {
            resDigits[i] = that.digits[i];
            i++;
        }
        return new BigInteger(1, resLength, resDigits);
    }

    static BigInteger xorDiffSigns(BigInteger positive, BigInteger negative) {
        int[] resDigits;
        int i;
        int limit;
        int resLength = Math.max(negative.numberLength, positive.numberLength);
        int iNeg = negative.getFirstNonzeroDigit();
        int iPos = positive.getFirstNonzeroDigit();
        if (iNeg < iPos) {
            resDigits = new int[resLength];
            i = iNeg;
            resDigits[iNeg] = negative.digits[iNeg];
            limit = Math.min(negative.numberLength, iPos);
            i = iNeg + 1;
            while (i < limit) {
                resDigits[i] = negative.digits[i];
                i++;
            }
            if (i == negative.numberLength) {
                while (i < positive.numberLength) {
                    resDigits[i] = positive.digits[i];
                    i++;
                }
            }
        } else if (iPos < iNeg) {
            resDigits = new int[resLength];
            i = iPos;
            resDigits[iPos] = -positive.digits[iPos];
            limit = Math.min(positive.numberLength, iNeg);
            i = iPos + 1;
            while (i < limit) {
                resDigits[i] = ~positive.digits[i];
                i++;
            }
            if (i == iNeg) {
                resDigits[i] = ~(positive.digits[i] ^ (-negative.digits[i]));
                i++;
            } else {
                while (i < iNeg) {
                    resDigits[i] = -1;
                    i++;
                }
                while (i < negative.numberLength) {
                    resDigits[i] = negative.digits[i];
                    i++;
                }
            }
        } else {
            i = iNeg;
            int digit = positive.digits[iNeg] ^ (-negative.digits[iNeg]);
            if (digit == 0) {
                limit = Math.min(positive.numberLength, negative.numberLength);
                i = iNeg + 1;
                while (i < limit) {
                    digit = positive.digits[i] ^ (~negative.digits[i]);
                    if (digit != 0) {
                        break;
                    }
                    i++;
                }
                if (digit == 0) {
                    while (i < positive.numberLength) {
                        digit = ~positive.digits[i];
                        if (digit != 0) {
                            break;
                        }
                        i++;
                    }
                    while (i < negative.numberLength) {
                        digit = ~negative.digits[i];
                        if (digit != 0) {
                            break;
                        }
                        i++;
                    }
                    if (digit == 0) {
                        resLength++;
                        resDigits = new int[resLength];
                        resDigits[resLength - 1] = 1;
                        return new BigInteger(-1, resLength, resDigits);
                    }
                }
            }
            resDigits = new int[resLength];
            resDigits[i] = -digit;
            i++;
        }
        limit = Math.min(negative.numberLength, positive.numberLength);
        while (i < limit) {
            resDigits[i] = ~((~negative.digits[i]) ^ positive.digits[i]);
            i++;
        }
        while (i < positive.numberLength) {
            resDigits[i] = positive.digits[i];
            i++;
        }
        while (i < negative.numberLength) {
            resDigits[i] = negative.digits[i];
            i++;
        }
        return new BigInteger(-1, resLength, resDigits);
    }
}
