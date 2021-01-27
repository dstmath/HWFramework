package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.telephony.CallQuality;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneCapability;
import android.telephony.PhysicalChannelConfig;
import android.telephony.ims.ImsReasonInfo;
import com.android.internal.telephony.PhoneConstants;
import java.util.List;

public interface PhoneNotifier {
    void notifyCallForwardingChanged(Phone phone);

    void notifyCallQualityChanged(Phone phone, CallQuality callQuality, int i);

    void notifyCellInfo(Phone phone, List<CellInfo> list);

    void notifyCellLocation(Phone phone, CellLocation cellLocation);

    void notifyDataActivationStateChanged(Phone phone, int i);

    void notifyDataActivity(Phone phone);

    void notifyDataConnection(Phone phone, String str, PhoneConstants.DataState dataState);

    void notifyDataConnectionFailed(Phone phone, String str);

    void notifyDisconnectCause(Phone phone, int i, int i2);

    void notifyEmergencyNumberList(Phone phone);

    void notifyImsDisconnectCause(Phone phone, ImsReasonInfo imsReasonInfo);

    @UnsupportedAppUsage
    void notifyMessageWaitingChanged(Phone phone);

    void notifyOemHookRawEventForSubscriber(Phone phone, byte[] bArr);

    void notifyOtaspChanged(Phone phone, int i);

    void notifyPhoneCapabilityChanged(PhoneCapability phoneCapability);

    void notifyPhoneState(Phone phone);

    void notifyPhysicalChannelConfiguration(Phone phone, List<PhysicalChannelConfig> list);

    void notifyPreciseCallState(Phone phone);

    void notifyPreciseDataConnectionFailed(Phone phone, String str, String str2, int i);

    void notifyRadioPowerStateChanged(Phone phone, int i);

    void notifyServiceState(Phone phone);

    @UnsupportedAppUsage
    void notifySignalStrength(Phone phone);

    void notifySrvccStateChanged(Phone phone, int i);

    void notifyUserMobileDataStateChanged(Phone phone, boolean z);

    void notifyVoiceActivationStateChanged(Phone phone, int i);
}
