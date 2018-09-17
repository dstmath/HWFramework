package com.android.internal.telephony;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.Log;
import com.huawei.chr.IHwChrService.Stub;

public class HwChrServiceManagerImpl implements HwChrServiceManager {
    private static final int EVENT_REPORT_CALL_EXCEPTION_MSG = 0;
    public static final String LOG_TAG = "HwChrServiceManager";
    private static HwChrServiceManager mInstance;
    private boolean IS_CHR_CALL_TRACKER_ENABLED;
    private CallExceptionHandler mCallExceptionHandler;
    private HandlerThread mHandlerThread;

    private static class CallExceptionHandler extends Handler {
        public CallExceptionHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwChrServiceManagerImpl.EVENT_REPORT_CALL_EXCEPTION_MSG /*0*/:
                    CallExceptionInfo info = msg.obj;
                    try {
                        Log.e(HwChrServiceManagerImpl.LOG_TAG, "Run the code of call IChrCallFlowTracker");
                        Stub.asInterface(ServiceManager.getService("chr_service")).reportCallException(info.mAppName, info.mSubId, info.mCallType, info.mParams, info.mTimestamp);
                    } catch (RemoteException e) {
                        Rlog.e(HwChrServiceManagerImpl.LOG_TAG, "Happen RemoteException.");
                    } catch (NullPointerException e2) {
                        Rlog.e(HwChrServiceManagerImpl.LOG_TAG, "Happen NullPointerException.");
                    }
                default:
            }
        }
    }

    private static class CallExceptionInfo {
        String mAppName;
        int mCallType;
        String mParams;
        int mSubId;
        long mTimestamp;

        public CallExceptionInfo(String appName, int subId, int callType, String params, long timestamp) {
            this.mAppName = appName;
            this.mSubId = subId;
            this.mCallType = callType;
            this.mParams = params;
            this.mTimestamp = timestamp;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwChrServiceManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwChrServiceManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwChrServiceManagerImpl.<clinit>():void");
    }

    public static HwChrServiceManager getDefault() {
        return mInstance;
    }

    public HwChrServiceManagerImpl() {
        this.IS_CHR_CALL_TRACKER_ENABLED = SystemProperties.getBoolean("ro.config.chr_call_tracker", false);
        this.mHandlerThread = new HandlerThread("CallExceptionHandler");
        this.mHandlerThread.start();
        this.mCallExceptionHandler = new CallExceptionHandler(this.mHandlerThread.getLooper());
    }

    public void reportCallException(String appName, int subId, int callType, String params) {
        if (this.IS_CHR_CALL_TRACKER_ENABLED) {
            this.mCallExceptionHandler.obtainMessage(EVENT_REPORT_CALL_EXCEPTION_MSG, new CallExceptionInfo(appName, subId, callType, params, System.nanoTime())).sendToTarget();
            Rlog.e(LOG_TAG, "reportCallException, ro.config.chr_call_tracker is true.");
            return;
        }
        Rlog.e(LOG_TAG, "reportCallException, ro.config.chr_call_tracker is false, return.");
    }
}
