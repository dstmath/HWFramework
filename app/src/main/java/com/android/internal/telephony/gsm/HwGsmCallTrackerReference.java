package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RegistrantListUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker.GsmCallTrackerReference;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwGsmCallTrackerReference extends Handler implements GsmCallTrackerReference {
    protected static final int EVENT_SWITCH_CALLBACKGROUND_STATE = 200;
    protected static final int SWITCH_VOICEBG_STATE_TIMER = 10;
    private static final String TAG = "HwGsmCallTrackerReference";
    private static boolean mIsLocalHangupSpeedUp;
    private GsmCdmaCallTracker mGsmCallTracker;

    private class SwitchVoiceBgStateHandler extends Handler {
        static final int EVENT_SWITCH_VOCIE_BG_STATE_DONE = 0;
        private AtomicBoolean mIsFinished;

        SwitchVoiceBgStateHandler(Looper looper) {
            super(looper);
            this.mIsFinished = new AtomicBoolean(false);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                    Rlog.d(HwGsmCallTrackerReference.TAG, "EVENT_SWITCH_VOCIE_BG_STATE_DONE");
                    AsyncResult ar = msg.obj;
                    Object result = ar.userObj;
                    synchronized (result) {
                        if (ar.exception != null) {
                            Rlog.d(HwGsmCallTrackerReference.TAG, "ar.exception" + ar.exception);
                        }
                        result.notifyAll();
                        break;
                    }
                    this.mIsFinished.set(true);
                    HwGsmCallTrackerReference.this.sendMessage(HwGsmCallTrackerReference.this.obtainMessage(HwGsmCallTrackerReference.EVENT_SWITCH_CALLBACKGROUND_STATE));
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
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwGsmCallTrackerReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwGsmCallTrackerReference.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwGsmCallTrackerReference.<clinit>():void");
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
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_SWITCH_CALLBACKGROUND_STATE /*200*/:
                Rlog.d(TAG, "EVENT_SWITCH_CALLBACKGROUND_STATE");
                CallManager.getInstance().onSwitchToOtherActiveSub(this.mGsmCallTracker.mPhone);
            default:
        }
    }
}
