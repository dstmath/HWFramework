package ohos.utils.fastjson;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.utils.fastjson.annotation.JSONField;
import ohos.utils.fastjson.parser.Feature;
import ohos.utils.fastjson.parser.ParserConfig;
import ohos.utils.fastjson.util.TypeUtils;

public class JSONObject extends JSON implements Map<String, Object>, Cloneable, Serializable, InvocationHandler {
    private final Map<String, Object> map;

    public JSONObject() {
        this(16, false);
    }

    public JSONObject(Map<String, Object> map2) {
        this.map = map2;
    }

    public JSONObject(boolean z) {
        this(16, z);
    }

    public JSONObject(int i) {
        this(i, false);
    }

    public JSONObject(int i, boolean z) {
        if (z) {
            this.map = new LinkedHashMap(i);
        } else {
            this.map = new HashMap(i);
        }
    }

    @Override // java.util.Map
    public int size() {
        return this.map.size();
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override // java.util.Map
    public boolean containsKey(Object obj) {
        return this.map.containsKey(obj);
    }

    @Override // java.util.Map
    public boolean containsValue(Object obj) {
        return this.map.containsValue(obj);
    }

    @Override // java.util.Map
    public Object get(Object obj) {
        return this.map.get(obj);
    }

    public JSONObject getJSONObject(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }
        if (obj instanceof String) {
            return JSON.parseObject((String) obj);
        }
        return (JSONObject) toJSON(obj);
    }

