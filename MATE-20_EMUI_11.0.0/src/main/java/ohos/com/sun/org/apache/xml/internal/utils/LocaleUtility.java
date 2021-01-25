package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.Locale;

public class LocaleUtility {
    public static final String EMPTY_STRING = "";
    public static final char IETF_SEPARATOR = '-';

    public static Locale langToLocale(String str) {
        String str2;
        String str3;
        String str4;
        if (str != null) {
            String str5 = "";
            if (!str.equals(str5)) {
                int indexOf = str.indexOf(45);
                if (indexOf < 0) {
                    str3 = str;
                    str2 = str5;
                    str4 = str2;
                } else {
                    str3 = str.substring(0, indexOf);
                    int i = indexOf + 1;
                    int indexOf2 = str.indexOf(45, i);
                    if (indexOf2 < 0) {
                        str2 = str.substring(i);
                        str4 = str5;
                    } else {
                        String substring = str.substring(i, indexOf2);
                        str4 = str.substring(indexOf2 + 1);
                        str2 = substring;
                    }
                }
                String lowerCase = str3.length() == 2 ? str3.toLowerCase() : str5;
                String upperCase = str2.length() == 2 ? str2.toUpperCase() : str5;
                if (str4.length() > 0 && (lowerCase.length() == 2 || upperCase.length() == 2)) {
                    str5 = str4.toUpperCase();
                }
                return new Locale(lowerCase, upperCase, str5);
            }
        }
        return Locale.getDefault();
    }
}
