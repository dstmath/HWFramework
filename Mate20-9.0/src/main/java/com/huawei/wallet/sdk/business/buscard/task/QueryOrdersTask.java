package com.huawei.wallet.sdk.business.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.impl.SPIOperatorManager;
import com.huawei.wallet.sdk.business.buscard.impl.TrafficCardOperator;

public class QueryOrdersTask extends TrafficCardBaseTask {
    private int mOrderType;

    public QueryOrdersTask(Context context, SPIOperatorManager operatorManager, String issuerId, int orderType) {
        super(context, operatorManager, issuerId);
        this.mOrderType = orderType;
    }

    /* access modifiers changed from: protected */
    public void excuteAction(TrafficCardOperator operator, IssuerInfoItem item) {
        if (item != null && operator != null) {
            operator.queryOrders(item, this.mOrderType);
        }
    }

    /* access modifiers changed from: protected */
    public String getTaskName() {
        return "QueryOrdersTask";
    }
}
