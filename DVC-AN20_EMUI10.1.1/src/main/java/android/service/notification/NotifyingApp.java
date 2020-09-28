package android.service.notification;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import java.util.Objects;

public final class NotifyingApp implements Parcelable, Comparable<NotifyingApp> {
    public static final Parcelable.Creator<NotifyingApp> CREATOR = new Parcelable.Creator<NotifyingApp>() {
        /* class android.service.notification.NotifyingApp.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NotifyingApp createFromParcel(Parcel in) {
            return new NotifyingApp(in);
        }

        @Override // android.os.Parcelable.Creator
        public NotifyingApp[] newArray(int size) {
            return new NotifyingApp[size];
        }
    };
    private long mLastNotified;
    private String mPkg;
    private int mUserId;

    public NotifyingApp() {
    }

    protected NotifyingApp(Parcel in) {
        this.mUserId = in.readInt();
        this.mPkg = in.readString();
        this.mLastNotified = in.readLong();
    }

    public int getUserId() {
        return this.mUserId;
    }

    public NotifyingApp setUserId(int mUserId2) {
        this.mUserId = mUserId2;
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUserId);
        dest.writeString(this.mPkg);
        dest.writeLong(this.mLastNotified);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotifyingApp that = (NotifyingApp) o;
        if (getUserId() == that.getUserId() && getLastNotified() == that.getLastNotified() && Objects.equals(this.mPkg, that.mPkg)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(getUserId()), this.mPkg, Long.valueOf(getLastNotified()));
    }

    public int compareTo(NotifyingApp o) {
        if (getLastNotified() != o.getLastNotified()) {
            return -Long.compare(getLastNotified(), o.getLastNotified());
        }
        if (getUserId() == o.getUserId()) {
            return getPackage().compareTo(o.getPackage());
        }
        return Integer.compare(getUserId(), o.getUserId());
    }

    public String toString() {
        return "NotifyingApp{mUserId=" + this.mUserId + ", mPkg='" + this.mPkg + DateFormat.QUOTE + ", mLastNotified=" + this.mLastNotified + '}';
    }
}
