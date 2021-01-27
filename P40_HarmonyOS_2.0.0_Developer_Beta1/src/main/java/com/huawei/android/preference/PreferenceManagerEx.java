package com.huawei.android.preference;

import android.content.Context;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PreferenceManagerEx {
    public static PreferenceScreen inflateFromResource(PreferenceManager preferenceManager, Context context, int resId, PreferenceScreen rootPreferences) {
        return preferenceManager.inflateFromResource(context, resId, rootPreferences);
    }
}
