package com.android.server.input;

import android.content.ContentResolver;
import android.content.Context;
import android.view.InputEvent;

public class HwCustFingerprintNavigation {
    static final String TAG = "HwCustInputManagerService";

    public HwCustFingerprintNavigation(Context context) {
    }

    public boolean handleFingerprintEvent(InputEvent ie) {
        return false;
    }

    public boolean needCustNavigation() {
        return false;
    }

    public boolean getCustNeedValue(ContentResolver cr, String name, int def, int userHandle, int compaireValue) {
        return false;
    }
}
