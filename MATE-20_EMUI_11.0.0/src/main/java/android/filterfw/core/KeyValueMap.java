package android.filterfw.core;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class KeyValueMap extends HashMap<String, Object> {
    public void setKeyValues(Object... keyValues) {
        if (keyValues.length % 2 == 0) {
            for (int i = 0; i < keyValues.length; i += 2) {
                if (keyValues[i] instanceof String) {
                    put((String) keyValues[i], keyValues[i + 1]);
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

    @Override // java.util.AbstractMap, java.lang.Object
    public String toString() {
        StringWriter writer = new StringWriter();
        for (Map.Entry<String, Object> entry : entrySet()) {
            Object value = entry.getValue();
            writer.write(entry.getKey() + " = " + (value instanceof String ? "\"" + value + "\"" : value.toString()) + ";\n");
        }
        return writer.toString();
    }
}
