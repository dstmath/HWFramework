package com.huawei.wallet.sdk.business.buscard.impl;

import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;
import com.huawei.wallet.sdk.business.buscard.model.TransferEvent;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardOperateException;

public interface TrafficCardOperator {
    void applyPayOrder(IssuerInfoItem issuerInfoItem, ApplyOrderInfo applyOrderInfo);

    void checkCloudTransferOutCondition(IssuerInfoItem issuerInfoItem) throws TrafficCardOperateException;

    void checkTransferOutCondition(IssuerInfoItem issuerInfoItem) throws TrafficCardOperateException;

    void cloudTransferOutTrafficCard(TransferEvent transferEvent, IssuerInfoItem issuerInfoItem) throws TrafficCardOperateException;

    void queryOrders(IssuerInfoItem issuerInfoItem, int i);

    boolean uninstallTrafficCard(IssuerInfoItem issuerInfoItem, boolean z, String str, String str2, String str3, String str4, String str5) throws TrafficCardOperateException;
}
