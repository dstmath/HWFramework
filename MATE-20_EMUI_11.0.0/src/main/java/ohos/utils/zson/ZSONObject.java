package ohos.utils.zson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.parser.Feature;
import ohos.utils.fastjson.serializer.SerializeConfig;
import ohos.utils.fastjson.serializer.SerializerFeature;

public class ZSONObject extends JSONObject {
    public ZSONObject() {
    }

    public ZSONObject(Map<String, Object> map) {
        super(map);
    }

    public static final <T> T stringToClass(String str, Class<T> cls) {
        return (T) parseObject(str, (Class<Object>) cls, new Feature[0]);
    }

    public static final ZSONObject stringToZSON(String str) {
        JSONObject parseObject = parseObject(str);
        if (parseObject == null) {
            return null;
        }
        return new ZSONObject(parseObject.getInnerMap());
    }

    public static final String toZSONString(Object obj) {
        return toJSONString(obj, SerializeConfig.globalInstance, null, null, JSON.DEFAULT_GENERATE_FEATURE, new SerializerFeature[0]);
    }

    public static final ZSONObject classToZSON(Object obj) {
        return stringToZSON(toZSONString(obj));
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public int size() {
        return super.size();
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public boolean containsKey(Object obj) {
        return super.containsKey(obj);
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public boolean containsValue(Object obj) {
        return super.containsValue(obj);
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public Object get(Object obj) {
        return super.get(obj);
    }

    public ZSONObject getZSONObject(String str) {
        JSONObject jSONObject = super.getJSONObject(str);
        if (jSONObject == null) {
            return null;
        }
        return new ZSONObject(jSONObject.getInnerMap());
    }

    public ZSONArray getZSONArray(String str) {
        JSONArray jSONArray = super.getJSONArray(str);
        if (jSONArray == null) {
            return null;
        }
        ZSONArray zSONArray = new ZSONArray(jSONArray.size());
        Iterator<Object> it = jSONArray.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof JSONObject) {
                zSONArray.add(classToZSON(next));
            } else {
                zSONArray.add(next);
            }
        }
        return zSONArray;
    }

    @Override // ohos.utils.fastjson.JSONObject
    public <T> T getObject(String str, Class<T> cls) {
        return (T) super.getObject(str, cls);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Boolean getBoolean(String str) {
        return super.getBoolean(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public boolean getBooleanValue(String str) {
        return super.getBooleanValue(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public byte[] getBytes(String str) {
        return super.getBytes(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Byte getByte(String str) {
        return super.getByte(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public byte getByteValue(String str) {
        return super.getByteValue(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Short getShort(String str) {
        return super.getShort(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public short getShortValue(String str) {
        return super.getShortValue(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Integer getInteger(String str) {
        return super.getInteger(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public int getIntValue(String str) {
        return super.getIntValue(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Long getLong(String str) {
        return super.getLong(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public long getLongValue(String str) {
        return super.getLongValue(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Float getFloat(String str) {
        return super.getFloat(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public float getFloatValue(String str) {
        return super.getFloatValue(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Double getDouble(String str) {
        return super.getDouble(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public double getDoubleValue(String str) {
        return super.getDoubleValue(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public BigDecimal getBigDecimal(String str) {
        return super.getBigDecimal(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public BigInteger getBigInteger(String str) {
        return super.getBigInteger(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public String getString(String str) {
        return super.getString(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Date getDate(String str) {
        return super.getDate(str);
    }

    @Override // ohos.utils.fastjson.JSONObject
    public Object put(String str, Object obj) {
        return super.put(str, obj);
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public void putAll(Map<? extends String, ? extends Object> map) {
        super.putAll(map);
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public void clear() {
        super.clear();
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public Object remove(Object obj) {
        return super.remove(obj);
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public Set<String> keySet() {
        return super.keySet();
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public Collection<Object> values() {
        return super.values();
    }

    @Override // ohos.utils.fastjson.JSONObject, java.util.Map
    public Set<Map.Entry<String, Object>> entrySet() {
        return super.entrySet();
    }
}
