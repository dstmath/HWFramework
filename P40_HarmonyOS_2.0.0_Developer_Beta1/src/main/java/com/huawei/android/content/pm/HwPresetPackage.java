package com.huawei.android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public final class HwPresetPackage implements Parcelable {
    public static final Parcelable.Creator<HwPresetPackage> CREATOR = new Parcelable.Creator<HwPresetPackage>() {
        /* class com.huawei.android.content.pm.HwPresetPackage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwPresetPackage createFromParcel(Parcel source) {
            return new HwPresetPackage(source);
        }

        @Override // android.os.Parcelable.Creator
        public HwPresetPackage[] newArray(int size) {
            int realSize;
            if (size < 0) {
                realSize = 0;
            } else if (size > 128) {
                realSize = 128;
            } else {
                realSize = size;
            }
            return new HwPresetPackage[realSize];
        }
    };
    private static final int MAX_APP_NUM = 128;
    private static final String TAG = "PresetPackage";
    private String packageName;
    private String packagePath;
    private AppType type;

    public enum AppType {
        PRIV,
        SYS,
        NOSYS
    }

    public HwPresetPackage() {
    }

    private HwPresetPackage(Parcel source) {
        this.packageName = source.readString();
        this.packagePath = source.readString();
        this.type = AppType.values()[source.readInt()];
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackagePath(String packagePath2) {
        this.packagePath = packagePath2;
    }

    public String getPackagePath() {
        return this.packagePath;
    }

    public AppType getType() {
        return this.type;
    }

    public void setType(AppType type2) {
        this.type = type2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.packageName);
        dest.writeString(this.packagePath);
        dest.writeInt(this.type.ordinal());
    }

    public String toString() {
        return "PresetPackage: packageName:" + this.packageName + " packagePath:" + this.packagePath + " type:" + this.type;
    }
}
