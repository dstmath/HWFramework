package android.icu.impl.coll;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.nio.ByteBuffer;
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
                    sb.append(s, 0, i);
                    sb.append((char) (c + ' '));
                    while (true) {
                        i++;
                        if (i >= s.length()) {
                            return sb.toString();
                        }
                        char c2 = s.charAt(i);
                        if ('A' <= c2 && c2 <= 'Z') {
                            c2 = (char) (c2 + ' ');
                        }
                        sb.append(c2);
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
        String type;
        ULocale uLocale = locale;
        Output<ULocale> output = outValidLocale;
        CollationTailoring root = CollationRoot.getRoot();
        String localeName = locale.getName();
        if (localeName.length() == 0) {
        } else if (localeName.equals("root")) {
            String str = localeName;
        } else {
            try {
                ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, uLocale, ICUResourceBundle.OpenType.LOCALE_ROOT);
                ULocale validLocale = bundleInstance.getULocale();
                String validLocaleName = validLocale.getName();
                if (validLocaleName.length() == 0 || validLocaleName.equals("root")) {
                    validLocale = ULocale.ROOT;
                }
                ULocale validLocale2 = validLocale;
                output.value = validLocale2;
                try {
                    UResourceBundle collations = bundleInstance.get("collations");
                    if (collations == null) {
                        return root;
                    }
                    String type2 = uLocale.getKeywordValue("collation");
                    String defaultType = "standard";
                    String defT = ((ICUResourceBundle) collations).findStringWithFallback("default");
                    if (defT != null) {
                        defaultType = defT;
                    }
                    if (type2 == null || type2.equals("default")) {
                        type = defaultType;
                    } else {
                        type = ASCII.toLowerCase(type2);
                    }
                    UResourceBundle data = findWithFallback(collations, type);
                    if (data == null && type.length() > 6 && type.startsWith("search")) {
                        type = "search";
                        data = findWithFallback(collations, type);
                    }
                    if (data == null && !type.equals(defaultType)) {
                        type = defaultType;
                        data = findWithFallback(collations, type);
                    }
                    if (data == null && !type.equals("standard")) {
                        type = "standard";
                        data = findWithFallback(collations, type);
                    }
                    String type3 = type;
                    if (data == null) {
                        return root;
                    }
                    ULocale actualLocale = data.getULocale();
                    String actualLocaleName = actualLocale.getName();
                    if (actualLocaleName.length() == 0 || actualLocaleName.equals("root")) {
                        actualLocale = ULocale.ROOT;
                        if (type3.equals("standard")) {
                            return root;
                        }
                    }
                    ULocale actualLocale2 = actualLocale;
                    CollationTailoring t = new CollationTailoring(root.settings);
                    t.actualLocale = actualLocale2;
                    UResourceBundle binary = data.get("%%CollationBin");
                    UResourceBundle uResourceBundle = binary;
                    ByteBuffer inBytes = binary.getBinary();
                    try {
                        CollationDataReader.read(root, inBytes, t);
                        try {
                            t.setRulesResource(data.get("Sequence"));
                        } catch (MissingResourceException e) {
                        }
                        if (!type3.equals(defaultType)) {
                            output.value = validLocale2.setKeywordValue("collation", type3);
                        }
                        if (!actualLocale2.equals(validLocale2)) {
                            UResourceBundle actualBundle = UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME, actualLocale2);
                            ByteBuffer byteBuffer = inBytes;
                            UResourceBundle uResourceBundle2 = actualBundle;
                            String defT2 = ((ICUResourceBundle) actualBundle).findStringWithFallback("collations/default");
                            if (defT2 != null) {
                                defaultType = defT2;
                            }
                        }
                        if (!type3.equals(defaultType)) {
                            t.actualLocale = t.actualLocale.setKeywordValue("collation", type3);
                        }
                        return t;
                    } catch (IOException e2) {
                        ByteBuffer byteBuffer2 = inBytes;
                        IOException iOException = e2;
                        String str2 = localeName;
                        StringBuilder sb = new StringBuilder();
                        ICUResourceBundle iCUResourceBundle = bundleInstance;
                        sb.append("Failed to load collation tailoring data for locale:");
                        sb.append(actualLocale2);
                        sb.append(" type:");
                        sb.append(type3);
                        throw new ICUUncheckedIOException(sb.toString(), e2);
                    }
                } catch (MissingResourceException e3) {
                    String str3 = localeName;
                    ICUResourceBundle iCUResourceBundle2 = bundleInstance;
                    return root;
                }
            } catch (MissingResourceException e4) {
                String str4 = localeName;
                output.value = ULocale.ROOT;
                return root;
            }
        }
        output.value = ULocale.ROOT;
        return root;
    }
}
