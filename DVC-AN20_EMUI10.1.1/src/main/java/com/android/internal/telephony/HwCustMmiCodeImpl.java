package com.android.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;

public class HwCustMmiCodeImpl extends HwCustMmiCode {
    private static boolean mToastSwitch = SystemProperties.getBoolean("ro.config.hw_ss_toast", false);

    public boolean isSsToastSwitchEnabled() {
        return mToastSwitch;
    }

    public CharSequence getCustSsToastString(Context context, boolean serviceEnable, boolean isQuery) {
        if (mToastSwitch) {
            if (serviceEnable && isQuery) {
                return context.getText(17041087);
            }
            if (serviceEnable && !isQuery) {
                return context.getText(17041286);
            }
            if (serviceEnable || !isQuery) {
                return context.getText(17041285);
            }
            return context.getText(17041086);
        } else if (serviceEnable) {
            return context.getText(17041199);
        } else {
            return context.getText(17041198);
        }
    }
}
