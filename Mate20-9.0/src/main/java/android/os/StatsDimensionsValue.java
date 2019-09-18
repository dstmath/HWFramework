package android.os;

import android.annotation.SystemApi;
import android.os.Parcelable;
import android.provider.SettingsStringUtil;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

@SystemApi
public final class StatsDimensionsValue implements Parcelable {
    public static final int BOOLEAN_VALUE_TYPE = 5;
    public static final Parcelable.Creator<StatsDimensionsValue> CREATOR = new Parcelable.Creator<StatsDimensionsValue>() {
        public StatsDimensionsValue createFromParcel(Parcel in) {
            return new StatsDimensionsValue(in);
        }

        public StatsDimensionsValue[] newArray(int size) {
            return new StatsDimensionsValue[size];
        }
    };
    public static final int FLOAT_VALUE_TYPE = 6;
    public static final int INT_VALUE_TYPE = 3;
    public static final int LONG_VALUE_TYPE = 4;
    public static final int STRING_VALUE_TYPE = 2;
    private static final String TAG = "StatsDimensionsValue";
    public static final int TUPLE_VALUE_TYPE = 7;
    private final int mField;
    private final Object mValue;
    private final int mValueType;

    public StatsDimensionsValue(Parcel in) {
        this.mField = in.readInt();
        this.mValueType = in.readInt();
        this.mValue = readValueFromParcel(this.mValueType, in);
    }

    public int getField() {
        return this.mField;
    }

    public String getStringValue() {
        try {
            if (this.mValueType == 2) {
                return (String) this.mValue;
            }
        } catch (ClassCastException e) {
            Slog.w(TAG, "Failed to successfully get value", e);
        }
        return null;
    }

    public int getIntValue() {
        try {
            if (this.mValueType == 3) {
                return ((Integer) this.mValue).intValue();
            }
        } catch (ClassCastException e) {
            Slog.w(TAG, "Failed to successfully get value", e);
        }
        return 0;
    }

    public long getLongValue() {
        try {
            if (this.mValueType == 4) {
                return ((Long) this.mValue).longValue();
            }
        } catch (ClassCastException e) {
            Slog.w(TAG, "Failed to successfully get value", e);
        }
        return 0;
    }

    public boolean getBooleanValue() {
        try {
            if (this.mValueType == 5) {
                return ((Boolean) this.mValue).booleanValue();
            }
        } catch (ClassCastException e) {
            Slog.w(TAG, "Failed to successfully get value", e);
        }
        return false;
    }

    public float getFloatValue() {
        try {
            if (this.mValueType == 6) {
                return ((Float) this.mValue).floatValue();
            }
        } catch (ClassCastException e) {
            Slog.w(TAG, "Failed to successfully get value", e);
        }
        return 0.0f;
    }

    public List<StatsDimensionsValue> getTupleValueList() {
        if (this.mValueType != 7) {
            return null;
        }
        try {
            StatsDimensionsValue[] orig = (StatsDimensionsValue[]) this.mValue;
            List<StatsDimensionsValue> copy = new ArrayList<>(orig.length);
            for (StatsDimensionsValue add : orig) {
                copy.add(add);
            }
            return copy;
        } catch (ClassCastException e) {
            Slog.w(TAG, "Failed to successfully get value", e);
            return null;
        }
    }

    public int getValueType() {
        return this.mValueType;
    }

    public boolean isValueType(int valueType) {
        return this.mValueType == valueType;
    }

    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(this.mField);
            sb.append(SettingsStringUtil.DELIMITER);
            if (this.mValueType == 7) {
                sb.append("{");
                StatsDimensionsValue[] sbvs = (StatsDimensionsValue[]) this.mValue;
                for (StatsDimensionsValue statsDimensionsValue : sbvs) {
                    sb.append(statsDimensionsValue.toString());
                    sb.append("|");
                }
                sb.append("}");
            } else {
                sb.append(this.mValue.toString());
            }
            return sb.toString();
        } catch (ClassCastException e) {
            Slog.w(TAG, "Failed to successfully get value", e);
            return "";
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mField);
        out.writeInt(this.mValueType);
        writeValueToParcel(this.mValueType, this.mValue, out, flags);
    }

    private static boolean writeValueToParcel(int valueType, Object value, Parcel out, int flags) {
        switch (valueType) {
            case 2:
                out.writeString((String) value);
                return true;
            case 3:
                out.writeInt(((Integer) value).intValue());
                return true;
            case 4:
                out.writeLong(((Long) value).longValue());
                return true;
            case 5:
                out.writeBoolean(((Boolean) value).booleanValue());
                return true;
            case 6:
                out.writeFloat(((Float) value).floatValue());
                return true;
            case 7:
                try {
                    StatsDimensionsValue[] values = (StatsDimensionsValue[]) value;
                    out.writeInt(values.length);
                    for (StatsDimensionsValue writeToParcel : values) {
                        writeToParcel.writeToParcel(out, flags);
                    }
                    return true;
                } catch (ClassCastException e) {
                    Slog.w(TAG, "writeValue cast failed", e);
                    return false;
                }
            default:
                Slog.w(TAG, "readValue of an impossible type " + valueType);
                return false;
        }
    }

    private static Object readValueFromParcel(int valueType, Parcel parcel) {
        switch (valueType) {
            case 2:
                return parcel.readString();
            case 3:
                return Integer.valueOf(parcel.readInt());
            case 4:
                return Long.valueOf(parcel.readLong());
            case 5:
                return Boolean.valueOf(parcel.readBoolean());
            case 6:
                return Float.valueOf(parcel.readFloat());
            case 7:
                int sz = parcel.readInt();
                StatsDimensionsValue[] values = new StatsDimensionsValue[sz];
                for (int i = 0; i < sz; i++) {
                    values[i] = new StatsDimensionsValue(parcel);
                }
                return values;
            default:
                Slog.w(TAG, "readValue of an impossible type " + valueType);
                return null;
        }
    }
}
