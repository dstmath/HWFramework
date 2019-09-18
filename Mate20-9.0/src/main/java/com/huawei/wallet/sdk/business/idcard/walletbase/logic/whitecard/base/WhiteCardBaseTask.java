package com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.common.WhiteCardOperatorApi;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;

public abstract class WhiteCardBaseTask implements Runnable {
    private static final int WAKE_LOCK_TIMEOUT = 600000;
    private String clsName = getTaskName();
    protected Context mContext;
    protected WhiteCardOperatorApi operator;
    protected String passTypeId;
    private PowerManager.WakeLock trafficCardTaskWakeLock;
    private final Object wakeLockSync = new Object();

    /* access modifiers changed from: protected */
    public abstract void excuteAction(IssuerInfoItem issuerInfoItem);

    /* access modifiers changed from: protected */
    public abstract String getTaskName();

    public WhiteCardBaseTask(Context mContext2, WhiteCardOperatorApi operator2, String passTypeId2) {
        this.mContext = mContext2;
        this.operator = operator2;
        this.passTypeId = passTypeId2;
    }

    public void run() {
        LogX.i(this.clsName + " run begin");
        excuteAction(null);
        LogX.i(this.clsName + " run end");
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"InvalidWakeLockTag"})
    public void acquireTrafficCardTaskWakelock() {
        LogX.i(this.clsName + " acquireTrafficCardTaskWakelock ");
        synchronized (this.wakeLockSync) {
            if (this.trafficCardTaskWakeLock == null) {
                LogX.i(this.clsName + " acquireTrafficCardTaskWakelock, trafficCardTaskWakeLock is null ,wake lock now.");
                this.trafficCardTaskWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "trafficCardTaskWakeLock");
                this.trafficCardTaskWakeLock.setReferenceCounted(true);
            } else {
                LogX.i(this.clsName + " acquireTrafficCardTaskWakelock, trafficCardTaskWakeLock not null .");
            }
            if (!this.trafficCardTaskWakeLock.isHeld()) {
                this.trafficCardTaskWakeLock.acquire(600000);
                LogX.i(this.clsName + " acquireTrafficCardTaskWakelock, lock has been wake. WAKE_LOCK_TIMEOUT= " + WAKE_LOCK_TIMEOUT);
            } else {
                LogX.i(this.clsName + " acquireTrafficCardTaskWakelock, trafficCardTaskWakeLock not held .");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void releaseTrafficCardTaskWakelock() {
        LogX.i(this.clsName + " releaseTrafficCardTaskWakelock");
        synchronized (this.wakeLockSync) {
            if (this.trafficCardTaskWakeLock != null) {
                LogX.d("release the wake lock now.");
                if (this.trafficCardTaskWakeLock.isHeld()) {
                    this.trafficCardTaskWakeLock.release();
                    LogX.i(this.clsName + " releaseTrafficCardTaskWakelock, trafficCardTaskWakeLock release. WAKE_LOCK_TIMEOUT= " + WAKE_LOCK_TIMEOUT);
                } else {
                    LogX.i(this.clsName + " releaseTrafficCardTaskWakelock, trafficCardTaskWakeLock not held. ");
                }
                this.trafficCardTaskWakeLock = null;
            } else {
                LogX.i(this.clsName + " releaseTrafficCardTaskWakelock, trafficCardTaskWakeLock is null. ");
            }
        }
    }
}
