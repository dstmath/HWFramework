package com.android.internal.app;

import android.content.Context;
import android.provider.SettingsEx.Systemex;
import android.util.Log;
import java.util.ArrayList;

public class HwCustLanguagePickerImpl extends HwCustLanguagePicker {
    private static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustLanguagePickerImpl";

    public void hideTestLanguage(Context context, ArrayList<String> firstLanguageList) {
        if ("true".equals(Systemex.getString(context.getContentResolver(), "hw_hide_test_languages"))) {
            firstLanguageList.remove("ar_XB");
            firstLanguageList.remove("en_XA");
        }
    }

    public String[] getNoSimLanguage(Context context) {
        String noSimStrings = null;
        try {
            noSimStrings = Systemex.getString(context.getContentResolver(), "no_sim_languages");
        } catch (Exception e) {
            Log.e(TAG, "Could not load no sim languages", e);
        }
        if (noSimStrings != null) {
            return noSimStrings.split(",");
        }
        return null;
    }
}
