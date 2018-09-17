package android.icu.math;

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
    private static final int[] ROUNDS = new int[]{4, 7, 2, 1, 3, 5, 6, 0};
    private static final String[] ROUNDWORDS = new String[]{"ROUND_HALF_UP", "ROUND_UNNECESSARY", "ROUND_CEILING", "ROUND_DOWN", "ROUND_FLOOR", "ROUND_HALF_DOWN", "ROUND_HALF_EVEN", "ROUND_UP"};
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

    public MathContext(int setdigits) {
        this(setdigits, 1, false, 4);
    }

    public MathContext(int setdigits, int setform) {
        this(setdigits, setform, false, 4);
    }

    public MathContext(int setdigits, int setform, boolean setlostdigits) {
        this(setdigits, setform, setlostdigits, 4);
    }

    public MathContext(int setdigits, int setform, boolean setlostdigits, int setroundingmode) {
        if (setdigits != 9) {
            if (setdigits < 0) {
                throw new IllegalArgumentException("Digits too small: " + setdigits);
            } else if (setdigits > MAX_DIGITS) {
                throw new IllegalArgumentException("Digits too large: " + setdigits);
            }
        }
        if (setform != 1 && setform != 2 && setform != 0) {
            throw new IllegalArgumentException("Bad form value: " + setform);
        } else if (isValidRound(setroundingmode)) {
            this.digits = setdigits;
            this.form = setform;
            this.lostDigits = setlostdigits;
            this.roundingMode = setroundingmode;
        } else {
            throw new IllegalArgumentException("Bad roundingMode value: " + setroundingmode);
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

    public String toString() {
        String formstr;
        String str;
        String roundword = null;
        if (this.form == 1) {
            formstr = "SCIENTIFIC";
        } else if (this.form == 2) {
            formstr = "ENGINEERING";
        } else {
            formstr = "PLAIN";
        }
        int $1 = ROUNDS.length;
        int r = 0;
        while ($1 > 0) {
            if (this.roundingMode == ROUNDS[r]) {
                roundword = ROUNDWORDS[r];
                break;
            }
            $1--;
            r++;
        }
        StringBuilder append = new StringBuilder().append("digits=").append(this.digits).append(" ").append("form=").append(formstr).append(" ").append("lostDigits=");
        if (this.lostDigits) {
            str = "1";
        } else {
            str = AndroidHardcodedSystemProperties.JAVA_VERSION;
        }
        return append.append(str).append(" ").append("roundingMode=").append(roundword).toString();
    }

    private static boolean isValidRound(int testround) {
        int $2 = ROUNDS.length;
        int r = 0;
        while ($2 > 0) {
            if (testround == ROUNDS[r]) {
                return true;
            }
            $2--;
            r++;
        }
        return false;
    }
}
