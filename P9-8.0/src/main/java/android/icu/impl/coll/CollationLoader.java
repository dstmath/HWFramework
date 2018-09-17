package android.icu.impl.coll;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceBundle.OpenType;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.util.MissingResourceException;

public final class CollationLoader {
    private static volatile String rootRules = null;

    private static final class ASCII {
        private ASCII() {
        }

        static String toLowerCase(String s) {
            int i = 0;
            while (i < s.length()) {
                char c = s.charAt(i);
                if ('A' > c || c > 'Z') {
                    i++;
                } else {
                    StringBuilder sb = new StringBuilder(s.length());
                    sb.append(s, 0, i).append((char) (c + 32));
                    while (true) {
                        i++;
                        if (i >= s.length()) {
                            return sb.toString();
                        }
                        c = s.charAt(i);
                        if ('A' <= c && c <= 'Z') {
                            c = (char) (c + 32);
                        }
                        sb.append(c);
                    }
                }
            }
            return s;
        }
    }

    private CollationLoader() {
    }

    private static void loadRootRules() {
        if (rootRules == null) {
            synchronized (CollationLoader.class) {
                if (rootRules == null) {
                    rootRules = UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, ULocale.ROOT).getString("UCARules");
                }
            }
        }
    }

    public static String getRootRules() {
        loadRootRules();
        return rootRules;
    }

    static String loadRules(ULocale locale, String collationType) {
        return ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, locale)).getWithFallback("collations/" + ASCII.toLowerCase(collationType)).getString("Sequence");
    }

    private static final UResourceBundle findWithFallback(UResourceBundle table, String entryName) {
        return ((ICUResourceBundle) table).findWithFallback(entryName);
    }

    public static CollationTailoring loadTailoring(ULocale locale, Output<ULocale> outValidLocale) {
        CollationTailoring root = CollationRoot.getRoot();
        String localeName = locale.getName();
        if (localeName.length() == 0 || localeName.equals("root")) {
            outValidLocale.value = ULocale.ROOT;
            return root;
        }
        try {
            UResourceBundle bundle = ICUResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, locale, OpenType.LOCALE_ROOT);
            ULocale validLocale = bundle.getULocale();
            String validLocaleName = validLocale.getName();
            if (validLocaleName.length() == 0 || validLocaleName.equals("root")) {
                validLocale = ULocale.ROOT;
            }
            outValidLocale.value = validLocale;
            try {
                UResourceBundle collations = bundle.get("collations");
                if (collations == null) {
                    return root;
                }
                String type = locale.getKeywordValue("collation");
                String defaultType = "standard";
                String defT = ((ICUResourceBundle) collations).findStringWithFallback("default");
                if (defT != null) {
                    defaultType = defT;
                }
                if (type == null || type.equals("default")) {
                    type = defaultType;
                } else {
                    type = ASCII.toLowerCase(type);
                }
                UResourceBundle data = findWithFallback(collations, type);
                if (data == null && type.length() > 6 && type.startsWith("search")) {
                    type = "search";
                    data = findWithFallback(collations, type);
                }
                if (data == null && (type.equals(defaultType) ^ 1) != 0) {
                    type = defaultType;
                    data = findWithFallback(collations, type);
                }
                if (data == null && (type.equals("standard") ^ 1) != 0) {
                    type = "standard";
                    data = findWithFallback(collations, type);
                }
                if (data == null) {
                    return root;
                }
                ULocale actualLocale = data.getULocale();
                String actualLocaleName = actualLocale.getName();
                if (actualLocaleName.length() == 0 || actualLocaleName.equals("root")) {
                    actualLocale = ULocale.ROOT;
                    if (type.equals("standard")) {
                        return root;
                    }
                }
                CollationTailoring collationTailoring = new CollationTailoring(root.settings);
                collationTailoring.actualLocale = actualLocale;
                try {
                    CollationDataReader.read(root, data.get("%%CollationBin").getBinary(), collationTailoring);
                    try {
                        collationTailoring.setRulesResource(data.get("Sequence"));
                    } catch (MissingResourceException e) {
                    }
                    if (!type.equals(defaultType)) {
                        outValidLocale.value = validLocale.setKeywordValue("collation", type);
                    }
                    if (!actualLocale.equals(validLocale)) {
                        defT = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, actualLocale)).findStringWithFallback("collations/default");
                        if (defT != null) {
                            defaultType = defT;
                        }
                    }
                    if (!type.equals(defaultType)) {
                        collationTailoring.actualLocale = collationTailoring.actualLocale.setKeywordValue("collation", type);
                    }
                    return collationTailoring;
                } catch (IOException e2) {
                    throw new ICUUncheckedIOException("Failed to load collation tailoring data for locale:" + actualLocale + " type:" + type, e2);
                }
            } catch (MissingResourceException e3) {
                return root;
            }
        } catch (MissingResourceException e4) {
            outValidLocale.value = ULocale.ROOT;
            return root;
        }
    }
}
