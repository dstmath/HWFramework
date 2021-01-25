package android.content;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ContentValues implements Parcelable {
    public static final Parcelable.Creator<ContentValues> CREATOR = new Parcelable.Creator<ContentValues>() {
        /* class android.content.ContentValues.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ContentValues createFromParcel(Parcel in) {
            return new ContentValues(in);
        }

        @Override // android.os.Parcelable.Creator
        public ContentValues[] newArray(int size) {
            return new ContentValues[size];
        }
    };
    public static final String TAG = "ContentValues";
    private final ArrayMap<String, Object> mMap;
    @UnsupportedAppUsage
    @Deprecated
    private HashMap<String, Object> mValues;

    public ContentValues() {
        this.mMap = new ArrayMap<>();
    }

    public ContentValues(int size) {
        Preconditions.checkArgumentNonnegative(size);
        this.mMap = new ArrayMap<>(size);
    }

    public ContentValues(ContentValues from) {
        Objects.requireNonNull(from);
        this.mMap = new ArrayMap<>(from.mMap);
    }

    @UnsupportedAppUsage
    @Deprecated
    private ContentValues(HashMap<String, Object> from) {
        this.mMap = new ArrayMap<>();
        this.mMap.putAll(from);
    }

    private ContentValues(Parcel in) {
        this.mMap = new ArrayMap<>(in.readInt());
        in.readArrayMap(this.mMap, null);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ContentValues)) {
            return false;
        }
        return this.mMap.equals(((ContentValues) object).mMap);
    }

    public ArrayMap<String, Object> getValues() {
        return this.mMap;
    }

    public int hashCode() {
        return this.mMap.hashCode();
    }

    public void put(String key, String value) {
        this.mMap.put(key, value);
    }

    public void putAll(ContentValues other) {
        this.mMap.putAll((ArrayMap<? extends String, ? extends Object>) other.mMap);
    }

    public void put(String key, Byte value) {
        this.mMap.put(key, value);
    }

    public void put(String key, Short value) {
        this.mMap.put(key, value);
    }

    public void put(String key, Integer value) {
        this.mMap.put(key, value);
    }

    public void put(String key, Long value) {
        this.mMap.put(key, value);
    }

    public void put(String key, Float value) {
        this.mMap.put(key, value);
    }

    public void put(String key, Double value) {
        this.mMap.put(key, value);
    }

    public void put(String key, Boolean value) {
        this.mMap.put(key, value);
    }

    public void put(String key, byte[] value) {
        this.mMap.put(key, value);
    }

    public void putNull(String key) {
        this.mMap.put(key, null);
    }

    public int size() {
        return this.mMap.size();
    }

    public boolean isEmpty() {
        return this.mMap.isEmpty();
    }

    public void remove(String key) {
        this.mMap.remove(key);
    }

    public void clear() {
        this.mMap.clear();
    }

    public boolean containsKey(String key) {
        return this.mMap.containsKey(key);
    }

    public Object get(String key) {
        return this.mMap.get(key);
    }

    public String getAsString(String key) {
        Object value = this.mMap.get(key);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public Long getAsLong(String key) {
        Object value = this.mMap.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(((Number) value).longValue());
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Long value at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Long", e);
                return null;
            }
        }
    }

    public Integer getAsInteger(String key) {
        Object value = this.mMap.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(((Number) value).intValue());
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Integer value at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Integer", e);
                return null;
            }
        }
    }

    public Short getAsShort(String key) {
        Object value = this.mMap.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Short.valueOf(((Number) value).shortValue());
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Short.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Short value at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Short:", e);
                return null;
            }
        }
    }

    public Byte getAsByte(String key) {
        Object value = this.mMap.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Byte.valueOf(((Number) value).byteValue());
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Byte.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Byte value at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Byte", e);
                return null;
            }
        }
    }

    public Double getAsDouble(String key) {
        Object value = this.mMap.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(((Number) value).doubleValue());
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Double.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Double value at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Double", e);
                return null;
            }
        }
    }

    public Float getAsFloat(String key) {
        Object value = this.mMap.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Float.valueOf(((Number) value).floatValue());
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannot parse Float value at key " + key);
                    return null;
                }
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Float", e);
                return null;
            }
        }
    }

    public Boolean getAsBoolean(String key) {
        Object value = this.mMap.get(key);
        try {
            return (Boolean) value;
        } catch (ClassCastException e) {
            boolean z = false;
            if (value instanceof CharSequence) {
                if (Boolean.valueOf(value.toString()).booleanValue() || "1".equals(value)) {
                    z = true;
                }
                return Boolean.valueOf(z);
            } else if (value instanceof Number) {
                if (((Number) value).intValue() != 0) {
                    z = true;
                }
                return Boolean.valueOf(z);
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Boolean: ", e);
                return null;
            }
        }
    }

    public byte[] getAsByteArray(String key) {
        Object value = this.mMap.get(key);
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return null;
    }

    public Set<Map.Entry<String, Object>> valueSet() {
        return this.mMap.entrySet();
    }

    public Set<String> keySet() {
        return this.mMap.keySet();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mMap.size());
        parcel.writeArrayMap(this.mMap);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void putStringArrayList(String key, ArrayList<String> value) {
        this.mMap.put(key, value);
    }

    @UnsupportedAppUsage
    @Deprecated
    public ArrayList<String> getStringArrayList(String key) {
        return (ArrayList) this.mMap.get(key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : this.mMap.keySet()) {
            String value = getAsString(name);
            if (sb.length() > 0) {
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            sb.append(name + "=" + value);
        }
        return sb.toString();
    }
}
