package com.android.internal.app;

import android.os.SystemProperties;
import android.util.Log;
import java.util.Locale;

public class HwCustLocaleHelperImpl extends HwCustLocaleHelper {
    protected static final boolean DEBUG = true;
    private static final String TAG = "HwCustLocaleHelperImpl";
    private static final String[] mSpecialCode = new String[]{"en_US", "ja_JP"};
    private static final String[] mSpecialNames1 = new String[]{"English", "Japanese"};
    private static final String[] mSpecialNames2 = new String[]{"英語", "日本語"};

    public boolean isDocomo() {
        return SystemProperties.get("ro.product.custom", "NULL").contains("docomo") ? DEBUG : false;
    }

    public String customDisplayName(Locale locale, Locale displayLocale, String originalResult) {
        if (isDocomo()) {
            Log.d(TAG, "customDisplayName locale:" + locale + ", displayLocale:" + displayLocale + ", originalResult:" + originalResult);
            String code = locale.toString();
            int i = 0;
            while (i < mSpecialCode.length) {
                if (mSpecialCode[i].equals(code)) {
                    return shouldShowJapaneseLanguage(displayLocale) ? mSpecialNames2[i] : mSpecialNames1[i];
                }
                i++;
            }
        }
        return originalResult;
    }

    private boolean shouldShowJapaneseLanguage(Locale locale) {
        String lang = locale.getLanguage();
        Log.d(TAG, "shouldShowJapaneseLanguage lang:" + lang);
        return "ja".equals(lang);
    }
}
