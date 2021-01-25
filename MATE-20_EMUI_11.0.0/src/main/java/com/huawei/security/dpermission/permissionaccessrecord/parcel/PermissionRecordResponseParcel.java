package com.huawei.security.dpermission.permissionaccessrecord.parcel;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class PermissionRecordResponseParcel implements Parcelable {
    public static final Parcelable.Creator<PermissionRecordResponseParcel> CREATOR = new Parcelable.Creator<PermissionRecordResponseParcel>() {
        /* class com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordResponseParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PermissionRecordResponseParcel createFromParcel(Parcel in) {
            return new PermissionRecordResponseParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public PermissionRecordResponseParcel[] newArray(int size) {
            return new PermissionRecordResponseParcel[size];
        }
    };
    private long beginTimeMillis;
    private List<BundlePermissionRecordParcel> bundlePermissionRecords = new ArrayList();
    private long endTimeMillis;

    public PermissionRecordResponseParcel() {
    }

    public PermissionRecordResponseParcel(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.beginTimeMillis = in.readLong();
        this.endTimeMillis = in.readLong();
        in.readParcelableList(this.bundlePermissionRecords, BundlePermissionRecordParcel.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.beginTimeMillis);
        dest.writeLong(this.endTimeMillis);
        dest.writeParcelableList(this.bundlePermissionRecords, flags);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
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

    public List<BundlePermissionRecordParcel> getBundlePermissionRecords() {
        return this.bundlePermissionRecords;
    }

    public void setBundlePermissionRecords(List<BundlePermissionRecordParcel> bundlePermissionRecords2) {
        this.bundlePermissionRecords = bundlePermissionRecords2;
    }

    @Override // java.lang.Object
    public String toString() {
        return "PermissionRecordResponseParcel{beginTimeMillis=" + this.beginTimeMillis + ", endTimeMillis=" + this.endTimeMillis + ", bundlePermissionRecords=" + this.bundlePermissionRecords + '}';
    }
}
