package com.huawei.wallet.sdk.business.buscard.task;

import android.content.Context;
import android.os.PowerManager;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.BuscardCloudTransferHelper;
import com.huawei.wallet.sdk.business.buscard.api.CardOperateLogic;
import com.huawei.wallet.sdk.business.buscard.impl.SPIOperatorManager;
import com.huawei.wallet.sdk.business.buscard.impl.TrafficCardOperator;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.NfcUtil;

public abstract class TrafficCardBaseTask implements Runnable {
    private static final int WAKE_LOCK_TIMEOUT = 600000;
    private String clsName = getTaskName();
    protected Context mContext;
    protected String mIssuerId;
    protected SPIOperatorManager operatorManager;
    private PowerManager.WakeLock trafficCardTaskWakeLock;
    private final Object wakeLockSync = new Object();

    /* access modifiers changed from: protected */
    public abstract void excuteAction(TrafficCardOperator trafficCardOperator, IssuerInfoItem issuerInfoItem);

    /* access modifiers changed from: protected */
    public abstract String getTaskName();

    public TrafficCardBaseTask(Context mContext2, SPIOperatorManager operatorManager2, String mIssuerId2) {
        this.mContext = mContext2;
        this.mIssuerId = mIssuerId2;
        this.operatorManager = operatorManager2;
    }

    public void run() {
        LogX.i(this.clsName + " run begin");
        TrafficCardOperator operator = null;
        IssuerInfoItem item = BuscardCloudTransferHelper.getIssuerInfo(this.mIssuerId);
        if (item == null) {
            LogX.w(this.clsName + " run failed. issuer info dose not exist.");
        } else {
            int mode = item.getMode();
            operator = this.operatorManager.getTrafficCardOpertor(mode);
            if (operator == null) {
                LogX.w(this.clsName + " run failed. don't support the mode. mode = " + mode);
            }
        }
        excuteAction(operator, item);
        CardOperateLogic.getInstance(this.mContext).removeTask();
        LogX.i(this.clsName + " run end");
    }

    /* access modifiers changed from: protected */
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
                    LogX.i(this.clsName + " releaseTrafficCardTaskWakelock, trafficCardTaskWakeLock not held .");
                }
                this.trafficCardTaskWakeLock = null;
            } else {
                LogX.i(this.clsName + " releaseTrafficCardTaskWakelock, trafficCardTaskWakeLock is null .");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkNFC(String errorHappenStepCode) throws TrafficCardOperateException {
        if (!NfcUtil.isEnabledNFC(this.mContext)) {
            String ms = this.clsName + " failed. nfc is disable";
            LogX.i(ms);
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(12, 12, errorHappenStepCode, ms, null);
            throw trafficCardOperateException;
        }
    }
}
