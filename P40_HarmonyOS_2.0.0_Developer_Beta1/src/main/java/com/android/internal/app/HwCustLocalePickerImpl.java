package com.android.internal.app;

import android.content.Context;
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
        if (!(noSimLanguages == null || localeInfos == null)) {
            int noSimLanguagesLength = noSimLanguages.length;
            int localeInfosSize = localeInfos.size();
            for (int i = noSimLanguagesLength - 1; i >= 0; i--) {
                int j = 0;
                while (true) {
                    if (j >= localeInfosSize) {
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
        } catch (SecurityException e) {
            Log.e(TAG, "Could not load default locales" + e.getMessage());
        } catch (Exception e2) {
            Log.e(TAG, "Could not load default locales throw Exception");
        }
        if (noSimStrings != null) {
            return noSimStrings.split(",");
        }
        return null;
    }

    public void customOperate(Context context, ArrayList<LocalePicker.LocaleInfo> localeInfos) {
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
}
