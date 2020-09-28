package com.android.internal.telephony;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.dataconnection.DcRequest;
import java.util.List;

public interface IHwPhoneSwitcherEx {
    public static final int EVENT_RETRY_ALLOW_DATA = 111;
    public static final int EVENT_VOICE_CALL_ENDED = 112;

    void addNetworkCapability(NetworkCapabilities networkCapabilities);

    int calActivePhonesNum(Context context, int i);

    NetworkCapabilities generateNetCapForVowifi();

    int getTopPrioritySubscriptionId(List<DcRequest> list, int[] iArr);

    void handleMessage(Handler handler, Phone[] phoneArr, Message message);

    void informDdsToQcril(int i);

    boolean isDualPsAllowedForSmartSwitch();

    boolean isSmartSwitchOnSwithing();
}
