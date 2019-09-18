package com.huawei.android.view;

import android.app.ActivityManager;
import android.os.Parcel;
import android.os.Parcelable;

public class HwTaskSnapshotWrapper implements Parcelable {
    public static final Parcelable.Creator<HwTaskSnapshotWrapper> CREATOR = new Parcelable.Creator<HwTaskSnapshotWrapper>() {
        public HwTaskSnapshotWrapper createFromParcel(Parcel in) {
            return new HwTaskSnapshotWrapper(in);
        }

        public HwTaskSnapshotWrapper[] newArray(int size) {
            return new HwTaskSnapshotWrapper[size];
        }
    };
    public ActivityManager.TaskSnapshot mTaskSnapshot;

    public HwTaskSnapshotWrapper() {
    }

    public HwTaskSnapshotWrapper(Parcel in) {
        this.mTaskSnapshot = in.readParcelable(null);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mTaskSnapshot == null) {
            dest.writeParcelable(null, 0);
        } else {
            dest.writeParcelable(this.mTaskSnapshot, 0);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.mTaskSnapshot = in.readParcelable(null);
    }

    public String toString() {
        if (this.mTaskSnapshot != null) {
            return this.mTaskSnapshot.toString();
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
