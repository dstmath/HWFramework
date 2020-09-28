package com.huawei.hwouc.plugin;

import android.os.Parcel;
import android.os.Parcelable;

public final class OucPluginDetailInfo implements Parcelable {
    public static final Parcelable.Creator<OucPluginDetailInfo> CREATOR = new Parcelable.Creator<OucPluginDetailInfo>() {
        /* class com.huawei.hwouc.plugin.OucPluginDetailInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OucPluginDetailInfo createFromParcel(Parcel source) {
            return new OucPluginDetailInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public OucPluginDetailInfo[] newArray(int size) {
            return new OucPluginDetailInfo[size];
        }
    };
    private String changelogSha256;
    private String changelogUrl;
    private String mediaSha256;
    private long mediaSize;
    private String mediaUrl;
    private long packageSize;
    private int packageType;
    private String pluginName;
    private String versionCode;

    public OucPluginDetailInfo() {
    }

    private OucPluginDetailInfo(Parcel source) {
        this.packageType = source.readInt();
        this.pluginName = source.readString();
        this.versionCode = source.readString();
        this.packageSize = source.readLong();
        this.changelogUrl = source.readString();
        this.changelogSha256 = source.readString();
        this.mediaUrl = source.readString();
        this.mediaSize = source.readLong();
        this.mediaSha256 = source.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getChangelogSha256() {
        return this.changelogSha256;
    }

    public void setChangelogSha256(String changelogSha2562) {
        this.changelogSha256 = changelogSha2562;
    }

    public int getPackageType() {
        return this.packageType;
    }

    public void setPackageType(int packageType2) {
        this.packageType = packageType2;
    }

    public String getPluginName() {
        return this.pluginName;
    }

    public void setPluginName(String pluginName2) {
        this.pluginName = pluginName2;
    }

    public String getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(String versionCode2) {
        this.versionCode = versionCode2;
    }

    public long getPackageSize() {
        return this.packageSize;
    }

    public void setPackageSize(long packageSize2) {
        this.packageSize = packageSize2;
    }

    public String getChangelogUrl() {
        return this.changelogUrl;
    }

    public void setChangelogUrl(String changelogUrl2) {
        this.changelogUrl = changelogUrl2;
    }

    public String getMediaUrl() {
        return this.mediaUrl;
    }

    public void setMediaUrl(String mediaUrl2) {
        this.mediaUrl = mediaUrl2;
    }

    public long getMediaSize() {
        return this.mediaSize;
    }

    public void setMediaSize(long mediaSize2) {
        this.mediaSize = mediaSize2;
    }

    public String getMediaSha256() {
        return this.mediaSha256;
    }

    public void setMediaSha256(String mediaSha2562) {
        this.mediaSha256 = mediaSha2562;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.packageType);
        dest.writeString(this.pluginName);
        dest.writeString(this.versionCode);
        dest.writeLong(this.packageSize);
        dest.writeString(this.changelogUrl);
        dest.writeString(this.changelogSha256);
        dest.writeString(this.mediaUrl);
        dest.writeLong(this.mediaSize);
        dest.writeString(this.mediaSha256);
    }
}
