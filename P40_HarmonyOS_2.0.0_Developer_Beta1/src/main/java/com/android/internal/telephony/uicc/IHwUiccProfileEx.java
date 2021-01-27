package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public interface IHwUiccProfileEx {
    default void handleMessageExtend(Message msg) {
    }

    default void registerUiccCardEventsExtend() {
    }

    default void unregisterUiccCardEventsExtend() {
    }

    default void custSetExternalState(IccCardApplicationStatusEx.PersoSubStateEx ps) {
    }

    default void supplyDepersonalization(String pin, int type, Message onComplete) {
    }

    default void custRegisterForNetworkLocked(Handler h, int what, Object obj) {
    }

    default void custUnregisterForNetworkLocked(Handler h) {
    }

    default void handleCustMessage(Message msg) {
    }

    default void setUiccApplication(UiccCardApplicationEx uiccCardApplication) {
    }

    default void registerForFdnStatusChange(Handler h) {
    }

    default void unregisterForFdnStatusChange(Handler h) {
    }

    default void queryFdn() {
    }

    default int processCurrentAppType(UiccCardExt uiccCard, int defaultValue, int cardIndex) {
        return defaultValue;
    }

    default boolean getIccCardStateHw() {
        return false;
    }

    default void custUpdateExternalState(IccCardConstantsEx.StateEx s) {
    }

    default int getUiccIndex(int requestPhoneId, Message msg) {
        return requestPhoneId;
    }

    default boolean isSimAbsent(Context context, UiccCardExt uiccCard) {
        return false;
    }

    default void processSimLockStateForCT() {
    }

    default void custResetExternalState() {
    }

    default IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx state) {
        return state;
    }
}
