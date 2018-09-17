package android.icu.impl;

import org.xmlpull.v1.XmlPullParser;

public class LocaleIDs {
    private static final String[] _countries = null;
    private static final String[] _countries3 = null;
    private static final String[] _deprecatedCountries = null;
    private static final String[] _languages = null;
    private static final String[] _languages3 = null;
    private static final String[] _obsoleteCountries = null;
    private static final String[] _obsoleteCountries3 = null;
    private static final String[] _obsoleteLanguages = null;
    private static final String[] _obsoleteLanguages3 = null;
    private static final String[] _replacementCountries = null;
    private static final String[] _replacementLanguages = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.LocaleIDs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.LocaleIDs.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.LocaleIDs.<clinit>():void");
    }

    public static String[] getISOCountries() {
        return (String[]) _countries.clone();
    }

    public static String[] getISOLanguages() {
        return (String[]) _languages.clone();
    }

    public static String getISO3Country(String country) {
        int offset = findIndex(_countries, country);
        if (offset >= 0) {
            return _countries3[offset];
        }
        offset = findIndex(_obsoleteCountries, country);
        if (offset >= 0) {
            return _obsoleteCountries3[offset];
        }
        return XmlPullParser.NO_NAMESPACE;
    }

    public static String getISO3Language(String language) {
        int offset = findIndex(_languages, language);
        if (offset >= 0) {
            return _languages3[offset];
        }
        offset = findIndex(_obsoleteLanguages, language);
        if (offset >= 0) {
            return _obsoleteLanguages3[offset];
        }
        return XmlPullParser.NO_NAMESPACE;
    }

    public static String threeToTwoLetterLanguage(String lang) {
        int offset = findIndex(_languages3, lang);
        if (offset >= 0) {
            return _languages[offset];
        }
        offset = findIndex(_obsoleteLanguages3, lang);
        if (offset >= 0) {
            return _obsoleteLanguages[offset];
        }
        return null;
    }

    public static String threeToTwoLetterRegion(String region) {
        int offset = findIndex(_countries3, region);
        if (offset >= 0) {
            return _countries[offset];
        }
        offset = findIndex(_obsoleteCountries3, region);
        if (offset >= 0) {
            return _obsoleteCountries[offset];
        }
        return null;
    }

    private static int findIndex(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (target.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static String getCurrentCountryID(String oldID) {
        int offset = findIndex(_deprecatedCountries, oldID);
        if (offset >= 0) {
            return _replacementCountries[offset];
        }
        return oldID;
    }

    public static String getCurrentLanguageID(String oldID) {
        int offset = findIndex(_obsoleteLanguages, oldID);
        if (offset >= 0) {
            return _replacementLanguages[offset];
        }
        return oldID;
    }
}
