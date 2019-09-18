package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.commonbase.util.log.LogErrorConstant;
import com.huawei.wallet.sdk.common.apdu.properties.WalletSystemProperties;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.log.LogC;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DicsQueryTask extends HttpConnTask<QueryDicsResponse, QueryDicsRequset> {
    private static final String QUERY_DICS_COMMANDER = "get.dics";
    private static String logBuildType;
    private StringBuilder builder = new StringBuilder();

    public DicsQueryTask(Context context, String url) {
        super(context, url);
    }

    protected static boolean isDebugBuild() {
        if (logBuildType == null && WalletSystemProperties.getInstance().containsProperty("LOG_BUILD_TYPE")) {
            logBuildType = WalletSystemProperties.getInstance().getProperty("LOG_BUILD_TYPE", "release");
        }
        if ("Debug".equalsIgnoreCase(logBuildType)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(QueryDicsRequset params) {
        if (params == null || StringUtil.isEmpty(params.getSrcTransactionID(), true) || StringUtil.isEmpty(params.getMerchantID(), true)) {
            LogC.e("DicsQueryTask prepareRequestStr params error.", false);
            return null;
        }
        JSONObject dataObject = createDataStr(JSONHelper.createHeaderStr(params.getSrcTransactionID(), QUERY_DICS_COMMANDER), params);
        JSONObject reportRequestMessageJson = reportRequestMessage(params);
        if (!isDebugBuild()) {
            LogC.i("DicsQueryTask prepareRequestStr, commander= get.dics reportRequestMessageJson= " + reportRequestMessageJson, false);
        }
        return JSONHelper.createRequestStr(params.getMerchantID(), params.getRsaKeyIndex(), dataObject, this.mContext);
    }

    /* access modifiers changed from: protected */
    public QueryDicsResponse readErrorResponse(int errorCode, String errorMessage) {
        LogC.i("DicsQueryTask readErrorResponse errorCode is  " + errorCode, false);
        QueryDicsResponse response = new QueryDicsResponse();
        if (-1 == errorCode) {
            response.returnCode = -1;
        } else if (-3 == errorCode) {
            response.returnCode = 1;
        } else if (-2 == errorCode) {
            response.returnCode = -2;
        }
        if (!isDebugBuild()) {
            LogC.i("DicsQueryTask readErrorResponse, commander= get.dics errorCode= " + errorCode, false);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public QueryDicsResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        LogC.i("DicsQueryTask readSuccessResponse", false);
        QueryDicsResponse response = new QueryDicsResponse();
        try {
            response = (QueryDicsResponse) makeResponseData(response, dataObject);
            response.returnCode = returnCode;
        } catch (JSONException e) {
            LogC.e("DicsQueryTask|readSuccessResponse|readSuccessResponse", false);
        }
        if (!isDebugBuild()) {
            LogC.i("DicsQueryTask readSuccessResponse, commander= get.dics responseStr= " + returnDesc + " dataObject: " + this.builder.toString(), false);
        }
        return response;
    }

    private JSONObject reportRequestMessage(QueryDicsRequset params) {
        JSONObject jObj;
        if (StringUtil.isEmpty(params.getDicName(), true)) {
            LogC.e("DicsQueryTask reportRequestMessage, params error.", false);
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put(ExecuteApduTask.SRC_TRANSACTION_ID, params.getSrcTransactionID());
            jObj.put("dicName", params.getDicName());
            jObj.put("itemName", params.getItemName());
        } catch (JSONException e) {
            LogC.e("DicsQueryTask reportRequestMessage JSONException.", false);
            jObj = null;
        }
        return jObj;
    }

    private JSONObject createDataStr(JSONObject headerObject, QueryDicsRequset params) {
        JSONObject jObj;
        if (headerObject == null || StringUtil.isEmpty(params.getDicName(), true)) {
            LogC.e("DicsQueryTask createDataStr params error.", false);
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put("dicName", params.getDicName());
            jObj.put("itemName", params.getItemName());
        } catch (JSONException e) {
            LogC.e("DicsQueryTask createDataStr JSONException.", (Throwable) e, true);
            jObj = null;
        }
        return jObj;
    }

    private CardServerBaseResponse makeResponseData(CardServerBaseResponse response, JSONObject dataObject) throws JSONException {
        if (!(response instanceof QueryDicsResponse)) {
            return response;
        }
        QueryDicsResponse resp = (QueryDicsResponse) response;
        if (dataObject.has("header")) {
            JSONObject header = dataObject.getJSONObject("header");
            if (header != null) {
                String srcTranId = header.getString("srcTranID");
                StringBuilder sb = this.builder;
                sb.append("srcTranId=");
                sb.append(srcTranId);
            }
        }
        JSONArray jsonArray = null;
        if (dataObject.has(SNBConstant.FIELD_DATA)) {
            jsonArray = dataObject.getJSONArray(SNBConstant.FIELD_DATA);
        }
        if (jsonArray != null) {
            int size = jsonArray.length();
            for (int i = 0; i < size; i++) {
                JSONObject dicObject = jsonArray.getJSONObject(i);
                if (dicObject != null) {
                    DicItem item = new DicItem();
                    item.setParent(JSONHelper.getStringValue(dicObject, "parent"));
                    item.setName(JSONHelper.getStringValue(dicObject, "name"));
                    item.setValue(JSONHelper.getStringValue(dicObject, "value"));
                    item.setBankCard(JSONHelper.getStringValue(dicObject, "bankCard"));
                    resp.dicItems.add(item);
                    StringBuilder sb2 = this.builder;
                    sb2.append(" item=");
                    sb2.append(item);
                }
            }
        }
        return response;
    }

    private String getTag() {
        return "DicsQueryTask readSuccessResponse ";
    }

    private int getErrorLogConstant() {
        return LogErrorConstant.DICS_QUERY_ERR;
    }
}
