package com.huawei.hwwifiproservice;

import android.os.Parcel;
import android.os.Parcelable;

public class HwDualBandMonitorInfo implements Parcelable {
    public static final Parcelable.Creator<HwDualBandMonitorInfo> CREATOR = new Parcelable.Creator<HwDualBandMonitorInfo>() {
        /* class com.huawei.hwwifiproservice.HwDualBandMonitorInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwDualBandMonitorInfo createFromParcel(Parcel source) {
            String bssid = source.readString();
            String ssid = source.readString();
            int authType = source.readInt();
            int targetRssi = source.readInt();
            int currentRssi = source.readInt();
            int scanRssi = source.readInt();
            int dulbandInfo = source.readInt();
            int nearApInfo = source.readInt();
            int initRssi = source.readInt();
            HwDualBandMonitorInfo result = new HwDualBandMonitorInfo(bssid, ssid, authType, targetRssi, currentRssi, dulbandInfo);
            result.mScanRssi = scanRssi;
            result.mInitializationRssi = initRssi;
            result.mIsNearAP = nearApInfo;
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public HwDualBandMonitorInfo[] newArray(int size) {
            return new HwDualBandMonitorInfo[size];
        }
    };
    public int mAuthType;
    public String mBssid;
    public int mCurrentRssi = 0;
    public WifiProDualBandApInfoRcd mDualBandApInfoRcd = null;
    public int mInitializationRssi = 0;
    public int mIsDualbandAp = 0;
    public int mIsNearAP = 0;
    public int mScanRssi = 0;
    public String mSsid;
    public int mTargetRssi = 0;

    public HwDualBandMonitorInfo(String bssid, String ssid, int authType, int targetRssi, int currentRssi, int isDualbandAp) {
        this.mBssid = bssid;
        this.mSsid = ssid;
        this.mAuthType = authType;
        this.mTargetRssi = targetRssi;
        this.mCurrentRssi = currentRssi;
        this.mIsDualbandAp = isDualbandAp;
        this.mInitializationRssi = 0;
        this.mDualBandApInfoRcd = HwDualBandInformationManager.getInstance().getDualBandAPInfo(bssid);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mBssid);
        dest.writeString(this.mSsid);
        dest.writeInt(this.mAuthType);
        dest.writeInt(this.mTargetRssi);
        dest.writeInt(this.mCurrentRssi);
        dest.writeInt(this.mScanRssi);
        dest.writeInt(this.mIsDualbandAp);
        dest.writeInt(this.mIsNearAP);
        dest.writeInt(this.mInitializationRssi);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
