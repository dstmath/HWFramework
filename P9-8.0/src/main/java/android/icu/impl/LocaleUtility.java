package android.icu.impl;

import java.util.Locale;

public class LocaleUtility {
    public static Locale getLocaleFromName(String name) {
        String language = "";
        String country = "";
        String variant = "";
        int i1 = name.indexOf(95);
        if (i1 < 0) {
            language = name;
        } else {
            language = name.substring(0, i1);
            i1++;
            int i2 = name.indexOf(95, i1);
            if (i2 < 0) {
                country = name.substring(i1);
            } else {
                country = name.substring(i1, i2);
                variant = name.substring(i2 + 1);
            }
        }
        return new Locale(language, country, variant);
    }

    public static boolean isFallbackOf(String parent, String child) {
        boolean z = true;
        if (!child.startsWith(parent)) {
            return false;
        }
        int i = parent.length();
        if (!(i == child.length() || child.charAt(i) == '_')) {
            z = false;
        }
        return z;
    }

    public static boolean isFallbackOf(Locale parent, Locale child) {
        return isFallbackOf(parent.toString(), child.toString());
    }

    public static Locale fallback(Locale loc) {
        String[] parts = new String[]{loc.getLanguage(), loc.getCountry(), loc.getVariant()};
        int i = 2;
        while (i >= 0) {
            if (parts[i].length() != 0) {
                parts[i] = "";
                break;
            }
            i--;
        }
        if (i < 0) {
            return null;
        }
        return new Locale(parts[0], parts[1], parts[2]);
    }
}
