package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RegistrantListUtils;
import android.os.SystemProperties;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.huawei.android.telephony.RlogEx;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwGsmCallTrackerReference extends Handler implements AbstractGsmCdmaCallTracker.GsmCallTrackerReference {
    protected static final int EVENT_SWITCH_CALLBACKGROUND_STATE = 200;
    protected static final int SWITCH_VOICEBG_STATE_TIMER = 10;
    private static final String TAG = "HwGsmCallTrackerReference";
    private static boolean mIsLocalHangupSpeedUp = SystemProperties.get("ro.config.hw_hangup_speedup", "true").equals("true");
    private GsmCdmaCallTracker mGsmCallTracker;

    public HwGsmCallTrackerReference(GsmCdmaCallTracker gsmCallTracker) {
        this.mGsmCallTracker = gsmCallTracker;
    }

    public boolean notifyRegistrantsDelayed() {
        if (!mIsLocalHangupSpeedUp) {
            return true;
        }
        RegistrantListUtils.notifyRegistrantsDelayed(this.mGsmCallTracker.getmVoiceCallEndedRegistrantsHw(), new AsyncResult((Object) null, (Object) null, (Throwable) null), 500);
        return false;
    }

    private class SwitchVoiceBgStateHandler extends Handler {
        static final int EVENT_SWITCH_VOCIE_BG_STATE_DONE = 0;
        private AtomicBoolean mIsFinished = new AtomicBoolean(false);

        SwitchVoiceBgStateHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 0) {
                getLooper().quit();
                return;
            }
            RlogEx.i(HwGsmCallTrackerReference.TAG, "EVENT_SWITCH_VOCIE_BG_STATE_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            Object result = ar.userObj;
            synchronized (result) {
                if (ar.exception != null) {
                    RlogEx.i(HwGsmCallTrackerReference.TAG, "ar.exception EVENT_SWITCH_VOCIE_BG_STATE_DONE");
                }
                result.notifyAll();
            }
            this.mIsFinished.set(true);
            HwGsmCallTrackerReference hwGsmCallTrackerReference = HwGsmCallTrackerReference.this;
            hwGsmCallTrackerReference.sendMessage(hwGsmCallTrackerReference.obtainMessage(HwGsmCallTrackerReference.EVENT_SWITCH_CALLBACKGROUND_STATE));
            getLooper().quit();
        }

        public boolean isFinished() {
            return this.mIsFinished.get();
        }
    }

    public void switchVoiceCallBackgroundState(int state) {
        RlogEx.i(TAG, "switchVoiceCallBackgroundState:" + state);
        HandlerThread ht = new HandlerThread("switchVoiceCallBackground");
        ht.start();
        SwitchVoiceBgStateHandler handler = new SwitchVoiceBgStateHandler(ht.getLooper());
        Object result = new Object();
        int cnt = 0;
        Message msg = handler.obtainMessage(0, result);
        synchronized (result) {
            this.mGsmCallTracker.mCi.switchVoiceCallBackgroundState(state, msg);
            while (!handler.isFinished() && cnt < 3) {
                try {
                    cnt++;
                    result.wait(10);
                } catch (InterruptedException e) {
                    RlogEx.e(TAG, "occur Interrupted exception.");
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        if (msg.what == EVENT_SWITCH_CALLBACKGROUND_STATE) {
            RlogEx.i(TAG, "EVENT_SWITCH_CALLBACKGROUND_STATE");
            CallManager.getInstance().onSwitchToOtherActiveSub(this.mGsmCallTracker.getmPhoneHw());
        }
    }
}
