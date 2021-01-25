package android.os;

import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Temperature implements Parcelable {
    public static final Parcelable.Creator<Temperature> CREATOR = new Parcelable.Creator<Temperature>() {
        /* class android.os.Temperature.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Temperature createFromParcel(Parcel p) {
            return new Temperature(p.readFloat(), p.readInt(), p.readString(), p.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public Temperature[] newArray(int size) {
            return new Temperature[size];
        }
    };
    public static final int THROTTLING_CRITICAL = 4;
    public static final int THROTTLING_EMERGENCY = 5;
    public static final int THROTTLING_LIGHT = 1;
    public static final int THROTTLING_MODERATE = 2;
    public static final int THROTTLING_NONE = 0;
    public static final int THROTTLING_SEVERE = 3;
    public static final int THROTTLING_SHUTDOWN = 6;
    public static final int TYPE_BATTERY = 2;
    public static final int TYPE_BCL_CURRENT = 7;
    public static final int TYPE_BCL_PERCENTAGE = 8;
    public static final int TYPE_BCL_VOLTAGE = 6;
    public static final int TYPE_CPU = 0;
    public static final int TYPE_GPU = 1;
    public static final int TYPE_NPU = 9;
    public static final int TYPE_POWER_AMPLIFIER = 5;
    public static final int TYPE_SKIN = 3;
    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_USB_PORT = 4;
    private final String mName;
    private final int mStatus;
    private final int mType;
    private final float mValue;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ThrottlingStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public static boolean isValidType(int type) {
        return type >= -1 && type <= 9;
    }

    public static boolean isValidStatus(int status) {
        return status >= 0 && status <= 6;
    }

    public Temperature(float value, int type, String name, int status) {
        Preconditions.checkArgument(isValidType(type), "Invalid Type");
        Preconditions.checkArgument(isValidStatus(status), "Invalid Status");
        this.mValue = value;
        this.mType = type;
        this.mName = (String) Preconditions.checkStringNotEmpty(name);
        this.mStatus = status;
    }

    public float getValue() {
        return this.mValue;
    }

    public int getType() {
        return this.mType;
    }

    public String getName() {
        return this.mName;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public String toString() {
        return "Temperature{mValue=" + this.mValue + ", mType=" + this.mType + ", mName=" + this.mName + ", mStatus=" + this.mStatus + "}";
    }

    public int hashCode() {
        return (((((this.mName.hashCode() * 31) + Float.hashCode(this.mValue)) * 31) + this.mType) * 31) + this.mStatus;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Temperature)) {
            return false;
        }
        Temperature other = (Temperature) o;
        if (other.mValue == this.mValue && other.mType == this.mType && other.mName.equals(this.mName) && other.mStatus == this.mStatus) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel p, int flags) {
        p.writeFloat(this.mValue);
        p.writeInt(this.mType);
        p.writeString(this.mName);
        p.writeInt(this.mStatus);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
