package android.common;

import android.content.Context;
import java.util.Date;
import org.json.JSONObject;

public interface HwFlogManager {
    void bdReport(int i);

    void bdReport(int i, String str);

    void bdReport(int i, String str, byte b);

    void bdReport(int i, String str, float f);

    void bdReport(int i, String str, int i2);

    void bdReport(int i, String str, long j);

    void bdReport(int i, String str, String str2);

    void bdReport(int i, String str, Date date);

    void bdReport(int i, String str, boolean z);

    void bdReport(int i, JSONObject jSONObject);

    @Deprecated
    boolean bdReport(Context context, int i);

    @Deprecated
    boolean bdReport(Context context, int i, String str);

    @Deprecated
    boolean bdReport(Context context, int i, JSONObject jSONObject);

    @Deprecated
    boolean bdReport(Context context, int i, JSONObject jSONObject, int i2);

    boolean handleLogRequest(String[] strArr);

    int slog(int i, int i2, String str);

    int slog(int i, int i2, String str, Throwable th);

    int slogd(String str, String str2);

    int slogv(String str, String str2);
}
