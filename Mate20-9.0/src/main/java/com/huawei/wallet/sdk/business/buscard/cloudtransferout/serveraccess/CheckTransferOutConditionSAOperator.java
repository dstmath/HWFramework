package com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletInfoApiFactory;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.QueryOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.TransferOutRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.QueryOrderResponse;
import com.huawei.wallet.sdk.business.buscard.model.CheckTransferOutConditionCallback;
import com.huawei.wallet.sdk.business.buscard.spi.SPIServiceFactory;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardOperateException;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.account.NFCAccountManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.logger.LoggerConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.Iterator;
import java.util.List;

public class CheckTransferOutConditionSAOperator {
    String aid;
    String cplc;
    int errorCode;
    String errorMsg;
    String issueid;
    protected final Context mContext;
    private boolean mIsFromCloudTransfer = false;
    protected final IssuerInfoItem mIssuerInfoItem;
    protected TACardInfo mTaInfo;

    public void setIsFromCloudTransfer(boolean mIsFromCloudTransfer2) {
        this.mIsFromCloudTransfer = mIsFromCloudTransfer2;
    }

    public CheckTransferOutConditionSAOperator(Context context, IssuerInfoItem issuerInfoItem) {
        this.mContext = context;
        this.mIssuerInfoItem = issuerInfoItem;
        this.aid = this.mIssuerInfoItem.getAid();
        this.cplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        this.issueid = this.mIssuerInfoItem.getIssuerId();
    }

    public void checkTransferOutCondition() throws TrafficCardOperateException {
        this.mTaInfo = WalletTaManager.getInstance(this.mContext).getCard(this.aid);
        if (this.mTaInfo != null) {
            int mCardStatus = this.mTaInfo.getCardStatus();
            if (this.mIssuerInfoItem != null && ((this.mIssuerInfoItem.getIssuerId().equals("90000025") || this.mIssuerInfoItem.getIssuerId().equals("90000029") || this.mIssuerInfoItem.getIssuerId().equals("t_yt_lnt")) && !this.mIsFromCloudTransfer)) {
                verifyTransferOut();
            }
            if (this.mIssuerInfoItem != null) {
                checkUnfinishedOrder();
                if (!this.mIsFromCloudTransfer || !(mCardStatus == 22 || mCardStatus == 15)) {
                    checkBalance();
                } else {
                    return;
                }
            }
            return;
        }
        this.errorMsg = "TransferOutTrafficCardSAOperator transferOut, empty taInfo";
        LogX.e(this.errorMsg);
        this.errorCode = 99;
        TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(this.errorCode, this.errorCode, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, this.errorMsg, null);
        throw trafficCardOperateException;
    }

    private void verifyTransferOut() throws TrafficCardOperateException {
        TransferOutRequest request = new TransferOutRequest(null, this.issueid, this.cplc, this.mIssuerInfoItem.getAid(), ProductConfigUtil.geteSEManufacturer(), Build.MODEL, this.mTaInfo.getFpanFour(), null);
        request.setOrderId(null);
        request.setSn(PhoneDeviceUtil.getSerialNumber());
        request.setPhoneManufacturer(Build.MANUFACTURER);
        request.setAppCode(this.mIssuerInfoItem.getCityCode());
        request.setTransferVerifyFlag("1");
        int returnCd = SPIServiceFactory.createServerAccessService(this.mContext).transferOut(request).getResultCode();
        if (returnCd != 0) {
            this.errorMsg = "CheckTransferOutConditionSAOperator verifyTransferOut, verify fail.";
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(returnCd, returnCd, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, this.errorMsg, null);
            throw trafficCardOperateException;
        }
    }

    private void checkUnfinishedOrder() throws TrafficCardOperateException {
        QueryOrderRequest request = new QueryOrderRequest(this.mIssuerInfoItem.getIssuerId(), ESEInfoManager.getInstance(this.mContext).queryCplc(), this.mIssuerInfoItem.getAid(), Build.MODEL, ProductConfigUtil.geteSEManufacturer());
        request.setAccountUserId(NFCAccountManager.getAccountUserId());
        request.setOrderStatus("1");
        if (this.mIsFromCloudTransfer) {
            request.setOrderType("10");
        }
        request.setSn(PhoneDeviceUtil.getSerialNumber());
        request.setAppCode(getAppCode());
        QueryOrderResponse response = SPIServiceFactory.createServerAccessService(this.mContext).queryOrder(request);
        if (response.getResultCode() == 0) {
            List<QueryOrder> orderList = response.getOrderList();
            if (orderList != null) {
                Iterator<QueryOrder> iterator = orderList.iterator();
                while (iterator.hasNext()) {
                    QueryOrder info = iterator.next();
                    if ((!"1".equals(info.getOrderType()) && !"2".equals(info.getOrderType()) && !"7".equals(info.getOrderType())) || QueryOrder.STATUS_REFUND_SUCCESS.equals(info.getStatus())) {
                        iterator.remove();
                    }
                }
                if (orderList.size() > 0) {
                    TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(CheckTransferOutConditionCallback.RESULT_HAS_UNFINISHED_ORDER, response.getResultCode(), LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CheckTransferOutConditionSAOperator checkUnfinishedOrder, has unfinished order.", null);
                    throw trafficCardOperateException;
                }
                return;
            }
            return;
        }
        TrafficCardOperateException trafficCardOperateException2 = new TrafficCardOperateException(CheckTransferOutConditionCallback.RESULT_FAILED_TRAFFIC_CARD_RECORDS_READ_FAILED, response.getResultCode(), LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CheckTransferOutConditionSAOperator checkUnfinishedOrder, traffic card records read fail.", null);
        throw trafficCardOperateException2;
    }

    private String getAppCode() {
        return this.mIssuerInfoItem.getCityCode();
    }

    private void checkBalance() throws TrafficCardOperateException {
        AppletCardResult<CardInfo> appletCardResult = AppletInfoApiFactory.createAppletCardInfoReader(this.mContext).readTrafficCardInfo(this.mIssuerInfoItem.getAid(), this.mIssuerInfoItem.getProductId(), 2);
        if (appletCardResult.getResultCode() != 0) {
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(CheckTransferOutConditionCallback.RETURN_CARD_QUERY_BALANCE_FAILED, CheckTransferOutConditionCallback.RETURN_CARD_QUERY_BALANCE_FAILED, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CheckTransferOutConditionSAOperator checkBalance, query balance failed.", null);
            throw trafficCardOperateException;
        } else if (appletCardResult.getData().getBalanceByFenUnit() < 0) {
            TrafficCardOperateException trafficCardOperateException2 = new TrafficCardOperateException(CheckTransferOutConditionCallback.RETURN_CARD_BALANCE_OVERDRAWN, CheckTransferOutConditionCallback.RETURN_CARD_BALANCE_OVERDRAWN, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CheckTransferOutConditionSAOperator checkBalance, card balance overdrawn.", null);
            throw trafficCardOperateException2;
        }
    }
}
