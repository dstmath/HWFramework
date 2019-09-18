package com.huawei.wallet.sdk.business.idcard.accesscard.logic.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.idcard.accesscard.api.AccessCardOperator;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback.NullifyCardResultCallback;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.exception.AccessCardOperatorException;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.HandleNullifyResultHandler;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;

public class DeleteAccessCardResultTask extends AccessCardBaseTask {
    private String mAid;
    private String mIssuerId;
    private final HandleNullifyResultHandler mResultHandler;

    public DeleteAccessCardResultTask(Context mContext, AccessCardOperator operator, String mIssuerId2, String mAid2, HandleNullifyResultHandler resultHandler, NullifyCardResultCallback cardInfoCallback) {
        super(mContext, operator, mIssuerId2);
        this.mResultHandler = resultHandler;
        this.mIssuerId = mIssuerId2;
        this.mAid = mAid2;
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "DeleteAccessCardResultTask";
    }

    /* access modifiers changed from: protected */
    public void excuteAction(IssuerInfoItem item) {
        if (item == null || this.operator == null) {
            this.mResultHandler.handleResult(10);
            return;
        }
        acquireAccessCardTaskWakelock();
        try {
            this.operator.uninstallAccessCard(this.mIssuerId, this.mAid, true, this.mResultHandler);
        } catch (AccessCardOperatorException e) {
            LogX.e("DeleteAccessCardResultTask excuteAction exception : " + e.getMessage());
        }
        releaseAccessCardTaskWakelock();
    }
}
