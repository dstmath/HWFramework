package com.huawei.security.dpermission.permissionaccessrecord.parcel;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class PermissionRecordRequestParcel implements Parcelable {
    public static final Parcelable.Creator<PermissionRecordRequestParcel> CREATOR = new Parcelable.Creator<PermissionRecordRequestParcel>() {
        /* class com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordRequestParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PermissionRecordRequestParcel createFromParcel(Parcel in) {
            return new PermissionRecordRequestParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public PermissionRecordRequestParcel[] newArray(int size) {
            return new PermissionRecordRequestParcel[size];
        }
    };
    private long beginTimeMillis;
    private String bundleLabel;
    private String bundleName;
    private String deviceName;
    private long endTimeMillis;
    private int flag;
    private List<String> permissionNames = new ArrayList();

    public PermissionRecordRequestParcel() {
    }

    public PermissionRecordRequestParcel(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.deviceName = in.readString();
        this.bundleName = in.readString();
        this.bundleLabel = in.readString();
        in.readStringList(this.permissionNames);
        this.beginTimeMillis = in.readLong();
        this.endTimeMillis = in.readLong();
        this.flag = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeString(this.bundleName);
        dest.writeString(this.bundleLabel);
        dest.writeStringList(this.permissionNames);
        dest.writeLong(this.beginTimeMillis);
        dest.writeLong(this.endTimeMillis);
        dest.writeInt(this.flag);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName2) {
        this.deviceName = deviceName2;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String bundleName2) {
        this.bundleName = bundleName2;
    }

    public String getBundleLabel() {
        return this.bundleLabel;
    }

    public void setBundleLabel(String bundleLabel2) {
        this.bundleLabel = bundleLabel2;
    }

    public List<String> getPermissionNames() {
        return this.permissionNames;
    }

    public void setPermissionNames(List<String> permissionNames2) {
        this.permissionNames = permissionNames2;
    }

    public long getBeginTimeMillis() {
        return this.beginTimeMillis;
    }

    public void setBeginTimeMillis(long beginTimeMillis2) {
        this.beginTimeMillis = beginTimeMillis2;
    }

    public long getEndTimeMillis() {
        return this.endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis2) {
        this.endTimeMillis = endTimeMillis2;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag2) {
        this.flag = flag2;
    }

    @Override // java.lang.Object
    public String toString() {
        return "PermissionRecordRequestParcel{deviceName='" + this.deviceName + "', bundleName='" + this.bundleName + "', bundleLabel='" + this.bundleLabel + "', permissionNames=" + this.permissionNames + ", beginTimeMillis=" + this.beginTimeMillis + ", endTimeMillis=" + this.endTimeMillis + ", flag=" + this.flag + '}';
    }
}
