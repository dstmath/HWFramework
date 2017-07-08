package android.text.method;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.accessibility.AccessibilityNodeInfo;
import com.hisi.perfhub.PerfHub;

public class DigitsKeyListener extends NumberKeyListener {
    private static final char[][] CHARACTERS = null;
    private static final int DECIMAL = 2;
    private static final int SIGN = 1;
    private static DigitsKeyListener[] sInstance;
    private char[] mAccepted;
    private boolean mDecimal;
    private boolean mSign;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.DigitsKeyListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.method.DigitsKeyListener.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.DigitsKeyListener.<clinit>():void");
    }

    protected char[] getAcceptedChars() {
        return this.mAccepted;
    }

    private static boolean isSignChar(char c) {
        return c == '-' || c == '+';
    }

    private static boolean isDecimalPointChar(char c) {
        return c == '.';
    }

    public DigitsKeyListener() {
        this(false, false);
    }

    public DigitsKeyListener(boolean sign, boolean decimal) {
        int i;
        int i2 = 0;
        this.mSign = sign;
        this.mDecimal = decimal;
        if (sign) {
            i = SIGN;
        } else {
            i = 0;
        }
        if (decimal) {
            i2 = DECIMAL;
        }
        this.mAccepted = CHARACTERS[i | i2];
    }

    public static DigitsKeyListener getInstance() {
        return getInstance(false, false);
    }

    public static DigitsKeyListener getInstance(boolean sign, boolean decimal) {
        int i;
        int i2 = 0;
        if (sign) {
            i = SIGN;
        } else {
            i = 0;
        }
        if (decimal) {
            i2 = DECIMAL;
        }
        int kind = i | i2;
        if (sInstance[kind] != null) {
            return sInstance[kind];
        }
        sInstance[kind] = new DigitsKeyListener(sign, decimal);
        return sInstance[kind];
    }

    public static DigitsKeyListener getInstance(String accepted) {
        DigitsKeyListener dim = new DigitsKeyListener();
        dim.mAccepted = new char[accepted.length()];
        accepted.getChars(0, accepted.length(), dim.mAccepted, 0);
        return dim;
    }

    public int getInputType() {
        int contentType = DECIMAL;
        if (this.mSign) {
            contentType = PerfHub.PERF_EVENT_RESTART;
        }
        if (this.mDecimal) {
            return contentType | AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
        }
        return contentType;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);
        if (!this.mSign && !this.mDecimal) {
            return out;
        }
        int i;
        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }
        int sign = -1;
        int decimal = -1;
        int dlen = dest.length();
        for (i = 0; i < dstart; i += SIGN) {
            char c = dest.charAt(i);
            if (isSignChar(c)) {
                sign = i;
            } else if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        for (i = dend; i < dlen; i += SIGN) {
            c = dest.charAt(i);
            if (isSignChar(c)) {
                return "";
            }
            if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        CharSequence stripped = null;
        for (i = end - 1; i >= start; i--) {
            c = source.charAt(i);
            boolean strip = false;
            if (isSignChar(c)) {
                if (i != start || dstart != 0) {
                    strip = true;
                } else if (sign >= 0) {
                    strip = true;
                } else {
                    sign = i;
                }
            } else if (isDecimalPointChar(c)) {
                if (decimal >= 0) {
                    strip = true;
                } else {
                    decimal = i;
                }
            }
            if (strip) {
                if (end == start + SIGN) {
                    return "";
                }
                if (stripped == null) {
                    stripped = new SpannableStringBuilder(source, start, end);
                }
                stripped.delete(i - start, (i + SIGN) - start);
            }
        }
        if (stripped != null) {
            return stripped;
        }
        if (out != null) {
            return out;
        }
        return null;
    }
}
