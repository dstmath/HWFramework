package com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.impl;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.diploma.util.DiplomaUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.log.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;

public final class JSONHelper {
    public static final String APPID = "com.huawei.wallet";
    public static final String CARD_SERVER_NEW_PROTOCAL_VERSION = "1.0";
    public static final String CARD_SERVER_NEW_PROTOCAL_VERSION_1 = "1.1";
    public static final String CARD_SERVER_PROTOCAL_VERSION = "1.0";

    public static JSONObject createHeaderStr(String srcTransID, String commandStr, boolean isNeedServiceTokenAuth) {
        JSONObject headerJson;
        if (commandStr == null) {
            LogC.i("JSONHelper createHeaderStr commandstr is null", false);
            return null;
        }
        LogC.d("createHeaderStr commandStr : " + commandStr, true);
        try {
            headerJson = new JSONObject();
            headerJson.put("srcTranID", srcTransID);
            if (isNeedServiceTokenAuth) {
                headerJson.put(HciConfigInfo.HCI_DATA_TYPE_VERSION, LogUtil.isDebugBuild() ? "1.0" : CARD_SERVER_NEW_PROTOCAL_VERSION_1);
                headerJson.put("serviceTokenAuth", createServiceTokenAuthStr());
            } else {
                headerJson.put(HciConfigInfo.HCI_DATA_TYPE_VERSION, "1.0");
            }
            headerJson.put("ts", System.currentTimeMillis() / 1000);
            headerJson.put("commander", commandStr);
        } catch (JSONException e) {
            Log.e("", "createHeaderObject, params invalid.");
            headerJson = null;
        }
        return headerJson;
    }

    public static String createRequestStr(String merchantId, int rsaKeyIndex, JSONObject dataObject, Context context) {
        JSONObject json;
        String str = null;
        if (dataObject == null) {
            return null;
        }
        LogC.d("prepareRequestStr dataStr : " + dataObject.toString(), true);
        try {
            json = new JSONObject();
            json.put("merchantID", merchantId);
            json.put("keyIndex", rsaKeyIndex);
            json.put(SNBConstant.FIELD_DATA, dataObject.toString());
        } catch (JSONException e) {
            Log.e("", "createRequestStr, params invalid.");
            json = null;
        }
        if (json != null) {
            str = json.toString();
        }
        return str;
    }

    public static JSONObject createServiceTokenAuthStr() {
        LogX.d("JSONHelper createServiceTokenAuthStr  begin", false);
        try {
            JSONObject serviceTokenAuth = new JSONObject();
            serviceTokenAuth.put("appID", "com.huawei.wallet");
            serviceTokenAuth.put("terminalType", Build.MODEL);
            return serviceTokenAuth;
        } catch (JSONException e) {
            LogX.i("createServiceTokenAuthStr, accountInfo invalid.", false);
            return null;
        }
    }

    public static String getStringValue(JSONObject object, String jsonIndex) throws JSONException {
        if (object.has(jsonIndex)) {
            return object.getString(jsonIndex);
        }
        return null;
    }

    public static int getIntValue(JSONObject object, String jsonIndex) throws JSONException {
        if (object.has(jsonIndex)) {
            return object.getInt(jsonIndex);
        }
        return -1;
    }

    public static String parseBussiCertSign(Context context, String request) {
        LogX.d("JSONHelper|parseBussiCertSign begin :", false);
        String sign = DiplomaUtil.getSignature(context, request);
        LogX.d("JSONHelper|parseBussiCertSign end :", false);
        return sign;
    }
}
