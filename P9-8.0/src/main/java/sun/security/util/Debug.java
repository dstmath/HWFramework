package sun.security.util;

import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.util.locale.LanguageTag;

public class Debug {
    private static final String args = null;
    private static final char[] hexDigits = "0123456789abcdef".toCharArray();
    private final String prefix;

    private Debug(String prefix) {
        this.prefix = prefix;
    }

    public static Debug getInstance(String option) {
        return getInstance(option, option);
    }

    public static Debug getInstance(String option, String prefix) {
        if (isOn(option)) {
            return new Debug(prefix);
        }
        return null;
    }

    public static boolean isOn(String option) {
        boolean z = true;
        if (args == null) {
            return false;
        }
        if (args.indexOf("all") != -1) {
            return true;
        }
        if (args.indexOf(option) == -1) {
            z = false;
        }
        return z;
    }

    public void println(String message) {
        System.err.println(this.prefix + ": " + message);
    }

    public void println() {
        System.err.println(this.prefix + ":");
    }

    public static String toHexString(BigInteger b) {
        String hexValue = b.toString(16);
        StringBuffer buf = new StringBuffer(hexValue.length() * 2);
        if (hexValue.startsWith(LanguageTag.SEP)) {
            buf.append("   -");
            hexValue = hexValue.substring(1);
        } else {
            buf.append("    ");
        }
        if (hexValue.length() % 2 != 0) {
            hexValue = "0" + hexValue;
        }
        int i = 0;
        while (i < hexValue.length()) {
            buf.append(hexValue.substring(i, i + 2));
            i += 2;
            if (i != hexValue.length()) {
                if (i % 64 == 0) {
                    buf.append("\n    ");
                } else if (i % 8 == 0) {
                    buf.append(" ");
                }
            }
        }
        return buf.toString();
    }

    private static String marshal(String args) {
        if (args == null) {
            return null;
        }
        StringBuffer target = new StringBuffer();
        String keyReg = "[Pp][Ee][Rr][Mm][Ii][Ss][Ss][Ii][Oo][Nn]=";
        String keyStr = "permission=";
        Matcher matcher = Pattern.compile(keyReg + "[a-zA-Z_$][a-zA-Z0-9_$]*([.][a-zA-Z_$][a-zA-Z0-9_$]*)*").matcher(new StringBuffer(args));
        StringBuffer left = new StringBuffer();
        while (matcher.find()) {
            target.append(matcher.group().replaceFirst(keyReg, keyStr));
            target.append("  ");
            matcher.appendReplacement(left, "");
        }
        matcher.appendTail(left);
        StringBuffer source = left;
        keyReg = "[Cc][Oo][Dd][Ee][Bb][Aa][Ss][Ee]=";
        keyStr = "codebase=";
        matcher = Pattern.compile(keyReg + "[^, ;]*").matcher(left);
        left = new StringBuffer();
        while (matcher.find()) {
            target.append(matcher.group().replaceFirst(keyReg, keyStr));
            target.append("  ");
            matcher.appendReplacement(left, "");
        }
        matcher.appendTail(left);
        source = left;
        target.append(left.toString().toLowerCase(Locale.ENGLISH));
        return target.toString();
    }

    public static String toString(byte[] b) {
        if (b == null) {
            return "(null)";
        }
        StringBuilder sb = new StringBuilder(b.length * 3);
        for (int i = 0; i < b.length; i++) {
            int k = b[i] & 255;
            if (i != 0) {
                sb.append(':');
            }
            sb.append(hexDigits[k >>> 4]);
            sb.append(hexDigits[k & 15]);
        }
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}
