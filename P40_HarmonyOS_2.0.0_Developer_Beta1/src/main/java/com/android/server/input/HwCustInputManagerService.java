package com.android.server.input;

import android.content.Context;
import android.util.Log;

public class HwCustInputManagerService {
    static final String TAG = "HwCustInputManagerService";

    public HwCustInputManagerService(Object obj) {
    }

    public int registerContentObserverForSetGloveMode(Context mContext) {
        Log.v(TAG, "registerContentObserverForSetGloveMode 0");
        return 0;
    }
}
