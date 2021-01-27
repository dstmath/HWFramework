package ohos.global.i18n;

import android.content.Context;
import com.huawei.android.app.LocaleHelperEx;
import java.util.ArrayList;
import java.util.Locale;
import ohos.hiviewdfx.HiLogLabel;

public class LocaleHelperUtilsAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "LocaleHelperUtilsAdapter");

    public static ArrayList<Locale> getBlackLangs(Object obj) {
        return obj instanceof Context ? LocaleHelperEx.getBlackLangs((Context) obj) : new ArrayList<>();
    }

    public static ArrayList<String> getBlackLangs2String(Object obj) {
        return obj instanceof Context ? LocaleHelperEx.getBlackLangs2String((Context) obj) : new ArrayList<>();
    }

    public static ArrayList<String> getBlackRegions(Object obj, Locale locale) {
        return obj instanceof Context ? LocaleHelperEx.getBlackRegions((Context) obj, locale) : new ArrayList<>();
    }

    public static ArrayList<String> getBlackCities(Object obj) {
        return obj instanceof Context ? LocaleHelperEx.getBlackCities((Context) obj) : new ArrayList<>();
    }

    public static String getCityName(String str, Locale locale) {
        return LocaleHelperEx.getCityName(str, locale);
    }

    public static String replaceCountryName(Locale locale, Locale locale2, String str) {
        return LocaleHelperEx.replaceCountryName(locale, locale2, str);
    }

    public static String getDisplayCountry(Locale locale, Locale locale2) {
        return LocaleHelperEx.getDisplayCountry(locale, locale2);
    }

    public static String getDisplayName(Locale locale, Locale locale2, boolean z) {
        return LocaleHelperEx.getDisplayName(locale, locale2, z);
    }
}
