package android.content;

import android.accounts.Account;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class PeriodicSync implements Parcelable {
    public static final Parcelable.Creator<PeriodicSync> CREATOR = new Parcelable.Creator<PeriodicSync>() {
        /* class android.content.PeriodicSync.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PeriodicSync createFromParcel(Parcel source) {
            return new PeriodicSync(source);
        }

        @Override // android.os.Parcelable.Creator
        public PeriodicSync[] newArray(int size) {
            return new PeriodicSync[size];
        }
    };
    public final Account account;
    public final String authority;
    public final Bundle extras;
    public final long flexTime;
    public final long period;

    public PeriodicSync(Account account2, String authority2, Bundle extras2, long periodInSeconds) {
        this.account = account2;
        this.authority = authority2;
        if (extras2 == null) {
            this.extras = new Bundle();
        } else {
            this.extras = new Bundle(extras2);
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

    public PeriodicSync(Account account2, String authority2, Bundle extras2, long period2, long flexTime2) {
        this.account = account2;
        this.authority = authority2;
        this.extras = new Bundle(extras2);
        this.period = period2;
        this.flexTime = flexTime2;
    }

    private PeriodicSync(Parcel in) {
        this.account = (Account) in.readParcelable(null);
        this.authority = in.readString();
        this.extras = in.readBundle();
        this.period = in.readLong();
        this.flexTime = in.readLong();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.account, flags);
        dest.writeString(this.authority);
        dest.writeBundle(this.extras);
        dest.writeLong(this.period);
        dest.writeLong(this.flexTime);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PeriodicSync)) {
            return false;
        }
        PeriodicSync other = (PeriodicSync) o;
        if (!this.account.equals(other.account) || !this.authority.equals(other.authority) || this.period != other.period || !syncExtrasEquals(this.extras, other.extras)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0022  */
    public static boolean syncExtrasEquals(Bundle b1, Bundle b2) {
        if (b1.size() != b2.size()) {
            return false;
        }
        if (b1.isEmpty()) {
            return true;
        }
        for (String key : b1.keySet()) {
            if (!b2.containsKey(key) || !Objects.equals(b1.get(key), b2.get(key))) {
                return false;
            }
            while (r0.hasNext()) {
            }
        }
        return true;
    }

    public String toString() {
        return "account: " + this.account + ", authority: " + this.authority + ". period: " + this.period + "s , flex: " + this.flexTime;
    }
}
