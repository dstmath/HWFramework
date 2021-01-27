package android.preference;

import java.util.Set;

@Deprecated
public interface PreferenceDataStore {
    default void putString(String key, String value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    default void putStringSet(String key, Set<String> set) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    default void putInt(String key, int value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    default void putLong(String key, long value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    default void putFloat(String key, float value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    default void putBoolean(String key, boolean value) {
        throw new UnsupportedOperationException("Not implemented on this data store");
    }

    default String getString(String key, String defValue) {
        return defValue;
    }

    default Set<String> getStringSet(String key, Set<String> defValues) {
        return defValues;
    }

    default int getInt(String key, int defValue) {
        return defValue;
    }

    default long getLong(String key, long defValue) {
        return defValue;
    }

    default float getFloat(String key, float defValue) {
        return defValue;
    }

    default boolean getBoolean(String key, boolean defValue) {
        return defValue;
    }
}
