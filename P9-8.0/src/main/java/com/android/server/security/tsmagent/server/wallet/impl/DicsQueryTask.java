package com.android.server.security.tsmagent.server.wallet.impl;

import android.content.Context;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.server.CardServerBaseRequest;
import com.android.server.security.tsmagent.server.CardServerBaseResponse;
import com.android.server.security.tsmagent.server.wallet.HttpConnTask;
import com.android.server.security.tsmagent.server.wallet.request.QueryDicsRequset;
import com.android.server.security.tsmagent.server.wallet.response.DicItem;
import com.android.server.security.tsmagent.server.wallet.response.QueryDicsResponse;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DicsQueryTask extends HttpConnTask {
    private static final String QUERY_DICS_COMMANDER = "get.dics";

    public DicsQueryTask(Context context, String url) {
        super(context, url);
    }

    protected String prepareRequestStr(CardServerBaseRequest params) {
        if (!(params instanceof QueryDicsRequset)) {
            return null;
        }
        if (StringUtil.isTrimedEmpty(params.getSrcTransactionID()) || StringUtil.isTrimedEmpty(params.getMerchantID())) {
            HwLog.e("DicsQueryTask prepareRequestStr params error.");
            return null;
        }
        return JSONHelper.createRequestStr(params.getMerchantID(), createDataStr(JSONHelper.createHeaderStr(params.getSrcTransactionID(), QUERY_DICS_COMMANDER), params), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, CardServerBaseRequest params) {
        JSONObject jSONObject;
        if (!(params instanceof QueryDicsRequset)) {
            return null;
        }
        QueryDicsRequset castPara = (QueryDicsRequset) params;
        if (headerObject == null || StringUtil.isTrimedEmpty(castPara.dicName)) {
            HwLog.e("DicsQueryTask createDataStr params error.");
            return null;
        }
        try {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("header", headerObject);
                jObj.put("dicName", castPara.dicName);
                jObj.put("itemName", castPara.itemName);
                jSONObject = jObj;
            } catch (JSONException e) {
                HwLog.e("DicsQueryTask createDataStr JSONException.");
                jSONObject = null;
                return jSONObject;
            }
        } catch (JSONException e2) {
            HwLog.e("DicsQueryTask createDataStr JSONException.");
            jSONObject = null;
            return jSONObject;
        }
        return jSONObject;
    }

    protected CardServerBaseResponse readErrorResponse(int errorCode) {
        CardServerBaseResponse response = new QueryDicsResponse();
        if (-1 == errorCode) {
            response.returnCode = -1;
        } else if (-3 == errorCode) {
            response.returnCode = 1;
        } else if (-2 == errorCode) {
            response.returnCode = -2;
        }
        return response;
    }

    protected QueryDicsResponse readSuccessResponse(int returnCode, String responseStr, JSONObject dataObject) {
        QueryDicsResponse response = new QueryDicsResponse();
        resolveResponse(response, responseStr);
        return response;
    }

    protected void makeResponseData(CardServerBaseResponse response, JSONObject dataObject) throws JSONException {
        if (response instanceof QueryDicsResponse) {
            QueryDicsResponse resp = (QueryDicsResponse) response;
            JSONArray jsonArray = null;
            if (dataObject.has("data")) {
                jsonArray = dataObject.getJSONArray("data");
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
                        resp.dicItems.add(item);
                    }
                }
            }
        }
    }

    protected void resolveResponse(CardServerBaseResponse response, String responseStr) {
        NumberFormatException ex;
        JSONException ex2;
        if (StringUtil.isTrimedEmpty(responseStr)) {
            HwLog.e("responseStr is null");
            response.returnCode = -99;
            return;
        }
        try {
            JSONObject responseJson = new JSONObject(responseStr);
            JSONObject jSONObject;
            try {
                String merchantID = JSONHelper.getStringValue(responseJson, "merchantID");
                int keyIndex = JSONHelper.getIntValue(responseJson, "keyIndex");
                String responseDataStr = JSONHelper.getStringValue(responseJson, "response");
                String errorCode = JSONHelper.getStringValue(responseJson, "errorCode");
                String errorMsg = JSONHelper.getStringValue(responseJson, "errorMsg");
                if (errorCode != null) {
                    HwLog.e(" code:" + errorCode + " msg:" + errorMsg);
                    response.returnCode = Integer.parseInt(errorCode);
                    return;
                }
                HwLog.d("merchantID: " + merchantID + "\nresponseDataStr: " + responseDataStr);
                if (!ServiceConfig.WALLET_MERCHANT_ID.equals(merchantID) || StringUtil.isTrimedEmpty(responseDataStr)) {
                    HwLog.d("unexpected error from server.");
                    response.returnCode = -99;
                    return;
                }
                String decryptedResponse = null;
                if (keyIndex == -1) {
                    decryptedResponse = responseDataStr;
                }
                HwLog.d("decryptedResponse : " + decryptedResponse + "keyIndex: " + keyIndex);
                JSONObject dataObject = new JSONObject();
                String returnCodeStr = null;
                String returnDesc = null;
                if (decryptedResponse != null) {
                    dataObject = new JSONObject(decryptedResponse);
                    returnCodeStr = JSONHelper.getStringValue(dataObject, "returnCode");
                    returnDesc = JSONHelper.getStringValue(dataObject, "returnDesc");
                }
                if (returnCodeStr == null) {
                    HwLog.d("returnCode is invalid.");
                    response.returnCode = -99;
                    return;
                }
                response.returnCode = Integer.parseInt(returnCodeStr);
                if (response.returnCode != 0) {
                    HwLog.e("returnDesc : " + (" code:" + response.returnCode + " desc:" + returnDesc));
                    return;
                }
                makeResponseData(response, dataObject);
                jSONObject = responseJson;
            } catch (NumberFormatException e) {
                ex = e;
                HwLog.e("NumberFormatException : " + ex);
                response.returnCode = -99;
            } catch (JSONException e2) {
                ex2 = e2;
                jSONObject = responseJson;
                HwLog.e("JSONException : " + ex2);
                response.returnCode = -99;
            }
        } catch (NumberFormatException e3) {
            ex = e3;
            HwLog.e("NumberFormatException : " + ex);
            response.returnCode = -99;
        } catch (JSONException e4) {
            ex2 = e4;
            HwLog.e("JSONException : " + ex2);
            response.returnCode = -99;
        }
    }
}
