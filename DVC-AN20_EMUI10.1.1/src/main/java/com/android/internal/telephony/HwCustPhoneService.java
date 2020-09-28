package com.android.internal.telephony;

import android.content.Context;
import com.huawei.internal.telephony.PhoneExt;

public class HwCustPhoneService {
    public static final int INVALID_NETWORK_MODE = -1;
    public static final int SERVICE_2G_OFF = 0;

    public void setPhone(PhoneExt phone, Context context) {
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

    public int getNrOnOffMappingNetworkMode(int slotId, int type, int ability) {
        return -1;
    }
}
