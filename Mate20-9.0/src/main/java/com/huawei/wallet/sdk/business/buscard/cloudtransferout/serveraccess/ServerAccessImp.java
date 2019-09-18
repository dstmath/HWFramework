package com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.impl.TrafficCardOperator;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;
import com.huawei.wallet.sdk.business.buscard.model.TransferEvent;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardOperateException;

public class ServerAccessImp implements TrafficCardOperator {
    private Context mContext;

    public ServerAccessImp(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void applyPayOrder(IssuerInfoItem item, ApplyOrderInfo applyOrderInfo) {
        new ApplyPayOrderOperator(this.mContext, item, applyOrderInfo).doApplyPayOrder();
    }

    public boolean uninstallTrafficCard(IssuerInfoItem item, boolean updateTA, String source, String reason, String reasonCode, String accountType, String account) throws TrafficCardOperateException {
        return new UninstallTrafficCardSAOperator(this.mContext, item, updateTA, false).uninstall(source, reason, reasonCode, accountType, account);
    }

    public void checkTransferOutCondition(IssuerInfoItem item) throws TrafficCardOperateException {
        new CheckTransferOutConditionSAOperator(this.mContext, item).checkTransferOutCondition();
    }

    public void checkCloudTransferOutCondition(IssuerInfoItem item) throws TrafficCardOperateException {
        CheckCloudTransferOutConditionSAOperator operator = new CheckCloudTransferOutConditionSAOperator(this.mContext, item);
        operator.setIsFromCloudTransfer(true);
        operator.checkCloudTransferCondtion();
    }

    public void queryOrders(IssuerInfoItem item, int orderType) {
        new QueryOrderSAOperator(this.mContext, item, orderType).queryOrders();
    }

    public void cloudTransferOutTrafficCard(TransferEvent event, IssuerInfoItem item) throws TrafficCardOperateException {
        new CloudTransferOutTrafficCardSAOperator(this.mContext, event, item).transferOut();
    }
}
