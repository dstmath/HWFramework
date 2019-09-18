package com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.modle.CardStatusItem;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletInfoApiFactory;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.result.TransferOutTrafficCardCallback;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.TransferOutRequest;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;
import com.huawei.wallet.sdk.business.buscard.model.TaskResult;
import com.huawei.wallet.sdk.business.buscard.model.TrafficOrder;
import com.huawei.wallet.sdk.business.buscard.model.TransferEvent;
import com.huawei.wallet.sdk.business.buscard.spi.SPIServiceFactory;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardOperateException;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.logger.LoggerConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.request.CardStatusQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.CardStatusQueryResponse;
import com.huawei.wallet.sdk.common.http.service.CommonService;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.Iterator;
import java.util.List;

public class CloudTransferOutTrafficCardSAOperator {
    private static final String mFlag = "1";
    private int mCardStatus;
    private Context mContext;
    private IssuerInfoItem mInfo;
    private TACardInfo mTaInfo;
    private TransferEvent mTransferEvent;

    public CloudTransferOutTrafficCardSAOperator(Context context, TransferEvent transferEvent, IssuerInfoItem issuerInfoItem) {
        this.mContext = context;
        this.mTransferEvent = transferEvent;
        this.mInfo = issuerInfoItem;
    }

    public void transferOutCheckParams() throws TrafficCardOperateException {
        if (this.mInfo == null || this.mTransferEvent == null) {
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(10, 10, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CloudTransferOutTrafficCardSAOperator transferOut, param is null.", null);
            throw trafficCardOperateException;
        }
    }

    public void transferOut() throws TrafficCardOperateException {
        int errorCode;
        String orderId;
        LogX.i("CloudTransferOutTrafficCardSAOperator transferOut begin.");
        transferOutCheckParams();
        String aid = this.mInfo.getAid();
        String productId = this.mInfo.getProductId();
        String cplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        String eventId = this.mTransferEvent.getEventId();
        if (StringUtil.isEmpty(aid, true) || StringUtil.isEmpty(eventId, true) || StringUtil.isEmpty(productId, true) || StringUtil.isEmpty(cplc, true)) {
            String errorMsg = "CloudTransferOutTrafficCardSAOperator transferOut failed. param is illegal. aid:" + aid + "eventId:" + eventId;
            LogX.w(errorMsg);
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(10, 10, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, errorMsg, null);
            throw trafficCardOperateException;
        }
        this.mTaInfo = WalletTaManager.getInstance(this.mContext).getCard(aid);
        if (this.mTaInfo == null) {
            LogX.e("CloudTransferOutTrafficCardSAOperator transferOut, empty taInfo");
            TrafficCardOperateException trafficCardOperateException2 = new TrafficCardOperateException(99, 99, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CloudTransferOutTrafficCardSAOperator transferOut, empty taInfo", null);
            throw trafficCardOperateException2;
        } else if (this.mCardStatus == 22 && queryUnusedTransferOrder() == null && isCardTransferedCloud(this.mTaInfo.getFpanFour())) {
            postTransferOut(this.mTaInfo.getAid());
        } else {
            TaskResult<TrafficOrder> orderResult = applyOrder();
            TrafficOrder order = orderResult.getData();
            if ((orderResult.getResultCd() == 0 || orderResult.getResultCd() == 1003) && order != null) {
                String orderId2 = getOrderID(order);
                if (orderId2 != null) {
                    this.mCardStatus = this.mTaInfo.getCardStatus();
                    LogX.i("CloudTransferOutTrafficCardSAOperator transferOut begin, cardStatus: " + this.mCardStatus);
                    if (this.mCardStatus == 2) {
                        updateTaCardInfoStatus(this.mTaInfo, 15);
                        this.mCardStatus = 15;
                    }
                    if (this.mCardStatus == 15) {
                        orderId = orderId2;
                        TrafficOrder trafficOrder = order;
                        TaskResult<TrafficOrder> taskResult = orderResult;
                        TransferOutRequest transferOutRequest = new TransferOutRequest(eventId, this.mInfo.getIssuerId(), cplc, this.mInfo.getAid(), ProductConfigUtil.geteSEManufacturer(), Build.MODEL, this.mTaInfo.getFpanFour(), checkBalance());
                        transferOutRequest.setOrderId(orderId);
                        transferOutRequest.setSn(PhoneDeviceUtil.getSerialNumber());
                        transferOutRequest.setPhoneManufacturer(Build.MANUFACTURER);
                        transferOutRequest.setAppCode(this.mInfo.getCityCode());
                        int resultCode = SPIServiceFactory.createServerAccessService(this.mContext).cloudTransferOut(transferOutRequest).getResultCode();
                        if (resultCode == 0) {
                            updateTaCardInfoStatus(this.mTaInfo, 22);
                            this.mCardStatus = 22;
                            updateTaCardInfoStatus(this.mTaInfo, 22);
                            this.mCardStatus = 22;
                        } else {
                            String errorMsg2 = "CloudTransferOutTrafficCardSAOperator. transOut failed. result : " + resultCode + ",msg:" + response.getResultDesc();
                            LogX.w(errorMsg2);
                            String str = errorMsg2;
                            int i = resultCode;
                            TrafficCardOperateException trafficCardOperateException3 = new TrafficCardOperateException(TransferOutTrafficCardCallback.RETURN_SP_TRANSFER_OUT_FAILED, resultCode, LoggerConstant.RESULT_CODE_TRANSFER_OUT_FAIL, errorMsg2, null);
                            throw trafficCardOperateException3;
                        }
                    } else {
                        orderId = orderId2;
                        TrafficOrder trafficOrder2 = order;
                        TaskResult<TrafficOrder> taskResult2 = orderResult;
                    }
                    if (this.mCardStatus == 22) {
                        UninstallTrafficCardSAOperator uninstallTrafficCardSAOperator = new UninstallTrafficCardSAOperator(this.mContext, this.mInfo, true, false, orderId, "1");
                        try {
                            if (!uninstallTrafficCardSAOperator.uninstall("", "cloud backup sucess and uninstall card", "2", null, null)) {
                                TrafficCardOperateException trafficCardOperateException4 = new TrafficCardOperateException(TransferOutTrafficCardCallback.RETURN_DELETE_SSD_FAILED, TransferOutTrafficCardCallback.RETURN_DELETE_SSD_FAILED, LoggerConstant.RESULT_CODE_DELETE_TRAFFIC_CARD_FAILED, "CloudTransferOutTrafficCardSAOperator uninstakll return faild", null);
                                throw trafficCardOperateException4;
                            }
                        } catch (TrafficCardOperateException e) {
                            LogX.e("CloudTransferOutTrafficCardSAOperator UninstallTrafficCardSAOperator error");
                            TrafficCardOperateException trafficCardOperateException5 = new TrafficCardOperateException(TransferOutTrafficCardCallback.RETURN_DELETE_SSD_FAILED, TransferOutTrafficCardCallback.RETURN_DELETE_SSD_FAILED, LoggerConstant.RESULT_CODE_DELETE_TRAFFIC_CARD_FAILED, "CloudTransferOutTrafficCardSAOperator UninstallTrafficCardSAOperator. sa shift out failed.", null);
                            throw trafficCardOperateException5;
                        }
                    }
                    LogX.i("CloudTransferOutTrafficCardSAOperator transferOut end.");
                    return;
                }
                TaskResult<TrafficOrder> taskResult3 = orderResult;
                String str2 = orderId2;
                TrafficCardOperateException trafficCardOperateException6 = new TrafficCardOperateException(10, 10, LoggerConstant.RESULT_CODE_ISSUE_CARD_OTHER_FAIL, "CloudTransferOutTrafficCardSAOperator transferOut err, get orderID err.", null);
                throw trafficCardOperateException6;
            }
            if (orderResult.getSpiResultCd() == 1101) {
                errorCode = TransferOutTrafficCardCallback.RETURN_CARD_NUM_LIMIT;
            } else {
                errorCode = TransferOutTrafficCardCallback.RETURN_APPLY_ORDER_FAILED;
            }
            TrafficCardOperateException trafficCardOperateException7 = new TrafficCardOperateException(errorCode, orderResult.getSpiResultCd(), LoggerConstant.RESULT_CODE_TRANSFER_OUT_APPLY_ORDER_FAIL, "CloudTransferOutTrafficCardSAOperator transferOut, apply order failed.", null);
            throw trafficCardOperateException7;
        }
    }

    private TaskResult<TrafficOrder> applyOrder() {
        ApplyOrderInfo applyOrderInfo = new ApplyOrderInfo(10, 0, 0);
        applyOrderInfo.setEventId(this.mTransferEvent.getEventId());
        applyOrderInfo.setPayType(2);
        return new ApplyPayOrderOperator(this.mContext, this.mInfo, applyOrderInfo).doApplyPayOrder();
    }

    private TrafficOrder queryUnusedTransferOrder() {
        ApplyOrderInfo applyOrderInfo = new ApplyOrderInfo(10, 0, 0);
        applyOrderInfo.setEventId(this.mTransferEvent.getEventId());
        applyOrderInfo.setPayType(2);
        return new ApplyPayOrderOperator(this.mContext, this.mInfo, applyOrderInfo).queryUnusedTransferOrder();
    }

    private void updateTaCardInfoStatus(TACardInfo taInfo, int newStatus) throws TrafficCardOperateException {
        try {
            WalletTaManager.getInstance(this.mContext).updateCardStatus(taInfo.getDpanDigest(), newStatus);
            taInfo.setCardStatus(newStatus);
            LogX.i("CloudTransferOutTrafficCardSAOperator updateTaCardInfoStatus.");
            this.mCardStatus = newStatus;
        } catch (WalletTaException.WalletTaCardNotExistException e) {
            LogX.e("CloudTransferOutTrafficCardSAOperator updateTaCardInfoStatus, WalletTaCardNotExistException");
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(99, 99, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CloudTransferOutTrafficCardSAOperator updateTaCardInfoStatus, WalletTaCardNotExistException", null);
            throw trafficCardOperateException;
        } catch (WalletTaException.WalletTaSystemErrorException e2) {
            LogX.e("CloudTransferOutTrafficCardSAOperator updateTaCardInfoStatus, WalletTaSystemErrorException");
            TrafficCardOperateException trafficCardOperateException2 = new TrafficCardOperateException(99, 99, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CloudTransferOutTrafficCardSAOperator updateTaCardInfoStatus, WalletTaSystemErrorException", null);
            throw trafficCardOperateException2;
        }
    }

    private String checkBalance() throws TrafficCardOperateException {
        AppletCardResult<CardInfo> appletCardResult = AppletInfoApiFactory.createAppletCardInfoReader(this.mContext).readTrafficCardInfo(this.mInfo.getAid(), this.mInfo.getProductId(), 2);
        if (appletCardResult.getResultCode() == 0) {
            return String.valueOf(appletCardResult.getData().getBalanceByFenUnit());
        }
        LogX.e("CloudTransferOutTrafficCardSAOperator transferOut, balance overdrawn. or read balance failed");
        TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(TransferOutTrafficCardCallback.RETURN_CARD_BALANCE_OVERDRAWN, TransferOutTrafficCardCallback.RETURN_CARD_BALANCE_OVERDRAWN, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CloudTransferOutTrafficCardSAOperator transferOut, balance overdrawn. or read balance failed", null);
        throw trafficCardOperateException;
    }

    private String getOrderID(TrafficOrder transferOrder) {
        if (transferOrder.getTransferOrder() != null) {
            return transferOrder.getTransferOrder().getOrderNum();
        }
        List<QueryOrder> queryOrders = transferOrder.getQueryOrders();
        if (queryOrders == null || queryOrders.size() <= 0) {
            return null;
        }
        QueryOrder cloudTransferOutQueryOrder = null;
        Iterator<QueryOrder> it = queryOrders.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            QueryOrder queryOrder = it.next();
            if ("10".equals(queryOrder.getOrderType())) {
                cloudTransferOutQueryOrder = queryOrder;
                break;
            }
        }
        if (cloudTransferOutQueryOrder != null) {
            return cloudTransferOutQueryOrder.getOrderId();
        }
        return null;
    }

    private void postTransferOut(String aid) throws TrafficCardOperateException {
        try {
            WalletTaManager.getInstance(this.mContext).removeCardByAid(aid);
        } catch (WalletTaException.WalletTaCardNotExistException e) {
            LogX.w(" TransferOutTrafficCardSAOperator updateTaAndReport WalletTaCardNotExistException, ta removeCard failed", (Throwable) e);
            TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(99, 99, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, " TransferOutTrafficCardSAOperator updateTaAndReport WalletTaCardNotExistException, ta removeCard failed", null);
            throw trafficCardOperateException;
        } catch (WalletTaException.WalletTaSystemErrorException e2) {
            LogX.w(" TransferOutTrafficCardSAOperator updateTaAndReport WalletTaSystemErrorException, ta removeCard failed", (Throwable) e2);
            TrafficCardOperateException trafficCardOperateException2 = new TrafficCardOperateException(99, 99, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, " TransferOutTrafficCardSAOperator updateTaAndReport WalletTaSystemErrorException, ta removeCard failed", null);
            throw trafficCardOperateException2;
        }
    }

    public boolean isCardTransferedCloud(String carNum) {
        boolean ret = false;
        if (TextUtils.isEmpty(carNum)) {
            return false;
        }
        CardStatusQueryRequest request = new CardStatusQueryRequest();
        request.setCplc(ESEInfoManager.getInstance(this.mContext).getCplcByBasicChannel());
        request.queryFlag = "2";
        CardStatusQueryResponse response = new CommonService(this.mContext).queryCardStatus(request);
        if (response.returnCode == 0 && response.getInCloudCount() > 0 && response.getCloudItems() != null && response.getCloudItems().size() > 0) {
            Iterator<CardStatusItem> it = response.getCloudItems().iterator();
            while (true) {
                if (it.hasNext()) {
                    if (carNum.equals(it.next().getCardNum())) {
                        ret = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return ret;
    }
}
