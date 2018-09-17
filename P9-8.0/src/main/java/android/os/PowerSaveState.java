package android.os;

import android.os.Parcelable.Creator;

public class PowerSaveState implements Parcelable {
    public static final Creator<PowerSaveState> CREATOR = new Creator<PowerSaveState>() {
        public PowerSaveState createFromParcel(Parcel source) {
            return new PowerSaveState(source);
        }

        public PowerSaveState[] newArray(int size) {
            return new PowerSaveState[size];
        }
    };
    public final boolean batterySaverEnabled;
    public final float brightnessFactor;
    public final boolean globalBatterySaverEnabled;
    public final int gpsMode;

    public static final class Builder {
        private boolean mBatterySaverEnabled = false;
        private float mBrightnessFactor = 0.5f;
        private boolean mGlobalBatterySaverEnabled = false;
        private int mGpsMode = 0;

        public Builder setBatterySaverEnabled(boolean enabled) {
            this.mBatterySaverEnabled = enabled;
            return this;
        }

        public Builder setGlobalBatterySaverEnabled(boolean enabled) {
            this.mGlobalBatterySaverEnabled = enabled;
            return this;
        }

        public Builder setGpsMode(int mode) {
            this.mGpsMode = mode;
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

    public PowerSaveState(Builder builder) {
        this.batterySaverEnabled = builder.mBatterySaverEnabled;
        this.gpsMode = builder.mGpsMode;
        this.brightnessFactor = builder.mBrightnessFactor;
        this.globalBatterySaverEnabled = builder.mGlobalBatterySaverEnabled;
    }

    public PowerSaveState(Parcel in) {
        boolean z;
        boolean z2 = true;
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.batterySaverEnabled = z;
        if (in.readByte() == (byte) 0) {
            z2 = false;
        }
        this.globalBatterySaverEnabled = z2;
        this.gpsMode = in.readInt();
        this.brightnessFactor = in.readFloat();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        if (this.batterySaverEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeByte((byte) i);
        if (!this.globalBatterySaverEnabled) {
            i2 = 0;
        }
        dest.writeByte((byte) i2);
        dest.writeInt(this.gpsMode);
        dest.writeFloat(this.brightnessFactor);
    }
}
