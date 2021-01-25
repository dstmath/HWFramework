package ohos.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class BasePacMap implements Sequenceable, Cloneable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218119424, "BasePacMap");
    private static final int MAGIC = 1346454349;
    private ClassLoader classLoader;
    protected HashMap<String, Object> dataMap;

    public BasePacMap(int i) {
        if (i > 0) {
            this.dataMap = new HashMap<>(i);
        } else {
            this.dataMap = new HashMap<>(0);
        }
    }

    public BasePacMap() {
        this(0);
    }

    BasePacMap(boolean z) {
    }

    public void putAll(BasePacMap basePacMap) {
        if (basePacMap != null) {
            this.dataMap.putAll(basePacMap.dataMap);
        }
    }

    /* access modifiers changed from: protected */
    public void putAll(Map<String, Object> map) {
        if (map != null) {
            this.dataMap.putAll(map);
        }
    }

    /* access modifiers changed from: protected */
    public void putObjectValue(String str, Object obj) {
        this.dataMap.put(str, obj);
    }

    public void putByteValue(String str, byte b) {
        this.dataMap.put(str, Byte.valueOf(b));
    }

    public void putShortValue(String str, short s) {
        this.dataMap.put(str, Short.valueOf(s));
    }

    public void putIntValue(String str, int i) {
        this.dataMap.put(str, Integer.valueOf(i));
    }

    public void putLongValue(String str, long j) {
        this.dataMap.put(str, Long.valueOf(j));
    }

    public void putFloatValue(String str, float f) {
        this.dataMap.put(str, Float.valueOf(f));
    }

    public void putDoubleValue(String str, double d) {
        this.dataMap.put(str, Double.valueOf(d));
    }

    public void putBooleanValue(String str, boolean z) {
        this.dataMap.put(str, Boolean.valueOf(z));
    }

    public void putChar(String str, char c) {
        this.dataMap.put(str, Character.valueOf(c));
    }

    public void putString(String str, String str2) {
        this.dataMap.put(str, str2);
    }

    public void putByteValueArray(String str, byte[] bArr) {
        this.dataMap.put(str, bArr);
    }

    public void putShortValueArray(String str, short[] sArr) {
        this.dataMap.put(str, sArr);
    }

    public void putIntValueArray(String str, int[] iArr) {
        this.dataMap.put(str, iArr);
    }

    public void putLongValueArray(String str, long[] jArr) {
        this.dataMap.put(str, jArr);
    }

    public void putFloatValueArray(String str, float[] fArr) {
        this.dataMap.put(str, fArr);
    }

    public void putDoubleValueArray(String str, double[] dArr) {
        this.dataMap.put(str, dArr);
    }

    public void putBooleanValueArray(String str, boolean[] zArr) {
        this.dataMap.put(str, zArr);
    }

    public void putCharArray(String str, char[] cArr) {
        this.dataMap.put(str, cArr);
    }

    public void putStringArray(String str, String[] strArr) {
        this.dataMap.put(str, strArr);
    }

    public void setClassLoader(ClassLoader classLoader2) {
        this.classLoader = classLoader2;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void putPlainArray(String str, PlainArray<? extends Sequenceable> plainArray) {
        this.dataMap.put(str, plainArray);
    }

    public PlainArray<?> getPlainArray(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null) {
            return null;
        }
        try {
            return (PlainArray) obj;
        } catch (ClassCastException unused) {
            HiLog.error(LABEL, "PacMap has type error: the %{private}s key expect the value is %{public}s,which can not be converted to the result type, just return null.", str, obj.getClass().getName());
            return null;
        }
    }

    public void putRemoteObject(String str, Object obj) {
        if (ParcelUtils.isInstanceof(obj.getClass(), "ohos.rpc.IRemoteObject")) {
            this.dataMap.put(str, obj);
            return;
        }
        throw new ParcelException("value type invalid. Should be ohos.rpc.IRemoteObject");
    }

    public <T> T getRemoteObject(String str, Class<T> cls) {
        Object obj = this.dataMap.get(str);
        if (obj == null) {
            return null;
        }
        if (ParcelUtils.isInstanceof(cls, "ohos.rpc.IRemoteObject") && ParcelUtils.isInstanceof(obj.getClass(), "ohos.rpc.IRemoteObject")) {
            return cls.cast(obj);
        }
        throw new ParcelException("The key expect the value is not an IRemoteObject");
    }

    public void putDimension(String str, Dimension dimension) {
        this.dataMap.put(str, dimension);
    }

    public Dimension getDimension(String str) {
        return (Dimension) getObjectValue(str, Dimension.class).orElse(null);
    }

    public void putFloatDimension(String str, FloatDimension floatDimension) {
        this.dataMap.put(str, floatDimension);
    }

    public FloatDimension getFloatDimension(String str) {
        return (FloatDimension) getObjectValue(str, FloatDimension.class).orElse(null);
    }

    /* access modifiers changed from: protected */
    public Map<String, Object> getAll() {
        return this.dataMap;
    }

    public Optional<Object> getObjectValue(String str) {
        return Optional.ofNullable(this.dataMap.get(str));
    }

    /* access modifiers changed from: protected */
    public final <T> Optional<T> getObjectValue(String str, Class<T> cls) {
        Object obj = this.dataMap.get(str);
        if (obj == null || cls == null) {
            return Optional.empty();
        }
        if (cls == obj.getClass()) {
            return Optional.of(obj);
        }
        HiLog.error(LABEL, "PacMap has type error: the %{private}s key expect the value type is %{public}s not %{public}s, just return default value.", str, cls.getName(), obj.getClass().getName());
        return Optional.empty();
    }

    public byte getByteValue(String str, byte b) {
        return ((Byte) getObjectValue(str, Byte.class).orElse(Byte.valueOf(b))).byteValue();
    }

    public byte getByteValue(String str) {
        return getByteValue(str, (byte) 0);
    }

    public short getShortValue(String str, short s) {
        return ((Short) getObjectValue(str, Short.class).orElse(Short.valueOf(s))).shortValue();
    }

    public short getShortValue(String str) {
        return getShortValue(str, 0);
    }

    public int getIntValue(String str, int i) {
        return ((Integer) getObjectValue(str, Integer.class).orElse(Integer.valueOf(i))).intValue();
    }

    public int getIntValue(String str) {
        return getIntValue(str, 0);
    }

    public long getLongValue(String str, long j) {
        return ((Long) getObjectValue(str, Long.class).orElse(Long.valueOf(j))).longValue();
    }

    public long getLongValue(String str) {
        return getLongValue(str, 0);
    }

    public float getFloatValue(String str, float f) {
        return ((Float) getObjectValue(str, Float.class).orElse(Float.valueOf(f))).floatValue();
    }

    public float getFloatValue(String str) {
        return getFloatValue(str, ConstantValue.MIN_ZOOM_VALUE);
    }

    public double getDoubleValue(String str, double d) {
        return ((Double) getObjectValue(str, Double.class).orElse(Double.valueOf(d))).doubleValue();
    }

    public double getDoubleValue(String str) {
        return getDoubleValue(str, 0.0d);
    }

    public boolean getBooleanValue(String str, boolean z) {
        return ((Boolean) getObjectValue(str, Boolean.class).orElse(Boolean.valueOf(z))).booleanValue();
    }

    public boolean getBooleanValue(String str) {
        return getBooleanValue(str, false);
    }

    public char getChar(String str, char c) {
        return ((Character) getObjectValue(str, Character.class).orElse(Character.valueOf(c))).charValue();
    }

    public char getChar(String str) {
        return getChar(str, 0);
    }

    public String getString(String str, String str2) {
        return (String) getObjectValue(str, String.class).orElse(str2);
    }

    public String getString(String str) {
        return getString(str, null);
    }

    public byte[] getByteValueArray(String str) {
        return (byte[]) getObjectValue(str, byte[].class).orElse(null);
    }

    public short[] getShortValueArray(String str) {
        return (short[]) getObjectValue(str, short[].class).orElse(null);
    }

    public int[] getIntValueArray(String str) {
        return (int[]) getObjectValue(str, int[].class).orElse(null);
    }

    public long[] getLongValueArray(String str) {
        return (long[]) getObjectValue(str, long[].class).orElse(null);
    }

    public float[] getFloatValueArray(String str) {
        return (float[]) getObjectValue(str, float[].class).orElse(null);
    }

    public double[] getDoubleValueArray(String str) {
        return (double[]) getObjectValue(str, double[].class).orElse(null);
    }

    public boolean[] getBooleanValueArray(String str) {
        return (boolean[]) getObjectValue(str, boolean[].class).orElse(null);
    }

    public char[] getCharArray(String str) {
        return (char[]) getObjectValue(str, char[].class).orElse(null);
    }

    public String[] getStringArray(String str) {
        return (String[]) getObjectValue(str, String[].class).orElse(null);
    }

    public int getSize() {
        return this.dataMap.size();
    }

    public boolean isEmpty() {
        return this.dataMap.isEmpty();
    }

    public Set<String> getKeys() {
        return this.dataMap.keySet();
    }

    public boolean hasKey(String str) {
        return this.dataMap.containsKey(str);
    }

    public void remove(String str) {
        this.dataMap.remove(str);
    }

    public void clear() {
        this.dataMap.clear();
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        try {
            parcel.writeInt(MAGIC);
            parcel.writeInt(this.dataMap.size());
            for (Map.Entry<String, Object> entry : this.dataMap.entrySet()) {
                parcel.writeString(entry.getKey());
                ParcelUtils.writeValueIntoParcel(entry.getValue(), parcel);
            }
            return true;
        } catch (ParcelException e) {
            HiLog.warn(LABEL, "fail to marshall pacmap: %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        int readInt;
        try {
            if (parcel.readInt() != MAGIC || (readInt = parcel.readInt()) < 0) {
                return false;
            }
            for (int i = 0; i < readInt; i++) {
                this.dataMap.put(parcel.readString(), ParcelUtils.readValueFromParcel(parcel));
            }
            return true;
        } catch (ParcelException e) {
            HiLog.warn(LABEL, "fail to unmarshalling pacmap: %{public}s", e.getMessage());
            return false;
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BasePacMap)) {
            return false;
        }
        return this.dataMap.equals(((BasePacMap) obj).dataMap);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.dataMap);
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        BasePacMap basePacMap = (BasePacMap) super.clone();
        basePacMap.dataMap = this.dataMap;
        return basePacMap;
    }

    /* access modifiers changed from: package-private */
    public void copyFrom(BasePacMap basePacMap, boolean z) {
        this.classLoader = basePacMap.classLoader;
        HashMap<String, Object> hashMap = basePacMap.dataMap;
        if (hashMap == null) {
            this.dataMap = null;
        } else if (!z) {
            this.dataMap = new HashMap<>(hashMap);
        } else {
            this.dataMap = new HashMap<>(hashMap.size());
            for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                this.dataMap.put(entry.getKey(), deepCopyValue(entry.getValue()));
            }
        }
    }

    private Object deepCopyValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof PacMap) {
            return ((PacMap) obj).deepCopy();
        }
        if (obj instanceof PersistablePacMap) {
            return ((PersistablePacMap) obj).deepCopy();
        }
        if (obj instanceof List) {
            return deepCopyArrayList((List) obj);
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof int[]) {
                return ((int[]) obj).clone();
            }
            if (obj instanceof long[]) {
                return ((long[]) obj).clone();
            }
            if (obj instanceof float[]) {
                return ((float[]) obj).clone();
            }
            if (obj instanceof double[]) {
                return ((double[]) obj).clone();
            }
            if (obj instanceof Object[]) {
                return ((Object[]) obj).clone();
            }
            if (obj instanceof byte[]) {
                return ((byte[]) obj).clone();
            }
            if (obj instanceof short[]) {
                return ((short[]) obj).clone();
            }
            if (obj instanceof char[]) {
                return ((char[]) obj).clone();
            }
        }
        return obj;
    }

    private ArrayList deepCopyArrayList(List list) {
        int size = list.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(deepCopyValue(list.get(i)));
        }
        return arrayList;
    }

    @Override // ohos.utils.Sequenceable
    public boolean hasFileDescriptor() {
        return super.hasFileDescriptor();
    }
}
