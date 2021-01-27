package com.android.internal.telephony;

import android.os.Handler;
import com.huawei.internal.telephony.MmiCodeExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;

public interface IHwGsmCdmaPhoneInner {
    void addPendingMMIsHw(MmiCodeExt mmiCodeExt);

    PhoneExt getGsmCdmaPhone();

    PhoneExt getImsPhoneHw();

    String getOperatorNumericHw();

    UiccCardApplicationEx getUiccApplicationHw();

    void gsmCdmaPhoneRegisterForLineControlInfo(Handler handler, int i, Object obj);

    void gsmCdmaPhoneSwitchVoiceCallBackgroundState(int i);

    void gsmCdmaPhoneUnRegisterForLineControlInfo(Handler handler);

    void handleEcmExitRespRegistrant();

    void handleWakeLock();

    boolean isCfEnableHw(int i);

    boolean isPhoneInEcmState();

    boolean isPhoneTypeCdmaLteHw();

    boolean isValidCommandInterfaceCFActionHw(int i);

    boolean isValidCommandInterfaceCFReasonHw(int i);

    void notifyEmergencyCallRegistrantsHw(boolean z);

    void notifyRegistrantsHw(Object obj, Object obj2, Throwable th);

    void phoneObjectUpdaterHw(int i);

    void removeCallbacksHw();

    void sendEmergencyCallbackModeChangeHw();

    void setPhoneInEcmState(boolean z);
}
