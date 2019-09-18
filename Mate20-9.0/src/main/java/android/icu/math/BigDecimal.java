package android.icu.math;

import android.icu.lang.UCharacter;
import android.icu.text.PluralRules;
import java.io.Serializable;
import java.math.BigInteger;

public class BigDecimal extends Number implements Serializable, Comparable<BigDecimal> {
    private static final int MaxArg = 999999999;
    private static final int MaxExp = 999999999;
    private static final int MinArg = -999999999;
    private static final int MinExp = -999999999;
    public static final BigDecimal ONE = new BigDecimal(1);
    public static final int ROUND_CEILING = 2;
    public static final int ROUND_DOWN = 1;
    public static final int ROUND_FLOOR = 3;
    public static final int ROUND_HALF_DOWN = 5;
    public static final int ROUND_HALF_EVEN = 6;
    public static final int ROUND_HALF_UP = 4;
    public static final int ROUND_UNNECESSARY = 7;
    public static final int ROUND_UP = 0;
    public static final BigDecimal TEN = new BigDecimal(10);
    public static final BigDecimal ZERO = new BigDecimal(0);
    private static byte[] bytecar = new byte[190];
    private static byte[] bytedig = diginit();
    private static final byte isneg = -1;
    private static final byte ispos = 1;
    private static final byte iszero = 0;
    private static final MathContext plainMC = new MathContext(0, 0);
    private static final long serialVersionUID = 8245355804974198832L;
    private int exp;
    private byte form;
    private byte ind;
    private byte[] mant;

    public BigDecimal(java.math.BigDecimal bd) {
        this(bd.toString());
    }

    public BigDecimal(BigInteger bi) {
        this(bi.toString(10));
    }

    public BigDecimal(BigInteger bi, int scale) {
        this(bi.toString(10));
        if (scale >= 0) {
            this.exp = -scale;
            return;
        }
        throw new NumberFormatException("Negative scale: " + scale);
    }

    public BigDecimal(char[] inchars) {
        this(inchars, 0, inchars.length);
    }

    public BigDecimal(char[] inchars, int offset, int length) {
        int length2;
        int offset2;
        char si;
        int offset3;
        int k;
        int dvalue;
        this.form = 0;
        char si2 = 0;
        if (length <= 0) {
            bad(inchars);
        }
        this.ind = 1;
        if (inchars[offset] == '-') {
            length2 = length - 1;
            if (length2 == 0) {
                bad(inchars);
            }
            this.ind = -1;
            offset2 = offset + 1;
        } else if (inchars[offset] == '+') {
            length2 = length - 1;
            if (length2 == 0) {
                bad(inchars);
            }
            offset2 = offset + 1;
        } else {
            offset2 = offset;
            length2 = length;
        }
        boolean exotic = false;
        boolean hadexp = false;
        int d = 0;
        int dotoff = -1;
        int last = -1;
        int $1 = length2;
        int i = offset2;
        while (true) {
            if ($1 <= 0) {
                break;
            }
            si2 = inchars[i];
            if (si2 >= '0' && si2 <= '9') {
                d++;
                last = i;
            } else if (si2 == '.') {
                if (dotoff >= 0) {
                    bad(inchars);
                }
                dotoff = i - offset2;
            } else if (si2 != 'e' && si2 != 'E') {
                if (!UCharacter.isDigit(si2)) {
                    bad(inchars);
                }
                d++;
                exotic = true;
                last = i;
            }
            $1--;
            i++;
        }
        if (i - offset2 > length2 - 2) {
            bad(inchars);
        }
        boolean eneg = false;
        if (inchars[i + 1] == '-') {
            eneg = true;
            k = i + 2;
        } else if (inchars[i + 1] == '+') {
            k = i + 2;
        } else {
            k = i + 1;
        }
        int k2 = k;
        boolean eneg2 = eneg;
        int elen = length2 - (k2 - offset2);
        if ((elen == 0) || (elen > 9)) {
            bad(inchars);
        }
        int $2 = elen;
        int j = k2;
        while ($2 > 0) {
            char sj = inchars[j];
            if (sj < '0') {
                bad(inchars);
            }
            if (sj > '9') {
                if (!UCharacter.isDigit(sj)) {
                    bad(inchars);
                }
                dvalue = UCharacter.digit(sj, 10);
                if (dvalue < 0) {
                    bad(inchars);
                }
            } else {
                dvalue = sj - '0';
            }
            this.exp = (this.exp * 10) + dvalue;
            $2--;
            j++;
        }
        if (eneg2) {
            this.exp = -this.exp;
        }
        hadexp = true;
        if (d == 0) {
            bad(inchars);
        }
        if (dotoff >= 0) {
            this.exp = (this.exp + dotoff) - d;
        }
        int $3 = last - 1;
        int i2 = offset2;
        int i3 = length2;
        int d2 = d;
        int dotoff2 = dotoff;
        while (i2 <= $3) {
            si2 = inchars[i2];
            int $32 = $3;
            if (si2 != '0') {
                if (si2 != '.') {
                    if (si2 <= '9' || UCharacter.digit(si2, 10) != 0) {
                        break;
                    }
                    offset3 = offset2 + 1;
                    dotoff2--;
                    d2--;
                } else {
                    offset3 = offset2 + 1;
                    dotoff2--;
                }
            } else {
                offset3 = offset2 + 1;
                dotoff2--;
                d2--;
            }
            i2++;
            $3 = $32;
        }
        this.mant = new byte[d2];
        int j2 = offset2;
        if (exotic) {
            int $4 = d2;
            int i4 = 0;
            while ($4 > 0) {
                j2 = i4 == dotoff2 ? j2 + 1 : j2;
                char sj2 = inchars[j2];
                int offset4 = offset2;
                if (sj2 <= '9') {
                    si = si2;
                    this.mant[i4] = (byte) (sj2 - '0');
                } else {
                    si = si2;
                    int dvalue2 = UCharacter.digit(sj2, 10);
                    if (dvalue2 < 0) {
                        bad(inchars);
                    }
                    this.mant[i4] = (byte) dvalue2;
                    int i5 = dvalue2;
                }
                j2++;
                $4--;
                i4++;
                offset2 = offset4;
                si2 = si;
            }
            char c = si2;
        } else {
            char c2 = si2;
            int $5 = d2;
            int i6 = 0;
            while ($5 > 0) {
                if (i6 == dotoff2) {
                    j2++;
                }
                this.mant[i6] = (byte) (inchars[j2] - '0');
                j2++;
                $5--;
                i6++;
            }
        }
        if (this.mant[0] == 0) {
            this.ind = 0;
            if (this.exp > 0) {
                this.exp = 0;
            }
            if (hadexp) {
                this.mant = ZERO.mant;
                this.exp = 0;
            }
        } else if (hadexp) {
            boolean z = true;
            this.form = 1;
            int mag = (this.exp + this.mant.length) - 1;
            if ((mag <= 999999999 ? false : z) || (mag < -999999999)) {
                bad(inchars);
            }
        }
    }

    public BigDecimal(double num) {
        this(new java.math.BigDecimal(num).toString());
    }

