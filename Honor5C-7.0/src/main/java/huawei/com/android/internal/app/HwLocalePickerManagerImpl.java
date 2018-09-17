package huawei.com.android.internal.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.util.Log;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.app.LocalePicker.LocaleInfo;
import java.lang.reflect.Method;
import java.util.Locale;

public class HwLocalePickerManagerImpl implements HwLocalePickerManager {
    private static final String AUTOMATIC = "automatic";
    private static String[] LatinAmerican = null;
    private static final String TAG = "LocalePicker";
    private static String[] black_languages;
    private static boolean isShowLanguageOnly;
    private static boolean mEnableSimLang;
    private static HwLocalePickerManager mInstance;
    private static String[] white_languages;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.internal.app.HwLocalePickerManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.internal.app.HwLocalePickerManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.internal.app.HwLocalePickerManagerImpl.<clinit>():void");
    }

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
        if ((white_languages == null || arrayContains(white_languages, s)) && (black_languages == null || !arrayContains(black_languages, s))) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public LocaleInfo[] addModifyLocaleInfos(Context context, LocaleInfo[] localeInfos) {
        String SimOptr = String.valueOf(context.getResources().getConfiguration().mcc);
        String ap_str1 = "FYROM";
        String ap_str2 = context.getResources().getString(33685839);
        String newlabel = "";
        int finalSize = localeInfos.length;
        int i = 0;
        while (i < finalSize) {
            if (!localeInfos[i].getLabel().equals(ap_str1)) {
                if (!localeInfos[i].getLabel().equals(ap_str2)) {
                    i++;
                }
            }
            if (!"".equals(SimOptr)) {
                if ("294".equals(SimOptr)) {
                    newlabel = ap_str2;
                    localeInfos[i] = new LocaleInfo(newlabel, localeInfos[i].getLocale());
                    i++;
                }
            }
            if (SimOptr.equals("") && SystemProperties.getBoolean("ro.config.MkLanguageCust", false)) {
                if (SystemProperties.getInt("ro.config.hw_opta", 0) == 150 && SystemProperties.getInt("ro.config.hw_optb", 0) == 300) {
                    newlabel = ap_str1;
                } else {
                    newlabel = ap_str2;
                }
                localeInfos[i] = new LocaleInfo(newlabel, localeInfos[i].getLocale());
                i++;
            } else {
                newlabel = ap_str1;
                localeInfos[i] = new LocaleInfo(newlabel, localeInfos[i].getLocale());
                i++;
            }
        }
        if (mEnableSimLang) {
            localeInfos = addLocaleInfo(localeInfos, new LocaleInfo(context.getResources().getString(33685673), new Locale(AUTOMATIC, Locale.getDefault().getCountry())));
        }
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.mcc > 0) {
            String simCountry = "";
            try {
                Class<?> clazz = context.getClass().getClassLoader().loadClass("com.android.internal.telephony.MccTable");
                if (clazz != null) {
                    Method m = clazz.getMethod("countryCodeForMcc", new Class[]{Integer.TYPE});
                    Integer[] numArr = new Object[1];
                    numArr[0] = Integer.valueOf(configuration.mcc);
                    simCountry = (String) m.invoke(null, numArr);
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not load mccTable", e);
            }
            if (simCountry != null && simCountry.length() > 0) {
                for (Locale locale : Locale.getAvailableLocales()) {
                    Locale locale2;
                    if (locale2.getLanguage().length() == 2) {
                        if (locale2.getVariant() != null) {
                        }
                        if (locale2.getCountry() != null) {
                            if (!simCountry.equalsIgnoreCase(locale2.getCountry())) {
                            }
                            if ("es".equals(locale2.getLanguage())) {
                                if (arrayContains(LatinAmerican, simCountry)) {
                                    localeInfos = replaceUSLocale(localeInfos, new Locale("es", simCountry));
                                }
                            }
                            Locale locale3 = new Locale(locale2.getLanguage(), simCountry);
                            if (checkHaveLocale(localeInfos, locale3)) {
                                locale2 = locale3;
                            } else {
                                if (checkHaveLanguage(localeInfos, locale3.getLanguage())) {
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
        if (!"zh".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            return localeInfos;
        }
        LocaleInfo newLocaleInfo = null;
        int length = localeInfos.length;
        LocaleInfo[] updateLocaleInfos = new LocaleInfo[length];
        int j = 0;
        for (i = 0; i < length; i++) {
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
