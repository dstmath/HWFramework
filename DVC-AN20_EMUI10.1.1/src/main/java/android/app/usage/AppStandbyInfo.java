package android.app.usage;

import android.os.Parcel;
import android.os.Parcelable;

public final class AppStandbyInfo implements Parcelable {
    public static final Parcelable.Creator<AppStandbyInfo> CREATOR = new Parcelable.Creator<AppStandbyInfo>() {
        /* class android.app.usage.AppStandbyInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppStandbyInfo createFromParcel(Parcel source) {
            return new AppStandbyInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public AppStandbyInfo[] newArray(int size) {
            return new AppStandbyInfo[size];
        }
    };
    public String mPackageName;
    public int mStandbyBucket;

    private AppStandbyInfo(Parcel in) {
        this.mPackageName = in.readString();
        this.mStandbyBucket = in.readInt();
    }

    public AppStandbyInfo(String packageName, int bucket) {
        this.mPackageName = packageName;
        this.mStandbyBucket = bucket;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mStandbyBucket);
    }
}