    public BigDecimal(int num) {
        this.form = 0;
        if (num > 9 || num < -9) {
            if (num > 0) {
                this.ind = 1;
                num = -num;
            } else {
                this.ind = -1;
            }
            int mun = num;
            int i = 9;
            while (true) {
                mun /= 10;
                if (mun == 0) {
                    break;
                }
                i--;
            }
            this.mant = new byte[(10 - i)];
            int i2 = (10 - i) - 1;
            while (true) {
                this.mant[i2] = (byte) (-((byte) (num % 10)));
                num /= 10;
                if (num != 0) {
                    i2--;
                } else {
                    return;
                }
            }
        } else {
            if (num == 0) {
                this.mant = ZERO.mant;
                this.ind = 0;
            } else if (num == 1) {
                this.mant = ONE.mant;
                this.ind = 1;
            } else if (num == -1) {
                this.mant = ONE.mant;
                this.ind = -1;
            } else {
                this.mant = new byte[1];
                if (num > 0) {
                    this.mant[0] = (byte) num;
                    this.ind = 1;
                } else {
                    this.mant[0] = (byte) (-num);
                    this.ind = -1;
                }
            }
        }
    }

    public BigDecimal(long num) {
        this.form = 0;
        if (num > 0) {
            this.ind = 1;
            num = -num;
        } else if (num == 0) {
            this.ind = 0;
        } else {
            this.ind = -1;
        }
        long mun = num;
        int i = 18;
        while (true) {
            mun /= 10;
            if (mun == 0) {
                break;
            }
            i--;
        }
        this.mant = new byte[(19 - i)];
        int i2 = (19 - i) - 1;
        while (true) {
            this.mant[i2] = (byte) (-((byte) ((int) (num % 10))));
            num /= 10;
            if (num != 0) {
                i2--;
            } else {
                return;
            }
        }
    }

    public BigDecimal(String string) {
        this(string.toCharArray(), 0, string.length());
    }

    private BigDecimal() {
        this.form = 0;
    }

    public BigDecimal abs() {
        return abs(plainMC);
    }

    public BigDecimal abs(MathContext set) {
        if (this.ind == -1) {
            return negate(set);
        }
        return plus(set);
    }

    public BigDecimal add(BigDecimal rhs) {
        return add(rhs, plainMC);
    }

    public BigDecimal add(BigDecimal rhs, MathContext set) {
        BigDecimal bigDecimal;
        int newlen;
        int mult;
        int i;
        byte ca;
        byte cb;
        BigDecimal rhs2 = rhs;
        MathContext mathContext = set;
        if (mathContext.lostDigits) {
            bigDecimal = this;
            bigDecimal.checkdigits(rhs2, mathContext.digits);
        } else {
            bigDecimal = this;
        }
        BigDecimal lhs = bigDecimal;
        if (lhs.ind == 0 && mathContext.form != 0) {
            return rhs.plus(set);
        }
        if (rhs2.ind == 0 && mathContext.form != 0) {
            return lhs.plus(mathContext);
        }
        int reqdig = mathContext.digits;
        if (reqdig > 0) {
            if (lhs.mant.length > reqdig) {
                lhs = clone(lhs).round(mathContext);
            }
            if (rhs2.mant.length > reqdig) {
                rhs2 = clone(rhs).round(mathContext);
            }
        }
        BigDecimal res = new BigDecimal();
        byte[] usel = lhs.mant;
        int usellen = lhs.mant.length;
        byte[] user = rhs2.mant;
        int userlen = rhs2.mant.length;
        if (lhs.exp == rhs2.exp) {
            res.exp = lhs.exp;
            newlen = 0;
        } else if (lhs.exp > rhs2.exp) {
            newlen = (lhs.exp + usellen) - rhs2.exp;
            if (newlen < userlen + reqdig + 1 || reqdig <= 0) {
                res.exp = rhs2.exp;
                if (newlen > reqdig + 1 && reqdig > 0) {
                    int tlen = (newlen - reqdig) - 1;
                    userlen -= tlen;
                    res.exp += tlen;
                    newlen = reqdig + 1;
                    int i2 = tlen;
                }
                if (newlen > usellen) {
                    usellen = newlen;
                }
            } else {
                res.mant = usel;
                res.exp = lhs.exp;
                res.ind = lhs.ind;
                if (usellen < reqdig) {
                    res.mant = extend(lhs.mant, reqdig);
                    res.exp -= reqdig - usellen;
                }
                return res.finish(mathContext, false);
            }
        } else {
            int newlen2 = (rhs2.exp + userlen) - lhs.exp;
            if (newlen2 < usellen + reqdig + 1 || reqdig <= 0) {
                res.exp = lhs.exp;
                if (newlen2 > reqdig + 1 && reqdig > 0) {
                    int tlen2 = (newlen2 - reqdig) - 1;
                    usellen -= tlen2;
                    res.exp += tlen2;
                    newlen2 = reqdig + 1;
                    int i3 = tlen2;
                }
                if (newlen > userlen) {
                    userlen = newlen;
                }
            } else {
                res.mant = user;
                res.exp = rhs2.exp;
                res.ind = rhs2.ind;
                if (userlen < reqdig) {
                    res.mant = extend(rhs2.mant, reqdig);
                    res.exp -= reqdig - userlen;
                }
                return res.finish(mathContext, false);
            }
        }
        if (lhs.ind == 0) {
            res.ind = 1;
        } else {
            res.ind = lhs.ind;
        }
        int i4 = newlen;
        if ((lhs.ind == -1) == (rhs2.ind == -1)) {
            i = 1;
        } else {
            i = -1;
            if (rhs2.ind != 0) {
                if ((usellen < userlen) || (lhs.ind == 0)) {
                    byte[] t = usel;
                    usel = user;
                    user = t;
                    int tlen3 = usellen;
                    usellen = userlen;
                    userlen = tlen3;
                    res.ind = (byte) (-res.ind);
                    BigDecimal bigDecimal2 = rhs2;
                    mult = -1;
                    byte[] bArr = t;
                } else if (usellen <= userlen) {
                    BigDecimal bigDecimal3 = rhs2;
                    int ea = usel.length - 1;
                    mult = -1;
                    int eb = user.length - 1;
                    int ia = 0;
                    int ib = 0;
                    while (true) {
                        if (ia <= ea) {
                            ca = usel[ia];
                        } else if (ib <= eb) {
                            ca = 0;
                        } else if (mathContext.form != 0) {
                            return ZERO;
                        } else {
                            int i5 = ea;
                            int i6 = ia;
                            int i7 = ib;
                        }
                        byte ca2 = ca;
                        if (ib <= eb) {
                            cb = user[ib];
                        } else {
                            cb = 0;
                        }
                        byte cb2 = cb;
                        if (ca2 == cb2) {
                            ia++;
                            ib++;
                            ea = ea;
                        } else if (ca2 < cb2) {
                            byte[] t2 = usel;
                            usel = user;
                            user = t2;
                            int tlen4 = usellen;
                            usellen = userlen;
                            userlen = tlen4;
                            res.ind = (byte) (-res.ind);
                            int i8 = ia;
                            int i9 = ib;
                            byte[] bArr2 = t2;
                            int i10 = ea;
                        } else {
                            int i11 = ia;
                            int i12 = ib;
                            int i13 = ea;
                        }
                    }
                }
                res.mant = byteaddsub(usel, usellen, user, userlen, mult, false);
                return res.finish(mathContext, false);
            }
        }
        mult = i;
        res.mant = byteaddsub(usel, usellen, user, userlen, mult, false);
        return res.finish(mathContext, false);
    }

    public int compareTo(BigDecimal rhs) {
        return compareTo(rhs, plainMC);
    }

