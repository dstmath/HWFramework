package com.huawei.coauthservice.identitymgr.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.coauthservice.identitymgr.utils.HwDeviceUtils;
import com.huawei.hwpartsecurity.BuildConfig;

public class DeviceInfo implements Parcelable {
    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
        /* class com.huawei.coauthservice.identitymgr.model.DeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };
    private String deviceId;
    private String ip;
    private LinkType linkType;

    public DeviceInfo() {
    }

    protected DeviceInfo(Parcel in) {
        if (in != null) {
            this.linkType = getLinkTypeOrDefault((LinkType) in.readParcelable(LinkType.class.getClassLoader()));
            this.deviceId = getStringOrDefault(in.readString());
            this.ip = getStringOrDefault(in.readString());
        }
    }

    public LinkType getLinkType() {
        return this.linkType;
    }

    public void setLinkType(LinkType linkType2) {
        this.linkType = linkType2;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setIp(String ip2) {
        this.ip = ip2;
    }

    public String getIp() {
        return this.ip;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.linkType, flags);
        dest.writeString(this.deviceId);
        dest.writeString(this.ip);
    }

    @Override // java.lang.Object
    public String toString() {
        return "DeviceInfo{linkType=" + this.linkType + ", deviceId='" + HwDeviceUtils.maskDeviceId(this.deviceId) + "', ip='" + HwDeviceUtils.maskDeviceIp(this.ip) + "'}";
    }

    private String getStringOrDefault(String readString) {
        return readString == null ? BuildConfig.FLAVOR : readString;
    }

    private LinkType getLinkTypeOrDefault(LinkType readLinkType) {
        if (readLinkType != null) {
            return readLinkType;
        }
        return LinkType.AP;
    }
}
