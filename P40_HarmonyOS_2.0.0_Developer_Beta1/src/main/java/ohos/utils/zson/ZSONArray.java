package ohos.utils.zson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.zson.ZSONTools;

public class ZSONArray extends JSONArray {
    public ZSONArray() {
    }

    public ZSONArray(List<Object> list) {
        super(list);
    }

    public ZSONArray(int i) {
        super(i);
    }

    public static final ZSONArray stringToZSONArray(String str) {
        JSONArray jSONArray = (JSONArray) ZSONTools.callFastJson(new ZSONTools.Caller(str) {
            /* class ohos.utils.zson.$$Lambda$ZSONArray$KLNaEEDzav_YtrtG_w8q_HLhmU */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return JSONArray.parseArray(this.f$0);
            }
        });
        if (jSONArray == null) {
            return null;
        }
        return new ZSONArray(new ArrayList(jSONArray.subList(0, jSONArray.size())));
    }

    public static final <T> List<T> stringToClassList(String str, Class<T> cls) {
        return (List) ZSONTools.callFastJson(new ZSONTools.Caller(str, cls) {
            /* class ohos.utils.zson.$$Lambda$ZSONArray$BK3C8rGSIVLa5NJESfM5fIfYVqo */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ Class f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONArray.parseArray(this.f$0, this.f$1);
            }
        });
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public int size() {
        return super.size();
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean contains(Object obj) {
        return super.contains(obj);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection, java.lang.Iterable
    public Iterator<Object> iterator() {
        return super.iterator();
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public Object[] toArray() {
        return super.toArray();
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public <T> T[] toArray(T[] tArr) {
        return (T[]) super.toArray(tArr);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean add(Object obj) {
        return super.add(obj);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean remove(Object obj) {
        return super.remove(obj);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean containsAll(Collection<?> collection) {
        return super.containsAll(collection);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean addAll(Collection<? extends Object> collection) {
        return super.addAll(collection);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public boolean addAll(int i, Collection<? extends Object> collection) {
        return super.addAll(i, collection);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean removeAll(Collection<?> collection) {
        return super.removeAll(collection);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public boolean retainAll(Collection<?> collection) {
        return super.retainAll(collection);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List, java.util.Collection
    public void clear() {
        super.clear();
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public Object set(int i, Object obj) {
        return super.set(i, obj);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public void add(int i, Object obj) {
        super.add(i, obj);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public Object remove(int i) {
        return super.remove(i);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public int indexOf(Object obj) {
        return super.indexOf(obj);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public int lastIndexOf(Object obj) {
        return super.lastIndexOf(obj);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public ListIterator<Object> listIterator() {
        return super.listIterator();
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public ListIterator<Object> listIterator(int i) {
        return super.listIterator(i);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public List<Object> subList(int i, int i2) {
        return super.subList(i, i2);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.util.List
    public Object get(int i) {
        return super.get(i);
    }

    public ZSONObject getZSONObject(int i) {
        JSONObject jSONObject = (JSONObject) ZSONTools.callFastJson(new ZSONTools.Caller(i) {
            /* class ohos.utils.zson.$$Lambda$ZSONArray$8alQwiPtZ5oWlNdoRnLUxfELBpw */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONArray.this.lambda$getZSONObject$2$ZSONArray(this.f$1);
            }
        });
        if (jSONObject == null) {
            return null;
        }
        return new ZSONObject(jSONObject.getInnerMap());
    }

    public /* synthetic */ JSONObject lambda$getZSONObject$2$ZSONArray(int i) {
        return super.getJSONObject(i);
    }

    public ZSONArray getZSONArray(int i) {
        JSONArray jSONArray = (JSONArray) ZSONTools.callFastJson(new ZSONTools.Caller(i) {
            /* class ohos.utils.zson.$$Lambda$ZSONArray$XzyFhOlxzreNjxXuvIbbQUl_LS0 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONArray.this.lambda$getZSONArray$3$ZSONArray(this.f$1);
            }
        });
        if (jSONArray == null) {
            return null;
        }
        return new ZSONArray(jSONArray.subList(0, jSONArray.size()));
    }

    public /* synthetic */ JSONArray lambda$getZSONArray$3$ZSONArray(int i) {
        return super.getJSONArray(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public <T> T getObject(int i, Class<T> cls) {
        return (T) super.getObject(i, cls);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Boolean getBoolean(int i) {
        return super.getBoolean(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public boolean getBooleanValue(int i) {
        return super.getBooleanValue(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Byte getByte(int i) {
        return super.getByte(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public byte getByteValue(int i) {
        return super.getByteValue(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Short getShort(int i) {
        return super.getShort(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public short getShortValue(int i) {
        return super.getShortValue(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Integer getInteger(int i) {
        return super.getInteger(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public int getIntValue(int i) {
        return super.getIntValue(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Long getLong(int i) {
        return super.getLong(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public long getLongValue(int i) {
        return super.getLongValue(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Float getFloat(int i) {
        return super.getFloat(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public float getFloatValue(int i) {
        return super.getFloatValue(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Double getDouble(int i) {
        return super.getDouble(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public double getDoubleValue(int i) {
        return super.getDoubleValue(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public BigDecimal getBigDecimal(int i) {
        return super.getBigDecimal(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public BigInteger getBigInteger(int i) {
        return super.getBigInteger(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public String getString(int i) {
        return super.getString(i);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public Date getDate(int i) {
        return super.getDate(i);
    }

    @Override // ohos.utils.fastjson.JSONArray, java.lang.Object
    public Object clone() {
        return new ZSONArray(new ArrayList(subList(0, size())));
    }

    public /* synthetic */ List lambda$toJavaList$4$ZSONArray(Class cls) {
        return super.toJavaList(cls);
    }

    @Override // ohos.utils.fastjson.JSONArray
    public <T> List<T> toJavaList(Class<T> cls) {
        return (List) ZSONTools.callFastJson(new ZSONTools.Caller(cls) {
            /* class ohos.utils.zson.$$Lambda$ZSONArray$0_PKGYAdUGa29CxYm_FlEblYSuk */
            private final /* synthetic */ Class f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.utils.zson.ZSONTools.Caller
            public final Object call() {
                return ZSONArray.this.lambda$toJavaList$4$ZSONArray(this.f$1);
            }
        });
    }

    public ZSONObject toZSONObject(String[] strArr) {
        Objects.requireNonNull(strArr, "keys is null.");
        int min = Math.min(size(), strArr.length);
        ZSONObject zSONObject = new ZSONObject();
        for (int i = 0; i < min; i++) {
            zSONObject.put(strArr[i], get(i));
        }
        return zSONObject;
    }

    @Override // ohos.utils.fastjson.JSON, java.lang.Object
    public String toString() {
        return toJSONString();
    }
}