    public int compareTo(BigDecimal rhs, MathContext set) {
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        boolean z = true;
        if ((this.ind == rhs.ind) && (this.exp == rhs.exp)) {
            int thislength = this.mant.length;
            if (thislength < rhs.mant.length) {
                return (byte) (-this.ind);
            }
            if (thislength > rhs.mant.length) {
                return this.ind;
            }
            boolean z2 = thislength <= set.digits;
            if (set.digits != 0) {
                z = false;
            }
            if (z2 || z) {
                int $6 = thislength;
                int i = 0;
                while ($6 > 0) {
                    if (this.mant[i] < rhs.mant[i]) {
                        return (byte) (-this.ind);
                    }
                    if (this.mant[i] > rhs.mant[i]) {
                        return this.ind;
                    }
                    $6--;
                    i++;
                }
                return 0;
            }
        } else if (this.ind < rhs.ind) {
            return -1;
        } else {
            if (this.ind > rhs.ind) {
                return 1;
            }
        }
        BigDecimal newrhs = clone(rhs);
        newrhs.ind = (byte) (-newrhs.ind);
        return add(newrhs, set).ind;
    }

    public BigDecimal divide(BigDecimal rhs) {
        return dodivide('D', rhs, plainMC, -1);
    }

    public BigDecimal divide(BigDecimal rhs, int round) {
        return dodivide('D', rhs, new MathContext(0, 0, false, round), -1);
    }

    public BigDecimal divide(BigDecimal rhs, int scale, int round) {
        if (scale >= 0) {
            return dodivide('D', rhs, new MathContext(0, 0, false, round), scale);
        }
        throw new ArithmeticException("Negative scale: " + scale);
    }

    public BigDecimal divide(BigDecimal rhs, MathContext set) {
        return dodivide('D', rhs, set, -1);
    }

    public BigDecimal divideInteger(BigDecimal rhs) {
        return dodivide('I', rhs, plainMC, 0);
    }

    public BigDecimal divideInteger(BigDecimal rhs, MathContext set) {
        return dodivide('I', rhs, set, 0);
    }

    public BigDecimal max(BigDecimal rhs) {
        return max(rhs, plainMC);
    }

    public BigDecimal max(BigDecimal rhs, MathContext set) {
        if (compareTo(rhs, set) >= 0) {
            return plus(set);
        }
        return rhs.plus(set);
    }

    public BigDecimal min(BigDecimal rhs) {
        return min(rhs, plainMC);
    }

    public BigDecimal min(BigDecimal rhs, MathContext set) {
        if (compareTo(rhs, set) <= 0) {
            return plus(set);
        }
        return rhs.plus(set);
    }

    public BigDecimal multiply(BigDecimal rhs) {
        return multiply(rhs, plainMC);
    }

    public BigDecimal multiply(BigDecimal rhs, MathContext set) {
        BigDecimal bigDecimal;
        byte[] multand;
        byte[] multer;
        int acclen;
        int acclen2;
        byte[] multand2;
        byte[] multer2;
        boolean z;
        BigDecimal res;
        BigDecimal rhs2 = rhs;
        MathContext mathContext = set;
        if (mathContext.lostDigits) {
            bigDecimal = this;
            bigDecimal.checkdigits(rhs2, mathContext.digits);
        } else {
            bigDecimal = this;
        }
        BigDecimal lhs = bigDecimal;
        int padding = 0;
        int reqdig = mathContext.digits;
        if (reqdig > 0) {
            if (lhs.mant.length > reqdig) {
                lhs = clone(lhs).round(mathContext);
            }
            if (rhs2.mant.length > reqdig) {
                rhs2 = clone(rhs).round(mathContext);
            }
        } else {
            if (lhs.exp > 0) {
                padding = 0 + lhs.exp;
            }
            if (rhs2.exp > 0) {
                padding += rhs2.exp;
            }
        }
        if (lhs.mant.length < rhs2.mant.length) {
            multer = lhs.mant;
            multand = rhs2.mant;
        } else {
            multer = rhs2.mant;
            multand = lhs.mant;
        }
        int multandlen = (multer.length + multand.length) - 1;
        boolean z2 = false;
        if (multer[0] * multand[0] > 9) {
            acclen = multandlen + 1;
        } else {
            acclen = multandlen;
        }
        BigDecimal res2 = new BigDecimal();
        int multandlen2 = multandlen;
        int n = 0;
        int $7 = multer.length;
        byte[] acc = new byte[acclen];
        while ($7 > 0) {
            byte mult = multer[n];
            if (mult != 0) {
                int length = acc.length;
                byte[] bArr = acc;
                multer2 = multer;
                byte[] multer3 = acc;
                byte[] acc2 = multand;
                multand2 = multand;
                res = res2;
                acclen2 = acclen;
                z = z2;
                acc = byteaddsub(bArr, length, acc2, multandlen2, mult, true);
            } else {
                multer2 = multer;
                multand2 = multand;
                acclen2 = acclen;
                byte[] multer4 = acc;
                res = res2;
                z = z2;
            }
            multandlen2--;
            $7--;
            n++;
            res2 = res;
            z2 = z;
            byte b = mult;
            multer = multer2;
            multand = multand2;
            acclen = acclen2;
        }
        byte[] bArr2 = multand;
        int i = acclen;
        byte[] acc3 = acc;
        BigDecimal res3 = res2;
        boolean z3 = z2;
        res3.ind = (byte) (lhs.ind * rhs2.ind);
        res3.exp = (lhs.exp + rhs2.exp) - padding;
        if (padding == 0) {
            res3.mant = acc3;
        } else {
            res3.mant = extend(acc3, acc3.length + padding);
        }
        return res3.finish(mathContext, z3);
    }

    public BigDecimal negate() {
        return negate(plainMC);
    }

    public BigDecimal negate(MathContext set) {
        if (set.lostDigits) {
            checkdigits(null, set.digits);
        }
        BigDecimal res = clone(this);
        res.ind = (byte) (-res.ind);
        return res.finish(set, false);
    }

    public BigDecimal plus() {
        return plus(plainMC);
    }

    public BigDecimal plus(MathContext set) {
        if (set.lostDigits) {
            checkdigits(null, set.digits);
        }
        if (set.form == 0 && this.form == 0 && (this.mant.length <= set.digits || set.digits == 0)) {
            return this;
        }
        return clone(this).finish(set, false);
    }

    public BigDecimal pow(BigDecimal rhs) {
        return pow(rhs, plainMC);
    }

    public BigDecimal pow(BigDecimal rhs, MathContext set) {
        int workdigits;
        int L = 0;
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        int n = rhs.intcheck(-999999999, 999999999);
        BigDecimal lhs = this;
        int reqdig = set.digits;
        if (reqdig == 0) {
            if (rhs.ind != -1) {
                workdigits = 0;
            } else {
                throw new ArithmeticException("Negative power: " + rhs.toString());
            }
        } else if (rhs.mant.length + rhs.exp <= reqdig) {
            if (lhs.mant.length > reqdig) {
                lhs = clone(lhs).round(set);
            }
            L = rhs.mant.length + rhs.exp;
            workdigits = reqdig + L + 1;
        } else {
            throw new ArithmeticException("Too many digits: " + rhs.toString());
        }
        BigDecimal lhs2 = lhs;
        MathContext workset = new MathContext(workdigits, set.form, false, set.roundingMode);
        BigDecimal res = ONE;
        if (n == 0) {
            return res;
        }
        if (n < 0) {
            n = -n;
        }
        boolean seenbit = false;
        int i = 1;
        while (true) {
            n += n;
            if (n < 0) {
                seenbit = true;
                res = res.multiply(lhs2, workset);
            }
            if (i == 31) {
                break;
            }
            if (seenbit) {
                res = res.multiply(res, workset);
            }
            i++;
        }
        if (rhs.ind < 0) {
            res = ONE.divide(res, workset);
        }
        return res.finish(set, true);
    }

    public BigDecimal remainder(BigDecimal rhs) {
        return dodivide('R', rhs, plainMC, -1);
    }

