package com.huawei.wallet.sdk.common.apdu.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessDeleteAppletTask extends HttpConnTask<ServerAccessDeleteAppletResponse, ServerAccessDeleteAppletRequest> {
    private static final String HEAD_COMMANDER = "delete.app";
    private StringBuilder builder = new StringBuilder();

    public ServerAccessDeleteAppletTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessDeleteAppletRequest request) {
        if (request == null || StringUtil.isEmpty(request.getIssuerId(), true) || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getAppletAid(), true) || StringUtil.isEmpty(request.getDeviceModel(), true) || StringUtil.isEmpty(request.getSeChipManuFacturer(), true)) {
            LogC.e("ServerAccessDeleteAppletTask prepareRequestStr, invalid param", false);
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "delete.app", request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessageJson = reportRequestMessage(request);
        if (!isDebugBuild()) {
            LogC.i("ServerAccessDeleteAppletTask prepareRequestStr, commander= delete.app reportRequestMessageJson= " + reportRequestMessageJson, true);
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(ServerAccessDeleteAppletRequest request) {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            if (!StringUtil.isEmpty(request.getPhoneManufacturer(), true)) {
                jObj.put("phoneManufacturer", request.getPhoneManufacturer());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
            if (!StringUtil.isEmpty(request.getRefundTicketId(), true)) {
                jObj.put("refundTicketid", request.getRefundTicketId());
            }
            if (!StringUtil.isEmpty(request.getReason(), true)) {
                jObj.put("reason", request.getReason());
            }
            if (!StringUtil.isEmpty(request.getAppCode(), true)) {
                jObj.put("appCode", request.getAppCode());
            }
            if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
                jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            }
            if (!StringUtil.isEmpty(request.getSource(), true)) {
                jObj.put("source", request.getSource());
            }
            if (!StringUtil.isEmpty(request.getOnlyDeleteApplet(), true)) {
                jObj.put("onlyDeleteApplet", request.getOnlyDeleteApplet());
            }
            if (StringUtil.isEmpty(request.getCardBalance(), true)) {
                return jObj;
            }
            jObj.put("cardBalance", "***");
            return jObj;
        } catch (JSONException e) {
            LogC.e("ServerAccessDeleteAppletTask reportRequestMessage, JSONException", false);
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, ServerAccessDeleteAppletRequest request) {
        JSONObject jObj;
        if (headerObject == null) {
            LogC.e("ServerAccessDeleteAppletTask createDataStr, invalid param", false);
            return null;
        }
        try {
            JSONObject jObj2 = new JSONObject();
            jObj2.put("header", headerObject);
            jObj2.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj2.put("cplc", request.getCplc());
            jObj2.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj2.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj2.put("seChipManuFacturer", request.getSeChipManuFacturer());
            if (!StringUtil.isEmpty(request.getPhoneNumber(), true)) {
                jObj2.put("phoneNumber", request.getPhoneNumber());
            }
            if (!StringUtil.isEmpty(request.getUserId(), true)) {
                jObj2.put("userid", request.getUserId());
            }
            if (!StringUtil.isEmpty(request.getSn(), true)) {
                jObj2.put(SNBConstant.FIELD_IMEI, request.getSn());
            }
            if (!StringUtil.isEmpty(request.getPhoneManufacturer(), true)) {
                jObj2.put("phoneManufacturer", request.getPhoneManufacturer());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj2.put("reserved", request.getReserved());
            }
            if (!StringUtil.isEmpty(request.getCardId(), true)) {
                jObj2.put("cardNo", request.getCardId());
            }
            if (!StringUtil.isEmpty(request.getRefundTicketId(), true)) {
                jObj2.put("refundTicketid", request.getRefundTicketId());
            }
            if (!StringUtil.isEmpty(request.getReason(), true)) {
                jObj2.put("reason", request.getReason());
            }
            if (!StringUtil.isEmpty(request.getFlag(), true)) {
                jObj2.put("flag", request.getFlag());
            }
            if (!StringUtil.isEmpty(request.getOrderNo(), true)) {
                jObj2.put("orderNo", request.getOrderNo());
            }
            jObj = parseJson(jObj2, request);
        } catch (JSONException e) {
            LogC.e("ServerAccessDeleteAppletTask createDataStr, JSONException", false);
            jObj = null;
        }
        return jObj;
    }

    private JSONObject parseJson(JSONObject jObj, ServerAccessDeleteAppletRequest request) throws JSONException {
        if (!StringUtil.isEmpty(request.getAppCode(), true)) {
            jObj.put("appCode", request.getAppCode());
        }
        if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
            jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
        }
        if (!StringUtil.isEmpty(request.getSource(), true)) {
            jObj.put("source", request.getSource());
        }
        if (!StringUtil.isEmpty(request.getOnlyDeleteApplet(), true)) {
            jObj.put("onlyDeleteApplet", request.getOnlyDeleteApplet());
        }
        if (!StringUtil.isEmpty(request.getCardBalance(), true)) {
            jObj.put("cardBalance", request.getCardBalance());
        }
        if (!StringUtil.isEmpty(request.getFlag(), true)) {
            jObj.put("flag", request.getFlag());
        }
        if (!StringUtil.isEmpty(request.getOrderNo(), true)) {
            jObj.put("orderNo", request.getOrderNo());
        }
        if (!StringUtil.isEmpty(request.getRefundAccountNumber(), true)) {
            jObj.put("refundAccountNumber", request.getRefundAccountNumber());
        }
        if (!StringUtil.isEmpty(request.getRefundAccountType(), true)) {
            jObj.put("refundAccountType", request.getRefundAccountType());
        }
        return jObj;
    }

    /* access modifiers changed from: protected */
    public ServerAccessDeleteAppletResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessDeleteAppletResponse response = new ServerAccessDeleteAppletResponse();
        response.setResultDesc(errorMessage);
        response.returnCode = errorCode;
        if (!isDebugBuild()) {
            LogC.i("ServerAccessDeleteAppletTask readErrorResponse, commander= delete.app errorCode= " + errorCode + " errorMessage= " + errorMessage, true);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessDeleteAppletResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessDeleteAppletResponse response = new ServerAccessDeleteAppletResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        setErrorInfo(dataObject, response);
        getSrcTranId(response, dataObject);
        if (dataObject != null && dataObject.has("errorInfo")) {
            ErrorInfo errorInfo = null;
            try {
                errorInfo = ErrorInfo.build(dataObject.getJSONObject("errorInfo"));
            } catch (JSONException e) {
                LogC.e("ServerAccessDeleteAppletTask readSuccessResponse, JSONException", false);
                response.returnCode = -99;
            }
            response.setErrorInfo(errorInfo);
            StringBuilder sb = this.builder;
            sb.append(" setErrorInfo=");
            sb.append(errorInfo);
        }
        if (returnCode == 0 && dataObject != null) {
            try {
                response.setTransactionId(JSONHelper.getStringValue(dataObject, "transactionid"));
                StringBuilder sb2 = this.builder;
                sb2.append(" transactionid=");
                sb2.append(JSONHelper.getStringValue(dataObject, "transactionid"));
                if (dataObject.has(ExecuteApduTask.NEXT_STEP)) {
                    response.setNextStep(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
                    StringBuilder sb3 = this.builder;
                    sb3.append(" nextStep=");
                    sb3.append(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
                }
                JSONArray apduArr = null;
                if (dataObject.has("apduList")) {
                    apduArr = dataObject.getJSONArray("apduList");
                }
                if (apduArr != null) {
                    List<ServerAccessAPDU> apduList = new ArrayList<>();
                    int n = apduArr.length();
                    for (int i = 0; i < n; i++) {
                        ServerAccessAPDU apdu = ServerAccessAPDU.buildFromJson(apduArr.getJSONObject(i));
                        if (apdu != null) {
                            apduList.add(apdu);
                        }
                    }
                    response.setApduList(apduList);
                    StringBuilder sb4 = this.builder;
                    sb4.append(" apduList=");
                    sb4.append(apduList);
                }
            } catch (JSONException e2) {
                LogC.e("ServerAccessDeleteAppletTask readSuccessResponse, JSONException", false);
                response.returnCode = -99;
            }
        }
        if (!isDebugBuild()) {
            LogC.i("ServerAccessDeleteAppletTask readSuccessResponse, commander= delete.app returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + this.builder.toString(), false);
        }
        return response;
    }

    private void getSrcTranId(ServerAccessDeleteAppletResponse response, JSONObject dataObject) {
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
                LogC.e("ServerAccessDeleteAppletTask getSrcTransationId, JSONException", false);
                response.returnCode = -99;
            }
        }
    }
}
