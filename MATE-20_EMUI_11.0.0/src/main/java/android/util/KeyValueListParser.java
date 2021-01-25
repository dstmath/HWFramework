package android.util;

import android.media.TtmlUtils;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.format.DateTimeParseException;

public class KeyValueListParser {
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
                    if (!value.startsWith(TtmlUtils.TAG_P)) {
                        return Long.parseLong(value);
                    }
                }
                return Duration.parse(value).toMillis();
            } catch (NumberFormatException | DateTimeParseException e) {
            }
        }
        return def;
    }

    public static class IntValue {
        private final int mDefaultValue;
        private final String mKey;
        private int mValue = this.mDefaultValue;

        public IntValue(String key, int defaultValue) {
            this.mKey = key;
            this.mDefaultValue = defaultValue;
        }

        public void parse(KeyValueListParser parser) {
            this.mValue = parser.getInt(this.mKey, this.mDefaultValue);
        }

        public String getKey() {
            return this.mKey;
        }

        public int getDefaultValue() {
            return this.mDefaultValue;
        }

        public int getValue() {
            return this.mValue;
        }

        public void setValue(int value) {
            this.mValue = value;
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.print(this.mKey);
            pw.print("=");
            pw.print(this.mValue);
            pw.println();
        }

        public void dumpProto(ProtoOutputStream proto, long tag) {
            proto.write(tag, this.mValue);
        }
    }

    public static class LongValue {
        private final long mDefaultValue;
        private final String mKey;
        private long mValue = this.mDefaultValue;

        public LongValue(String key, long defaultValue) {
            this.mKey = key;
            this.mDefaultValue = defaultValue;
        }

        public void parse(KeyValueListParser parser) {
            this.mValue = parser.getLong(this.mKey, this.mDefaultValue);
        }

        public String getKey() {
            return this.mKey;
        }

        public long getDefaultValue() {
            return this.mDefaultValue;
        }

        public long getValue() {
            return this.mValue;
        }

        public void setValue(long value) {
            this.mValue = value;
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.print(this.mKey);
            pw.print("=");
            pw.print(this.mValue);
            pw.println();
        }

        public void dumpProto(ProtoOutputStream proto, long tag) {
            proto.write(tag, this.mValue);
        }
    }

    public static class StringValue {
        private final String mDefaultValue;
        private final String mKey;
        private String mValue = this.mDefaultValue;

        public StringValue(String key, String defaultValue) {
            this.mKey = key;
            this.mDefaultValue = defaultValue;
        }

        public void parse(KeyValueListParser parser) {
            this.mValue = parser.getString(this.mKey, this.mDefaultValue);
        }

        public String getKey() {
            return this.mKey;
        }

        public String getDefaultValue() {
            return this.mDefaultValue;
        }

        public String getValue() {
            return this.mValue;
        }

        public void setValue(String value) {
            this.mValue = value;
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.print(this.mKey);
            pw.print("=");
            pw.print(this.mValue);
            pw.println();
        }

        public void dumpProto(ProtoOutputStream proto, long tag) {
            proto.write(tag, this.mValue);
        }
    }

    public static class FloatValue {
        private final float mDefaultValue;
        private final String mKey;
        private float mValue = this.mDefaultValue;

        public FloatValue(String key, float defaultValue) {
            this.mKey = key;
            this.mDefaultValue = defaultValue;
        }

        public void parse(KeyValueListParser parser) {
            this.mValue = parser.getFloat(this.mKey, this.mDefaultValue);
        }

        public String getKey() {
            return this.mKey;
        }

        public float getDefaultValue() {
            return this.mDefaultValue;
        }

        public float getValue() {
            return this.mValue;
        }

        public void setValue(float value) {
            this.mValue = value;
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.print(this.mKey);
            pw.print("=");
            pw.print(this.mValue);
            pw.println();
        }

        public void dumpProto(ProtoOutputStream proto, long tag) {
            proto.write(tag, this.mValue);
        }
    }
}
