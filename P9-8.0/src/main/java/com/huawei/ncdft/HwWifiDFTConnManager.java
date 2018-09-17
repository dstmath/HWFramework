package com.huawei.ncdft;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.List;

public class HwWifiDFTConnManager extends Handler {
    private static final int DOMAIN_WIFI = 1;
    private static final int EVENT_REPORT_EXCEPTION = 1;
    private static final String TAG = "HwWifiDFTConnManager";
    private static final int WIFI_STAT_STATIC_EVENT = 50;
    private static HwNcDftConnManager mClient;
    private static Context mContext;
    private static HwWifiDFTConnManager sInstance;
    private static final Object sLock = new Object();

    private HwWifiDFTConnManager(Looper looper) {
        super(looper);
    }

    /* JADX WARNING: Missing block: B:12:0x0020, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void init(Context context) {
        synchronized (sLock) {
            if (context == null) {
                Log.d(TAG, "HwWifiDFTConnManager init, context is null!");
            } else if (mContext == null) {
                mContext = context;
                mClient = new HwNcDftConnManager(mContext);
            } else if (mContext != context) {
                Log.d(TAG, "Detect difference context while do init");
            }
        }
    }

    public static HwWifiDFTConnManager getInstance() {
        HwWifiDFTConnManager hwWifiDFTConnManager;
        synchronized (sLock) {
            if (sInstance == null) {
                Log.d(TAG, "start HwWifiDFTConnManager init  !");
                HandlerThread thread = new HandlerThread(TAG);
                thread.start();
                sInstance = new HwWifiDFTConnManager(thread.getLooper());
            }
            hwWifiDFTConnManager = sInstance;
        }
        return hwWifiDFTConnManager;
    }

    public void reportWifiDFTEvent(int ncEventID, List<String> list) {
        if (mClient != null) {
            mClient.reportToDft(1, ncEventID, list);
        } else {
            Log.e(TAG, "reportWifiDFTEvent,mClient is null");
        }
    }

    public void triggerWifiUpload(int event, int errorCode) {
        if (mClient != null) {
            mClient.triggerUpload(1, event, errorCode);
        } else {
            Log.e(TAG, "triggerWifiUpload,mClient is null");
        }
    }

    public boolean isCommercialUser() {
        if (mClient != null) {
            return mClient.isCommercialUser();
        }
        Log.e(TAG, "isCommercialUser,mClient is null");
        return true;
    }

    public void handleMessage(Message msg) {
        if (msg == null) {
            Log.d(TAG, "msg is null, return");
            return;
        }
        Log.d(TAG, "The event is: " + msg.what);
        int i = msg.what;
    }
}
