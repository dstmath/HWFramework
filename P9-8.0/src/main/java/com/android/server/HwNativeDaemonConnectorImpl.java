package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.telephony.Rlog;

public class HwNativeDaemonConnectorImpl implements HwNativeDaemonConnector {
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final boolean DEBUG = false;
    private static final String TAG = "HwNativeDaemonConnector";
    private static volatile HwNativeDaemonConnectorImpl mInstance = null;
    private Context mContext;

    public static synchronized HwNativeDaemonConnectorImpl getInstance() {
        HwNativeDaemonConnectorImpl hwNativeDaemonConnectorImpl;
        synchronized (HwNativeDaemonConnectorImpl.class) {
            if (mInstance == null) {
                mInstance = new HwNativeDaemonConnectorImpl();
            }
            hwNativeDaemonConnectorImpl = mInstance;
        }
        return hwNativeDaemonConnectorImpl;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    private HwNativeDaemonConnectorImpl() {
        Rlog.d(TAG, "HwNativeDaemonConnectorImpl");
    }

    public void reportChrForAddRouteFail(String cmd, String receiveEvent) {
        if (cmd != null && cmd.indexOf("route add") >= 0 && cmd.indexOf("rmnet") >= 0) {
            if (receiveEvent == null || (receiveEvent.indexOf("success") < 0 && receiveEvent.indexOf("Network is unreachable") < 0 && receiveEvent.indexOf("No route to host") < 0)) {
                Rlog.d(TAG, "Add router failed");
                this.mContext.sendBroadcast(new Intent("com.android.intent.action.add_router_fail"), CHR_BROADCAST_PERMISSION);
            }
        }
    }
}
