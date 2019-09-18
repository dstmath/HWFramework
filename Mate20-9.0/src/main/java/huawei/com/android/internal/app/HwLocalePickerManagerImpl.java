package huawei.com.android.internal.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.provider.SettingsEx;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.app.LocalePicker;
import java.util.Locale;

public class HwLocalePickerManagerImpl implements HwLocalePickerManager {
    private static final String AUTOMATIC = "automatic";
    private static String[] LatinAmerican = {"AR", "BO", "CL", "CO", "CR", "CU", "DO", "EC", "GT", "HN", "MX", "NI", "PA", "PE", "PR", "PY", "SV", "UY", "VE"};
    private static final String TAG = "LocalePicker";
    private static String[] black_languages = null;
    private static boolean isShowLanguageOnly = false;
    private static boolean mEnableSimLang = SystemProperties.getBoolean("ro.config.simlang", false);
    private static HwLocalePickerManager mInstance = new HwLocalePickerManagerImpl();
    private static String[] white_languages = null;

    public void initParams(Context context) {
        isShowLanguageOnly = "true".equals(SettingsEx.Systemex.getString(context.getContentResolver(), "hw_show_languages_only"));
    }

    public Locale getLnumLocale(Locale locale) {
        if (locale.getLanguage().equals("ar") || locale.getLanguage().equals("fa")) {
            return new Locale(locale.getLanguage(), locale.getCountry(), "LNum");
        }
        return locale;
    }

    public String getLanguageNameOnly(String lableName) {
        if (!isShowLanguageOnly || lableName.indexOf("(") <= 0) {
            return lableName;
        }
        return lableName.substring(0, lableName.indexOf("("));
    }

