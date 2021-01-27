package com.android.internal.telephony;

import com.huawei.internal.telephony.PhoneExt;

public interface HwNetworkManager {
    default boolean isNetworkModeAsynchronized(PhoneExt phone) {
        return false;
    }

    default void setPreferredNetworkTypeForNoMdn(PhoneExt phone, int settingMode) {
    }

    default void factoryResetNetworkTypeForNoMdn(PhoneExt phone) {
    }
}
