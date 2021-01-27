package com.huawei.internal.telephony;

import android.os.Handler;
import com.android.internal.telephony.CallTracker;
import com.huawei.internal.telephony.PhoneConstantsExt;

public class CallTrackerEx {
    private CallTracker mCallTracker;

    public static CallTrackerEx getCallTrackerEx(Object callTracker) {
        CallTrackerEx callTrackerEx = new CallTrackerEx();
        if (callTracker instanceof CallTracker) {
            callTrackerEx.setCallTracker((CallTracker) callTracker);
        }
        return callTrackerEx;
    }

    public CallTracker getCallTracker() {
        return this.mCallTracker;
    }

    public void setCallTracker(CallTracker callTracker) {
        this.mCallTracker = callTracker;
    }

    public PhoneConstantsExt.StateEx getState() {
        CallTracker callTracker = this.mCallTracker;
        if (callTracker == null) {
            return null;
        }
        return PhoneConstantsExt.StateEx.getStateExByState(callTracker.getState());
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        CallTracker callTracker = this.mCallTracker;
        if (callTracker != null) {
            callTracker.registerForVoiceCallStarted(h, what, obj);
        }
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        CallTracker callTracker = this.mCallTracker;
        if (callTracker != null) {
            callTracker.unregisterForVoiceCallStarted(h);
        }
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        CallTracker callTracker = this.mCallTracker;
        if (callTracker != null) {
            callTracker.registerForVoiceCallEnded(h, what, obj);
        }
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        CallTracker callTracker = this.mCallTracker;
        if (callTracker != null) {
            callTracker.unregisterForVoiceCallEnded(h);
        }
    }
}
