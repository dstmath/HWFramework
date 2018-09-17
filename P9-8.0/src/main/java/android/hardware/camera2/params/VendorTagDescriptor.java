package android.hardware.camera2.params;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class VendorTagDescriptor implements Parcelable {
    public static final Creator<VendorTagDescriptor> CREATOR = new Creator<VendorTagDescriptor>() {
        public VendorTagDescriptor createFromParcel(Parcel source) {
            try {
                return new VendorTagDescriptor(source, null);
            } catch (Exception e) {
                Log.e(VendorTagDescriptor.TAG, "Exception creating VendorTagDescriptor from parcel", e);
                return null;
            }
        }

        public VendorTagDescriptor[] newArray(int size) {
            return new VendorTagDescriptor[size];
        }
    };
    private static final String TAG = "VendorTagDescriptor";

    /* synthetic */ VendorTagDescriptor(Parcel source, VendorTagDescriptor -this1) {
        this(source);
    }

    private VendorTagDescriptor(Parcel source) {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            throw new IllegalArgumentException("dest must not be null");
        }
    }
}
