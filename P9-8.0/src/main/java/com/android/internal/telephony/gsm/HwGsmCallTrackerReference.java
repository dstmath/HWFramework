package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RegistrantListUtils;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker.GsmCallTrackerReference;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.GsmCdmaCallTracker;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwGsmCallTrackerReference extends Handler implements GsmCallTrackerReference {
    protected static final int EVENT_SWITCH_CALLBACKGROUND_STATE = 200;
    protected static final int SWITCH_VOICEBG_STATE_TIMER = 10;
    private static final String TAG = "HwGsmCallTrackerReference";
    private static boolean mIsLocalHangupSpeedUp = SystemProperties.get("ro.config.hw_hangup_speedup", "true").equals("true");
    private GsmCdmaCallTracker mGsmCallTracker;

    private class SwitchVoiceBgStateHandler extends Handler {
        static final int EVENT_SWITCH_VOCIE_BG_STATE_DONE = 0;
        private AtomicBoolean mIsFinished = new AtomicBoolean(false);

        SwitchVoiceBgStateHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Rlog.d(HwGsmCallTrackerReference.TAG, "EVENT_SWITCH_VOCIE_BG_STATE_DONE");
                    AsyncResult ar = msg.obj;
                    Object result = ar.userObj;
                    synchronized (result) {
                        if (ar.exception != null) {
                            Rlog.d(HwGsmCallTrackerReference.TAG, "ar.exception" + ar.exception);
                        }
                        result.notifyAll();
                    }
                    this.mIsFinished.set(true);
                    HwGsmCallTrackerReference.this.sendMessage(HwGsmCallTrackerReference.this.obtainMessage(HwGsmCallTrackerReference.EVENT_SWITCH_CALLBACKGROUND_STATE));
                    getLooper().quit();
                    return;
                default:
                    getLooper().quit();
                    return;
            }
        }

        public boolean isFinished() {
            return this.mIsFinished.get();
        }
    }

    public HwGsmCallTrackerReference(GsmCdmaCallTracker gsmCallTracker) {
        this.mGsmCallTracker = gsmCallTracker;
    }

    public boolean notifyRegistrantsDelayed() {
        if (!mIsLocalHangupSpeedUp) {
            return true;
        }
        RegistrantListUtils.notifyRegistrantsDelayed(this.mGsmCallTracker.mVoiceCallEndedRegistrants, new AsyncResult(null, null, null), 500);
        return false;
    }

    public void switchVoiceCallBackgroundState(int state) {
        Rlog.d(TAG, "switchVoiceCallBackgroundState:" + state);
        HandlerThread ht = new HandlerThread("switchVoiceCallBackground");
        ht.start();
        SwitchVoiceBgStateHandler handler = new SwitchVoiceBgStateHandler(ht.getLooper());
        Object result = new Object();
        Message msg = handler.obtainMessage(0, result);
        synchronized (result) {
            this.mGsmCallTracker.mCi.switchVoiceCallBackgroundState(state, msg);
            int cnt = 0;
            while (!handler.isFinished() && cnt < 3) {
                try {
                    cnt++;
                    result.wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_SWITCH_CALLBACKGROUND_STATE /*200*/:
                Rlog.d(TAG, "EVENT_SWITCH_CALLBACKGROUND_STATE");
                CallManager.getInstance().onSwitchToOtherActiveSub(this.mGsmCallTracker.mPhone);
                return;
            default:
                return;
        }
    }
}
