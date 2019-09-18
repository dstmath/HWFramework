package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import android.os.Build;
import com.google.gson.Gson;
import com.huawei.wallet.sdk.common.apdu.tsm.bean.CommonRequestParams;
import com.huawei.wallet.sdk.common.apdu.tsm.business.ApduResBean;
import com.huawei.wallet.sdk.common.apdu.tsm.business.BaseBusinessForReq;
import com.huawei.wallet.sdk.common.apdu.tsm.business.BaseBusinessForReqNext;
import com.huawei.wallet.sdk.common.apdu.tsm.business.BaseRequest;
import com.huawei.wallet.sdk.common.log.LogC;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonUtil {
    private static final String TAG = "JsonUtil";

    private JsonUtil() {
    }

    public static int getIntValue(JSONObject jsonObject, String tag, int defaultValue) {
        int value = defaultValue;
        if (jsonObject.isNull(tag)) {
            return value;
        }
        try {
            return jsonObject.getInt(tag);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static String getStringValue(JSONObject jsonObject, String tag) {
        if (jsonObject.isNull(tag)) {
            return null;
        }
        try {
            return jsonObject.getString(tag);
        } catch (JSONException e) {
            return null;
        }
    }

    public static JSONArray getJsonArray(JSONObject jsonObject, String tag) {
        if (jsonObject != null && !jsonObject.isNull(tag)) {
            try {
                return jsonObject.getJSONArray(tag);
            } catch (Exception e) {
                LogC.d(TAG, "getJsonArray failed:" + tag, false);
            }
        }
        return null;
    }

    public static long getLongValue(JSONObject jsonObject, String tag, long defaultValue) {
        long value = defaultValue;
        if (jsonObject.isNull(tag)) {
            return value;
        }
        try {
            return jsonObject.getLong(tag);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static JSONObject getJsonObject(JSONObject jsonObject, String tag) {
        try {
            return jsonObject.getJSONObject(tag);
        } catch (Exception e) {
            LogC.w("getJsonObject error:" + tag, (Throwable) e, true);
            return null;
        }
    }

    public static double getDoubleValue(JSONObject jsonObject, String tag) {
        if (jsonObject.isNull(tag)) {
            return 0.0d;
        }
        try {
            return jsonObject.getDouble(tag);
        } catch (JSONException e) {
            LogC.d(TAG, "getDoubleValue failed.", false);
            return 0.0d;
        }
    }

    public static ArrayList<String> getStringArrayValue(JSONObject jsonObject, String tag) {
        ArrayList<String> list = null;
        try {
            JSONArray array = jsonObject.getJSONArray(tag);
            if (array != null) {
                list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.getString(i));
                }
            }
        } catch (JSONException e) {
            LogC.e("getStringArrayValue, json exception.", false);
        }
        return list;
    }

    public static String getReqNextJsonResult(Context context, CommonRequestParams params, int businessType, ApduResBean rapduList, int result, int taskIndex) {
        BaseRequest<BaseBusinessForReqNext> request = new BaseRequest<>();
        fillBaseData(request, params, context);
        request.setBusiness(BaseBusinessForReqNext.build(businessType, taskIndex, result, rapduList));
        return new Gson().toJson(request);
    }

    public static String getBaseReqJsonResult(Context context, CommonRequestParams params, int businessType, int taskIndex) {
        BaseRequest<BaseBusinessForReq> request = new BaseRequest<>();
        fillBaseData(request, params, context);
        request.setBusiness(BaseBusinessForReq.build(businessType, taskIndex));
        return new Gson().toJson(request);
    }

    private static void fillBaseData(BaseRequest<?> request, CommonRequestParams params, Context context) {
        request.setClientVersion("2.0.6");
        request.setImei("");
        request.setMobileType(Build.MODEL);
        request.setVersion("1.0");
        request.setServiceId(params.getServiceId());
        request.setFunctionCallId(params.getFunCallId());
        request.setSeid(params.getSeid());
    }
}
