package com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.impl;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.util.log.LogErrorConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.constant.BaseConfigrations;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.HttpConnTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.unionpay.tsmservice.data.Constant;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseWalletTask<Result, RequestParams> extends HttpConnTask<Result, RequestParams> {
    /* access modifiers changed from: protected */
    public abstract int getErrorLogConstant();

    /* access modifiers changed from: protected */
    public abstract String getTag();

    /* access modifiers changed from: protected */
    public abstract void makeResponseData(CardServerBaseResponse cardServerBaseResponse, JSONObject jSONObject) throws JSONException;

    public BaseWalletTask(Context context, String url) {
        super(context, url);
    }

    public BaseWalletTask(Context context, String url, int connTimeout, int socketTimeout) {
        super(context, url, connTimeout, socketTimeout);
    }

    /* access modifiers changed from: protected */
    public void resolveResponse(CardServerBaseResponse response, String responseStr) {
        CardServerBaseResponse cardServerBaseResponse = response;
        String str = responseStr;
        String TAG = getTag();
        if (StringUtil.isEmpty(str, true)) {
            cardServerBaseResponse.returnCode = -99;
            return;
        }
        JSONObject responseJson = null;
        try {
            responseJson = new JSONObject(str);
            String merchantID = JSONHelper.getStringValue(responseJson, "merchantID");
            int keyIndex = JSONHelper.getIntValue(responseJson, "keyIndex");
            String responseDataStr = JSONHelper.getStringValue(responseJson, "response");
            String errorCode = JSONHelper.getStringValue(responseJson, Constant.KEY_ERROR_CODE);
            String errorMsg = JSONHelper.getStringValue(responseJson, "errorMsg");
            if (errorCode != null) {
                String str2 = " code:" + errorCode + " msg:" + errorMsg;
                cardServerBaseResponse.returnCode = Integer.parseInt(errorCode);
                return;
            }
            if (BaseConfigrations.getWalletMerchantId().equals(merchantID) && !StringUtil.isEmpty(responseDataStr, true)) {
                if (keyIndex == -1) {
                    LogC.d(TAG + ", decryptedResponse : " + decryptedResponse, true);
                    JSONObject dataObject = new JSONObject(responseDataStr);
                    String returnCodeStr = JSONHelper.getStringValue(dataObject, "returnCode");
                    String returnDesc = JSONHelper.getStringValue(dataObject, "returnDesc");
                    if (returnCodeStr == null) {
                        LogC.d(TAG + ", returnCode is invalid.", false);
                        cardServerBaseResponse.returnCode = -99;
                        return;
                    }
                    cardServerBaseResponse.returnCode = Integer.parseInt(returnCodeStr);
                    if (cardServerBaseResponse.returnCode != 0) {
                        String err = " code:" + cardServerBaseResponse.returnCode + " desc:" + returnDesc;
                        LogC.e(TAG + ", returnDesc : " + err + getErrorLogConstant() + LogErrorConstant.getLocalAndErrMap(TAG, err), false);
                        return;
                    }
                    try {
                        makeResponseData(cardServerBaseResponse, dataObject);
                    } catch (NumberFormatException e) {
                        ex = e;
                        LogC.e(TAG, ", NumberFormatException : " + ex.getMessage() + LogErrorConstant.EXCEPTION_NUMBERFORMAT_ERR + LogErrorConstant.getLocalAndErrMap(TAG, ex.getMessage()), false);
                        cardServerBaseResponse.returnCode = -99;
                    } catch (JSONException e2) {
                        ex = e2;
                        LogC.e(TAG, ", JSONException : " + ex.getMessage(), null, LogErrorConstant.EXCEPTION_JSON_ERR, LogErrorConstant.getLocalAndErrMap(TAG, ex.getMessage()), false, true);
                        cardServerBaseResponse.returnCode = -99;
                        JSONObject jSONObject = responseJson;
                    }
                }
            }
            LogC.d(TAG + ", unexpected error from server.", false);
            cardServerBaseResponse.returnCode = -99;
        } catch (NumberFormatException e3) {
            ex = e3;
            LogC.e(TAG, ", NumberFormatException : " + ex.getMessage() + LogErrorConstant.EXCEPTION_NUMBERFORMAT_ERR + LogErrorConstant.getLocalAndErrMap(TAG, ex.getMessage()), false);
            cardServerBaseResponse.returnCode = -99;
        } catch (JSONException e4) {
            ex = e4;
            LogC.e(TAG, ", JSONException : " + ex.getMessage(), null, LogErrorConstant.EXCEPTION_JSON_ERR, LogErrorConstant.getLocalAndErrMap(TAG, ex.getMessage()), false, true);
            cardServerBaseResponse.returnCode = -99;
            JSONObject jSONObject2 = responseJson;
        }
    }
}
