package com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.base.WhiteCardBaseTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.common.WhiteCardOperatorApi;
import com.huawei.wallet.sdk.business.idcard.walletbase.whitecard.BaseResultHandler;

public class DeleteWhiteCardTask extends WhiteCardBaseTask {
    private String aid;
    private BaseResultHandler mHandler;
    private String passId;

    public DeleteWhiteCardTask(Context mContext, WhiteCardOperatorApi operator, String passTypeId, String passId2, String aid2, BaseResultHandler handler) {
        super(mContext, operator, passTypeId);
        this.mHandler = handler;
        this.passId = passId2;
        this.aid = aid2;
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "DeleteWhiteCardTask";
    }

    /* access modifiers changed from: protected */
    public void excuteAction(IssuerInfoItem item) {
        acquireTrafficCardTaskWakelock();
        this.operator.deleteWhiteCard(this.passTypeId, this.passId, this.aid, this.mHandler);
        releaseTrafficCardTaskWakelock();
    }
}
