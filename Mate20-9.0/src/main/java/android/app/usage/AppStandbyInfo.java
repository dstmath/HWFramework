package android.app.usage;

import android.os.Parcel;
import android.os.Parcelable;

public final class AppStandbyInfo implements Parcelable {
    public static final Parcelable.Creator<AppStandbyInfo> CREATOR = new Parcelable.Creator<AppStandbyInfo>() {
        public AppStandbyInfo createFromParcel(Parcel source) {
            return new AppStandbyInfo(source);
        }

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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mStandbyBucket);
    }
}
