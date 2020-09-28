package com.android.internal.telephony;

import android.content.Context;

public class HwCustMmiCode {
    public boolean isSsToastSwitchEnabled() {
        return false;
    }

    public CharSequence getCustSsToastString(Context context, boolean serviceEnable, boolean isQuery) {
        if (serviceEnable) {
            return context.getText(17041199);
        }
        return context.getText(17041198);
    }
}
