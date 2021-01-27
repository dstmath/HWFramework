package android.os;

import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class CoolingDevice implements Parcelable {
    public static final Parcelable.Creator<CoolingDevice> CREATOR = new Parcelable.Creator<CoolingDevice>() {
        /* class android.os.CoolingDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CoolingDevice createFromParcel(Parcel p) {
            return new CoolingDevice(p.readLong(), p.readInt(), p.readString());
        }

        @Override // android.os.Parcelable.Creator
        public CoolingDevice[] newArray(int size) {
            return new CoolingDevice[size];
        }
    };
    public static final int TYPE_BATTERY = 1;
    public static final int TYPE_COMPONENT = 6;
    public static final int TYPE_CPU = 2;
    public static final int TYPE_FAN = 0;
    public static final int TYPE_GPU = 3;
    public static final int TYPE_MODEM = 4;
    public static final int TYPE_NPU = 5;
    private final String mName;
    private final int mType;
    private final long mValue;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public static boolean isValidType(int type) {
        return type >= 0 && type <= 6;
    }

    public CoolingDevice(long value, int type, String name) {
        Preconditions.checkArgument(isValidType(type), "Invalid Type");
        this.mValue = value;
        this.mType = type;
        this.mName = (String) Preconditions.checkStringNotEmpty(name);
    }

    public long getValue() {
        return this.mValue;
    }

    public int getType() {
        return this.mType;
    }

    public String getName() {
        return this.mName;
    }

    public String toString() {
        return "CoolingDevice{mValue=" + this.mValue + ", mType=" + this.mType + ", mName=" + this.mName + "}";
    }

    public int hashCode() {
        return (((this.mName.hashCode() * 31) + Long.hashCode(this.mValue)) * 31) + this.mType;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CoolingDevice)) {
            return false;
        }
        CoolingDevice other = (CoolingDevice) o;
        if (other.mValue == this.mValue && other.mType == this.mType && other.mName.equals(this.mName)) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel p, int flags) {
        p.writeLong(this.mValue);
        p.writeInt(this.mType);
        p.writeString(this.mName);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
