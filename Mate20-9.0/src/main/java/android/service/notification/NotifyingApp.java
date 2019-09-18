package android.service.notification;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import java.util.Objects;

public final class NotifyingApp implements Parcelable, Comparable<NotifyingApp> {
    public static final Parcelable.Creator<NotifyingApp> CREATOR = new Parcelable.Creator<NotifyingApp>() {
        public NotifyingApp createFromParcel(Parcel in) {
            return new NotifyingApp(in);
        }

        public NotifyingApp[] newArray(int size) {
            return new NotifyingApp[size];
        }
    };
    private long mLastNotified;
    private String mPkg;
    private int mUid;

    public NotifyingApp() {
    }

    protected NotifyingApp(Parcel in) {
        this.mUid = in.readInt();
        this.mPkg = in.readString();
        this.mLastNotified = in.readLong();
    }

    public int getUid() {
        return this.mUid;
    }

    public NotifyingApp setUid(int mUid2) {
        this.mUid = mUid2;
        return this;
    }

    public String getPackage() {
        return this.mPkg;
    }

    public NotifyingApp setPackage(String mPkg2) {
        this.mPkg = mPkg2;
        return this;
    }

    public long getLastNotified() {
        return this.mLastNotified;
    }

    public NotifyingApp setLastNotified(long mLastNotified2) {
        this.mLastNotified = mLastNotified2;
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUid);
        dest.writeString(this.mPkg);
        dest.writeLong(this.mLastNotified);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotifyingApp that = (NotifyingApp) o;
        if (!(getUid() == that.getUid() && getLastNotified() == that.getLastNotified() && Objects.equals(this.mPkg, that.mPkg))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(getUid()), this.mPkg, Long.valueOf(getLastNotified())});
    }

    public int compareTo(NotifyingApp o) {
        if (getLastNotified() != o.getLastNotified()) {
            return -Long.compare(getLastNotified(), o.getLastNotified());
        }
        if (getUid() == o.getUid()) {
            return getPackage().compareTo(o.getPackage());
        }
        return Integer.compare(getUid(), o.getUid());
    }

    public String toString() {
        return "NotifyingApp{mUid=" + this.mUid + ", mPkg='" + this.mPkg + DateFormat.QUOTE + ", mLastNotified=" + this.mLastNotified + '}';
    }
}
