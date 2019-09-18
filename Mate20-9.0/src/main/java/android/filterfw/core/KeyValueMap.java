package android.filterfw.core;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class KeyValueMap extends HashMap<String, Object> {
    public void setKeyValues(Object... keyValues) {
        if (keyValues.length % 2 == 0) {
            int i = 0;
            while (i < keyValues.length) {
                if (keyValues[i] instanceof String) {
                    put(keyValues[i], keyValues[i + 1]);
                    i += 2;
                } else {
                    throw new RuntimeException("Key-value argument " + i + " must be a key of type String, but found an object of type " + keyValues[i].getClass() + "!");
                }
            }
            return;
        }
        throw new RuntimeException("Key-Value arguments passed into setKeyValues must be an alternating list of keys and values!");
    }

    public static KeyValueMap fromKeyValues(Object... keyValues) {
        KeyValueMap result = new KeyValueMap();
        result.setKeyValues(keyValues);
        return result;
    }

    public String getString(String key) {
        Object result = get(key);
        if (result != null) {
            return (String) result;
        }
        return null;
    }

    public int getInt(String key) {
        Object result = get(key);
        return (result != null ? (Integer) result : null).intValue();
    }

    public float getFloat(String key) {
        Object result = get(key);
        return (result != null ? (Float) result : null).floatValue();
    }

    public String toString() {
        String valueString;
        StringWriter writer = new StringWriter();
        for (Map.Entry<String, Object> entry : entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                valueString = "\"" + value + "\"";
            } else {
                valueString = value.toString();
            }
            writer.write(entry.getKey() + " = " + valueString + ";\n");
        }
        return writer.toString();
    }
}
