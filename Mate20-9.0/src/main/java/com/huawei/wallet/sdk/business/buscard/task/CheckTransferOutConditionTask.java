package com.huawei.wallet.sdk.business.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.impl.SPIOperatorManager;
import com.huawei.wallet.sdk.business.buscard.impl.TrafficCardOperator;
import com.huawei.wallet.sdk.business.buscard.model.CheckTransferOutConditionResultHandler;
import com.huawei.wallet.sdk.common.log.LogC;

public class CheckTransferOutConditionTask extends TrafficCardBaseTask {
    private boolean mIsFromCloudTranser = false;
    private CheckTransferOutConditionResultHandler mResultHandler;

    public void setIsFromCloudTranser(boolean mIsFromCloudTranser2) {
        this.mIsFromCloudTranser = mIsFromCloudTranser2;
    }

    public CheckTransferOutConditionTask(Context mContext, SPIOperatorManager operatorManager, String mIssuerId, CheckTransferOutConditionResultHandler resultHandler) {
        super(mContext, operatorManager, mIssuerId);
        this.mResultHandler = resultHandler;
    }

    /* access modifiers changed from: protected */
    public void excuteAction(TrafficCardOperator operator, IssuerInfoItem item) {
        if (item == null || operator == null) {
            LogC.i("CheckTransferOutConditionTask excuteAction item or operator is null", false);
            return;
        }
        try {
            if (this.mIsFromCloudTranser) {
                operator.checkCloudTransferOutCondition(item);
            } else {
                operator.checkTransferOutCondition(item);
            }
            this.mResultHandler.handleResult(0);
        } catch (TrafficCardOperateException e) {
            LogC.i("CheckTransferOutConditionTask excuteAction exception", false);
        }
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "CheckTransferOutConditionTask";
    }
}
