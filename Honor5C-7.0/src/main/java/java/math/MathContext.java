package java.math;

import android.icu.text.PluralRules;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

public final class MathContext implements Serializable {
    public static final MathContext DECIMAL128 = null;
    public static final MathContext DECIMAL32 = null;
    public static final MathContext DECIMAL64 = null;
    public static final MathContext UNLIMITED = null;
    private static final long serialVersionUID = 5579720004786848255L;
    private final int precision;
    private final RoundingMode roundingMode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.math.MathContext.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.math.MathContext.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.math.MathContext.<clinit>():void");
    }

    public MathContext(int precision) {
        this(precision, RoundingMode.HALF_UP);
    }

    public MathContext(int precision, RoundingMode roundingMode) {
        this.precision = precision;
        this.roundingMode = roundingMode;
        checkValid();
    }

    public MathContext(String s) {
        int precisionLength = "precision=".length();
        int roundingModeLength = "roundingMode=".length();
        if (s.startsWith("precision=")) {
            int spaceIndex = s.indexOf(32, precisionLength);
            if (spaceIndex != -1) {
                try {
                    this.precision = Integer.parseInt(s.substring(precisionLength, spaceIndex));
                    int roundingModeStart = spaceIndex + 1;
                    if (s.regionMatches(roundingModeStart, "roundingMode=", 0, roundingModeLength)) {
                        this.roundingMode = RoundingMode.valueOf(s.substring(roundingModeStart + roundingModeLength));
                        checkValid();
                        return;
                    }
                    throw invalidMathContext("Missing rounding mode", s);
                } catch (NumberFormatException e) {
                    throw invalidMathContext("Bad precision", s);
                }
            }
        }
        throw invalidMathContext("Missing precision", s);
    }

    private IllegalArgumentException invalidMathContext(String reason, String s) {
        throw new IllegalArgumentException(reason + PluralRules.KEYWORD_RULE_SEPARATOR + s);
    }

    private void checkValid() {
        if (this.precision < 0) {
            throw new IllegalArgumentException("Negative precision: " + this.precision);
        } else if (this.roundingMode == null) {
            throw new NullPointerException("roundingMode == null");
        }
    }

    public int getPrecision() {
        return this.precision;
    }

    public RoundingMode getRoundingMode() {
        return this.roundingMode;
    }

    public boolean equals(Object x) {
        if (!(x instanceof MathContext) || ((MathContext) x).getPrecision() != this.precision) {
            return false;
        }
        if (((MathContext) x).getRoundingMode() == this.roundingMode) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.precision << 3) | this.roundingMode.ordinal();
    }

    public String toString() {
        return "precision=" + this.precision + " roundingMode=" + this.roundingMode;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        try {
            checkValid();
        } catch (Exception ex) {
            throw new StreamCorruptedException(ex.getMessage());
        }
    }
}
