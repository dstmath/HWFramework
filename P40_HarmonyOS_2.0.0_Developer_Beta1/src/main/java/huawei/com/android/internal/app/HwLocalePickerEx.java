package huawei.com.android.internal.app;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.HwAssetManagerEx;
import android.icu.util.ULocale;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.app.IHwLocalePickerInner;
import com.android.internal.app.LocalePicker;
import com.huawei.android.app.IHwLocalePickerEx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

public class HwLocalePickerEx implements IHwLocalePickerEx {
    private static final String ADD = "add";
    private static final int AMEND_LANGUAGES_LENGTH = 2;
    private static final String DELETE = "del";
    private static final String FLAG_LANGUAGES_AMEND = "white_languages_amend_for_cust";
    private static final String TAG = "HwLocaleWhiteLanguageAmend";
    private Context mContext;
    private IHwLocalePickerInner mInner;

    public HwLocalePickerEx() {
        this(null, null);
    }

    public HwLocalePickerEx(IHwLocalePickerInner inner, Context context) {
        this.mInner = inner;
        this.mContext = context;
    }

    public boolean isBlackLanguage(Context context, String language) {
        Locale locale = Locale.forLanguageTag(language);
        Iterator<Locale> it = HwLocaleHelperEx.getBlackLangsPart(context).iterator();
        while (it.hasNext()) {
            Locale black = it.next();
            if (black.getScript().isEmpty()) {
                if (locale.getLanguage().equals(black.getLanguage())) {
                    return true;
                }
            } else if (locale.getLanguage().equals(black.getLanguage()) && locale.getScript().equals(black.getScript())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getWhiteLanguage(Context context) {
        ArrayList<String> whiteLanguages = new ArrayList<>();
        String whiteStrings = getWhiteString(context);
        if (!TextUtils.isEmpty(whiteStrings)) {
            String whiteStrings2 = whiteStrings.replace("tl", "fil");
            Set<String> systemAssetLocales = new HashSet<>();
            for (String systemAssetLocale : LocalePicker.getSystemAssetLocales()) {
                systemAssetLocales.add(ULocale.addLikelySubtags(ULocale.forLanguageTag(systemAssetLocale)).getFallback().toLanguageTag());
            }
            if (systemAssetLocales.contains("en-Latn")) {
                systemAssetLocales.add("en-Qaag");
            }
            for (String localeStr : whiteStrings2.split(",")) {
                String localeTag = ULocale.addLikelySubtags(ULocale.forLanguageTag(localeStr.replace('_', '-'))).getFallback().toLanguageTag();
                if (systemAssetLocales.contains(localeTag) && !HwFrameworkFactory.getHwLocalePickerEx().isBlackLanguage(context, localeTag)) {
                    whiteLanguages.add(localeTag);
                }
            }
        }
        String[] downloadedLanguage = HwAssetManagerEx.getSharedResList();
        if (downloadedLanguage != null) {
            for (String locale : downloadedLanguage) {
                if (!TextUtils.isEmpty(locale)) {
                    boolean repeat = false;
                    String downTag = ULocale.addLikelySubtags(ULocale.forLanguageTag(locale)).getFallback().toLanguageTag();
                    int i = 0;
                    int listSize = whiteLanguages.size();
                    while (true) {
                        if (i >= listSize) {
                            break;
                        } else if (downTag.equals(whiteLanguages.get(i))) {
                            repeat = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (!repeat && !HwFrameworkFactory.getHwLocalePickerEx().isBlackLanguage(context, downTag)) {
                        whiteLanguages.add(downTag);
                    }
                }
            }
        }
        return whiteLanguages;
    }

    private String getWhiteString(Context context) {
        if (context == null) {
            return null;
        }
        String whiteStrings = Settings.System.getString(context.getContentResolver(), "white_languages");
        String whiteLanguagesAmendForCust = getWhiteLanguageForAmend(context);
        if (whiteStrings == null || whiteLanguagesAmendForCust == null) {
            return whiteStrings;
        }
        return parseWhiteLanguageAmend(whiteLanguagesAmendForCust, whiteStrings);
    }

    private String parseWhiteLanguageAmend(String whiteLanguagesAmendForCust, String whiteStrings) {
        if (whiteStrings == null) {
            return whiteStrings;
        }
        String whiteStringsForAmend = whiteStrings;
        String amendDelStrings = null;
        String amendAddString = null;
        for (String operLanguagesStr : whiteLanguagesAmendForCust.split(";")) {
            String[] operLanguagesArr = operLanguagesStr.split(":");
            if (operLanguagesArr.length == 2) {
                if (ADD.equalsIgnoreCase(operLanguagesArr[0])) {
                    amendAddString = operLanguagesArr[1];
                } else if (DELETE.equalsIgnoreCase(operLanguagesArr[0])) {
                    amendDelStrings = operLanguagesArr[1];
                } else {
                    Slog.w(TAG, "Invalid tag for Language amend");
                }
            }
        }
        if (amendAddString != null) {
            String[] split = amendAddString.split(",");
            for (String amendAddLanguage : split) {
                if (!whiteStringsForAmend.contains(amendAddLanguage)) {
                    whiteStringsForAmend = whiteStringsForAmend + "," + amendAddLanguage;
                }
            }
        }
        if (amendDelStrings != null) {
            return delAmendLanguages(amendDelStrings, whiteStringsForAmend);
        }
        return whiteStringsForAmend;
    }

    private String delAmendLanguages(String amendDelStrings, String whiteStringsForAmend) {
        List<Locale> whiteLocales = changeStrToLocale(whiteStringsForAmend);
        for (Locale del : changeStrToLocale(amendDelStrings)) {
            if (whiteLocales.contains(del)) {
                whiteLocales.remove(del);
            } else {
                ListIterator<Locale> it = whiteLocales.listIterator();
                while (it.hasNext()) {
                    if (ULocale.addLikelySubtags(ULocale.forLocale(it.next())).getFallback().toLanguageTag().equals(ULocale.addLikelySubtags(ULocale.forLocale(del)).getFallback().toLanguageTag())) {
                        it.remove();
                    }
                }
            }
        }
        return changeLocaleToStr(whiteLocales);
    }

    private List<Locale> changeStrToLocale(String langs) {
        String[] arr = langs.split(",");
        List<Locale> locales = new ArrayList<>();
        for (String str : arr) {
            locales.add(Locale.forLanguageTag(str.replace("_", "-")));
        }
        return locales;
    }

    private String changeLocaleToStr(List<Locale> locales) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < locales.size(); i++) {
            String str = locales.get(i).toLanguageTag().replace("-", "_");
            if (i == locales.size() - 1) {
                result.append(str);
            } else {
                result.append(str);
                result.append(",");
            }
        }
        return result.toString();
    }

    private String getWhiteLanguageForAmend(Context context) {
        if (context == null) {
            return null;
        }
        return Settings.System.getString(context.getContentResolver(), FLAG_LANGUAGES_AMEND);
    }
}
