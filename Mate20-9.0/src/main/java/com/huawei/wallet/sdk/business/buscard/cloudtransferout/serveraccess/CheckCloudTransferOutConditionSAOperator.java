package com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.TransferOutRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.TransferOutResponse;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;
import com.huawei.wallet.sdk.business.buscard.model.CheckTransferOutConditionCallback;
import com.huawei.wallet.sdk.business.buscard.model.TrafficOrder;
import com.huawei.wallet.sdk.business.buscard.spi.SPIServiceFactory;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardOperateException;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.logger.LoggerConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;

public class CheckCloudTransferOutConditionSAOperator extends CheckTransferOutConditionSAOperator {
    public CheckCloudTransferOutConditionSAOperator(Context context, IssuerInfoItem issuerInfoItem) {
        super(context, issuerInfoItem);
    }

    public void checkCloudTransferCondtion() throws TrafficCardOperateException {
        checkTransferOutCondition();
        if (queryUnusedTransferOrder() == null && this.mIssuerInfoItem.extConditionCheck) {
            String issueid = this.mIssuerInfoItem.getIssuerId();
            String aid = this.mIssuerInfoItem.getAid();
            String cplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
            this.mTaInfo = WalletTaManager.getInstance(this.mContext).getCardInfoByAid(aid);
            if (this.mTaInfo != null) {
                TransferOutRequest request = new TransferOutRequest(null, issueid, cplc, this.mIssuerInfoItem.getAid(), ProductConfigUtil.geteSEManufacturer(), Build.MODEL, this.mTaInfo.getFpanFour(), null);
                request.setSn(PhoneDeviceUtil.getSerialNumber());
                request.setPhoneManufacturer(Build.MANUFACTURER);
                request.setTransferVerifyFlag("1");
                TransferOutResponse response = SPIServiceFactory.createServerAccessService(this.mContext).checkCloudTransferOut(request);
                int returnCode = response.getResultCode();
                if (returnCode != 0) {
                    String errorMsg = response.getResultDesc();
                    if (errorMsg.equals("account is wrong")) {
                        returnCode = CheckTransferOutConditionCallback.RETURN_CARD_ACCOUT_CONFRIM_FAILED;
                    }
                    TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(returnCode, returnCode, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, errorMsg, null);
                    throw trafficCardOperateException;
                }
                return;
            }
            LogX.e("CheckCloudTransferOutConditionSAOperator  empty taInfo");
            TrafficCardOperateException trafficCardOperateException2 = new TrafficCardOperateException(99, 99, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CheckCloudTransferOutConditionSAOperator  empty taInfo", null);
            throw trafficCardOperateException2;
        }
    }

    private TrafficOrder queryUnusedTransferOrder() {
        ApplyOrderInfo applyOrderInfo = new ApplyOrderInfo(10, 0, 0);
        applyOrderInfo.setEventId("10");
        applyOrderInfo.setPayType(2);
        return new ApplyPayOrderOperator(this.mContext, this.mIssuerInfoItem, applyOrderInfo).queryUnusedTransferOrder();
    }
}
