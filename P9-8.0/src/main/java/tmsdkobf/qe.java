package tmsdkobf;

import android.telephony.PhoneNumberUtils;

public class qe extends PhoneNumberUtils {
    public static final String[] Lh = new String[]{"-", "+86", "12593", "17909", "17951", "17911", "10193", "12583", "12520", "96688"};

    public static String cA(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        Object obj = null;
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt == '+') {
                if (obj == null) {
                    obj = 1;
                } else {
                    continue;
                }
            }
            if (!isDialable(charAt)) {
                if (isStartsPostDial(charAt)) {
                    break;
                }
            } else {
                stringBuilder.append(charAt);
            }
        }
        return stringBuilder.toString();
    }

    public static String cv(String str) {
        if (str == null || str.length() <= 2) {
            return str;
        }
        for (String str2 : Lh) {
            if (str.startsWith(str2)) {
                return str.substring(str2.length());
            }
        }
        return str;
    }

    public static boolean cw(String str) {
        for (String startsWith : Lh) {
            if (str.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    public static String cx(String str) {
        return str == null ? str : stripSeparators(str).replace("-", "").replace(" ", "").trim();
    }

    public static boolean cy(String str) {
        for (int length = str.length() - 1; length >= 0; length--) {
            if (!isISODigit(str.charAt(length))) {
                return false;
            }
        }
        return true;
    }

    public static String cz(String str) {
        return i(cA(cv(str)), 8);
    }

    private static String i(String str, int i) {
        if (str == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(i);
        int length = str.length();
        int i2 = length - 1;
        int i3 = length;
        while (i2 >= 0 && length - i2 <= i) {
            stringBuilder.append(str.charAt(i2));
            i2--;
        }
        return stringBuilder.toString();
    }
}
