package com.huawei.nb.utils;

import android.text.TextUtils;
import com.google.json.JsonSanitizer;
import com.huawei.gson.Gson;
import com.huawei.gson.JsonElement;
import com.huawei.gson.JsonParser;
import com.huawei.gson.JsonSyntaxException;
import com.huawei.nb.utils.logger.DSLog;
import java.text.Normalizer;

public final class JsonUtils {
    private JsonUtils() {
    }

    public static <T> T parse(String str, Class<T> cls) {
        if (TextUtils.isEmpty(str) || cls == null) {
            DSLog.e("Failed to parse json file, error: invalid parameter.", new Object[0]);
            return null;
        }
        try {
            return (T) new Gson().fromJson(sanitize(str), cls);
        } catch (JsonSyntaxException e) {
            DSLog.e("Failed to parse json file, error: %s.", e.getMessage());
            return null;
        }
    }

    public static JsonElement parse(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.e("Failed to parse json string, error: Json is empty.", new Object[0]);
            return null;
        }
        try {
            return new JsonParser().parse(sanitize(str));
        } catch (JsonSyntaxException unused) {
            return null;
        }
    }

    public static boolean isValidJson(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.e(" Json is empty!", new Object[0]);
            return false;
        }
        try {
            new JsonParser().parse(sanitize(str));
            return true;
        } catch (JsonSyntaxException | IllegalStateException unused) {
            return false;
        }
    }

    public static boolean isJsonObject(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.e(" Json is empty!", new Object[0]);
            return false;
        }
        try {
            return new JsonParser().parse(sanitize(str)).isJsonObject();
        } catch (JsonSyntaxException | IllegalStateException unused) {
            return false;
        }
    }

    public static boolean isValidJsonArray(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.e("Failed to parse json string, error: JsonArray is empty.", new Object[0]);
            return false;
        }
        try {
            if (new JsonParser().parse(sanitize(str)).getAsJsonArray().size() >= 0) {
                return true;
            }
            return false;
        } catch (JsonSyntaxException | IllegalStateException unused) {
            return false;
        }
    }

    public static boolean isJsonFormat(String str) {
        if (TextUtils.isEmpty(str)) {
            DSLog.e(" Json is empty! ", new Object[0]);
            return false;
        }
        String normalize = Normalizer.normalize(str, Normalizer.Form.NFKC);
        if (parse(sanitize(normalize)) == null || !normalize.startsWith("{")) {
            return false;
        }
        return true;
    }

    public static String sanitize(String str) {
        return JsonSanitizer.sanitize(str);
    }
}