    public JSONArray getJSONArray(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof JSONArray) {
            return (JSONArray) obj;
        }
        if (obj instanceof String) {
            return (JSONArray) JSON.parse((String) obj);
        }
        return (JSONArray) toJSON(obj);
    }

    public <T> T getObject(String str, Class<T> cls) {
        return (T) TypeUtils.castToJavaBean(this.map.get(str), cls);
    }

    public <T> T getObject(String str, Class<T> cls, Feature... featureArr) {
        Object obj = this.map.get(str);
        int i = JSON.DEFAULT_PARSER_FEATURE;
        for (Feature feature : featureArr) {
            i |= feature.mask;
        }
        return (T) TypeUtils.cast(obj, cls, ParserConfig.global, i);
    }

    public Boolean getBoolean(String str) {
        Object obj = get(str);
        if (obj == null) {
            return null;
        }
        return TypeUtils.castToBoolean(obj);
    }

    public byte[] getBytes(String str) {
        Object obj = get(str);
        if (obj == null) {
            return null;
        }
        return TypeUtils.castToBytes(obj);
    }

    public boolean getBooleanValue(String str) {
        Boolean castToBoolean = TypeUtils.castToBoolean(get(str));
        if (castToBoolean == null) {
            return false;
        }
        return castToBoolean.booleanValue();
    }

    public Byte getByte(String str) {
        return TypeUtils.castToByte(get(str));
    }

    public byte getByteValue(String str) {
        Byte castToByte = TypeUtils.castToByte(get(str));
        if (castToByte == null) {
            return 0;
        }
        return castToByte.byteValue();
    }

    public Short getShort(String str) {
        return TypeUtils.castToShort(get(str));
    }

    public short getShortValue(String str) {
        Short castToShort = TypeUtils.castToShort(get(str));
        if (castToShort == null) {
            return 0;
        }
        return castToShort.shortValue();
    }

    public Integer getInteger(String str) {
        return TypeUtils.castToInt(get(str));
    }

    public int getIntValue(String str) {
        Integer castToInt = TypeUtils.castToInt(get(str));
        if (castToInt == null) {
            return 0;
        }
        return castToInt.intValue();
    }

    public Long getLong(String str) {
        return TypeUtils.castToLong(get(str));
    }

    public long getLongValue(String str) {
        Long castToLong = TypeUtils.castToLong(get(str));
        if (castToLong == null) {
            return 0;
        }
        return castToLong.longValue();
    }

    public Float getFloat(String str) {
        return TypeUtils.castToFloat(get(str));
    }

    public float getFloatValue(String str) {
        Float castToFloat = TypeUtils.castToFloat(get(str));
        if (castToFloat == null) {
            return ConstantValue.MIN_ZOOM_VALUE;
        }
        return castToFloat.floatValue();
    }

    public Double getDouble(String str) {
        return TypeUtils.castToDouble(get(str));
    }

    public double getDoubleValue(String str) {
        Double castToDouble = TypeUtils.castToDouble(get(str));
        if (castToDouble == null) {
            return 0.0d;
        }
        return castToDouble.doubleValue();
    }

    public BigDecimal getBigDecimal(String str) {
        return TypeUtils.castToBigDecimal(get(str));
    }

    public BigInteger getBigInteger(String str) {
        return TypeUtils.castToBigInteger(get(str));
    }

    public String getString(String str) {
        Object obj = get(str);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public Date getDate(String str) {
        return TypeUtils.castToDate(get(str));
    }

    public Object put(String str, Object obj) {
        return this.map.put(str, obj);
    }

    @Override // java.util.Map
    public void putAll(Map<? extends String, ? extends Object> map2) {
        this.map.putAll(map2);
    }

    @Override // java.util.Map
    public void clear() {
        this.map.clear();
    }

    @Override // java.util.Map
    public Object remove(Object obj) {
        return this.map.remove(obj);
    }

    @Override // java.util.Map
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override // java.util.Map
    public Collection<Object> values() {
        return this.map.values();
    }

    @Override // java.util.Map
    public Set<Map.Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    @Override // java.lang.Object
    public Object clone() {
        return new JSONObject(new LinkedHashMap(this.map));
    }

    @Override // java.util.Map, java.lang.Object
    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    @Override // java.util.Map, java.lang.Object
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        String str = null;
        if (parameterTypes.length == 1) {
            if (method.getName().equals("equals")) {
                return Boolean.valueOf(equals(objArr[0]));
            }
            if (method.getReturnType() == Void.TYPE) {
                JSONField jSONField = (JSONField) method.getAnnotation(JSONField.class);
                String name = (jSONField == null || jSONField.name().length() == 0) ? null : jSONField.name();
                if (name == null) {
                    String name2 = method.getName();
                    if (name2.startsWith("set")) {
                        String substring = name2.substring(3);
                        if (substring.length() != 0) {
                            name = Character.toLowerCase(substring.charAt(0)) + substring.substring(1);
                        } else {
                            throw new JSONException("illegal setter");
                        }
                    } else {
                        throw new JSONException("illegal setter");
                    }
                }
                this.map.put(name, objArr[0]);
                return null;
            }
            throw new JSONException("illegal setter");
        } else if (parameterTypes.length != 0) {
            throw new UnsupportedOperationException(method.toGenericString());
        } else if (method.getReturnType() != Void.TYPE) {
            JSONField jSONField2 = (JSONField) method.getAnnotation(JSONField.class);
            if (!(jSONField2 == null || jSONField2.name().length() == 0)) {
                str = jSONField2.name();
            }
            if (str == null) {
                String name3 = method.getName();
                if (name3.startsWith("get")) {
                    String substring2 = name3.substring(3);
                    if (substring2.length() != 0) {
                        str = Character.toLowerCase(substring2.charAt(0)) + substring2.substring(1);
                    } else {
                        throw new JSONException("illegal getter");
                    }
                } else if (name3.startsWith("is")) {
                    String substring3 = name3.substring(2);
                    if (substring3.length() != 0) {
                        str = Character.toLowerCase(substring3.charAt(0)) + substring3.substring(1);
                    } else {
                        throw new JSONException("illegal getter");
                    }
                } else if (name3.startsWith("hashCode")) {
                    return Integer.valueOf(hashCode());
                } else {
                    if (name3.startsWith("toString")) {
                        return toString();
                    }
                    throw new JSONException("illegal getter");
                }
            }
            return TypeUtils.cast(this.map.get(str), method.getGenericReturnType(), ParserConfig.global);
        } else {
            throw new JSONException("illegal getter");
        }
    }

    public Map<String, Object> getInnerMap() {
        return this.map;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: ohos.utils.fastjson.JSONObject */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // ohos.utils.fastjson.JSON
    public <T> T toJavaObject(Class<T> cls) {
        if (cls == Map.class) {
            return this;
        }
        return (cls != Object.class || containsKey(JSON.DEFAULT_TYPE_KEY)) ? (T) TypeUtils.castToJavaBean(this, cls, ParserConfig.getGlobalInstance(), 0) : this;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: ohos.utils.fastjson.JSONObject */
    /* JADX WARN: Multi-variable type inference failed */
    public <T> T toJavaObject(Class<T> cls, ParserConfig parserConfig, int i) {
        if (cls == Map.class) {
            return this;
        }
        return (cls != Object.class || containsKey(JSON.DEFAULT_TYPE_KEY)) ? (T) TypeUtils.castToJavaBean(this, cls, parserConfig, i) : this;
    }
}
