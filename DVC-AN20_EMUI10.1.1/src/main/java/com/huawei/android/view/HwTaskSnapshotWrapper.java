package com.huawei.android.view;

import android.app.ActivityManager;
import android.os.Parcel;
import android.os.Parcelable;

public class HwTaskSnapshotWrapper implements Parcelable {
    public static final Parcelable.Creator<HwTaskSnapshotWrapper> CREATOR = new Parcelable.Creator<HwTaskSnapshotWrapper>() {
        /* class com.huawei.android.view.HwTaskSnapshotWrapper.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwTaskSnapshotWrapper createFromParcel(Parcel in) {
            return new HwTaskSnapshotWrapper(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwTaskSnapshotWrapper[] newArray(int size) {
            return new HwTaskSnapshotWrapper[size];
        }
    };
    public ActivityManager.TaskSnapshot mTaskSnapshot;

    public HwTaskSnapshotWrapper() {
    }

    public HwTaskSnapshotWrapper(Parcel in) {
        this.mTaskSnapshot = (ActivityManager.TaskSnapshot) in.readParcelable(null);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        ActivityManager.TaskSnapshot taskSnapshot = this.mTaskSnapshot;
        if (taskSnapshot == null) {
            dest.writeParcelable(null, 0);
        } else {
            dest.writeParcelable(taskSnapshot, 0);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.mTaskSnapshot = (ActivityManager.TaskSnapshot) in.readParcelable(null);
    }

    public String toString() {
        ActivityManager.TaskSnapshot taskSnapshot = this.mTaskSnapshot;
        if (taskSnapshot != null) {
            return taskSnapshot.toString();
        }
        return null;
    }

    public void setTaskSnapshot(ActivityManager.TaskSnapshot taskSnapshot) {
        this.mTaskSnapshot = taskSnapshot;
    }

    public ActivityManager.TaskSnapshot getTaskSnapshot() {
        return this.mTaskSnapshot;
    }
}
