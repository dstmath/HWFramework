package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.BankCardServerCmdConstant;
import com.huawei.wallet.sdk.business.bankcard.request.QueryAidRequest;
import com.huawei.wallet.sdk.business.bankcard.response.QueryAidResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryAidOnCUPCardTask extends HttpConnTask {
    private static final String ERROR_CODE_NOT_GET_AID = "N90300";
    private static final String ERROR_CODE_NO_QUERIED_ITEM = "N90317";
    private static final String TAG = "QueryAidOnCUPCardTask";

    public QueryAidOnCUPCardTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(Object o) {
        QueryAidRequest request = (QueryAidRequest) o;
        if (request == null || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getCardRefId(), true)) {
            return null;
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), BankCardServerCmdConstant.QUERY_AID_CMD), request), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, QueryAidRequest request) {
        if (headerObject == null) {
            return null;
        }
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("header", headerObject);
            dataJson.put("cplc", request.getCplc());
            dataJson.put("cardRefId", request.getCardRefId());
        } catch (JSONException e) {
            dataJson = null;
        }
        return dataJson;
    }

    /* access modifiers changed from: protected */
    public QueryAidResponse readErrorResponse(int errorCode, String errorMessage) {
        QueryAidResponse response = new QueryAidResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    /* access modifiers changed from: protected */
    public QueryAidResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        QueryAidResponse response = new QueryAidResponse();
        response.returnCode = returnCode;
        if (returnCode == -98) {
            try {
                response.returnCode = -99;
                String returnCodeStr = JSONHelper.getStringValue(dataObject, "returnCode");
                if (returnCodeStr != null) {
                    if (ERROR_CODE_NOT_GET_AID.equals(returnCodeStr)) {
                        response.returnCode = -3;
                    } else if (ERROR_CODE_NO_QUERIED_ITEM.equals(returnCodeStr)) {
                        response.returnCode = -5;
                    }
                }
                return response;
            } catch (JSONException e) {
                response.returnCode = -99;
            }
        } else {
            if (response.returnCode == 0) {
                response.setVirtualCardRefID(JSONHelper.getStringValue(dataObject, "cardRefId"));
                response.setAid(JSONHelper.getStringValue(dataObject, "aid"));
            }
            return response;
        }
    }
}
