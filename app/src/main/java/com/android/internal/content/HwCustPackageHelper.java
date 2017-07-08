package com.android.internal.content;

import android.content.Context;
import android.util.Log;

public class HwCustPackageHelper {
    static final String TAG = "HwCustPackageHelper";

    public HwCustPackageHelper() {
        Log.d(TAG, TAG);
    }

    public boolean isSdInstallEnabled() {
        return false;
    }

    public boolean fitsOnExternalEx(Context context, long sizeBytes) {
        return false;
    }
}
