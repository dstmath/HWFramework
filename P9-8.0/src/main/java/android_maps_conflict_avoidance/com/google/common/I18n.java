package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import android_maps_conflict_avoidance.com.google.debug.DebugUtil;

public class I18n {
    private static String STRING_RESOURCE = "/strings.dat";
    private static I18n instance = null;
    private String[] embeddedLocalizedStrings = null;
    private String[] remoteLocalizedStrings = null;
    private String systemLanguage;
    private String systemLocale;
    private String uiLanguage;
    private String uiLocale;

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
        if ("en".equals(locale) || (locale.length() == 2 && downloadLocale.startsWith(locale))) {
            return downloadLocale;
        }
        return locale;
    }

    I18n(String downloadLocale) {
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
