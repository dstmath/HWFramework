package com.android.internal.app;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.SettingsEx;
import android.util.Log;
import com.android.internal.app.LocalePicker;
import java.util.ArrayList;
import java.util.Iterator;

public class HwCustLocalePickerImpl extends HwCustLocalePicker {
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustLocalePickerImpl";

    public void changeLanguageOrder(Context context, ArrayList<LocalePicker.LocaleInfo> localeInfos) {
        String[] noSimLanguages = getNoSimLanguage(context);
        if (noSimLanguages != null && localeInfos != null) {
            int noSimLanguages_length = noSimLanguages.length;
            int localeInfos_size = localeInfos.size();
            for (int i = noSimLanguages_length - 1; i >= 0; i--) {
                int j = 0;
                while (true) {
                    if (j >= localeInfos_size) {
                        break;
                    } else if (localeInfos.get(j).getLabel().equals(noSimLanguages[i])) {
                        localeInfos.add(0, localeInfos.remove(j));
                        break;
                    } else {
                        j++;
                    }
                }
            }
        }
    }

    public String[] getNoSimLanguage(Context context) {
        String noSimStrings = null;
        try {
            noSimStrings = SettingsEx.Systemex.getString(context.getContentResolver(), "no_sim_languages");
        } catch (Exception e) {
            Log.e(TAG, "Could not load default locales", e);
        }
        if (noSimStrings != null) {
            return noSimStrings.split(",");
        }
        return null;
    }

    public void customOperate(Context context, ArrayList<LocalePicker.LocaleInfo> localeInfos) {
        removeCountryName(context, localeInfos);
        replaceMK(context, localeInfos);
        hideTestLanguage(context, localeInfos);
    }

    private void hideTestLanguage(Context context, ArrayList<LocalePicker.LocaleInfo> localeInfos) {
        if ("true".equals(SettingsEx.Systemex.getString(context.getContentResolver(), "hw_hide_test_languages"))) {
            Iterator<LocalePicker.LocaleInfo> iterator = localeInfos.iterator();
            while (iterator.hasNext()) {
                String languageName = iterator.next().getLabel();
                if ("[Bidirection test locale]".equalsIgnoreCase(languageName) || "[Pseudo locale]".equalsIgnoreCase(languageName)) {
                    iterator.remove();
                }
            }
        }
    }

    private void removeCountryName(Context context, ArrayList<LocalePicker.LocaleInfo> localeInfos) {
        if ("true".equals(SettingsEx.Systemex.getString(context.getContentResolver(), "hw_show_languages_only"))) {
            int localeInfos_size = localeInfos.size();
            for (int i = 0; i < localeInfos_size; i++) {
                String languageName = localeInfos.get(i).getLabel();
                if (languageName.indexOf("(") <= 0) {
                    languageName.indexOf("（");
                }
            }
        }
    }

    private void replaceMK(Context context, ArrayList<LocalePicker.LocaleInfo> localeInfos) {
        if (SystemProperties.getBoolean("ro.config.hw_ReplaceMk", false)) {
            int localeInfos_size = localeInfos.size();
            for (int i = 0; i < localeInfos_size; i++) {
                "FYROM".equals(localeInfos.get(i).getLabel());
            }
        }
    }
}
