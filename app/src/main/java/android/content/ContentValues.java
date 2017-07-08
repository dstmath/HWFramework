package android.content;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public final class ContentValues implements Parcelable {
    public static final Creator<ContentValues> CREATOR = null;
    public static final String TAG = "ContentValues";
    private HashMap<String, Object> mValues;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.ContentValues.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.ContentValues.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ContentValues.<clinit>():void");
    }

    public ContentValues() {
        this.mValues = new HashMap(8);
    }

    public ContentValues(int size) {
        this.mValues = new HashMap(size, Engine.DEFAULT_VOLUME);
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
        boolean z = false;
        Object value = this.mValues.get(key);
        try {
            return (Boolean) value;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                return Boolean.valueOf(value.toString());
            }
            if (value instanceof Number) {
                if (((Number) value).intValue() != 0) {
                    z = true;
                }
                return Boolean.valueOf(z);
            }
            Log.e(TAG, "Cannot cast value for " + key + " to a Boolean: " + value, e);
            return null;
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
