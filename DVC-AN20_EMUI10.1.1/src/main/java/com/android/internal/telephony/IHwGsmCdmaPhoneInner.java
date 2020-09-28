package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import com.android.internal.telephony.uicc.UiccCardApplication;

public interface IHwGsmCdmaPhoneInner {
    void addPendingMMIsHw(MmiCode mmiCode);

    GsmCdmaPhone getGsmCdmaPhone();

    Phone getImsPhoneHw();

    String getOperatorNumericHw();

    UiccCardApplication getUiccApplicationHw();

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

    void notifyRegistrantsHw(AsyncResult asyncResult);

    void phoneObjectUpdaterHw(int i);

    void removeCallbacksHw();

    void sendEmergencyCallbackModeChangeHw();

    void setPhoneInEcmState(boolean z);
}
