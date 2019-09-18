package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.server.CUPOperateService;
import com.huawei.wallet.sdk.common.log.LogC;
import java.util.ArrayList;

public final class BankCardOperateServiceManager {
    private static final Object M_WAKE_LOCK_SYNC = new Object();
    private static final String TAG = "BankCardOperateServiceManager|";
    private static final int WAKE_LOCK_TIMEOUT = 120000;
    private static PowerManager.WakeLock lostServiceWakeLock;
    private static String processPrefix = "";
    private static String walletProcessStartTimestamp = "";

    private BankCardOperateServiceManager() {
    }

    public static void startCUPOperateService(Context context, String event, String ssid, String sign, ArrayList<String> refIds) {
        acquireWakeLock(context);
        Intent intent = new Intent();
        intent.putExtra("event", event);
        intent.putExtra("ssid", ssid);
        intent.putExtra("sign", sign);
        intent.putStringArrayListExtra(CUPOperateService.SERVICE_INTENT_KEY_REFIDS, refIds);
        intent.setClass(context, CUPOperateService.class);
        intent.setAction(CUPOperateService.SERVICE_ACTION_CUP_OPERATE);
        LogC.i(processPrefix + "Start CUPOperateService for event " + event, false);
        CUPOperateService.getInstance(context).startCommand(intent);
    }

    private static void acquireWakeLock(Context context) {
        synchronized (M_WAKE_LOCK_SYNC) {
            if (lostServiceWakeLock == null) {
                LogC.i("BankCardOperateServiceManager acquireWakeLock, lostServiceWakeLock is null ,wake lock now.", false);
                lostServiceWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "beginWakeLock");
                lostServiceWakeLock.setReferenceCounted(false);
            } else {
                LogC.i("BankCardOperateServiceManager acquireWakeLock, lostServiceWakeLock not null .", false);
            }
            lostServiceWakeLock.acquire(120000);
            LogC.i("BankCardOperateServiceManager acquireWakeLock,  lock has been wake. WAKE_LOCK_TIMEOUT= 120000", false);
        }
    }

    public static void releaseOperateWakeLock() {
        LogC.d("releaseLostTaskWakeLock", false);
        synchronized (M_WAKE_LOCK_SYNC) {
            if (lostServiceWakeLock != null) {
                LogC.d("release the wake lock now.", false);
                if (lostServiceWakeLock.isHeld()) {
                    lostServiceWakeLock.release();
                    LogC.i("BankCardOperateServiceManager releaseOperateWakeLock, lostServiceWakeLock release. WAKE_LOCK_TIMEOUT= 120000", false);
                } else {
                    LogC.i("BankCardOperateServiceManager releaseOperateWakeLock, lostServiceWakeLock not held. ", false);
                }
                lostServiceWakeLock = null;
            } else {
                LogC.i("BankCardOperateServiceManager releaseOperateWakeLock, lostServiceWakeLock is null. ", false);
            }
        }
    }

    public static void setProcessPrefix(String processPre) {
        if (!TextUtils.isEmpty(processPre)) {
            String[] splits = processPre.split("\\|");
            if (splits.length > 1) {
                walletProcessStartTimestamp = splits[1];
            }
            processPrefix = processPre + TAG;
        }
    }

    public static void resetProcessPrefix() {
        processPrefix = "";
        walletProcessStartTimestamp = "";
    }
}
