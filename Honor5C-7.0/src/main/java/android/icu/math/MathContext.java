package android.icu.math;

import java.io.Serializable;

public final class MathContext implements Serializable {
    public static final MathContext DEFAULT = null;
    private static final int DEFAULT_DIGITS = 9;
    private static final int DEFAULT_FORM = 1;
    private static final boolean DEFAULT_LOSTDIGITS = false;
    private static final int DEFAULT_ROUNDINGMODE = 4;
    public static final int ENGINEERING = 2;
    private static final int MAX_DIGITS = 999999999;
    private static final int MIN_DIGITS = 0;
    public static final int PLAIN = 0;
    private static final int[] ROUNDS = null;
    private static final String[] ROUNDWORDS = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.math.MathContext.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.math.MathContext.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.math.MathContext.<clinit>():void");
    }

    public MathContext(int setdigits) {
        this(setdigits, SCIENTIFIC, DEFAULT_LOSTDIGITS, ROUND_HALF_UP);
    }

    public MathContext(int setdigits, int setform) {
        this(setdigits, setform, DEFAULT_LOSTDIGITS, ROUND_HALF_UP);
    }

    public MathContext(int setdigits, int setform, boolean setlostdigits) {
        this(setdigits, setform, setlostdigits, ROUND_HALF_UP);
    }

    public MathContext(int setdigits, int setform, boolean setlostdigits, int setroundingmode) {
        if (setdigits != DEFAULT_DIGITS) {
            if (setdigits < 0) {
                throw new IllegalArgumentException("Digits too small: " + setdigits);
            } else if (setdigits > MAX_DIGITS) {
                throw new IllegalArgumentException("Digits too large: " + setdigits);
            }
        }
        if (setform != SCIENTIFIC && setform != ROUND_CEILING && setform != 0) {
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
        if (this.form == SCIENTIFIC) {
            formstr = "SCIENTIFIC";
        } else if (this.form == ROUND_CEILING) {
            formstr = "ENGINEERING";
        } else {
            formstr = "PLAIN";
        }
        int $1 = ROUNDS.length;
        int r = ROUND_UP;
        while ($1 > 0) {
            if (this.roundingMode == ROUNDS[r]) {
                roundword = ROUNDWORDS[r];
                break;
            }
            $1--;
            r += SCIENTIFIC;
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
        int r = ROUND_UP;
        while ($2 > 0) {
            if (testround == ROUNDS[r]) {
                return true;
            }
            $2--;
            r += SCIENTIFIC;
        }
        return DEFAULT_LOSTDIGITS;
    }
}
