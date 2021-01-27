package ohos.bundle;

import ohos.utils.fastjson.JSONObject;

public class ProfileConstants {
    public static final String ABILITIES = "abilities";
    public static final String API_VERSION = "apiVersion";
    public static final String APP = "app";
    public static final String APPLICATION = "application";
    public static final String BUNDLE_NAME = "bundleName";
    public static final String DESCRIPTION = "description";
    public static final String DEVICE_CONFIG = "deviceConfig";
    public static final String DEVICE_CONFIG_DEFAULT = "default";
    public static final String ICON = "icon";
    public static final String JOINT_USER_ID = "jointUserId";
    public static final String LABEL = "label";
    public static final String MODULE = "module";
    public static final String NAME = "name";
    public static final String REQ_SDK = "reqSdk";
    public static final String SDK_COMPATIBLE = "compatible";
    public static final String SDK_TARGET = "target";
    public static final String VENDOR = "vendor";
    public static final String VERSION = "version";
    public static final String VERSION_CODE = "code";
    public static final String VERSION_NAME = "name";

    public static String getJsonString(JSONObject jSONObject, String str) {
        return (jSONObject == null || !jSONObject.containsKey(str)) ? "" : jSONObject.get(str).toString();
    }

    public static boolean getJsonBoolean(JSONObject jSONObject, String str, boolean z) {
        return (jSONObject == null || !jSONObject.containsKey(str)) ? z : jSONObject.getBoolean(str).booleanValue();
    }

    public static int getJsonInt(JSONObject jSONObject, String str) {
        if (jSONObject == null || !jSONObject.containsKey(str)) {
            return 0;
        }
        return jSONObject.getIntValue(str);
    }
}
