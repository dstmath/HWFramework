package huawei.com.android.internal.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.provider.SettingsEx;
import android.text.TextUtils;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.app.LocalePicker;
import com.android.internal.telephony.MccTable;
import com.huawei.uikit.effect.BuildConfig;
import java.util.Locale;

public class HwLocalePickerManagerImpl implements HwLocalePickerManager {
    private static final String AUTOMATIC = "automatic";
    private static final String TAG = "LocalePicker";
    private static String[] blackLanguages = null;
    private static boolean enableSimLang = SystemProperties.getBoolean("ro.config.simlang", false);
    private static volatile HwLocalePickerManager instance;
    private static boolean isShowLanguageOnly = false;
    private static String[] latinAmerican = {"AR", "BO", "CL", "CO", "CR", "CU", "DO", "EC", "GT", "HN", "MX", "NI", "PA", "PE", "PR", "PY", "SV", "UY", "VE"};
    private static final Object lock = new Object();
    private static String[] whiteLanguages = null;

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
        String whiteStrings = SettingsEx.Systemex.getString(context.getContentResolver(), "white_languages");
        if (whiteStrings != null) {
            return whiteStrings.split(",");
        }
        return null;
    }

    private static String[] getBlackLanguage(Context context) {
        String blackStrings = SettingsEx.Systemex.getString(context.getContentResolver(), "black_languages");
        if (blackStrings != null) {
            return blackStrings.split(",");
        }
        return null;
    }

    private static boolean arrayContains(String[] array, String value) {
        for (String str : array) {
            if (str.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkCustLanguages(Context context, String lang) {
        if (whiteLanguages == null) {
            whiteLanguages = getWhiteLanguage(context);
        }
        if (blackLanguages == null) {
            blackLanguages = getBlackLanguage(context);
        }
        String[] strArr = whiteLanguages;
        if (strArr != null && !arrayContains(strArr, lang)) {
            return true;
        }
        String[] strArr2 = blackLanguages;
        if (strArr2 == null || !arrayContains(strArr2, lang)) {
            return false;
        }
        return true;
    }

    public LocalePicker.LocaleInfo[] addModifyLocaleInfos(Context context, LocalePicker.LocaleInfo[] localeInfos) {
        String newlabel;
        String simOptr = String.valueOf(context.getResources().getConfiguration().mcc);
        String apStr2 = context.getResources().getString(33685846);
        int finalSize = localeInfos.length;
        for (int i = 0; i < finalSize; i++) {
            if (localeInfos[i].getLabel().equals("FYROM") || localeInfos[i].getLabel().equals(apStr2)) {
                if (!BuildConfig.FLAVOR.equals(simOptr) && "294".equals(simOptr)) {
                    newlabel = apStr2;
                } else if (!simOptr.equals(BuildConfig.FLAVOR) || !SystemProperties.getBoolean("ro.config.MkLanguageCust", false)) {
                    newlabel = "FYROM";
                } else {
                    newlabel = (SystemProperties.getInt("ro.config.hw_opta", 0) == 150 && SystemProperties.getInt("ro.config.hw_optb", 0) == 300) ? "FYROM" : apStr2;
                }
                localeInfos[i] = new LocalePicker.LocaleInfo(newlabel, localeInfos[i].getLocale());
            }
        }
        if (enableSimLang) {
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
                Locale[] availablelocale = Locale.getAvailableLocales();
                for (Locale locale : availablelocale) {
                    if (locale.getLanguage().length() == 2 && ((locale.getVariant() != null && simCountry.equalsIgnoreCase(locale.getVariant())) || (locale.getCountry() != null && simCountry.equalsIgnoreCase(locale.getCountry())))) {
                        if (!"es".equals(locale.getLanguage()) || !arrayContains(latinAmerican, simCountry)) {
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
        return MccTable.countryCodeForMcc(configuration.mcc);
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
        for (LocalePicker.LocaleInfo localeInfo : localeInfos) {
            if (localeInfo.getLocale().getLanguage().equals(lang)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkHaveLocale(LocalePicker.LocaleInfo[] localeInfos, Locale checkLocale) {
        for (LocalePicker.LocaleInfo localeInfo : localeInfos) {
            if (localeInfo.getLocale().equals(checkLocale)) {
                return true;
            }
        }
        return false;
    }

    private static String toTitleCase(String str) {
        if (str.length() == 0) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static LocalePicker.LocaleInfo[] addSIMLocale(LocalePicker.LocaleInfo[] localeInfos, Locale newLocale) {
        return addLocaleInfo(localeInfos, new LocalePicker.LocaleInfo(toTitleCase(newLocale.getDisplayName(newLocale)), newLocale));
    }

    private static LocalePicker.LocaleInfo[] addLocaleInfo(LocalePicker.LocaleInfo[] localeInfos, LocalePicker.LocaleInfo newLocaleInfo) {
        int finalSize = localeInfos.length;
        LocalePicker.LocaleInfo[] updateLocaleInfos = new LocalePicker.LocaleInfo[(finalSize + 1)];
        updateLocaleInfos[0] = newLocaleInfo;
        System.arraycopy(localeInfos, 0, updateLocaleInfos, 1, finalSize);
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
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new HwLocalePickerManagerImpl();
                }
            }
        }
        return instance;
    }
}
