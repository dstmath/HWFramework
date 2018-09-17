package com.android.internal.telephony.imsphone;

import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.HwTelephonyFactory;

public abstract class AbstractImsPhoneCallTracker extends CallTracker {
    ImsPhoneCallTrackerReference mReference = HwTelephonyFactory.getHwPhoneManager().createHwImsPhoneCallTrackerReference(this);

    public interface ImsPhoneCallTrackerReference {
        void sendAnswerResultCheckMessage();

        void updateCallLog(ImsPhoneConnection imsPhoneConnection, ImsPhone imsPhone);
    }

    public void sendAnswerResultCheckMessage() {
        this.mReference.sendAnswerResultCheckMessage();
    }

    public void updateCallLog(ImsPhoneConnection conn, ImsPhone phone) {
        HwTelephonyFactory.getHwVolteChrManager().updateCallLog(conn, phone);
    }
}
