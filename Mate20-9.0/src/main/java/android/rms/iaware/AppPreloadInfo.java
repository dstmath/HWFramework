package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;

public class AppPreloadInfo implements Parcelable {
    public static final Parcelable.Creator<AppPreloadInfo> CREATOR = new Parcelable.Creator<AppPreloadInfo>() {
        public AppPreloadInfo createFromParcel(Parcel source) {
            return new AppPreloadInfo(source);
        }

        public AppPreloadInfo[] newArray(int size) {
            return new AppPreloadInfo[size];
        }
    };
    private int appAttribute;
    private int coldstartTime;
    private String packageName;
    private int powerDissipation;
    private int preloadMem;
    private int warmstartTime;

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

    @Deprecated
    public int getPowerDissipation() {
        return this.powerDissipation;
    }

    @Deprecated
    public void setPowerDissipation(int powerDissipation2) {
        this.powerDissipation = powerDissipation2;
    }

    @Deprecated
    public int getPreloadMem() {
        return this.preloadMem;
    }

    @Deprecated
    public void setPreloadMem(int preloadMem2) {
        this.preloadMem = preloadMem2;
    }

    @Deprecated
    public int getColdstartTime() {
        return this.coldstartTime;
    }

    @Deprecated
    public void setColdstartTime(int coldstartTime2) {
        this.coldstartTime = coldstartTime2;
    }

    @Deprecated
    public int getWarmstartTime() {
        return this.warmstartTime;
    }

    @Deprecated
    public void setWarmstartTime(int warmstartTime2) {
        this.warmstartTime = warmstartTime2;
    }

    public void setAppAttribute(int appAttribute2) {
        this.appAttribute = appAttribute2;
    }

    public int getAppAttribute() {
        return this.appAttribute;
    }

    public String toString() {
        return "AppPreloadInfo [packageName=" + this.packageName + ", appAttribute=" + this.appAttribute + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.appAttribute);
    }
}
