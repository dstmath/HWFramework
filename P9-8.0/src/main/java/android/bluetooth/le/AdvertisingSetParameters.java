package android.bluetooth.le;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class AdvertisingSetParameters implements Parcelable {
    public static final Creator<AdvertisingSetParameters> CREATOR = new Creator<AdvertisingSetParameters>() {
        public AdvertisingSetParameters[] newArray(int size) {
            return new AdvertisingSetParameters[size];
        }

        public AdvertisingSetParameters createFromParcel(Parcel in) {
            return new AdvertisingSetParameters(in, null);
        }
    };
    public static final int INTERVAL_HIGH = 1600;
    public static final int INTERVAL_LOW = 160;
    public static final int INTERVAL_MAX = 16777215;
    public static final int INTERVAL_MEDIUM = 400;
    public static final int INTERVAL_MIN = 160;
    private static final int LIMITED_ADVERTISING_MAX_MILLIS = 180000;
    public static final int TX_POWER_HIGH = 1;
    public static final int TX_POWER_LOW = -15;
    public static final int TX_POWER_MAX = 1;
    public static final int TX_POWER_MEDIUM = -7;
    public static final int TX_POWER_MIN = -127;
    public static final int TX_POWER_ULTRA_LOW = -21;
    private final boolean connectable;
    private final boolean includeTxPower;
    private final int interval;
    private final boolean isAnonymous;
    private final boolean isLegacy;
    private final int primaryPhy;
    private final boolean scannable;
    private final int secondaryPhy;
    private final int txPowerLevel;

    public static final class Builder {
        private boolean connectable = false;
        private boolean includeTxPower = false;
        private int interval = 160;
        private boolean isAnonymous = false;
        private boolean isLegacy = false;
        private int primaryPhy = 1;
        private boolean scannable = false;
        private int secondaryPhy = 1;
        private int txPowerLevel = -7;

        public Builder setConnectable(boolean connectable) {
            this.connectable = connectable;
            return this;
        }

        public Builder setScannable(boolean scannable) {
            this.scannable = scannable;
            return this;
        }

        public Builder setLegacyMode(boolean isLegacy) {
            this.isLegacy = isLegacy;
            return this;
        }

        public Builder setAnonymous(boolean isAnonymous) {
            this.isAnonymous = isAnonymous;
            return this;
        }

        public Builder setIncludeTxPower(boolean includeTxPower) {
            this.includeTxPower = includeTxPower;
            return this;
        }

        public Builder setPrimaryPhy(int primaryPhy) {
            if (primaryPhy == 1 || primaryPhy == 3) {
                this.primaryPhy = primaryPhy;
                return this;
            }
            throw new IllegalArgumentException("bad primaryPhy " + primaryPhy);
        }

        public Builder setSecondaryPhy(int secondaryPhy) {
            if (secondaryPhy == 1 || secondaryPhy == 2 || secondaryPhy == 3) {
                this.secondaryPhy = secondaryPhy;
                return this;
            }
            throw new IllegalArgumentException("bad secondaryPhy " + secondaryPhy);
        }

        public Builder setInterval(int interval) {
            if (interval < 160 || interval > 16777215) {
                throw new IllegalArgumentException("unknown interval " + interval);
            }
            this.interval = interval;
            return this;
        }

        public Builder setTxPowerLevel(int txPowerLevel) {
            if (txPowerLevel < -127 || txPowerLevel > 1) {
                throw new IllegalArgumentException("unknown txPowerLevel " + txPowerLevel);
            }
            this.txPowerLevel = txPowerLevel;
            return this;
        }

        public AdvertisingSetParameters build() {
            if (this.isLegacy) {
                if (this.isAnonymous) {
                    throw new IllegalArgumentException("Legacy advertising can't be anonymous");
                } else if (this.connectable && !this.scannable) {
                    throw new IllegalStateException("Legacy advertisement can't be connectable and non-scannable");
                } else if (this.includeTxPower) {
                    throw new IllegalStateException("Legacy advertising can't include TX power level in header");
                }
            } else if (this.connectable && this.scannable) {
                throw new IllegalStateException("Advertising can't be both connectable and scannable");
            } else if (this.isAnonymous && this.connectable) {
                throw new IllegalStateException("Advertising can't be both connectable and anonymous");
            }
            return new AdvertisingSetParameters(this.connectable, this.scannable, this.isLegacy, this.isAnonymous, this.includeTxPower, this.primaryPhy, this.secondaryPhy, this.interval, this.txPowerLevel, null);
        }
    }

    /* synthetic */ AdvertisingSetParameters(Parcel in, AdvertisingSetParameters -this1) {
        this(in);
    }

    /* synthetic */ AdvertisingSetParameters(boolean connectable, boolean scannable, boolean isLegacy, boolean isAnonymous, boolean includeTxPower, int primaryPhy, int secondaryPhy, int interval, int txPowerLevel, AdvertisingSetParameters -this9) {
        this(connectable, scannable, isLegacy, isAnonymous, includeTxPower, primaryPhy, secondaryPhy, interval, txPowerLevel);
    }

    private AdvertisingSetParameters(boolean connectable, boolean scannable, boolean isLegacy, boolean isAnonymous, boolean includeTxPower, int primaryPhy, int secondaryPhy, int interval, int txPowerLevel) {
        this.connectable = connectable;
        this.scannable = scannable;
        this.isLegacy = isLegacy;
        this.isAnonymous = isAnonymous;
        this.includeTxPower = includeTxPower;
        this.primaryPhy = primaryPhy;
        this.secondaryPhy = secondaryPhy;
        this.interval = interval;
        this.txPowerLevel = txPowerLevel;
    }

    private AdvertisingSetParameters(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.connectable = in.readInt() != 0;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.scannable = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isLegacy = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isAnonymous = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.includeTxPower = z2;
        this.primaryPhy = in.readInt();
        this.secondaryPhy = in.readInt();
        this.interval = in.readInt();
        this.txPowerLevel = in.readInt();
    }

    public boolean isConnectable() {
        return this.connectable;
    }

    public boolean isScannable() {
        return this.scannable;
    }

    public boolean isLegacy() {
        return this.isLegacy;
    }

    public boolean isAnonymous() {
        return this.isAnonymous;
    }

    public boolean includeTxPower() {
        return this.includeTxPower;
    }

    public int getPrimaryPhy() {
        return this.primaryPhy;
    }

    public int getSecondaryPhy() {
        return this.secondaryPhy;
    }

    public int getInterval() {
        return this.interval;
    }

    public int getTxPowerLevel() {
        return this.txPowerLevel;
    }

    public String toString() {
        return "AdvertisingSetParameters [connectable=" + this.connectable + ", isLegacy=" + this.isLegacy + ", isAnonymous=" + this.isAnonymous + ", includeTxPower=" + this.includeTxPower + ", primaryPhy=" + this.primaryPhy + ", secondaryPhy=" + this.secondaryPhy + ", interval=" + this.interval + ", txPowerLevel=" + this.txPowerLevel + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.connectable ? 1 : 0);
        if (this.scannable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isLegacy) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isAnonymous) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.includeTxPower) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.primaryPhy);
        dest.writeInt(this.secondaryPhy);
        dest.writeInt(this.interval);
        dest.writeInt(this.txPowerLevel);
    }
}
