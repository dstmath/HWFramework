package tmsdkobf;

import java.util.Map;

public interface jx {
    void beginTransaction();

    void clear();

    void endTransaction();

    Map<String, ?> getAll();

    boolean getBoolean(String str, boolean z);

    int getInt(String str);

    int getInt(String str, int i);

    long getLong(String str, long j);

    String getString(String str, String str2);

    void putBoolean(String str, boolean z);

    void putInt(String str, int i);

    void putLong(String str, long j);

    void putString(String str, String str2);

    void remove(String str);
}
