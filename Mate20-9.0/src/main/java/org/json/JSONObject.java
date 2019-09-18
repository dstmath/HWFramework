package org.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.json.JSONStringer;

public class JSONObject {
    private static final Double NEGATIVE_ZERO = Double.valueOf(-0.0d);
    public static final Object NULL = new Object() {
        public boolean equals(Object o) {
            return o == this || o == null;
        }

        public int hashCode() {
            return Objects.hashCode(null);
        }

        public String toString() {
            return "null";
        }
    };
    private final LinkedHashMap<String, Object> nameValuePairs;

    public JSONObject() {
        this.nameValuePairs = new LinkedHashMap<>();
    }

    public JSONObject(Map copyFrom) {
        this();
        for (Map.Entry<?, ?> entry : copyFrom.entrySet()) {
            String key = (String) entry.getKey();
            if (key != null) {
                this.nameValuePairs.put(key, wrap(entry.getValue()));
            } else {
                throw new NullPointerException("key == null");
            }
        }
    }

    public JSONObject(JSONTokener readFrom) throws JSONException {
        Object object = readFrom.nextValue();
        if (object instanceof JSONObject) {
            this.nameValuePairs = ((JSONObject) object).nameValuePairs;
            return;
        }
        throw JSON.typeMismatch(object, "JSONObject");
    }

    public JSONObject(String json) throws JSONException {
        this(new JSONTokener(json));
    }

    public JSONObject(JSONObject copyFrom, String[] names) throws JSONException {
        this();
        for (String name : names) {
            Object value = copyFrom.opt(name);
            if (value != null) {
                this.nameValuePairs.put(name, value);
            }
        }
    }

    public int length() {
        return this.nameValuePairs.size();
    }

    public JSONObject put(String name, boolean value) throws JSONException {
        this.nameValuePairs.put(checkName(name), Boolean.valueOf(value));
        return this;
    }

    public JSONObject put(String name, double value) throws JSONException {
        this.nameValuePairs.put(checkName(name), Double.valueOf(JSON.checkDouble(value)));
        return this;
    }

    public JSONObject put(String name, int value) throws JSONException {
        this.nameValuePairs.put(checkName(name), Integer.valueOf(value));
        return this;
    }

    public JSONObject put(String name, long value) throws JSONException {
        this.nameValuePairs.put(checkName(name), Long.valueOf(value));
        return this;
    }

    public JSONObject put(String name, Object value) throws JSONException {
        if (value == null) {
            this.nameValuePairs.remove(name);
            return this;
        }
        if (value instanceof Number) {
            JSON.checkDouble(((Number) value).doubleValue());
        }
        this.nameValuePairs.put(checkName(name), value);
        return this;
    }

    public JSONObject putOpt(String name, Object value) throws JSONException {
        if (name == null || value == null) {
            return this;
        }
        return put(name, value);
    }

    public JSONObject accumulate(String name, Object value) throws JSONException {
        Object current = this.nameValuePairs.get(checkName(name));
        if (current == null) {
            return put(name, value);
        }
        if (current instanceof JSONArray) {
            ((JSONArray) current).checkedPut(value);
        } else {
            JSONArray array = new JSONArray();
            array.checkedPut(current);
            array.checkedPut(value);
            this.nameValuePairs.put(name, array);
        }
        return this;
    }

    public JSONObject append(String name, Object value) throws JSONException {
        JSONArray array;
        Object current = this.nameValuePairs.get(checkName(name));
        if (current instanceof JSONArray) {
            array = (JSONArray) current;
        } else if (current == null) {
            array = new JSONArray();
            this.nameValuePairs.put(name, array);
        } else {
            throw new JSONException("Key " + name + " is not a JSONArray");
        }
        array.checkedPut(value);
        return this;
    }

    /* access modifiers changed from: package-private */
    public String checkName(String name) throws JSONException {
        if (name != null) {
            return name;
        }
        throw new JSONException("Names must be non-null");
    }

    public Object remove(String name) {
        return this.nameValuePairs.remove(name);
    }

    public boolean isNull(String name) {
        Object value = this.nameValuePairs.get(name);
        return value == null || value == NULL;
    }

    public boolean has(String name) {
        return this.nameValuePairs.containsKey(name);
    }

    public Object get(String name) throws JSONException {
        Object result = this.nameValuePairs.get(name);
        if (result != null) {
            return result;
        }
        throw new JSONException("No value for " + name);
    }

    public Object opt(String name) {
        return this.nameValuePairs.get(name);
    }

    public boolean getBoolean(String name) throws JSONException {
        Object object = get(name);
        Boolean result = JSON.toBoolean(object);
        if (result != null) {
            return result.booleanValue();
        }
        throw JSON.typeMismatch(name, object, "boolean");
    }

    public boolean optBoolean(String name) {
        return optBoolean(name, false);
    }

    public boolean optBoolean(String name, boolean fallback) {
        Boolean result = JSON.toBoolean(opt(name));
        return result != null ? result.booleanValue() : fallback;
    }

