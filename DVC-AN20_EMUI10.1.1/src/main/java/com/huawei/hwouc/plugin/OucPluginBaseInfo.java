package com.huawei.hwouc.plugin;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public final class OucPluginBaseInfo implements Parcelable {
    public static final Parcelable.Creator<OucPluginBaseInfo> CREATOR = new Parcelable.Creator<OucPluginBaseInfo>() {
        /* class com.huawei.hwouc.plugin.OucPluginBaseInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OucPluginBaseInfo createFromParcel(Parcel source) {
            return new OucPluginBaseInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public OucPluginBaseInfo[] newArray(int size) {
            return new OucPluginBaseInfo[size];
        }
    };
    private int forceUpdate;
    private List<String> pluginCategorys;
    private String pluginName;
    private String pluginVersionCode;

    public OucPluginBaseInfo() {
    }

    private OucPluginBaseInfo(Parcel source) {
        this.pluginName = source.readString();
        this.pluginVersionCode = source.readString();
        this.forceUpdate = source.readInt();
        this.pluginCategorys = source.createStringArrayList();
    }

    public int describeContents() {
        return 0;
    }

    public String getPluginName() {
        return this.pluginName;
    }

    public void setPluginName(String pluginName2) {
        this.pluginName = pluginName2;
    }

    public String getPluginVersionCode() {
        return this.pluginVersionCode;
    }

    public void setPluginVersionCode(String pluginVersionCode2) {
        this.pluginVersionCode = pluginVersionCode2;
    }

    public int getForceUpdate() {
        return this.forceUpdate;
    }

    public void setForceUpdate(int forceUpdate2) {
        this.forceUpdate = forceUpdate2;
    }

    public List<String> getPluginCategorys() {
        return this.pluginCategorys;
    }

    public void setPluginCategorys(List<String> pluginCategorys2) {
        this.pluginCategorys = pluginCategorys2;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.pluginName);
        dest.writeString(this.pluginVersionCode);
        dest.writeInt(this.forceUpdate);
        dest.writeStringList(this.pluginCategorys);
    }
}
