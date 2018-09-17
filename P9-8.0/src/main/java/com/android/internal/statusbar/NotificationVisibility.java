package com.android.internal.statusbar;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LogException;
import java.util.ArrayDeque;

public class NotificationVisibility implements Parcelable {
    public static final Creator<NotificationVisibility> CREATOR = new Creator<NotificationVisibility>() {
        public NotificationVisibility createFromParcel(Parcel parcel) {
            return NotificationVisibility.obtain(parcel);
        }

        public NotificationVisibility[] newArray(int size) {
            return new NotificationVisibility[size];
        }
    };
    private static final int MAX_POOL_SIZE = 25;
    private static final String TAG = "NoViz";
    private static int sNexrId = 0;
    private static ArrayDeque<NotificationVisibility> sPool = new ArrayDeque(25);
    int id;
    public String key;
    public int rank;
    public boolean visible;

    private NotificationVisibility() {
        this.visible = true;
        int i = sNexrId;
        sNexrId = i + 1;
        this.id = i;
    }

    private NotificationVisibility(String key, int rank, boolean visibile) {
        this();
        this.key = key;
        this.rank = rank;
        this.visible = visibile;
    }

    public String toString() {
        return "NotificationVisibility(id=" + this.id + "key=" + this.key + " rank=" + this.rank + (this.visible ? " visible" : LogException.NO_VALUE) + " )";
    }

    public NotificationVisibility clone() {
        return obtain(this.key, this.rank, this.visible);
    }

    public int hashCode() {
        return this.key == null ? 0 : this.key.hashCode();
    }

    public boolean equals(Object that) {
        if (!(that instanceof NotificationVisibility)) {
            return false;
        }
        NotificationVisibility thatViz = (NotificationVisibility) that;
        boolean equals = (this.key == null && thatViz.key == null) ? true : this.key.equals(thatViz.key);
        return equals;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.key);
        out.writeInt(this.rank);
        out.writeInt(this.visible ? 1 : 0);
    }

    private void readFromParcel(Parcel in) {
        boolean z = false;
        this.key = in.readString();
        this.rank = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        }
        this.visible = z;
    }

    public static NotificationVisibility obtain(String key, int rank, boolean visible) {
        NotificationVisibility vo = obtain();
        vo.key = key;
        vo.rank = rank;
        vo.visible = visible;
        return vo;
    }

    private static NotificationVisibility obtain(Parcel in) {
        NotificationVisibility vo = obtain();
        vo.readFromParcel(in);
        return vo;
    }

    private static NotificationVisibility obtain() {
        synchronized (sPool) {
            if (sPool.isEmpty()) {
                return new NotificationVisibility();
            }
            NotificationVisibility notificationVisibility = (NotificationVisibility) sPool.poll();
            return notificationVisibility;
        }
    }

    public void recycle() {
        if (this.key != null) {
            this.key = null;
            if (sPool.size() < 25) {
                synchronized (sPool) {
                    sPool.offer(this);
                }
            }
        }
    }
}
