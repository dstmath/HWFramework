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

    /* access modifiers changed from: protected */
    public String prepareRequestStr(CardServerBaseRequest params) {
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
        JSONObject jObj;
        if (!(params instanceof QueryDicsRequset)) {
            return null;
        }
        QueryDicsRequset castPara = (QueryDicsRequset) params;
        if (headerObject == null || StringUtil.isTrimedEmpty(castPara.dicName)) {
            HwLog.e("DicsQueryTask createDataStr params error.");
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put("dicName", castPara.dicName);
            jObj.put("itemName", castPara.itemName);
        } catch (JSONException e) {
            HwLog.e("DicsQueryTask createDataStr JSONException.");
            jObj = null;
        }
        return jObj;
    }

    /* access modifiers changed from: protected */
    public CardServerBaseResponse readErrorResponse(int errorCode) {
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

    /* access modifiers changed from: protected */
    public QueryDicsResponse readSuccessResponse(int returnCode, String responseStr, JSONObject dataObject) {
        QueryDicsResponse response = new QueryDicsResponse();
        resolveResponse(response, responseStr);
        return response;
    }

    /* access modifiers changed from: protected */
    public void makeResponseData(CardServerBaseResponse response, JSONObject dataObject) throws JSONException {
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

    /* access modifiers changed from: protected */
    public void resolveResponse(CardServerBaseResponse response, String responseStr) {
        CardServerBaseResponse cardServerBaseResponse = response;
        if (StringUtil.isTrimedEmpty(responseStr)) {
            HwLog.e("responseStr is null");
            cardServerBaseResponse.returnCode = -99;
            return;
        }
        try {
            try {
                JSONObject responseJson = new JSONObject(responseStr);
                String merchantID = JSONHelper.getStringValue(responseJson, "merchantID");
                int keyIndex = JSONHelper.getIntValue(responseJson, "keyIndex");
                String responseDataStr = JSONHelper.getStringValue(responseJson, "response");
                String errorCode = JSONHelper.getStringValue(responseJson, "errorCode");
                String errorMsg = JSONHelper.getStringValue(responseJson, "errorMsg");
                if (errorCode != null) {
                    HwLog.e(" code:" + errorCode + " msg:" + errorMsg);
                    cardServerBaseResponse.returnCode = Integer.parseInt(errorCode);
                    return;
                }
                HwLog.d("merchantID: " + merchantID + "\nresponseDataStr: " + responseDataStr);
                if (ServiceConfig.getWalletId().equals(merchantID)) {
                    if (!StringUtil.isTrimedEmpty(responseDataStr)) {
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
                            cardServerBaseResponse.returnCode = -99;
                            return;
                        }
                        cardServerBaseResponse.returnCode = Integer.parseInt(returnCodeStr);
                        if (cardServerBaseResponse.returnCode != 0) {
                            String err = " code:" + cardServerBaseResponse.returnCode + " desc:" + returnDesc;
                            HwLog.e("returnDesc : " + err);
                            return;
                        }
                        try {
                            makeResponseData(cardServerBaseResponse, dataObject);
                        } catch (NumberFormatException e) {
                            ex = e;
                            HwLog.e("NumberFormatException : " + ex);
                            cardServerBaseResponse.returnCode = -99;
                        } catch (JSONException e2) {
                            ex = e2;
                            HwLog.e("JSONException : " + ex);
                            cardServerBaseResponse.returnCode = -99;
                        }
                    }
                }
                HwLog.d("unexpected error from server.");
                cardServerBaseResponse.returnCode = -99;
            } catch (NumberFormatException e3) {
                ex = e3;
                HwLog.e("NumberFormatException : " + ex);
                cardServerBaseResponse.returnCode = -99;
            } catch (JSONException e4) {
                ex = e4;
                HwLog.e("JSONException : " + ex);
                cardServerBaseResponse.returnCode = -99;
            }
        } catch (NumberFormatException e5) {
            ex = e5;
            String str = responseStr;
            HwLog.e("NumberFormatException : " + ex);
            cardServerBaseResponse.returnCode = -99;
        } catch (JSONException e6) {
            ex = e6;
            String str2 = responseStr;
            HwLog.e("JSONException : " + ex);
            cardServerBaseResponse.returnCode = -99;
        }
    }
}
