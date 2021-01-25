package com.huawei.security.dpermission.permissionaccessrecord.parcel;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class BundlePermissionRecordParcel implements Parcelable {
    public static final Parcelable.Creator<BundlePermissionRecordParcel> CREATOR = new Parcelable.Creator<BundlePermissionRecordParcel>() {
        /* class com.huawei.security.dpermission.permissionaccessrecord.parcel.BundlePermissionRecordParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BundlePermissionRecordParcel createFromParcel(Parcel in) {
            return new BundlePermissionRecordParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public BundlePermissionRecordParcel[] newArray(int size) {
            return new BundlePermissionRecordParcel[size];
        }
    };
    private String bundleLabel;
    private String bundleName;
    private String deviceName;
    private List<PermissionRecordParcel> permissionRecords = new ArrayList();
    private int uid;

    public BundlePermissionRecordParcel() {
    }

    public BundlePermissionRecordParcel(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.deviceName = in.readString();
        this.uid = in.readInt();
        this.bundleName = in.readString();
        this.bundleLabel = in.readString();
        in.readParcelableList(this.permissionRecords, PermissionRecordParcel.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeInt(this.uid);
        dest.writeString(this.bundleName);
        dest.writeString(this.bundleLabel);
        dest.writeParcelableList(this.permissionRecords, flags);
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

    public int getUid() {
        return this.uid;
    }

    public void setUid(int uid2) {
        this.uid = uid2;
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

    public List<PermissionRecordParcel> getPermissionRecords() {
        return this.permissionRecords;
    }

    public void setPermissionRecords(List<PermissionRecordParcel> permissionRecords2) {
        this.permissionRecords = permissionRecords2;
    }

    @Override // java.lang.Object
    public String toString() {
        return "BundlePermissionRecordParcel{deviceName='" + this.deviceName + "', uid=" + this.uid + ", bundleName='" + this.bundleName + "', bundleLabel='" + this.bundleLabel + "', permissionRecords=" + this.permissionRecords + '}';
    }
}
