package com.huawei.hwouc.plugin;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public final class OucPluginInfo implements Parcelable {
    public static final Parcelable.Creator<OucPluginInfo> CREATOR = new Parcelable.Creator<OucPluginInfo>() {
        /* class com.huawei.hwouc.plugin.OucPluginInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OucPluginInfo createFromParcel(Parcel source) {
            return new OucPluginInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public OucPluginInfo[] newArray(int size) {
            return new OucPluginInfo[size];
        }
    };
    private int installFlag;
    private String packageName;
    private List<String> pluginInstallPaths;
    private List<String> pluginNames;
    private String pluginSdk;
    private List<String> pluginVersionCodes;
    private int triggerMode;

    public OucPluginInfo() {
    }

    private OucPluginInfo(Parcel source) {
        this.packageName = source.readString();
        this.pluginSdk = source.readString();
        this.installFlag = source.readInt();
        this.triggerMode = source.readInt();
        this.pluginNames = source.createStringArrayList();
        this.pluginVersionCodes = source.createStringArrayList();
        this.pluginInstallPaths = source.createStringArrayList();
    }

    public int describeContents() {
        return 0;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getPluginSdk() {
        return this.pluginSdk;
    }

    public void setPluginSdk(String pluginSdk2) {
        this.pluginSdk = pluginSdk2;
    }

    public int getInstallFlag() {
        return this.installFlag;
    }

    public void setInstallFlag(int installFlag2) {
        this.installFlag = installFlag2;
    }

    public int getTriggerMode() {
        return this.triggerMode;
    }

    public void setTriggerMode(int triggerMode2) {
        this.triggerMode = triggerMode2;
    }

    public List<String> getPluginNames() {
        return this.pluginNames;
    }

    public void setPluginNames(List<String> pluginNames2) {
        this.pluginNames = pluginNames2;
    }

    public List<String> getPluginVersionCodes() {
        return this.pluginVersionCodes;
    }

    public void setPluginVersionCodes(List<String> pluginVersionCodes2) {
        this.pluginVersionCodes = pluginVersionCodes2;
    }

    public List<String> getPluginInstallPaths() {
        return this.pluginInstallPaths;
    }

    public void setPluginInstallPaths(List<String> pluginInstallPaths2) {
        this.pluginInstallPaths = pluginInstallPaths2;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.packageName);
        dest.writeString(this.pluginSdk);
        dest.writeInt(this.installFlag);
        dest.writeInt(this.triggerMode);
        dest.writeStringList(this.pluginNames);
        dest.writeStringList(this.pluginVersionCodes);
        dest.writeStringList(this.pluginInstallPaths);
    }
}
