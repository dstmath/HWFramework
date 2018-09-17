package android.content;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public final class ContentValues implements Parcelable {
    public static final Creator<ContentValues> CREATOR = new Creator<ContentValues>() {
        public ContentValues createFromParcel(Parcel in) {
            return new ContentValues(in.readHashMap(null), null);
        }

        public ContentValues[] newArray(int size) {
            return new ContentValues[size];
        }
    };
    public static final String TAG = "ContentValues";
    private HashMap<String, Object> mValues;

    /* synthetic */ ContentValues(HashMap values, ContentValues -this1) {
        this(values);
    }

    public ContentValues() {
        this.mValues = new HashMap(8);
    }

    public ContentValues(int size) {
        this.mValues = new HashMap(size, 1.0f);
    }

    public ContentValues(ContentValues from) {
        this.mValues = new HashMap(from.mValues);
    }

    private ContentValues(HashMap<String, Object> values) {
        this.mValues = values;
    }

    public boolean equals(Object object) {
        if (object instanceof ContentValues) {
            return this.mValues.equals(((ContentValues) object).mValues);
        }
        return false;
    }

    public int hashCode() {
        return this.mValues.hashCode();
    }

    public void put(String key, String value) {
        this.mValues.put(key, value);
    }

    public void putAll(ContentValues other) {
        this.mValues.putAll(other.mValues);
    }

    public void put(String key, Byte value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Short value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Integer value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Long value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Float value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Double value) {
        this.mValues.put(key, value);
    }

    public void put(String key, Boolean value) {
        this.mValues.put(key, value);
    }

    public void put(String key, byte[] value) {
        this.mValues.put(key, value);
    }

    public void putNull(String key) {
        this.mValues.put(key, null);
    }

    public int size() {
        return this.mValues.size();
    }

    public boolean isEmpty() {
        return this.mValues.isEmpty();
    }

    public void remove(String key) {
        this.mValues.remove(key);
    }

    public void clear() {
        this.mValues.clear();
    }

    public boolean containsKey(String key) {
        return this.mValues.containsKey(key);
    }

    public Object get(String key) {
        return this.mValues.get(key);
    }

    public String getAsString(String key) {
        Object value = this.mValues.get(key);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public Long getAsLong(String key) {
        Long valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Long.valueOf(((Number) value).longValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Long.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Long value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Long: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Integer getAsInteger(String key) {
        Integer valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Integer.valueOf(((Number) value).intValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Integer.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Integer value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Integer: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Short getAsShort(String key) {
        Short valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Short.valueOf(((Number) value).shortValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Short.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Short value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Short: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Byte getAsByte(String key) {
        Byte valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Byte.valueOf(((Number) value).byteValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Byte.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Byte value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Byte: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Double getAsDouble(String key) {
        Double valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Double.valueOf(((Number) value).doubleValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Double.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Double value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Double: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Float getAsFloat(String key) {
        Float valueOf;
        Object value = this.mValues.get(key);
        if (value != null) {
            try {
                valueOf = Float.valueOf(((Number) value).floatValue());
            } catch (ClassCastException e) {
                if (value instanceof CharSequence) {
                    try {
                        return Float.valueOf(value.toString());
                    } catch (NumberFormatException e2) {
                        Log.e(TAG, "Cannot parse Float value for " + value + " at key " + key);
                        return null;
                    }
                }
                Log.e(TAG, "Cannot cast value for " + key + " to a Float: " + value, e);
                return null;
            }
        }
        valueOf = null;
        return valueOf;
    }

    public Boolean getAsBoolean(String key) {
        boolean z = true;
        Object value = this.mValues.get(key);
        try {
            return (Boolean) value;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                if (!Boolean.valueOf(value.toString()).booleanValue()) {
                    z = "1".equals(value);
                }
                return Boolean.valueOf(z);
            } else if (value instanceof Number) {
                if (((Number) value).intValue() == 0) {
                    z = false;
                }
                return Boolean.valueOf(z);
            } else {
                Log.e(TAG, "Cannot cast value for " + key + " to a Boolean: " + value, e);
                return null;
            }
        }
    }

    public byte[] getAsByteArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return null;
    }

    public Set<Entry<String, Object>> valueSet() {
        return this.mValues.entrySet();
    }

    public Set<String> keySet() {
        return this.mValues.keySet();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeMap(this.mValues);
    }

    @Deprecated
    public void putStringArrayList(String key, ArrayList<String> value) {
        this.mValues.put(key, value);
    }

    @Deprecated
    public ArrayList<String> getStringArrayList(String key) {
        return (ArrayList) this.mValues.get(key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : this.mValues.keySet()) {
            String value = getAsString(name);
            if (sb.length() > 0) {
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            sb.append(name).append("=").append(value);
        }
        return sb.toString();
    }
}
