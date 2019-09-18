package com.android.internal.telephony;

import android.os.AsyncResult;
import com.android.internal.telephony.uicc.UiccCardApplication;

public interface IHwGsmCdmaPhoneInner {
    void addPendingMMIsHw(MmiCode mmiCode);

    GsmCdmaPhone getGsmCdmaPhone();

    Phone getImsPhoneHw();

    UiccCardApplication getUiccApplicationHw();

    void handleEcmExitRespRegistrant();

    void handleWakeLock();

    boolean isCfEnableHw(int i);

    boolean isPhoneInEcmState();

    boolean isValidCommandInterfaceCFActionHw(int i);

    boolean isValidCommandInterfaceCFReasonHw(int i);

    void notifyRegistrantsHw(AsyncResult asyncResult);

    void phoneObjectUpdaterHw(int i);

    void removeCallbacksHw();

    void sendEmergencyCallbackModeChangeHw();

    void setPhoneInEcmState(boolean z);
}
