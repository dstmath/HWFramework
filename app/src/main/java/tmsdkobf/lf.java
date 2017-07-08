package tmsdkobf;

import java.util.Map;

/* compiled from: Unknown */
public interface lf {
    void beginTransaction();

    void clear();

    boolean d(String str, long j);

    boolean d(String str, boolean z);

    boolean dj();

    boolean e(String str, int i);

    Map<String, ?> getAll();

    boolean getBoolean(String str, boolean z);

    int getInt(String str, int i);

    long getLong(String str, long j);

    String getString(String str, String str2);

    boolean m(String str, String str2);
}
