package com.android.internal.telephony;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.chr.IHwChrService;

public class HwChrServiceManagerImpl extends DefaultHwChrServiceManager {
    private static final int EVENT_REPORT_CALL_EXCEPTION_MSG = 0;
    private static final String LOG_TAG = "HwChrServiceManager";
    private static HwChrServiceManager sInstance = new HwChrServiceManagerImpl();
    private boolean isChrCallTrackerEnabled = SystemPropertiesEx.getBoolean("ro.config.chr_call_tracker", true);
    private CallExceptionHandler mCallExceptionHandler;
    private HandlerThread mHandlerThread = new HandlerThread("CallExceptionHandler");

    public HwChrServiceManagerImpl() {
        this.mHandlerThread.start();
        Looper looper = this.mHandlerThread.getLooper();
        if (looper != null) {
            this.mCallExceptionHandler = new CallExceptionHandler(looper);
        }
    }

    public static HwChrServiceManager getDefault() {
        return sInstance;
    }

    private static class CallExceptionHandler extends Handler {
        CallExceptionHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                CallExceptionInfo info = (CallExceptionInfo) msg.obj;
                try {
                    RlogEx.i(HwChrServiceManagerImpl.LOG_TAG, "Run the code of call IChrCallFlowTracker");
                    IHwChrService hwChrService = IHwChrService.Stub.asInterface(ServiceManagerEx.getService("chr_service"));
                    if (info != null) {
                        if (hwChrService != null) {
                            hwChrService.reportCallException(info.mAppName, info.mSubId, info.mCallType, info.mParams, info.mTimestamp);
                            return;
                        }
                    }
                    RlogEx.e(HwChrServiceManagerImpl.LOG_TAG, "info or hwChrService null.");
                } catch (RemoteException e) {
                    RlogEx.e(HwChrServiceManagerImpl.LOG_TAG, "Happen RemoteException.");
                }
            }
        }
    }

    private static class CallExceptionInfo {
        String mAppName;
        int mCallType;
        String mParams;
        int mSubId;
        long mTimestamp;

        CallExceptionInfo(String appName, int subId, int callType, String params, long timestamp) {
            this.mAppName = appName;
            this.mSubId = subId;
            this.mCallType = callType;
            this.mParams = params;
            this.mTimestamp = timestamp;
        }
    }

    public void reportCallException(String appName, int subId, int callType, String params) {
        CallExceptionHandler callExceptionHandler;
        if (!this.isChrCallTrackerEnabled || (callExceptionHandler = this.mCallExceptionHandler) == null) {
            RlogEx.e(LOG_TAG, "reportCallException, ro.config.chr_call_tracker is false, return.");
            return;
        }
        callExceptionHandler.obtainMessage(0, new CallExceptionInfo(appName, subId, callType, params, System.nanoTime())).sendToTarget();
        RlogEx.e(LOG_TAG, "reportCallException, ro.config.chr_call_tracker is true.");
    }
}
