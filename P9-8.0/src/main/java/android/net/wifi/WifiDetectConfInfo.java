package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class WifiDetectConfInfo implements Parcelable {
    public static final Creator<WifiDetectConfInfo> CREATOR = new Creator<WifiDetectConfInfo>() {
        public WifiDetectConfInfo createFromParcel(Parcel in) {
            return new WifiDetectConfInfo(in.readInt(), in.readInt(), in.readInt());
        }

        public WifiDetectConfInfo[] newArray(int size) {
            return new WifiDetectConfInfo[size];
        }
    };
    public int mEnvalueCount;
    public int mThreshold;
    public int mWifiDetectMode;

    public WifiDetectConfInfo(int DetectMode, int Threshold, int EnvalueCount) {
        this.mWifiDetectMode = DetectMode;
        this.mThreshold = Threshold;
        this.mEnvalueCount = EnvalueCount;
    }

    public WifiDetectConfInfo(WifiDetectConfInfo info) {
        this.mWifiDetectMode = info.mWifiDetectMode;
        this.mThreshold = info.mWifiDetectMode;
        this.mEnvalueCount = info.mWifiDetectMode;
    }

    public boolean isEqual(WifiDetectConfInfo info) {
        if (this.mWifiDetectMode == info.mWifiDetectMode && this.mThreshold == info.mThreshold && this.mEnvalueCount == info.mEnvalueCount) {
            return true;
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mWifiDetectMode);
        out.writeInt(this.mThreshold);
        out.writeInt(this.mEnvalueCount);
    }
}
