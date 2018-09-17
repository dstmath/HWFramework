package com.android.server.security.tsmagent.server.wallet.impl;

import android.content.Context;
import android.util.Log;
import com.android.server.security.tsmagent.utils.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public final class JSONHelper {
    private static final String CARD_SERVER_PROTOCAL_VERSION = "1.0";

    public static JSONObject createHeaderStr(String srcTransID, String commandStr) {
        JSONObject jSONObject;
        if (commandStr == null) {
            return null;
        }
        HwLog.d("createHeaderStr commandStr : " + commandStr);
        try {
            JSONObject headerJson = new JSONObject();
            try {
                headerJson.put("srcTranID", srcTransID);
                headerJson.put("version", "1.0");
                headerJson.put("ts", System.currentTimeMillis() / 1000);
                headerJson.put("commander", commandStr);
                jSONObject = headerJson;
            } catch (JSONException e) {
                HwLog.e("createHeaderObject, params invalid.");
                jSONObject = null;
                return jSONObject;
            }
        } catch (JSONException e2) {
            HwLog.e("createHeaderObject, params invalid.");
            jSONObject = null;
            return jSONObject;
        }
        return jSONObject;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0037  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String createRequestStr(String merchantId, JSONObject dataObject, Context context) {
        JSONObject json;
        String str = null;
        if (dataObject == null) {
            return null;
        }
        HwLog.d("prepareRequestStr dataStr");
        try {
            JSONObject json2 = new JSONObject();
            try {
                json2.put("merchantID", merchantId);
                json2.put("keyIndex", -1);
                json2.put("data", dataObject.toString());
                json = json2;
            } catch (JSONException e) {
                Log.e("", "createRequestStr, params invalid.");
                json = null;
                if (json != null) {
                }
                return str;
            }
        } catch (JSONException e2) {
            Log.e("", "createRequestStr, params invalid.");
            json = null;
            if (json != null) {
            }
            return str;
        }
        if (json != null) {
            str = json.toString();
        }
        return str;
    }

    public static String getStringValue(JSONObject object, String jsonIndex) throws JSONException {
        if (object == null || jsonIndex == null || !object.has(jsonIndex)) {
            return null;
        }
        return object.getString(jsonIndex);
    }

    public static int getIntValue(JSONObject object, String jsonIndex) throws JSONException {
        if (object.has(jsonIndex)) {
            return object.getInt(jsonIndex);
        }
        return -1;
    }
}
