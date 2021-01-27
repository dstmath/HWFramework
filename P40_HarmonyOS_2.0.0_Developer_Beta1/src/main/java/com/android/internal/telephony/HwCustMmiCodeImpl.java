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
                return context.getText(17041091);
            }
            if (serviceEnable && !isQuery) {
                return context.getText(17041289);
            }
            if (serviceEnable || !isQuery) {
                return context.getText(17041288);
            }
            return context.getText(17041090);
        } else if (serviceEnable) {
            return context.getText(17041203);
        } else {
            return context.getText(17041202);
        }
    }
}
