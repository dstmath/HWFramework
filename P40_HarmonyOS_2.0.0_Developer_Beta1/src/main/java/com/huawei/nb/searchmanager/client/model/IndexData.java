package com.huawei.nb.searchmanager.client.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.searchmanager.utils.logger.DSLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class IndexData implements Parcelable {
    public static final Parcelable.Creator<IndexData> CREATOR = new Parcelable.Creator<IndexData>() {
        /* class com.huawei.nb.searchmanager.client.model.IndexData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IndexData createFromParcel(Parcel parcel) {
            return new IndexData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public IndexData[] newArray(int i) {
            return new IndexData[i];
        }
    };
    private static final String TAG = "IndexData";
    private Map<String, Object> values = new HashMap();

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public IndexData() {
    }

    protected IndexData(Parcel parcel) {
        readFromParcel(parcel);
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    public void setValues(Map<String, Object> map) {
        this.values = map;
    }

    public void put(String str, String str2) {
        if (str == null || str2 == null) {
            DSLog.et(TAG, "null String value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, str2);
        }
    }

    public void put(String str, Integer num) {
        if (str == null || num == null) {
            DSLog.et(TAG, "null Integer value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, num);
        }
    }

    public void put(String str, Float f) {
        if (str == null || f == null) {
            DSLog.et(TAG, "null Float value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, f);
        }
    }

    public void put(String str, Double d) {
        if (str == null || d == null) {
            DSLog.et(TAG, "null Double value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, d);
        }
    }

    public void put(String str, Long l) {
        if (str == null || l == null) {
            DSLog.et(TAG, "null Long value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, l);
        }
    }

    public Object get(String str) {
        return this.values.get(str);
    }

    public String getAsString(String str) {
        Object obj = this.values.get(str);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    public Integer getAsInteger(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Integer.valueOf(((Number) obj).intValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Integer.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                DSLog.et(TAG, "Cannot parse value to a Integer", new Object[0]);
                return null;
            }
        } else {
            DSLog.et(TAG, "Cannot cast value to a Integer", new Object[0]);
            return null;
        }
    }

    public Float getAsFloat(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Float.valueOf(((Number) obj).floatValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Float.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                DSLog.et(TAG, "Cannot parse value to a Float", new Object[0]);
                return null;
            }
        } else {
            DSLog.et(TAG, "Cannot cast value to a Float", new Object[0]);
            return null;
        }
    }

    public Double getAsDouble(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Double.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Double.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                DSLog.et(TAG, "Cannot parse value to a Double", new Object[0]);
                return null;
            }
        } else {
            DSLog.et(TAG, "Cannot cast value to a Double", new Object[0]);
            return null;
        }
    }

    public Long getAsLong(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Long.valueOf(((Number) obj).longValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Long.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                DSLog.et(TAG, "Cannot parse value to a Long", new Object[0]);
                return null;
            }
        } else {
            DSLog.et(TAG, "Cannot cast value to a Long", new Object[0]);
            return null;
        }
    }

    public int size() {
        return this.values.size();
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public void remove(String str) {
        this.values.remove(str);
    }

    public void clear() {
        this.values.clear();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeMap(this.values);
    }

    private void readFromParcel(Parcel parcel) {
        parcel.readMap(this.values, null);
    }

    public String toJson() {
        return new JSONObject(this.values).toString();
    }

    public IndexData fromJson(String str) {
        if (str != null && !"".equals(str.trim())) {
            try {
                JSONObject jSONObject = new JSONObject(str);
                Iterator<String> keys = jSONObject.keys();
                while (keys.hasNext()) {
                    String next = keys.next();
                    if (next == null) {
                        DSLog.et(TAG, "key is null, skip", new Object[0]);
                    } else {
                        Object obj = jSONObject.get(next);
                        if (obj == null) {
                            DSLog.et(TAG, "value is null, skip", new Object[0]);
                        } else if (isSupportedType(obj)) {
                            this.values.put(next, obj);
                        }
                    }
                }
            } catch (JSONException e) {
                DSLog.et(TAG, "throw JSONException: " + e.getMessage(), new Object[0]);
            }
        }
        return this;
    }

    private boolean isSupportedType(Object obj) {
        return (obj instanceof String) || (obj instanceof Integer) || (obj instanceof Float) || (obj instanceof Double) || (obj instanceof Long);
    }
}
