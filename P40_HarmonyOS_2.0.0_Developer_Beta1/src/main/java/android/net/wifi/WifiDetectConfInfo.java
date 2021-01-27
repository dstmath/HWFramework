package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiDetectConfInfo implements Parcelable {
    public static final Parcelable.Creator<WifiDetectConfInfo> CREATOR = new Parcelable.Creator<WifiDetectConfInfo>() {
        /* class android.net.wifi.WifiDetectConfInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiDetectConfInfo createFromParcel(Parcel in) {
            return new WifiDetectConfInfo(in.readInt(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public WifiDetectConfInfo[] newArray(int size) {
            return new WifiDetectConfInfo[size];
        }
    };
    public int mEnvalueCount;
    public int mThreshold;
    public int mWifiDetectMode;

    public WifiDetectConfInfo(int detectMode, int Threshold, int EnvalueCount) {
        this.mWifiDetectMode = detectMode;
        this.mThreshold = Threshold;
        this.mEnvalueCount = EnvalueCount;
    }

    public WifiDetectConfInfo(WifiDetectConfInfo info) {
        this.mWifiDetectMode = info.mWifiDetectMode;
        int i = info.mWifiDetectMode;
        this.mThreshold = i;
        this.mEnvalueCount = i;
    }

    public WifiDetectConfInfo() {
    }

    public boolean isEqual(WifiDetectConfInfo info) {
        return this.mWifiDetectMode == info.mWifiDetectMode && this.mThreshold == info.mThreshold && this.mEnvalueCount == info.mEnvalueCount;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mWifiDetectMode);
        out.writeInt(this.mThreshold);
        out.writeInt(this.mEnvalueCount);
    }
}
