package com.android.server.pm;

import android.util.Log;

public class HwCustSettings {
    static final String TAG = "HwCustSettings";

    public HwCustSettings() {
        Log.d(TAG, TAG);
    }

    public boolean isInNosysAppList(String packageName) {
        return false;
    }
}
