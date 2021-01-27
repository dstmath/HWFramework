package com.android.internal.telephony;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Message;
import android.telephony.SubscriptionManager;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.DcRequestExt;
import java.util.List;

public interface IHwPhoneSwitcherEx {
    public static final int EVENT_RETRY_ALLOW_DATA = 111;
    public static final int EVENT_VOICE_CALL_ENDED = 112;

    default void addNetworkCapability(NetworkCapabilities netCap) {
    }

    default NetworkCapabilities generateNetCapForVowifi() {
        return new NetworkCapabilities();
    }

    default void handleMessage(Handler phoneSwitcherHandler, PhoneExt[] phones, Message msg) {
    }

    default int getTopPrioritySubscriptionId(List<DcRequestExt> list, int[] phoneSubscriptions) {
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    default int calActivePhonesNum(Context context, int maxActivePhones) {
        return maxActivePhones;
    }

    default void informDdsToQcril(int dataSub) {
    }

    default boolean isSmartSwitchOnSwithing() {
        return false;
    }

    default boolean isDualPsAllowedForSmartSwitch() {
        return false;
    }
}
