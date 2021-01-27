package com.huawei.dmsdpsdk2.hiplay;

import com.huawei.dmsdp.devicevirtualization.Capability;
import com.huawei.dmsdpsdk2.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class HiPlayUtils {
    public static final String AUTH_MODE = "auth_mode";
    public static final String AUTH_MODE_DIFF_ACCOUNT_STR = "DIFF_ACCOUNT";
    public static final String AUTH_MODE_SAME_ACCOUNT_STR = "SAME_ACCOUNT";
    public static final String DISCOVER_MODE = "discover_mode";
    public static final int DISCOVER_NONE = 0;
    private static final String EMPTY_STR = "";
    public static final int HIPLAY_VERSION_TWO = 2;
    private static final String KEY_HIPLAY_VERSION = "hiplay_version";
    private static final int NONE_VERSION = 0;
    private static final String TAG = "HiPlayUtils";

    public static boolean isHiPlayNeedDevice(int deviceType) {
        if (deviceType == 3 || deviceType == 10 || deviceType == 6) {
            return true;
        }
        HwLog.i(TAG, "Not HiPlay need TV or VoideBox device");
        return false;
    }

    public static boolean isHiPlayNeedService(int serviceType) {
        if (serviceType == 4) {
            return true;
        }
        HwLog.i(TAG, "Not HiPlay need speaker service");
        return false;
    }

    public static boolean isHiPlayNeedCapability(Capability cat) {
        if (Capability.SPEAKER.equals(cat)) {
            return true;
        }
        HwLog.i(TAG, "Not HiPlay need Speaker Capability");
        return false;
    }

    public static String parseAuthMode(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            if (jsonObject.has(AUTH_MODE)) {
                if (!"".equals(jsonObject.getString(AUTH_MODE))) {
                    String authMode = jsonObject.getString(AUTH_MODE);
                    HwLog.i(TAG, "authMode:" + authMode);
                    return authMode;
                }
            }
            HwLog.e(TAG, "json is invalid");
            return "";
        } catch (JSONException e) {
            HwLog.e(TAG, "JSON parse fail.");
            return "";
        }
    }

    public static boolean isDiffAccount(String json) {
        if (!parseAuthMode(json).equals(AUTH_MODE_DIFF_ACCOUNT_STR)) {
            return false;
        }
        return true;
    }

    public static int parseVersion(String str) {
        try {
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.has(KEY_HIPLAY_VERSION)) {
                HwLog.e(TAG, "none hiPlay version");
                return 0;
            }
            int version = jsonObject.getInt(KEY_HIPLAY_VERSION);
            HwLog.i(TAG, "version:" + version);
            return version;
        } catch (JSONException e) {
            HwLog.e(TAG, "JSON parse fail.");
            return 0;
        }
    }

    public static boolean isUpperVersion(String json) {
        if (parseVersion(json) >= 2) {
            return true;
        }
        return false;
    }

    public static boolean checkJsonEmpty(String json) {
        if (json != null && !"".equals(json)) {
            return true;
        }
        HwLog.e(TAG, "connJson is null");
        return false;
    }

    public static boolean checkJsonHasKeys(String json, String... keys) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            for (String key : keys) {
                if (key != null) {
                    if (!key.isEmpty()) {
                        if (!jsonObject.has(key) || "".equals(jsonObject.getString(key))) {
                            HwLog.e(TAG, "key invalid");
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (JSONException e) {
            HwLog.e(TAG, "parse fail.");
            return false;
        }
    }
}
