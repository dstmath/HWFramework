package com.huawei.wallet.sdk.business.idcard.accesscard.logic.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.idcard.accesscard.api.AccessCardOperator;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.exception.AccessCardOperatorException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.NfcUtil;

public abstract class AccessCardBaseTask implements Runnable {
    private static final int WAKE_LOCK_TIMEOUT = 600000;
    private PowerManager.WakeLock accessCardTaskWakeLock;
    private String clsName = getTaskName();
    protected Context mContext;
    protected String mIssuerId;
    protected AccessCardOperator operator;
    private final Object wakeLockSync = new Object();

    /* access modifiers changed from: protected */
    public abstract void excuteAction(IssuerInfoItem issuerInfoItem);

    /* access modifiers changed from: protected */
    public abstract String getTaskName();

    public AccessCardBaseTask(Context mContext2, AccessCardOperator operator2, String mIssuerId2) {
        this.mContext = mContext2;
        this.mIssuerId = mIssuerId2;
        this.operator = operator2;
    }

    public void run() {
        LogX.i(this.clsName + " run begin");
        excuteAction(new IssuerInfoItem());
        LogX.i(this.clsName + " run end");
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"InvalidWakeLockTag"})
    public void acquireAccessCardTaskWakelock() {
        LogX.i(this.clsName + " acquireAccessCardTaskWakelock ");
        synchronized (this.wakeLockSync) {
            if (this.accessCardTaskWakeLock == null) {
                LogX.i(this.clsName + " acquireAccessCardTaskWakelock, accessCardTaskWakeLock is null ,wake lock now.");
                this.accessCardTaskWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "accessCardTaskWakeLock");
                this.accessCardTaskWakeLock.setReferenceCounted(true);
            } else {
                LogX.i(this.clsName + " acquireAccessCardTaskWakelock, accessCardTaskWakeLock not null .");
            }
            if (!this.accessCardTaskWakeLock.isHeld()) {
                this.accessCardTaskWakeLock.acquire(600000);
                LogX.i(this.clsName + " acquireAccessCardTaskWakelock, lock has been wake. WAKE_LOCK_TIMEOUT= " + WAKE_LOCK_TIMEOUT);
            } else {
                LogX.i(this.clsName + " acquireAccessCardTaskWakelock, accessCardTaskWakeLock not held .");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void releaseAccessCardTaskWakelock() {
        LogX.i(this.clsName + " releaseAccessCardTaskWakelock");
        synchronized (this.wakeLockSync) {
            if (this.accessCardTaskWakeLock != null) {
                LogX.d("release the wake lock now.");
                if (this.accessCardTaskWakeLock.isHeld()) {
                    this.accessCardTaskWakeLock.release();
                    LogX.i(this.clsName + " releaseAccessCardTaskWakelock, accessCardTaskWakeLock release. WAKE_LOCK_TIMEOUT= " + WAKE_LOCK_TIMEOUT);
                } else {
                    LogX.i(this.clsName + " releaseAccessCardTaskWakelock, accessCardTaskWakeLock not held .");
                }
                this.accessCardTaskWakeLock = null;
            } else {
                LogX.i(this.clsName + " releaseAccessCardTaskWakelock, accessCardTaskWakeLock is null .");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkNFCAutoOpen(String errorHappenStepCode) throws AccessCardOperatorException {
        if (!NfcUtil.isEnabledNFC(this.mContext)) {
            String ms = this.clsName + " failed. nfc is disable";
            LogX.i(ms);
            NfcUtil.enableNFC(this.mContext);
            AccessCardOperatorException accessCardOperatorException = new AccessCardOperatorException(12, 12, errorHappenStepCode, ms, null);
            throw accessCardOperatorException;
        }
    }
}
