package android.os;

import android.os.Parcelable;

public class PowerSaveState implements Parcelable {
    public static final Parcelable.Creator<PowerSaveState> CREATOR = new Parcelable.Creator<PowerSaveState>() {
        /* class android.os.PowerSaveState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PowerSaveState createFromParcel(Parcel source) {
            return new PowerSaveState(source);
        }

        @Override // android.os.Parcelable.Creator
        public PowerSaveState[] newArray(int size) {
            return new PowerSaveState[size];
        }
    };
    public final boolean batterySaverEnabled;
    public final float brightnessFactor;
    public final boolean globalBatterySaverEnabled;
    public final int locationMode;

    public PowerSaveState(Builder builder) {
        this.batterySaverEnabled = builder.mBatterySaverEnabled;
        this.locationMode = builder.mLocationMode;
        this.brightnessFactor = builder.mBrightnessFactor;
        this.globalBatterySaverEnabled = builder.mGlobalBatterySaverEnabled;
    }

    public PowerSaveState(Parcel in) {
        boolean z = true;
        this.batterySaverEnabled = in.readByte() != 0;
        this.globalBatterySaverEnabled = in.readByte() == 0 ? false : z;
        this.locationMode = in.readInt();
        this.brightnessFactor = in.readFloat();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.batterySaverEnabled ? (byte) 1 : 0);
        dest.writeByte(this.globalBatterySaverEnabled ? (byte) 1 : 0);
        dest.writeInt(this.locationMode);
        dest.writeFloat(this.brightnessFactor);
    }

    public static final class Builder {
        private boolean mBatterySaverEnabled = false;
        private float mBrightnessFactor = 0.5f;
        private boolean mGlobalBatterySaverEnabled = false;
        private int mLocationMode = 0;

        public Builder setBatterySaverEnabled(boolean enabled) {
            this.mBatterySaverEnabled = enabled;
            return this;
        }

        public Builder setGlobalBatterySaverEnabled(boolean enabled) {
            this.mGlobalBatterySaverEnabled = enabled;
            return this;
        }

        public Builder setLocationMode(int mode) {
            this.mLocationMode = mode;
            return this;
        }

        public Builder setBrightnessFactor(float factor) {
            this.mBrightnessFactor = factor;
            return this;
        }

        public PowerSaveState build() {
            return new PowerSaveState(this);
        }
    }
}
