package huawei.com.android.internal.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.rms.iaware.AppTypeInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.app.LocalePicker.LocaleInfo;
import java.util.Locale;

public class HwLocalePickerManagerImpl implements HwLocalePickerManager {
    private static final String AUTOMATIC = "automatic";
    private static String[] LatinAmerican = new String[]{"AR", "BO", "CL", "CO", "CR", "CU", "DO", "EC", "GT", "HN", "MX", "NI", "PA", "PE", "PR", "PY", "SV", "UY", "VE"};
    private static final String TAG = "LocalePicker";
    private static String[] black_languages = null;
    private static boolean isShowLanguageOnly = false;
    private static boolean mEnableSimLang = SystemProperties.getBoolean("ro.config.simlang", false);
    private static HwLocalePickerManager mInstance = new HwLocalePickerManagerImpl();
    private static String[] white_languages = null;

    public void initParams(Context context) {
        isShowLanguageOnly = "true".equals(Systemex.getString(context.getContentResolver(), "hw_show_languages_only"));
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
            white_strings = Systemex.getString(context.getContentResolver(), "white_languages");
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
            black_strings = Systemex.getString(context.getContentResolver(), "black_languages");
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
        if ((white_languages == null || (arrayContains(white_languages, s) ^ 1) == 0) && (black_languages == null || !arrayContains(black_languages, s))) {
            return false;
        }
        return true;
    }

    public LocaleInfo[] addModifyLocaleInfos(Context context, LocaleInfo[] localeInfos) {
        String SimOptr = String.valueOf(context.getResources().getConfiguration().mcc);
        String ap_str1 = "FYROM";
        String ap_str2 = context.getResources().getString(33685846);
        String newlabel = "";
        int finalSize = localeInfos.length;
        int i = 0;
        while (i < finalSize) {
            if (localeInfos[i].getLabel().equals(ap_str1) || localeInfos[i].getLabel().equals(ap_str2)) {
                if (!"".equals(SimOptr) && "294".equals(SimOptr)) {
                    newlabel = ap_str2;
                } else if (!SimOptr.equals("") || !SystemProperties.getBoolean("ro.config.MkLanguageCust", false)) {
                    newlabel = ap_str1;
                } else if (SystemProperties.getInt("ro.config.hw_opta", 0) == 150 && SystemProperties.getInt("ro.config.hw_optb", 0) == AppTypeInfo.PG_TYPE_BASE) {
                    newlabel = ap_str1;
                } else {
                    newlabel = ap_str2;
                }
                localeInfos[i] = new LocaleInfo(newlabel, localeInfos[i].getLocale());
            }
            i++;
        }
        if (mEnableSimLang) {
            localeInfos = addLocaleInfo(localeInfos, new LocaleInfo(context.getResources().getString(33685681), new Locale(AUTOMATIC, Locale.getDefault().getCountry())));
        }
        updateLocaleForSimCountry(context, localeInfos);
        if ("zh".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            return changeJapanLocale(localeInfos);
        }
        return localeInfos;
    }

    private void updateLocaleForSimCountry(Context context, LocaleInfo[] localeInfos) {
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.mcc > 0) {
            String simCountry = getSimCountry(context, configuration);
            if (!TextUtils.isEmpty(simCountry)) {
                for (Locale locale : Locale.getAvailableLocales()) {
                    Locale locale2;
                    if (locale2.getLanguage().length() == 2 && ((locale2.getVariant() != null && simCountry.equalsIgnoreCase(locale2.getVariant())) || (locale2.getCountry() != null && simCountry.equalsIgnoreCase(locale2.getCountry())))) {
                        if ("es".equals(locale2.getLanguage()) && arrayContains(LatinAmerican, simCountry)) {
                            localeInfos = replaceUSLocale(localeInfos, new Locale("es", simCountry));
                        } else {
                            Locale locale3 = new Locale(locale2.getLanguage(), simCountry);
                            if (checkHaveLocale(localeInfos, locale3)) {
                            } else if (checkHaveLanguage(localeInfos, locale3.getLanguage())) {
                                localeInfos = addSIMLocale(localeInfos, locale3);
                                locale2 = locale3;
                            } else {
                                locale2 = locale3;
                            }
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

    private LocaleInfo[] changeJapanLocale(LocaleInfo[] localeInfos) {
        LocaleInfo newLocaleInfo = null;
        int length = localeInfos.length;
        LocaleInfo[] updateLocaleInfos = new LocaleInfo[length];
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

    private static boolean checkHaveLanguage(LocaleInfo[] localeInfos, String lang) {
        for (LocaleInfo locale : localeInfos) {
            if (locale.getLocale().getLanguage().equals(lang)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkHaveLocale(LocaleInfo[] localeInfos, Locale checkLocale) {
        for (LocaleInfo locale : localeInfos) {
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

    private static LocaleInfo[] addSIMLocale(LocaleInfo[] localeInfos, Locale newLocale) {
        return addLocaleInfo(localeInfos, new LocaleInfo(toTitleCase(newLocale.getDisplayName(newLocale)), newLocale));
    }

    private static LocaleInfo[] addLocaleInfo(LocaleInfo[] localeInfos, LocaleInfo newLocaleInfo) {
        int finalSize = localeInfos.length;
        LocaleInfo[] updateLocaleInfos = new LocaleInfo[(finalSize + 1)];
        updateLocaleInfos[0] = newLocaleInfo;
        for (int i = 0; i < finalSize; i++) {
            updateLocaleInfos[i + 1] = localeInfos[i];
        }
        return updateLocaleInfos;
    }

    private static LocaleInfo[] replaceUSLocale(LocaleInfo[] localeInfos, Locale newLocale) {
        int finalSize = localeInfos.length;
        for (int i = 0; i < finalSize; i++) {
            if (new Locale("es", "US").equals(localeInfos[i].getLocale())) {
                localeInfos[i] = new LocaleInfo(toTitleCase(newLocale.getDisplayName(newLocale)), newLocale);
                break;
            }
        }
        return localeInfos;
    }

    public static HwLocalePickerManager getDefault() {
        return mInstance;
    }
}
