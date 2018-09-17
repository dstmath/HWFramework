package android.icu.math;

import android.icu.lang.UCharacter;
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
    private static final byte isneg = (byte) -1;
    private static final byte ispos = (byte) 1;
    private static final byte iszero = (byte) 0;
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
        if (scale < 0) {
            throw new NumberFormatException("Negative scale: " + scale);
        }
        this.exp = -scale;
    }

    public BigDecimal(char[] inchars) {
        this(inchars, 0, inchars.length);
    }

    /* JADX WARNING: Removed duplicated region for block: B:76:0x0156  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01f9  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01a7  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x0225  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public BigDecimal(char[] inchars, int offset, int length) {
        int j;
        int $3;
        this.form = (byte) 0;
        if (length <= 0) {
            bad(inchars);
        }
        this.ind = (byte) 1;
        if (inchars[offset] == '-') {
            length--;
            if (length == 0) {
                bad(inchars);
            }
            this.ind = (byte) -1;
            offset++;
        } else if (inchars[offset] == '+') {
            length--;
            if (length == 0) {
                bad(inchars);
            }
            offset++;
        }
        boolean exotic = false;
        boolean hadexp = false;
        int d = 0;
        int dotoff = -1;
        int last = -1;
        int $1 = length;
        int i = offset;
        while ($1 > 0) {
            char si = inchars[i];
            if (si >= '0' && si <= '9') {
                last = i;
                d++;
            } else if (si == '.') {
                if (dotoff >= 0) {
                    bad(inchars);
                }
                dotoff = i - offset;
            } else if (si == 'e' || si == 'E') {
                int k;
                int i2;
                int i3;
                char sj;
                int dvalue;
                if (i - offset > length - 2) {
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
                int elen = length - (k - offset);
                if (elen == 0) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                if (elen > 9) {
                    i3 = 1;
                } else {
                    i3 = 0;
                }
                if ((i3 | i2) != 0) {
                    bad(inchars);
                }
                int $2 = elen;
                j = k;
                while ($2 > 0) {
                    sj = inchars[j];
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
                        dvalue = sj - 48;
                    }
                    this.exp = (this.exp * 10) + dvalue;
                    $2--;
                    j++;
                }
                if (eneg) {
                    this.exp = -this.exp;
                }
                hadexp = true;
                if (d == 0) {
                    bad(inchars);
                }
                if (dotoff >= 0) {
                    this.exp = (this.exp + dotoff) - d;
                }
                $3 = last - 1;
                for (i = offset; i <= $3; i++) {
                    si = inchars[i];
                    if (si != '0') {
                        if (si != '.') {
                            if (si <= '9' || UCharacter.digit(si, 10) != 0) {
                                break;
                            }
                            offset++;
                            dotoff--;
                            d--;
                        } else {
                            offset++;
                            dotoff--;
                        }
                    } else {
                        offset++;
                        dotoff--;
                        d--;
                    }
                }
                this.mant = new byte[d];
                j = offset;
                if (exotic) {
                    int $5 = d;
                    i = 0;
                    while ($5 > 0) {
                        if (i == dotoff) {
                            j++;
                        }
                        this.mant[i] = (byte) (inchars[j] - 48);
                        j++;
                        $5--;
                        i++;
                    }
                } else {
                    int $4 = d;
                    i = 0;
                    while ($4 > 0) {
                        if (i == dotoff) {
                            j++;
                        }
                        sj = inchars[j];
                        if (sj <= '9') {
                            this.mant[i] = (byte) (sj - 48);
                        } else {
                            dvalue = UCharacter.digit(sj, 10);
                            if (dvalue < 0) {
                                bad(inchars);
                            }
                            this.mant[i] = (byte) dvalue;
                        }
                        j++;
                        $4--;
                        i++;
                    }
                }
                if (this.mant[0] != (byte) 0) {
                    this.ind = (byte) 0;
                    if (this.exp > 0) {
                        this.exp = 0;
                    }
                    if (hadexp) {
                        this.mant = ZERO.mant;
                        this.exp = 0;
                        return;
                    }
                    return;
                } else if (hadexp) {
                    this.form = (byte) 1;
                    int mag = (this.exp + this.mant.length) - 1;
                    if (((mag > 999999999 ? 1 : 0) | (mag < -999999999 ? 1 : 0)) != 0) {
                        bad(inchars);
                        return;
                    }
                    return;
                } else {
                    return;
                }
            } else {
                if (!UCharacter.isDigit(si)) {
                    bad(inchars);
                }
                exotic = true;
                last = i;
                d++;
            }
            $1--;
            i++;
        }
        if (d == 0) {
        }
        if (dotoff >= 0) {
        }
        $3 = last - 1;
        while (i <= $3) {
        }
        this.mant = new byte[d];
        j = offset;
        if (exotic) {
        }
        if (this.mant[0] != (byte) 0) {
        }
    }

    public BigDecimal(double num) {
        this(new java.math.BigDecimal(num).toString());
    }

    public BigDecimal(int num) {
        this.form = (byte) 0;
        if (num > 9 || num < -9) {
            if (num > 0) {
                this.ind = (byte) 1;
                num = -num;
            } else {
                this.ind = (byte) -1;
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
            i = (10 - i) - 1;
            while (true) {
                this.mant[i] = (byte) (-((byte) (num % 10)));
                num /= 10;
                if (num != 0) {
                    i--;
                } else {
                    return;
                }
            }
        }
        if (num == 0) {
            this.mant = ZERO.mant;
            this.ind = (byte) 0;
        } else if (num == 1) {
            this.mant = ONE.mant;
            this.ind = (byte) 1;
        } else if (num == -1) {
            this.mant = ONE.mant;
            this.ind = (byte) -1;
        } else {
            this.mant = new byte[1];
            if (num > 0) {
                this.mant[0] = (byte) num;
                this.ind = (byte) 1;
            } else {
                this.mant[0] = (byte) (-num);
                this.ind = (byte) -1;
            }
        }
    }

    public BigDecimal(long num) {
        this.form = (byte) 0;
        if (num > 0) {
            this.ind = (byte) 1;
            num = -num;
        } else if (num == 0) {
            this.ind = (byte) 0;
        } else {
            this.ind = (byte) -1;
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
        i = (19 - i) - 1;
        while (true) {
            this.mant[i] = (byte) (-((byte) ((int) (num % 10))));
            num /= 10;
            if (num != 0) {
                i--;
            } else {
                return;
            }
        }
    }

    public BigDecimal(String string) {
        this(string.toCharArray(), 0, string.length());
    }

    private BigDecimal() {
        this.form = (byte) 0;
    }

    public BigDecimal abs() {
        return abs(plainMC);
    }

    public BigDecimal abs(MathContext set) {
        if (this.ind == (byte) -1) {
            return negate(set);
        }
        return plus(set);
    }

    public BigDecimal add(BigDecimal rhs) {
        return add(rhs, plainMC);
    }

    public BigDecimal add(BigDecimal rhs, MathContext set) {
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        BigDecimal lhs = this;
        if (this.ind == (byte) 0 && set.form != 0) {
            return rhs.plus(set);
        }
        if (rhs.ind == (byte) 0 && set.form != 0) {
            return plus(set);
        }
        int tlen;
        int mult;
        int reqdig = set.digits;
        if (reqdig > 0) {
            if (this.mant.length > reqdig) {
                lhs = clone(this).round(set);
            }
            if (rhs.mant.length > reqdig) {
                rhs = clone(rhs).round(set);
            }
        }
        BigDecimal res = new BigDecimal();
        byte[] usel = lhs.mant;
        int usellen = lhs.mant.length;
        byte[] user = rhs.mant;
        int userlen = rhs.mant.length;
        int newlen;
        if (lhs.exp == rhs.exp) {
            res.exp = lhs.exp;
        } else if (lhs.exp > rhs.exp) {
            newlen = (lhs.exp + usellen) - rhs.exp;
            if (newlen < (userlen + reqdig) + 1 || reqdig <= 0) {
                res.exp = rhs.exp;
                if (newlen > reqdig + 1 && reqdig > 0) {
                    tlen = (newlen - reqdig) - 1;
                    userlen -= tlen;
                    res.exp += tlen;
                    newlen = reqdig + 1;
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
                return res.finish(set, false);
            }
        } else {
            newlen = (rhs.exp + userlen) - lhs.exp;
            if (newlen < (usellen + reqdig) + 1 || reqdig <= 0) {
                res.exp = lhs.exp;
                if (newlen > reqdig + 1 && reqdig > 0) {
                    tlen = (newlen - reqdig) - 1;
                    usellen -= tlen;
                    res.exp += tlen;
                    newlen = reqdig + 1;
                }
                if (newlen > userlen) {
                    userlen = newlen;
                }
            } else {
                res.mant = user;
                res.exp = rhs.exp;
                res.ind = rhs.ind;
                if (userlen < reqdig) {
                    res.mant = extend(rhs.mant, reqdig);
                    res.exp -= reqdig - userlen;
                }
                return res.finish(set, false);
            }
        }
        if (lhs.ind == (byte) 0) {
            res.ind = (byte) 1;
        } else {
            res.ind = lhs.ind;
        }
        if ((lhs.ind == (byte) -1 ? 1 : null) == (rhs.ind == (byte) -1 ? 1 : null)) {
            mult = 1;
        } else {
            mult = -1;
            if (rhs.ind != (byte) 0) {
                byte[] t;
                if (((usellen < userlen ? 1 : 0) | (lhs.ind == (byte) 0 ? 1 : 0)) != 0) {
                    t = usel;
                    usel = user;
                    user = t;
                    tlen = usellen;
                    usellen = userlen;
                    userlen = tlen;
                    res.ind = (byte) (-res.ind);
                } else if (usellen <= userlen) {
                    int ia = 0;
                    int ib = 0;
                    int ea = usel.length - 1;
                    int eb = user.length - 1;
                    while (true) {
                        byte b;
                        byte b2;
                        if (ia <= ea) {
                            b = usel[ia];
                        } else if (ib <= eb) {
                            b = (byte) 0;
                        } else if (set.form != 0) {
                            return ZERO;
                        }
                        if (ib <= eb) {
                            b2 = user[ib];
                        } else {
                            b2 = (byte) 0;
                        }
                        if (b == b2) {
                            ia++;
                            ib++;
                        } else if (b < b2) {
                            t = usel;
                            usel = user;
                            user = t;
                            tlen = usellen;
                            usellen = userlen;
                            userlen = tlen;
                            res.ind = (byte) (-res.ind);
                        }
                    }
                }
            }
        }
        res.mant = byteaddsub(usel, usellen, user, userlen, mult, false);
        return res.finish(set, false);
    }

    public int compareTo(BigDecimal rhs) {
        return compareTo(rhs, plainMC);
    }

    public int compareTo(BigDecimal rhs, MathContext set) {
        int i;
        int i2 = 1;
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        if (this.ind == rhs.ind) {
            i = 1;
        } else {
            i = 0;
        }
        if ((i & (this.exp == rhs.exp ? 1 : 0)) != 0) {
            int thislength = this.mant.length;
            if (thislength < rhs.mant.length) {
                return (byte) (-this.ind);
            }
            if (thislength > rhs.mant.length) {
                return this.ind;
            }
            if (thislength <= set.digits) {
                i = 1;
            } else {
                i = 0;
            }
            if (set.digits != 0) {
                i2 = 0;
            }
            if ((i | i2) != 0) {
                int $6 = thislength;
                int i3 = 0;
                while ($6 > 0) {
                    if (this.mant[i3] < rhs.mant[i3]) {
                        return (byte) (-this.ind);
                    }
                    if (this.mant[i3] > rhs.mant[i3]) {
                        return this.ind;
                    }
                    $6--;
                    i3++;
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
        byte[] multer;
        byte[] multand;
        int acclen;
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        BigDecimal lhs = this;
        int padding = 0;
        int reqdig = set.digits;
        if (reqdig > 0) {
            if (this.mant.length > reqdig) {
                lhs = clone(this).round(set);
            }
            if (rhs.mant.length > reqdig) {
                rhs = clone(rhs).round(set);
            }
        } else {
            if (this.exp > 0) {
                padding = this.exp + 0;
            }
            if (rhs.exp > 0) {
                padding += rhs.exp;
            }
        }
        if (lhs.mant.length < rhs.mant.length) {
            multer = lhs.mant;
            multand = rhs.mant;
        } else {
            multer = rhs.mant;
            multand = lhs.mant;
        }
        int multandlen = (multer.length + multand.length) - 1;
        if (multer[0] * multand[0] > 9) {
            acclen = multandlen + 1;
        } else {
            acclen = multandlen;
        }
        BigDecimal res = new BigDecimal();
        byte[] acc = new byte[acclen];
        int $7 = multer.length;
        int n = 0;
        while ($7 > 0) {
            byte mult = multer[n];
            if (mult != (byte) 0) {
                acc = byteaddsub(acc, acc.length, multand, multandlen, mult, true);
            }
            multandlen--;
            $7--;
            n++;
        }
        res.ind = (byte) (lhs.ind * rhs.ind);
        res.exp = (lhs.exp + rhs.exp) - padding;
        if (padding == 0) {
            res.mant = acc;
        } else {
            res.mant = extend(acc, acc.length + padding);
        }
        return res.finish(set, false);
    }

    public BigDecimal negate() {
        return negate(plainMC);
    }

    public BigDecimal negate(MathContext set) {
        if (set.lostDigits) {
            checkdigits((BigDecimal) null, set.digits);
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
            checkdigits((BigDecimal) null, set.digits);
        }
        if (set.form == 0 && this.form == (byte) 0 && (this.mant.length <= set.digits || set.digits == 0)) {
            return this;
        }
        return clone(this).finish(set, false);
    }

    public BigDecimal pow(BigDecimal rhs) {
        return pow(rhs, plainMC);
    }

    public BigDecimal pow(BigDecimal rhs, MathContext set) {
        int workdigits;
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        int n = rhs.intcheck(-999999999, 999999999);
        BigDecimal lhs = this;
        int reqdig = set.digits;
        if (reqdig == 0) {
            if (rhs.ind == (byte) -1) {
                throw new ArithmeticException("Negative power: " + rhs.toString());
            }
            workdigits = 0;
        } else if (rhs.mant.length + rhs.exp > reqdig) {
            throw new ArithmeticException("Too many digits: " + rhs.toString());
        } else {
            if (this.mant.length > reqdig) {
                lhs = clone(this).round(set);
            }
            workdigits = (reqdig + (rhs.mant.length + rhs.exp)) + 1;
        }
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
                res = res.multiply(lhs, workset);
            }
            if (i == 31) {
                break;
            }
            if (seenbit) {
                res = res.multiply(res, workset);
            }
            i++;
        }
        if (rhs.ind < (byte) 0) {
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
        int i;
        int i2 = 1;
        int num = intValueExact();
        if (num > 127) {
            i = 1;
        } else {
            i = 0;
        }
        if (num >= -128) {
            i2 = 0;
        }
        if ((i2 | i) == 0) {
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
        int i;
        if (this.mant.length == rhs.mant.length) {
            i = 1;
        } else {
            i = 0;
        }
        int i2;
        if (((this.form == rhs.form ? 1 : 0) & ((this.exp == rhs.exp ? 1 : 0) & i)) != 0) {
            int $8 = this.mant.length;
            i2 = 0;
            while ($8 > 0) {
                if (this.mant[i2] != rhs.mant[i2]) {
                    return false;
                }
                $8--;
                i2++;
            }
        } else {
            char[] lca = layout();
            char[] rca = rhs.layout();
            if (lca.length != rca.length) {
                return false;
            }
            int $9 = lca.length;
            i2 = 0;
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

    public String format(int before, int after, int explaces, int exdigits, int exformint, int exround) {
        int p;
        char[] newa;
        int i;
        if (((before == 0 ? 1 : 0) | (before < -1 ? 1 : 0)) != 0) {
            badarg("format", 1, String.valueOf(before));
        }
        if (after < -1) {
            badarg("format", 2, String.valueOf(after));
        }
        if (((explaces == 0 ? 1 : 0) | (explaces < -1 ? 1 : 0)) != 0) {
            badarg("format", 3, String.valueOf(explaces));
        }
        if (exdigits < -1) {
            badarg("format", 4, String.valueOf(explaces));
        }
        if (!(exformint == 1 || exformint == 2)) {
            if (exformint == -1) {
                exformint = 1;
            } else {
                badarg("format", 5, String.valueOf(exformint));
            }
        }
        if (exround != 4) {
            if (exround == -1) {
                exround = 4;
            } else {
                try {
                    MathContext mathContext = new MathContext(9, 1, false, exround);
                } catch (IllegalArgumentException e) {
                    badarg("format", 6, String.valueOf(exround));
                }
            }
        }
        BigDecimal num = clone(this);
        if (exdigits == -1) {
            num.form = (byte) 0;
        } else if (num.ind == (byte) 0) {
            num.form = (byte) 0;
        } else {
            int mag = num.exp + num.mant.length;
            if (mag > exdigits) {
                num.form = (byte) exformint;
            } else if (mag < -5) {
                num.form = (byte) exformint;
            } else {
                num.form = (byte) 0;
            }
        }
        if (after >= 0) {
            while (true) {
                int thisafter;
                if (num.form == (byte) 0) {
                    thisafter = -num.exp;
                } else if (num.form == (byte) 1) {
                    thisafter = num.mant.length - 1;
                } else {
                    int lead = ((num.exp + num.mant.length) - 1) % 3;
                    if (lead < 0) {
                        lead += 3;
                    }
                    lead++;
                    if (lead >= num.mant.length) {
                        thisafter = 0;
                    } else {
                        thisafter = num.mant.length - lead;
                    }
                }
                if (thisafter == after) {
                    break;
                } else if (thisafter < after) {
                    num.mant = extend(num.mant, (num.mant.length + after) - thisafter);
                    num.exp -= after - thisafter;
                    if (num.exp < -999999999) {
                        throw new ArithmeticException("Exponent Overflow: " + num.exp);
                    }
                } else {
                    int chop = thisafter - after;
                    if (chop > num.mant.length) {
                        num.mant = ZERO.mant;
                        num.ind = (byte) 0;
                        num.exp = 0;
                    } else {
                        int need = num.mant.length - chop;
                        int oldexp = num.exp;
                        num.round(need, exround);
                        if (num.exp - oldexp == chop) {
                            break;
                        }
                    }
                }
            }
        }
        char[] a = num.layout();
        if (before > 0) {
            int $11 = a.length;
            p = 0;
            while ($11 > 0 && a[p] != '.' && a[p] != 'E') {
                $11--;
                p++;
            }
            if (p > before) {
                badarg("format", 1, String.valueOf(before));
            }
            if (p < before) {
                newa = new char[((a.length + before) - p)];
                int $12 = before - p;
                i = 0;
                while ($12 > 0) {
                    newa[i] = ' ';
                    $12--;
                    i++;
                }
                System.arraycopy(a, 0, newa, i, a.length);
                a = newa;
            }
        }
        if (explaces > 0) {
            int $13 = a.length - 1;
            p = a.length - 1;
            while ($13 > 0 && a[p] != 'E') {
                $13--;
                p--;
            }
            if (p == 0) {
                newa = new char[((a.length + explaces) + 2)];
                System.arraycopy(a, 0, newa, 0, a.length);
                int $14 = explaces + 2;
                i = a.length;
                while ($14 > 0) {
                    newa[i] = ' ';
                    $14--;
                    i++;
                }
                a = newa;
            } else {
                int places = (a.length - p) - 2;
                if (places > explaces) {
                    badarg("format", 3, String.valueOf(explaces));
                }
                if (places < explaces) {
                    newa = new char[((a.length + explaces) - places)];
                    System.arraycopy(a, 0, newa, 0, p + 2);
                    int $15 = explaces - places;
                    i = p + 2;
                    while ($15 > 0) {
                        newa[i] = '0';
                        $15--;
                        i++;
                    }
                    System.arraycopy(a, p + 2, newa, i, places);
                    a = newa;
                }
            }
        }
        return new String(a);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public int intValue() {
        return toBigInteger().intValue();
    }

    public int intValueExact() {
        if (this.ind == (byte) 0) {
            return 0;
        }
        int useexp;
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
        } else if (this.exp + lodigit > 9) {
            throw new ArithmeticException("Conversion overflow: " + toString());
        } else {
            useexp = this.exp;
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
            if (this.ind == (byte) 1) {
                return result;
            }
            return -result;
        } else if (result == Integer.MIN_VALUE && this.ind == (byte) -1 && this.mant[0] == (byte) 2) {
            return result;
        } else {
            throw new ArithmeticException("Conversion overflow: " + toString());
        }
    }

    public long longValue() {
        return toBigInteger().longValue();
    }

    public long longValueExact() {
        if (this.ind == (byte) 0) {
            return 0;
        }
        int useexp;
        int lodigit = this.mant.length - 1;
        if (this.exp < 0) {
            int cstart;
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
        } else if (this.exp + this.mant.length > 18) {
            throw new ArithmeticException("Conversion overflow: " + toString());
        } else {
            useexp = this.exp;
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
            if (this.ind == (byte) 1) {
                return result;
            }
            return -result;
        } else if (result == Long.MIN_VALUE && this.ind == (byte) -1 && this.mant[0] == (byte) 9) {
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
        int ourscale = scale();
        if (ourscale == scale && this.form == (byte) 0) {
            return this;
        }
        BigDecimal res = clone(this);
        if (ourscale <= scale) {
            int padding;
            if (ourscale == 0) {
                padding = res.exp + scale;
            } else {
                padding = scale - ourscale;
            }
            res.mant = extend(res.mant, res.mant.length + padding);
            res.exp = -scale;
        } else if (scale < 0) {
            throw new ArithmeticException("Negative scale: " + scale);
        } else {
            res = res.round(res.mant.length - (ourscale - scale), round);
            if (res.exp != (-scale)) {
                res.mant = extend(res.mant, res.mant.length + 1);
                res.exp--;
            }
        }
        res.form = (byte) 0;
        return res;
    }

    public short shortValueExact() {
        int i;
        int i2 = 1;
        int num = intValueExact();
        if (num > 32767) {
            i = 1;
        } else {
            i = 0;
        }
        if (num >= -32768) {
            i2 = 0;
        }
        if ((i2 | i) == 0) {
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
        int i;
        BigDecimal res;
        int i2 = 1;
        if (this.exp >= 0) {
            i = 1;
        } else {
            i = 0;
        }
        if (this.form != (byte) 0) {
            i2 = 0;
        }
        if ((i & i2) != 0) {
            res = this;
        } else if (this.exp >= 0) {
            res = clone(this);
            res.form = (byte) 0;
        } else if ((-this.exp) >= this.mant.length) {
            res = ZERO;
        } else {
            res = clone(this);
            int newlen = res.mant.length + res.exp;
            byte[] newmant = new byte[newlen];
            System.arraycopy(res.mant, 0, newmant, 0, newlen);
            res.mant = newmant;
            res.form = (byte) 0;
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
        if (scale < 0) {
            throw new NumberFormatException("Negative scale: " + scale);
        }
        res = clone(res);
        res.exp = -scale;
        return res;
    }

    private char[] layout() {
        char[] cmant = new char[this.mant.length];
        int $18 = this.mant.length;
        int i = 0;
        while ($18 > 0) {
            cmant[i] = (char) (this.mant[i] + 48);
            $18--;
            i++;
        }
        char[] rec;
        if (this.form != (byte) 0) {
            StringBuilder sb = new StringBuilder(cmant.length + 15);
            if (this.ind == (byte) -1) {
                sb.append('-');
            }
            int euse = (this.exp + cmant.length) - 1;
            if (this.form == (byte) 1) {
                sb.append(cmant[0]);
                if (cmant.length > 1) {
                    sb.append('.').append(cmant, 1, cmant.length - 1);
                }
            } else {
                int sig = euse % 3;
                if (sig < 0) {
                    sig += 3;
                }
                euse -= sig;
                sig++;
                if (sig >= cmant.length) {
                    sb.append(cmant, 0, cmant.length);
                    for (int $19 = sig - cmant.length; $19 > 0; $19--) {
                        sb.append('0');
                    }
                } else {
                    sb.append(cmant, 0, sig).append('.').append(cmant, sig, cmant.length - sig);
                }
            }
            if (euse != 0) {
                char csign;
                if (euse < 0) {
                    csign = '-';
                    euse = -euse;
                } else {
                    csign = '+';
                }
                sb.append('E').append(csign).append(euse);
            }
            rec = new char[sb.length()];
            int srcEnd = sb.length();
            if (srcEnd != 0) {
                sb.getChars(0, srcEnd, rec, 0);
            }
            return rec;
        } else if (this.exp != 0) {
            int needsign = this.ind == (byte) -1 ? 1 : 0;
            int mag = this.exp + cmant.length;
            if (mag < 1) {
                rec = new char[((needsign + 2) - this.exp)];
                if (needsign != 0) {
                    rec[0] = '-';
                }
                rec[needsign] = '0';
                rec[needsign + 1] = '.';
                int $20 = -mag;
                i = needsign + 2;
                while ($20 > 0) {
                    rec[i] = '0';
                    $20--;
                    i++;
                }
                System.arraycopy(cmant, 0, rec, (needsign + 2) - mag, cmant.length);
                return rec;
            } else if (mag > cmant.length) {
                rec = new char[(needsign + mag)];
                if (needsign != 0) {
                    rec[0] = '-';
                }
                System.arraycopy(cmant, 0, rec, needsign, cmant.length);
                int $21 = mag - cmant.length;
                i = needsign + cmant.length;
                while ($21 > 0) {
                    rec[i] = '0';
                    $21--;
                    i++;
                }
                return rec;
            } else {
                rec = new char[((needsign + 1) + cmant.length)];
                if (needsign != 0) {
                    rec[0] = '-';
                }
                System.arraycopy(cmant, 0, rec, needsign, mag);
                rec[needsign + mag] = '.';
                System.arraycopy(cmant, mag, rec, (needsign + mag) + 1, cmant.length - mag);
                return rec;
            }
        } else if (this.ind >= (byte) 0) {
            return cmant;
        } else {
            rec = new char[(cmant.length + 1)];
            rec[0] = '-';
            System.arraycopy(cmant, 0, rec, 1, cmant.length);
            return rec;
        }
    }

    private int intcheck(int min, int max) {
        int i;
        int i2 = 1;
        int i3 = intValueExact();
        if (i3 < min) {
            i = 1;
        } else {
            i = 0;
        }
        if (i3 <= max) {
            i2 = 0;
        }
        if ((i2 | i) == 0) {
            return i3;
        }
        throw new ArithmeticException("Conversion overflow: " + i3);
    }

    private BigDecimal dodivide(char code, BigDecimal rhs, MathContext set, int scale) {
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        BigDecimal lhs = this;
        if (rhs.ind == (byte) 0) {
            throw new ArithmeticException("Divide by 0");
        } else if (this.ind != (byte) 0) {
            int reqdig = set.digits;
            if (reqdig > 0) {
                if (this.mant.length > reqdig) {
                    lhs = clone(this).round(set);
                }
                if (rhs.mant.length > reqdig) {
                    rhs = clone(rhs).round(set);
                }
            } else {
                if (scale == -1) {
                    scale = scale();
                }
                reqdig = this.mant.length;
                if (scale != (-this.exp)) {
                    reqdig = (reqdig + scale) + this.exp;
                }
                reqdig = (reqdig - (rhs.mant.length - 1)) - rhs.exp;
                if (reqdig < this.mant.length) {
                    reqdig = this.mant.length;
                }
                if (reqdig < rhs.mant.length) {
                    reqdig = rhs.mant.length;
                }
            }
            int newexp = ((lhs.exp - rhs.exp) + lhs.mant.length) - rhs.mant.length;
            if (newexp >= 0 || code == 'D') {
                int i;
                BigDecimal res = new BigDecimal();
                res.ind = (byte) (lhs.ind * rhs.ind);
                res.exp = newexp;
                res.mant = new byte[(reqdig + 1)];
                int newlen = (reqdig + reqdig) + 1;
                byte[] var1 = extend(lhs.mant, newlen);
                int var1len = newlen;
                byte[] var2 = rhs.mant;
                int var2len = newlen;
                int b2b = (var2[0] * 10) + 1;
                if (var2.length > 1) {
                    b2b += var2[1];
                }
                int have = 0;
                loop0:
                while (true) {
                    int thisdigit = 0;
                    while (var1len >= var2len) {
                        int ba;
                        if (var1len == var2len) {
                            int $22 = var1len;
                            i = 0;
                            while ($22 > 0) {
                                byte b;
                                if (i < var2.length) {
                                    b = var2[i];
                                } else {
                                    b = (byte) 0;
                                }
                                if (var1[i] < b) {
                                    break;
                                } else if (var1[i] > b) {
                                    ba = var1[0];
                                } else {
                                    $22--;
                                    i++;
                                }
                            }
                            res.mant[have] = (byte) (thisdigit + 1);
                            have++;
                            var1[0] = (byte) 0;
                            break loop0;
                        }
                        ba = var1[0] * 10;
                        if (var1len > 1) {
                            ba += var1[1];
                        }
                        int mult = (ba * 10) / b2b;
                        if (mult == 0) {
                            mult = 1;
                        }
                        thisdigit += mult;
                        var1 = byteaddsub(var1, var1len, var2, var2len, -mult, true);
                        if (var1[0] == (byte) 0) {
                            int $23 = var1len - 2;
                            int start = 0;
                            while (start <= $23 && var1[start] == (byte) 0) {
                                var1len--;
                                start++;
                            }
                            if (start != 0) {
                                System.arraycopy(var1, start, var1, 0, var1len);
                            }
                        }
                    }
                    if (((thisdigit != 0 ? 1 : 0) | (have != 0 ? 1 : 0)) != 0) {
                        res.mant[have] = (byte) thisdigit;
                        have++;
                        if (have != reqdig + 1) {
                            if (var1[0] == (byte) 0) {
                                break;
                            }
                        }
                        break;
                    }
                    if ((scale >= 0 && (-res.exp) > scale) || (code != 'D' && res.exp <= 0)) {
                        break;
                    }
                    res.exp--;
                    var2len--;
                }
                if (have == 0) {
                    have = 1;
                }
                if (((code == 'R' ? 1 : 0) | (code == 'I' ? 1 : 0)) != 0) {
                    if (res.exp + have > reqdig) {
                        throw new ArithmeticException("Integer overflow");
                    } else if (code == 'R') {
                        if (res.mant[0] == (byte) 0) {
                            return clone(lhs).finish(set, false);
                        }
                        if (var1[0] == (byte) 0) {
                            return ZERO;
                        }
                        res.ind = lhs.ind;
                        res.exp = (res.exp - (((reqdig + reqdig) + 1) - lhs.mant.length)) + lhs.exp;
                        int d = var1len;
                        i = var1len - 1;
                        while (i >= 1) {
                            if (((res.exp >= lhs.exp ? 1 : 0) | (res.exp >= rhs.exp ? 1 : 0)) != 0 || var1[i] != (byte) 0) {
                                break;
                            }
                            d--;
                            res.exp++;
                            i--;
                        }
                        if (d < var1.length) {
                            byte[] newvar1 = new byte[d];
                            System.arraycopy(var1, 0, newvar1, 0, d);
                            var1 = newvar1;
                        }
                        res.mant = var1;
                        return res.finish(set, false);
                    }
                } else if (var1[0] != (byte) 0) {
                    byte lasthave = res.mant[have - 1];
                    if (lasthave % 5 == 0) {
                        res.mant[have - 1] = (byte) (lasthave + 1);
                    }
                }
                if (scale >= 0) {
                    if (have != res.mant.length) {
                        res.exp -= res.mant.length - have;
                    }
                    res.round(res.mant.length - ((-res.exp) - scale), set.roundingMode);
                    if (res.exp != (-scale)) {
                        res.mant = extend(res.mant, res.mant.length + 1);
                        res.exp--;
                    }
                    return res.finish(set, true);
                }
                if (have == res.mant.length) {
                    res.round(set);
                    have = reqdig;
                } else if (res.mant[0] == (byte) 0) {
                    return ZERO;
                } else {
                    byte[] newmant = new byte[have];
                    System.arraycopy(res.mant, 0, newmant, 0, have);
                    res.mant = newmant;
                }
                return res.finish(set, true);
            } else if (code == 'I') {
                return ZERO;
            } else {
                return clone(lhs).finish(set, false);
            }
        } else if (set.form != 0) {
            return ZERO;
        } else {
            if (scale == -1) {
                return this;
            }
            return setScale(scale);
        }
    }

    private void bad(char[] s) {
        throw new NumberFormatException("Not a number: " + String.valueOf(s));
    }

    private void badarg(String name, int pos, String value) {
        throw new IllegalArgumentException("Bad argument " + pos + " " + "to" + " " + name + ":" + " " + value);
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
        int alength = a.length;
        int blength = b.length;
        int ap = avlen - 1;
        int bp = bvlen - 1;
        int maxarr = bp;
        if (bp < ap) {
            maxarr = ap;
        }
        byte[] reb = null;
        if (reuse && maxarr + 1 == alength) {
            reb = a;
        }
        if (reb == null) {
            reb = new byte[(maxarr + 1)];
        }
        boolean quickm = false;
        if (m == 1) {
            quickm = true;
        } else if (m == -1) {
            quickm = true;
        }
        int digit = 0;
        for (int op = maxarr; op >= 0; op--) {
            if (ap >= 0) {
                if (ap < alength) {
                    digit += a[ap];
                }
                ap--;
            }
            if (bp >= 0) {
                if (bp < blength) {
                    if (!quickm) {
                        digit += b[bp] * m;
                    } else if (m > 0) {
                        digit += b[bp];
                    } else {
                        digit -= b[bp];
                    }
                }
                bp--;
            }
            if (digit >= 10 || digit < 0) {
                int dp90 = digit + 90;
                reb[op] = bytedig[dp90];
                digit = bytecar[dp90];
            } else {
                reb[op] = (byte) digit;
                digit = 0;
            }
        }
        if (digit == 0) {
            return reb;
        }
        byte[] newarr = null;
        if (reuse && maxarr + 2 == a.length) {
            newarr = a;
        }
        if (newarr == null) {
            newarr = new byte[(maxarr + 2)];
        }
        newarr[0] = (byte) digit;
        if (maxarr < 10) {
            int $24 = maxarr + 1;
            int i = 0;
            while ($24 > 0) {
                newarr[i + 1] = reb[i];
                $24--;
                i++;
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
                digit += 100;
                work[op] = (byte) (digit % 10);
                bytecar[op] = (byte) ((digit / 10) - 10);
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

    private BigDecimal round(int len, int mode) {
        int adjust = this.mant.length - len;
        if (adjust <= 0) {
            return this;
        }
        boolean reuse;
        byte first;
        this.exp += adjust;
        int sign = this.ind;
        byte[] oldmant = this.mant;
        if (len > 0) {
            this.mant = new byte[len];
            System.arraycopy(oldmant, 0, this.mant, 0, len);
            reuse = true;
            first = oldmant[len];
        } else {
            this.mant = ZERO.mant;
            this.ind = (byte) 0;
            reuse = false;
            if (len == 0) {
                first = oldmant[0];
            } else {
                first = (byte) 0;
            }
        }
        int increment = 0;
        if (mode == 4) {
            if (first >= (byte) 5) {
                increment = sign;
            }
        } else if (mode == 7) {
            if (!allzero(oldmant, len)) {
                throw new ArithmeticException("Rounding necessary");
            }
        } else if (mode == 5) {
            if (first > (byte) 5) {
                increment = sign;
            } else if (first == (byte) 5 && !allzero(oldmant, len + 1)) {
                increment = sign;
            }
        } else if (mode == 6) {
            if (first > (byte) 5) {
                increment = sign;
            } else if (first == (byte) 5) {
                if (!allzero(oldmant, len + 1)) {
                    increment = sign;
                } else if (this.mant[this.mant.length - 1] % 2 != 0) {
                    increment = sign;
                }
            }
        } else if (mode != 1) {
            if (mode == 0) {
                if (!allzero(oldmant, len)) {
                    increment = sign;
                }
            } else if (mode == 2) {
                if (sign > 0 && !allzero(oldmant, len)) {
                    increment = sign;
                }
            } else if (mode != 3) {
                throw new IllegalArgumentException("Bad round value: " + mode);
            } else if (sign < 0 && !allzero(oldmant, len)) {
                increment = sign;
            }
        }
        if (increment != 0) {
            if (this.ind == (byte) 0) {
                this.mant = ONE.mant;
                this.ind = (byte) increment;
            } else {
                if (this.ind == (byte) -1) {
                    increment = -increment;
                }
                byte[] newmant = byteaddsub(this.mant, this.mant.length, ONE.mant, 1, increment, reuse);
                if (newmant.length > this.mant.length) {
                    this.exp++;
                    System.arraycopy(newmant, 0, this.mant, 0, this.mant.length);
                } else {
                    this.mant = newmant;
                }
            }
        }
        if (this.exp <= 999999999) {
            return this;
        }
        throw new ArithmeticException("Exponent Overflow: " + this.exp);
    }

    private static final boolean allzero(byte[] array, int start) {
        if (start < 0) {
            start = 0;
        }
        int $25 = array.length - 1;
        for (int i = start; i <= $25; i++) {
            if (array[i] != (byte) 0) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:48:0x00a3, code:
            if (r3 <= 999999999) goto L_0x00a5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private BigDecimal finish(MathContext set, boolean strip) {
        int i;
        byte[] newmant;
        int i2 = 1;
        if (set.digits != 0 && this.mant.length > set.digits) {
            round(set);
        }
        if (strip && set.form != 0) {
            int d = this.mant.length;
            i = d - 1;
            while (i >= 1 && this.mant[i] == (byte) 0) {
                d--;
                this.exp++;
                i--;
            }
            if (d < this.mant.length) {
                newmant = new byte[d];
                System.arraycopy(this.mant, 0, newmant, 0, d);
                this.mant = newmant;
            }
        }
        this.form = (byte) 0;
        int $26 = this.mant.length;
        i = 0;
        while ($26 > 0) {
            if (this.mant[i] != (byte) 0) {
                if (i > 0) {
                    newmant = new byte[(this.mant.length - i)];
                    System.arraycopy(this.mant, i, newmant, 0, this.mant.length - i);
                    this.mant = newmant;
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
                mag--;
                int i3 = mag < -999999999 ? 1 : 0;
                if (mag <= 999999999) {
                    i2 = 0;
                }
                if ((i2 | i3) != 0) {
                    if (this.form == (byte) 2) {
                        int sig = mag % 3;
                        if (sig < 0) {
                            sig += 3;
                        }
                        mag -= sig;
                        if (mag >= -999999999) {
                        }
                    }
                    throw new ArithmeticException("Exponent Overflow: " + mag);
                }
                return this;
            }
            $26--;
            i++;
        }
        this.ind = (byte) 0;
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
