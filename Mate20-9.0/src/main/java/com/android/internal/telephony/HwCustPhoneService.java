package com.android.internal.telephony;

import android.content.Context;

public class HwCustPhoneService {
    public static final int INVALID_NETWORK_MODE = -1;
    public static final int SERVICE_2G_OFF = 0;

    public void setPhone(HwPhone hwPhone, Context context) {
    }

    public boolean isDisable2GServiceCapabilityEnabled() {
        return false;
    }

    public int get2GServiceAbility() {
        return 0;
    }

    public void set2GServiceAbility(int ability) {
    }

    public int getNetworkTypeBaseOnDisabled2G(int curPrefMode) {
        return curPrefMode;
    }
}
