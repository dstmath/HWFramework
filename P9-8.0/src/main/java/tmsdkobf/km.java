package tmsdkobf;

import android.telephony.PhoneNumberUtils;

public final class km {
    static final String[] tI = new String[]{"-", "+86", "0086", "12593", "17909", "17951", "17911", "10193", "12583", "12520", "96688"};

    public static String aW(String str) {
        if (str == null || str.length() <= 2) {
            return str;
        }
        for (String str2 : tI) {
            if (str.startsWith(str2)) {
                return str.substring(str2.length());
            }
        }
        return str;
    }

    public static String aX(String str) {
        return aW(stripSeparators(str));
    }

    public static String stripSeparators(String str) {
        return str == null ? str : PhoneNumberUtils.stripSeparators(str).replace("-", "").replace(" ", "").trim();
    }
}
