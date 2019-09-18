package android.content;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;

public class SyncInfo implements Parcelable {
    public static final Parcelable.Creator<SyncInfo> CREATOR = new Parcelable.Creator<SyncInfo>() {
        public SyncInfo createFromParcel(Parcel in) {
            return new SyncInfo(in);
        }

        public SyncInfo[] newArray(int size) {
            return new SyncInfo[size];
        }
    };
    private static final Account REDACTED_ACCOUNT = new Account("*****", "*****");
    public final Account account;
    public final String authority;
    public final int authorityId;
    public final long startTime;

    public static SyncInfo createAccountRedacted(int authorityId2, String authority2, long startTime2) {
        SyncInfo syncInfo = new SyncInfo(authorityId2, REDACTED_ACCOUNT, authority2, startTime2);
        return syncInfo;
    }

    public SyncInfo(int authorityId2, Account account2, String authority2, long startTime2) {
        this.authorityId = authorityId2;
        this.account = account2;
        this.authority = authority2;
        this.startTime = startTime2;
    }

    public SyncInfo(SyncInfo other) {
        this.authorityId = other.authorityId;
        this.account = new Account(other.account.name, other.account.type);
        this.authority = other.authority;
        this.startTime = other.startTime;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.authorityId);
        parcel.writeParcelable(this.account, flags);
        parcel.writeString(this.authority);
        parcel.writeLong(this.startTime);
    }

    SyncInfo(Parcel parcel) {
        this.authorityId = parcel.readInt();
        this.account = (Account) parcel.readParcelable(Account.class.getClassLoader());
        this.authority = parcel.readString();
        this.startTime = parcel.readLong();
    }
}
