package android.icu.math;

import android.icu.lang.UCharacter;
import dalvik.bytecode.Opcodes;
import java.io.Serializable;
import java.math.BigInteger;
import javax.xml.datatype.DatatypeConstants;

public class BigDecimal extends Number implements Serializable, Comparable<BigDecimal> {
    private static final int MaxArg = 999999999;
    private static final int MaxExp = 999999999;
    private static final int MinArg = -999999999;
    private static final int MinExp = -999999999;
    public static final BigDecimal ONE = null;
    public static final int ROUND_CEILING = 2;
    public static final int ROUND_DOWN = 1;
    public static final int ROUND_FLOOR = 3;
    public static final int ROUND_HALF_DOWN = 5;
    public static final int ROUND_HALF_EVEN = 6;
    public static final int ROUND_HALF_UP = 4;
    public static final int ROUND_UNNECESSARY = 7;
    public static final int ROUND_UP = 0;
    public static final BigDecimal TEN = null;
    public static final BigDecimal ZERO = null;
    private static byte[] bytecar = null;
    private static byte[] bytedig = null;
    private static final byte isneg = (byte) -1;
    private static final byte ispos = (byte) 1;
    private static final byte iszero = (byte) 0;
    private static final MathContext plainMC = null;
    private static final long serialVersionUID = 8245355804974198832L;
    private int exp;
    private byte form;
    private byte ind;
    private byte[] mant;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.math.BigDecimal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.math.BigDecimal.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.math.BigDecimal.<clinit>():void");
    }

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
        this(inchars, ROUND_UP, inchars.length);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public BigDecimal(char[] inchars, int offset, int length) {
        int j;
        int dvalue;
        int $3;
        int $5;
        this.form = iszero;
        if (length <= 0) {
            bad(inchars);
        }
        this.ind = ispos;
        if (inchars[offset] == '-') {
            length--;
            if (length == 0) {
                bad(inchars);
            }
            this.ind = isneg;
            offset += ROUND_DOWN;
        } else if (inchars[offset] == '+') {
            length--;
            if (length == 0) {
                bad(inchars);
            }
            offset += ROUND_DOWN;
        }
        boolean exotic = false;
        boolean hadexp = false;
        int d = ROUND_UP;
        int dotoff = -1;
        int last = -1;
        int $1 = length;
        int i = offset;
        while ($1 > 0) {
            char sj;
            int $4;
            int mag;
            char si = inchars[i];
            if (si >= '0' && si <= '9') {
                last = i;
                d += ROUND_DOWN;
            } else if (si == '.') {
                if (dotoff >= 0) {
                    bad(inchars);
                }
                dotoff = i - offset;
            } else if (si == 'e' || si == 'E') {
                int k;
                int i2;
                int i3;
                if (i - offset > length - 2) {
                    bad(inchars);
                }
                boolean eneg = false;
                if (inchars[i + ROUND_DOWN] == '-') {
                    eneg = true;
                    k = i + ROUND_CEILING;
                } else if (inchars[i + ROUND_DOWN] == '+') {
                    k = i + ROUND_CEILING;
                } else {
                    k = i + ROUND_DOWN;
                }
                int elen = length - (k - offset);
                if (elen == 0) {
                    i2 = ROUND_DOWN;
                } else {
                    i2 = ROUND_UP;
                }
                if (elen > 9) {
                    i3 = ROUND_DOWN;
                } else {
                    i3 = ROUND_UP;
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
                    j += ROUND_DOWN;
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
                for (i = offset; i <= $3; i += ROUND_DOWN) {
                    si = inchars[i];
                    if (si == '0') {
                        offset += ROUND_DOWN;
                        dotoff--;
                        d--;
                    } else if (si != '.') {
                        offset += ROUND_DOWN;
                        dotoff--;
                    } else if (si > '9' && UCharacter.digit(si, 10) == 0) {
                        offset += ROUND_DOWN;
                        dotoff--;
                        d--;
                    }
                }
                this.mant = new byte[d];
                j = offset;
                if (exotic) {
                    $5 = d;
                    i = ROUND_UP;
                    while ($5 > 0) {
                        if (i == dotoff) {
                            j += ROUND_DOWN;
                        }
                        this.mant[i] = (byte) (inchars[j] - 48);
                        j += ROUND_DOWN;
                        $5--;
                        i += ROUND_DOWN;
                    }
                } else {
                    $4 = d;
                    i = ROUND_UP;
                    while ($4 > 0) {
                        if (i == dotoff) {
                            j += ROUND_DOWN;
                        }
                        sj = inchars[j];
                        if (sj > '9') {
                            this.mant[i] = (byte) (sj - 48);
                        } else {
                            dvalue = UCharacter.digit(sj, 10);
                            if (dvalue < 0) {
                                bad(inchars);
                            }
                            this.mant[i] = (byte) dvalue;
                        }
                        j += ROUND_DOWN;
                        $4--;
                        i += ROUND_DOWN;
                    }
                }
                if (this.mant[ROUND_UP] == null) {
                    this.ind = iszero;
                    if (this.exp > 0) {
                        this.exp = ROUND_UP;
                    }
                    if (hadexp) {
                        this.mant = ZERO.mant;
                        this.exp = ROUND_UP;
                    }
                } else if (hadexp) {
                    this.form = ispos;
                    mag = (this.exp + this.mant.length) - 1;
                    if (((mag <= MaxExp ? ROUND_DOWN : ROUND_UP) | (mag >= MinExp ? ROUND_DOWN : ROUND_UP)) != 0) {
                        bad(inchars);
                    }
                }
            } else {
                if (!UCharacter.isDigit(si)) {
                    bad(inchars);
                }
                exotic = true;
                last = i;
                d += ROUND_DOWN;
            }
            $1--;
            i += ROUND_DOWN;
        }
        if (d == 0) {
            bad(inchars);
        }
        if (dotoff >= 0) {
            this.exp = (this.exp + dotoff) - d;
        }
        $3 = last - 1;
        for (i = offset; i <= $3; i += ROUND_DOWN) {
            si = inchars[i];
            if (si == '0') {
                offset += ROUND_DOWN;
                dotoff--;
                d--;
            } else if (si != '.') {
                offset += ROUND_DOWN;
                dotoff--;
                d--;
            } else {
                offset += ROUND_DOWN;
                dotoff--;
            }
        }
        this.mant = new byte[d];
        j = offset;
        if (exotic) {
            $5 = d;
            i = ROUND_UP;
            while ($5 > 0) {
                if (i == dotoff) {
                    j += ROUND_DOWN;
                }
                this.mant[i] = (byte) (inchars[j] - 48);
                j += ROUND_DOWN;
                $5--;
                i += ROUND_DOWN;
            }
        } else {
            $4 = d;
            i = ROUND_UP;
            while ($4 > 0) {
                if (i == dotoff) {
                    j += ROUND_DOWN;
                }
                sj = inchars[j];
                if (sj > '9') {
                    dvalue = UCharacter.digit(sj, 10);
                    if (dvalue < 0) {
                        bad(inchars);
                    }
                    this.mant[i] = (byte) dvalue;
                } else {
                    this.mant[i] = (byte) (sj - 48);
                }
                j += ROUND_DOWN;
                $4--;
                i += ROUND_DOWN;
            }
        }
        if (this.mant[ROUND_UP] == null) {
            this.ind = iszero;
            if (this.exp > 0) {
                this.exp = ROUND_UP;
            }
            if (hadexp) {
                this.mant = ZERO.mant;
                this.exp = ROUND_UP;
            }
        } else if (hadexp) {
            this.form = ispos;
            mag = (this.exp + this.mant.length) - 1;
            if (mag >= MinExp) {
            }
            if (mag <= MaxExp) {
            }
            if (((mag <= MaxExp ? ROUND_DOWN : ROUND_UP) | (mag >= MinExp ? ROUND_DOWN : ROUND_UP)) != 0) {
                bad(inchars);
            }
        }
    }

    public BigDecimal(double num) {
        this(new java.math.BigDecimal(num).toString());
    }

    public BigDecimal(int num) {
        this.form = iszero;
        if (num > 9 || num < -9) {
            if (num > 0) {
                this.ind = ispos;
                num = -num;
            } else {
                this.ind = isneg;
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
            this.ind = iszero;
        } else if (num == ROUND_DOWN) {
            this.mant = ONE.mant;
            this.ind = ispos;
        } else if (num == -1) {
            this.mant = ONE.mant;
            this.ind = isneg;
        } else {
            this.mant = new byte[ROUND_DOWN];
            if (num > 0) {
                this.mant[ROUND_UP] = (byte) num;
                this.ind = ispos;
            } else {
                this.mant[ROUND_UP] = (byte) (-num);
                this.ind = isneg;
            }
        }
    }

    public BigDecimal(long num) {
        this.form = iszero;
        if (num > 0) {
            this.ind = ispos;
            num = -num;
        } else if (num == 0) {
            this.ind = iszero;
        } else {
            this.ind = isneg;
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
        this(string.toCharArray(), ROUND_UP, string.length());
    }

    private BigDecimal() {
        this.form = iszero;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public BigDecimal add(BigDecimal rhs, MathContext set) {
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        BigDecimal lhs = this;
        if (this.ind == null && set.form != 0) {
            return rhs.plus(set);
        }
        if (rhs.ind == null && set.form != 0) {
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
        int i = lhs.exp;
        int i2 = rhs.exp;
        if (i == r0) {
            res.exp = lhs.exp;
        } else {
            i = lhs.exp;
            i2 = rhs.exp;
            int newlen;
            if (i > r0) {
                newlen = (lhs.exp + usellen) - rhs.exp;
                if (newlen < (userlen + reqdig) + ROUND_DOWN || reqdig <= 0) {
                    res.exp = rhs.exp;
                    if (newlen > reqdig + ROUND_DOWN && reqdig > 0) {
                        tlen = (newlen - reqdig) - 1;
                        userlen -= tlen;
                        res.exp += tlen;
                        newlen = reqdig + ROUND_DOWN;
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
            }
            newlen = (rhs.exp + userlen) - lhs.exp;
            if (newlen < (usellen + reqdig) + ROUND_DOWN || reqdig <= 0) {
                res.exp = lhs.exp;
                if (newlen > reqdig + ROUND_DOWN && reqdig > 0) {
                    tlen = (newlen - reqdig) - 1;
                    usellen -= tlen;
                    res.exp += tlen;
                    newlen = reqdig + ROUND_DOWN;
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
        if (lhs.ind == null) {
            res.ind = ispos;
        } else {
            res.ind = lhs.ind;
        }
        Object obj = lhs.ind == -1 ? ROUND_DOWN : null;
        byte b = rhs.ind;
        if (obj == (r0 == -1 ? ROUND_DOWN : null)) {
            mult = ROUND_DOWN;
        } else {
            mult = -1;
            if (rhs.ind != null) {
                if (((usellen < userlen ? ROUND_DOWN : ROUND_UP) | (lhs.ind == null ? ROUND_DOWN : ROUND_UP)) != 0) {
                    byte[] t = usel;
                    usel = user;
                    user = t;
                    tlen = usellen;
                    usellen = userlen;
                    userlen = tlen;
                    res.ind = (byte) (-res.ind);
                } else if (usellen <= userlen) {
                    int ia = ROUND_UP;
                    int ib = ROUND_UP;
                    int ea = usel.length - 1;
                    int eb = user.length - 1;
                    while (true) {
                        byte b2;
                        byte b3;
                        if (ia <= ea) {
                            b2 = usel[ia];
                        } else if (ib > eb) {
                            break;
                        } else {
                            b2 = iszero;
                        }
                        if (ib <= eb) {
                            b3 = user[ib];
                        } else {
                            b3 = iszero;
                        }
                        if (b2 != b3) {
                            break;
                        }
                        ia += ROUND_DOWN;
                        ib += ROUND_DOWN;
                    }
                    if (set.form != 0) {
                        return ZERO;
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
        int i2 = ROUND_DOWN;
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        if (this.ind == rhs.ind) {
            i = ROUND_DOWN;
        } else {
            i = ROUND_UP;
        }
        if ((i & (this.exp == rhs.exp ? ROUND_DOWN : ROUND_UP)) != 0) {
            int thislength = this.mant.length;
            if (thislength < rhs.mant.length) {
                return (byte) (-this.ind);
            }
            if (thislength > rhs.mant.length) {
                return this.ind;
            }
            if (thislength <= set.digits) {
                i = ROUND_DOWN;
            } else {
                i = ROUND_UP;
            }
            if (set.digits != 0) {
                i2 = ROUND_UP;
            }
            if ((i | i2) != 0) {
                int $6 = thislength;
                int i3 = ROUND_UP;
                while ($6 > 0) {
                    if (this.mant[i3] < rhs.mant[i3]) {
                        return (byte) (-this.ind);
                    }
                    if (this.mant[i3] > rhs.mant[i3]) {
                        return this.ind;
                    }
                    $6--;
                    i3 += ROUND_DOWN;
                }
                return ROUND_UP;
            }
        } else if (this.ind < rhs.ind) {
            return -1;
        } else {
            if (this.ind > rhs.ind) {
                return ROUND_DOWN;
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
        return dodivide('D', rhs, new MathContext(ROUND_UP, ROUND_UP, false, round), -1);
    }

    public BigDecimal divide(BigDecimal rhs, int scale, int round) {
        if (scale >= 0) {
            return dodivide('D', rhs, new MathContext(ROUND_UP, ROUND_UP, false, round), scale);
        }
        throw new ArithmeticException("Negative scale: " + scale);
    }

    public BigDecimal divide(BigDecimal rhs, MathContext set) {
        return dodivide('D', rhs, set, -1);
    }

    public BigDecimal divideInteger(BigDecimal rhs) {
        return dodivide('I', rhs, plainMC, ROUND_UP);
    }

    public BigDecimal divideInteger(BigDecimal rhs, MathContext set) {
        return dodivide('I', rhs, set, ROUND_UP);
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
        int padding = ROUND_UP;
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
                padding = this.exp + ROUND_UP;
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
        if (multer[ROUND_UP] * multand[ROUND_UP] > 9) {
            acclen = multandlen + ROUND_DOWN;
        } else {
            acclen = multandlen;
        }
        BigDecimal res = new BigDecimal();
        byte[] acc = new byte[acclen];
        int $7 = multer.length;
        int n = ROUND_UP;
        while ($7 > 0) {
            byte mult = multer[n];
            if (mult != null) {
                acc = byteaddsub(acc, acc.length, multand, multandlen, mult, true);
            }
            multandlen--;
            $7--;
            n += ROUND_DOWN;
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
        if (set.form == 0 && this.form == null && (this.mant.length <= set.digits || set.digits == 0)) {
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
        int n = rhs.intcheck(MinExp, MaxExp);
        BigDecimal lhs = this;
        int reqdig = set.digits;
        if (reqdig == 0) {
            if (rhs.ind == -1) {
                throw new ArithmeticException("Negative power: " + rhs.toString());
            }
            workdigits = ROUND_UP;
        } else if (rhs.mant.length + rhs.exp > reqdig) {
            throw new ArithmeticException("Too many digits: " + rhs.toString());
        } else {
            if (this.mant.length > reqdig) {
                lhs = clone(this).round(set);
            }
            workdigits = (reqdig + (rhs.mant.length + rhs.exp)) + ROUND_DOWN;
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
        int i = ROUND_DOWN;
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
            i += ROUND_DOWN;
        }
        if (rhs.ind < null) {
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
        int i2 = ROUND_DOWN;
        int num = intValueExact();
        if (num > Opcodes.OP_NEG_FLOAT) {
            i = ROUND_DOWN;
        } else {
            i = ROUND_UP;
        }
        if (num >= -128) {
            i2 = ROUND_UP;
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
            i = ROUND_DOWN;
        } else {
            i = ROUND_UP;
        }
        int i2;
        if (((this.form == rhs.form ? ROUND_DOWN : ROUND_UP) & ((this.exp == rhs.exp ? ROUND_DOWN : ROUND_UP) & i)) != 0) {
            int $8 = this.mant.length;
            i2 = ROUND_UP;
            while ($8 > 0) {
                if (this.mant[i2] != rhs.mant[i2]) {
                    return false;
                }
                $8--;
                i2 += ROUND_DOWN;
            }
        } else {
            char[] lca = layout();
            char[] rca = rhs.layout();
            if (lca.length != rca.length) {
                return false;
            }
            int $9 = lca.length;
            i2 = ROUND_UP;
            while ($9 > 0) {
                if (lca[i2] != rca[i2]) {
                    return false;
                }
                $9--;
                i2 += ROUND_DOWN;
            }
        }
        return true;
    }

    public float floatValue() {
        return Float.valueOf(toString()).floatValue();
    }

    public String format(int before, int after) {
        return format(before, after, -1, -1, ROUND_DOWN, ROUND_HALF_UP);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String format(int before, int after, int explaces, int exdigits, int exformint, int exround) {
        int p;
        Object newa;
        int i;
        if (((before == 0 ? ROUND_DOWN : ROUND_UP) | (before < -1 ? ROUND_DOWN : ROUND_UP)) != 0) {
            badarg("format", ROUND_DOWN, String.valueOf(before));
        }
        if (after < -1) {
            badarg("format", ROUND_CEILING, String.valueOf(after));
        }
        if (((explaces == 0 ? ROUND_DOWN : ROUND_UP) | (explaces < -1 ? ROUND_DOWN : ROUND_UP)) != 0) {
            badarg("format", ROUND_FLOOR, String.valueOf(explaces));
        }
        if (exdigits < -1) {
            badarg("format", ROUND_HALF_UP, String.valueOf(explaces));
        }
        if (!(exformint == ROUND_DOWN || exformint == ROUND_CEILING)) {
            if (exformint == -1) {
                exformint = ROUND_DOWN;
            } else {
                badarg("format", ROUND_HALF_DOWN, String.valueOf(exformint));
            }
        }
        if (exround != ROUND_HALF_UP) {
            if (exround == -1) {
                exround = ROUND_HALF_UP;
            } else {
                try {
                    MathContext mathContext = new MathContext(9, ROUND_DOWN, false, exround);
                } catch (IllegalArgumentException e) {
                    badarg("format", ROUND_HALF_EVEN, String.valueOf(exround));
                }
            }
        }
        BigDecimal num = clone(this);
        if (exdigits == -1) {
            num.form = iszero;
        } else if (num.ind == null) {
            num.form = iszero;
        } else {
            int mag = num.exp + num.mant.length;
            if (mag > exdigits) {
                num.form = (byte) exformint;
            } else if (mag < -5) {
                num.form = (byte) exformint;
            } else {
                num.form = iszero;
            }
        }
        if (after >= 0) {
            while (true) {
                int thisafter;
                int length;
                if (num.form == null) {
                    thisafter = -num.exp;
                } else {
                    byte b = num.form;
                    if (r0 == ROUND_DOWN) {
                        thisafter = num.mant.length - 1;
                    } else {
                        int lead = ((num.exp + num.mant.length) - 1) % ROUND_FLOOR;
                        if (lead < 0) {
                            lead += ROUND_FLOOR;
                        }
                        lead += ROUND_DOWN;
                        length = num.mant.length;
                        if (lead >= r0) {
                            thisafter = ROUND_UP;
                        } else {
                            thisafter = num.mant.length - lead;
                        }
                    }
                }
                if (thisafter == after) {
                    break;
                } else if (thisafter < after) {
                    break;
                } else {
                    int chop = thisafter - after;
                    length = num.mant.length;
                    if (chop > r0) {
                        num.mant = ZERO.mant;
                        num.ind = iszero;
                        num.exp = ROUND_UP;
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
            p = ROUND_UP;
            while ($11 > 0 && a[p] != '.' && a[p] != 'E') {
                $11--;
                p += ROUND_DOWN;
            }
            if (p > before) {
                badarg("format", ROUND_DOWN, String.valueOf(before));
            }
            if (p < before) {
                newa = new char[((a.length + before) - p)];
                int $12 = before - p;
                i = ROUND_UP;
                while ($12 > 0) {
                    newa[i] = ' ';
                    $12--;
                    i += ROUND_DOWN;
                }
                System.arraycopy(a, ROUND_UP, newa, i, a.length);
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
                newa = new char[((a.length + explaces) + ROUND_CEILING)];
                System.arraycopy(a, ROUND_UP, newa, ROUND_UP, a.length);
                int $14 = explaces + ROUND_CEILING;
                i = a.length;
                while ($14 > 0) {
                    newa[i] = ' ';
                    $14--;
                    i += ROUND_DOWN;
                }
                a = newa;
            } else {
                int places = (a.length - p) - 2;
                if (places > explaces) {
                    badarg("format", ROUND_FLOOR, String.valueOf(explaces));
                }
                if (places < explaces) {
                    newa = new char[((a.length + explaces) - places)];
                    System.arraycopy(a, ROUND_UP, newa, ROUND_UP, p + ROUND_CEILING);
                    int $15 = explaces - places;
                    i = p + ROUND_CEILING;
                    while ($15 > 0) {
                        newa[i] = '0';
                        $15--;
                        i += ROUND_DOWN;
                    }
                    System.arraycopy(a, p + ROUND_CEILING, newa, i, places);
                    Object a2 = newa;
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
        if (this.ind == null) {
            return ROUND_UP;
        }
        int useexp;
        int lodigit = this.mant.length - 1;
        if (this.exp < 0) {
            lodigit += this.exp;
            if (!allzero(this.mant, lodigit + ROUND_DOWN)) {
                throw new ArithmeticException("Decimal part non-zero: " + toString());
            } else if (lodigit < 0) {
                return ROUND_UP;
            } else {
                useexp = ROUND_UP;
            }
        } else if (this.exp + lodigit > 9) {
            throw new ArithmeticException("Conversion overflow: " + toString());
        } else {
            useexp = this.exp;
        }
        int result = ROUND_UP;
        int $16 = lodigit + useexp;
        for (int i = ROUND_UP; i <= $16; i += ROUND_DOWN) {
            result *= 10;
            if (i <= lodigit) {
                result += this.mant[i];
            }
        }
        if (lodigit + useexp != 9 || result / 1000000000 == this.mant[ROUND_UP]) {
            if (this.ind == ROUND_DOWN) {
                return result;
            }
            return -result;
        } else if (result == DatatypeConstants.FIELD_UNDEFINED && this.ind == -1 && this.mant[ROUND_UP] == ROUND_CEILING) {
            return result;
        } else {
            throw new ArithmeticException("Conversion overflow: " + toString());
        }
    }

    public long longValue() {
        return toBigInteger().longValue();
    }

    public long longValueExact() {
        if (this.ind == null) {
            return 0;
        }
        int useexp;
        int lodigit = this.mant.length - 1;
        if (this.exp < 0) {
            int cstart;
            lodigit += this.exp;
            if (lodigit < 0) {
                cstart = ROUND_UP;
            } else {
                cstart = lodigit + ROUND_DOWN;
            }
            if (!allzero(this.mant, cstart)) {
                throw new ArithmeticException("Decimal part non-zero: " + toString());
            } else if (lodigit < 0) {
                return 0;
            } else {
                useexp = ROUND_UP;
            }
        } else if (this.exp + this.mant.length > 18) {
            throw new ArithmeticException("Conversion overflow: " + toString());
        } else {
            useexp = this.exp;
        }
        long result = 0;
        int $17 = lodigit + useexp;
        for (int i = ROUND_UP; i <= $17; i += ROUND_DOWN) {
            result *= 10;
            if (i <= lodigit) {
                result += (long) this.mant[i];
            }
        }
        if (lodigit + useexp != 18 || result / 1000000000000000000L == ((long) this.mant[ROUND_UP])) {
            if (this.ind == ROUND_DOWN) {
                return result;
            }
            return -result;
        } else if (result == Long.MIN_VALUE && this.ind == -1 && this.mant[ROUND_UP] == 9) {
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
            return ROUND_UP;
        }
        return -this.exp;
    }

    public BigDecimal setScale(int scale) {
        return setScale(scale, ROUND_UNNECESSARY);
    }

    public BigDecimal setScale(int scale, int round) {
        int ourscale = scale();
        if (ourscale == scale && this.form == null) {
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
                res.mant = extend(res.mant, res.mant.length + ROUND_DOWN);
                res.exp--;
            }
        }
        res.form = iszero;
        return res;
    }

    public short shortValueExact() {
        int i;
        int i2 = ROUND_DOWN;
        int num = intValueExact();
        if (num > 32767) {
            i = ROUND_DOWN;
        } else {
            i = ROUND_UP;
        }
        if (num >= -32768) {
            i2 = ROUND_UP;
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
        int i2 = ROUND_DOWN;
        if (this.exp >= 0) {
            i = ROUND_DOWN;
        } else {
            i = ROUND_UP;
        }
        if (this.form != null) {
            i2 = ROUND_UP;
        }
        if ((i & i2) != 0) {
            res = this;
        } else if (this.exp >= 0) {
            res = clone(this);
            res.form = iszero;
        } else if ((-this.exp) >= this.mant.length) {
            res = ZERO;
        } else {
            res = clone(this);
            int newlen = res.mant.length + res.exp;
            byte[] newmant = new byte[newlen];
            System.arraycopy(res.mant, ROUND_UP, newmant, ROUND_UP, newlen);
            res.mant = newmant;
            res.form = iszero;
            res.exp = ROUND_UP;
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
            res.exp = ROUND_UP;
        }
        return res.toBigInteger();
    }

    public static BigDecimal valueOf(double dub) {
        return new BigDecimal(new Double(dub).toString());
    }

    public static BigDecimal valueOf(long lint) {
        return valueOf(lint, ROUND_UP);
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
        int i = ROUND_UP;
        while ($18 > 0) {
            cmant[i] = (char) (this.mant[i] + 48);
            $18--;
            i += ROUND_DOWN;
        }
        byte b;
        int length;
        char[] rec;
        if (this.form != null) {
            StringBuilder sb = new StringBuilder(cmant.length + 15);
            b = this.ind;
            if (r0 == -1) {
                sb.append('-');
            }
            int euse = (this.exp + cmant.length) - 1;
            b = this.form;
            if (r0 == ROUND_DOWN) {
                sb.append(cmant[ROUND_UP]);
                length = cmant.length;
                if (r0 > ROUND_DOWN) {
                    sb.append('.').append(cmant, ROUND_DOWN, cmant.length - 1);
                }
            } else {
                int sig = euse % ROUND_FLOOR;
                if (sig < 0) {
                    sig += ROUND_FLOOR;
                }
                euse -= sig;
                sig += ROUND_DOWN;
                if (sig >= cmant.length) {
                    sb.append(cmant, ROUND_UP, cmant.length);
                    for (int $19 = sig - cmant.length; $19 > 0; $19--) {
                        sb.append('0');
                    }
                } else {
                    sb.append(cmant, ROUND_UP, sig).append('.').append(cmant, sig, cmant.length - sig);
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
                sb.getChars(ROUND_UP, srcEnd, rec, ROUND_UP);
            }
            return rec;
        } else if (this.exp != 0) {
            b = this.ind;
            int needsign = r0 == -1 ? ROUND_DOWN : ROUND_UP;
            int mag = this.exp + cmant.length;
            if (mag < ROUND_DOWN) {
                rec = new char[((needsign + ROUND_CEILING) - this.exp)];
                if (needsign != 0) {
                    rec[ROUND_UP] = '-';
                }
                rec[needsign] = '0';
                rec[needsign + ROUND_DOWN] = '.';
                int $20 = -mag;
                i = needsign + ROUND_CEILING;
                while ($20 > 0) {
                    rec[i] = '0';
                    $20--;
                    i += ROUND_DOWN;
                }
                System.arraycopy(cmant, ROUND_UP, rec, (needsign + ROUND_CEILING) - mag, cmant.length);
                return rec;
            }
            length = cmant.length;
            if (mag > r0) {
                rec = new char[(needsign + mag)];
                if (needsign != 0) {
                    rec[ROUND_UP] = '-';
                }
                System.arraycopy(cmant, ROUND_UP, rec, needsign, cmant.length);
                int $21 = mag - cmant.length;
                i = needsign + cmant.length;
                while ($21 > 0) {
                    rec[i] = '0';
                    $21--;
                    i += ROUND_DOWN;
                }
                return rec;
            }
            rec = new char[((needsign + ROUND_DOWN) + cmant.length)];
            if (needsign != 0) {
                rec[ROUND_UP] = '-';
            }
            System.arraycopy(cmant, ROUND_UP, rec, needsign, mag);
            rec[needsign + mag] = '.';
            System.arraycopy(cmant, mag, rec, (needsign + mag) + ROUND_DOWN, cmant.length - mag);
            return rec;
        } else if (this.ind >= null) {
            return cmant;
        } else {
            rec = new char[(cmant.length + ROUND_DOWN)];
            rec[ROUND_UP] = '-';
            System.arraycopy(cmant, ROUND_UP, rec, ROUND_DOWN, cmant.length);
            return rec;
        }
    }

    private int intcheck(int min, int max) {
        int i;
        int i2 = ROUND_DOWN;
        int i3 = intValueExact();
        if (i3 < min) {
            i = ROUND_DOWN;
        } else {
            i = ROUND_UP;
        }
        if (i3 <= max) {
            i2 = ROUND_UP;
        }
        if ((i2 | i) == 0) {
            return i3;
        }
        throw new ArithmeticException("Conversion overflow: " + i3);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private BigDecimal dodivide(char code, BigDecimal rhs, MathContext set, int scale) {
        if (set.lostDigits) {
            checkdigits(rhs, set.digits);
        }
        BigDecimal lhs = this;
        if (rhs.ind == null) {
            throw new ArithmeticException("Divide by 0");
        } else if (this.ind != null) {
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
                res.mant = new byte[(reqdig + ROUND_DOWN)];
                int newlen = (reqdig + reqdig) + ROUND_DOWN;
                byte[] var1 = extend(lhs.mant, newlen);
                int var1len = newlen;
                byte[] var2 = rhs.mant;
                int var2len = newlen;
                int b2b = (var2[ROUND_UP] * 10) + ROUND_DOWN;
                if (var2.length > ROUND_DOWN) {
                    b2b += var2[ROUND_DOWN];
                }
                int have = ROUND_UP;
                loop0:
                while (true) {
                    int thisdigit = ROUND_UP;
                    while (var1len >= var2len) {
                        int ba;
                        if (var1len == var2len) {
                            int $22 = var1len;
                            i = ROUND_UP;
                            while ($22 > 0) {
                                byte b;
                                if (i < var2.length) {
                                    b = var2[i];
                                } else {
                                    b = iszero;
                                }
                                if (var1[i] < b) {
                                    break;
                                } else if (var1[i] > b) {
                                    ba = var1[ROUND_UP];
                                } else {
                                    $22--;
                                    i += ROUND_DOWN;
                                }
                            }
                            break loop0;
                        }
                        ba = var1[ROUND_UP] * 10;
                        if (var1len > ROUND_DOWN) {
                            ba += var1[ROUND_DOWN];
                        }
                        int mult = (ba * 10) / b2b;
                        if (mult == 0) {
                            mult = ROUND_DOWN;
                        }
                        thisdigit += mult;
                        var1 = byteaddsub(var1, var1len, var2, var2len, -mult, true);
                        if (var1[ROUND_UP] == null) {
                            int $23 = var1len - 2;
                            int start = ROUND_UP;
                            while (start <= $23 && var1[start] == null) {
                                var1len--;
                                start += ROUND_DOWN;
                            }
                            if (start != 0) {
                                System.arraycopy(var1, start, var1, ROUND_UP, var1len);
                            }
                        }
                    }
                    if (((thisdigit != 0 ? ROUND_DOWN : ROUND_UP) | (have != 0 ? ROUND_DOWN : ROUND_UP)) != 0) {
                        res.mant[have] = (byte) thisdigit;
                        have += ROUND_DOWN;
                        if (have != reqdig + ROUND_DOWN) {
                            if (var1[ROUND_UP] == null) {
                                break;
                            }
                        }
                        break;
                    }
                    if ((scale < 0 || (-res.exp) <= scale) && (code == 'D' || res.exp > 0)) {
                        res.exp--;
                        var2len--;
                    }
                }
                if (have == 0) {
                    have = ROUND_DOWN;
                }
                if (((code == 'R' ? ROUND_DOWN : ROUND_UP) | (code == 'I' ? ROUND_DOWN : ROUND_UP)) != 0) {
                    if (res.exp + have > reqdig) {
                        throw new ArithmeticException("Integer overflow");
                    } else if (code == 'R') {
                        if (res.mant[ROUND_UP] == null) {
                            return clone(lhs).finish(set, false);
                        }
                        if (var1[ROUND_UP] == null) {
                            return ZERO;
                        }
                        res.ind = lhs.ind;
                        res.exp = (res.exp - (((reqdig + reqdig) + ROUND_DOWN) - lhs.mant.length)) + lhs.exp;
                        int d = var1len;
                        i = var1len - 1;
                        while (i >= ROUND_DOWN) {
                            int i2 = res.exp >= lhs.exp ? ROUND_DOWN : ROUND_UP;
                            int i3 = res.exp;
                            int i4 = rhs.exp;
                            if ((i2 | (i3 >= r0 ? ROUND_DOWN : ROUND_UP)) == 0 && var1[i] == null) {
                                d--;
                                res.exp += ROUND_DOWN;
                                i--;
                            }
                        }
                        if (d < var1.length) {
                            Object newvar1 = new byte[d];
                            System.arraycopy(var1, ROUND_UP, newvar1, ROUND_UP, d);
                            var1 = newvar1;
                        }
                        res.mant = var1;
                        return res.finish(set, false);
                    }
                } else if (var1[ROUND_UP] != null) {
                    byte lasthave = res.mant[have - 1];
                    if (lasthave % ROUND_HALF_DOWN == 0) {
                        res.mant[have - 1] = (byte) (lasthave + ROUND_DOWN);
                    }
                }
                if (scale >= 0) {
                    if (have != res.mant.length) {
                        res.exp -= res.mant.length - have;
                    }
                    res.round(res.mant.length - ((-res.exp) - scale), set.roundingMode);
                    if (res.exp != (-scale)) {
                        res.mant = extend(res.mant, res.mant.length + ROUND_DOWN);
                        res.exp--;
                    }
                    return res.finish(set, true);
                }
                if (have == res.mant.length) {
                    res.round(set);
                    have = reqdig;
                } else if (res.mant[ROUND_UP] == null) {
                    return ZERO;
                } else {
                    Object newmant = new byte[have];
                    System.arraycopy(res.mant, ROUND_UP, newmant, ROUND_UP, have);
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
        System.arraycopy(inarr, ROUND_UP, newarr, ROUND_UP, inarr.length);
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
        if (reuse && maxarr + ROUND_DOWN == alength) {
            reb = a;
        }
        if (reb == null) {
            reb = new byte[(maxarr + ROUND_DOWN)];
        }
        boolean quickm = false;
        if (m == ROUND_DOWN) {
            quickm = true;
        } else if (m == -1) {
            quickm = true;
        }
        int digit = ROUND_UP;
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
                digit = ROUND_UP;
            }
        }
        if (digit == 0) {
            return reb;
        }
        byte[] newarr = null;
        if (reuse && maxarr + ROUND_CEILING == a.length) {
            newarr = a;
        }
        if (newarr == null) {
            newarr = new byte[(maxarr + ROUND_CEILING)];
        }
        newarr[ROUND_UP] = (byte) digit;
        if (maxarr < 10) {
            int $24 = maxarr + ROUND_DOWN;
            int i = ROUND_UP;
            while ($24 > 0) {
                newarr[i + ROUND_DOWN] = reb[i];
                $24--;
                i += ROUND_DOWN;
            }
        } else {
            System.arraycopy(reb, ROUND_UP, newarr, ROUND_DOWN, maxarr + ROUND_DOWN);
        }
        return newarr;
    }

    private static final byte[] diginit() {
        byte[] work = new byte[Opcodes.OP_DIV_LONG_2ADDR];
        for (int op = ROUND_UP; op <= Opcodes.OP_MUL_LONG_2ADDR; op += ROUND_DOWN) {
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
            System.arraycopy(oldmant, ROUND_UP, this.mant, ROUND_UP, len);
            reuse = true;
            first = oldmant[len];
        } else {
            this.mant = ZERO.mant;
            this.ind = iszero;
            reuse = false;
            if (len == 0) {
                first = oldmant[ROUND_UP];
            } else {
                first = iszero;
            }
        }
        int increment = ROUND_UP;
        if (mode == ROUND_HALF_UP) {
            if (first >= (byte) 5) {
                increment = sign;
            }
        } else if (mode == ROUND_UNNECESSARY) {
            if (!allzero(oldmant, len)) {
                throw new ArithmeticException("Rounding necessary");
            }
        } else if (mode == ROUND_HALF_DOWN) {
            if (first > (byte) 5) {
                increment = sign;
            } else if (first == (byte) 5 && !allzero(oldmant, len + ROUND_DOWN)) {
                increment = sign;
            }
        } else if (mode == ROUND_HALF_EVEN) {
            if (first > (byte) 5) {
                increment = sign;
            } else if (first == (byte) 5) {
                if (!allzero(oldmant, len + ROUND_DOWN)) {
                    increment = sign;
                } else if (this.mant[this.mant.length - 1] % ROUND_CEILING != 0) {
                    increment = sign;
                }
            }
        } else if (mode != ROUND_DOWN) {
            if (mode == 0) {
                if (!allzero(oldmant, len)) {
                    increment = sign;
                }
            } else if (mode == ROUND_CEILING) {
                if (sign > 0 && !allzero(oldmant, len)) {
                    increment = sign;
                }
            } else if (mode != ROUND_FLOOR) {
                throw new IllegalArgumentException("Bad round value: " + mode);
            } else if (sign < 0 && !allzero(oldmant, len)) {
                increment = sign;
            }
        }
        if (increment != 0) {
            if (this.ind == null) {
                this.mant = ONE.mant;
                this.ind = (byte) increment;
            } else {
                if (this.ind == -1) {
                    increment = -increment;
                }
                byte[] newmant = byteaddsub(this.mant, this.mant.length, ONE.mant, ROUND_DOWN, increment, reuse);
                if (newmant.length > this.mant.length) {
                    this.exp += ROUND_DOWN;
                    System.arraycopy(newmant, ROUND_UP, this.mant, ROUND_UP, this.mant.length);
                } else {
                    this.mant = newmant;
                }
            }
        }
        if (this.exp <= MaxExp) {
            return this;
        }
        throw new ArithmeticException("Exponent Overflow: " + this.exp);
    }

    private static final boolean allzero(byte[] array, int start) {
        if (start < 0) {
            start = ROUND_UP;
        }
        int $25 = array.length - 1;
        for (int i = start; i <= $25; i += ROUND_DOWN) {
            if (array[i] != null) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private BigDecimal finish(MathContext set, boolean strip) {
        int i;
        int i2 = ROUND_DOWN;
        if (set.digits != 0 && this.mant.length > set.digits) {
            round(set);
        }
        if (strip && set.form != 0) {
            int d = this.mant.length;
            i = d - 1;
            while (i >= ROUND_DOWN && this.mant[i] == null) {
                d--;
                this.exp += ROUND_DOWN;
                i--;
            }
            if (d < this.mant.length) {
                byte[] newmant = new byte[d];
                System.arraycopy(this.mant, ROUND_UP, newmant, ROUND_UP, d);
                this.mant = newmant;
            }
        }
        this.form = iszero;
        int $26 = this.mant.length;
        i = ROUND_UP;
        while ($26 > 0) {
            if (this.mant[i] != null) {
                if (i > 0) {
                    newmant = new byte[(this.mant.length - i)];
                    System.arraycopy(this.mant, i, newmant, ROUND_UP, this.mant.length - i);
                    this.mant = newmant;
                }
                int mag = this.exp + this.mant.length;
                if (mag > 0) {
                    if (mag > set.digits && set.digits != 0) {
                        this.form = (byte) set.form;
                    }
                    if (mag - 1 <= MaxExp) {
                        return this;
                    }
                } else if (mag < -5) {
                    this.form = (byte) set.form;
                }
                mag--;
                int i3 = mag < MinExp ? ROUND_DOWN : ROUND_UP;
                if (mag <= MaxExp) {
                    i2 = ROUND_UP;
                }
                if ((i2 | i3) != 0) {
                    if (this.form == ROUND_CEILING) {
                        int sig = mag % ROUND_FLOOR;
                        if (sig < 0) {
                            sig += ROUND_FLOOR;
                        }
                        mag -= sig;
                        if (mag >= MinExp) {
                        }
                    }
                    throw new ArithmeticException("Exponent Overflow: " + mag);
                }
                return this;
            }
            $26--;
            i += ROUND_DOWN;
        }
        this.ind = iszero;
        if (set.form != 0) {
            this.exp = ROUND_UP;
        } else if (this.exp > 0) {
            this.exp = ROUND_UP;
        } else if (this.exp < MinExp) {
            throw new ArithmeticException("Exponent Overflow: " + this.exp);
        }
        this.mant = ZERO.mant;
        return this;
    }
}
