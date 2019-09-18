package com.android.internal.statusbar;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayDeque;

public class NotificationVisibility implements Parcelable {
    public static final Parcelable.Creator<NotificationVisibility> CREATOR = new Parcelable.Creator<NotificationVisibility>() {
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
    private static ArrayDeque<NotificationVisibility> sPool = new ArrayDeque<>(25);
    public int count;
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

    private NotificationVisibility(String key2, int rank2, int count2, boolean visibile) {
        this();
        this.key = key2;
        this.rank = rank2;
        this.count = count2;
        this.visible = visibile;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NotificationVisibility(id=");
        sb.append(this.id);
        sb.append(" key=");
        sb.append(this.key);
        sb.append(" rank=");
        sb.append(this.rank);
        sb.append(" count=");
        sb.append(this.count);
        sb.append(this.visible ? " visible" : "");
        sb.append(" )");
        return sb.toString();
    }

    public NotificationVisibility clone() {
        return obtain(this.key, this.rank, this.count, this.visible);
    }

    public int hashCode() {
        if (this.key == null) {
            return 0;
        }
        return this.key.hashCode();
    }

    public boolean equals(Object that) {
        boolean z = false;
        if (!(that instanceof NotificationVisibility)) {
            return false;
        }
        NotificationVisibility thatViz = (NotificationVisibility) that;
        if ((this.key == null && thatViz.key == null) || this.key.equals(thatViz.key)) {
            z = true;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.key);
        out.writeInt(this.rank);
        out.writeInt(this.count);
        out.writeInt(this.visible ? 1 : 0);
    }

    private void readFromParcel(Parcel in) {
        this.key = in.readString();
        this.rank = in.readInt();
        this.count = in.readInt();
        this.visible = in.readInt() != 0;
    }

    public static NotificationVisibility obtain(String key2, int rank2, int count2, boolean visible2) {
        NotificationVisibility vo = obtain();
        vo.key = key2;
        vo.rank = rank2;
        vo.count = count2;
        vo.visible = visible2;
        return vo;
    }

    /* access modifiers changed from: private */
    public static NotificationVisibility obtain(Parcel in) {
        NotificationVisibility vo = obtain();
        vo.readFromParcel(in);
        return vo;
    }

    private static NotificationVisibility obtain() {
        synchronized (sPool) {
            if (sPool.isEmpty()) {
                return new NotificationVisibility();
            }
            NotificationVisibility poll = sPool.poll();
            return poll;
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
