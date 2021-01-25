package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;

public class AppPreloadInfo implements Parcelable {
    public static final Parcelable.Creator<AppPreloadInfo> CREATOR = new Parcelable.Creator<AppPreloadInfo>() {
        /* class android.rms.iaware.AppPreloadInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppPreloadInfo createFromParcel(Parcel source) {
            return new AppPreloadInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public AppPreloadInfo[] newArray(int size) {
            return new AppPreloadInfo[size];
        }
    };
    private int appAttribute;
    private String packageName;

    public AppPreloadInfo() {
    }

    public AppPreloadInfo(Parcel source) {
        this.packageName = source.readString();
        this.appAttribute = source.readInt();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public void setAppAttribute(int appAttribute2) {
        this.appAttribute = appAttribute2;
    }

    public int getAppAttribute() {
        return this.appAttribute;
    }

    @Override // java.lang.Object
    public String toString() {
        return "AppPreloadInfo [packageName=" + this.packageName + ", appAttribute=" + this.appAttribute + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.appAttribute);
    }
}
