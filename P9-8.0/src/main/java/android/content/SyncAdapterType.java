package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public class SyncAdapterType implements Parcelable {
    public static final Creator<SyncAdapterType> CREATOR = new Creator<SyncAdapterType>() {
        public SyncAdapterType createFromParcel(Parcel source) {
            return new SyncAdapterType(source);
        }

        public SyncAdapterType[] newArray(int size) {
            return new SyncAdapterType[size];
        }
    };
    public final String accountType;
    private final boolean allowParallelSyncs;
    public final String authority;
    private final boolean isAlwaysSyncable;
    public final boolean isKey;
    private final String packageName;
    private final String settingsActivity;
    private final boolean supportsUploading;
    private final boolean userVisible;

    public SyncAdapterType(String authority, String accountType, boolean userVisible, boolean supportsUploading) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority);
        } else if (TextUtils.isEmpty(accountType)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType);
        } else {
            this.authority = authority;
            this.accountType = accountType;
            this.userVisible = userVisible;
            this.supportsUploading = supportsUploading;
            this.isAlwaysSyncable = false;
            this.allowParallelSyncs = false;
            this.settingsActivity = null;
            this.isKey = false;
            this.packageName = null;
        }
    }

    public SyncAdapterType(String authority, String accountType, boolean userVisible, boolean supportsUploading, boolean isAlwaysSyncable, boolean allowParallelSyncs, String settingsActivity, String packageName) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority);
        } else if (TextUtils.isEmpty(accountType)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType);
        } else {
            this.authority = authority;
            this.accountType = accountType;
            this.userVisible = userVisible;
            this.supportsUploading = supportsUploading;
            this.isAlwaysSyncable = isAlwaysSyncable;
            this.allowParallelSyncs = allowParallelSyncs;
            this.settingsActivity = settingsActivity;
            this.isKey = false;
            this.packageName = packageName;
        }
    }

    private SyncAdapterType(String authority, String accountType) {
        if (TextUtils.isEmpty(authority)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority);
        } else if (TextUtils.isEmpty(accountType)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType);
        } else {
            this.authority = authority;
            this.accountType = accountType;
            this.userVisible = true;
            this.supportsUploading = true;
            this.isAlwaysSyncable = false;
            this.allowParallelSyncs = false;
            this.settingsActivity = null;
            this.isKey = true;
            this.packageName = null;
        }
    }

    public boolean supportsUploading() {
        if (!this.isKey) {
            return this.supportsUploading;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean isUserVisible() {
        if (!this.isKey) {
            return this.userVisible;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean allowParallelSyncs() {
        if (!this.isKey) {
            return this.allowParallelSyncs;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean isAlwaysSyncable() {
        if (!this.isKey) {
            return this.isAlwaysSyncable;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public String getSettingsActivity() {
        if (!this.isKey) {
            return this.settingsActivity;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public String getPackageName() {
        return this.packageName;
    }

    public static SyncAdapterType newKey(String authority, String accountType) {
        return new SyncAdapterType(authority, accountType);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof SyncAdapterType)) {
            return false;
        }
        SyncAdapterType other = (SyncAdapterType) o;
        if (this.authority.equals(other.authority)) {
            z = this.accountType.equals(other.accountType);
        }
        return z;
    }

    public int hashCode() {
        return ((this.authority.hashCode() + 527) * 31) + this.accountType.hashCode();
    }

    public String toString() {
        if (this.isKey) {
            return "SyncAdapterType Key {name=" + this.authority + ", type=" + this.accountType + "}";
        }
        return "SyncAdapterType {name=" + this.authority + ", type=" + this.accountType + ", userVisible=" + this.userVisible + ", supportsUploading=" + this.supportsUploading + ", isAlwaysSyncable=" + this.isAlwaysSyncable + ", allowParallelSyncs=" + this.allowParallelSyncs + ", settingsActivity=" + this.settingsActivity + ", packageName=" + this.packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        if (this.isKey) {
            throw new IllegalStateException("keys aren't parcelable");
        }
        int i2;
        dest.writeString(this.authority);
        dest.writeString(this.accountType);
        dest.writeInt(this.userVisible ? 1 : 0);
        if (this.supportsUploading) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (this.isAlwaysSyncable) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (!this.allowParallelSyncs) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.settingsActivity);
        dest.writeString(this.packageName);
    }

    public SyncAdapterType(Parcel source) {
        boolean z;
        boolean z2;
        boolean z3 = false;
        String readString = source.readString();
        String readString2 = source.readString();
        boolean z4 = source.readInt() != 0;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        if (source.readInt() != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        if (source.readInt() != 0) {
            z3 = true;
        }
        this(readString, readString2, z4, z, z2, z3, source.readString(), source.readString());
    }
}