    public double getDouble(String name) throws JSONException {
        Object object = get(name);
        Double result = JSON.toDouble(object);
        if (result != null) {
            return result.doubleValue();
        }
        throw JSON.typeMismatch(name, object, "double");
    }

    public double optDouble(String name) {
        return optDouble(name, Double.NaN);
    }

    public double optDouble(String name, double fallback) {
        Double result = JSON.toDouble(opt(name));
        return result != null ? result.doubleValue() : fallback;
    }

    public int getInt(String name) throws JSONException {
        Object object = get(name);
        Integer result = JSON.toInteger(object);
        if (result != null) {
            return result.intValue();
        }
        throw JSON.typeMismatch(name, object, "int");
    }

    public int optInt(String name) {
        return optInt(name, 0);
    }

    public int optInt(String name, int fallback) {
        Integer result = JSON.toInteger(opt(name));
        return result != null ? result.intValue() : fallback;
    }

    public long getLong(String name) throws JSONException {
        Object object = get(name);
        Long result = JSON.toLong(object);
        if (result != null) {
            return result.longValue();
        }
        throw JSON.typeMismatch(name, object, "long");
    }

    public long optLong(String name) {
        return optLong(name, 0);
    }

    public long optLong(String name, long fallback) {
        Long result = JSON.toLong(opt(name));
        return result != null ? result.longValue() : fallback;
    }

    public String getString(String name) throws JSONException {
        Object object = get(name);
        String result = JSON.toString(object);
        if (result != null) {
            return result;
        }
        throw JSON.typeMismatch(name, object, "String");
    }

    public String optString(String name) {
        return optString(name, "");
    }

    public String optString(String name, String fallback) {
        String result = JSON.toString(opt(name));
        return result != null ? result : fallback;
    }

    public JSONArray getJSONArray(String name) throws JSONException {
        Object object = get(name);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw JSON.typeMismatch(name, object, "JSONArray");
    }

    public JSONArray optJSONArray(String name) {
        Object object = opt(name);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        return null;
    }

    public JSONObject getJSONObject(String name) throws JSONException {
        Object object = get(name);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw JSON.typeMismatch(name, object, "JSONObject");
    }

    public JSONObject optJSONObject(String name) {
        Object object = opt(name);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        return null;
    }

    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        JSONArray result = new JSONArray();
        if (names == null) {
            return null;
        }
        int length = names.length();
        if (length == 0) {
            return null;
        }
        for (int i = 0; i < length; i++) {
            result.put(opt(JSON.toString(names.opt(i))));
        }
        return result;
    }

    public Iterator<String> keys() {
        return this.nameValuePairs.keySet().iterator();
    }

    public Set<String> keySet() {
        return this.nameValuePairs.keySet();
    }

    public JSONArray names() {
        if (this.nameValuePairs.isEmpty()) {
            return null;
        }
        return new JSONArray((Collection) new ArrayList(this.nameValuePairs.keySet()));
    }

    public String toString() {
        try {
            JSONStringer stringer = new JSONStringer();
            writeTo(stringer);
            return stringer.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public String toString(int indentSpaces) throws JSONException {
        JSONStringer stringer = new JSONStringer(indentSpaces);
        writeTo(stringer);
        return stringer.toString();
    }

    /* access modifiers changed from: package-private */
    public void writeTo(JSONStringer stringer) throws JSONException {
        stringer.object();
        for (Map.Entry<String, Object> entry : this.nameValuePairs.entrySet()) {
            stringer.key(entry.getKey()).value(entry.getValue());
        }
        stringer.endObject();
    }

    public static String numberToString(Number number) throws JSONException {
        if (number != null) {
            double doubleValue = number.doubleValue();
            JSON.checkDouble(doubleValue);
            if (number.equals(NEGATIVE_ZERO)) {
                return "-0";
            }
            long longValue = number.longValue();
            if (doubleValue == ((double) longValue)) {
                return Long.toString(longValue);
            }
            return number.toString();
        }
        throw new JSONException("Number must be non-null");
    }

    public static String quote(String data) {
        if (data == null) {
            return "\"\"";
        }
        try {
            JSONStringer stringer = new JSONStringer();
            stringer.open(JSONStringer.Scope.NULL, "");
            stringer.value((Object) data);
            stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "");
            return stringer.toString();
        } catch (JSONException e) {
            throw new AssertionError();
        }
    }

    public static Object wrap(Object o) {
        if (o == null) {
            return NULL;
        }
        if ((o instanceof JSONArray) || (o instanceof JSONObject) || o.equals(NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            }
            if (o.getClass().isArray()) {
                return new JSONArray(o);
            }
            if (o instanceof Map) {
                return new JSONObject((Map) o);
            }
            if (!(o instanceof Boolean) && !(o instanceof Byte) && !(o instanceof Character) && !(o instanceof Double) && !(o instanceof Float) && !(o instanceof Integer) && !(o instanceof Long) && !(o instanceof Short)) {
                if (!(o instanceof String)) {
                    if (o.getClass().getPackage().getName().startsWith("java.")) {
                        return o.toString();
                    }
                    return null;
                }
            }
            return o;
        } catch (Exception e) {
        }
    }
}
