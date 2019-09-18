package com.android.server.security.trustcircle.lifecycle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status;
import com.android.server.security.trustcircle.utils.Utils;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();
    private static volatile long lastNetworkAvailableTime = System.currentTimeMillis();

    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) {
            LogHelper.e(TAG, "error: intent or context is null");
            return;
        }
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (info == null) {
                LogHelper.e(TAG, "error: NetworkInfo is null");
            } else if (NetworkInfo.State.CONNECTED != info.getState() || !info.isAvailable()) {
                String str = TAG;
                LogHelper.i(str, getConnectionType(info.getType()) + " disconnected");
            } else if (info.getType() == 1 || info.getType() == 0) {
                String str2 = TAG;
                LogHelper.i(str2, getConnectionType(info.getType()) + " connected");
                long currentTime = System.currentTimeMillis();
                if (!isHwIdAndTaLoginedStateConsistent(context) || Status.ExceptionStep.NO_EXCEPTION.ordinal() != LifeCycleProcessor.getExceptionStepOfCurrentUserId()) {
                    if (LifeCycleProcessor.startTcisService(context) == null) {
                        LogHelper.e(TAG, "error: could not start tcis service");
                    } else {
                        updateTime(currentTime);
                    }
                }
            }
        } else {
            String str3 = TAG;
            LogHelper.e(str3, "error: receive unexpected intent " + intent.getAction());
        }
    }

    private String getConnectionType(int type) {
        if (type == 0) {
            return "mobile network";
        }
        if (type == 1) {
            return "WIFI network";
        }
        return "unknown network";
    }

    public static void updateTime(long time) {
        synchronized (NetworkChangeReceiver.class) {
            lastNetworkAvailableTime = time;
        }
    }

    public static boolean isHwIdAndTaLoginedStateConsistent(Context context) {
        return Utils.hasLoginAccount(context) == ((LifeCycleProcessor.getLoginedUserID() > -1 ? 1 : (LifeCycleProcessor.getLoginedUserID() == -1 ? 0 : -1)) != 0);
    }
}
