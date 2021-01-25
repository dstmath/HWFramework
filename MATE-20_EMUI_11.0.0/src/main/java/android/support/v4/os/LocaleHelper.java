package android.support.v4.os;

import android.support.annotation.RestrictTo;
import java.util.Locale;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
final class LocaleHelper {
    static Locale forLanguageTag(String str) {
        if (str.contains("-")) {
            String[] args = str.split("-", -1);
            if (args.length > 2) {
                return new Locale(args[0], args[1], args[2]);
            }
            if (args.length > 1) {
                return new Locale(args[0], args[1]);
            }
            if (args.length == 1) {
                return new Locale(args[0]);
            }
        } else if (!str.contains("_")) {
            return new Locale(str);
        } else {
            String[] args2 = str.split("_", -1);
            if (args2.length > 2) {
                return new Locale(args2[0], args2[1], args2[2]);
            }
            if (args2.length > 1) {
                return new Locale(args2[0], args2[1]);
            }
            if (args2.length == 1) {
                return new Locale(args2[0]);
            }
        }
        throw new IllegalArgumentException("Can not parse language tag: [" + str + "]");
    }

    static String toLanguageTag(Locale locale) {
        StringBuilder buf = new StringBuilder();
        buf.append(locale.getLanguage());
        String country = locale.getCountry();
        if (country != null && !country.isEmpty()) {
            buf.append("-");
            buf.append(locale.getCountry());
        }
        return buf.toString();
    }

    private LocaleHelper() {
    }
}
