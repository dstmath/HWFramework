package com.huawei.android.preference;

import android.preference.Preference;

public class PreferenceEx {
    public static void setPreferenceId(Preference preference, int id) {
        if (preference != null) {
            preference.setPreferenceId(id);
        }
    }
}
