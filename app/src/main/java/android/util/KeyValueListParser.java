package android.util;

import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;

public class KeyValueListParser {
    private final StringSplitter mSplitter;
    private final ArrayMap<String, String> mValues;

    public KeyValueListParser(char delim) {
        this.mValues = new ArrayMap();
        this.mSplitter = new SimpleStringSplitter(delim);
    }

    public void setString(String str) throws IllegalArgumentException {
        this.mValues.clear();
        if (str != null) {
            this.mSplitter.setString(str);
            for (String pair : this.mSplitter) {
                int sep = pair.indexOf(61);
                if (sep < 0) {
                    this.mValues.clear();
                    throw new IllegalArgumentException("'" + pair + "' in '" + str + "' is not a valid key-value pair");
                }
                this.mValues.put(pair.substring(0, sep).trim(), pair.substring(sep + 1).trim());
            }
        }
    }

    public int getInt(String key, int def) {
        String value = (String) this.mValues.get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    public long getLong(String key, long def) {
        String value = (String) this.mValues.get(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    public float getFloat(String key, float def) {
        String value = (String) this.mValues.get(key);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

    public String getString(String key, String def) {
        String value = (String) this.mValues.get(key);
        if (value != null) {
            return value;
        }
        return def;
    }
}
