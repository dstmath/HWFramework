package com.android.internal.telephony.cdma;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RegistrantListUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker.CdmaCallTrackerReference;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.GsmCdmaConnection;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaLineControlInfoRec;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwCdmaCallTrackerReference implements CdmaCallTrackerReference {
    private static final int EVENT_CALL_LINE_CONTROL_INFO = 500;
    protected static final int EVENT_SWITCH_CALLBACKGROUND_STATE = 200;
    private static final String LOG_TAG = "HwCdmaCallTrackerReference";
    protected static final int SWITCH_VOICEBG_STATE_TIMER = 10;
    public static final boolean mIsLocalHangupSpeedUp = false;
    private boolean hasRegisterForLineControlInfo;
    HwCdmaCallTrackerReferenceHandler hwCdmaCallTrackerReferenceHandler;
    RegistrantList lineControlInfoRegistrants;
    private GsmCdmaCallTracker mCdmaCallTracker;

    public class HwCdmaCallTrackerReferenceHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwCdmaCallTrackerReference.EVENT_CALL_LINE_CONTROL_INFO /*500*/:
                    AsyncResult ar = msg.obj;
                    if (ar.exception == null) {
                        HwCdmaCallTrackerReference.this.handleLineControlInfo((CdmaLineControlInfoRec) ar.result);
                        Rlog.i(HwCdmaCallTrackerReference.LOG_TAG, "EVENT_CALL_LINE_CONTROL_INFO Received");
                        return;
                    }
                    Rlog.i(HwCdmaCallTrackerReference.LOG_TAG, "EVENT_CALL_LINE_CONTROL_INFO Received but there is some exception!");
                default:
            }
        }
    }

    private static class SwitchVoiceBgStateHandler extends Handler {
        static final int EVENT_SWITCH_VOCIE_BG_STATE_DONE = 0;
        private AtomicBoolean mIsFinished;

        SwitchVoiceBgStateHandler(Looper looper) {
            super(looper);
            this.mIsFinished = new AtomicBoolean(false);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                    Rlog.d(HwCdmaCallTrackerReference.LOG_TAG, "EVENT_SWITCH_VOCIE_BG_STATE_DONE");
                    AsyncResult ar = msg.obj;
                    Object result = ar.userObj;
                    synchronized (result) {
                        if (ar.exception != null) {
                            Rlog.d(HwCdmaCallTrackerReference.LOG_TAG, "ar.exception" + ar.exception);
                        }
                        result.notifyAll();
                        break;
                    }
                    this.mIsFinished.set(true);
                    getLooper().quit();
                default:
                    getLooper().quit();
            }
        }

        public boolean isFinished() {
            return this.mIsFinished.get();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.HwCdmaCallTrackerReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.HwCdmaCallTrackerReference.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.HwCdmaCallTrackerReference.<clinit>():void");
    }

    public HwCdmaCallTrackerReference(GsmCdmaCallTracker cdmaCallTracker) {
        this.lineControlInfoRegistrants = new RegistrantList();
        this.hasRegisterForLineControlInfo = false;
        this.mCdmaCallTracker = cdmaCallTracker;
        initHandler();
    }

    protected void initHandler() {
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
            this.mCdmaCallTracker.mCi.registerForLineControlInfo(this.hwCdmaCallTrackerReferenceHandler, EVENT_CALL_LINE_CONTROL_INFO, null);
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

    private void handleLineControlInfo(CdmaLineControlInfoRec lineConRec) {
        if (lineConRec.lineCtrlPolarityIncluded == 1) {
            Rlog.i(LOG_TAG, "CdmaCallTracker  lineCtrlPolarityIncluded==1");
            GsmCdmaConnection fgConn = (GsmCdmaConnection) this.mCdmaCallTracker.mForegroundCall.getLatestConnection();
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
        Message msg = handler.obtainMessage(0, result);
        synchronized (result) {
            this.mCdmaCallTracker.mCi.switchVoiceCallBackgroundState(state, msg);
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
        for (Connection conn : conns) {
            if (conn != null && ((GsmCdmaConnection) conn).compareToNumber(number)) {
                ((GsmCdmaConnection) conn).setEncryptCall(val);
            }
        }
    }
}
