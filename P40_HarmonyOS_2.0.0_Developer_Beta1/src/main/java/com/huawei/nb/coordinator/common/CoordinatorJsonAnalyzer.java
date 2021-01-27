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

    public static String getJsonValue(String str, String str2) {
        StringBuilder sb = new StringBuilder();
        try {
            if (!TextUtils.isEmpty(str)) {
                sb.append(new JSONObject(str).getString(str2));
            }
        } catch (JSONException unused) {
            DSLog.e("CoordinatorJsonAnalyzer JSONException when get JsonValue, jsonKey = " + str2, new Object[0]);
        }
        return sb.toString();
    }
}
