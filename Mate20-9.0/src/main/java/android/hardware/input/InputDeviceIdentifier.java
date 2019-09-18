package android.hardware.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.Objects;

public final class InputDeviceIdentifier implements Parcelable {
    public static final Parcelable.Creator<InputDeviceIdentifier> CREATOR = new Parcelable.Creator<InputDeviceIdentifier>() {
        public InputDeviceIdentifier createFromParcel(Parcel source) {
            return new InputDeviceIdentifier(source);
        }

        public InputDeviceIdentifier[] newArray(int size) {
            return new InputDeviceIdentifier[size];
        }
    };
    private final String mDescriptor;
    private final int mProductId;
    private final int mVendorId;

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
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof InputDeviceIdentifier)) {
            return false;
        }
        InputDeviceIdentifier that = (InputDeviceIdentifier) o;
        if (!(this.mVendorId == that.mVendorId && this.mProductId == that.mProductId && TextUtils.equals(this.mDescriptor, that.mDescriptor))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mDescriptor, Integer.valueOf(this.mVendorId), Integer.valueOf(this.mProductId)});
    }
}
