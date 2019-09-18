package com.android.internal.telephony.cdma;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RegistrantListUtils;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwCdmaCallTrackerReference implements AbstractGsmCdmaCallTracker.CdmaCallTrackerReference {
    private static final int EVENT_CALL_LINE_CONTROL_INFO = 500;
    protected static final int EVENT_SWITCH_CALLBACKGROUND_STATE = 200;
    private static final String LOG_TAG = "HwCdmaCallTrackerReference";
    protected static final int SWITCH_VOICEBG_STATE_TIMER = 10;
    public static final boolean mIsLocalHangupSpeedUp = SystemProperties.get("ro.config.hw_hangup_speedup", "true").equals("true");
    private boolean hasRegisterForLineControlInfo = false;
    HwCdmaCallTrackerReferenceHandler hwCdmaCallTrackerReferenceHandler;
    RegistrantList lineControlInfoRegistrants = new RegistrantList();
    private GsmCdmaCallTracker mCdmaCallTracker;

    public class HwCdmaCallTrackerReferenceHandler extends Handler {
        public HwCdmaCallTrackerReferenceHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 500) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    HwCdmaCallTrackerReference.this.handleLineControlInfo((CdmaInformationRecords.CdmaLineControlInfoRec) ar.result);
                    Rlog.i(HwCdmaCallTrackerReference.LOG_TAG, "EVENT_CALL_LINE_CONTROL_INFO Received");
                    return;
                }
                Rlog.i(HwCdmaCallTrackerReference.LOG_TAG, "EVENT_CALL_LINE_CONTROL_INFO Received but there is some exception!");
            }
        }
    }

    private static class SwitchVoiceBgStateHandler extends Handler {
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
            Rlog.d(HwCdmaCallTrackerReference.LOG_TAG, "EVENT_SWITCH_VOCIE_BG_STATE_DONE");
            AsyncResult ar = (AsyncResult) msg.obj;
            Object result = ar.userObj;
            synchronized (result) {
                if (ar.exception != null) {
                    Rlog.d(HwCdmaCallTrackerReference.LOG_TAG, "ar.exception" + ar.exception);
                }
                result.notifyAll();
            }
            this.mIsFinished.set(true);
            getLooper().quit();
        }

        public boolean isFinished() {
            return this.mIsFinished.get();
        }
    }

    public HwCdmaCallTrackerReference(GsmCdmaCallTracker cdmaCallTracker) {
        this.mCdmaCallTracker = cdmaCallTracker;
        initHandler();
    }

    /* access modifiers changed from: protected */
    public void initHandler() {
        if (this.hwCdmaCallTrackerReferenceHandler == null) {
            this.hwCdmaCallTrackerReferenceHandler = new HwCdmaCallTrackerReferenceHandler();
        }
    }

    public void dispose() {
        Rlog.i(LOG_TAG, "CdmaCallTracker unregisterForLineControlInfo!");
        this.mCdmaCallTracker.mCi.unregisterForLineControlInfo(this.hwCdmaCallTrackerReferenceHandler);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        if (!this.hasRegisterForLineControlInfo) {
            Rlog.i(LOG_TAG, "CdmaCallTracker registerForLineControlInfo!");
            this.mCdmaCallTracker.mCi.registerForLineControlInfo(this.hwCdmaCallTrackerReferenceHandler, 500, null);
            this.hasRegisterForLineControlInfo = true;
        }
        Rlog.i(LOG_TAG, "Some one call me  registerForLineControlInfo!");
        this.lineControlInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLineControlInfo(Handler h) {
        Rlog.i(LOG_TAG, "Some one call me  unregisterForLineControlInfo!");
        this.lineControlInfoRegistrants.remove(h);
    }

    private void notifyLineControlInfo() {
        if (this.lineControlInfoRegistrants != null) {
            Rlog.i(LOG_TAG, "CdmaCallTracker  notifyLineControlInfo!");
            this.lineControlInfoRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
    }

    /* access modifiers changed from: private */
    public void handleLineControlInfo(CdmaInformationRecords.CdmaLineControlInfoRec lineConRec) {
        if (lineConRec.lineCtrlPolarityIncluded == 1) {
            Rlog.i(LOG_TAG, "CdmaCallTracker  lineCtrlPolarityIncluded==1");
            GsmCdmaConnection fgConn = this.mCdmaCallTracker.mForegroundCall.getLatestConnection();
            if (fgConn != null) {
                Rlog.i(LOG_TAG, "CdmaCallTracker  there is foreground connection!");
                fgConn.onLineControlInfo();
                notifyLineControlInfo();
                return;
            }
            return;
        }
        Rlog.i(LOG_TAG, "CdmaCallTracker  lineCtrlPolarityIncluded!=1");
    }

    public boolean notifyRegistrantsDelayed() {
        if (!mIsLocalHangupSpeedUp) {
            return true;
        }
        RegistrantListUtils.notifyRegistrantsDelayed(this.mCdmaCallTracker.mVoiceCallEndedRegistrants, new AsyncResult(null, null, null), 500);
        return false;
    }

    public void switchVoiceCallBackgroundState(int state) {
        Rlog.d(LOG_TAG, "switchVoiceCallBackgroundState:" + state);
        HandlerThread ht = new HandlerThread("switchVoiceCallBackground");
        ht.start();
        SwitchVoiceBgStateHandler handler = new SwitchVoiceBgStateHandler(ht.getLooper());
        Object result = new Object();
        int cnt = 0;
        Message msg = handler.obtainMessage(0, result);
        synchronized (result) {
            this.mCdmaCallTracker.mCi.switchVoiceCallBackgroundState(state, msg);
            while (!handler.isFinished() && cnt < 3) {
                try {
                    cnt++;
                    result.wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setConnEncryptCallByNumber(String number, boolean val) {
        Rlog.d(LOG_TAG, "ringingCall  size=" + this.mCdmaCallTracker.mRingingCall.getConnections().size());
        setConnsEncryptCall(this.mCdmaCallTracker.mRingingCall.getConnections(), number, val);
        Rlog.d(LOG_TAG, "foregroundCall  size=" + this.mCdmaCallTracker.mForegroundCall.getConnections().size());
        setConnsEncryptCall(this.mCdmaCallTracker.mForegroundCall.getConnections(), number, val);
        Rlog.d(LOG_TAG, "backgroundCall size=" + this.mCdmaCallTracker.mBackgroundCall.getConnections().size());
        setConnsEncryptCall(this.mCdmaCallTracker.mBackgroundCall.getConnections(), number, val);
    }

    private void setConnsEncryptCall(List<Connection> conns, String number, boolean val) {
        Iterator<Connection> it = conns.iterator();
        while (it.hasNext()) {
            GsmCdmaConnection gsmCdmaConnection = (Connection) it.next();
            if (gsmCdmaConnection != null && gsmCdmaConnection.compareToNumber(number)) {
                gsmCdmaConnection.setEncryptCall(val);
            }
        }
    }
}
