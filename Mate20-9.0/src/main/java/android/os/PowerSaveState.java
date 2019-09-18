package android.os;

import android.os.Parcelable;

public class PowerSaveState implements Parcelable {
    public static final Parcelable.Creator<PowerSaveState> CREATOR = new Parcelable.Creator<PowerSaveState>() {
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
        /* access modifiers changed from: private */
        public boolean mBatterySaverEnabled = false;
        /* access modifiers changed from: private */
        public float mBrightnessFactor = 0.5f;
        /* access modifiers changed from: private */
        public boolean mGlobalBatterySaverEnabled = false;
        /* access modifiers changed from: private */
        public int mGpsMode = 0;

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
        boolean z = false;
        this.batterySaverEnabled = in.readByte() != 0;
        this.globalBatterySaverEnabled = in.readByte() != 0 ? true : z;
        this.gpsMode = in.readInt();
        this.brightnessFactor = in.readFloat();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.batterySaverEnabled ? (byte) 1 : 0);
        dest.writeByte(this.globalBatterySaverEnabled ? (byte) 1 : 0);
        dest.writeInt(this.gpsMode);
        dest.writeFloat(this.brightnessFactor);
    }
}
