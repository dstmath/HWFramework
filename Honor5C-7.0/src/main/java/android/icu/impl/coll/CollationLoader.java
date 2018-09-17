package android.icu.impl.coll;

import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceBundle.OpenType;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.util.MissingResourceException;

public final class CollationLoader {
    private static volatile String rootRules;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationLoader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationLoader.<clinit>():void");
    }

    private CollationLoader() {
    }

    private static void loadRootRules() {
        if (rootRules == null) {
            synchronized (CollationLoader.class) {
                if (rootRules == null) {
                    rootRules = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_COLLATION_BASE_NAME, ULocale.ROOT).getString("UCARules");
                }
            }
        }
    }

    public static String getRootRules() {
        loadRootRules();
        return rootRules;
    }

    static String loadRules(ULocale locale, String collationType) {
        return ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_COLLATION_BASE_NAME, locale)).getWithFallback("collations/" + ASCII.toLowerCase(collationType)).getString("Sequence");
    }

    private static final UResourceBundle findWithFallback(UResourceBundle table, String entryName) {
        return ((ICUResourceBundle) table).findWithFallback(entryName);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static CollationTailoring loadTailoring(ULocale locale, Output<ULocale> outValidLocale) {
        ULocale actualLocale;
        CollationTailoring root = CollationRoot.getRoot();
        String localeName = locale.getName();
        if (localeName.length() != 0) {
            if (!localeName.equals("root")) {
                try {
                    UResourceBundle bundle = ICUResourceBundle.getBundleInstance(ICUResourceBundle.ICU_COLLATION_BASE_NAME, locale, OpenType.LOCALE_ROOT);
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
                        UResourceBundle data;
                        String actualLocaleName;
                        CollationTailoring collationTailoring;
                        String str;
                        String type = locale.getKeywordValue("collation");
                        String defaultType = "standard";
                        String defT = ((ICUResourceBundle) collations).findStringWithFallback("default");
                        if (defT != null) {
                            defaultType = defT;
                        }
                        if (type != null) {
                            if (!type.equals("default")) {
                                type = ASCII.toLowerCase(type);
                                data = findWithFallback(collations, type);
                                if (data == null && type.length() > 6) {
                                    if (type.startsWith("search")) {
                                        type = "search";
                                        data = findWithFallback(collations, type);
                                    }
                                }
                                if (data == null && !type.equals(defaultType)) {
                                    type = defaultType;
                                    data = findWithFallback(collations, type);
                                }
                                if (data == null) {
                                    if (!type.equals("standard")) {
                                        type = "standard";
                                        data = findWithFallback(collations, type);
                                    }
                                }
                                if (data == null) {
                                    return root;
                                }
                                actualLocale = data.getULocale();
                                actualLocaleName = actualLocale.getName();
                                if (actualLocaleName.length() != 0) {
                                }
                                actualLocale = ULocale.ROOT;
                                if (type.equals("standard")) {
                                    return root;
                                }
                                collationTailoring = new CollationTailoring(root.settings);
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
                                        str = "collations/default";
                                        defT = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_COLLATION_BASE_NAME, actualLocale)).findStringWithFallback(r22);
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
                            }
                        }
                        type = defaultType;
                        data = findWithFallback(collations, type);
                        if (type.startsWith("search")) {
                            type = "search";
                            data = findWithFallback(collations, type);
                        }
                        type = defaultType;
                        data = findWithFallback(collations, type);
                        if (data == null) {
                            if (type.equals("standard")) {
                                type = "standard";
                                data = findWithFallback(collations, type);
                            }
                        }
                        if (data == null) {
                            return root;
                        }
                        actualLocale = data.getULocale();
                        actualLocaleName = actualLocale.getName();
                        if (actualLocaleName.length() != 0) {
                        }
                        actualLocale = ULocale.ROOT;
                        if (type.equals("standard")) {
                            return root;
                        }
                        collationTailoring = new CollationTailoring(root.settings);
                        collationTailoring.actualLocale = actualLocale;
                        CollationDataReader.read(root, data.get("%%CollationBin").getBinary(), collationTailoring);
                        collationTailoring.setRulesResource(data.get("Sequence"));
                        if (type.equals(defaultType)) {
                            outValidLocale.value = validLocale.setKeywordValue("collation", type);
                        }
                        if (actualLocale.equals(validLocale)) {
                            str = "collations/default";
                            defT = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_COLLATION_BASE_NAME, actualLocale)).findStringWithFallback(r22);
                            if (defT != null) {
                                defaultType = defT;
                            }
                        }
                        if (type.equals(defaultType)) {
                            collationTailoring.actualLocale = collationTailoring.actualLocale.setKeywordValue("collation", type);
                        }
                        return collationTailoring;
                    } catch (MissingResourceException e3) {
                        return root;
                    }
                } catch (MissingResourceException e4) {
                    outValidLocale.value = ULocale.ROOT;
                    return root;
                }
            }
        }
        outValidLocale.value = ULocale.ROOT;
        return root;
    }
}
