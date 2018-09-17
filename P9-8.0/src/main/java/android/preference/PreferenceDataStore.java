package android.preference;

import java.util.Set;

public interface PreferenceDataStore {
    void putString(String key, String value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    void putStringSet(String key, Set<String> set) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    void putInt(String key, int value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    void putLong(String key, long value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    void putFloat(String key, float value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    void putBoolean(String key, boolean value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    String getString(String key, String defValue) {
        return defValue;
    }

    Set<String> getStringSet(String key, Set<String> defValues) {
        return defValues;
    }

    int getInt(String key, int defValue) {
        return defValue;
    }

    long getLong(String key, long defValue) {
        return defValue;
    }

    float getFloat(String key, float defValue) {
        return defValue;
    }

    boolean getBoolean(String key, boolean defValue) {
        return defValue;
    }
}
