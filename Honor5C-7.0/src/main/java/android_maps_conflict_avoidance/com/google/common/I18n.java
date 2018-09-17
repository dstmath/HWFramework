package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import android_maps_conflict_avoidance.com.google.debug.DebugUtil;

public class I18n {
    private static String STRING_RESOURCE;
    private static I18n instance;
    private String[] embeddedLocalizedStrings;
    private String[] remoteLocalizedStrings;
    private String systemLanguage;
    private String systemLocale;
    private String uiLanguage;
    private String uiLocale;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.I18n.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.I18n.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.I18n.<clinit>():void");
    }

    public static String locale() {
        return DebugUtil.getAntPropertyOrNull("en");
    }

    public static I18n init(String downloadLocale) {
        instance = new I18n(downloadLocale);
        return instance;
    }

    public static String normalizeLocale(String rawLocale) {
        String locale = "en";
        if (rawLocale == null) {
            return locale;
        }
        String[] localeParts = TextUtil.split(rawLocale.replace('-', '_'), '_');
        if (localeParts[0].length() != 2 && localeParts[0].length() != 3) {
            return locale;
        }
        locale = localeParts[0].toLowerCase();
        if (localeParts.length < 2 || localeParts[1].length() != 2) {
            return locale;
        }
        return locale + "_" + localeParts[1].toUpperCase();
    }

    private static String calculateSystemLocale(String downloadLocale) {
        downloadLocale = normalizeLocale(downloadLocale);
        String locale = normalizeLocale(System.getProperty("microedition.locale"));
        if (!"en".equals(locale)) {
            if (locale.length() != 2) {
                return locale;
            }
            if (!downloadLocale.startsWith(locale)) {
                return locale;
            }
        }
        return downloadLocale;
    }

    I18n(String downloadLocale) {
        this.embeddedLocalizedStrings = null;
        this.remoteLocalizedStrings = null;
        setSystemLocale(calculateSystemLocale(downloadLocale));
        setUiLocale(locale());
    }

    public String getUiLocale() {
        return this.uiLocale;
    }

    public void setSystemLocale(String locale) {
        this.systemLocale = normalizeLocale(locale);
        int split = this.systemLocale.indexOf(95);
        this.systemLanguage = split >= 0 ? this.systemLocale.substring(0, split) : this.systemLocale;
    }

    public void setUiLocale(String locale) {
        this.uiLocale = locale == null ? this.systemLocale : normalizeLocale(locale);
        this.uiLanguage = getLanguage(this.uiLocale);
    }

    public static String getLanguage(String locale) {
        int split = locale.indexOf(95);
        if (split < 0) {
            split = locale.indexOf(45);
        }
        return split >= 0 ? locale.substring(0, split) : locale;
    }
}
