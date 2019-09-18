package com.huawei.wallet.sdk.common.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessQueryOrder;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessQueryOrderRequest;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessQueryOrderTask extends HttpConnTask<ServerAccessQueryOrderResponse, ServerAccessQueryOrderRequest> {
    private static final String HEAD_COMMANDER = "query.order";
    private StringBuilder builder = new StringBuilder();

    public ServerAccessQueryOrderTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessQueryOrderRequest request) {
        if (request == null || StringUtil.isEmpty(request.getIssuerId(), true) || StringUtil.isEmpty(request.getAppletAid(), true) || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getDeviceModel(), true) || StringUtil.isEmpty(request.getSeChipManuFacturer(), true)) {
            LogX.e("ServerAccessQueryOrderTask prepareRequestStr, invalid param");
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "query.order", request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessageJson = reportRequestMessage(request);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessQueryOrderTask prepareRequestStr, commander= query.order reportRequestMessageJson= " + reportRequestMessageJson);
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(ServerAccessQueryOrderRequest request) {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            if (!StringUtil.isEmpty(request.getOrderId(), true)) {
                jObj.put("orderNo", request.getOrderId());
            }
            if (!StringUtil.isEmpty(request.getOrderStatus(), true)) {
                jObj.put("orderStatus", request.getOrderStatus());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
            if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
                jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            }
            if (!StringUtil.isEmpty(request.getAppCode(), true)) {
                jObj.put("appCode", request.getAppCode());
            }
            if (StringUtil.isEmpty(request.getOrderType(), true)) {
                return jObj;
            }
            jObj.put("orderType", request.getOrderType());
            return jObj;
        } catch (JSONException e) {
            LogX.e("ServerAccessQueryOrderTask reportRequestMessage, JSONException");
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, ServerAccessQueryOrderRequest request) {
        JSONObject jObj;
        if (headerObject == null) {
            LogX.e("ServerAccessQueryOrderTask createDataStr, invalid param");
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put("cplc", request.getCplc());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            if (!StringUtil.isEmpty(request.getPhoneNumber(), true)) {
                jObj.put("phoneNumber", request.getPhoneNumber());
            }
            if (!StringUtil.isEmpty(request.getUserId(), true)) {
                jObj.put("userid", request.getUserId());
            }
            if (!StringUtil.isEmpty(request.getOrderId(), true)) {
                jObj.put("orderNo", request.getOrderId());
            }
            if (!StringUtil.isEmpty(request.getOrderStatus(), true)) {
                jObj.put("orderStatus", request.getOrderStatus());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
            if (!StringUtil.isEmpty(request.getSn(), true)) {
                jObj.put(SNBConstant.FIELD_IMEI, request.getSn());
            }
            if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
                jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            }
            if (!StringUtil.isEmpty(request.getAppCode(), true)) {
                jObj.put("appCode", request.getAppCode());
            }
            if (!StringUtil.isEmpty(request.getOrderType(), true)) {
                jObj.put("orderType", request.getOrderType());
            }
        } catch (JSONException e) {
            LogX.e("ServerAccessQueryOrderTask createDataStr, JSONException");
            jObj = null;
        }
        return jObj;
    }

    /* access modifiers changed from: protected */
    public ServerAccessQueryOrderResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessQueryOrderResponse response = new ServerAccessQueryOrderResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessQueryOrderTask readErrorResponse, commander= query.order errorCode= " + errorCode + " errorMessage= " + errorMessage);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessQueryOrderResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessQueryOrderResponse response = new ServerAccessQueryOrderResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        setErrorInfo(dataObject, response);
        getSrcTranId(response, dataObject);
        if (dataObject != null && dataObject.has("errorInfo")) {
            ErrorInfo errorInfo = null;
            try {
                errorInfo = ErrorInfo.build(dataObject.getJSONObject("errorInfo"));
            } catch (JSONException e) {
                LogX.e("ServerAccessQueryOrderTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
            response.setErrorInfo(errorInfo);
            StringBuilder sb = this.builder;
            sb.append(" setErrorInfo=");
            sb.append(errorInfo);
        }
        if (returnCode == 0) {
            JSONArray orderArray = null;
            try {
                if (dataObject.has("orderList")) {
                    orderArray = dataObject.getJSONArray("orderList");
                }
                if (orderArray != null) {
                    List<ServerAccessQueryOrder> orderList = new ArrayList<>();
                    int n = orderArray.length();
                    for (int i = 0; i < n; i++) {
                        ServerAccessQueryOrder order = ServerAccessQueryOrder.buildFromJson(orderArray.getJSONObject(i));
                        if (order != null) {
                            orderList.add(order);
                        }
                    }
                    response.setOrderList(orderList);
                    StringBuilder sb2 = this.builder;
                    sb2.append(" orderList=");
                    sb2.append(orderList);
                }
                if (dataObject.has(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE)) {
                    response.setBalance(dataObject.getInt(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE));
                    StringBuilder sb3 = this.builder;
                    sb3.append(" balance=");
                    sb3.append(dataObject.getInt(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE));
                }
            } catch (JSONException e2) {
                LogX.e("ServerAccessQueryOrderTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        if (!isDebugBuild()) {
            LogX.i("ServerAccessQueryOrderTask readSuccessResponse, commander= query.order returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + this.builder.toString());
        }
        return response;
    }

    private void getSrcTranId(ServerAccessQueryOrderResponse response, JSONObject dataObject) {
        if (dataObject != null) {
            try {
                if (dataObject.has("header")) {
                    JSONObject header = dataObject.getJSONObject("header");
                    if (header != null) {
                        String srcTranId = header.getString("srcTranID");
                        response.setSrcTranID(srcTranId);
                        StringBuilder sb = this.builder;
                        sb.append("srcTranID=");
                        sb.append(srcTranId);
                    }
                }
            } catch (JSONException e) {
                LogX.e("ServerAccessQueryOrderTask getSrcTransationId, JSONException");
                response.returnCode = -99;
            }
        }
    }
}
