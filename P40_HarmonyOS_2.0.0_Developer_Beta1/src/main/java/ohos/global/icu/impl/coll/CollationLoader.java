package ohos.global.icu.impl.coll;

import java.io.IOException;
import java.util.MissingResourceException;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public final class CollationLoader {
    private static volatile String rootRules;

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

    private static final class ASCII {
        private ASCII() {
        }

        static String toLowerCase(String str) {
            int i = 0;
            while (i < str.length()) {
                char charAt = str.charAt(i);
                if ('A' > charAt || charAt > 'Z') {
                    i++;
                } else {
                    StringBuilder sb = new StringBuilder(str.length());
                    sb.append((CharSequence) str, 0, i);
                    sb.append((char) (charAt + ' '));
                    while (true) {
                        i++;
                        if (i >= str.length()) {
                            return sb.toString();
                        }
                        char charAt2 = str.charAt(i);
                        if ('A' <= charAt2 && charAt2 <= 'Z') {
                            charAt2 = (char) (charAt2 + ' ');
                        }
                        sb.append(charAt2);
                    }
                }
            }
            return str;
        }
    }

    static String loadRules(ULocale uLocale, String str) {
        return UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, uLocale).getWithFallback("collations/" + ASCII.toLowerCase(str)).getString("Sequence");
    }

    private static final UResourceBundle findWithFallback(UResourceBundle uResourceBundle, String str) {
        return ((ICUResourceBundle) uResourceBundle).findWithFallback(str);
    }

    public static CollationTailoring loadTailoring(ULocale uLocale, Output<ULocale> output) {
        String str;
        CollationTailoring root = CollationRoot.getRoot();
        String name = uLocale.getName();
        if (name.length() == 0 || name.equals(Constants.ELEMNAME_ROOT_STRING)) {
            output.value = ULocale.ROOT;
            return root;
        }
        try {
            ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, uLocale, ICUResourceBundle.OpenType.LOCALE_ROOT);
            ULocale uLocale2 = bundleInstance.getULocale();
            String name2 = uLocale2.getName();
            if (name2.length() == 0 || name2.equals(Constants.ELEMNAME_ROOT_STRING)) {
                uLocale2 = ULocale.ROOT;
            }
            output.value = uLocale2;
            try {
                ICUResourceBundle iCUResourceBundle = bundleInstance.get("collations");
                if (iCUResourceBundle == null) {
                    return root;
                }
                String keywordValue = uLocale.getKeywordValue("collation");
                String findStringWithFallback = iCUResourceBundle.findStringWithFallback("default");
                if (findStringWithFallback == null) {
                    findStringWithFallback = "standard";
                }
                String lowerCase = (keywordValue == null || keywordValue.equals("default")) ? findStringWithFallback : ASCII.toLowerCase(keywordValue);
                UResourceBundle findWithFallback = findWithFallback(iCUResourceBundle, lowerCase);
                if (findWithFallback == null && lowerCase.length() > 6 && lowerCase.startsWith("search")) {
                    findWithFallback = findWithFallback(iCUResourceBundle, "search");
                    lowerCase = "search";
                }
                if (findWithFallback == null && !lowerCase.equals(findStringWithFallback)) {
                    findWithFallback = findWithFallback(iCUResourceBundle, findStringWithFallback);
                    lowerCase = findStringWithFallback;
                }
                if (findWithFallback == null && !lowerCase.equals("standard")) {
                    findWithFallback = findWithFallback(iCUResourceBundle, "standard");
                    lowerCase = "standard";
                }
                if (findWithFallback == null) {
                    return root;
                }
                ULocale uLocale3 = findWithFallback.getULocale();
                String name3 = uLocale3.getName();
                if (name3.length() == 0 || name3.equals(Constants.ELEMNAME_ROOT_STRING)) {
                    uLocale3 = ULocale.ROOT;
                    if (lowerCase.equals("standard")) {
                        return root;
                    }
                }
                CollationTailoring collationTailoring = new CollationTailoring(root.settings);
                collationTailoring.actualLocale = uLocale3;
                try {
                    CollationDataReader.read(root, findWithFallback.get("%%CollationBin").getBinary(), collationTailoring);
                    try {
                        collationTailoring.setRulesResource(findWithFallback.get("Sequence"));
                    } catch (MissingResourceException unused) {
                    }
                    if (!lowerCase.equals(findStringWithFallback)) {
                        output.value = uLocale2.setKeywordValue("collation", lowerCase);
                    }
                    if (uLocale3.equals(uLocale2) || (str = UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, uLocale3).findStringWithFallback("collations/default")) == null) {
                        str = findStringWithFallback;
                    }
                    if (!lowerCase.equals(str)) {
                        collationTailoring.actualLocale = collationTailoring.actualLocale.setKeywordValue("collation", lowerCase);
                    }
                    return collationTailoring;
                } catch (IOException e) {
                    throw new ICUUncheckedIOException("Failed to load collation tailoring data for locale:" + uLocale3 + " type:" + lowerCase, e);
                }
            } catch (MissingResourceException unused2) {
                return root;
            }
        } catch (MissingResourceException unused3) {
            output.value = ULocale.ROOT;
            return root;
        }
    }
}
