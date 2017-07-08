package com.android.server.wifi.wifipro;

import android.os.Parcel;
import android.os.Parcelable;

public class HwDualBandMonitorInfo implements Parcelable {
    public int mAuthType;
    public String mBssid;
    public int mCurrentRssi;
    public WifiProDualBandApInfoRcd mDualBandApInfoRcd;
    public int mInitializationRssi;
    public int mIsDualbandAP;
    public int mIsNearAP;
    public int mScanRssi;
    public String mSsid;
    public int mTargetRssi;

    public HwDualBandMonitorInfo(String bssid, String ssid, int authtype, int targetRssi, int currentRssi, int isDualbandAP) {
        this.mTargetRssi = 0;
        this.mCurrentRssi = 0;
        this.mScanRssi = 0;
        this.mIsDualbandAP = 0;
        this.mInitializationRssi = 0;
        this.mIsNearAP = 0;
        this.mDualBandApInfoRcd = null;
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
