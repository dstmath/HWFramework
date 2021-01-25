package com.huawei.pluginmanager;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public final class CloudPluginInfo implements Parcelable {
    public static final Parcelable.Creator<CloudPluginInfo> CREATOR = new Parcelable.Creator<CloudPluginInfo>() {
        /* class com.huawei.pluginmanager.CloudPluginInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CloudPluginInfo createFromParcel(Parcel source) {
            return new CloudPluginInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public CloudPluginInfo[] newArray(int size) {
            return new CloudPluginInfo[size];
        }
    };
    private static final String TAG = "CloudPluginQuery";
    private boolean isForceUpdate;
    private List<String> pluginCategorys;
    private String pluginName;
    private int versionCode;

    public CloudPluginInfo() {
    }

    private CloudPluginInfo(Parcel source) {
        this.pluginName = source.readString();
        this.versionCode = source.readInt();
        this.isForceUpdate = source.readBoolean();
        this.pluginCategorys = source.createStringArrayList();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getPluginName() {
        return this.pluginName;
    }

    public void setPluginName(String pluginName2) {
        this.pluginName = pluginName2;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(int versionCode2) {
        this.versionCode = versionCode2;
    }

    public boolean isForceUpdate() {
        return this.isForceUpdate;
    }

    public void setForceUpdate(boolean isForceUpdate2) {
        this.isForceUpdate = isForceUpdate2;
    }

    public List<String> getPluginCategory() {
        return this.pluginCategorys;
    }

    public void setPluginCategory(List<String> pluginCategorys2) {
        this.pluginCategorys = pluginCategorys2;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.pluginName);
        dest.writeInt(this.versionCode);
        dest.writeBoolean(this.isForceUpdate);
        dest.writeStringList(this.pluginCategorys);
    }
}
