package com.huawei.wallet.sdk.common.apdu.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.diploma.util.DiplomaUtil;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.unionpay.tsmservice.data.Constant;
import org.json.JSONException;
import org.json.JSONObject;

public class TsmParamQueryTask extends HttpConnTask<TsmParamQueryResponse, TsmParamQueryRequest> {
    static final String TASK_COMMANDER_CREATE_SSD = "nfc.get.create.SSD";
    static final String TASK_COMMANDER_DEL_APP = "nfc.get.del.APP";
    static final String TASK_COMMANDER_DEL_SSD = "nfc.get.del.SSD";
    static final String TASK_COMMANDER_INFO_INIT = "nfc.get.NotifyInfoInit";
    static final String TASK_COMMANDER_INSTALL_APP = "nfc.get.install.APP";
    static final String TASK_COMMANDER_LOCK_APP = "nfc.get.lock.APP";
    public static final String TASK_COMMANDER_RESET_ESE = "nfc.se.reset";
    static final String TASK_COMMANDER_SYNC_INFO = "nfc.get.NotifyEseInfoSync";
    static final String TASK_COMMANDER_UNLOCK_APP = "nfc.get.unlock.APP";
    static final String TASK_COMMANDER_UNLOCK_ESE = "nfc.se.unlock";
    static final String TASK_COMMANDER_UPDATE_APP = "nfc.get.transit.temp.update.APP";
    private static final int TSM_CHANNEL_HUAWEI = 0;
    private final String paramType;

    public TsmParamQueryTask(Context context, String url, String type) {
        super(context, url);
        this.paramType = type;
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(TsmParamQueryRequest request) {
        if (request == null || StringUtil.isEmpty(request.getSrcTransactionID(), true) || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getTerminal(), true) || StringUtil.isEmpty(this.paramType, true)) {
            LogC.d("prepareRequestStr, params invalid.", false);
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), this.paramType), request, this.paramType);
        JSONObject reportRequestMessageJson = reportRequestMessage(request, this.paramType);
        if (!isDebugBuild()) {
            LogC.i("ServerAccessApplyAPDUTask prepareRequestStr, commander= " + this.paramType + " reportRequestMessageJson= " + reportRequestMessageJson, false);
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(TsmParamQueryRequest request, String requestType) {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            dataJson.put("requestId", System.currentTimeMillis());
            if (!StringUtil.isEmpty(request.getAid(), true)) {
                dataJson.put("aid", request.getAid());
            }
            if (!StringUtil.isEmpty(request.getTerminal(), true)) {
                dataJson.put(HciConfigInfo.HCI_DATA_TYPE_AFTER_TERMINAL_ID, request.getTerminal());
            }
            if (!StringUtil.isEmpty(request.getIssuerId(), true)) {
                dataJson.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            }
            dataJson.put("tsmChannel", 0);
            if (request.isDeleteRelatedObjects()) {
                dataJson.put("deleteRelatedObjects", request.isDeleteRelatedObjects());
            }
            if (StringUtil.isEmpty(request.getSignType(), true)) {
                return dataJson;
            }
            dataJson.put(SNBConstant.FIELD_RSA_SIGN_TYPE, "***");
            return dataJson;
        } catch (JSONException e) {
            LogC.e("TsmParamQueryTask reportRequestMessage, params invalid.", false);
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, TsmParamQueryRequest request, String requestType) {
        JSONObject dataJson;
        if (headerObject == null) {
            return null;
        }
        LogC.d("createDataStr headerStr : " + headerObject.toString(), true);
        try {
            dataJson = new JSONObject();
            dataJson.put("header", headerObject);
            dataJson.put("requestId", System.currentTimeMillis());
            if (!StringUtil.isEmpty(request.getCplc(), true)) {
                dataJson.put("cplc", request.getCplc());
            }
            if (!StringUtil.isEmpty(request.getAid(), true)) {
                dataJson.put("aid", request.getAid());
            }
            if (!StringUtil.isEmpty(request.getIssuerId(), true)) {
                dataJson.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            }
            if (!StringUtil.isEmpty(request.getTerminal(), true)) {
                dataJson.put(HciConfigInfo.HCI_DATA_TYPE_AFTER_TERMINAL_ID, request.getTerminal());
            }
            dataJson.put("tsmChannel", 0);
            if (SignCommand.getSignCommand(requestType) != null) {
                if (!StringUtil.isEmpty(request.getBankSignResult(), true)) {
                    dataJson.put("sign", request.getBankSignResult());
                }
                if (!StringUtil.isEmpty(request.getBankSignTime(), true)) {
                    dataJson.put(Constant.KEY_CONTENT, signCommand + "|" + request.getBankSignTime());
                }
            }
            if (requestType.equals("nfc.se.reset")) {
                String content = "nfc.se.reset|" + request.getCplc() + "|" + request.getDeviceId() + "|" + request.getRandomStr() + "|" + request.getSignType();
                dataJson.put(Constant.KEY_CONTENT, content);
                dataJson.put("deviceId", request.getDeviceId());
                dataJson.put("sign", DiplomaUtil.getSignature(this.mContext, content));
            }
            if (!StringUtil.isEmpty(request.getBankRsaIndex(), true)) {
                dataJson.put("rsaindex", request.getBankRsaIndex());
            }
            if (!StringUtil.isEmpty(request.getSignType(), true)) {
                dataJson.put(SNBConstant.FIELD_RSA_SIGN_TYPE, request.getSignType());
            }
            if (request.isDeleteRelatedObjects()) {
                dataJson.put("deleteRelatedObjects", request.isDeleteRelatedObjects());
            }
        } catch (JSONException e) {
            LogC.e("createDataStr, params invalid.", false);
            dataJson = null;
        }
        return dataJson;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse readErrorResponse(int errorCode, String errorMessage) {
        TsmParamQueryResponse response = new TsmParamQueryResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogC.i("TsmParamQueryTask readErrorResponse, commander= " + this.paramType + " errorCode= " + errorCode + " errorMessage= " + errorMessage, false);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        StringBuilder builder = new StringBuilder();
        TsmParamQueryResponse response = new TsmParamQueryResponse();
        response.returnCode = returnCode;
        if (returnCode == 0) {
            try {
                String funcID = JSONHelper.getStringValue(dataObject, "funcID");
                String servicID = JSONHelper.getStringValue(dataObject, "servicID");
                if (!StringUtil.isEmpty(funcID, true)) {
                    if (!StringUtil.isEmpty(servicID, true)) {
                        response.funcID = funcID;
                        response.servicID = servicID;
                        builder.append("funcID=");
                        builder.append(JSONHelper.getStringValue(dataObject, "funcID"));
                        builder.append(" servicID=");
                        builder.append(JSONHelper.getStringValue(dataObject, "servicID"));
                    }
                }
                LogC.d("readSuccessResponse, illegal funcID or servicID", false);
                response.returnCode = -99;
            } catch (JSONException e) {
                LogC.e("readSuccessResponse, JSONException : " + e.getMessage(), true);
                response.returnCode = -99;
            }
        }
        if (!isDebugBuild()) {
            LogC.i("TsmParamQueryTask readSuccessResponse, commander= " + this.paramType + " returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + builder.toString(), false);
        }
        return response;
    }
}
