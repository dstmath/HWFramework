package com.huawei.wallet.sdk.common.apdu.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoServerItem;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.QueryIssuerInfoRequest;
import com.huawei.wallet.sdk.common.apdu.response.QueryIssuerInfoResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IssuerInfoQueryTask extends HttpConnTask<QueryIssuerInfoResponse, QueryIssuerInfoRequest> {
    private static final String ISSUER_INFO_GET_COMMANDER = "nfc.get.issuers";

    public IssuerInfoQueryTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(QueryIssuerInfoRequest request) {
        if (request == null || StringUtil.isEmpty(request.getSrcTransactionID(), true) || StringUtil.isEmpty(request.getMerchantID(), true)) {
            LogX.d("IssuerInfoQueryTask prepareRequestStr, params invalid.");
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "nfc.get.issuers", request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessageJson = reportRequestMessage(request);
        if (!isDebugBuild()) {
            LogX.i("IssuerInfoQueryTask prepareRequestStr, commander= nfc.get.issuers reportRequestMessageJson= " + reportRequestMessageJson);
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(QueryIssuerInfoRequest request) {
        if (request == null) {
            return null;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            obj.put("timestamp", request.timeStamp);
        } catch (JSONException e) {
            LogX.e("IssuerInfoQueryTask reportRequestMessage parse json error", true);
            obj = null;
        }
        return obj;
    }

    private JSONObject createDataStr(JSONObject headerObject, QueryIssuerInfoRequest request) {
        if (headerObject == null || request == null) {
            return null;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("header", headerObject);
            obj.put("timestamp", request.timeStamp);
        } catch (JSONException e) {
            LogX.e("IssuerInfoQueryTask createDataStr parse json error" + e.getMessage(), true);
            obj = null;
        }
        return obj;
    }

    /* access modifiers changed from: protected */
    public QueryIssuerInfoResponse readErrorResponse(int errorCode, String errorMessage) {
        QueryIssuerInfoResponse response = new QueryIssuerInfoResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogX.i("IssuerInfoQueryTask readErrorResponse, commander= nfc.get.issuers errorCode= " + errorCode + " errorMessage= " + errorMessage);
        }
        return response;
    }

    private IssuerInfoServerItem createIssuerInfoItem(JSONObject tempJsonItem) {
        IssuerInfoServerItem item = null;
        if (tempJsonItem == null) {
            return null;
        }
        try {
            item = new IssuerInfoServerItem(tempJsonItem);
        } catch (JSONException e) {
        }
        return item;
    }

    /* access modifiers changed from: protected */
    public QueryIssuerInfoResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        StringBuilder builder = new StringBuilder();
        QueryIssuerInfoResponse response = new QueryIssuerInfoResponse();
        response.returnCode = returnCode;
        if (returnCode == 0) {
            JSONArray cardArrays = null;
            try {
                if (dataObject.has(SNBConstant.FIELD_DATA)) {
                    cardArrays = dataObject.getJSONArray(SNBConstant.FIELD_DATA);
                }
                if (cardArrays != null) {
                    response.issueInfos = new ArrayList();
                    response.issueInfosMap = new HashMap();
                    for (int i = 0; i < cardArrays.length(); i++) {
                        IssuerInfoServerItem tempCardItem = createIssuerInfoItem(cardArrays.getJSONObject(i));
                        if (tempCardItem != null) {
                            response.issueInfos.add(tempCardItem);
                            response.issueInfosMap.put(tempCardItem.getIssuerId(), tempCardItem);
                            builder.append("tempCardItem=");
                            builder.append(tempCardItem);
                            builder.append(" issuerid=");
                            builder.append(tempCardItem.getIssuerId());
                        }
                    }
                }
            } catch (JSONException e) {
                LogX.e("readSuccessResponse, JSONException : " + e.getMessage(), true);
                response.returnCode = -99;
            }
        }
        if (!isDebugBuild()) {
            LogX.i("IssuerInfoQueryTask readSuccessResponse, commander= nfc.get.issuers returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + builder.toString());
        }
        return response;
    }
}
