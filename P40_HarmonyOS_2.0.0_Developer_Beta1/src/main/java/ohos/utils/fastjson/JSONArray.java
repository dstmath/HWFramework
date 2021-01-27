package ohos.utils.fastjson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import ohos.utils.fastjson.parser.ParserConfig;
import ohos.utils.fastjson.util.TypeUtils;

public class JSONArray extends JSON implements List<Object>, Cloneable, RandomAccess, Serializable {
    protected transient Type componentType;
    private final List<Object> list;
    protected transient Object relatedArray;

    public JSONArray() {
        this.list = new ArrayList(10);
    }

    public JSONArray(List<Object> list2) {
        this.list = list2;
    }

    public JSONArray(int i) {
        this.list = new ArrayList(i);
    }

    public Object getRelatedArray() {
        return this.relatedArray;
    }

    public void setRelatedArray(Object obj) {
        this.relatedArray = obj;
    }

    public Type getComponentType() {
        return this.componentType;
    }

    public void setComponentType(Type type) {
        this.componentType = type;
    }

    @Override // java.util.List, java.util.Collection
    public int size() {
        return this.list.size();
    }

    @Override // java.util.List, java.util.Collection
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override // java.util.List, java.util.Collection
    public boolean contains(Object obj) {
        return this.list.contains(obj);
    }

    @Override // java.util.List, java.util.Collection, java.lang.Iterable
    public Iterator<Object> iterator() {
        return this.list.iterator();
    }

    @Override // java.util.List, java.util.Collection
    public Object[] toArray() {
        return this.list.toArray();
    }

    @Override // java.util.List, java.util.Collection
    public <T> T[] toArray(T[] tArr) {
        return (T[]) this.list.toArray(tArr);
    }

    @Override // java.util.List, java.util.Collection
    public boolean add(Object obj) {
        return this.list.add(obj);
    }

    @Override // java.util.List, java.util.Collection
    public boolean remove(Object obj) {
        return this.list.remove(obj);
    }

    @Override // java.util.List, java.util.Collection
    public boolean containsAll(Collection<?> collection) {
        return this.list.containsAll(collection);
    }

    @Override // java.util.List, java.util.Collection
    public boolean addAll(Collection<? extends Object> collection) {
        return this.list.addAll(collection);
    }

    @Override // java.util.List
    public boolean addAll(int i, Collection<? extends Object> collection) {
        return this.list.addAll(i, collection);
    }

    @Override // java.util.List, java.util.Collection
    public boolean removeAll(Collection<?> collection) {
        return this.list.removeAll(collection);
    }

    @Override // java.util.List, java.util.Collection
    public boolean retainAll(Collection<?> collection) {
        return this.list.retainAll(collection);
    }

    @Override // java.util.List, java.util.Collection
    public void clear() {
        this.list.clear();
    }

    @Override // java.util.List
    public Object set(int i, Object obj) {
        return this.list.set(i, obj);
    }

    @Override // java.util.List
    public void add(int i, Object obj) {
        this.list.add(i, obj);
    }

    @Override // java.util.List
    public Object remove(int i) {
        return this.list.remove(i);
    }

    @Override // java.util.List
    public int indexOf(Object obj) {
        return this.list.indexOf(obj);
    }

    @Override // java.util.List
    public int lastIndexOf(Object obj) {
        return this.list.lastIndexOf(obj);
    }

    @Override // java.util.List
    public ListIterator<Object> listIterator() {
        return this.list.listIterator();
    }

    @Override // java.util.List
    public ListIterator<Object> listIterator(int i) {
        return this.list.listIterator(i);
    }

    @Override // java.util.List
    public List<Object> subList(int i, int i2) {
        return this.list.subList(i, i2);
    }

    @Override // java.util.List
    public Object get(int i) {
        return this.list.get(i);
    }

    public JSONObject getJSONObject(int i) {
        Object obj = this.list.get(i);
        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }
        return (JSONObject) toJSON(obj);
    }

    public JSONArray getJSONArray(int i) {
        Object obj = this.list.get(i);
        if (obj instanceof JSONArray) {
            return (JSONArray) obj;
        }
        return (JSONArray) toJSON(obj);
    }

    public <T> T getObject(int i, Class<T> cls) {
        return (T) TypeUtils.castToJavaBean(this.list.get(i), cls);
    }

    public Boolean getBoolean(int i) {
        Object obj = get(i);
        if (obj == null) {
            return null;
        }
        return TypeUtils.castToBoolean(obj);
    }

    public boolean getBooleanValue(int i) {
        Object obj = get(i);
        if (obj == null) {
            return false;
        }
        return TypeUtils.castToBoolean(obj).booleanValue();
    }

    public Byte getByte(int i) {
        return TypeUtils.castToByte(get(i));
    }

    public byte getByteValue(int i) {
        Object obj = get(i);
        if (obj == null) {
            return 0;
        }
        return TypeUtils.castToByte(obj).byteValue();
    }

    public Short getShort(int i) {
        return TypeUtils.castToShort(get(i));
    }

    public short getShortValue(int i) {
        Object obj = get(i);
        if (obj == null) {
            return 0;
        }
        return TypeUtils.castToShort(obj).shortValue();
    }

    public Integer getInteger(int i) {
        return TypeUtils.castToInt(get(i));
    }

    public int getIntValue(int i) {
        Object obj = get(i);
        if (obj == null) {
            return 0;
        }
        return TypeUtils.castToInt(obj).intValue();
    }

    public Long getLong(int i) {
        return TypeUtils.castToLong(get(i));
    }

    public long getLongValue(int i) {
        Object obj = get(i);
        if (obj == null) {
            return 0;
        }
        return TypeUtils.castToLong(obj).longValue();
    }

    public Float getFloat(int i) {
        return TypeUtils.castToFloat(get(i));
    }

    public float getFloatValue(int i) {
        Object obj = get(i);
        if (obj == null) {
            return 0.0f;
        }
        return TypeUtils.castToFloat(obj).floatValue();
    }

    public Double getDouble(int i) {
        return TypeUtils.castToDouble(get(i));
    }

    public double getDoubleValue(int i) {
        Object obj = get(i);
        if (obj == null) {
            return 0.0d;
        }
        return TypeUtils.castToDouble(obj).doubleValue();
    }

    public BigDecimal getBigDecimal(int i) {
        return TypeUtils.castToBigDecimal(get(i));
    }

    public BigInteger getBigInteger(int i) {
        return TypeUtils.castToBigInteger(get(i));
    }

    public String getString(int i) {
        return TypeUtils.castToString(get(i));
    }

    public Date getDate(int i) {
        return TypeUtils.castToDate(get(i));
    }

    @Override // java.lang.Object
    public Object clone() {
        return new JSONArray(new ArrayList(this.list));
    }

    @Override // java.util.List, java.util.Collection, java.lang.Object
    public boolean equals(Object obj) {
        return this.list.equals(obj);
    }

    @Override // java.util.List, java.util.Collection, java.lang.Object
    public int hashCode() {
        return this.list.hashCode();
    }

    public <T> List<T> toJavaList(Class<T> cls) {
        ArrayList arrayList = new ArrayList(size());
        ParserConfig globalInstance = ParserConfig.getGlobalInstance();
        Iterator<Object> it = iterator();
        while (it.hasNext()) {
            arrayList.add(TypeUtils.cast(it.next(), (Class<Object>) cls, globalInstance));
        }
        return arrayList;
    }
}
