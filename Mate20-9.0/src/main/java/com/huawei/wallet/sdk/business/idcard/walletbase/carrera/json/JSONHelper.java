package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.diploma.util.DiplomaUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.TCISParameterUtils;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
    public static final String APPID = "com.huawei.wallet";
    public static final String CARD_SERVER_NEW_PROTOCAL_VERSION = "1.0";
    public static final String CARD_SERVER_PROTOCAL_VERSION = "1.0";

    public static JSONObject createHeaderStr(String srcTransID, String commandStr, boolean isServiceTokenAuth) {
        JSONObject headerJson;
        if (commandStr == null) {
            return null;
        }
        LogX.i("createHeaderStr commandStr : " + commandStr + " srcTransID: " + srcTransID);
        try {
            headerJson = new JSONObject();
            headerJson.put("srcTranID", srcTransID);
            if (isServiceTokenAuth) {
                headerJson.put(HciConfigInfo.HCI_DATA_TYPE_VERSION, "1.0");
                headerJson.put("serviceTokenAuth", createServiceTokenAuthStr());
            } else {
                headerJson.put(HciConfigInfo.HCI_DATA_TYPE_VERSION, "1.0");
            }
            headerJson.put("ts", System.currentTimeMillis() / 1000);
            headerJson.put("commander", commandStr);
        } catch (JSONException e) {
            LogX.e("createHeaderObject, params invalid.");
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
        LogX.d("prepareRequestStr dataStr : " + dataObject.toString(), true);
        try {
            json = new JSONObject();
            json.put("merchantID", merchantId);
            json.put("keyIndex", rsaKeyIndex);
            json.put(SNBConstant.FIELD_DATA, new TCISParameterUtils().reSignJsonData(dataObject, context, true));
        } catch (JSONException e) {
            LogX.e("createRequestStr, params invalid.");
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
        if (object == null || !object.has(jsonIndex)) {
            return null;
        }
        return object.getString(jsonIndex);
    }

    public static int getIntValue(JSONObject object, String jsonIndex) throws JSONException {
        if (object == null || !object.has(jsonIndex)) {
            return -1;
        }
        return object.getInt(jsonIndex);
    }

    public static String parseBussiCertSign(Context context, String request) {
        LogX.d("JSONHelper|parseBussiCertSign begin:", true);
        String sign = DiplomaUtil.getSignature(context, request);
        LogX.d("JSONHelper|parseBussiCertSign end", false);
        return sign;
    }

    public static long getLongValue(JSONObject object, String jsonIndex) throws JSONException {
        if (object == null || !object.has(jsonIndex)) {
            return -1;
        }
        return object.getLong(jsonIndex);
    }
}
