package ohos.utils.zson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.parser.Feature;
import ohos.utils.fastjson.parser.deserializer.ParseProcess;
import ohos.utils.fastjson.serializer.SerializeConfig;
import ohos.utils.fastjson.serializer.SerializeWriter;
import ohos.utils.fastjson.serializer.SerializerFeature;
import ohos.utils.zson.ZSONTools;
import ohos.utils.zson.annotation.ZSONFieldFilter;
import ohos.utils.zson.annotation.ZSONFieldProcessor;

public class ZSONObject extends JSONObject {
    public ZSONObject() {
    }

    public ZSONObject(Map<String, Object> map) {
        super(map);
    }

    public static final <T> T stringToClass(String str, Class<T> cls) {
        return (T) ZSONTools.callFastJson(new ZSONTools.Caller(str, cls) {
            /* class ohos.utils.zson.$$Lambda$ZSONObject$Rywg32cgEaZgTX7d28jE9JNjcKU */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ Class f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                Class cls;
                return ZSONObject.parseObject(this.f$0, (Class) ((Class<Object>) cls), (ParseProcess) new ZSONFieldProcessor(this.f$1), new Feature[0]);
            }
        });
    }

    public static final ZSONObject stringToZSON(String str) {
        JSONObject jSONObject = (JSONObject) ZSONTools.callFastJson(new ZSONTools.Caller(str) {
            /* class ohos.utils.zson.$$Lambda$ZSONObject$IvhKFavwtE36DytYWa09efGjpP0 */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONObject.parseObject(this.f$0);
            }
        });
        if (jSONObject == null) {
            return null;
        }
        return new ZSONObject(jSONObject.getInnerMap());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0030, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0031, code lost:
        r4.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0034, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002b, code lost:
        r1 = move-exception;
     */
    public static final String toZSONString(Object obj) {
        try {
            SerializeWriter serializeWriter = new SerializeWriter(null, JSON.DEFAULT_GENERATE_FEATURE, new SerializerFeature[0]);
            ZSONSerializer zSONSerializer = new ZSONSerializer(serializeWriter, SerializeConfig.globalInstance);
            zSONSerializer.getNameFilters().add(new ZSONFieldFilter());
            zSONSerializer.write(obj);
            String serializeWriter2 = serializeWriter.toString();
            serializeWriter.close();
            return serializeWriter2;
        } catch (JSONException e) {
            throw new ZSONException(e);
        }
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
        JSONObject jSONObject = (JSONObject) ZSONTools.callFastJson(new ZSONTools.Caller(str) {
            /* class ohos.utils.zson.$$Lambda$ZSONObject$opzpj0UHMYCn1YKQ_xsKwO_T7Zk */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONObject.this.lambda$getZSONObject$2$ZSONObject(this.f$1);
            }
        });
        if (jSONObject == null) {
            return null;
        }
        return new ZSONObject(jSONObject.getInnerMap());
    }

    public /* synthetic */ JSONObject lambda$getZSONObject$2$ZSONObject(String str) {
        return super.getJSONObject(str);
    }

    public ZSONArray getZSONArray(String str) {
        JSONArray jSONArray = (JSONArray) ZSONTools.callFastJson(new ZSONTools.Caller(str) {
            /* class ohos.utils.zson.$$Lambda$ZSONObject$BfNCeiNhj2510mSN7ma2HhCKCyw */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONObject.this.lambda$getZSONArray$3$ZSONObject(this.f$1);
            }
        });
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

    public /* synthetic */ JSONArray lambda$getZSONArray$3$ZSONObject(String str) {
        return super.getJSONArray(str);
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

    public ZSONArray toZSONArray(String[] strArr) {
        Objects.requireNonNull(strArr, "names is null.");
        ZSONArray zSONArray = new ZSONArray();
        for (String str : strArr) {
            zSONArray.add(get(str));
        }
        return zSONArray;
    }

    @Override // ohos.utils.fastjson.JSON, java.lang.Object
    public String toString() {
        return toZSONString(this);
    }
}
