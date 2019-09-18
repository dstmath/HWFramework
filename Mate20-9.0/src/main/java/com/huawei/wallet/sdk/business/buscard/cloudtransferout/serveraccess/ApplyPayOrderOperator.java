package com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletInfoApiFactory;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.model.UnionPayInfo;
import com.huawei.wallet.sdk.business.buscard.base.spi.ServerAccessOperatorUtils;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.ApplyOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.TransferOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.ApplyOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.QueryOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.ApplyOrderResponse;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.QueryOrderResponse;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;
import com.huawei.wallet.sdk.business.buscard.model.ApplyPayOrderCallback;
import com.huawei.wallet.sdk.business.buscard.model.PayInfo;
import com.huawei.wallet.sdk.business.buscard.model.TaskResult;
import com.huawei.wallet.sdk.business.buscard.model.TrafficOrder;
import com.huawei.wallet.sdk.business.buscard.model.WXPayInfo;
import com.huawei.wallet.sdk.business.buscard.spi.SPIServiceFactory;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.account.NFCAccountManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.ese.ESEApiFactory;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApplyPayOrderOperator {
    public static final int APPLY_ORDER_ERRORCODE = 111;
    private static final int LIMITED_BALANCE = 100000;
    private String failCode;
    private int logUploadEventId;
    private String mAppCode = null;
    private final ApplyOrderInfo mApplyOrderInfo;
    private final Context mContext;
    private final IssuerInfoItem mItem;
    private final String partnerId;

    public ApplyPayOrderOperator(Context context, IssuerInfoItem item, ApplyOrderInfo applyOrderInfo) {
        this.mContext = context;
        this.mApplyOrderInfo = applyOrderInfo;
        this.mItem = item;
        this.partnerId = this.mApplyOrderInfo.isBeijingAppMode() ? SNBConstant.BMAC_SPID : SNBConstant.SPID;
    }

    public TaskResult<TrafficOrder> doApplyPayOrder() {
        LogX.i("ApplyPayOrderSAOperator doApplyPayOrder begin");
        String aid = this.mItem.getAid();
        String productId = this.mItem.getProductId();
        if (StringUtil.isEmpty(aid, true) || StringUtil.isEmpty(productId, true)) {
            LogX.w("ApplyPayOrderSAOperator doApplyPayOrder failed. aid or productId is illegal. aid = " + aid + " productId = " + productId);
            return new TaskResult<>(10, 10, "Param error");
        }
        getAppCode();
        TrafficOrder order = hasUnfinishedIssueOrder();
        if (order != null) {
            LogX.i("ApplyPayOrderOperator doApplyPayOrder, hasUnfinishedIssueOrder, use it.");
            return new TaskResult<>(0, 0, "Has unused issuer order", order);
        }
        TrafficOrder order2 = queryUnusedTransferOrder();
        if (order2 != null) {
            LogX.i("ApplyPayOrderOperator doApplyPayOrder, queryUnusedTransferOrder, use it.");
            return new TaskResult<>(0, 0, "Has unused transfer order", order2);
        } else if (!checkBalance()) {
            LogX.i("ApplyPayOrderOperator err, recharge amount over limit.");
            return new TaskResult<>(1001, 1001, "recharge amount over limit.");
        } else {
            ApplyOrderResponse response = SPIServiceFactory.createServerAccessService(this.mContext).applyOrder(bulidApplyOrderRequest());
            if (response.getResultCode() != 0 && response.getResultCode() != 1003) {
                return new TaskResult<>(ApplyPayOrderCallback.RETURN_FAILED_APPLY_ORDER_INNER_ERROR, response.getResultCode(), response.getResultDesc());
            }
            return new TaskResult<>(0, response.getResultCode(), response.getResultDesc(), buildOrderAndPayInfo(response));
        }
    }

    private String getAppCode() {
        this.mAppCode = this.mItem.getCityCode();
        return this.mAppCode;
    }

    private TrafficOrder buildOrderAndPayInfo(ApplyOrderResponse response) {
        TrafficOrder trafficOrder;
        List<ApplyOrder> orderList = response.getOrderList();
        TransferOrder transferOrder = response.getTransferOrder();
        ApplyOrder payOrder = null;
        if (orderList != null) {
            Iterator<ApplyOrder> it = orderList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ApplyOrder o = it.next();
                if (isOrderValid(o)) {
                    payOrder = o;
                    break;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ApplyPayOrderOperator buildOrderAndPayInfo: orderList size:");
        sb.append(orderList == null ? 0 : orderList.size());
        LogX.i(sb.toString());
        int orderType = this.mApplyOrderInfo.getOrderType();
        if (transferOrder == null || !(orderType == 10 || orderType == 11)) {
            if (payOrder != null) {
                trafficOrder = new TrafficOrder();
                trafficOrder.setApplyOrders(orderList);
                PayInfo payInfo = null;
                WXPayInfo wxPayInfo = null;
                UnionPayInfo unionPayInfo = null;
                if (this.mApplyOrderInfo.getPayType() == 3) {
                }
                if (this.mApplyOrderInfo.getPayType() == 3) {
                    unionPayInfo = UnionPayInfo.buildUnion(payOrder);
                } else if (this.mApplyOrderInfo.getPayType() == 2) {
                    wxPayInfo = new WXPayInfo();
                    wxPayInfo.setAppId(payOrder.getWechatPayAppId());
                    wxPayInfo.setNonceStr(payOrder.getWechatPayNonceStr());
                    wxPayInfo.setPartnerId(payOrder.getWechatPayPartnerId());
                    wxPayInfo.setPrepayId(payOrder.getWechatPayPrepayId());
                    wxPayInfo.setSign(payOrder.getSign());
                    wxPayInfo.setTimeStamp(payOrder.getWechatPayTimeStamp());
                    wxPayInfo.setPackageValue(payOrder.getWechatPayPackageValue());
                    wxPayInfo.setPayOrderNo(payOrder.getOrderId());
                } else {
                    payInfo = PayInfo.build(payOrder);
                }
                trafficOrder.setPayInfo(payInfo);
                trafficOrder.setWXPayInfo(wxPayInfo);
                trafficOrder.setUnionPayInfo(unionPayInfo);
                trafficOrder.setSpId(this.partnerId);
                trafficOrder.setNewPayVersion(true);
                trafficOrder.setPayType(this.mApplyOrderInfo.getPayType());
            } else {
                trafficOrder = getWxTrafficOrder(response);
            }
            return trafficOrder;
        }
        TrafficOrder trafficOrder2 = new TrafficOrder();
        trafficOrder2.setTransferOrder(transferOrder);
        return trafficOrder2;
    }

    private TrafficOrder getWxTrafficOrder(ApplyOrderResponse response) {
        LogX.i("ApplyPayOrderOperator buildOrderAndPayInfo: getWxTrafficOrder.");
        TrafficOrder trafficOrder = new TrafficOrder();
        List<ApplyOrder> orderList = new ArrayList<>();
        String wxOrderListJsonString = response.getWxOrderListJsonString();
        WXPayInfo wxPayInfo = null;
        if (wxOrderListJsonString != null) {
            try {
                JSONArray wxOrderArray = new JSONArray(wxOrderListJsonString);
                int i = 0;
                int n = wxOrderArray.length();
                while (true) {
                    if (i >= n) {
                        break;
                    }
                    JSONObject wxOrderJsonObj = wxOrderArray.getJSONObject(i);
                    wxPayInfo = WXPayInfo.build(wxOrderJsonObj.toString());
                    String wxOrderNo = JSONHelper.getStringValue(wxOrderJsonObj, "requestId");
                    String wxOrderType = JSONHelper.getStringValue(wxOrderJsonObj, "orderType");
                    ApplyOrder applyOrder = new ApplyOrder();
                    applyOrder.setOrderId(wxOrderNo);
                    applyOrder.setOrderType(wxOrderType);
                    orderList.add(applyOrder);
                    if (wxPayInfo != null && !StringUtil.isEmpty(wxOrderNo, true)) {
                        break;
                    }
                    i++;
                }
            } catch (JSONException e) {
                LogX.i("buildOrderAndPayInfo get WXOrder JSONException.");
            }
        } else {
            LogX.i("buildOrderAndPayInfo getWxOrderListJsonString is null.");
        }
        LogX.i("ApplyPayOrderOperator buildOrderAndPayInfo, getWxTrafficOrder orderList size:" + orderList.size());
        trafficOrder.setApplyOrders(orderList);
        trafficOrder.setSpId(this.partnerId);
        trafficOrder.setNewPayVersion(true);
        trafficOrder.setPayType(this.mApplyOrderInfo.getPayType());
        trafficOrder.setWXPayInfo(wxPayInfo);
        return trafficOrder;
    }

    private boolean isOrderValid(ApplyOrder o) {
        boolean isRechargeOrderType = this.mApplyOrderInfo.getOrderType() == 2 && (o.getOrderType().equals("1") || !TextUtils.isEmpty(o.getApplyOrderTn()));
        boolean isIssueCardType = this.mApplyOrderInfo.getOrderType() == 1 && (o.getOrderType().equals("0") || !TextUtils.isEmpty(o.getApplyOrderTn()));
        boolean isIssueAnRechargeType = this.mApplyOrderInfo.getOrderType() == 3 && (o.getOrderType().equals("2") || !TextUtils.isEmpty(o.getApplyOrderTn()));
        boolean isTransferOutType = this.mApplyOrderInfo.getOrderType() == 4 && (o.getOrderType().equals("3") || !TextUtils.isEmpty(o.getApplyOrderTn()));
        boolean isTransferInType = this.mApplyOrderInfo.getOrderType() == 5 && (o.getOrderType().equals("4") || !TextUtils.isEmpty(o.getApplyOrderTn()));
        boolean isTransferInRechargeType = this.mApplyOrderInfo.getOrderType() == 6 && (o.getOrderType().equals("7") || !TextUtils.isEmpty(o.getApplyOrderTn()));
        if (isRechargeOrderType || isIssueCardType || isIssueAnRechargeType || isTransferOutType || isTransferInType || isTransferInRechargeType) {
            return true;
        }
        return false;
    }

    private boolean checkBalance() {
        if (this.mApplyOrderInfo.getOrderType() != 2) {
            return true;
        }
        int currentBalance = getBalance();
        if (currentBalance < 0 || this.mApplyOrderInfo.getTheoreticalPayment() + currentBalance <= 100000) {
            return true;
        }
        return false;
    }

    private int getBalance() {
        AppletCardResult<CardInfo> response = AppletInfoApiFactory.createAppletCardInfoReader(this.mContext).readTrafficCardInfo(this.mItem.getAid(), this.mItem.getProductId(), 2);
        if (response.getResultCode() != 0) {
            return -1;
        }
        return response.getData().getBalanceByFenUnit();
    }

    private TrafficOrder hasUnfinishedIssueOrder() {
        if (this.mApplyOrderInfo.getOrderType() != 1 && this.mApplyOrderInfo.getOrderType() != 3) {
            return null;
        }
        QueryOrderResponse response = SPIServiceFactory.createServerAccessService(this.mContext).queryOrder(getQueryOrderRequest());
        if (response.getResultCode() == 0) {
            if (response.getOrderList() != null && response.getOrderList().size() > 0) {
                List<QueryOrder> orderList = response.getOrderList();
                List<QueryOrder> orderResultList = new ArrayList<>();
                boolean hasRetriableOpenOrder = false;
                for (QueryOrder order : orderList) {
                    if ((QueryOrder.STATUS_CREATE_SSD_FAIL.equals(order.getStatus()) || QueryOrder.STATUS_DOWNLOAD_CAP_FAIL.equals(order.getStatus()) || QueryOrder.STATUS_PERSONALIZED_FAIL.equals(order.getStatus()) || QueryOrder.STATUS_RECHARGE_FAIL.equals(order.getStatus()) || "1001".equals(order.getStatus()) || QueryOrder.STATUS_SNB_PAYED_BUT_FAILED.equals(order.getStatus()) || "1002".equals(order.getStatus())) && ("2".equals(order.getOrderType()) || "0".equals(order.getOrderType()))) {
                        hasRetriableOpenOrder = true;
                        orderResultList.add(order);
                    }
                }
                if (hasRetriableOpenOrder) {
                    LogX.i("ApplyPayOrderOperator, hasRetriableOrder");
                    TrafficOrder trafficOrder = new TrafficOrder();
                    trafficOrder.setQueryOrders(orderResultList);
                    trafficOrder.setHasUnusedIssueOrder(true);
                    trafficOrder.setPayType(this.mApplyOrderInfo.getPayType());
                    if (this.mApplyOrderInfo.getOrderType() == 1 || this.mApplyOrderInfo.getOrderType() == 3) {
                        return trafficOrder;
                    }
                }
            }
            LogX.i("ApplyPayOrderOperator, no unfinished orders.");
            return null;
        }
        LogX.e("ApplyPayOrderOperator, applyOrder err, code =" + response.getResultCode() + ", desc = " + response.getResultDesc());
        return null;
    }

    private QueryOrderRequest getQueryOrderRequest() {
        QueryOrderRequest request = new QueryOrderRequest(this.mItem.getIssuerId(), ESEInfoManager.getInstance(this.mContext).queryCplc(), this.mItem.getAid(), Build.MODEL, ProductConfigUtil.geteSEManufacturer());
        request.setAccountUserId(NFCAccountManager.getAccountUserId());
        request.setOrderStatus("1");
        if (this.mApplyOrderInfo.getOrderType() == 10) {
            request.setOrderType("10");
        }
        request.setSn(PhoneDeviceUtil.getSerialNumber());
        request.setPartnerId(this.mApplyOrderInfo.isBeijingAppMode() ? SNBConstant.BMAC_SPID : SNBConstant.SPID);
        request.setAppCode(this.mAppCode);
        return request;
    }

    public TrafficOrder queryUnusedTransferOrder() {
        if (this.mApplyOrderInfo.getOrderType() != 5 && this.mApplyOrderInfo.getOrderType() != 4 && this.mApplyOrderInfo.getOrderType() != 10 && this.mApplyOrderInfo.getOrderType() != 11) {
            return null;
        }
        QueryOrderResponse response = SPIServiceFactory.createServerAccessService(this.mContext).queryOrder(getQueryOrderRequest());
        if (response.getResultCode() != 0) {
            return null;
        }
        if (response.getOrderList() != null && response.getOrderList().size() > 0) {
            List<QueryOrder> orderList = response.getOrderList();
            List<QueryOrder> orderResultList = new ArrayList<>();
            if (findOrders(orderList, orderResultList)) {
                LogX.i("ApplyPayOrderOperator, hasRetriableOrder");
                TrafficOrder trafficOrder = new TrafficOrder();
                trafficOrder.setQueryOrders(orderResultList);
                trafficOrder.setHasUnusedIssueOrder(true);
                if (this.mApplyOrderInfo.getOrderType() == 4 || this.mApplyOrderInfo.getOrderType() == 5 || this.mApplyOrderInfo.getOrderType() == 10 || this.mApplyOrderInfo.getOrderType() == 11) {
                    LogX.i("ApplyPayOrderOperator, ApplyPayOrderCallback.RETURN_SUCCESS");
                    return trafficOrder;
                }
            }
        }
        LogX.e("ApplyPayOrderOperator, applyOrder err, code =" + response.getResultCode() + ", desc = " + response.getResultDesc());
        return null;
    }

    private boolean findOrders(List<QueryOrder> orderList, List<QueryOrder> orderResultList) {
        boolean isRetriable = false;
        for (QueryOrder queryOrder : orderList) {
            if (QueryOrder.STATUS_TRANSFER_OUT_FAILED.equals(queryOrder.getStatus()) || QueryOrder.STATUS_TRANSFER_IN_FAILED.equals(queryOrder.getStatus())) {
                orderResultList.add(queryOrder);
                isRetriable = true;
            } else if (this.mApplyOrderInfo.getOrderType() == 10) {
                if ("1001".equals(queryOrder.getStatus()) || "1002".equals(queryOrder.getStatus())) {
                    orderResultList.add(queryOrder);
                    isRetriable = true;
                }
            } else if (this.mApplyOrderInfo.getOrderType() == 11 && "1101".equals(queryOrder.getStatus())) {
                orderResultList.add(queryOrder);
                isRetriable = true;
            }
        }
        return isRetriable;
    }

    public String dealApplyOrderEmptyNum(TACardInfo ta) {
        if (ta == null) {
            return "";
        }
        String cardNum = ta.getFpanFour();
        if (TextUtils.isEmpty(cardNum)) {
            cardNum = ServerAccessOperatorUtils.getCardNum(1, this.mItem.getAid(), this.mItem.getProductId(), this.mContext);
        }
        return cardNum;
    }

    private ApplyOrderRequest bulidApplyOrderRequest() {
        String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
        String eSEManufacturer = ProductConfigUtil.geteSEManufacturer();
        String orderType = null;
        String aIssuePayment = null;
        String tIssuePayment = null;
        String eventId = null;
        String cardNum = null;
        String aCardMovePayment = null;
        String tCardMovePayment = null;
        String orderType2 = null;
        String aIssuePayment2 = null;
        if (1 == this.mApplyOrderInfo.getOrderType()) {
            orderType = "0";
            aIssuePayment = getPayment(this.mApplyOrderInfo.getActualIssuePayment());
            tIssuePayment = getPayment(this.mApplyOrderInfo.getTheoreticalIssuePayment());
        } else if (2 == this.mApplyOrderInfo.getOrderType() || 10 == this.mApplyOrderInfo.getOrderType()) {
            if (2 == this.mApplyOrderInfo.getOrderType()) {
                orderType = "1";
            } else if (10 == this.mApplyOrderInfo.getOrderType()) {
                orderType = String.valueOf(10);
            }
            eventId = getPayment(this.mApplyOrderInfo.getActualRechargePayment());
            cardNum = getPayment(this.mApplyOrderInfo.getTheoreticalRechargePayment());
            aCardMovePayment = dealApplyOrderEmptyNum(WalletTaManager.getInstance(this.mContext).getCard(this.mItem.getAid()));
        } else if (11 == this.mApplyOrderInfo.getOrderType()) {
            orderType = String.valueOf(11);
            aCardMovePayment = this.mApplyOrderInfo.getCardNo();
        } else if (3 == this.mApplyOrderInfo.getOrderType()) {
            orderType = "2";
            aIssuePayment = getPayment(this.mApplyOrderInfo.getActualIssuePayment());
            tIssuePayment = getPayment(this.mApplyOrderInfo.getTheoreticalIssuePayment());
            eventId = getPayment(this.mApplyOrderInfo.getActualRechargePayment());
            cardNum = getPayment(this.mApplyOrderInfo.getTheoreticalRechargePayment());
        } else if (4 == this.mApplyOrderInfo.getOrderType()) {
            orderType = "3";
            tCardMovePayment = getPayment(this.mApplyOrderInfo.getTheoreticalCardMovePayment());
            orderType2 = getPayment(this.mApplyOrderInfo.getActualCardMovePayment());
            aIssuePayment2 = this.mApplyOrderInfo.getEventId();
        } else if (5 == this.mApplyOrderInfo.getOrderType()) {
            orderType = "4";
            tCardMovePayment = getPayment(this.mApplyOrderInfo.getTheoreticalCardMovePayment());
            orderType2 = getPayment(this.mApplyOrderInfo.getActualCardMovePayment());
            eventId = getPayment(this.mApplyOrderInfo.getActualRechargePayment());
            cardNum = getPayment(this.mApplyOrderInfo.getTheoreticalRechargePayment());
            aIssuePayment2 = this.mApplyOrderInfo.getEventId();
        } else if (6 == this.mApplyOrderInfo.getOrderType()) {
            orderType = "7";
            tCardMovePayment = getPayment(this.mApplyOrderInfo.getTheoreticalCardMovePayment());
            orderType2 = getPayment(this.mApplyOrderInfo.getActualCardMovePayment());
            eventId = getPayment(this.mApplyOrderInfo.getActualRechargePayment());
            cardNum = getPayment(this.mApplyOrderInfo.getTheoreticalRechargePayment());
            aIssuePayment2 = this.mApplyOrderInfo.getEventId();
        }
        String tIssuePayment2 = tIssuePayment;
        String aRechargePayment = eventId;
        String tRechargePayment = cardNum;
        String cardNum2 = aCardMovePayment;
        String aCardMovePayment2 = orderType2;
        String eventId2 = aIssuePayment2;
        String orderType3 = orderType;
        String aIssuePayment3 = aIssuePayment;
        String issuerId = this.mItem.getIssuerId();
        String eventId3 = this.mItem.getAid();
        String str = cplc;
        String cplc2 = cardNum2;
        String cardNum3 = orderType3;
        String str2 = orderType3;
        String tCardMovePayment2 = tCardMovePayment;
        ApplyOrderRequest request = new ApplyOrderRequest(issuerId, cplc, eventId3, cardNum3, Build.MODEL, eSEManufacturer);
        request.setActualIssuePayment(aIssuePayment3);
        request.setTheoreticalIssuePayment(tIssuePayment2);
        request.setActualRecharegePayment(aRechargePayment);
        request.setTheoreticalRecharegePayment(tRechargePayment);
        request.setTheoreticalCardMovePayment(tCardMovePayment2);
        request.setActualCardMovePayment(aCardMovePayment2);
        request.setTrafficCardId(cplc2);
        request.setAccountUserId(NFCAccountManager.getAccountUserId());
        request.setEventId(eventId2);
        String str3 = tCardMovePayment2;
        request.setAppCode(this.mAppCode);
        request.setSn(PhoneDeviceUtil.getSerialNumber());
        request.setPartnerId(this.partnerId);
        request.setPayType(getOderPayType(this.mApplyOrderInfo.getPayType()));
        if (this.mItem.getIssuerId().equals("t_sh_01") && 10 == this.mApplyOrderInfo.getOrderType()) {
            request.setBuCardInfo(getFMCardInfo());
        }
        return request;
    }

    public String getFMCardInfo() {
        String buCardInfo;
        try {
            JSONArray jsonArray = new JSONArray("[{\"apduNo\":\"1\",\"apduContent\":\"00A4040009A00000000386980701\",\"apduStatus\":\"9000\"},{\"apduNo\":\"2\",\"apduContent\":\"80BE000100\",\"apduStatus\":\"6A86|9000\"},{\"apduNo\":\"3\",\"apduContent\":\"80CA000009\",\"apduStatus\":\"9000\"},{\"apduNo\":\"4\",\"apduContent\":\"00B2038C00\",\"apduStatus\":\"9000\"},{\"apduNo\":\"5\",\"apduContent\":\"00B207A400\",\"apduStatus\":\"9000\"},{\"apduNo\":\"6\",\"apduContent\":\"805000020B0100000400112233445566\",\"apduStatus\":\"9000\"}]");
            if (jsonArray.length() <= 0) {
                return "";
            }
            int length = jsonArray.length();
            List<ApduCommand> apduList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                ServerAccessAPDU apdu = ServerAccessAPDU.buildFromJson(jsonArray.getJSONObject(i));
                String apduId = apdu.getApduId();
                if (!StringUtil.isEmpty(apduId, true)) {
                    try {
                        ApduCommand apduCommand = new ApduCommand();
                        apduCommand.setIndex(Integer.parseInt(apduId));
                        apduCommand.setApdu(apdu.getApduContent());
                        if (apdu.getApduStatus() != null) {
                            apduCommand.setChecker(apdu.getApduStatus().split("\\|"));
                        }
                        apduList.add(apduCommand);
                    } catch (NumberFormatException e) {
                        LogX.e("ServerAccessServiceImpl changeServerAccessAPDU2ApduCommand, NumberFormatException");
                    }
                }
            }
            if (apduList.isEmpty() != 0) {
                return "";
            }
            IAPDUService omaService = OmaApduManager.getInstance(this.mContext);
            ChannelID channel = new ChannelID();
            channel.setMediaType(0);
            synchronized (IAPDUService.OMA_ACCESS_SYNC_LOCK) {
                omaService.excuteApduList(apduList, channel);
                omaService.closeChannel(channel);
            }
            StringBuilder sb = new StringBuilder();
            for (int i2 = 0; i2 < length; i2++) {
                ApduCommand command = apduList.get(i2);
                sb.append(command.getApdu());
                sb.append("|");
                sb.append(command.getRapdu());
                sb.append(command.getSw());
                if (i2 < length - 1) {
                    sb.append(SNBConstant.FILTER);
                }
            }
            LogX.i("CloudTransfer FM CardInfo: " + buCardInfo, false);
            return buCardInfo;
        } catch (JSONException e2) {
            LogX.e("CloudTransfer parse FM CardInfo error", false);
            return "";
        }
    }

    private String getOderPayType(int mPayType) {
        String orderPaytype = ServerAccessOperatorUtils.getInstance().getOrderPayType(mPayType);
        LogX.i("ApplyPayOrderSAOperator doApplyPayOrder PayType = " + mPayType + ",orderPaytype = " + orderPaytype);
        return orderPaytype;
    }

    private String getPayment(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        int decimal = i % 100;
        stringBuilder.append(i / 100);
        stringBuilder.append('.');
        if (decimal < 10) {
            stringBuilder.append(0);
        }
        stringBuilder.append(decimal);
        return stringBuilder.toString();
    }
}
