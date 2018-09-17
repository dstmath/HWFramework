package com.android.internal.app;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.util.Log;
import com.android.internal.app.LocalePicker.LocaleInfo;
import java.util.ArrayList;
import java.util.Iterator;

public class HwCustLocalePickerImpl extends HwCustLocalePicker {
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustLocalePickerImpl";

    public void changeLanguageOrder(Context context, ArrayList<LocaleInfo> localeInfos) {
        String[] noSimLanguages = getNoSimLanguage(context);
        if (noSimLanguages != null && localeInfos != null) {
            for (int i = noSimLanguages.length - 1; i >= 0; i--) {
                for (int j = 0; j < localeInfos.size(); j++) {
                    if (((LocaleInfo) localeInfos.get(j)).getLabel().equals(noSimLanguages[i])) {
                        localeInfos.add(0, (LocaleInfo) localeInfos.remove(j));
                        break;
                    }
                }
            }
        }
    }

    public String[] getNoSimLanguage(Context context) {
        String noSimStrings = null;
        try {
            noSimStrings = Systemex.getString(context.getContentResolver(), "no_sim_languages");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (noSimStrings != null) {
            return noSimStrings.split(",");
        }
        return null;
    }

    public void customOperate(Context context, ArrayList<LocaleInfo> localeInfos) {
        removeCountryName(context, localeInfos);
        replaceMK(context, localeInfos);
        hideTestLanguage(context, localeInfos);
    }

    private void hideTestLanguage(Context context, ArrayList<LocaleInfo> localeInfos) {
        if ("true".equals(Systemex.getString(context.getContentResolver(), "hw_hide_test_languages"))) {
            Iterator<LocaleInfo> iterator = localeInfos.iterator();
            while (iterator.hasNext()) {
                String languageName = ((LocaleInfo) iterator.next()).getLabel();
                if ("[Bidirection test locale]".equalsIgnoreCase(languageName) || "[Pseudo locale]".equalsIgnoreCase(languageName)) {
                    iterator.remove();
                }
            }
        }
    }

    private void removeCountryName(Context context, ArrayList<LocaleInfo> localeInfos) {
        if ("true".equals(Systemex.getString(context.getContentResolver(), "hw_show_languages_only"))) {
            for (int i = 0; i < localeInfos.size(); i++) {
                String languageName = ((LocaleInfo) localeInfos.get(i)).getLabel();
                if (languageName.indexOf("(") <= 0 && languageName.indexOf("\uff08") > 0) {
                }
            }
        }
    }

    private void replaceMK(Context context, ArrayList<LocaleInfo> localeInfos) {
        if (SystemProperties.getBoolean("ro.config.hw_ReplaceMk", false)) {
            int i = 0;
            while (i < localeInfos.size()) {
                i = "FYROM".equals(((LocaleInfo) localeInfos.get(i)).getLabel()) ? i + 1 : i + 1;
            }
        }
    }
}
