package com.android.server.wifi.wifipro;

import android.os.Parcel;
import android.os.Parcelable;

public class HwDualBandMonitorInfo implements Parcelable {
    public static final Parcelable.Creator<HwDualBandMonitorInfo> CREATOR = new Parcelable.Creator<HwDualBandMonitorInfo>() {
        public HwDualBandMonitorInfo createFromParcel(Parcel source) {
            String bssid = source.readString();
            String ssid = source.readString();
            int authType = source.readInt();
            int targetRssi = source.readInt();
            int currentRssi = source.readInt();
            int scanRssi = source.readInt();
            int isDulband = source.readInt();
            int isNearAp = source.readInt();
            int initRssi = source.readInt();
            HwDualBandMonitorInfo result = new HwDualBandMonitorInfo(bssid, ssid, authType, targetRssi, currentRssi, isDulband);
            result.mScanRssi = scanRssi;
            result.mInitializationRssi = initRssi;
            result.mIsNearAP = isNearAp;
            return result;
        }

        public HwDualBandMonitorInfo[] newArray(int size) {
            return new HwDualBandMonitorInfo[size];
        }
    };
    public int mAuthType;
    public String mBssid;
    public int mCurrentRssi = 0;
    public WifiProDualBandApInfoRcd mDualBandApInfoRcd = null;
    public int mInitializationRssi = 0;
    public int mIsDualbandAP = 0;
    public int mIsNearAP = 0;
    public int mScanRssi = 0;
    public String mSsid;
    public int mTargetRssi = 0;

    public HwDualBandMonitorInfo(String bssid, String ssid, int authtype, int targetRssi, int currentRssi, int isDualbandAP) {
        this.mBssid = bssid;
        this.mSsid = ssid;
        this.mAuthType = authtype;
        this.mTargetRssi = targetRssi;
        this.mCurrentRssi = currentRssi;
        this.mIsDualbandAP = isDualbandAP;
        this.mInitializationRssi = 0;
        this.mDualBandApInfoRcd = HwDualBandInformationManager.getInstance().getDualBandAPInfo(bssid);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mBssid);
        dest.writeString(this.mSsid);
        dest.writeInt(this.mAuthType);
        dest.writeInt(this.mTargetRssi);
        dest.writeInt(this.mCurrentRssi);
        dest.writeInt(this.mScanRssi);
        dest.writeInt(this.mIsDualbandAP);
        dest.writeInt(this.mIsNearAP);
        dest.writeInt(this.mInitializationRssi);
    }

    public int describeContents() {
        return 0;
    }
}
