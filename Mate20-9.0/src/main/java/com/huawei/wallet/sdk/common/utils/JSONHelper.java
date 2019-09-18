package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.diploma.util.DiplomaUtil;
import com.huawei.wallet.sdk.common.log.LogC;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
    public static JSONObject createHeaderStr(String srcTransID, String commandStr) {
        JSONObject headerJson;
        if (commandStr == null) {
            return null;
        }
        LogC.i("createHeaderStr commandStr : " + commandStr + " srcTransID: " + srcTransID, false);
        try {
            headerJson = new JSONObject();
            headerJson.put("srcTranID", srcTransID);
            headerJson.put(HciConfigInfo.HCI_DATA_TYPE_VERSION, "1.0");
            headerJson.put("ts", System.currentTimeMillis() / 1000);
            headerJson.put("commander", commandStr);
        } catch (JSONException e) {
            LogC.e("createHeaderObject, params invalid.", false);
            headerJson = null;
        }
        return headerJson;
    }

    public static JSONObject createCommonStr(String spOrderid, String hwOrderid, String source, String accessTransid, String mcid, String actionTransid, String payType) {
        try {
            JSONObject commonJson = new JSONObject();
            commonJson.put("spOrderid", spOrderid);
            commonJson.put("hwOrderid", hwOrderid);
            commonJson.put("source", source);
            commonJson.put("accessTransid", accessTransid);
            commonJson.put("mcid", mcid);
            commonJson.put("actionTransid", actionTransid);
            commonJson.put("payType", payType);
            return commonJson;
        } catch (JSONException e) {
            LogC.e("createCommonStr, params invalid.", false);
            return null;
        }
    }

    public static String createRequestStr(String merchantId, int rsaKeyIndex, JSONObject dataObject, Context context) {
        JSONObject json;
        String str = null;
        if (dataObject == null) {
            return null;
        }
        try {
            json = new JSONObject();
            json.put("merchantID", merchantId);
            json.put("keyIndex", rsaKeyIndex);
            json.put(SNBConstant.FIELD_DATA, dataObject.toString());
            LogC.d("prepareRequestStr createRequestStr : " + json.toString(), true);
        } catch (JSONException e) {
            LogC.e("createRequestStr, params invalid.", false);
            json = null;
        }
        if (json != null) {
            str = json.toString();
        }
        return str;
    }

    public static long getLongValue(JSONObject object, String jsonIndex) throws JSONException {
        if (object == null || !object.has(jsonIndex)) {
            return -1;
        }
        return object.getLong(jsonIndex);
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
        LogC.d("JSONHelper|getRequestAndSign|getSortedJson :" + request, false);
        String sign = DiplomaUtil.getSignature(context, request);
        LogC.d("JSONHelper|getRequestAndSign|getSignature :" + sign, false);
        return sign;
    }

    private static String getSortedJson(JSONObject originalJson) throws JSONException {
        String sortedJson = "";
        if (originalJson == null || originalJson.length() == 0) {
            return null;
        }
        Iterator<String> keysIterator = originalJson.keys();
        SortedMap map = new TreeMap();
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            map.put(key, originalJson.optString(key));
        }
        for (String key2 : map.keySet()) {
            String value = map.get(key2).toString();
            if ("header".equals(key2)) {
                value = getSortedJson(new JSONObject(value));
            }
            sortedJson = sortedJson + SNBConstant.FILTER + key2 + "=" + value;
        }
        return sortedJson.substring(1, sortedJson.length());
    }
}
