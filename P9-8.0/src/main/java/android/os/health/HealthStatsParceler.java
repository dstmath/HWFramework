package android.os.health;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HealthStatsParceler implements Parcelable {
    public static final Creator<HealthStatsParceler> CREATOR = new Creator<HealthStatsParceler>() {
        public HealthStatsParceler createFromParcel(Parcel in) {
            return new HealthStatsParceler(in);
        }

        public HealthStatsParceler[] newArray(int size) {
            return new HealthStatsParceler[size];
        }
    };
    private HealthStats mHealthStats;
    private HealthStatsWriter mWriter;

    public HealthStatsParceler(HealthStatsWriter writer) {
        this.mWriter = writer;
    }

    public HealthStatsParceler(Parcel in) {
        this.mHealthStats = new HealthStats(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.mWriter != null) {
            this.mWriter.flattenToParcel(out);
            return;
        }
        throw new RuntimeException("Can not re-parcel HealthStatsParceler that was constructed from a Parcel");
    }

    public HealthStats getHealthStats() {
        if (this.mWriter != null) {
            Parcel parcel = Parcel.obtain();
            this.mWriter.flattenToParcel(parcel);
            parcel.setDataPosition(0);
            this.mHealthStats = new HealthStats(parcel);
            parcel.recycle();
        }
        return this.mHealthStats;
    }
}
