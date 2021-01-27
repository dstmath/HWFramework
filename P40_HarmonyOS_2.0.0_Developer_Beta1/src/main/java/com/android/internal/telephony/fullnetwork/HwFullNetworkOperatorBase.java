package com.android.internal.telephony.fullnetwork;

public interface HwFullNetworkOperatorBase {
    int getDefaultMainSlot(boolean z);

    boolean isMainSlotFound();

    void logd(String str);

    void loge(String str);
}
