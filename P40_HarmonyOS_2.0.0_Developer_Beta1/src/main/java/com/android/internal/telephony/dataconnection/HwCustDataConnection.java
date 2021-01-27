package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.telephony.data.ApnSetting;
import com.android.internal.telephony.Phone;

public class HwCustDataConnection {
    public static final int REQUEST_REASON_EMERGENCY = 15;

    public boolean setMtuIfNeeded(LinkProperties lp, Phone phone) {
        return false;
    }

    public boolean whetherSetApnByCust(Phone phone) {
        return false;
    }

    public boolean isNeedReMakeCapability() {
        return false;
    }

    public NetworkCapabilities getNetworkCapabilities(String[] types, NetworkCapabilities result, ApnSetting apnSetting, DcTracker dct) {
        return result;
    }

    public void clearInternetPcoValue(int profileId, Phone phone) {
    }

    public boolean isEmergencyApnSetting(ApnSetting sApnSetting) {
        return false;
    }
}
