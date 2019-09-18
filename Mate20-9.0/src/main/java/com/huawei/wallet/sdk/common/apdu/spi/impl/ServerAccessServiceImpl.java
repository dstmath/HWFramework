package com.huawei.wallet.sdk.common.apdu.spi.impl;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.ApplyOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.ApplyOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.QueryOrderRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.request.TransferOutRequest;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.ApplyOrderResponse;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.QueryOrderResponse;
import com.huawei.wallet.sdk.business.buscard.base.spi.response.TransferOutResponse;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import com.huawei.wallet.sdk.common.apdu.TaskResult;
import com.huawei.wallet.sdk.common.apdu.contactless.ContactlessApduManager;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessApplyOrder;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessQueryOrder;
import com.huawei.wallet.sdk.common.apdu.oma.OmaApduManager;
import com.huawei.wallet.sdk.common.apdu.request.DeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.apdu.response.DeleteAppletResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.common.apdu.spi.ServerAccessService;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessApplyOrderRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessQueryOrderRequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessTransferOutRequest;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessApplyOrderResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResponse;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessTransferOutResponse;
import com.huawei.wallet.sdk.common.buscard.task.ServerApiInvocationHandler;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.http.response.BaseResponse;
import com.huawei.wallet.sdk.common.http.service.CommonService;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ServerAccessServiceImpl implements ServerAccessService {
    private static final String APDU_SUCCESS_9000 = "9000";
    private static final String APDU_SUCCES_6A88 = "6A88";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final byte[] SYNC_LOCK_CONTACTLESS_INSTANCE = new byte[0];
    private static volatile ServerAccessServiceImpl contactlessInstance = null;
    private static volatile ServerAccessServiceImpl instance = null;
    private String apduError = "";
    private CommonService cardServer = null;
    private ServerApiInvocationHandler invocationHandler;
    private ChannelID mChannel = null;
    private Context mContext = null;
    private IAPDUService omaService = null;
    private CommonService proxy;

    private ServerAccessServiceImpl(Context context, boolean isContactless) {
        this.mContext = context.getApplicationContext();
        this.cardServer = new CommonService(this.mContext, AddressNameMgr.MODULE_NAME_TRANSPORTATIONCARD);
        this.invocationHandler = new ServerApiInvocationHandler(this.mContext, this.cardServer);
        this.omaService = isContactless ? ContactlessApduManager.getInstance() : OmaApduManager.getInstance(this.mContext);
    }

    private ServerAccessServiceImpl(Context context, boolean isContactless, String module) {
        this.mContext = context.getApplicationContext();
        this.cardServer = new CommonService(this.mContext, AddressNameMgr.MODULE_NAME_TRANSPORTATIONCARD);
        this.invocationHandler = new ServerApiInvocationHandler(this.mContext, this.cardServer);
        this.omaService = isContactless ? ContactlessApduManager.getInstance() : OmaApduManager.getInstance(this.mContext);
    }

    public static ServerAccessService getInstance(Context context) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null && context != null) {
                    instance = new ServerAccessServiceImpl(context, false);
                }
            }
        }
        return instance;
    }

    public static ServerAccessService getInstance(Context context, String module) {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null && context != null) {
                    instance = new ServerAccessServiceImpl(context, false, module);
                }
            }
        }
        return instance;
    }

    public static ServerAccessService getContactlessInstance(Context context) {
        if (contactlessInstance == null) {
            synchronized (SYNC_LOCK_CONTACTLESS_INSTANCE) {
                if (contactlessInstance == null && context != null) {
                    contactlessInstance = new ServerAccessServiceImpl(context, true);
                }
            }
        }
        return contactlessInstance;
    }

    public static ServerAccessService getContactlessInstance(Context context, String module) {
        if (contactlessInstance == null) {
            synchronized (SYNC_LOCK_CONTACTLESS_INSTANCE) {
                if (contactlessInstance == null && context != null) {
                    contactlessInstance = new ServerAccessServiceImpl(context, true, module);
                }
            }
        }
        return contactlessInstance;
    }

    public DeleteAppletResponse deleteApplet(DeleteAppletRequest request) {
        return deleteApplet(request, 0);
    }

    public DeleteAppletResponse deleteApplet(DeleteAppletRequest request, int mediaType) {
        ServerAccessDeleteAppletResponse res;
        DeleteAppletResponse response = new DeleteAppletResponse();
        if (request == null) {
            LogC.e("ServerAccessServiceImpl deleteApplet, iCnvalid param", false);
            response.setResultCode(1);
            response.setResultDesc("client check, invalid param");
            return response;
        }
        ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest = new ServerAccessDeleteAppletRequest(request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getDeviceModel(), request.getSeChipManuFacturer());
        ServerAccessDeleteAppletRequest req = serverAccessDeleteAppletRequest;
        req.setCardId(request.getTrafficCardId());
        req.setSn(request.getSn());
        req.setPhoneNumber(request.getPhoneNumber());
        req.setPhoneManufacturer(request.getPhoneManufacturer());
        req.setReason(request.getReason());
        req.setRefundTicketId(request.getRefundTicketNum());
        req.setReserved(request.getReserved());
        req.setUserId(request.getAccountUserId());
        req.setAppCode(request.getAppCode());
        req.setPartnerId(request.getPartnerId());
        req.setReason(request.getReason());
        req.setSource(request.getSource());
        if (!StringUtil.isEmpty(request.getRefundAccountNumber(), true) && !StringUtil.isEmpty(request.getRefundAccountType(), true)) {
            req.setRefundAccountType(request.getRefundAccountType());
            req.setRefundAccountNumber(request.getRefundAccountNumber());
        }
        LogC.i("deleteApp source : " + req.getSource(), false);
        if (!TextUtils.isEmpty(request.getCardBalance())) {
            req.setCardBalance(request.getCardBalance());
        }
        req.setOnlyDeleteApplet(request.isOnlyDeleteApplet() ? "true" : "false");
        req.setFlag(request.getFlag());
        req.setOrderNo(request.getOrderNo());
        ServerAccessDeleteAppletResponse res2 = this.cardServer.deleteApplet(req);
        if (res2 != null) {
            String srcTranID = res2.getSrcTranID();
            response.setTransactionId(srcTranID);
            String transactionId = res2.getTransactionId();
            LogC.i("ServerAccessServiceImpl deleteApplet, response = " + res2.returnCode, false);
            if (res2.returnCode == 0) {
                List<ServerAccessAPDU> apduList = res2.getApduList();
                if (apduList == null) {
                    List<ServerAccessAPDU> list = apduList;
                    String str = srcTranID;
                    res = res2;
                    ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest2 = req;
                } else if (apduList.isEmpty()) {
                    List<ServerAccessAPDU> list2 = apduList;
                    String str2 = srcTranID;
                    res = res2;
                    ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest3 = req;
                } else {
                    HashMap hashMap = new HashMap();
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.AID, req.getAppletAid());
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, req.getIssuerId());
                    hashMap.put("cplc", req.getCplc());
                    ChannelID channel = new ChannelID();
                    channel.setMediaType(mediaType);
                    channel.setChannelType(1);
                    String deviceModel = req.getDeviceModel();
                    String seChipManuFacturer = req.getSeChipManuFacturer();
                    String nextStep = res2.getNextStep();
                    ChannelID channelID = channel;
                    HashMap hashMap2 = hashMap;
                    String str3 = nextStep;
                    List<ServerAccessAPDU> list3 = apduList;
                    String partnerId = req.getPartnerId();
                    String str4 = srcTranID;
                    res = res2;
                    ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest4 = req;
                    executeCommand(transactionId, apduList, response, hashMap, deviceModel, seChipManuFacturer, channel, 1, str3, partnerId, req.getSrcTransactionID(), false, res2.getErrorInfo());
                    ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse = res;
                }
                response.setResultCode(0);
                ServerAccessDeleteAppletResponse serverAccessDeleteAppletResponse2 = res;
            } else {
                ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest5 = req;
                translateErrorCode(res2, response);
            }
        } else {
            ServerAccessDeleteAppletRequest serverAccessDeleteAppletRequest6 = req;
        }
        return response;
    }

    public void executeCommand(String transactionId, List<ServerAccessAPDU> apduList, BaseResponse response, Map<String, String> paramMap, String deviceModel, String seChipManuFacturer, ChannelID channel, Boolean closeChannel, String nextStep, String partnerId, String srcTransactionID, boolean isNoNeedCommandResp, ErrorInfo errorInfo) {
        BaseResponse baseResponse = response;
        boolean z = true;
        String str = transactionId;
        if (StringUtil.isEmpty(str, true) || apduList == null || apduList.isEmpty()) {
            LogX.e("ServerAccessServiceImpl executeCommand, invalid param");
            baseResponse.setResultCode(1);
            baseResponse.setResultDesc("client check, invalid param");
            baseResponse.setErrorInfo(errorInfo);
            return;
        }
        ErrorInfo errorInfo2 = errorInfo;
        List<ServerAccessAPDU> apduList2 = apduList;
        String nextStep2 = nextStep;
        String transactionId2 = str;
        ChannelID channel2 = channel;
        while (true) {
            this.apduError = "";
            List<ApduCommand> apduCommandList = changeServerAccessAPDU2ApduCommand(apduList2);
            TaskResult<ChannelID> result = this.omaService.excuteApduList(apduCommandList, channel2);
            channel2 = result.getData();
            LogX.i("ServerAccessServiceImpl executeCommand, oma execute command, " + result.getPrintMsg());
            ApduCommand command = result.getLastExcutedCommand();
            this.apduError = getApduError(command, result);
            List<ServerAccessAPDU> apduList3 = changeApduCommand2ServerAccessAPDU(apduCommandList, result.getLastExcutedCommand(), isNoNeedCommandResp);
            ApduCommand apduCommand = command;
            ServerAccessApplyAPDURequest serverAccessApplyAPDURequest = new ServerAccessApplyAPDURequest(transactionId2, paramMap, apduList3.size(), apduList3, deviceModel, seChipManuFacturer);
            ServerAccessApplyAPDURequest req = serverAccessApplyAPDURequest;
            req.setCurrentStep(nextStep2);
            req.setSn(PhoneDeviceUtil.getSerialNumber());
            req.setPartnerId(partnerId);
            req.setSrcTransactionID(srcTransactionID);
            req.setPhoneNumber(getPhoneNum(paramMap.get(ServerAccessApplyAPDURequest.ReqKey.AID)));
            ServerAccessApplyAPDUResponse res = this.cardServer.applyApdu(req);
            if (res == null) {
                LogX.e("ServerAccessServiceImpl executeCommand, invalid apply apdu response");
                break;
            }
            baseResponse.setTransactionId(res.getSrcTranID());
            ServerAccessApplyAPDURequest req2 = req;
            if (!StringUtil.isEmpty(res.getTransactionId(), z)) {
                transactionId2 = res.getTransactionId();
            }
            LogX.i("ServerAccessServiceImpl executeCommand, apply apdu response = " + res.returnCode);
            if (res.returnCode != 0) {
                translateErrorCode(res, baseResponse);
                checkApduResult(baseResponse, result);
                baseResponse.setApduError(this.apduError);
                baseResponse.setResultDesc(res.getResultDesc() + response.getClass().getSimpleName() + ", OMA result : " + result.getPrintMsg());
                break;
            }
            apduList2 = res.getApduList();
            String nextStep3 = res.getNextStep();
            if (apduList2 == null || apduList2.isEmpty()) {
                baseResponse.setResultCode(0);
            }
            if (apduList2 == null || apduList2.isEmpty()) {
                String str2 = nextStep3;
            } else {
                nextStep2 = nextStep3;
                ServerAccessApplyAPDURequest serverAccessApplyAPDURequest2 = req2;
                z = true;
            }
        }
        if (closeChannel.booleanValue() || response.getResultCode() != 0) {
            this.omaService.closeChannel(channel2);
            this.mChannel = null;
        } else {
            this.mChannel = channel2;
        }
    }

    private String getPhoneNum(String aid) {
        return "18888888888";
    }

    private void checkApduResult(BaseResponse response, TaskResult<ChannelID> result) {
        if (result.getResultCode() == 4002) {
            response.setLocalApduResultCode(checkVerifyPinSW(result.getLastExcutedCommand().getApdu(), result.getLastExcutedCommand().getSw()));
        }
    }

    private int checkVerifyPinSW(String command, String sw) {
        LogC.i("sw is :" + sw, false);
        if (command.startsWith("00200000") && "6983".equals(sw)) {
            return -1;
        }
        return 0;
    }

    private String getApduError(ApduCommand command, TaskResult<ChannelID> result) {
        String apduError2 = "";
        if (command != null) {
            apduError2 = "resultCode_" + result.getResultCode() + "_idx_" + command.getIndex() + "_rapdu_" + command.getRapdu() + "_sw_" + command.getSw();
            if (result.getResultCode() != 0) {
                "resultCode:" + result.getResultCode() + "," + command.toString();
            }
        }
        return apduError2;
    }

    private List<ServerAccessAPDU> changeApduCommand2ServerAccessAPDU(List<ApduCommand> apduCommandList, ApduCommand lastApduCommand, boolean isNoNeedCommandResp) {
        List<ServerAccessAPDU> apduList = new ArrayList<>();
        if (lastApduCommand != null) {
            for (ApduCommand apduCommand : apduCommandList) {
                ServerAccessAPDU apdu = new ServerAccessAPDU();
                apdu.setApduId(String.valueOf(apduCommand.getIndex()));
                if (!StringUtil.isEmpty(apduCommand.getSw(), true)) {
                    apdu.setApduContent(apduCommand.getRapdu() + apduCommand.getSw());
                    apdu.setApduStatus(apduCommand.getSw());
                    if (!isNoNeedCommandResp) {
                        apdu.setCommand(apduCommand.getApdu());
                        apdu.setChecker(apduCommand.getChecker());
                    }
                } else {
                    apdu.setApduContent("");
                    apdu.setApduStatus(null);
                }
                apduList.add(apdu);
            }
        }
        return apduList;
    }

    private List<ApduCommand> changeServerAccessAPDU2ApduCommand(List<ServerAccessAPDU> apduList) {
        List<ApduCommand> apduCommandList = new ArrayList<>();
        for (ServerAccessAPDU apdu : apduList) {
            String apduId = apdu.getApduId();
            if (StringUtil.isEmpty(apduId, true)) {
                LogC.e("ServerAccessServiceImpl changeServerAccessAPDU2ApduCommand, invalid apduId", false);
            } else {
                try {
                    ApduCommand apduCommand = new ApduCommand();
                    apduCommand.setIndex(Integer.parseInt(apduId));
                    apduCommand.setApdu(apdu.getApduContent());
                    if (apdu.getApduStatus() != null) {
                        apduCommand.setChecker(apdu.getApduStatus().split("[|]"));
                    }
                    apduCommandList.add(apduCommand);
                } catch (NumberFormatException e) {
                    LogC.e("ServerAccessServiceImpl changeServerAccessAPDU2ApduCommand, NumberFormatException", false);
                }
            }
        }
        return apduCommandList;
    }

    private void translateErrorCode(ServerAccessBaseResponse oldResponse, BaseResponse newResponse) {
        newResponse.setOriginResultCode(oldResponse.returnCode);
        ErrorInfo errInfo = oldResponse.getErrorInfo();
        if (errInfo != null) {
            errInfo.setSrcTransationId(oldResponse.getSrcTranID());
        }
        newResponse.setErrorInfo(errInfo);
        int i = oldResponse.returnCode;
        if (i != -4) {
            switch (i) {
                case -99:
                case CardServerBaseResponse.RESPONSE_CODE_CANNOT_BE_RESOLVED:
                    break;
                default:
                    switch (i) {
                        case -2:
                            newResponse.setResultCode(3);
                            newResponse.setResultDesc(oldResponse.getResultDesc());
                            return;
                        case -1:
                            newResponse.setResultCode(2);
                            newResponse.setResultDesc(oldResponse.getResultDesc());
                            return;
                        default:
                            switch (i) {
                                case 1:
                                    newResponse.setResultCode(1);
                                    newResponse.setResultDesc(oldResponse.getResultDesc());
                                    Map<String, String> params = new HashMap<>();
                                    String errorInfo = "ServerAccessService Interface, " + oldResponse.getResultDesc() + ", scene : " + newResponse.getClass();
                                    params.put("fail_code", "" + oldResponse.returnCode);
                                    params.put("fail_reason", errorInfo);
                                    LogC.e(errorInfo, false);
                                    return;
                                case 2:
                                    newResponse.setResultCode(4);
                                    newResponse.setResultDesc(oldResponse.getResultDesc());
                                    return;
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                    break;
                                default:
                                    newResponse.setResultCode(oldResponse.returnCode);
                                    newResponse.setResultDesc(oldResponse.getResultDesc());
                                    return;
                            }
                    }
            }
        }
        newResponse.setResultCode(-99);
        newResponse.setResultDesc(oldResponse.getResultDesc());
        Map<String, String> params2 = new HashMap<>();
        String errorInfo2 = "ServerAccessService Interface, " + oldResponse.getResultDesc() + ", scene : " + newResponse.getClass();
        params2.put("fail_code", "" + oldResponse.returnCode);
        params2.put("fail_reason", errorInfo2);
        LogC.e(errorInfo2, false);
    }

    public QueryOrderResponse queryOrder(QueryOrderRequest request) {
        QueryOrderResponse response = new QueryOrderResponse();
        if (request == null) {
            LogX.e("ServerAccessServiceImpl queryOrder, invalid param");
            response.setResultCode(1);
            response.setResultDesc("client check, invalid param");
            return response;
        }
        ServerAccessQueryOrderRequest serverAccessQueryOrderRequest = new ServerAccessQueryOrderRequest(request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getDeviceModel(), request.getSeChipManuFacturer());
        serverAccessQueryOrderRequest.setOrderId(request.getOrderId());
        serverAccessQueryOrderRequest.setOrderStatus(request.getOrderStatus());
        serverAccessQueryOrderRequest.setPhoneNumber(request.getPhoneNumber());
        serverAccessQueryOrderRequest.setReserved(request.getReserved());
        serverAccessQueryOrderRequest.setUserId(request.getAccountUserId());
        serverAccessQueryOrderRequest.setOrderType(request.getOrderType());
        serverAccessQueryOrderRequest.setSn(request.getSn());
        serverAccessQueryOrderRequest.setPartnerId(request.getPartnerId());
        serverAccessQueryOrderRequest.setAppCode(request.getAppCode());
        serverAccessQueryOrderRequest.setOrderType(request.getOrderType());
        ServerAccessQueryOrderResponse res = this.cardServer.queryOrder(serverAccessQueryOrderRequest);
        if (res != null) {
            response.setTransactionId(res.getSrcTranID());
            LogX.i("ServerAccessServiceImpl queryOrder, response = " + res.returnCode);
            if (res.returnCode == 0) {
                response.setResultCode(0);
                response.setBalance(res.getBalance());
                List<ServerAccessQueryOrder> orderList = res.getOrderList();
                if (orderList != null && !orderList.isEmpty()) {
                    List<QueryOrder> orders = new ArrayList<>();
                    for (ServerAccessQueryOrder o : orderList) {
                        QueryOrder order = new QueryOrder();
                        order.setIssuerId(o.getIssuerId());
                        order.setOrderId(o.getOrderId());
                        order.setOrderType(o.getOrderType());
                        order.setStatus(o.getStatus());
                        order.setCurrency(o.getCurrency());
                        order.setAmount(o.getAmount());
                        order.setOrderTime(o.getOrderTime());
                        order.setCplc(o.getCplc());
                        orders.add(order);
                    }
                    response.setOrderList(orders);
                }
            } else {
                translateErrorCode(res, response);
            }
        }
        return response;
    }

    public ApplyOrderResponse applyOrder(ApplyOrderRequest request) {
        ApplyOrderResponse response = new ApplyOrderResponse();
        if (request == null) {
            LogX.e("ServerAccessServiceImpl applyOrder, invalid param");
            response.setResultCode(1);
            response.setResultDesc("client check, invalid param");
            return response;
        }
        ServerAccessApplyOrderRequest serverAccessApplyOrderRequest = new ServerAccessApplyOrderRequest(request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getScene(), request.getDeviceModel(), request.getSeChipManuFacturer());
        serverAccessApplyOrderRequest.setCardId(request.getTrafficCardId());
        serverAccessApplyOrderRequest.setActualIssuePayment(request.getActualIssuePayment());
        serverAccessApplyOrderRequest.setTheoreticalIssuePayment(request.getTheoreticalIssuePayment());
        serverAccessApplyOrderRequest.setActualRecharegePayment(request.getActualRecharegePayment());
        serverAccessApplyOrderRequest.setTheoreticalRecharegePayment(request.getTheoreticalRecharegePayment());
        serverAccessApplyOrderRequest.setTheoreticalCardMovePayment(request.getTheoreticalCardMovePayment());
        serverAccessApplyOrderRequest.setActualCardMovePayment(request.getActualCardMovePayment());
        serverAccessApplyOrderRequest.setCurrency(request.getCurrency());
        serverAccessApplyOrderRequest.setPayType(request.getPayType());
        serverAccessApplyOrderRequest.setPhoneNumber(request.getPhoneNumber());
        serverAccessApplyOrderRequest.setReserved(request.getReserved());
        serverAccessApplyOrderRequest.setTheoreticalIssuePayment(request.getTheoreticalIssuePayment());
        serverAccessApplyOrderRequest.setUserId(request.getAccountUserId());
        serverAccessApplyOrderRequest.setEventId(request.getEventId());
        serverAccessApplyOrderRequest.setBuCardInfo(request.getBuCardInfo());
        serverAccessApplyOrderRequest.setAppCode(request.getAppCode());
        serverAccessApplyOrderRequest.setSn(request.getSn());
        serverAccessApplyOrderRequest.setPartnerId(request.getPartnerId());
        ServerAccessApplyOrderResponse res = this.cardServer.applyOrder(serverAccessApplyOrderRequest);
        if (res != null) {
            response.setTransactionId(res.getSrcTranID());
            LogX.i("ServerAccessServiceImpl applyOrder, response = " + res.returnCode);
            if (res.returnCode == 0) {
                response.setResultCode(0);
                List<ServerAccessApplyOrder> orderList = res.getOrderList();
                if (orderList != null && !orderList.isEmpty()) {
                    List<ApplyOrder> orders = new ArrayList<>();
                    for (ServerAccessApplyOrder o : orderList) {
                        ApplyOrder order = new ApplyOrder();
                        order.setAccessMode(o.getAccessMode());
                        order.setAmount(o.getAmount());
                        order.setApplicationID(o.getApplicationID());
                        order.setCurrency(o.getCurrency());
                        order.setOrderId(o.getOrderId());
                        order.setOrderTime(o.getOrderTime());
                        order.setOrderType(o.getOrderType());
                        order.setPackageName(o.getPackageName());
                        order.setProductDesc(o.getProductDesc());
                        order.setProductName(o.getProductName());
                        order.setServiceCatalog(o.getServiceCatalog());
                        order.setSign(o.getSign());
                        order.setSignType(o.getSignType());
                        order.setSPMerchantId(o.getSPMerchantId());
                        order.setMerchantName(o.getMerchantName());
                        order.setUrl(o.getUrl());
                        order.setUrlVer(o.getUrlVer());
                        order.setSdkChannel(o.getSdkChannel());
                        order.setApplyOrderTn(o.getTn());
                        order.setWechatPayAppId(o.getWxAppId());
                        order.setWechatPayNonceStr(o.getWxNonceStr());
                        order.setWechatPayPartnerId(o.getWxPartnerId());
                        order.setWechatPayPrepayId(o.getWxPrepayId());
                        order.setWechatPayTimeStamp(o.getWxTimeStamp());
                        order.setWechatPayPackageValue(o.getWxPackageValue());
                        orders.add(order);
                    }
                    response.setOrderList(orders);
                }
                response.setTransferOrder(res.getTransferOrder());
                String appCode = res.getAppCode();
                if (appCode != null) {
                    response.setAppCode(appCode);
                }
                response.setWxOrderListJsonString(res.getWxOrderListJsonString());
            } else {
                translateErrorCode(res, response);
            }
        }
        return response;
    }

    private ServerAccessTransferOutRequest buildServerTransferOutRequest(TransferOutRequest request) {
        ServerAccessTransferOutRequest cloudTransferOutReq = new ServerAccessTransferOutRequest(request.getEventId(), request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getSeChipManuFacturer(), request.getDeviceModel(), request.getTrafficCardId(), request.getBalance());
        cloudTransferOutReq.setOrderId(request.getOrderId());
        cloudTransferOutReq.setPhoneNumber(request.getPhoneNumber());
        cloudTransferOutReq.setSn(request.getSn());
        cloudTransferOutReq.setPhoneManufacturer(request.getPhoneManufacturer());
        cloudTransferOutReq.setReserved(request.getReserved());
        cloudTransferOutReq.setExtend(request.getExtend());
        cloudTransferOutReq.setAppCode(request.getAppCode());
        cloudTransferOutReq.setPartnerId(request.getPartnerId());
        cloudTransferOutReq.setTransferVerifyFlag(request.getTransferVerifyFlag());
        return cloudTransferOutReq;
    }

    public TransferOutResponse cloudTransferOut(TransferOutRequest request) {
        return cloudTransferOut(request, 0);
    }

    public TransferOutResponse cloudTransferOut(TransferOutRequest request, int mediaType) {
        TransferOutResponse cloudTransferOutResponse;
        boolean z;
        ServerAccessTransferOutResponse res;
        ServerAccessTransferOutResponse res2;
        ChannelID channel;
        TransferOutResponse cloudTransferOutResponse2 = new TransferOutResponse();
        if (request == null) {
            LogX.e("ServerAccessServiceImpl cloudTransferOut, invalid param", false);
            cloudTransferOutResponse2.setResultCode(1);
            cloudTransferOutResponse2.setResultDesc("client check, invalid param");
            return cloudTransferOutResponse2;
        }
        ServerAccessTransferOutRequest cloudTransferOutReq = buildServerTransferOutRequest(request);
        try {
            ServerAccessTransferOutResponse res3 = this.cardServer.cloudTransferOut(cloudTransferOutReq);
            if (res3 != null) {
                try {
                    String srcTranID = res3.getSrcTranID();
                    cloudTransferOutResponse2.setTransactionId(srcTranID);
                    String transactionId = res3.getTransactionId();
                    LogX.i("ServerAccessServiceImpl transferOut, response = " + res3.returnCode);
                    if (res3.returnCode == 0) {
                        try {
                            List<ServerAccessAPDU> apduList = res3.getApduList();
                            if (apduList == null) {
                                List<ServerAccessAPDU> list = apduList;
                                String str = srcTranID;
                                res2 = res3;
                                ServerAccessTransferOutRequest serverAccessTransferOutRequest = cloudTransferOutReq;
                                cloudTransferOutResponse = cloudTransferOutResponse2;
                            } else if (apduList.isEmpty()) {
                                List<ServerAccessAPDU> list2 = apduList;
                                String str2 = srcTranID;
                                res2 = res3;
                                ServerAccessTransferOutRequest serverAccessTransferOutRequest2 = cloudTransferOutReq;
                                cloudTransferOutResponse = cloudTransferOutResponse2;
                            } else {
                                HashMap hashMap = new HashMap();
                                hashMap.put(ServerAccessApplyAPDURequest.ReqKey.AID, cloudTransferOutReq.getAppletAid());
                                hashMap.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, cloudTransferOutReq.getIssuerId());
                                hashMap.put("cplc", cloudTransferOutReq.getCplc());
                                LogX.i("ServerAccessServiceImpl has Content", false);
                                if (this.mChannel == null) {
                                    try {
                                        channel = new ChannelID();
                                        try {
                                            channel.setMediaType(mediaType);
                                        } catch (Exception e) {
                                        }
                                    } catch (Exception e2) {
                                        int i = mediaType;
                                        z = false;
                                        cloudTransferOutResponse = cloudTransferOutResponse2;
                                        LogX.i("ServerAccessServiceImpl apduList == Exception", z);
                                        return cloudTransferOutResponse;
                                    }
                                } else {
                                    int i2 = mediaType;
                                    channel = this.mChannel;
                                }
                                ChannelID channel2 = channel;
                                String deviceModel = cloudTransferOutReq.getDeviceModel();
                                String seChipManuFacturer = cloudTransferOutReq.getSeChipManuFacturer();
                                String str3 = seChipManuFacturer;
                                HashMap hashMap2 = hashMap;
                                List<ServerAccessAPDU> list3 = apduList;
                                String str4 = srcTranID;
                                res2 = res3;
                                ServerAccessTransferOutRequest serverAccessTransferOutRequest3 = cloudTransferOutReq;
                                cloudTransferOutResponse = cloudTransferOutResponse2;
                                try {
                                    executeCommand(transactionId, apduList, cloudTransferOutResponse2, hashMap, deviceModel, str3, channel2, 1, res3.getNextStep(), cloudTransferOutReq.getPartnerId(), cloudTransferOutReq.getSrcTransactionID(), false, res3.getErrorInfo());
                                    res = res2;
                                } catch (Exception e3) {
                                    ServerAccessTransferOutResponse serverAccessTransferOutResponse = res2;
                                    z = false;
                                }
                            }
                        } catch (Exception e4) {
                            ServerAccessTransferOutResponse serverAccessTransferOutResponse2 = res3;
                            ServerAccessTransferOutRequest serverAccessTransferOutRequest4 = cloudTransferOutReq;
                            cloudTransferOutResponse = cloudTransferOutResponse2;
                            z = false;
                            LogX.i("ServerAccessServiceImpl apduList == Exception", z);
                            return cloudTransferOutResponse;
                        }
                        try {
                            cloudTransferOutResponse.setResultCode(0);
                            LogX.i("ServerAccessServiceImpl apduList==null", false);
                            res = res2;
                        } catch (Exception e5) {
                            z = false;
                            ServerAccessTransferOutResponse serverAccessTransferOutResponse3 = res2;
                            LogX.i("ServerAccessServiceImpl apduList == Exception", z);
                            return cloudTransferOutResponse;
                        }
                    } else {
                        String str5 = srcTranID;
                        ServerAccessTransferOutRequest serverAccessTransferOutRequest5 = cloudTransferOutReq;
                        cloudTransferOutResponse = cloudTransferOutResponse2;
                        z = false;
                        res = res3;
                        try {
                            translateErrorCode(res, cloudTransferOutResponse);
                        } catch (Exception e6) {
                            ServerAccessTransferOutResponse serverAccessTransferOutResponse4 = res;
                        }
                    }
                } catch (Exception e7) {
                    ServerAccessTransferOutResponse serverAccessTransferOutResponse5 = res3;
                    ServerAccessTransferOutRequest serverAccessTransferOutRequest6 = cloudTransferOutReq;
                    z = false;
                    cloudTransferOutResponse = cloudTransferOutResponse2;
                    LogX.i("ServerAccessServiceImpl apduList == Exception", z);
                    return cloudTransferOutResponse;
                }
            } else {
                res = res3;
                ServerAccessTransferOutRequest serverAccessTransferOutRequest7 = cloudTransferOutReq;
                cloudTransferOutResponse = cloudTransferOutResponse2;
            }
        } catch (Exception e8) {
            ServerAccessTransferOutRequest serverAccessTransferOutRequest8 = cloudTransferOutReq;
            z = false;
            cloudTransferOutResponse = cloudTransferOutResponse2;
            LogX.i("ServerAccessServiceImpl apduList == Exception", z);
            return cloudTransferOutResponse;
        }
        return cloudTransferOutResponse;
    }

    public TransferOutResponse checkCloudTransferOut(TransferOutRequest request) {
        ServerAccessTransferOutResponse res;
        TransferOutResponse checkCloudTransferResponse = new TransferOutResponse();
        if (request == null) {
            LogX.e("ServerAccessServiceImpl checkCloudTransferOut, invalid param");
            checkCloudTransferResponse.setResultCode(1);
            checkCloudTransferResponse.setResultDesc("client check, invalid param");
            return checkCloudTransferResponse;
        }
        ServerAccessTransferOutRequest serverAccessTransferOutRequest = new ServerAccessTransferOutRequest(request.getEventId(), request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getSeChipManuFacturer(), request.getDeviceModel(), request.getTrafficCardId(), request.getBalance());
        ServerAccessTransferOutRequest checkCloudTransferRequset = serverAccessTransferOutRequest;
        checkCloudTransferRequset.setOrderId(request.getOrderId());
        checkCloudTransferRequset.setPhoneNumber(request.getPhoneNumber());
        checkCloudTransferRequset.setSn(request.getSn());
        checkCloudTransferRequset.setPhoneManufacturer(request.getPhoneManufacturer());
        checkCloudTransferRequset.setReserved(request.getReserved());
        checkCloudTransferRequset.setExtend(request.getExtend());
        checkCloudTransferRequset.setAppCode(request.getAppCode());
        checkCloudTransferRequset.setPartnerId(request.getPartnerId());
        checkCloudTransferRequset.setTransferVerifyFlag(request.getTransferVerifyFlag());
        ServerAccessTransferOutResponse res2 = this.cardServer.checkCloudTransferOut(checkCloudTransferRequset);
        if (res2 != null) {
            String srcTranID = res2.getSrcTranID();
            checkCloudTransferResponse.setTransactionId(srcTranID);
            String transactionId = res2.getTransactionId();
            LogX.i("ServerAccessServiceImpl checkCloudTransferOut, response = " + res2.returnCode);
            if (res2.returnCode == 0) {
                List<ServerAccessAPDU> apduList = res2.getApduList();
                if (apduList == null) {
                    List<ServerAccessAPDU> list = apduList;
                    String str = srcTranID;
                    res = res2;
                    ServerAccessTransferOutRequest serverAccessTransferOutRequest2 = checkCloudTransferRequset;
                } else if (apduList.isEmpty()) {
                    List<ServerAccessAPDU> list2 = apduList;
                    String str2 = srcTranID;
                    res = res2;
                    ServerAccessTransferOutRequest serverAccessTransferOutRequest3 = checkCloudTransferRequset;
                } else {
                    HashMap hashMap = new HashMap();
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.AID, checkCloudTransferRequset.getAppletAid());
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, checkCloudTransferRequset.getIssuerId());
                    hashMap.put("cplc", checkCloudTransferRequset.getCplc());
                    LogX.i("ServerAccessServiceImpl has Content", false);
                    ChannelID channel = new ChannelID();
                    String deviceModel = checkCloudTransferRequset.getDeviceModel();
                    String seChipManuFacturer = checkCloudTransferRequset.getSeChipManuFacturer();
                    String nextStep = res2.getNextStep();
                    String partnerId = checkCloudTransferRequset.getPartnerId();
                    HashMap hashMap2 = hashMap;
                    HashMap hashMap3 = hashMap;
                    String str3 = nextStep;
                    List<ServerAccessAPDU> list3 = apduList;
                    String str4 = partnerId;
                    String str5 = srcTranID;
                    String srcTranID2 = checkCloudTransferRequset.getSrcTransactionID();
                    res = res2;
                    ServerAccessTransferOutRequest serverAccessTransferOutRequest4 = checkCloudTransferRequset;
                    executeCommand(transactionId, apduList, checkCloudTransferResponse, hashMap2, deviceModel, seChipManuFacturer, channel, true, str3, str4, srcTranID2, false, res2.getErrorInfo());
                    ServerAccessTransferOutResponse serverAccessTransferOutResponse = res;
                }
                checkCloudTransferResponse.setResultCode(0);
                LogX.i("ServerAccessServiceImpl apduList==null", false);
                ServerAccessTransferOutResponse serverAccessTransferOutResponse2 = res;
            } else {
                ServerAccessTransferOutRequest serverAccessTransferOutRequest5 = checkCloudTransferRequset;
                translateErrorCode(res2, checkCloudTransferResponse);
            }
        } else {
            ServerAccessTransferOutRequest serverAccessTransferOutRequest6 = checkCloudTransferRequset;
        }
        return checkCloudTransferResponse;
    }

    public TransferOutResponse transferOut(TransferOutRequest request) {
        return transferOut(request, 0);
    }

    public TransferOutResponse transferOut(TransferOutRequest request, int mediaType) {
        ServerAccessTransferOutResponse res;
        TransferOutResponse response = new TransferOutResponse();
        if (request == null) {
            LogX.e("ServerAccessServiceImpl transferOut, invalid param");
            response.setResultCode(1);
            response.setResultDesc("client check, invalid param");
            return response;
        }
        ServerAccessTransferOutRequest serverAccessTransferOutRequest = new ServerAccessTransferOutRequest(request.getEventId(), request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getSeChipManuFacturer(), request.getDeviceModel(), request.getTrafficCardId(), request.getBalance());
        ServerAccessTransferOutRequest req = serverAccessTransferOutRequest;
        req.setOrderId(request.getOrderId());
        req.setPhoneNumber(request.getPhoneNumber());
        req.setSn(request.getSn());
        req.setPhoneManufacturer(request.getPhoneManufacturer());
        req.setReserved(request.getReserved());
        req.setExtend(request.getExtend());
        req.setAppCode(request.getAppCode());
        req.setPartnerId(request.getPartnerId());
        req.setTransferVerifyFlag(request.getTransferVerifyFlag());
        ServerAccessTransferOutResponse res2 = this.cardServer.transferOut(req);
        if (res2 != null) {
            String srcTranID = res2.getSrcTranID();
            response.setTransactionId(srcTranID);
            String transactionId = res2.getTransactionId();
            LogX.i("ServerAccessServiceImpl transferOut, response = " + res2.returnCode);
            if (res2.returnCode == 0) {
                List<ServerAccessAPDU> apduList = res2.getApduList();
                if (apduList == null) {
                    List<ServerAccessAPDU> list = apduList;
                    String str = srcTranID;
                    res = res2;
                    ServerAccessTransferOutRequest serverAccessTransferOutRequest2 = req;
                } else if (apduList.isEmpty()) {
                    List<ServerAccessAPDU> list2 = apduList;
                    String str2 = srcTranID;
                    res = res2;
                    ServerAccessTransferOutRequest serverAccessTransferOutRequest3 = req;
                } else {
                    HashMap hashMap = new HashMap();
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.AID, req.getAppletAid());
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, req.getIssuerId());
                    hashMap.put("cplc", req.getCplc());
                    LogX.i("ServerAccessServiceImpl has Content", false);
                    ChannelID channel = new ChannelID();
                    channel.setMediaType(mediaType);
                    String deviceModel = req.getDeviceModel();
                    String seChipManuFacturer = req.getSeChipManuFacturer();
                    String nextStep = res2.getNextStep();
                    String partnerId = req.getPartnerId();
                    ChannelID channelID = channel;
                    HashMap hashMap2 = hashMap;
                    String str3 = nextStep;
                    List<ServerAccessAPDU> list3 = apduList;
                    String str4 = partnerId;
                    String str5 = srcTranID;
                    String srcTranID2 = req.getSrcTransactionID();
                    res = res2;
                    ServerAccessTransferOutRequest serverAccessTransferOutRequest4 = req;
                    executeCommand(transactionId, apduList, response, hashMap, deviceModel, seChipManuFacturer, channel, 1, str3, str4, srcTranID2, false, res2.getErrorInfo());
                    ServerAccessTransferOutResponse serverAccessTransferOutResponse = res;
                }
                response.setResultCode(0);
                LogX.i("ServerAccessServiceImpl apduList==null", false);
                ServerAccessTransferOutResponse serverAccessTransferOutResponse2 = res;
            } else {
                ServerAccessTransferOutRequest serverAccessTransferOutRequest5 = req;
                translateErrorCode(res2, response);
            }
        } else {
            ServerAccessTransferOutRequest serverAccessTransferOutRequest6 = req;
        }
        return response;
    }
}
