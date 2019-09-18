package com.huawei.wallet.sdk.common.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.TransferOrder;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessApplyOrder;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessApplyOrderRequest;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessApplyOrderResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessApplyOrderTask extends HttpConnTask<ServerAccessApplyOrderResponse, ServerAccessApplyOrderRequest> {
    private static final String HEAD_COMMANDER = "create.order";
    private StringBuilder builder = new StringBuilder();

    public ServerAccessApplyOrderTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessApplyOrderRequest request) {
        if (request == null || StringUtil.isEmpty(request.getIssuerId(), true) || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getAppletAid(), true) || StringUtil.isEmpty(request.getPayType(), true) || StringUtil.isEmpty(request.getScene(), true) || StringUtil.isEmpty(request.getDeviceModel(), true) || StringUtil.isEmpty(request.getSeChipManuFacturer(), true)) {
            LogX.e("ServerAccessApplyOrderTask prepareRequestStr, invalid param");
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "create.order", request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessageJson = reportRequestMessage(request);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessApplyOrderTask prepareRequestStr, commander= create.order reportRequestMessageJson= " + reportRequestMessageJson);
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(ServerAccessApplyOrderRequest request) {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put("payType", request.getPayType());
            jObj.put("changeType", request.getScene());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            if (!StringUtil.isEmpty(request.getTheoreticalIssuePayment(), true)) {
                jObj.put("priceEnroll", request.getTheoreticalIssuePayment());
            }
            if (!StringUtil.isEmpty(request.getActualIssuePayment(), true)) {
                jObj.put("amountEnroll", request.getActualIssuePayment());
            }
            if (!StringUtil.isEmpty(request.getTheoreticalRecharegePayment(), true)) {
                jObj.put("priceRecharge", request.getTheoreticalRecharegePayment());
            }
            if (!StringUtil.isEmpty(request.getActualRecharegePayment(), true)) {
                jObj.put("amountRecharge", request.getActualRecharegePayment());
            }
            if (!StringUtil.isEmpty(request.getCurrency(), true)) {
                jObj.put("currency", request.getCurrency());
            }
            if (!StringUtil.isEmpty(request.getActualCardMovePayment(), true)) {
                jObj.put("amountCardMove", request.getActualCardMovePayment());
            }
            if (!StringUtil.isEmpty(request.getTheoreticalCardMovePayment(), true)) {
                jObj.put("priceCardMove", request.getTheoreticalCardMovePayment());
            }
            if (!StringUtil.isEmpty(request.getEventId(), true)) {
                jObj.put("eventid", request.getEventId());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
            if (!StringUtil.isEmpty(request.getAppCode(), true)) {
                jObj.put("appCode", request.getAppCode());
            }
            if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
                jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            }
            if (StringUtil.isEmpty(request.getCardId(), true)) {
                return jObj;
            }
            jObj.put("cardNo", "***");
            return jObj;
        } catch (JSONException e) {
            LogX.e("ServerAccessApplyOrderTask reportRequestMessage, JSONException");
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, ServerAccessApplyOrderRequest request) {
        JSONObject jObj;
        if (headerObject == null) {
            LogX.e("ServerAccessApplyOrderTask createDataStr, invalid param");
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put("cplc", request.getCplc());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put("payType", request.getPayType());
            jObj.put("changeType", request.getScene());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            if (!StringUtil.isEmpty(request.getTheoreticalIssuePayment(), true)) {
                jObj.put("priceEnroll", request.getTheoreticalIssuePayment());
            }
            if (!StringUtil.isEmpty(request.getActualIssuePayment(), true)) {
                jObj.put("amountEnroll", request.getActualIssuePayment());
            }
            if (!StringUtil.isEmpty(request.getTheoreticalRecharegePayment(), true)) {
                jObj.put("priceRecharge", request.getTheoreticalRecharegePayment());
            }
            if (!StringUtil.isEmpty(request.getActualRecharegePayment(), true)) {
                jObj.put("amountRecharge", request.getActualRecharegePayment());
            }
            if (!StringUtil.isEmpty(request.getUserId(), true)) {
                jObj.put("userid", request.getUserId());
            }
            if (!StringUtil.isEmpty(request.getPhoneNumber(), true)) {
                jObj.put("phoneNumber", request.getPhoneNumber());
            }
            if (!StringUtil.isEmpty(request.getCardId(), true)) {
                jObj.put("cardNo", request.getCardId());
                LogX.i("ServerAccessApplyOrderTask cardNo is not null");
            }
            if (!StringUtil.isEmpty(request.getCurrency(), true)) {
                jObj.put("currency", request.getCurrency());
            }
            if (!StringUtil.isEmpty(request.getActualCardMovePayment(), true)) {
                jObj.put("amountCardMove", request.getActualCardMovePayment());
            }
            if (!StringUtil.isEmpty(request.getTheoreticalCardMovePayment(), true)) {
                jObj.put("priceCardMove", request.getTheoreticalCardMovePayment());
            }
            if (!StringUtil.isEmpty(request.getEventId(), true)) {
                jObj.put("eventid", request.getEventId());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
            initJObj(request, jObj);
        } catch (JSONException e) {
            LogX.e("ServerAccessApplyOrderTask createDataStr, JSONException");
            jObj = null;
        }
        return jObj;
    }

    private void initJObj(ServerAccessApplyOrderRequest request, JSONObject jObj) throws JSONException {
        if (!StringUtil.isEmpty(request.getAppCode(), true)) {
            jObj.put("appCode", request.getAppCode());
        }
        if (!StringUtil.isEmpty(request.getSn(), true)) {
            jObj.put(SNBConstant.FIELD_IMEI, request.getSn());
        }
        if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
            jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
        }
        if (!StringUtil.isEmpty(request.getBuCardInfo(), true)) {
            jObj.put("buCardInfo", request.getBuCardInfo());
        }
    }

    /* access modifiers changed from: protected */
    public ServerAccessApplyOrderResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessApplyOrderResponse response = new ServerAccessApplyOrderResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessApplyOrderTask readErrorResponse, commander= create.order errorCode= " + errorCode + " errorMessage= " + errorMessage);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessApplyOrderResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessApplyOrderResponse response = new ServerAccessApplyOrderResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        getSrcTranId(response, dataObject);
        if (returnCode == 0) {
            JSONArray orderArr = null;
            try {
                if (dataObject.has("orderList")) {
                    orderArr = dataObject.getJSONArray("orderList");
                }
                if (dataObject.has("cupOrderList")) {
                    orderArr = dataObject.getJSONArray("cupOrderList");
                }
                if (dataObject.has("wechatOrderList")) {
                    orderArr = dataObject.getJSONArray("wechatOrderList");
                }
                if (orderArr != null) {
                    List<ServerAccessApplyOrder> orderList = new ArrayList<>();
                    int n = orderArr.length();
                    for (int i = 0; i < n; i++) {
                        ServerAccessApplyOrder order = ServerAccessApplyOrder.buildFromJson(orderArr.getJSONObject(i));
                        if (order != null) {
                            orderList.add(order);
                        }
                    }
                    response.setOrderList(orderList);
                    StringBuilder sb = this.builder;
                    sb.append(" orderList=");
                    sb.append(orderList);
                }
                if (dataObject.has("transferOrder")) {
                    JSONObject orderObject = dataObject.getJSONObject("transferOrder");
                    TransferOrder order2 = new TransferOrder();
                    order2.setOrderNum(JSONHelper.getStringValue(orderObject, "orderNo"));
                    order2.setOrderType(JSONHelper.getStringValue(orderObject, "orderType"));
                    order2.setDateTime(JSONHelper.getStringValue(orderObject, "orderTime"));
                    order2.setUserId(JSONHelper.getStringValue(orderObject, "userid"));
                    order2.setCplc(JSONHelper.getStringValue(orderObject, "cplc"));
                    order2.setAppletType(JSONHelper.getStringValue(orderObject, ServerAccessApplyAPDURequest.ReqKey.AID));
                    order2.setOrderStatus(JSONHelper.getStringValue(orderObject, Constants.FIELD_APPLET_CONFIG_STATUS));
                    response.setTransferOrder(order2);
                    StringBuilder sb2 = this.builder;
                    sb2.append(" setTransferOrder=");
                    sb2.append(order2);
                }
                if (dataObject.has("appCode")) {
                    response.setAppCode(dataObject.getString("appCode"));
                    StringBuilder sb3 = this.builder;
                    sb3.append(" appCode=");
                    sb3.append(dataObject.getString("appCode"));
                }
                if (dataObject.has("wechatOrderList")) {
                    response.setWxOrderListJsonString(dataObject.getString("wechatOrderList"));
                    StringBuilder sb4 = this.builder;
                    sb4.append(" wechatOrderList=");
                    sb4.append(dataObject.getString("wechatOrderList"));
                }
            } catch (JSONException e) {
                LogX.e("ServerAccessApplyOrderTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        if (!isDebugBuild()) {
            LogX.i("ServerAccessApplyOrderTask readSuccessResponse, commander= create.order returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + this.builder.toString());
        }
        return response;
    }

    private void getSrcTranId(ServerAccessApplyOrderResponse response, JSONObject dataObject) {
        if (dataObject != null) {
            try {
                if (dataObject.has("header")) {
                    JSONObject header = dataObject.getJSONObject("header");
                    if (header != null) {
                        String srcTranId = header.getString("srcTranID");
                        response.setSrcTranID(srcTranId);
                        StringBuilder sb = this.builder;
                        sb.append("srcTranId=");
                        sb.append(srcTranId);
                    }
                }
            } catch (JSONException e) {
                LogX.e("ServerAccessApplyOrderTask getSrcTransationId, parse header JSONException");
                response.returnCode = -99;
            }
        }
    }
}
