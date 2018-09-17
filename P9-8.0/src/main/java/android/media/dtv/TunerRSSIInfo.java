package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TunerRSSIInfo implements Parcelable {
    public static final Creator<TunerRSSIInfo> CREATOR = new Creator<TunerRSSIInfo>() {
        public TunerRSSIInfo createFromParcel(Parcel source) {
            return new TunerRSSIInfo(source, null);
        }

        public TunerRSSIInfo[] newArray(int size) {
            return new TunerRSSIInfo[size];
        }
    };
    public static final String TAG = "TunerRSSIInfo";
    private int mTunerRSSI;

    /* synthetic */ TunerRSSIInfo(Parcel in, TunerRSSIInfo -this1) {
        this(in);
    }

    public int getTunerRSSI() {
        return this.mTunerRSSI;
    }

    public void setTunerRSSI(int tunerRSSI) {
        this.mTunerRSSI = tunerRSSI;
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
