package com.huawei.wallet.sdk.business.idcard.accesscard.logic.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.idcard.accesscard.api.AccessCardOperator;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.exception.AccessCardOperatorException;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.InitAccessCardResultHandler;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import java.util.List;

public class InitAccessCardTask extends AccessCardBaseTask {
    private InitAccessCardResultHandler mHandler = null;
    private List<TACardInfo> mList;

    public InitAccessCardTask(Context mContext, List<TACardInfo> list, String issureid, AccessCardOperator operatorManager, InitAccessCardResultHandler resultHandler) {
        super(mContext, operatorManager, issureid);
        this.mHandler = resultHandler;
        this.mList = list;
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "InitAccessCardTask";
    }

    /* access modifiers changed from: protected */
    public void excuteAction(IssuerInfoItem item) {
        if (item == null || this.operator == null) {
            this.mHandler.handleResult(10);
            return;
        }
        acquireAccessCardTaskWakelock();
        try {
            this.operator.init(this.mList, this.mHandler);
        } catch (AccessCardOperatorException e) {
            LogX.e("EditAccessCardTask excuteAction exception : " + e.getMessage());
        }
        releaseAccessCardTaskWakelock();
    }
}
