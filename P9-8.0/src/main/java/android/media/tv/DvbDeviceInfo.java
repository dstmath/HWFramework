package android.media.tv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class DvbDeviceInfo implements Parcelable {
    public static final Creator<DvbDeviceInfo> CREATOR = new Creator<DvbDeviceInfo>() {
        public DvbDeviceInfo createFromParcel(Parcel source) {
            try {
                return new DvbDeviceInfo(source, null);
            } catch (Exception e) {
                Log.e(DvbDeviceInfo.TAG, "Exception creating DvbDeviceInfo from parcel", e);
                return null;
            }
        }

        public DvbDeviceInfo[] newArray(int size) {
            return new DvbDeviceInfo[size];
        }
    };
    static final String TAG = "DvbDeviceInfo";
    private final int mAdapterId;
    private final int mDeviceId;

    private DvbDeviceInfo(Parcel source) {
        this.mAdapterId = source.readInt();
        this.mDeviceId = source.readInt();
    }

    public DvbDeviceInfo(int adapterId, int deviceId) {
        this.mAdapterId = adapterId;
        this.mDeviceId = deviceId;
    }

    public int getAdapterId() {
        return this.mAdapterId;
    }

    public int getDeviceId() {
        return this.mDeviceId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAdapterId);
        dest.writeInt(this.mDeviceId);
    }
}
