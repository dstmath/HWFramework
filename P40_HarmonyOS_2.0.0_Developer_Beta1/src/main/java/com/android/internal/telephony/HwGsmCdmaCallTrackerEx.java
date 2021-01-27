package com.android.internal.telephony;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.RegistrantEx;
import com.huawei.android.os.RegistrantListEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CallManagerExt;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.GsmCdmaConnectionEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.cdma.CdmaLineControlInfoRecEx;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwGsmCdmaCallTrackerEx extends DefaultHwGsmCdmaCallTrackerEx {
    private static final int CALLENDED_DELAY_NOTIFY_TIMER = 500;
    private static final int EVENT_CALLENDED_DELAY_NOTIFY = 201;
    private static final int EVENT_CALL_LINE_CONTROL_INFO = 202;
    private static final int EVENT_SWITCH_CALLBACKGROUND_STATE = 200;
    private static final boolean IS_LOCAL_HANGUP_SPEEDUP = SystemPropertiesEx.getBoolean("ro.config.hw_hangup_speedup", true);
    private static final int LINE_POLARITY = 1;
    private static final String LOG_TAG = "HwGsmCdmaCallTrackerEx";
    private static final int SWITCH_VOICEBG_STATE_TIMER = 10;
    private boolean hasRegisterForLineControlInfo = false;
    private RegistrantListEx lineControlInfoRegistrants = new RegistrantListEx();
    private CommandsInterfaceEx mCi;
    private IGsmCdmaCallTrackerInner mGsmCdmaCallTracker;
    private MyHandler mHandler;
    private PhoneExt mPhone;

    public HwGsmCdmaCallTrackerEx(IGsmCdmaCallTrackerInner gsmCdmaCallTrackerInner) {
        this.mGsmCdmaCallTracker = gsmCdmaCallTrackerInner;
        this.mPhone = gsmCdmaCallTrackerInner.getPhoneHw();
        this.mCi = this.mPhone.getCi();
        this.mHandler = new MyHandler();
    }

    public void dispose() {
        RlogEx.i(LOG_TAG, "CdmaCallTracker unregisterForLineControlInfo!");
        this.mCi.unregisterForLineControlInfo(this.mHandler);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        if (!this.hasRegisterForLineControlInfo) {
            RlogEx.i(LOG_TAG, "CdmaCallTracker registerForLineControlInfo!");
            this.mCi.registerForLineControlInfo(this.mHandler, (int) EVENT_CALL_LINE_CONTROL_INFO, (Object) null);
            this.hasRegisterForLineControlInfo = true;
        }
        RlogEx.i(LOG_TAG, "Some one call me  registerForLineControlInfo!");
        this.lineControlInfoRegistrants.add(new RegistrantEx(h, what, obj));
    }

    public void unregisterForLineControlInfo(Handler h) {
        RlogEx.i(LOG_TAG, "Some one call me  unregisterForLineControlInfo!");
        this.lineControlInfoRegistrants.remove(h);
    }

    private void notifyLineControlInfo() {
        if (this.lineControlInfoRegistrants != null) {
            RlogEx.i(LOG_TAG, "CdmaCallTracker  notifyLineControlInfo!");
            this.lineControlInfoRegistrants.notifyRegistrants((Object) null, (Object) null, (Throwable) null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLineControlInfo(CdmaLineControlInfoRecEx lineConRec) {
        if (lineConRec == null || lineConRec.getlineCtrlPolarityIncluded() != 1) {
            RlogEx.i(LOG_TAG, "CdmaCallTracker  lineCtrlPolarityIncluded!=1");
            return;
        }
        RlogEx.i(LOG_TAG, "CdmaCallTracker  lineCtrlPolarityIncluded==1");
        GsmCdmaConnectionEx fgConn = this.mGsmCdmaCallTracker.getForegroundCallLatestConnection();
        if (fgConn != null) {
            RlogEx.i(LOG_TAG, "CdmaCallTracker  there is foreground connection!");
            fgConn.onLineControlInfo();
            notifyLineControlInfo();
        }
    }

    public boolean notifyRegistrantsDelayed() {
        if (!IS_LOCAL_HANGUP_SPEEDUP) {
            return true;
        }
        this.mHandler.sendEmptyMessageDelayed(EVENT_CALLENDED_DELAY_NOTIFY, 500);
        return false;
    }

    public void switchVoiceCallBackgroundState(int state) {
        RlogEx.i(LOG_TAG, "switchVoiceCallBackgroundState:" + state);
        HandlerThread ht = new HandlerThread("switchVoiceCallBackground");
        ht.start();
        SwitchVoiceBgStateHandler handler = new SwitchVoiceBgStateHandler(ht.getLooper());
        Object result = new Object();
        int cnt = 0;
        Message msg = handler.obtainMessage(0, result);
        synchronized (result) {
            if (this.mCi != null) {
                this.mCi.switchVoiceCallBackgroundState(state, msg);
            } else {
                msg.sendToTarget();
            }
            while (!handler.isFinished() && cnt < 3) {
                try {
                    cnt++;
                    result.wait(10);
                } catch (InterruptedException e) {
                    RlogEx.e(LOG_TAG, "occur Interrupted exception.");
                }
            }
        }
    }

    public void setConnEncryptCallByNumber(String number, boolean val) {
        List<GsmCdmaConnectionEx> ringingCallConnections = this.mGsmCdmaCallTracker.getRingingCallConnections();
        RlogEx.i(LOG_TAG, "ringingCall  size=" + ringingCallConnections.size());
        setConnsEncryptCall(ringingCallConnections, number, val);
        List<GsmCdmaConnectionEx> foregroundCallConnections = this.mGsmCdmaCallTracker.getForegroundCallConnections();
        RlogEx.i(LOG_TAG, "foregroundCall  size=" + foregroundCallConnections.size());
        setConnsEncryptCall(foregroundCallConnections, number, val);
        List<GsmCdmaConnectionEx> backgroundCallConnections = this.mGsmCdmaCallTracker.getBackgroundCallConnections();
        RlogEx.i(LOG_TAG, "backgroundCall size=" + backgroundCallConnections.size());
        setConnsEncryptCall(backgroundCallConnections, number, val);
    }

    private void setConnsEncryptCall(List<GsmCdmaConnectionEx> connectionList, String number, boolean val) {
        for (GsmCdmaConnectionEx gsmCdmaConnectionEx : connectionList) {
            if (gsmCdmaConnectionEx != null && gsmCdmaConnectionEx.compareToNumber(number)) {
                gsmCdmaConnectionEx.setEncryptCall(val);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logi(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private class SwitchVoiceBgStateHandler extends Handler {
        static final int EVENT_SWITCH_VOCIE_BG_STATE_DONE = 0;
        private AtomicBoolean mIsFinished = new AtomicBoolean(false);

        SwitchVoiceBgStateHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 0) {
                getLooper().quit();
                return;
            }
            RlogEx.i(HwGsmCdmaCallTrackerEx.LOG_TAG, "EVENT_SWITCH_VOCIE_BG_STATE_DONE");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar != null) {
                Object result = ar.getUserObj();
                synchronized (result) {
                    if (ar.getException() != null) {
                        RlogEx.i(HwGsmCdmaCallTrackerEx.LOG_TAG, "ar.exception EVENT_SWITCH_VOCIE_BG_STATE_DONE");
                    }
                    result.notifyAll();
                }
            }
            this.mIsFinished.set(true);
            sendMessage(HwGsmCdmaCallTrackerEx.this.mHandler.obtainMessage(HwGsmCdmaCallTrackerEx.EVENT_SWITCH_CALLBACKGROUND_STATE));
            getLooper().quit();
        }

        public boolean isFinished() {
            return this.mIsFinished.get();
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwGsmCdmaCallTrackerEx.EVENT_SWITCH_CALLBACKGROUND_STATE /* 200 */:
                    HwGsmCdmaCallTrackerEx.this.logi("EVENT_SWITCH_CALLBACKGROUND_STATE");
                    CallManagerExt.onSwitchToOtherActiveSub(HwGsmCdmaCallTrackerEx.this.mPhone);
                    return;
                case HwGsmCdmaCallTrackerEx.EVENT_CALLENDED_DELAY_NOTIFY /* 201 */:
                    HwGsmCdmaCallTrackerEx.this.logi("EVENT_CALLENDED_DELAY_NOTIFY");
                    HwGsmCdmaCallTrackerEx.this.mGsmCdmaCallTracker.voiceCallEndedRegistrantsNotifyHw();
                    return;
                case HwGsmCdmaCallTrackerEx.EVENT_CALL_LINE_CONTROL_INFO /* 202 */:
                    AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                    if (ar.getException() == null) {
                        HwGsmCdmaCallTrackerEx.this.handleLineControlInfo(CdmaLineControlInfoRecEx.from(ar.getResult()));
                        RlogEx.i(HwGsmCdmaCallTrackerEx.LOG_TAG, "EVENT_CALL_LINE_CONTROL_INFO Received");
                        return;
                    }
                    RlogEx.i(HwGsmCdmaCallTrackerEx.LOG_TAG, "EVENT_CALL_LINE_CONTROL_INFO Received but there is some exception!");
                    return;
                default:
                    return;
            }
        }
    }
}
