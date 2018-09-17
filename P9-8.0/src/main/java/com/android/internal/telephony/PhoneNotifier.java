package com.android.internal.telephony;

import android.telephony.CellInfo;
import android.telephony.VoLteServiceState;
import com.android.internal.telephony.PhoneConstants.DataState;
import java.util.List;

public interface PhoneNotifier {
    void notifyCallForwardingChanged(Phone phone);

    void notifyCellInfo(Phone phone, List<CellInfo> list);

    void notifyCellLocation(Phone phone);

    void notifyDataActivationStateChanged(Phone phone, int i);

    void notifyDataActivity(Phone phone);

    void notifyDataConnection(Phone phone, String str, String str2, DataState dataState);

    void notifyDataConnectionFailed(Phone phone, String str, String str2);

    void notifyDisconnectCause(int i, int i2);

    void notifyMessageWaitingChanged(Phone phone);

    void notifyOemHookRawEventForSubscriber(int i, byte[] bArr);

    void notifyOtaspChanged(Phone phone, int i);

    void notifyPhoneState(Phone phone);

    void notifyPreciseCallState(Phone phone);

    void notifyPreciseDataConnectionFailed(Phone phone, String str, String str2, String str3, String str4);

    void notifyServiceState(Phone phone);

    void notifySignalStrength(Phone phone);

    void notifyVoLteServiceStateChanged(Phone phone, VoLteServiceState voLteServiceState);

    void notifyVoiceActivationStateChanged(Phone phone, int i);
}
