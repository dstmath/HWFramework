package ohos.global.i18n;

import java.util.ArrayList;
import java.util.Locale;
import ohos.app.Context;

public class LocaleHelperUtilsImpl extends LocaleHelperUtils {
    public static ArrayList<Locale> getBlackLangs(Context context) {
        return LocaleHelperUtilsAdapter.getBlackLangs(context.getHostContext());
    }

    public static ArrayList<String> getBlackLangs2String(Context context) {
        return LocaleHelperUtilsAdapter.getBlackLangs2String(context.getHostContext());
    }

    public static ArrayList<String> getBlackRegions(Context context, Locale locale) {
        return LocaleHelperUtilsAdapter.getBlackRegions(context.getHostContext(), locale);
    }

    public static ArrayList<String> getBlackCities(Context context) {
        return LocaleHelperUtilsAdapter.getBlackCities(context.getHostContext());
    }

    public static String getCityName(String str, Locale locale) {
        return LocaleHelperUtilsAdapter.getCityName(str, locale);
    }

    public static String replaceCountryName(Locale locale, Locale locale2, String str) {
        return LocaleHelperUtilsAdapter.replaceCountryName(locale, locale2, str);
    }

    public static String getDisplayCountry(Locale locale, Locale locale2) {
        return LocaleHelperUtilsAdapter.getDisplayCountry(locale, locale2);
    }

    public static String getDisplayName(Locale locale, Locale locale2, boolean z) {
        return LocaleHelperUtilsAdapter.getDisplayName(locale, locale2, z);
    }
}
