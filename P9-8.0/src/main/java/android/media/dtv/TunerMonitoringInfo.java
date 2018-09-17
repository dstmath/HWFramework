package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TunerMonitoringInfo implements Parcelable {
    public static final Creator<TunerMonitoringInfo> CREATOR = new Creator<TunerMonitoringInfo>() {
        public TunerMonitoringInfo createFromParcel(Parcel source) {
            return new TunerMonitoringInfo(source, null);
        }

        public TunerMonitoringInfo[] newArray(int size) {
            return new TunerMonitoringInfo[size];
        }
    };
    public static final String TAG = "TunerMonitoringInfo";
    private TunerBperInfo mTunerBperInfo1;
    private TunerBperInfo mTunerBperInfo2;
    private TunerCNInfo mTunerCNInfo;
    private TunerRSSIInfo mTunerRSSIInfo;
    private TunerSyncInfo mTunerSyncInfo;
    private TunerTMCCInfo mTunerTMCCInfo;

    /* synthetic */ TunerMonitoringInfo(Parcel in, TunerMonitoringInfo -this1) {
        this(in);
    }

    public TunerMonitoringInfo() {
        this.mTunerTMCCInfo = new TunerTMCCInfo();
        this.mTunerSyncInfo = new TunerSyncInfo();
        this.mTunerRSSIInfo = new TunerRSSIInfo();
        this.mTunerCNInfo = new TunerCNInfo();
        this.mTunerBperInfo1 = new TunerBperInfo();
        this.mTunerBperInfo2 = new TunerBperInfo();
    }

    public TunerSyncInfo getmTunerSyncInfo() {
        return this.mTunerSyncInfo;
    }

    public void setTunerSyncInfo(TunerSyncInfo tunerSyncInfo) {
        this.mTunerSyncInfo = tunerSyncInfo;
    }

    public TunerRSSIInfo getTunerRSSIInfo() {
        return this.mTunerRSSIInfo;
    }

    public void setTunerRSSIInfo(TunerRSSIInfo tunerRSSIInfo) {
        this.mTunerRSSIInfo = tunerRSSIInfo;
    }

    public TunerTMCCInfo getTunerTMCCInfo() {
        return this.mTunerTMCCInfo;
    }

    public void setTunerTMCCInfo(TunerTMCCInfo tunerTMCCInfo) {
        this.mTunerTMCCInfo = tunerTMCCInfo;
    }

    public TunerCNInfo getTunerCNInfo() {
        return this.mTunerCNInfo;
    }

    public void setTunerCNInfo(TunerCNInfo tunerCNInfo) {
        this.mTunerCNInfo = tunerCNInfo;
    }

    public TunerBperInfo getTunerBperInfo1() {
        return this.mTunerBperInfo1;
    }

    public void setTunerBperInfo1(TunerBperInfo tunerBperInfo1) {
        this.mTunerBperInfo1 = tunerBperInfo1;
    }

    public TunerBperInfo getTunerBperInfo2() {
        return this.mTunerBperInfo2;
    }

    public void setTunerBperInfo2(TunerBperInfo tunerBperInfo2) {
        this.mTunerBperInfo2 = tunerBperInfo2;
    }

    private TunerMonitoringInfo(Parcel in) {
        this.mTunerSyncInfo = (TunerSyncInfo) in.readValue(TunerSyncInfo.class.getClassLoader());
        this.mTunerRSSIInfo = (TunerRSSIInfo) in.readValue(TunerRSSIInfo.class.getClassLoader());
        this.mTunerTMCCInfo = (TunerTMCCInfo) in.readValue(TunerSyncInfo.class.getClassLoader());
        this.mTunerCNInfo = (TunerCNInfo) in.readValue(TunerCNInfo.class.getClassLoader());
        this.mTunerBperInfo1 = (TunerBperInfo) in.readValue(TunerBperInfo.class.getClassLoader());
        this.mTunerBperInfo2 = (TunerBperInfo) in.readValue(TunerBperInfo.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.mTunerSyncInfo);
        dest.writeValue(this.mTunerRSSIInfo);
        dest.writeValue(this.mTunerTMCCInfo);
        dest.writeValue(this.mTunerCNInfo);
        dest.writeValue(this.mTunerBperInfo1);
        dest.writeValue(this.mTunerBperInfo2);
    }

    public void readFromParcel(Parcel source) {
        this.mTunerSyncInfo = (TunerSyncInfo) source.readValue(TunerSyncInfo.class.getClassLoader());
        this.mTunerRSSIInfo = (TunerRSSIInfo) source.readValue(TunerRSSIInfo.class.getClassLoader());
        this.mTunerTMCCInfo = (TunerTMCCInfo) source.readValue(TunerSyncInfo.class.getClassLoader());
        this.mTunerCNInfo = (TunerCNInfo) source.readValue(TunerCNInfo.class.getClassLoader());
        this.mTunerBperInfo1 = (TunerBperInfo) source.readValue(TunerBperInfo.class.getClassLoader());
        this.mTunerBperInfo2 = (TunerBperInfo) source.readValue(TunerBperInfo.class.getClassLoader());
    }
}
