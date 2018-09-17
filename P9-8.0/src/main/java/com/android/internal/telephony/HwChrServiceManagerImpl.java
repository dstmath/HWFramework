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
    private static HwChrServiceManager mInstance = new HwChrServiceManagerImpl();
    private boolean IS_CHR_CALL_TRACKER_ENABLED = SystemProperties.getBoolean("ro.config.chr_call_tracker", true);
    private CallExceptionHandler mCallExceptionHandler;
    private HandlerThread mHandlerThread = new HandlerThread("CallExceptionHandler");

    private static class CallExceptionHandler extends Handler {
        public CallExceptionHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    CallExceptionInfo info = msg.obj;
                    try {
                        Log.e(HwChrServiceManagerImpl.LOG_TAG, "Run the code of call IChrCallFlowTracker");
                        Stub.asInterface(ServiceManager.getService("chr_service")).reportCallException(info.mAppName, info.mSubId, info.mCallType, info.mParams, info.mTimestamp);
                        return;
                    } catch (RemoteException e) {
                        Rlog.e(HwChrServiceManagerImpl.LOG_TAG, "Happen RemoteException.");
                        return;
                    } catch (NullPointerException e2) {
                        Rlog.e(HwChrServiceManagerImpl.LOG_TAG, "Happen NullPointerException.");
                        Rlog.e(HwChrServiceManagerImpl.LOG_TAG, "e = " + e2);
                        return;
                    }
                default:
                    return;
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

    public static HwChrServiceManager getDefault() {
        return mInstance;
    }

    public HwChrServiceManagerImpl() {
        this.mHandlerThread.start();
        this.mCallExceptionHandler = new CallExceptionHandler(this.mHandlerThread.getLooper());
    }

    public void reportCallException(String appName, int subId, int callType, String params) {
        if (this.IS_CHR_CALL_TRACKER_ENABLED) {
            this.mCallExceptionHandler.obtainMessage(0, new CallExceptionInfo(appName, subId, callType, params, System.nanoTime())).sendToTarget();
            Rlog.e(LOG_TAG, "reportCallException, ro.config.chr_call_tracker is true.");
            return;
        }
        Rlog.e(LOG_TAG, "reportCallException, ro.config.chr_call_tracker is false, return.");
    }
}
