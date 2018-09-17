package android.rms.iaware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AppPreloadInfo implements Parcelable {
    public static final Creator<AppPreloadInfo> CREATOR = new Creator<AppPreloadInfo>() {
        public AppPreloadInfo createFromParcel(Parcel source) {
            return new AppPreloadInfo(source);
        }

        public AppPreloadInfo[] newArray(int size) {
            return new AppPreloadInfo[size];
        }
    };
    private int coldstartTime;
    private String packageName;
    private int powerDissipation;
    private int preloadMem;
    private int warmstartTime;

    public AppPreloadInfo(Parcel source) {
        this.packageName = source.readString();
        this.powerDissipation = source.readInt();
        this.preloadMem = source.readInt();
        this.coldstartTime = source.readInt();
        this.warmstartTime = source.readInt();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getPowerDissipation() {
        return this.powerDissipation;
    }

    public void setPowerDissipation(int powerDissipation) {
        this.powerDissipation = powerDissipation;
    }

    public int getPreloadMem() {
        return this.preloadMem;
    }

    public void setPreloadMem(int preloadMem) {
        this.preloadMem = preloadMem;
    }

    public int getColdstartTime() {
        return this.coldstartTime;
    }

    public void setColdstartTime(int coldstartTime) {
        this.coldstartTime = coldstartTime;
    }

    public int getWarmstartTime() {
        return this.warmstartTime;
    }

    public void setWarmstartTime(int warmstartTime) {
        this.warmstartTime = warmstartTime;
    }

    public String toString() {
        return "AppPreloadInfo [packageName=" + this.packageName + ", powerDissipation=" + this.powerDissipation + ", preloadMem=" + this.preloadMem + ", coldstartTime=" + this.coldstartTime + ", warmstartTime=" + this.warmstartTime + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.powerDissipation);
        dest.writeInt(this.preloadMem);
        dest.writeInt(this.coldstartTime);
        dest.writeInt(this.warmstartTime);
    }
}
