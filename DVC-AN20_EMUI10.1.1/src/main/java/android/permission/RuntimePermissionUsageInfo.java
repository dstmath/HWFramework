package android.permission;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;

@SystemApi
public final class RuntimePermissionUsageInfo implements Parcelable {
    public static final Parcelable.Creator<RuntimePermissionUsageInfo> CREATOR = new Parcelable.Creator<RuntimePermissionUsageInfo>() {
        /* class android.permission.RuntimePermissionUsageInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RuntimePermissionUsageInfo createFromParcel(Parcel source) {
            return new RuntimePermissionUsageInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public RuntimePermissionUsageInfo[] newArray(int size) {
            return new RuntimePermissionUsageInfo[size];
        }
    };
    private final String mName;
    private final int mNumUsers;

    public RuntimePermissionUsageInfo(String name, int numUsers) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgumentNonnegative(numUsers);
        this.mName = name;
        this.mNumUsers = numUsers;
    }

    private RuntimePermissionUsageInfo(Parcel parcel) {
        this(parcel.readString(), parcel.readInt());
    }

    public int getAppAccessCount() {
        return this.mNumUsers;
    }

    public String getName() {
        return this.mName;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mName);
        parcel.writeInt(this.mNumUsers);
    }
}
