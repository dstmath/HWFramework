package ohos.data.preferences;

import java.util.Map;
import java.util.Set;

public interface Preferences {
    public static final int MAX_KEY_LENGTH = 80;
    public static final int MAX_VALUE_LENGTH = 8192;

    public interface PreferencesObserver {
        void onChange(Preferences preferences, String str);
    }

    Preferences clear();

    Preferences delete(String str);

    void flush();

    boolean flushSync();

    Map<String, ?> getAll();

    boolean getBoolean(String str, boolean z);

    float getFloat(String str, float f);

    int getInt(String str, int i);

    long getLong(String str, long j);

    String getString(String str, String str2);

    Set<String> getStringSet(String str, Set<String> set);

    boolean hasKey(String str);

    Preferences putBoolean(String str, boolean z);

    Preferences putFloat(String str, float f);

    Preferences putInt(String str, int i);

    Preferences putLong(String str, long j);

    Preferences putString(String str, String str2);

    Preferences putStringSet(String str, Set<String> set);

    void registerObserver(PreferencesObserver preferencesObserver);

    void unregisterObserver(PreferencesObserver preferencesObserver);
}
