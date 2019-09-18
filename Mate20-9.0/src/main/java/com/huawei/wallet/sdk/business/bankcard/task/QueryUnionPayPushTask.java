package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.BankCardServerCmdConstant;
import com.huawei.wallet.sdk.business.bankcard.request.QueryUnionPayPushRequest;
import com.huawei.wallet.sdk.business.bankcard.response.QueryUnionPayPushResponse;
import com.huawei.wallet.sdk.business.bankcard.server.CUPOperateService;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryUnionPayPushTask extends HttpConnTask {
    private static final String TAG = "QueryUnionPayPushTask";

    public QueryUnionPayPushTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(Object o) {
        QueryUnionPayPushRequest request = (QueryUnionPayPushRequest) o;
        if (request == null || StringUtil.isEmpty(request.getSrcTransactionID(), true) || StringUtil.isEmpty(request.getMerchantID(), true)) {
            return null;
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), BankCardServerCmdConstant.QUERY_UNION_PAY_PUSH_CMD), request), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, QueryUnionPayPushRequest request) {
        if (headerObject == null) {
            return null;
        }
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("header", headerObject);
            dataJson.put("requestid", String.valueOf(System.currentTimeMillis()));
            if (!StringUtil.isEmpty(request.cplc, true)) {
                dataJson.put("cplc", request.cplc);
            }
            dataJson.put("cardRefId", request.getCardRefId());
            dataJson.put("queryFlag", request.getQueryFlag());
        } catch (JSONException e) {
            dataJson = null;
        }
        return dataJson;
    }

    /* access modifiers changed from: protected */
    public QueryUnionPayPushResponse readErrorResponse(int errorCode, String errorMessage) {
        QueryUnionPayPushResponse response = new QueryUnionPayPushResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    /* access modifiers changed from: protected */
    public QueryUnionPayPushResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        QueryUnionPayPushResponse response = new QueryUnionPayPushResponse();
        response.returnCode = returnCode;
        if (returnCode == 0) {
            try {
                response.setPushMsg(JSONHelper.getStringValue(dataObject, "pushMsg"));
                response.setPushTime(JSONHelper.getStringValue(dataObject, CUPOperateService.SERVICE_INTENT_KEY_PUSHTIME));
                response.setSystemCurrentTime(JSONHelper.getStringValue(dataObject, "systemCurrentTime"));
            } catch (JSONException e) {
                response.returnCode = -99;
            }
        }
        return response;
    }
}
