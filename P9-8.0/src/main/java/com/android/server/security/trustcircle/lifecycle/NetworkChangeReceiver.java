package com.android.server.security.trustcircle.lifecycle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Binder;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status.ExceptionStep;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final long RETRY_IOT_TIME_INTERVAL = 120000;
    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();
    private static final long UPDATE_TIME_INTERVAL = 7200000;
    private static volatile long lastIotRetryTime = System.currentTimeMillis();
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
            } else if (State.CONNECTED != info.getState() || !info.isAvailable()) {
                LogHelper.i(TAG, getConnectionType(info.getType()) + " disconnected");
            } else if (info.getType() == 1 || info.getType() == 0) {
                LogHelper.i(TAG, getConnectionType(info.getType()) + " connected");
                long currentTime = System.currentTimeMillis();
                if (ExceptionStep.NO_EXCEPTION.ordinal() != LifeCycleProcessor.getExceptionStepOfCurrentUserId() || currentTime - lastNetworkAvailableTime > UPDATE_TIME_INTERVAL) {
                    if (LifeCycleProcessor.startTcisService(context) == null) {
                        LogHelper.e(TAG, "error: could not start tcis service");
                    } else {
                        updateTime(currentTime);
                    }
                }
            }
        } else {
            LogHelper.e(TAG, "error: receive unexpected intent " + intent.getAction());
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

    public static void requestApkServiceLogin(Context context) {
        NetworkInfo network = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (network != null && (network.isConnected() ^ 1) == 0) {
            LogHelper.d(TAG, "iot exception request tcis service");
            synchronized (NetworkChangeReceiver.class) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastIotRetryTime > RETRY_IOT_TIME_INTERVAL) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        if (LifeCycleProcessor.startTcisService(context) == null) {
                            LogHelper.e(TAG, "error: could not start tcis service");
                        }
                        Binder.restoreCallingIdentity(token);
                    } catch (Exception e) {
                        LogHelper.e(TAG, "exception start service");
                        Binder.restoreCallingIdentity(token);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token);
                    }
                    lastIotRetryTime = currentTime;
                }
            }
            return;
        }
        return;
    }
}
