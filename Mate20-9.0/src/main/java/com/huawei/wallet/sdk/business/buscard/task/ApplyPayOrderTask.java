package com.huawei.wallet.sdk.business.buscard.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.bankcard.util.Router;
import com.huawei.wallet.sdk.business.bankcard.util.SecureCommonUtil;
import com.huawei.wallet.sdk.business.bankcard.util.ThreadPoolManager;
import com.huawei.wallet.sdk.business.buscard.impl.SPIOperatorManager;
import com.huawei.wallet.sdk.business.buscard.impl.TrafficCardOperator;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;
import com.huawei.wallet.sdk.business.buscard.model.TrafficOrder;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.NfcUtil;

public class ApplyPayOrderTask extends TrafficCardBaseTask {
    /* access modifiers changed from: private */
    public ApplyOrderInfo applyOrderInfo;
    /* access modifiers changed from: private */
    public Handler timeoutHandle;

    private class EnableNfcResultReceiver extends BroadcastReceiver {
        /* access modifiers changed from: private */
        public IssuerInfoItem item;
        /* access modifiers changed from: private */
        public TrafficCardOperator operator;

        public EnableNfcResultReceiver(TrafficCardOperator operator2, IssuerInfoItem item2) {
            this.operator = operator2;
            this.item = item2;
        }

        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= 18) {
                int status = SecureCommonUtil.getIntExtra(intent, "android.nfc.extra.ADAPTER_STATE", 3);
                LogX.i("ApplyPayOrderTask enable nfc result is :" + status);
                if (status == 3) {
                    if (ApplyPayOrderTask.this.timeoutHandle != null) {
                        ApplyPayOrderTask.this.timeoutHandle.removeMessages(0);
                        Handler unused = ApplyPayOrderTask.this.timeoutHandle = null;
                    }
                    ThreadPoolManager.getInstance().execute(new Runnable() {
                        public void run() {
                            EnableNfcResultReceiver.this.operator.applyPayOrder(EnableNfcResultReceiver.this.item, ApplyPayOrderTask.this.applyOrderInfo);
                        }
                    });
                    ApplyPayOrderTask.this.mContext.unregisterReceiver(this);
                }
            }
        }
    }

    public ApplyPayOrderTask(Context mContext, SPIOperatorManager operatorManager, String mIssuerId, ApplyOrderInfo orderInfo) {
        super(mContext, operatorManager, mIssuerId);
        this.applyOrderInfo = orderInfo;
    }

    /* access modifiers changed from: protected */
    public void excuteAction(TrafficCardOperator operator, IssuerInfoItem item) {
        if (item == null || operator == null) {
            new TrafficOrder();
            return;
        }
        int orderType = this.applyOrderInfo.getOrderType();
        try {
            if (!hasNFCAutoOpen(null) || Build.VERSION.SDK_INT < 18) {
                operator.applyPayOrder(item, this.applyOrderInfo);
            } else {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
                EnableNfcResultReceiver receiver = new EnableNfcResultReceiver(operator, item);
                this.mContext.registerReceiver(receiver, filter);
                startTimer(receiver, null);
            }
        } catch (TrafficCardOperateException e) {
        }
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "ApplyPayOrderTask";
    }

    private boolean hasNFCAutoOpen(String errorHappenStepCode) throws TrafficCardOperateException {
        if (NfcUtil.isEnabledNFC(this.mContext)) {
            return false;
        }
        LogX.i("ApplyPayOrderTask failed. nfc is disable");
        if (!Router.getNFCOpenApi(this.mContext).isAutoOpenNFC(this.mContext)) {
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(12, 12, errorHappenStepCode, "ApplyPayOrderTask failed. nfc is disable", null);
            throw trafficCardOperateException;
        } else if (NfcUtil.enableNFC(this.mContext)) {
            return true;
        } else {
            TrafficCardOperateException trafficCardOperateException2 = new TrafficCardOperateException(12, 12, errorHappenStepCode, "ApplyPayOrderTask failed. nfc is disable", null);
            throw trafficCardOperateException2;
        }
    }

    private void startTimer(final BroadcastReceiver receiver, String errorHappenStepCode) {
        if (this.timeoutHandle == null) {
            this.timeoutHandle = new Handler(Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (receiver != null) {
                        ApplyPayOrderTask.this.mContext.unregisterReceiver(receiver);
                    }
                }
            };
        }
        this.timeoutHandle.sendEmptyMessageDelayed(0, 2000);
    }
}
