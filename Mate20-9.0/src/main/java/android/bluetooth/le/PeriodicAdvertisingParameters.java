package android.bluetooth.le;

import android.os.Parcel;
import android.os.Parcelable;

public final class PeriodicAdvertisingParameters implements Parcelable {
    public static final Parcelable.Creator<PeriodicAdvertisingParameters> CREATOR = new Parcelable.Creator<PeriodicAdvertisingParameters>() {
        public PeriodicAdvertisingParameters[] newArray(int size) {
            return new PeriodicAdvertisingParameters[size];
        }

        public PeriodicAdvertisingParameters createFromParcel(Parcel in) {
            return new PeriodicAdvertisingParameters(in);
        }
    };
    private static final int INTERVAL_MAX = 65519;
    private static final int INTERVAL_MIN = 80;
    private final boolean mIncludeTxPower;
    private final int mInterval;

    public static final class Builder {
        private boolean mIncludeTxPower = false;
        private int mInterval = PeriodicAdvertisingParameters.INTERVAL_MAX;

        public Builder setIncludeTxPower(boolean includeTxPower) {
            this.mIncludeTxPower = includeTxPower;
            return this;
        }

        public Builder setInterval(int interval) {
            if (interval < 80 || interval > PeriodicAdvertisingParameters.INTERVAL_MAX) {
                throw new IllegalArgumentException("Invalid interval (must be 80-65519)");
            }
            this.mInterval = interval;
            return this;
        }

        public PeriodicAdvertisingParameters build() {
            return new PeriodicAdvertisingParameters(this.mIncludeTxPower, this.mInterval);
        }
    }

    private PeriodicAdvertisingParameters(boolean includeTxPower, int interval) {
        this.mIncludeTxPower = includeTxPower;
        this.mInterval = interval;
    }

    private PeriodicAdvertisingParameters(Parcel in) {
        this.mIncludeTxPower = in.readInt() != 0;
        this.mInterval = in.readInt();
    }

    public boolean getIncludeTxPower() {
        return this.mIncludeTxPower;
    }

    public int getInterval() {
        return this.mInterval;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mIncludeTxPower ? 1 : 0);
        dest.writeInt(this.mInterval);
    }
}