    public BigDecimal remainder(BigDecimal rhs, MathContext set) {
        return dodivide('R', rhs, set, -1);
    }

    public BigDecimal subtract(BigDecimal rhs) {
        return subtract(rhs, plainMC);
    }

    public BigDecimal subtract(BigDecimal rhs, MathContext set) {
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        BigDecimal newrhs = clone(rhs);
        newrhs.ind = (byte) (-newrhs.ind);
        return add(newrhs, set);
    }

    public byte byteValueExact() {
        int num = intValueExact();
        boolean z = false;
        boolean z2 = num > 127;
        if (num < -128) {
            z = true;
        }
        if (!z && !z2) {
            return (byte) num;
        }
        throw new ArithmeticException("Conversion overflow: " + toString());
    }

    public double doubleValue() {
        return Double.valueOf(toString()).doubleValue();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BigDecimal)) {
            return false;
        }
        BigDecimal rhs = (BigDecimal) obj;
        if (this.ind != rhs.ind) {
            return false;
        }
        if (((this.mant.length == rhs.mant.length) & (this.exp == rhs.exp)) && (this.form == rhs.form)) {
            int $8 = this.mant.length;
            int i = 0;
            while ($8 > 0) {
                if (this.mant[i] != rhs.mant[i]) {
                    return false;
                }
                $8--;
                i++;
            }
        } else {
            char[] lca = layout();
            char[] rca = rhs.layout();
            if (lca.length != rca.length) {
                return false;
            }
            int $9 = lca.length;
            int i2 = 0;
            while ($9 > 0) {
                if (lca[i2] != rca[i2]) {
                    return false;
                }
                $9--;
                i2++;
            }
        }
        return true;
    }

    public float floatValue() {
        return Float.valueOf(toString()).floatValue();
    }

    public String format(int before, int after) {
        return format(before, after, -1, -1, 1, 4);
    }

    /* JADX WARNING: Removed duplicated region for block: B:113:0x01dc  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00bc  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00e2 A[LOOP:0: B:67:0x00e2->B:92:0x0174, LOOP_START, PHI: r7 r8 
      PHI: (r7v30 'exformint' int) = (r7v4 'exformint' int), (r7v34 'exformint' int) binds: [B:66:0x00e0, B:92:0x0174] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r8v27 'mag' int) = (r8v8 'mag' int), (r8v30 'mag' int) binds: [B:66:0x00e0, B:92:0x0174] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x018e  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x019a  */
    public String format(int before, int after, int explaces, int exdigits, int exformint, int exround) {
        int exround2;
        int mag;
        int lead;
        int thisafter;
        int i = before;
        int i2 = after;
        int i3 = explaces;
        int i4 = exdigits;
        int i5 = exformint;
        int i6 = exround;
        if ((i < -1) || (i == 0)) {
            badarg("format", 1, String.valueOf(before));
        }
        if (i2 < -1) {
            badarg("format", 2, String.valueOf(after));
        }
        if ((i3 < -1) || (i3 == 0)) {
            badarg("format", 3, String.valueOf(explaces));
        }
        if (i4 < -1) {
            badarg("format", 4, String.valueOf(explaces));
        }
        if (!(i5 == 1 || i5 == 2)) {
            if (i5 == -1) {
                i5 = 1;
            } else {
                badarg("format", 5, String.valueOf(exformint));
            }
        }
        int exformint2 = i5;
        if (i6 == 4) {
            exround2 = i6;
        } else if (i6 == -1) {
            exround2 = 4;
        } else {
            try {
                try {
                    new MathContext(9, 1, false, i6);
                    exround2 = i6;
                } catch (IllegalArgumentException e) {
                    badarg("format", 6, String.valueOf(exround));
                    exround2 = i6;
                    BigDecimal num = clone(this);
                    if (i4 == -1) {
                    }
                    mag = 0;
                    if (i2 < 0) {
                    }
                    char[] a = num.layout();
                    if (i > 0) {
                    }
                    if (i3 > 0) {
                    }
                    return new String(a);
                }
            } catch (IllegalArgumentException e2) {
                badarg("format", 6, String.valueOf(exround));
                exround2 = i6;
                BigDecimal num2 = clone(this);
                if (i4 == -1) {
                }
                mag = 0;
                if (i2 < 0) {
                }
                char[] a2 = num2.layout();
                if (i > 0) {
                }
                if (i3 > 0) {
                }
                return new String(a2);
            }
        }
        BigDecimal num22 = clone(this);
        if (i4 == -1) {
            num22.form = 0;
        } else if (num22.ind == 0) {
            num22.form = 0;
        } else {
            mag = num22.exp + num22.mant.length;
            if (mag > i4) {
                num22.form = (byte) exformint2;
            } else if (mag < -5) {
                num22.form = (byte) exformint2;
            } else {
                num22.form = 0;
            }
            if (i2 < 0) {
                while (true) {
                    if (num22.form == 0) {
                        lead = -num22.exp;
                    } else if (num22.form == 1) {
                        lead = num22.mant.length - 1;
                    } else {
                        int lead2 = ((num22.exp + num22.mant.length) - 1) % 3;
                        if (lead2 < 0) {
                            lead2 = 3 + lead2;
                        }
                        int lead3 = lead2 + 1;
                        if (lead3 >= num22.mant.length) {
                            thisafter = 0;
                        } else {
                            thisafter = num22.mant.length - lead3;
                        }
                        lead = thisafter;
                    }
                    if (lead == i2) {
                        int i7 = exformint2;
                        int i8 = mag;
                        break;
                    } else if (lead < i2) {
                        byte[] newmant = extend(num22.mant, (num22.mant.length + i2) - lead);
                        num22.mant = newmant;
                        num22.exp -= i2 - lead;
                        int i9 = exformint2;
                        if (num22.exp >= -999999999) {
                            int i10 = mag;
                            byte[] bArr = newmant;
                        } else {
                            StringBuilder sb = new StringBuilder();
                            int i11 = mag;
                            sb.append("Exponent Overflow: ");
                            sb.append(num22.exp);
                            throw new ArithmeticException(sb.toString());
                        }
                    } else {
                        int exformint3 = exformint2;
                        int i12 = mag;
                        int chop = lead - i2;
                        if (chop > num22.mant.length) {
                            num22.mant = ZERO.mant;
                            num22.ind = 0;
                            num22.exp = 0;
                        } else {
                            int oldexp = num22.exp;
                            num22.round(num22.mant.length - chop, exround2);
                            if (num22.exp - oldexp == chop) {
                                break;
                            }
                        }
                        int thisafter2 = lead;
                        exformint2 = exformint3;
                        mag = i12;
                        int i13 = exdigits;
                    }
                }
            } else {
                int i14 = mag;
            }
            char[] a22 = num22.layout();
            if (i > 0) {
                int $11 = a22.length;
                int p = 0;
                while ($11 > 0 && a22[p] != '.' && a22[p] != 'E') {
                    $11--;
                    p++;
                }
                if (p > i) {
                    badarg("format", 1, String.valueOf(before));
                }
                if (p < i) {
                    char[] newa = new char[((a22.length + i) - p)];
                    int $12 = i - p;
                    int i15 = 0;
                    while ($12 > 0) {
                        newa[i15] = ' ';
                        $12--;
                        i15++;
                    }
                    System.arraycopy(a22, 0, newa, i15, a22.length);
                    a22 = newa;
                    int i16 = i15;
                }
            }
            if (i3 > 0) {
                int $13 = a22.length - 1;
                int p2 = a22.length - 1;
                while ($13 > 0 && a22[p2] != 'E') {
                    $13--;
                    p2--;
                }
                if (p2 == 0) {
                    char[] newa2 = new char[(a22.length + i3 + 2)];
                    System.arraycopy(a22, 0, newa2, 0, a22.length);
                    int $14 = i3 + 2;
                    int i17 = a22.length;
                    while ($14 > 0) {
                        newa2[i17] = ' ';
                        $14--;
                        i17++;
                    }
                    a22 = newa2;
                } else {
                    int places = (a22.length - p2) - 2;
                    if (places > i3) {
                        badarg("format", 3, String.valueOf(explaces));
                    }
                    if (places < i3) {
                        char[] newa3 = new char[((a22.length + i3) - places)];
                        System.arraycopy(a22, 0, newa3, 0, p2 + 2);
                        int $15 = i3 - places;
                        int i18 = p2 + 2;
                        while ($15 > 0) {
                            newa3[i18] = '0';
                            $15--;
                            i18++;
                        }
                        System.arraycopy(a22, p2 + 2, newa3, i18, places);
                        a22 = newa3;
                        int i19 = i18;
                    }
                    return new String(a22);
                }
            }
            return new String(a22);
        }
        mag = 0;
        if (i2 < 0) {
        }
        char[] a222 = num22.layout();
        if (i > 0) {
        }
        if (i3 > 0) {
        }
        return new String(a222);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public int intValue() {
        return toBigInteger().intValue();
    }

    public int intValueExact() {
        int useexp;
        if (this.ind == 0) {
            return 0;
        }
        int lodigit = this.mant.length - 1;
        if (this.exp < 0) {
            lodigit += this.exp;
            if (!allzero(this.mant, lodigit + 1)) {
                throw new ArithmeticException("Decimal part non-zero: " + toString());
            } else if (lodigit < 0) {
                return 0;
            } else {
                useexp = 0;
            }
        } else if (this.exp + lodigit <= 9) {
            useexp = this.exp;
        } else {
            throw new ArithmeticException("Conversion overflow: " + toString());
        }
        int result = 0;
        int $16 = lodigit + useexp;
        for (int i = 0; i <= $16; i++) {
            result *= 10;
            if (i <= lodigit) {
                result += this.mant[i];
            }
        }
        if (lodigit + useexp != 9 || result / 1000000000 == this.mant[0]) {
            if (this.ind == 1) {
                return result;
            }
            return -result;
        } else if (result == Integer.MIN_VALUE && this.ind == -1 && this.mant[0] == 2) {
            return result;
        } else {
            throw new ArithmeticException("Conversion overflow: " + toString());
        }
    }

    public long longValue() {
        return toBigInteger().longValue();
    }

    public long longValueExact() {
        int useexp;
        int cstart;
        if (this.ind == 0) {
            return 0;
        }
        int lodigit = this.mant.length - 1;
        if (this.exp < 0) {
            lodigit += this.exp;
            if (lodigit < 0) {
                cstart = 0;
            } else {
                cstart = lodigit + 1;
            }
            if (!allzero(this.mant, cstart)) {
                throw new ArithmeticException("Decimal part non-zero: " + toString());
            } else if (lodigit < 0) {
                return 0;
            } else {
                useexp = 0;
            }
        } else if (this.exp + this.mant.length <= 18) {
            useexp = this.exp;
        } else {
            throw new ArithmeticException("Conversion overflow: " + toString());
        }
        long result = 0;
        int $17 = lodigit + useexp;
        for (int i = 0; i <= $17; i++) {
            result *= 10;
            if (i <= lodigit) {
                result += (long) this.mant[i];
            }
        }
        if (lodigit + useexp != 18 || result / 1000000000000000000L == ((long) this.mant[0])) {
            if (this.ind == 1) {
                return result;
            }
            return -result;
        } else if (result == Long.MIN_VALUE && this.ind == -1 && this.mant[0] == 9) {
            return result;
        } else {
            throw new ArithmeticException("Conversion overflow: " + toString());
        }
    }

    public BigDecimal movePointLeft(int n) {
        BigDecimal res = clone(this);
        res.exp -= n;
        return res.finish(plainMC, false);
    }

    public BigDecimal movePointRight(int n) {
        BigDecimal res = clone(this);
        res.exp += n;
        return res.finish(plainMC, false);
    }

    public int scale() {
        if (this.exp >= 0) {
            return 0;
        }
        return -this.exp;
    }

    public BigDecimal setScale(int scale) {
        return setScale(scale, 7);
    }

    public BigDecimal setScale(int scale, int round) {
        int padding;
        int ourscale = scale();
        if (ourscale == scale && this.form == 0) {
            return this;
        }
        BigDecimal res = clone(this);
        if (ourscale <= scale) {
            if (ourscale == 0) {
                padding = res.exp + scale;
            } else {
                padding = scale - ourscale;
            }
            res.mant = extend(res.mant, res.mant.length + padding);
            res.exp = -scale;
        } else if (scale >= 0) {
            res = res.round(res.mant.length - (ourscale - scale), round);
            if (res.exp != (-scale)) {
                res.mant = extend(res.mant, res.mant.length + 1);
                res.exp--;
            }
        } else {
            throw new ArithmeticException("Negative scale: " + scale);
        }
        res.form = 0;
        return res;
    }

    public short shortValueExact() {
        int num = intValueExact();
        boolean z = false;
        boolean z2 = num > 32767;
        if (num < -32768) {
            z = true;
        }
        if (!z && !z2) {
            return (short) num;
        }
        throw new ArithmeticException("Conversion overflow: " + toString());
    }

    public int signum() {
        return this.ind;
    }

    public java.math.BigDecimal toBigDecimal() {
        return new java.math.BigDecimal(unscaledValue(), scale());
    }

    public BigInteger toBigInteger() {
        BigDecimal res;
        boolean z = true;
        boolean z2 = this.exp >= 0;
        if (this.form != 0) {
            z = false;
        }
        if (z2 && z) {
            res = this;
        } else if (this.exp >= 0) {
            res = clone(this);
            res.form = 0;
        } else if ((-this.exp) >= this.mant.length) {
            res = ZERO;
        } else {
            res = clone(this);
            int newlen = res.mant.length + res.exp;
            byte[] newmant = new byte[newlen];
            System.arraycopy(res.mant, 0, newmant, 0, newlen);
            res.mant = newmant;
            res.form = 0;
            res.exp = 0;
        }
        return new BigInteger(new String(res.layout()));
    }

    public BigInteger toBigIntegerExact() {
        if (this.exp >= 0 || allzero(this.mant, this.mant.length + this.exp)) {
            return toBigInteger();
        }
        throw new ArithmeticException("Decimal part non-zero: " + toString());
    }

    public char[] toCharArray() {
        return layout();
    }

    public String toString() {
        return new String(layout());
    }

    public BigInteger unscaledValue() {
        BigDecimal res;
        if (this.exp >= 0) {
            res = this;
        } else {
            res = clone(this);
            res.exp = 0;
        }
        return res.toBigInteger();
    }

    public static BigDecimal valueOf(double dub) {
        return new BigDecimal(new Double(dub).toString());
    }

    public static BigDecimal valueOf(long lint) {
        return valueOf(lint, 0);
    }

    public static BigDecimal valueOf(long lint, int scale) {
        BigDecimal res;
        if (lint == 0) {
            res = ZERO;
        } else if (lint == 1) {
            res = ONE;
        } else if (lint == 10) {
            res = TEN;
        } else {
            res = new BigDecimal(lint);
        }
        if (scale == 0) {
            return res;
        }
        if (scale >= 0) {
            BigDecimal res2 = clone(res);
            res2.exp = -scale;
            return res2;
        }
        throw new NumberFormatException("Negative scale: " + scale);
    }

    private char[] layout() {
        char csign;
        char[] cmant = new char[this.mant.length];
        int $18 = this.mant.length;
        int i = 0;
        while ($18 > 0) {
            cmant[i] = (char) (this.mant[i] + 48);
            $18--;
            i++;
        }
        if (this.form != 0) {
            StringBuilder sb = new StringBuilder(cmant.length + 15);
            if (this.ind == -1) {
                sb.append('-');
            }
            int euse = (this.exp + cmant.length) - 1;
            if (this.form == 1) {
                sb.append(cmant[0]);
                if (cmant.length > 1) {
                    sb.append('.');
                    sb.append(cmant, 1, cmant.length - 1);
                }
            } else {
                int sig = euse % 3;
                if (sig < 0) {
                    sig += 3;
                }
                euse -= sig;
                int sig2 = sig + 1;
                if (sig2 >= cmant.length) {
                    sb.append(cmant, 0, cmant.length);
                    for (int $19 = sig2 - cmant.length; $19 > 0; $19--) {
                        sb.append('0');
                    }
                } else {
                    sb.append(cmant, 0, sig2);
                    sb.append('.');
                    sb.append(cmant, sig2, cmant.length - sig2);
                }
            }
            if (euse != 0) {
                if (euse < 0) {
                    csign = '-';
                    euse = -euse;
                } else {
                    csign = '+';
                }
                char csign2 = csign;
                sb.append('E');
                sb.append(csign2);
                sb.append(euse);
            }
            char[] rec = new char[sb.length()];
            int srcEnd = sb.length();
            if (srcEnd != 0) {
                sb.getChars(0, srcEnd, rec, 0);
            }
            return rec;
        } else if (this.exp != 0) {
            int needsign = this.ind == -1 ? 1 : 0;
            int mag = this.exp + cmant.length;
            if (mag < 1) {
                char[] rec2 = new char[((needsign + 2) - this.exp)];
                if (needsign != 0) {
                    rec2[0] = '-';
                }
                rec2[needsign] = '0';
                rec2[needsign + 1] = '.';
                int $20 = -mag;
                int i2 = needsign + 2;
                while ($20 > 0) {
                    rec2[i2] = '0';
                    $20--;
                    i2++;
                }
                System.arraycopy(cmant, 0, rec2, (needsign + 2) - mag, cmant.length);
                return rec2;
            } else if (mag > cmant.length) {
                char[] rec3 = new char[(needsign + mag)];
                if (needsign != 0) {
                    rec3[0] = '-';
                }
                System.arraycopy(cmant, 0, rec3, needsign, cmant.length);
                int $21 = mag - cmant.length;
                int i3 = cmant.length + needsign;
                while ($21 > 0) {
                    rec3[i3] = '0';
                    $21--;
                    i3++;
                }
                return rec3;
            } else {
                char[] rec4 = new char[(needsign + 1 + cmant.length)];
                if (needsign != 0) {
                    rec4[0] = '-';
                }
                System.arraycopy(cmant, 0, rec4, needsign, mag);
                rec4[needsign + mag] = '.';
                System.arraycopy(cmant, mag, rec4, needsign + mag + 1, cmant.length - mag);
                return rec4;
            }
        } else if (this.ind >= 0) {
            return cmant;
        } else {
            char[] rec5 = new char[(cmant.length + 1)];
            rec5[0] = '-';
            System.arraycopy(cmant, 0, rec5, 1, cmant.length);
            return rec5;
        }
    }

    private int intcheck(int min, int max) {
        int i = intValueExact();
        boolean z = false;
        boolean z2 = i < min;
        if (i > max) {
            z = true;
        }
        if (!z && !z2) {
            return i;
        }
        throw new ArithmeticException("Conversion overflow: " + i);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r21v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v36, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r21v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r21v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v41, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v42, resolved type: byte} */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0147, code lost:
        if (r9 == 0) goto L_0x014b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0149, code lost:
        r7 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x014b, code lost:
        r7 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x014c, code lost:
        if (r13 == 0) goto L_0x0150;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x014e, code lost:
        r14 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0150, code lost:
        r14 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0152, code lost:
        if ((r7 | r14) == false) goto L_0x0168;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0154, code lost:
        r4.mant[r9] = (byte) r13;
        r9 = r9 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x015d, code lost:
        if (r9 != (r1 + 1)) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0164, code lost:
        if (r12[0] != 0) goto L_0x0168;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0168, code lost:
        if (r3 < 0) goto L_0x0170;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x016d, code lost:
        if ((-r4.exp) <= r3) goto L_0x0170;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0172, code lost:
        if (r0 == 'D') goto L_0x0179;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0176, code lost:
        if (r4.exp > 0) goto L_0x0179;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private BigDecimal dodivide(char code, BigDecimal rhs, MathContext set, int scale) {
        BigDecimal bigDecimal;
        int scale2;
        BigDecimal rhs2;
        int reqdig;
        Object obj;
        int d;
        byte[] var1;
        byte[] var2;
        int actdig;
        Object obj2;
        boolean z;
        boolean z2;
        BigDecimal lhs;
        int ba;
        byte v2;
        char c = code;
        BigDecimal bigDecimal2 = rhs;
        MathContext mathContext = set;
        int scale3 = scale;
        int var1len = 0;
        Object obj3 = null;
        int thisdigit = 0;
        int actdig2 = 0;
        Object obj4 = null;
        if (mathContext.lostDigits != 0) {
            bigDecimal = this;
            bigDecimal.checkdigits(bigDecimal2, mathContext.digits);
        } else {
            bigDecimal = this;
        }
        BigDecimal lhs2 = bigDecimal;
        if (bigDecimal2.ind == 0) {
            BigDecimal bigDecimal3 = lhs2;
            throw new ArithmeticException("Divide by 0");
        } else if (lhs2.ind != 0) {
            int reqdig2 = mathContext.digits;
            if (reqdig2 > 0) {
                if (lhs2.mant.length > reqdig2) {
                    lhs2 = clone(lhs2).round(mathContext);
                }
                if (bigDecimal2.mant.length > reqdig2) {
                    bigDecimal2 = clone(rhs).round(mathContext);
                }
                rhs2 = bigDecimal2;
                reqdig = reqdig2;
            } else {
                if (scale3 == -1) {
                    scale3 = lhs2.scale();
                }
                int reqdig3 = lhs2.mant.length;
                if (scale3 != (-lhs2.exp)) {
                    scale2 = scale3;
                    reqdig3 = reqdig3 + scale3 + lhs2.exp;
                } else {
                    scale2 = scale3;
                }
                int reqdig4 = (reqdig3 - (bigDecimal2.mant.length - 1)) - bigDecimal2.exp;
                if (reqdig4 < lhs2.mant.length) {
                    reqdig4 = lhs2.mant.length;
                }
                int reqdig5 = reqdig4;
                if (reqdig5 < bigDecimal2.mant.length) {
                    reqdig5 = bigDecimal2.mant.length;
                }
                rhs2 = bigDecimal2;
                reqdig = reqdig5;
                scale3 = scale2;
            }
            BigDecimal lhs3 = lhs2;
            int newexp = ((lhs3.exp - rhs2.exp) + lhs3.mant.length) - rhs2.mant.length;
            if (newexp >= 0 || c == 'D') {
                BigDecimal res = new BigDecimal();
                res.ind = (byte) (lhs3.ind * rhs2.ind);
                res.exp = newexp;
                res.mant = new byte[(reqdig + 1)];
                int newlen = reqdig + reqdig + 1;
                byte[] var12 = extend(lhs3.mant, newlen);
                int var1len2 = newlen;
                int i = newexp;
                byte[] var22 = rhs2.mant;
                int var2len = newlen;
                int i2 = newlen;
                int b2b = (var22[0] * 10) + 1;
                byte[] var13 = var12;
                if (var22.length > 1) {
                    b2b += var22[1];
                }
                int b2b2 = b2b;
                int var2len2 = var2len;
                int have = 0;
                loop0:
                while (true) {
                    int d2 = var1len;
                    obj = obj3;
                    int lasthave = thisdigit;
                    int thisdigit2 = 0;
                    d = var1len2;
                    var1 = var13;
                    while (true) {
                        if (d < var2len2) {
                            var2 = var22;
                            actdig = actdig2;
                            obj2 = obj4;
                            break;
                        }
                        if (d == var2len2) {
                            int $22 = d;
                            actdig = actdig2;
                            int i3 = 0;
                            while ($22 > 0) {
                                obj2 = obj4;
                                if (i3 < var22.length) {
                                    v2 = var22[i3];
                                } else {
                                    v2 = 0;
                                }
                                var2 = var22;
                                if (var1[i3] < v2) {
                                    int i4 = i3;
                                    byte b = v2;
                                    break;
                                } else if (var1[i3] > v2) {
                                    lhs = lhs3;
                                    int i5 = i3;
                                    byte b2 = v2;
                                    ba = var1[0];
                                } else {
                                    $22--;
                                    i3++;
                                    byte b3 = v2;
                                    obj4 = obj2;
                                    var22 = var2;
                                }
                            }
                            Object obj5 = obj4;
                            res.mant[have] = (byte) (thisdigit2 + 1);
                            have++;
                            var1[0] = 0;
                            int i6 = i3;
                            break loop0;
                        }
                        lhs = lhs3;
                        var2 = var22;
                        actdig = actdig2;
                        obj2 = obj4;
                        int ba2 = var1[0] * 10;
                        int ba3 = ba2;
                        if (d > 1) {
                            ba3 = ba2 + var1[1];
                        }
                        ba = ba3;
                        int mult = (ba * 10) / b2b2;
                        if (mult == 0) {
                            mult = 1;
                        }
                        thisdigit2 += mult;
                        var1 = byteaddsub(var1, d, var2, var2len2, -mult, true);
                        if (var1[0] != 0) {
                            int i7 = mult;
                        } else {
                            int $23 = d - 2;
                            int start = 0;
                            while (start <= $23 && var1[start] == 0) {
                                d--;
                                start++;
                            }
                            if (start != 0) {
                                System.arraycopy(var1, start, var1, 0, d);
                            }
                            int i8 = mult;
                            int i9 = start;
                        }
                        actdig2 = actdig;
                        obj4 = obj2;
                        var22 = var2;
                        lhs3 = lhs;
                        c = code;
                    }
                    res.exp--;
                    var2len2--;
                    var1len2 = d;
                    var13 = var1;
                    int i10 = thisdigit2;
                    var1len = d2;
                    obj3 = obj;
                    thisdigit = lasthave;
                    actdig2 = actdig;
                    obj4 = obj2;
                    var22 = var2;
                }
                if (have == 0) {
                    have = 1;
                }
                if (!(c == 'I') && !(c == 'R')) {
                    if (var1[0] != 0) {
                        byte lasthave2 = res.mant[have - 1];
                        if (lasthave2 % 5 == 0) {
                            res.mant[have - 1] = (byte) (lasthave2 + 1);
                        }
                        byte b4 = lasthave2;
                    }
                } else if (res.exp + have > reqdig) {
                    throw new ArithmeticException("Integer overflow");
                } else if (c != 'R') {
                } else if (res.mant[0] == 0) {
                    return clone(lhs3).finish(mathContext, false);
                } else {
                    if (var1[0] == 0) {
                        return ZERO;
                    }
                    res.ind = lhs3.ind;
                    int padding = ((reqdig + reqdig) + 1) - lhs3.mant.length;
                    res.exp = (res.exp - padding) + lhs3.exp;
                    int d3 = d;
                    int i11 = d3 - 1;
                    while (true) {
                        if (i11 < 1) {
                            int i12 = padding;
                            break;
                        }
                        int padding2 = padding;
                        BigDecimal lhs4 = lhs3;
                        if ((!(res.exp < lhs3.exp) || !(res.exp < rhs2.exp)) || var1[i11] != 0) {
                            break;
                        }
                        d3--;
                        res.exp++;
                        i11--;
                        padding = padding2;
                        lhs3 = lhs4;
                        char c2 = code;
                    }
                    if (d3 < var1.length) {
                        byte[] newvar1 = new byte[d3];
                        z2 = false;
                        System.arraycopy(var1, 0, newvar1, 0, d3);
                        var1 = newvar1;
                    } else {
                        z2 = false;
                        Object obj6 = obj;
                    }
                    res.mant = var1;
                    return res.finish(mathContext, z2);
                }
                if (scale3 >= 0) {
                    if (have != res.mant.length) {
                        res.exp -= res.mant.length - have;
                    }
                    res.round(res.mant.length - ((-res.exp) - scale3), mathContext.roundingMode);
                    if (res.exp != (-scale3)) {
                        z = true;
                        res.mant = extend(res.mant, res.mant.length + 1);
                        res.exp--;
                    } else {
                        z = true;
                    }
                    return res.finish(mathContext, z);
                }
                if (have == res.mant.length) {
                    res.round(mathContext);
                    int have2 = reqdig;
                } else if (res.mant[0] == 0) {
                    return ZERO;
                } else {
                    byte[] newmant = new byte[have];
                    System.arraycopy(res.mant, 0, newmant, 0, have);
                    res.mant = newmant;
                    byte[] bArr = newmant;
                }
                return res.finish(mathContext, true);
            } else if (c == 'I') {
                return ZERO;
            } else {
                return clone(lhs3).finish(mathContext, false);
            }
        } else if (mathContext.form != 0) {
            return ZERO;
        } else {
            if (scale3 == -1) {
                return lhs2;
            }
            return lhs2.setScale(scale3);
        }
    }

    private void bad(char[] s) {
        throw new NumberFormatException("Not a number: " + String.valueOf(s));
    }

    private void badarg(String name, int pos, String value) {
        throw new IllegalArgumentException("Bad argument " + pos + " to " + name + PluralRules.KEYWORD_RULE_SEPARATOR + value);
    }

    private static final byte[] extend(byte[] inarr, int newlen) {
        if (inarr.length == newlen) {
            return inarr;
        }
        byte[] newarr = new byte[newlen];
        System.arraycopy(inarr, 0, newarr, 0, inarr.length);
        return newarr;
    }

    private static final byte[] byteaddsub(byte[] a, int avlen, byte[] b, int bvlen, int m, boolean reuse) {
        byte[] newarr;
        int digit;
        byte[] bArr = a;
        byte[] bArr2 = b;
        int i = m;
        int dp90 = 0;
        int alength = bArr.length;
        int blength = bArr2.length;
        int ap = avlen - 1;
        int bp = bvlen - 1;
        int maxarr = bp;
        if (maxarr < ap) {
            maxarr = ap;
        }
        byte[] reb = null;
        if (reuse && maxarr + 1 == alength) {
            reb = bArr;
        }
        if (reb == null) {
            reb = new byte[(maxarr + 1)];
        }
        boolean quickm = false;
        if (i == 1) {
            quickm = true;
        } else if (i == -1) {
            quickm = true;
        }
        int digit2 = 0;
        int op = maxarr;
        while (true) {
            int dp902 = dp90;
            if (op < 0) {
                break;
            }
            if (ap >= 0) {
                if (ap < alength) {
                    digit2 += bArr[ap];
                }
                ap--;
            }
            if (bp >= 0) {
                if (bp < blength) {
                    if (!quickm) {
                        digit2 += bArr2[bp] * i;
                    } else if (i > 0) {
                        digit2 += bArr2[bp];
                    } else {
                        digit2 -= bArr2[bp];
                    }
                }
                bp--;
            }
            if (digit2 >= 10 || digit2 < 0) {
                dp90 = digit2 + 90;
                reb[op] = bytedig[dp90];
                digit = bytecar[dp90];
            } else {
                reb[op] = (byte) digit2;
                digit = 0;
                dp90 = dp902;
            }
            op--;
            digit2 = digit;
        }
        if (digit2 == 0) {
            return reb;
        }
        byte[] newarr2 = null;
        if (reuse && maxarr + 2 == bArr.length) {
            newarr2 = bArr;
        }
        if (newarr2 == null) {
            newarr = new byte[(maxarr + 2)];
        } else {
            newarr = newarr2;
        }
        newarr[0] = (byte) digit2;
        if (maxarr < 10) {
            int $24 = maxarr + 1;
            int i2 = 0;
            while ($24 > 0) {
                newarr[i2 + 1] = reb[i2];
                $24--;
                i2++;
            }
        } else {
            System.arraycopy(reb, 0, newarr, 1, maxarr + 1);
        }
        return newarr;
    }

    private static final byte[] diginit() {
        byte[] work = new byte[190];
        for (int op = 0; op <= 189; op++) {
            int digit = op - 90;
            if (digit >= 0) {
                work[op] = (byte) (digit % 10);
                bytecar[op] = (byte) (digit / 10);
            } else {
                work[op] = (byte) ((digit + 100) % 10);
                bytecar[op] = (byte) (((digit + 100) / 10) - 10);
            }
        }
        return work;
    }

    private static final BigDecimal clone(BigDecimal dec) {
        BigDecimal copy = new BigDecimal();
        copy.ind = dec.ind;
        copy.exp = dec.exp;
        copy.form = dec.form;
        copy.mant = dec.mant;
        return copy;
    }

    private void checkdigits(BigDecimal rhs, int dig) {
        if (dig != 0) {
            if (this.mant.length > dig && !allzero(this.mant, dig)) {
                throw new ArithmeticException("Too many digits: " + toString());
            } else if (rhs != null && rhs.mant.length > dig && !allzero(rhs.mant, dig)) {
                throw new ArithmeticException("Too many digits: " + rhs.toString());
            }
        }
    }

    private BigDecimal round(MathContext set) {
        return round(set.digits, set.roundingMode);
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x00fb A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00fc  */
    private BigDecimal round(int len, int mode) {
        byte first;
        boolean reuse;
        int i = len;
        int i2 = mode;
        int adjust = this.mant.length - i;
        if (adjust <= 0) {
            return this;
        }
        this.exp += adjust;
        int sign = this.ind;
        byte[] oldmant = this.mant;
        if (i > 0) {
            this.mant = new byte[i];
            System.arraycopy(oldmant, 0, this.mant, 0, i);
            reuse = true;
            first = oldmant[i];
        } else {
            this.mant = ZERO.mant;
            this.ind = 0;
            reuse = false;
            if (i == 0) {
                first = oldmant[0];
            } else {
                first = 0;
            }
        }
        int increment = 0;
        if (i2 == 4) {
            if (first >= 5) {
                increment = sign;
            }
        } else if (i2 == 7) {
            if (!allzero(oldmant, i)) {
                throw new ArithmeticException("Rounding necessary");
            }
        } else if (i2 == 5) {
            if (first > 5) {
                increment = sign;
            } else if (first == 5 && !allzero(oldmant, i + 1)) {
                increment = sign;
            }
        } else if (i2 == 6) {
            if (first > 5) {
                increment = sign;
            } else if (first == 5) {
                if (!allzero(oldmant, i + 1)) {
                    increment = sign;
                } else if (this.mant[this.mant.length - 1] % 2 != 0) {
                    increment = sign;
                }
            }
        } else if (i2 != 1) {
            if (i2 == 0) {
                if (!allzero(oldmant, i)) {
                    increment = sign;
                }
            } else if (i2 == 2) {
                if (sign > 0 && !allzero(oldmant, i)) {
                    increment = sign;
                }
            } else if (i2 != 3) {
                throw new IllegalArgumentException("Bad round value: " + i2);
            } else if (sign < 0 && !allzero(oldmant, i)) {
                increment = sign;
            }
        }
        if (increment != 0) {
            if (this.ind == 0) {
                this.mant = ONE.mant;
                this.ind = (byte) increment;
            } else {
                if (this.ind == -1) {
                    increment = -increment;
                }
                byte[] newmant = byteaddsub(this.mant, this.mant.length, ONE.mant, 1, increment, reuse);
                if (newmant.length > this.mant.length) {
                    this.exp++;
                    System.arraycopy(newmant, 0, this.mant, 0, this.mant.length);
                } else {
                    this.mant = newmant;
                }
                if (this.exp > 999999999) {
                    return this;
                }
                throw new ArithmeticException("Exponent Overflow: " + this.exp);
            }
        }
        if (this.exp > 999999999) {
        }
    }

    private static final boolean allzero(byte[] array, int start) {
        if (start < 0) {
            start = 0;
        }
        int $25 = array.length - 1;
        for (int i = start; i <= $25; i++) {
            if (array[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00a7, code lost:
        if (r9 <= 999999999) goto L_0x00c1;
     */
    private BigDecimal finish(MathContext set, boolean strip) {
        if (set.digits != 0 && this.mant.length > set.digits) {
            round(set);
        }
        boolean z = true;
        if (strip && set.form != 0) {
            int d = this.mant.length;
            int i = d - 1;
            while (i >= 1 && this.mant[i] == 0) {
                d--;
                this.exp++;
                i--;
            }
            if (d < this.mant.length) {
                byte[] newmant = new byte[d];
                System.arraycopy(this.mant, 0, newmant, 0, d);
                this.mant = newmant;
            }
        }
        this.form = 0;
        int $26 = this.mant.length;
        int i2 = 0;
        while ($26 > 0) {
            if (this.mant[i2] != 0) {
                if (i2 > 0) {
                    byte[] newmant2 = new byte[(this.mant.length - i2)];
                    System.arraycopy(this.mant, i2, newmant2, 0, this.mant.length - i2);
                    this.mant = newmant2;
                }
                int mag = this.exp + this.mant.length;
                if (mag > 0) {
                    if (mag > set.digits && set.digits != 0) {
                        this.form = (byte) set.form;
                    }
                    if (mag - 1 <= 999999999) {
                        return this;
                    }
                } else if (mag < -5) {
                    this.form = (byte) set.form;
                }
                int mag2 = mag - 1;
                boolean z2 = mag2 < -999999999;
                if (mag2 <= 999999999) {
                    z = false;
                }
                if (z || z2) {
                    if (this.form == 2) {
                        int sig = mag2 % 3;
                        if (sig < 0) {
                            sig = 3 + sig;
                        }
                        mag2 -= sig;
                        if (mag2 >= -999999999) {
                        }
                    }
                    throw new ArithmeticException("Exponent Overflow: " + mag2);
                }
                return this;
            }
            $26--;
            i2++;
        }
        this.ind = 0;
        if (set.form != 0) {
            this.exp = 0;
        } else if (this.exp > 0) {
            this.exp = 0;
        } else if (this.exp < -999999999) {
            throw new ArithmeticException("Exponent Overflow: " + this.exp);
        }
        this.mant = ZERO.mant;
        return this;
    }
}