    private static String[] getWhiteLanguage(Context context) {
        String white_strings = null;
        try {
            white_strings = SettingsEx.Systemex.getString(context.getContentResolver(), "white_languages");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (white_strings != null) {
            return white_strings.split(",");
        }
        return null;
    }

    private static String[] getBlackLanguage(Context context) {
        String black_strings = null;
        try {
            black_strings = SettingsEx.Systemex.getString(context.getContentResolver(), "black_languages");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (black_strings != null) {
            return black_strings.split(",");
        }
        return null;
    }

    private static boolean arrayContains(String[] array, String value) {
        for (String equalsIgnoreCase : array) {
            if (equalsIgnoreCase.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkCustLanguages(Context context, String s) {
        if (white_languages == null) {
            white_languages = getWhiteLanguage(context);
        }
        if (black_languages == null) {
            black_languages = getBlackLanguage(context);
        }
        if ((white_languages == null || arrayContains(white_languages, s)) && (black_languages == null || !arrayContains(black_languages, s))) {
            return false;
        }
        return true;
    }

    public LocalePicker.LocaleInfo[] addModifyLocaleInfos(Context context, LocalePicker.LocaleInfo[] localeInfos) {
        String newlabel;
        String SimOptr = String.valueOf(context.getResources().getConfiguration().mcc);
        String ap_str2 = context.getResources().getString(33685846);
        int finalSize = localeInfos.length;
        Object obj = "";
        for (int i = 0; i < finalSize; i++) {
            if (localeInfos[i].getLabel().equals("FYROM") || localeInfos[i].getLabel().equals(ap_str2)) {
                if (!"".equals(SimOptr) && "294".equals(SimOptr)) {
                    newlabel = ap_str2;
                } else if (!SimOptr.equals("") || !SystemProperties.getBoolean("ro.config.MkLanguageCust", false)) {
                    newlabel = "FYROM";
                } else {
                    newlabel = (SystemProperties.getInt("ro.config.hw_opta", 0) == 150 && SystemProperties.getInt("ro.config.hw_optb", 0) == 300) ? "FYROM" : ap_str2;
                }
                localeInfos[i] = new LocalePicker.LocaleInfo(newlabel, localeInfos[i].getLocale());
            }
        }
        if (mEnableSimLang != 0) {
            localeInfos = addLocaleInfo(localeInfos, new LocalePicker.LocaleInfo(context.getResources().getString(33685681), new Locale(AUTOMATIC, Locale.getDefault().getCountry())));
        }
        updateLocaleForSimCountry(context, localeInfos);
        if (!"zh".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            return localeInfos;
        }
        return changeJapanLocale(localeInfos);
    }

    private void updateLocaleForSimCountry(Context context, LocalePicker.LocaleInfo[] localeInfos) {
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.mcc > 0) {
            String simCountry = getSimCountry(context, configuration);
            if (!TextUtils.isEmpty(simCountry)) {
                for (Locale locale : Locale.getAvailableLocales()) {
                    if (locale.getLanguage().length() == 2 && ((locale.getVariant() != null && simCountry.equalsIgnoreCase(locale.getVariant())) || (locale.getCountry() != null && simCountry.equalsIgnoreCase(locale.getCountry())))) {
                        if (!"es".equals(locale.getLanguage()) || !arrayContains(LatinAmerican, simCountry)) {
                            Locale locale2 = new Locale(locale.getLanguage(), simCountry);
                            if (!checkHaveLocale(localeInfos, locale2) && checkHaveLanguage(localeInfos, locale2.getLanguage())) {
                                localeInfos = addSIMLocale(localeInfos, locale2);
                            }
                        } else {
                            localeInfos = replaceUSLocale(localeInfos, new Locale("es", simCountry));
                        }
                    }
                }
            }
        }
    }

    private String getSimCountry(Context context, Configuration configuration) {
        String simCountry = "";
        try {
            ClassLoader classLoader = context.getClass().getClassLoader();
            if (classLoader == null) {
                Log.e(TAG, "getClassLoader failed");
                return simCountry;
            }
            Class<?> clazz = classLoader.loadClass("com.android.internal.telephony.MccTable");
            if (clazz != null) {
                simCountry = (String) clazz.getMethod("countryCodeForMcc", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(configuration.mcc)});
            }
            return simCountry;
        } catch (Exception e) {
            Log.e(TAG, "Could not load mccTable", e);
        }
    }

    private LocalePicker.LocaleInfo[] changeJapanLocale(LocalePicker.LocaleInfo[] localeInfos) {
        LocalePicker.LocaleInfo newLocaleInfo = null;
        int length = localeInfos.length;
        LocalePicker.LocaleInfo[] updateLocaleInfos = new LocalePicker.LocaleInfo[length];
        int j = 0;
        for (int i = 0; i < length; i++) {
            if ("ja".equalsIgnoreCase(localeInfos[i].getLocale().getLanguage())) {
                newLocaleInfo = localeInfos[i];
            } else {
                updateLocaleInfos[j] = localeInfos[i];
                j++;
            }
        }
        if (newLocaleInfo != null) {
            updateLocaleInfos[length - 1] = newLocaleInfo;
        }
        return updateLocaleInfos;
    }

    private static boolean checkHaveLanguage(LocalePicker.LocaleInfo[] localeInfos, String lang) {
        for (LocalePicker.LocaleInfo locale : localeInfos) {
            if (locale.getLocale().getLanguage().equals(lang)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkHaveLocale(LocalePicker.LocaleInfo[] localeInfos, Locale checkLocale) {
        for (LocalePicker.LocaleInfo locale : localeInfos) {
            if (locale.getLocale().equals(checkLocale)) {
                return true;
            }
        }
        return false;
    }

    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static LocalePicker.LocaleInfo[] addSIMLocale(LocalePicker.LocaleInfo[] localeInfos, Locale newLocale) {
        return addLocaleInfo(localeInfos, new LocalePicker.LocaleInfo(toTitleCase(newLocale.getDisplayName(newLocale)), newLocale));
    }

    private static LocalePicker.LocaleInfo[] addLocaleInfo(LocalePicker.LocaleInfo[] localeInfos, LocalePicker.LocaleInfo newLocaleInfo) {
        int finalSize = localeInfos.length;
        LocalePicker.LocaleInfo[] updateLocaleInfos = new LocalePicker.LocaleInfo[(finalSize + 1)];
        updateLocaleInfos[0] = newLocaleInfo;
        for (int i = 0; i < finalSize; i++) {
            updateLocaleInfos[i + 1] = localeInfos[i];
        }
        return updateLocaleInfos;
    }

    private static LocalePicker.LocaleInfo[] replaceUSLocale(LocalePicker.LocaleInfo[] localeInfos, Locale newLocale) {
        int finalSize = localeInfos.length;
        int i = 0;
        while (true) {
            if (i >= finalSize) {
                break;
            } else if (new Locale("es", "US").equals(localeInfos[i].getLocale())) {
                localeInfos[i] = new LocalePicker.LocaleInfo(toTitleCase(newLocale.getDisplayName(newLocale)), newLocale);
                break;
            } else {
                i++;
            }
        }
        return localeInfos;
    }

    public static HwLocalePickerManager getDefault() {
        return mInstance;
    }
}
