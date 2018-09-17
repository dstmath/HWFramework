package android.common;

import android.content.Context;
import org.json.JSONObject;

public interface HwFlogManager {
    boolean bdReport(Context context, int i);

    boolean bdReport(Context context, int i, String str);

    boolean bdReport(Context context, int i, JSONObject jSONObject);

    boolean bdReport(Context context, int i, JSONObject jSONObject, int i2);

    boolean handleLogRequest(String[] strArr);

    int slog(int i, int i2, String str);

    int slog(int i, int i2, String str, Throwable th);

    int slogd(String str, String str2);

    int slogv(String str, String str2);
}
