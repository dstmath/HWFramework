package android.hardware.camera2.params;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class VendorTagDescriptorCache implements Parcelable {
    public static final Creator<VendorTagDescriptorCache> CREATOR = new Creator<VendorTagDescriptorCache>() {
        public VendorTagDescriptorCache createFromParcel(Parcel source) {
            try {
                return new VendorTagDescriptorCache(source, null);
            } catch (Exception e) {
                Log.e(VendorTagDescriptorCache.TAG, "Exception creating VendorTagDescriptorCache from parcel", e);
                return null;
            }
        }

        public VendorTagDescriptorCache[] newArray(int size) {
            return new VendorTagDescriptorCache[size];
        }
    };
    private static final String TAG = "VendorTagDescriptorCache";

    /* synthetic */ VendorTagDescriptorCache(Parcel source, VendorTagDescriptorCache -this1) {
        this(source);
    }

    private VendorTagDescriptorCache(Parcel source) {
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
