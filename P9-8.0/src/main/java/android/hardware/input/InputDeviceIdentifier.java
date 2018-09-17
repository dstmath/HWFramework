package android.hardware.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.Objects;

public final class InputDeviceIdentifier implements Parcelable {
    public static final Creator<InputDeviceIdentifier> CREATOR = new Creator<InputDeviceIdentifier>() {
        public InputDeviceIdentifier createFromParcel(Parcel source) {
            return new InputDeviceIdentifier(source, null);
        }

        public InputDeviceIdentifier[] newArray(int size) {
            return new InputDeviceIdentifier[size];
        }
    };
    private final String mDescriptor;
    private final int mProductId;
    private final int mVendorId;

    /* synthetic */ InputDeviceIdentifier(Parcel src, InputDeviceIdentifier -this1) {
        this(src);
    }

    public InputDeviceIdentifier(String descriptor, int vendorId, int productId) {
        this.mDescriptor = descriptor;
        this.mVendorId = vendorId;
        this.mProductId = productId;
    }

    private InputDeviceIdentifier(Parcel src) {
        this.mDescriptor = src.readString();
        this.mVendorId = src.readInt();
        this.mProductId = src.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDescriptor);
        dest.writeInt(this.mVendorId);
        dest.writeInt(this.mProductId);
    }

    public String getDescriptor() {
        return this.mDescriptor;
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int getProductId() {
        return this.mProductId;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || ((o instanceof InputDeviceIdentifier) ^ 1) != 0) {
            return false;
        }
        InputDeviceIdentifier that = (InputDeviceIdentifier) o;
        if (this.mVendorId == that.mVendorId && this.mProductId == that.mProductId) {
            z = TextUtils.equals(this.mDescriptor, that.mDescriptor);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mDescriptor, Integer.valueOf(this.mVendorId), Integer.valueOf(this.mProductId)});
    }
}
