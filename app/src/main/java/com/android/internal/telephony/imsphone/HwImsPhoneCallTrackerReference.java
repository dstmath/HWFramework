package com.android.internal.telephony.imsphone;

import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker.ImsPhoneCallTrackerReference;

public class HwImsPhoneCallTrackerReference extends Handler implements ImsPhoneCallTrackerReference {
    private static final int EVENT_ANSWER_RESULT_CHECK = 1;
    private static final String TAG = "HwImsPhoneCallTrackerReference";
    private static final int TIMER_ANSWER_CHECK = 2000;
    private ImsPhoneCallTracker mImsPhoneCallTracker;

    public HwImsPhoneCallTrackerReference(ImsPhoneCallTracker imsPhoneCallTracker) {
        this.mImsPhoneCallTracker = imsPhoneCallTracker;
    }

    public void sendAnswerResultCheckMessage() {
        if (!hasMessages(EVENT_ANSWER_RESULT_CHECK)) {
            sendMessageDelayed(obtainMessage(EVENT_ANSWER_RESULT_CHECK), 2000);
        }
    }

    public void updateCallLog(ImsPhoneConnection conn, ImsPhone phone) {
        HwTelephonyFactory.getHwVolteChrManager().updateCallLog(conn, phone);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_ANSWER_RESULT_CHECK /*1*/:
                Rlog.d(TAG, "EVENT_ANSWER_RESULT_CHECK");
                if (this.mImsPhoneCallTracker.mRingingCall.isRinging()) {
                    HwTelephonyFactory.getHwVolteChrManager().triggerAnswerFailedEvent(this.mImsPhoneCallTracker.mRingingCall.getState().ordinal());
                }
            default:
        }
    }
}
