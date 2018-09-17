package android.content;

import android.accounts.Account;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public class PeriodicSync implements Parcelable {
    public static final Creator<PeriodicSync> CREATOR = new Creator<PeriodicSync>() {
        public PeriodicSync createFromParcel(Parcel source) {
            return new PeriodicSync(source, null);
        }

        public PeriodicSync[] newArray(int size) {
            return new PeriodicSync[size];
        }
    };
    public final Account account;
    public final String authority;
    public final Bundle extras;
    public final long flexTime;
    public final long period;

    /* synthetic */ PeriodicSync(Parcel in, PeriodicSync -this1) {
        this(in);
    }

    public PeriodicSync(Account account, String authority, Bundle extras, long periodInSeconds) {
        this.account = account;
        this.authority = authority;
        if (extras == null) {
            this.extras = new Bundle();
        } else {
            this.extras = new Bundle(extras);
        }
        this.period = periodInSeconds;
        this.flexTime = 0;
    }

    public PeriodicSync(PeriodicSync other) {
        this.account = other.account;
        this.authority = other.authority;
        this.extras = new Bundle(other.extras);
        this.period = other.period;
        this.flexTime = other.flexTime;
    }

    public PeriodicSync(Account account, String authority, Bundle extras, long period, long flexTime) {
        this.account = account;
        this.authority = authority;
        this.extras = new Bundle(extras);
        this.period = period;
        this.flexTime = flexTime;
    }

    private PeriodicSync(Parcel in) {
        this.account = (Account) in.readParcelable(null);
        this.authority = in.readString();
        this.extras = in.readBundle();
        this.period = in.readLong();
        this.flexTime = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.account, flags);
        dest.writeString(this.authority);
        dest.writeBundle(this.extras);
        dest.writeLong(this.period);
        dest.writeLong(this.flexTime);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof PeriodicSync)) {
            return false;
        }
        PeriodicSync other = (PeriodicSync) o;
        if (this.account.equals(other.account) && this.authority.equals(other.authority) && this.period == other.period) {
            z = syncExtrasEquals(this.extras, other.extras);
        }
        return z;
    }

    public static boolean syncExtrasEquals(Bundle b1, Bundle b2) {
        if (b1.size() != b2.size()) {
            return false;
        }
        if (b1.isEmpty()) {
            return true;
        }
        for (String key : b1.keySet()) {
            if (!b2.containsKey(key)) {
                return false;
            }
            if (!Objects.equals(b1.get(key), b2.get(key))) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "account: " + this.account + ", authority: " + this.authority + ". period: " + this.period + "s " + ", flex: " + this.flexTime;
    }
}
