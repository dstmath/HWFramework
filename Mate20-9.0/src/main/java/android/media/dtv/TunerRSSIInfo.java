package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;

public class TunerRSSIInfo implements Parcelable {
    public static final Parcelable.Creator<TunerRSSIInfo> CREATOR = new Parcelable.Creator<TunerRSSIInfo>() {
        public TunerRSSIInfo createFromParcel(Parcel source) {
            return new TunerRSSIInfo(source);
        }

        public TunerRSSIInfo[] newArray(int size) {
            return new TunerRSSIInfo[size];
        }
    };
    public static final String TAG = "TunerRSSIInfo";
    private int mTunerRSSI;

    public int getTunerRSSI() {
        return this.mTunerRSSI;
    }

    public void setTunerRSSI(int tunerRSSI) {
        this.mTunerRSSI = tunerRSSI;
    }

    public TunerRSSIInfo() {
    }

    private TunerRSSIInfo(Parcel in) {
        this.mTunerRSSI = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTunerRSSI);
    }

    public void readFromParcel(Parcel source) {
        this.mTunerRSSI = source.readInt();
    }
}
