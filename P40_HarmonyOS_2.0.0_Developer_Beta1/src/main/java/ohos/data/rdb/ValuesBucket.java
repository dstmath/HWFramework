package ohos.data.rdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ValuesBucket implements Sequenceable {
    private static final int INVALID_OBJECT_FLAG = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109441, "ValuesBucket");
    private static final int VALID_OBJECT_FLAG = 1;
    private static final int VALUE_OF_BOOLEAN = 9;
    private static final int VALUE_OF_BYTE = 3;
    private static final int VALUE_OF_BYTE_ARRAY = 10;
    private static final int VALUE_OF_DOUBLE = 8;
    private static final int VALUE_OF_FLOAT = 7;
    private static final int VALUE_OF_INTEGER = 5;
    private static final int VALUE_OF_LONG = 6;
    private static final int VALUE_OF_SHORT = 4;
    private static final int VALUE_OF_STRING = 2;
    private HashMap<String, Object> map;

    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    public ValuesBucket() {
        this.map = new HashMap<>();
    }

    public ValuesBucket(int i) {
        this.map = new HashMap<>(i, 1.0f);
    }

    public ValuesBucket(ValuesBucket valuesBucket) {
        if (valuesBucket != null) {
            this.map = new HashMap<>(valuesBucket.map);
            return;
        }
        throw new NullPointerException();
    }

    public ValuesBucket(Parcel parcel) {
        this.map = parcel.readInt() != 0 ? readHashMap(parcel) : null;
    }

    public void putString(String str, String str2) {
        this.map.put(str, str2);
    }

    public void putValues(ValuesBucket valuesBucket) {
        this.map.putAll(valuesBucket.map);
    }

    public void putByte(String str, Byte b) {
        this.map.put(str, b);
    }

    public void putShort(String str, Short sh) {
        this.map.put(str, sh);
    }

    public void putInteger(String str, Integer num) {
        this.map.put(str, num);
    }

    public void putLong(String str, Long l) {
        this.map.put(str, l);
    }

    public void putFloat(String str, Float f) {
        this.map.put(str, f);
    }

    public void putDouble(String str, Double d) {
        this.map.put(str, d);
    }

    public void putBoolean(String str, Boolean bool) {
        this.map.put(str, bool);
    }

    public void putByteArray(String str, byte[] bArr) {
        this.map.put(str, bArr);
    }

    public void putNull(String str) {
        this.map.put(str, null);
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public void delete(String str) {
        this.map.remove(str);
    }

    public void clear() {
        this.map.clear();
    }

    public boolean hasColumn(String str) {
        return this.map.containsKey(str);
    }

    public Object getObject(String str) {
        return this.map.get(str);
    }

    public String getString(String str) {
        Object obj = this.map.get(str);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    public Long getLong(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof Number) {
            return Long.valueOf(((Number) obj).longValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Long.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.info(LABEL, "Cannot parse Long value for %{private}s at column name %{private}s.", new Object[]{obj, str});
                return null;
            }
        } else {
            HiLog.info(LABEL, "Cannot cast value for %{private}s to a Long value: %{private}s.", new Object[]{str, obj});
            return null;
        }
    }

    public Integer getInteger(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof Number) {
            return Integer.valueOf(((Number) obj).intValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Integer.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.info(LABEL, "Cannot parse Integer value for %{private}s at column name %{private}s.", new Object[]{obj, str});
                return null;
            }
        } else {
            HiLog.info(LABEL, "Cannot cast value for %{private}s to a Integer value: %{private}s.", new Object[]{str, obj});
            return null;
        }
    }

    public Short getShort(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof Number) {
            return Short.valueOf(((Number) obj).shortValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Short.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.info(LABEL, "Cannot parse Short value for %{private}s at column name %{private}s.", new Object[]{obj, str});
                return null;
            }
        } else {
            HiLog.info(LABEL, "Cannot cast value for %{private}s to a Short value: %{private}s.", new Object[]{str, obj});
            return null;
        }
    }

    public Byte getByte(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof Number) {
            return Byte.valueOf(((Number) obj).byteValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Byte.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.info(LABEL, "Cannot parse Byte value for %{private}s at column name %{private}s.", new Object[]{obj, str});
                return null;
            }
        } else {
            HiLog.info(LABEL, "Cannot cast value for %{private}s to a Byte value: %{private}s.", new Object[]{str, obj});
            return null;
        }
    }

    public Double getDouble(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof Number) {
            return Double.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Double.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.info(LABEL, "Cannot parse Double value for %{private}s at column name %{private}s.", new Object[]{obj, str});
                return null;
            }
        } else {
            HiLog.info(LABEL, "Cannot cast value for %{private}s to a Double value: %{private}s.", new Object[]{str, obj});
            return null;
        }
    }

    public Float getFloat(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof Number) {
            return Float.valueOf(((Number) obj).floatValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Float.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.info(LABEL, "Cannot parse Float value for %{private}s at column name %{private}s.", new Object[]{obj, str});
                return null;
            }
        } else {
            HiLog.info(LABEL, "Cannot cast value for %{private}s to a Float value: %{private}s.", new Object[]{str, obj});
            return null;
        }
    }

    public Boolean getBoolean(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        boolean z = true;
        if (obj instanceof CharSequence) {
            if (!Boolean.valueOf(obj.toString()).booleanValue() && !"1".equals(obj)) {
                z = false;
            }
            return Boolean.valueOf(z);
        } else if (obj instanceof Number) {
            if (((Number) obj).intValue() == 0) {
                z = false;
            }
            return Boolean.valueOf(z);
        } else {
            HiLog.info(LABEL, "Cannot cast value for %{private}s to a Boolean value: %{private}s.", new Object[]{str, obj});
            return null;
        }
    }

    public byte[] getByteArray(String str) {
        Object obj = this.map.get(str);
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        return null;
    }

    public Set<Map.Entry<String, Object>> getAll() {
        return this.map.entrySet();
    }

    public Set<String> getColumnSet() {
        return this.map.keySet();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ValuesBucket)) {
            return false;
        }
        return this.map.equals(((ValuesBucket) obj).map);
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String str : this.map.keySet()) {
            String string = getString(str);
            if (sb.length() > 0) {
                sb.append("||");
            }
            sb.append(str);
            sb.append(" = ");
            sb.append(string);
        }
        return sb.toString();
    }

    public boolean marshalling(Parcel parcel) {
        if (this.map != null) {
            parcel.writeInt(1);
            Set<Map.Entry<String, Object>> entrySet = this.map.entrySet();
            parcel.writeInt(entrySet.size());
            for (Map.Entry<String, Object> entry : entrySet) {
                parcel.writeString(entry.getKey());
                writeValue(parcel, entry.getValue());
            }
        } else {
            parcel.writeInt(0);
        }
        return true;
    }

    private boolean writeValue(Parcel parcel, Object obj) {
        if (obj == null) {
            parcel.writeInt(0);
            return true;
        } else if (obj instanceof String) {
            parcel.writeInt(2);
            parcel.writeString((String) obj);
            return true;
        } else if (obj instanceof Byte) {
            parcel.writeInt(3);
            parcel.writeInt(((Byte) obj).byteValue());
            return true;
        } else if (obj instanceof Short) {
            parcel.writeInt(4);
            parcel.writeInt(((Short) obj).intValue());
            return true;
        } else if (obj instanceof Integer) {
            parcel.writeInt(5);
            parcel.writeInt(((Integer) obj).intValue());
            return true;
        } else if (obj instanceof Long) {
            parcel.writeInt(6);
            parcel.writeLong(((Long) obj).longValue());
            return true;
        } else if (obj instanceof Float) {
            parcel.writeInt(7);
            parcel.writeFloat(((Float) obj).floatValue());
            return true;
        } else if (obj instanceof Double) {
            parcel.writeInt(8);
            parcel.writeDouble(((Double) obj).doubleValue());
            return true;
        } else if (obj instanceof Boolean) {
            parcel.writeInt(9);
            parcel.writeBoolean(((Boolean) obj).booleanValue());
            return true;
        } else if (obj instanceof byte[]) {
            parcel.writeInt(10);
            parcel.writeByteArray((byte[]) obj);
            return true;
        } else {
            throw new AssertionError("Parcel: unable to marshal value " + obj);
        }
    }

    private HashMap<String, Object> readHashMap(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt < 0) {
            return null;
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        while (readInt > 0 && parcel.getReadableBytes() > 0) {
            hashMap.put(parcel.readString(), readValue(parcel));
            readInt--;
        }
        return hashMap;
    }

    private Object readValue(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt == 0) {
            return null;
        }
        switch (readInt) {
            case 2:
                return parcel.readString();
            case 3:
                return Byte.valueOf(parcel.readByte());
            case 4:
                return Short.valueOf((short) parcel.readInt());
            case 5:
                return Integer.valueOf(parcel.readInt());
            case 6:
                return Long.valueOf(parcel.readLong());
            case 7:
                return Float.valueOf(parcel.readFloat());
            case 8:
                return Double.valueOf(parcel.readDouble());
            case 9:
                return Boolean.valueOf(parcel.readBoolean());
            case 10:
                return parcel.readByteArray();
            default:
                throw new AssertionError("Parcel: unmarshalling unknown type " + readInt);
        }
    }
}
