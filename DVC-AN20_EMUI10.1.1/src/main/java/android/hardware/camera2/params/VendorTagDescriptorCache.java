package android.hardware.camera2.params;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class VendorTagDescriptorCache implements Parcelable {
    public static final Parcelable.Creator<VendorTagDescriptorCache> CREATOR = new Parcelable.Creator<VendorTagDescriptorCache>() {
        /* class android.hardware.camera2.params.VendorTagDescriptorCache.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VendorTagDescriptorCache createFromParcel(Parcel source) {
            try {
                return new VendorTagDescriptorCache(source);
            } catch (Exception e) {
                Log.e(VendorTagDescriptorCache.TAG, "Exception creating VendorTagDescriptorCache from parcel", e);
                return null;
            }
        }

        @Override // android.os.Parcelable.Creator
        public VendorTagDescriptorCache[] newArray(int size) {
            return new VendorTagDescriptorCache[size];
        }
    };
    private static final String TAG = "VendorTagDescriptorCache";

    private VendorTagDescriptorCache(Parcel source) {
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
