package com.huawei.nb.coordinator.common;

import android.text.TextUtils;
import com.huawei.nb.utils.logger.DSLog;
import org.json.JSONException;
import org.json.JSONObject;

public final class CoordinatorJsonAnalyzer {
    public static final String CODE_TYPE = "code";
    public static final String DATA_TYPE = "data";
    public static final String DESC_TYPE = "desc";
    public static final String MSG_TYPE = "message";
    public static final String PARAMS_TYPE = "params";
    public static final String RAND_TYPE = "rand";
    public static final String SESSIONID_TYPE = "sessionId";
    private static final String TAG = "CoordinatorJsonAnalyzer";

    private CoordinatorJsonAnalyzer() {
    }

    public static String getJsonValue(String json, String jsonKey) {
        StringBuilder jsonValue = new StringBuilder();
        try {
            if (!TextUtils.isEmpty(json)) {
                jsonValue.append(new JSONObject(json).getString(jsonKey));
            }
        } catch (JSONException e) {
            DSLog.e("CoordinatorJsonAnalyzer JSONException when get JsonValue, jsonKey = " + jsonKey, new Object[0]);
        }
        return jsonValue.toString();
    }
}
