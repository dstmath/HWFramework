package android.content;

import java.util.Map;
import java.util.Set;

public interface SharedPreferences {

    public interface Editor {
        void apply();

        Editor clear();

        boolean commit();

        Editor putBoolean(String str, boolean z);

        Editor putFloat(String str, float f);

        Editor putInt(String str, int i);

        Editor putLong(String str, long j);

        Editor putString(String str, String str2);

        Editor putStringSet(String str, Set<String> set);

        Editor remove(String str);
    }

    public interface OnSharedPreferenceChangeListener {
        void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str);
    }

    boolean contains(String str);

    Editor edit();

    Map<String, ?> getAll();

    boolean getBoolean(String str, boolean z);

    float getFloat(String str, float f);

    int getInt(String str, int i);

    long getLong(String str, long j);

    String getString(String str, String str2);

    Set<String> getStringSet(String str, Set<String> set);

    void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener);

    void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener);
}
