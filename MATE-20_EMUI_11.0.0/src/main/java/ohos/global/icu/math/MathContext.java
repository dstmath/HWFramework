package ohos.global.icu.math;

import java.io.Serializable;

public final class MathContext implements Serializable {
    public static final MathContext DEFAULT = new MathContext(9, 1, false, 4);
    private static final int DEFAULT_DIGITS = 9;
    private static final int DEFAULT_FORM = 1;
    private static final boolean DEFAULT_LOSTDIGITS = false;
    private static final int DEFAULT_ROUNDINGMODE = 4;
    public static final int ENGINEERING = 2;
    private static final int MAX_DIGITS = 999999999;
    private static final int MIN_DIGITS = 0;
    public static final int PLAIN = 0;
    private static final int[] ROUNDS = {4, 7, 2, 1, 3, 5, 6, 0};
    private static final String[] ROUNDWORDS = {"ROUND_HALF_UP", "ROUND_UNNECESSARY", "ROUND_CEILING", "ROUND_DOWN", "ROUND_FLOOR", "ROUND_HALF_DOWN", "ROUND_HALF_EVEN", "ROUND_UP"};
    public static final int ROUND_CEILING = 2;
    public static final int ROUND_DOWN = 1;
    public static final int ROUND_FLOOR = 3;
    public static final int ROUND_HALF_DOWN = 5;
    public static final int ROUND_HALF_EVEN = 6;
    public static final int ROUND_HALF_UP = 4;
    public static final int ROUND_UNNECESSARY = 7;
    public static final int ROUND_UP = 0;
    public static final int SCIENTIFIC = 1;
    private static final long serialVersionUID = 7163376998892515376L;
    int digits;
    int form;
    boolean lostDigits;
    int roundingMode;

    public MathContext(int i) {
        this(i, 1, false, 4);
    }

    public MathContext(int i, int i2) {
        this(i, i2, false, 4);
    }

    public MathContext(int i, int i2, boolean z) {
        this(i, i2, z, 4);
    }

    public MathContext(int i, int i2, boolean z, int i3) {
        if (i != 9) {
            if (i < 0) {
                throw new IllegalArgumentException("Digits too small: " + i);
            } else if (i > MAX_DIGITS) {
                throw new IllegalArgumentException("Digits too large: " + i);
            }
        }
        if (i2 != 1 && i2 != 2 && i2 != 0) {
            throw new IllegalArgumentException("Bad form value: " + i2);
        } else if (isValidRound(i3)) {
            this.digits = i;
            this.form = i2;
            this.lostDigits = z;
            this.roundingMode = i3;
        } else {
            throw new IllegalArgumentException("Bad roundingMode value: " + i3);
        }
    }

    public int getDigits() {
        return this.digits;
    }

    public int getForm() {
        return this.form;
    }

    public boolean getLostDigits() {
        return this.lostDigits;
    }

    public int getRoundingMode() {
        return this.roundingMode;
    }

    @Override // java.lang.Object
    public String toString() {
        String str;
        int i = this.form;
        String str2 = i == 1 ? "SCIENTIFIC" : i == 2 ? "ENGINEERING" : "PLAIN";
        int length = ROUNDS.length;
        int i2 = 0;
        while (true) {
            if (length <= 0) {
                str = null;
                break;
            } else if (this.roundingMode == ROUNDS[i2]) {
                str = ROUNDWORDS[i2];
                break;
            } else {
                length--;
                i2++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("digits=");
        sb.append(this.digits);
        sb.append(" form=");
        sb.append(str2);
        sb.append(" lostDigits=");
        sb.append(this.lostDigits ? "1" : "0");
        sb.append(" roundingMode=");
        sb.append(str);
        return sb.toString();
    }

    private static boolean isValidRound(int i) {
        int length = ROUNDS.length;
        int i2 = 0;
        while (length > 0) {
            if (i == ROUNDS[i2]) {
                return true;
            }
            length--;
            i2++;
        }
        return false;
    }
}
