package com.huawei.softnet.connect;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class ModuleIdentifier implements Parcelable {
    public static final Parcelable.Creator<ModuleIdentifier> CREATOR = new Parcelable.Creator<ModuleIdentifier>() {
        /* class com.huawei.softnet.connect.ModuleIdentifier.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ModuleIdentifier createFromParcel(Parcel in) {
            return new ModuleIdentifier(in);
        }

        @Override // android.os.Parcelable.Creator
        public ModuleIdentifier[] newArray(int size) {
            return new ModuleIdentifier[size];
        }
    };
    private static final int PARCEL_FLAG = 0;
    private String mModuleId;

    protected ModuleIdentifier(Parcel in) {
        this.mModuleId = in.readString();
    }

    public ModuleIdentifier(Context context) {
        this.mModuleId = context.getPackageName();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mModuleId);
    }

    public String getModuleId() {
        return this.mModuleId;
    }
}
