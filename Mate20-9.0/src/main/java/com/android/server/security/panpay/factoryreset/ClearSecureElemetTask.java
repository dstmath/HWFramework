package com.android.server.security.panpay.factoryreset;

import android.content.Context;
import android.util.Flog;
import android.util.Log;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public class ClearSecureElemetTask {
    private static final String CLEAR_SE_FAILED = "failed";
    private static final String CLEAR_SE_RETRY = "retry";
    private static final String CLEAR_SE_START = "start";
    private static final String CLEAR_SE_SUCCESS = "success";
    private static final long DELAY_TIME = 3;
    private static final long MAX_TRYTIME = 5;
    private static final long PERIOD_TIME = 10;
    private static final String RESULT = "result";
    private static final String TAG = "ClearSecureElemetTask";
    private final Runnable call;
    private ScheduledFuture future = null;
    private final Context mContext;
    private ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);
    private int times = 0;
    private final WalletTask walletTask;

    public ClearSecureElemetTask(Context context, Runnable callback) {
        this.mContext = context;
        this.walletTask = new WalletTask(context);
        this.call = callback;
    }

    /* access modifiers changed from: private */
    public void deal() {
        if (isWalletDeleting()) {
            Log.d(TAG, "ClearSecureElemetTask WalletisDeleting " + this.times);
        } else if (isWalletDeleteOK()) {
            Log.d(TAG, "ClearSecureElemetTask WalletisDeletOK " + this.times);
            finsh("success");
        } else {
            if (NetworkStatus.isActive(this.mContext)) {
                if (((long) this.times) >= MAX_TRYTIME) {
                    finsh(CLEAR_SE_FAILED);
                    Log.d(TAG, "ClearSecureElemetTask reach max time = " + this.times);
                } else {
                    this.times++;
                    startWallet();
                    Log.d(TAG, "ClearSecureElemetTask start time = " + this.times);
                }
            }
        }
    }

    public void start() {
        Log.d(TAG, "ClearSecureElemetTask start");
        timerStart();
        reportBD(CLEAR_SE_START, true);
    }

    private void finsh(String result) {
        Log.d(TAG, "ClearSecureElemetTask stop and run callback");
        timerStop();
        if (this.call != null) {
            this.call.run();
        }
        reportBD(result + ": " + CLEAR_SE_RETRY + " " + String.valueOf(this.times) + " times", false);
    }

    private synchronized void timerStart() {
        this.times = 0;
        if (this.future == null || this.future.isCancelled()) {
            try {
                Log.d(TAG, "ClearSecureElemetTask timerStart");
                this.future = this.scheduExec.scheduleAtFixedRate(new Runnable() {
                    public final void run() {
                        ClearSecureElemetTask.this.deal();
                    }
                }, 3, PERIOD_TIME, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.e(TAG, "timerStart faild: " + e.getMessage());
            }
        } else {
            return;
        }
        return;
    }

    private void timerStop() {
        Log.d(TAG, "ClearSecureElemetTask stop");
        if (this.future != null) {
            try {
                this.future.cancel(false);
            } catch (Exception e) {
                Log.e(TAG, "stop faild: " + e.getMessage());
            }
        }
    }

    private boolean isWalletDeleting() {
        return this.walletTask.isInProc();
    }

    private boolean isWalletDeleteOK() {
        return this.walletTask.isSucceed();
    }

    private void startWallet() {
        this.walletTask.startProcess();
    }

    public void reportBD(String result, boolean isStart) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(RESULT, result);
            if (isStart) {
                Flog.bdReport(this.mContext, 569, obj.toString());
            } else {
                Flog.bdReport(this.mContext, 570, obj.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "clear SE reportBD faild: " + e.getMessage());
        }
    }
}
