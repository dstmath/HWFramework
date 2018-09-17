package com.huawei.android.pushselfshow.richpush.html.api;

import com.huawei.android.pushagent.a.a.c;
import org.json.JSONObject;

public class d {
    private static final String[] a = new String[]{"OK.", "Failed to start compass.", "Can't find method.", "Service not found.", "Class not found.", "Illegal access.", "Instantiation error.", "Malformed url.", "IO error.", "Invalid action.", "Illegal parameter.", "Subject to play the file is not found.", "Supports only HTTP / HTTPS or local file.", "Play abnormal, please try again.", "Application does not exist.", "Application does not exist, thus opening the application market.", "Application market does not exist.", "NetWork provider is not available.", "GPS provider is not available.", "NetWork Provider is out of service.", "GPS Provider is out of service.", "Location API is not available for this device.", "No sensors found to register accelerometer listening to.", "Accelerometer could not be started.", "Not found Sd card.", "Error"};
    private String b;
    private JSONObject c = null;

    public enum a {
        OK,
        FAILED_TO_START_COMPASS,
        METHOD_NOT_FOUND_EXCEPTION,
        SERVICE_NOT_FOUND_EXCEPTION,
        CLASS_NOT_FOUND_EXCEPTION,
        ILLEGAL_ACCESS_EXCEPTION,
        INSTANTIATION_EXCEPTION,
        MALFORMED_URL_EXCEPTION,
        IO_EXCEPTION,
        INVALID_ACTION,
        JSON_EXCEPTION,
        AUDIO_SRC_NOT_FOUND,
        AUDIO_ONLY_SUPPORT_HTTP,
        AUDIO_PLAY_ERROR,
        APP_NOT_EXIST,
        APP_OPEN_APPMARKET,
        APP_NOT_APPMARKET,
        POSITION_UNAVAILABLE_NETOWRK,
        POSITION_UNAVAILABLE_GPS,
        POSTION_OUT_OF_SERVICE_NETOWRK,
        POSTION_OUT_OF_SERVICE_GPS,
        POSTION_API_NOT_SUPPORT,
        ACCL_NO_SENSORS,
        ACCL_CAN_NOT_START,
        ACCL_NO_SDCARD,
        ERROR
    }

    public d(String str, a aVar) {
        this.b = str;
        this.c = a(aVar, new JSONObject());
    }

    public d(String str, a aVar, JSONObject jSONObject) {
        this.b = str;
        this.c = a(aVar, jSONObject);
    }

    public static JSONObject a(a aVar) {
        JSONObject jSONObject = new JSONObject();
        try {
            int ordinal = aVar.ordinal();
            jSONObject.put("result_code", ordinal);
            jSONObject.put("result_info", a[ordinal]);
            return jSONObject;
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "pluginRecsult encodeMsg error ", e);
            return jSONObject;
        }
    }

    private JSONObject a(a aVar, JSONObject jSONObject) {
        try {
            int ordinal = aVar.ordinal();
            jSONObject.put("result_code", ordinal);
            jSONObject.put("result_info", a[ordinal]);
            return jSONObject;
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "pluginRecsult encodeMsg error ", e);
            return null;
        }
    }

    public static String[] c() {
        Object obj = new String[a.length];
        System.arraycopy(a, 0, obj, 0, a.length);
        return obj;
    }

    public String a() {
        return this.b;
    }

    public JSONObject b() {
        return this.c;
    }
}
