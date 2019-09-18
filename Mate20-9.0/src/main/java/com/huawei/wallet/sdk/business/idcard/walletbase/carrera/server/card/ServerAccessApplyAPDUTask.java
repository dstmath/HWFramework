package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.server.card;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessCutoverInfo;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessApplyAPDUTask extends HttpConnTask<ServerAccessApplyAPDUResponse, ServerAccessApplyAPDURequest> {
    private static final String HEAD_COMMANDER = "get.apdu";
    private StringBuilder builder = new StringBuilder();

    public ServerAccessApplyAPDUTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessApplyAPDURequest request) {
        if (request == null || StringUtil.isEmpty(request.getIssuerId(), true) || StringUtil.isEmpty(request.getAppletAid(), true) || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getTransactionId(), true) || request.getApduList() == null || request.getApduList().isEmpty() || request.getApduCount() != request.getApduList().size() || StringUtil.isEmpty(request.getDeviceModel(), true) || StringUtil.isEmpty(request.getSeChipManuFacturer(), true)) {
            LogX.e("ServerAccessApplyAPDUTask prepareRequestStr, invalid param");
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "get.apdu", request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessage = reportRequestMessage(request);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessApplyAPDUTask prepareRequestStr, commander= get.apdu");
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(ServerAccessApplyAPDURequest request) {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put("transactionid", request.getTransactionId());
            jObj.put("apduCount", request.getApduCount());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            if (!StringUtil.isEmpty(request.getTokenReId(), false)) {
                jObj.put(ServerAccessApplyAPDURequest.ReqKey.TOKENREFID, request.getTokenReId());
            }
            if (!StringUtil.isEmpty(request.getEnrollmentId(), false)) {
                jObj.put(ServerAccessApplyAPDURequest.ReqKey.ENROLLMENTID, request.getEnrollmentId());
            }
            JSONArray jArray = new JSONArray();
            for (ServerAccessAPDU apdu : request.getApduList()) {
                JSONObject jObject = new JSONObject();
                jObject.put("apduNo", apdu.getApduId());
                jObject.put("apduContent", apdu.getApduContent());
                if (!StringUtil.isEmpty(apdu.getApduStatus(), true)) {
                    jObject.put("apduStatus", apdu.getApduStatus());
                }
                if (!StringUtil.isEmpty(apdu.getCommand(), true)) {
                    jObject.put("command", apdu.getCommand());
                }
                if (!StringUtil.isEmpty(apdu.getChecker(), true)) {
                    jObject.put("checker", apdu.getChecker());
                }
                jArray.put(jObject);
            }
            jObj.put("apduList", jArray);
            if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
                jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            }
            if (!StringUtil.isEmpty(request.getCurrentStep(), true)) {
                jObj.put("currentStep", request.getCurrentStep());
            }
            if (StringUtil.isEmpty(request.getReserved(), true)) {
                return jObj;
            }
            jObj.put("reserved", request.getReserved());
            return jObj;
        } catch (JSONException e) {
            LogX.e("ServerAccessApplyAPDUTask reportRequestMessage, JSONException");
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, ServerAccessApplyAPDURequest request) {
        JSONObject jObj;
        if (headerObject == null) {
            LogX.e("ServerAccessApplyAPDUTask createDataStr, invalid param");
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put("cplc", request.getCplc());
            jObj.put("transactionid", request.getTransactionId());
            jObj.put("apduCount", request.getApduCount());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            if (!StringUtil.isEmpty(request.getTokenReId(), false)) {
                jObj.put(ServerAccessApplyAPDURequest.ReqKey.TOKENREFID, request.getTokenReId());
            }
            if (!StringUtil.isEmpty(request.getEnrollmentId(), false)) {
                jObj.put(ServerAccessApplyAPDURequest.ReqKey.ENROLLMENTID, request.getEnrollmentId());
            }
            if (!StringUtil.isEmpty(request.getCommandId(), false)) {
                jObj.put(ServerAccessApplyAPDURequest.ReqKey.COMMANDID, request.getCommandId());
            }
            JSONArray jArray = new JSONArray();
            for (ServerAccessAPDU apdu : request.getApduList()) {
                JSONObject jObject = new JSONObject();
                jObject.put("apduNo", apdu.getApduId());
                jObject.put("apduContent", apdu.getApduContent());
                if (!StringUtil.isEmpty(apdu.getApduStatus(), true)) {
                    jObject.put("apduStatus", apdu.getApduStatus());
                }
                if (!StringUtil.isEmpty(apdu.getCommand(), true)) {
                    jObject.put("command", apdu.getCommand());
                }
                if (!StringUtil.isEmpty(apdu.getChecker(), true)) {
                    jObject.put("checker", apdu.getChecker());
                }
                jArray.put(jObject);
            }
            jObj.put("apduList", jArray);
            if (!StringUtil.isEmpty(request.getSn(), true)) {
                jObj.put(SNBConstant.FIELD_IMEI, request.getSn());
            }
            if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
                jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            }
            if (!StringUtil.isEmpty(request.getCurrentStep(), true)) {
                jObj.put("currentStep", request.getCurrentStep());
            }
            if (!StringUtil.isEmpty(request.getPhoneNumber(), true)) {
                jObj.put("phoneNumber", request.getPhoneNumber());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
        } catch (JSONException e) {
            LogX.e("ServerAccessApplyAPDUTask createDataStr, JSONException");
            jObj = null;
        }
        return jObj;
    }

    /* access modifiers changed from: protected */
    public ServerAccessApplyAPDUResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessApplyAPDUResponse response = new ServerAccessApplyAPDUResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessApplyAPDUTask readErrorResponse, commander= get.apdu errorCode= " + errorCode + " errorMessage= " + errorMessage);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessApplyAPDUResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessApplyAPDUResponse response = new ServerAccessApplyAPDUResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        setErrorInfo(dataObject, response);
        getSrcTranId(response, dataObject);
        if (dataObject != null && dataObject.has("errorInfo")) {
            ErrorInfo errorInfo = null;
            try {
                errorInfo = ErrorInfo.build(dataObject.getJSONObject("errorInfo"));
            } catch (JSONException e) {
                LogX.e("ServerAccessApplyAPDUTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
            response.setErrorInfo(errorInfo);
            StringBuilder sb = this.builder;
            sb.append(" setErrorInfo=");
            sb.append(errorInfo);
        }
        if (returnCode == 0) {
            JSONArray apduArr = null;
            try {
                if (dataObject.has("apduList")) {
                    apduArr = dataObject.getJSONArray("apduList");
                }
                if (dataObject.has(ExecuteApduTask.NEXT_STEP)) {
                    response.setNextStep(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
                    StringBuilder sb2 = this.builder;
                    sb2.append(" nextStep=");
                    sb2.append(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
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
                    StringBuilder sb3 = this.builder;
                    sb3.append(" apduList=");
                    sb3.append(apduList);
                }
                if (dataObject.has("cutoverInfo")) {
                    ServerAccessCutoverInfo cutoverInfo = ServerAccessCutoverInfo.build(dataObject.getJSONObject("cutoverInfo"));
                    response.setServerAccessCutoverInfo(cutoverInfo);
                    StringBuilder sb4 = this.builder;
                    sb4.append(" cutoverInfo=");
                    sb4.append(cutoverInfo);
                }
            } catch (JSONException e2) {
                LogX.e("ServerAccessApplyAPDUTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
            parseAwardsInfo(response, dataObject);
        }
        if (!isDebugBuild()) {
            LogX.i("ServerAccessApplyAPDUTask readSuccessResponse, commander= get.apdu returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + this.builder.toString());
        }
        return response;
    }

    private void getSrcTranId(ServerAccessApplyAPDUResponse response, JSONObject dataObject) {
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
                LogX.e("ServerAccessApplyAPDUTask getSrcTransationId, JSONException");
                response.returnCode = -99;
            }
        }
    }

    private void parseAwardsInfo(ServerAccessApplyAPDUResponse response, JSONObject dataObject) {
        LogX.d("ServerAccessApplyAPDUTask parseAwardsInfo dataObject: " + dataObject.toString());
        try {
            if (dataObject.has("openCardLotteryResult")) {
                response.openCardLotteryResult = dataObject.getString("openCardLotteryResult");
                StringBuilder sb = this.builder;
                sb.append(" openCardLotteryResult=");
                sb.append(dataObject.getString("openCardLotteryResult"));
            }
            if (dataObject.has("triggerOpencardID")) {
                response.triggerOpencardID = dataObject.getString("triggerOpencardID");
                StringBuilder sb2 = this.builder;
                sb2.append(" triggerOpencardID=");
                sb2.append(dataObject.getString("triggerOpencardID"));
            }
            if (dataObject.has("promotionId")) {
                response.promotionId = dataObject.getString("promotionId");
                StringBuilder sb3 = this.builder;
                sb3.append(" promotionId=");
                sb3.append(dataObject.getString("promotionId"));
            }
            if (dataObject.has("uploadObsPath")) {
                response.uploadObsPath = dataObject.getString("uploadObsPath");
                StringBuilder sb4 = this.builder;
                sb4.append(" uploadObsPath=");
                sb4.append(dataObject.getString("uploadObsPath"));
            }
            if (dataObject.has("uploadConfigPath")) {
                response.uploadConfigPath = dataObject.getString("uploadConfigPath");
                StringBuilder sb5 = this.builder;
                sb5.append(" uploadConfigPath=");
                sb5.append(dataObject.getString("uploadConfigPath"));
            }
            if (dataObject.has("materialName")) {
                response.materialName = dataObject.getString("materialName");
                StringBuilder sb6 = this.builder;
                sb6.append(" materialName=");
                sb6.append(dataObject.getString("materialName"));
            }
            if (dataObject.has("responseType")) {
                response.responseType = dataObject.getString("responseType");
                StringBuilder sb7 = this.builder;
                sb7.append(" responseType=");
                sb7.append(dataObject.getString("responseType"));
            } else {
                LogX.d("ServerAccessApplyAPDUTask parseAwardsInfo, no responseType, set to 1_lottery");
                response.responseType = "1_lottery";
            }
            if (dataObject.has("huaweiCoinInfo")) {
                StringBuilder sb8 = this.builder;
                sb8.append(" huaweiCoinInfo=");
                sb8.append(dataObject.getString("huaweiCoinInfo"));
            }
        } catch (JSONException e) {
            LogX.i("ServerAccessApplyAPDUTask parseAwardsInfo,  Exception occured.");
        }
    }
}
