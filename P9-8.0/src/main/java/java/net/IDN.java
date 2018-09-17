package java.net;

import android.icu.text.IDNA;
import android.icu.text.StringPrepParseException;

public final class IDN {
    public static final int ALLOW_UNASSIGNED = 1;
    public static final int USE_STD3_ASCII_RULES = 2;

    private IDN() {
    }

    public static String toASCII(String input, int flag) {
        try {
            return IDNA.convertIDNToASCII(input, flag).toString();
        } catch (StringPrepParseException e) {
            throw new IllegalArgumentException("Invalid input to toASCII: " + input, e);
        }
    }

    public static String toASCII(String input) {
        return toASCII(input, 0);
    }

    public static String toUnicode(String input, int flag) {
        try {
            return convertFullStop(IDNA.convertIDNToUnicode(input, flag)).toString();
        } catch (StringPrepParseException e) {
            return input;
        }
    }

    private static boolean isLabelSeperator(char c) {
        return c == 12290 || c == 65294 || c == 65377;
    }

    private static StringBuffer convertFullStop(StringBuffer input) {
        for (int i = 0; i < input.length(); i++) {
            if (isLabelSeperator(input.charAt(i))) {
                input.setCharAt(i, '.');
            }
        }
        return input;
    }

    public static String toUnicode(String input) {
        return toUnicode(input, 0);
    }
}
