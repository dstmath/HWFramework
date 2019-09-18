package com.android.server.security.tsmagent.server.wallet.impl;

import android.content.Context;
import android.util.Log;
import com.android.server.security.tsmagent.utils.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public final class JSONHelper {
    private static final String CARD_SERVER_PROTOCAL_VERSION = "1.0";

    public static JSONObject createHeaderStr(String srcTransID, String commandStr) {
        JSONObject headerJson;
        if (commandStr == null) {
            return null;
        }
        HwLog.d("createHeaderStr commandStr : " + commandStr);
        try {
            headerJson = new JSONObject();
            headerJson.put("srcTranID", srcTransID);
            headerJson.put("version", "1.0");
            headerJson.put("ts", System.currentTimeMillis() / 1000);
            headerJson.put("commander", commandStr);
        } catch (JSONException e) {
            HwLog.e("createHeaderObject, params invalid.");
            headerJson = null;
        }
        return headerJson;
    }

    public static String createRequestStr(String merchantId, JSONObject dataObject, Context context) {
        JSONObject json;
        String str = null;
        if (dataObject == null) {
            return null;
        }
        HwLog.d("prepareRequestStr dataStr");
        try {
            json = new JSONObject();
            json.put("merchantID", merchantId);
            json.put("keyIndex", -1);
            json.put("data", dataObject.toString());
        } catch (JSONException e) {
            Log.e("", "createRequestStr, params invalid.");
            json = null;
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
