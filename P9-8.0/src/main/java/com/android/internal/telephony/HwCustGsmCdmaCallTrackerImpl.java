package com.android.internal.telephony;

import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.metrics.TelephonyMetrics;

public class HwCustGsmCdmaCallTrackerImpl extends HwCustGsmCdmaCallTracker {
    public static final String LOG_TAG = "HwCustGsmCdmaCallTrackerImpl";
    protected static final boolean REJCALL_RINGING_REJECT = SystemProperties.getBoolean("ro.config.ringing_reject", false);
    private int mRejectCause = -1;

    public void rejectCallForCause(CommandsInterface ci, GsmCdmaCall ringCall, Message message) {
        if (ringCall != null && (ringCall.isRinging() ^ 1) == 0) {
            Rlog.d(LOG_TAG, "rejectCallForCause, cause:" + this.mRejectCause);
            int count = ringCall.mConnections.size();
            for (int i = 0; i < count; i++) {
                GsmCdmaConnection cn = (GsmCdmaConnection) ringCall.mConnections.get(i);
                try {
                    if (!cn.mDisconnected) {
                        Rlog.d(LOG_TAG, "rejectCallForCause start");
                        TelephonyMetrics.getInstance().writeRilHangup(ringCall.getPhone().getPhoneId(), cn, cn.getGsmCdmaIndex());
                        if (this.mRejectCause == -1) {
                            ci.hangupConnection(cn.getGsmCdmaIndex(), message);
                        } else {
                            ci.rejectCallForCause(cn.getGsmCdmaIndex(), this.mRejectCause, message);
                        }
                    }
                } catch (CallStateException ex) {
                    Rlog.e(LOG_TAG, "hangupConnectionByIndex caught " + ex);
                }
            }
        }
    }

    public int getRejectCallCause(GsmCdmaCall ringCall) {
        if (!REJCALL_RINGING_REJECT) {
            return -1;
        }
        this.mRejectCause = 1;
        return 1;
    }
}
