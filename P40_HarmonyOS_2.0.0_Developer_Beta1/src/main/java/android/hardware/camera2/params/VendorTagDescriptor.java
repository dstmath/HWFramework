package android.hardware.camera2.params;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class VendorTagDescriptor implements Parcelable {
    public static final Parcelable.Creator<VendorTagDescriptor> CREATOR = new Parcelable.Creator<VendorTagDescriptor>() {
        /* class android.hardware.camera2.params.VendorTagDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VendorTagDescriptor createFromParcel(Parcel source) {
            try {
                return new VendorTagDescriptor(source);
            } catch (Exception e) {
                Log.e(VendorTagDescriptor.TAG, "Exception creating VendorTagDescriptor from parcel", e);
                return null;
            }
        }

        @Override // android.os.Parcelable.Creator
        public VendorTagDescriptor[] newArray(int size) {
            return new VendorTagDescriptor[size];
        }
    };
    private static final String TAG = "VendorTagDescriptor";

    private VendorTagDescriptor(Parcel source) {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            throw new IllegalArgumentException("dest must not be null");
        }
    }
}
