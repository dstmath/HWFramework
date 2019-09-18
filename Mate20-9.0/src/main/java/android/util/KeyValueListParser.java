package android.util;

import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import java.time.Duration;
import java.time.format.DateTimeParseException;

public class KeyValueListParser {
    private static final String TAG = "KeyValueListParser";
    private final TextUtils.StringSplitter mSplitter;
    private final ArrayMap<String, String> mValues = new ArrayMap<>();

    public KeyValueListParser(char delim) {
        this.mSplitter = new TextUtils.SimpleStringSplitter(delim);
    }

    public void setString(String str) throws IllegalArgumentException {
        this.mValues.clear();
        if (str != null) {
            this.mSplitter.setString(str);
            for (String pair : this.mSplitter) {
                int sep = pair.indexOf(61);
                if (sep >= 0) {
                    this.mValues.put(pair.substring(0, sep).trim(), pair.substring(sep + 1).trim());
                } else {
                    this.mValues.clear();
                    throw new IllegalArgumentException("'" + pair + "' in '" + str + "' is not a valid key-value pair");
                }
            }
        }
    }

    public int getInt(String key, int def) {
        String value = this.mValues.get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "NumberFormatException: getInt");
            }
        }
        return def;
    }

    public long getLong(String key, long def) {
        String value = this.mValues.get(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "NumberFormatException: getLong");
            }
        }
        return def;
    }

    public float getFloat(String key, float def) {
        String value = this.mValues.get(key);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "NumberFormatException: getFloat");
            }
        }
        return def;
    }

    public String getString(String key, String def) {
        String value = this.mValues.get(key);
        if (value != null) {
            return value;
        }
        return def;
    }

    public boolean getBoolean(String key, boolean def) {
        String value = this.mValues.get(key);
        if (value != null) {
            try {
                return Boolean.parseBoolean(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Could not change value to boolean");
            }
        }
        return def;
    }

    public int[] getIntArray(String key, int[] def) {
        String value = this.mValues.get(key);
        if (value != null) {
            try {
                String[] parts = value.split(SettingsStringUtil.DELIMITER);
                if (parts.length > 0) {
                    int[] ret = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        ret[i] = Integer.parseInt(parts[i]);
                    }
                    return ret;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "NumberFormatException: getIntArray");
            }
        }
        return def;
    }

    public int size() {
        return this.mValues.size();
    }

    public String keyAt(int index) {
        return this.mValues.keyAt(index);
    }

    public long getDurationMillis(String key, long def) {
        String value = this.mValues.get(key);
        if (value != null) {
            try {
                if (!value.startsWith("P")) {
                    if (!value.startsWith("p")) {
                        return Long.parseLong(value);
                    }
                }
                return Duration.parse(value).toMillis();
            } catch (NumberFormatException | DateTimeParseException e) {
                Log.e(TAG, "NumberFormatException | DateTimeParseException: getDurationMillis");
            }
        }
        return def;
    }
}
