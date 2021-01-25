package android.bluetooth.le;

import android.os.Parcel;
import android.os.Parcelable;

public final class AdvertiseSettings implements Parcelable {
    public static final int ADVERTISE_MODE_BALANCED = 1;
    public static final int ADVERTISE_MODE_LOWEST_LATENCY = 3;
    public static final int ADVERTISE_MODE_LOW_LATENCY = 2;
    public static final int ADVERTISE_MODE_LOW_POWER = 0;
    public static final int ADVERTISE_TX_POWER_HIGH = 3;
    public static final int ADVERTISE_TX_POWER_LOW = 1;
    public static final int ADVERTISE_TX_POWER_MEDIUM = 2;
    public static final int ADVERTISE_TX_POWER_ULTRA_HIGH = 4;
    public static final int ADVERTISE_TX_POWER_ULTRA_LOW = 0;
    public static final Parcelable.Creator<AdvertiseSettings> CREATOR = new Parcelable.Creator<AdvertiseSettings>() {
        /* class android.bluetooth.le.AdvertiseSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AdvertiseSettings[] newArray(int size) {
            return new AdvertiseSettings[size];
        }

        @Override // android.os.Parcelable.Creator
        public AdvertiseSettings createFromParcel(Parcel in) {
            return new AdvertiseSettings(in);
        }
    };
    private static final int LIMITED_ADVERTISING_MAX_MILLIS = 180000;
    private final boolean mAdvertiseConnectable;
    private final int mAdvertiseMode;
    private final int mAdvertiseTimeoutMillis;
    private final int mAdvertiseTxPowerLevel;

    private AdvertiseSettings(int advertiseMode, int advertiseTxPowerLevel, boolean advertiseConnectable, int advertiseTimeout) {
        this.mAdvertiseMode = advertiseMode;
        this.mAdvertiseTxPowerLevel = advertiseTxPowerLevel;
        this.mAdvertiseConnectable = advertiseConnectable;
        this.mAdvertiseTimeoutMillis = advertiseTimeout;
    }

    private AdvertiseSettings(Parcel in) {
        this.mAdvertiseMode = in.readInt();
        this.mAdvertiseTxPowerLevel = in.readInt();
        this.mAdvertiseConnectable = in.readInt() != 0;
        this.mAdvertiseTimeoutMillis = in.readInt();
    }

    public int getMode() {
        return this.mAdvertiseMode;
    }

    public int getTxPowerLevel() {
        return this.mAdvertiseTxPowerLevel;
    }

    public boolean isConnectable() {
        return this.mAdvertiseConnectable;
    }

    public int getTimeout() {
        return this.mAdvertiseTimeoutMillis;
    }

    public String toString() {
        return "Settings [mAdvertiseMode=" + this.mAdvertiseMode + ", mAdvertiseTxPowerLevel=" + this.mAdvertiseTxPowerLevel + ", mAdvertiseConnectable=" + this.mAdvertiseConnectable + ", mAdvertiseTimeoutMillis=" + this.mAdvertiseTimeoutMillis + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAdvertiseMode);
        dest.writeInt(this.mAdvertiseTxPowerLevel);
        dest.writeInt(this.mAdvertiseConnectable ? 1 : 0);
        dest.writeInt(this.mAdvertiseTimeoutMillis);
    }

    public static final class Builder {
        private boolean mConnectable = true;
        private int mMode = 0;
        private int mTimeoutMillis = 0;
        private int mTxPowerLevel = 2;

        public Builder setAdvertiseMode(int advertiseMode) {
            if (advertiseMode < 0 || advertiseMode > 2) {
                throw new IllegalArgumentException("unknown mode " + advertiseMode);
            }
            this.mMode = advertiseMode;
            return this;
        }

        public Builder setTxPowerLevel(int txPowerLevel) {
            if (txPowerLevel < 0 || txPowerLevel > 4) {
                throw new IllegalArgumentException("unknown tx power level " + txPowerLevel);
            }
            this.mTxPowerLevel = txPowerLevel;
            return this;
        }

        public Builder setConnectable(boolean connectable) {
            this.mConnectable = connectable;
            return this;
        }

        public Builder setTimeout(int timeoutMillis) {
            if (timeoutMillis < 0 || timeoutMillis > AdvertiseSettings.LIMITED_ADVERTISING_MAX_MILLIS) {
                throw new IllegalArgumentException("timeoutMillis invalid (must be 0-180000 milliseconds)");
            }
            this.mTimeoutMillis = timeoutMillis;
            return this;
        }

        public AdvertiseSettings build() {
            return new AdvertiseSettings(this.mMode, this.mTxPowerLevel, this.mConnectable, this.mTimeoutMillis);
        }
    }
}
