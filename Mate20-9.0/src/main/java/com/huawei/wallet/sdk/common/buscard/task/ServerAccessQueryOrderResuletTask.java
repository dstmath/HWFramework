package com.huawei.wallet.sdk.common.buscard.task;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessQueryOrderResultRequest;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessQueryOrderResultResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessQueryOrderResuletTask extends HttpConnTask<ServerAccessQueryOrderResultResponse, ServerAccessQueryOrderResultRequest> {
    private static final String HEAD_COMMANDER = "query.business.result";
    private final String clsName = getClass().getSimpleName();
    private StringBuilder sBuild = new StringBuilder();

    public ServerAccessQueryOrderResuletTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessQueryOrderResultRequest request) {
        if (request == null || TextUtils.isEmpty(request.getCplc()) || TextUtils.isEmpty(request.getSeChipManuFacturer()) || TextUtils.isEmpty(request.getDeviceModel()) || TextUtils.isEmpty(request.getAppletAid()) || TextUtils.isEmpty(request.getIssuerId())) {
            LogX.i(this.clsName + " prepareRequestStr, invalid param", false);
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "query.business.result", request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessage = reportRequestMessage(request);
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(ServerAccessQueryOrderResultRequest request) {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            if (StringUtil.isEmpty(request.getOrderId(), true)) {
                return jObj;
            }
            jObj.put("orderNo", request.getOrderId());
            return jObj;
        } catch (JSONException e) {
            LogX.e("ServerAccessQueryOrderTask reportRequestMessage, JSONException");
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, ServerAccessQueryOrderResultRequest request) {
        JSONObject jObj;
        if (headerObject == null) {
            LogX.e(this.clsName + " createDataStr, invalid param");
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
            if (!StringUtil.isEmpty(request.getOrderId(), true)) {
                jObj.put("orderNo", request.getOrderId());
            }
            if (request.getRequestTimes() > 0) {
                jObj.put("times", request.getRequestTimes());
            }
        } catch (JSONException e) {
            LogX.e(this.clsName + " createDataStr, JSONException", false);
            jObj = null;
        }
        return jObj;
    }

    /* access modifiers changed from: protected */
    public ServerAccessQueryOrderResultResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessQueryOrderResultResponse response = new ServerAccessQueryOrderResultResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogX.i(this.clsName + " readErrorResponse, commander= " + "query.business.result" + " errorCode= " + errorCode + " errorMessage= " + errorMessage, false);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessQueryOrderResultResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessQueryOrderResultResponse response = new ServerAccessQueryOrderResultResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        setErrorInfo(dataObject, response);
        String srcTranId = getSrcTranId(response, dataObject);
        if (!TextUtils.isEmpty(srcTranId)) {
            StringBuilder sb = this.sBuild;
            sb.append("srcTranId=");
            sb.append(srcTranId);
        }
        if (returnCode == 0) {
            try {
                response.setTransactionId(JSONHelper.getStringValue(dataObject, "transactionid"));
                StringBuilder sb2 = this.sBuild;
                sb2.append(" transactionid=");
                sb2.append(JSONHelper.getStringValue(dataObject, "transactionid"));
                response.setResult(JSONHelper.getIntValue(dataObject, "result"));
                StringBuilder sb3 = this.sBuild;
                sb3.append(" result=");
                sb3.append(response.getResult());
                int intervalTime = JSONHelper.getIntValue(dataObject, "invokeIntervalTime");
                if (intervalTime > 0) {
                    StringBuilder sb4 = this.sBuild;
                    sb4.append(" invokeIntervalTime=");
                    sb4.append(intervalTime);
                    response.setInvokeIntervalTime(intervalTime);
                }
            } catch (JSONException e) {
                LogX.e(this.clsName + " readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        return response;
    }
}
