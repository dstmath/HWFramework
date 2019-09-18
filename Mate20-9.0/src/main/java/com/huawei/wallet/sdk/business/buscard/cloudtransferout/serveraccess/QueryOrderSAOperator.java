package com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.BuscardCloudTransferHelper;
import com.huawei.wallet.sdk.business.buscard.base.model.RefundOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.QueryOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.QueryOrderResponse;
import com.huawei.wallet.sdk.business.buscard.spi.SPIServiceFactory;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.account.NFCAccountManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.ese.ESEApiFactory;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.List;

public class QueryOrderSAOperator {
    private String mCardName;
    private Context mContext;
    private String mCplc;
    private IssuerInfoItem mInfo;
    private int mOrderType;

    public QueryOrderSAOperator(Context context, IssuerInfoItem info, int orderType) {
        this.mContext = context;
        this.mInfo = info;
        this.mOrderType = orderType;
    }

    public void queryOrders() {
        LogX.i("QueryOrderSAOperator queryOrders begin");
        String aid = this.mInfo.getAid();
        String productId = this.mInfo.getProductId();
        if (StringUtil.isEmpty(aid, true) || StringUtil.isEmpty(productId, true)) {
            LogX.e("QueryOrderSAOperator queryOrders failed. aid or productId is illegal. aid = " + aid + " productId = " + productId);
            return;
        }
        String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
        this.mCplc = cplc;
        QueryOrderRequest request = new QueryOrderRequest(this.mInfo.getIssuerId(), cplc, this.mInfo.getAid(), Build.MODEL, ProductConfigUtil.geteSEManufacturer());
        request.setAccountUserId(NFCAccountManager.getAccountUserId());
        request.setOrderStatus("2");
        request.setSn(PhoneDeviceUtil.getSerialNumber());
        request.setAppCode(getAppCode());
        QueryOrderResponse response = SPIServiceFactory.createServerAccessService(this.mContext).queryOrder(request);
        if (response.getResultCode() != 0) {
            LogX.e("queryOrder err : " + response.getResultCode() + ", desc : " + response.getResultDesc());
            return;
        }
        if (response.getOrderList() != null && !response.getOrderList().isEmpty()) {
            List<QueryOrder> orderList = response.getOrderList();
            if (orderList != null) {
                List<RefundOrder> result = new ArrayList<>();
                if (this.mOrderType == 7) {
                    for (QueryOrder qo : orderList) {
                        if (QueryOrder.STATUS_REFUNDING.equals(qo.getStatus()) || QueryOrder.STATUS_REFUND_FAIL.equals(qo.getStatus()) || QueryOrder.STATUS_REFUND_SUCCESS.equals(qo.getStatus()) || QueryOrder.STATUS_REFUND_APPLET.equals(qo.getStatus()) || QueryOrder.STATUS_BALANCE_CONFIRMATION.equals(qo.getStatus())) {
                            this.mCardName = getCardName(qo.getIssuerId());
                            if (!TextUtils.isEmpty(this.mCardName)) {
                                result.add(new RefundOrder(qo, this.mCardName, isLocalRefundRecord(qo.getCplc())));
                            }
                        }
                    }
                    return;
                }
            }
        }
        LogX.i("QueryOrderSAOperator, there is no unfinished order!");
    }

    private String getCardName(String issuerid) {
        if (!TextUtils.isEmpty(issuerid)) {
            IssuerInfoItem item = BuscardCloudTransferHelper.getIssuerInfo(issuerid);
            if (item != null) {
                this.mCardName = item.getName();
                return this.mCardName;
            }
        }
        return null;
    }

    private String getAppCode() {
        return this.mInfo.getCityCode();
    }

    private boolean isLocalRefundRecord(String cplc) {
        LogX.i("QueryOrderSAOperator, isLocalRefundRecord cplc: " + cplc);
        if (this.mCplc == null || this.mCplc.equals(cplc)) {
            return true;
        }
        return false;
    }
}
