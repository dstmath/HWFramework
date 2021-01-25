package android.bluetooth.le;

import android.os.Parcel;
import android.os.Parcelable;

public final class AdvertisingSetParameters implements Parcelable {
    public static final Parcelable.Creator<AdvertisingSetParameters> CREATOR = new Parcelable.Creator<AdvertisingSetParameters>() {
        /* class android.bluetooth.le.AdvertisingSetParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AdvertisingSetParameters[] newArray(int size) {
            return new AdvertisingSetParameters[size];
        }

        @Override // android.os.Parcelable.Creator
        public AdvertisingSetParameters createFromParcel(Parcel in) {
            return new AdvertisingSetParameters(in);
        }
    };
    public static final int INTERVAL_HIGH = 1600;
    public static final int INTERVAL_LOW = 160;
    public static final int INTERVAL_MAX = 16777215;
    public static final int INTERVAL_MEDIUM = 400;
    public static final int INTERVAL_MIN = 160;
    private static final int INVALID_INTERVAL = -1;
    private static final int LIMITED_ADVERTISING_MAX_MILLIS = 180000;
    public static final int TX_POWER_HIGH = 1;
    public static final int TX_POWER_LOW = -15;
    public static final int TX_POWER_MAX = 1;
    public static final int TX_POWER_MEDIUM = -7;
    public static final int TX_POWER_MIN = -127;
    public static final int TX_POWER_ULTRA_LOW = -21;
    public static final int TX_POWER_ULTRA_MAX = 8;
    private final boolean mConnectable;
    private final boolean mIncludeTxPower;
    private final int mInterval;
    private final boolean mIsAnonymous;
    private final boolean mIsLegacy;
    private int mMandatoryInterval;
    private final int mPrimaryPhy;
    private final boolean mScannable;
    private final int mSecondaryPhy;
    private final int mTxPowerLevel;

    private AdvertisingSetParameters(boolean connectable, boolean scannable, boolean isLegacy, boolean isAnonymous, boolean includeTxPower, int primaryPhy, int secondaryPhy, int interval, int txPowerLevel) {
        this.mConnectable = connectable;
        this.mScannable = scannable;
        this.mIsLegacy = isLegacy;
        this.mIsAnonymous = isAnonymous;
        this.mIncludeTxPower = includeTxPower;
        this.mPrimaryPhy = primaryPhy;
        this.mSecondaryPhy = secondaryPhy;
        this.mInterval = interval;
        this.mTxPowerLevel = txPowerLevel;
    }

    private AdvertisingSetParameters(Parcel in) {
        boolean z = true;
        this.mConnectable = in.readInt() != 0;
        this.mScannable = in.readInt() != 0;
        this.mIsLegacy = in.readInt() != 0;
        this.mIsAnonymous = in.readInt() != 0;
        this.mIncludeTxPower = in.readInt() == 0 ? false : z;
        this.mPrimaryPhy = in.readInt();
        this.mSecondaryPhy = in.readInt();
        this.mInterval = in.readInt();
        this.mTxPowerLevel = in.readInt();
        this.mMandatoryInterval = in.readInt();
    }

    public boolean isConnectable() {
        return this.mConnectable;
    }

    public boolean isScannable() {
        return this.mScannable;
    }

    public boolean isLegacy() {
        return this.mIsLegacy;
    }

    public boolean isAnonymous() {
        return this.mIsAnonymous;
    }

    public boolean includeTxPower() {
        return this.mIncludeTxPower;
    }

    public int getPrimaryPhy() {
        return this.mPrimaryPhy;
    }

    public int getSecondaryPhy() {
        return this.mSecondaryPhy;
    }

    public int getInterval() {
        return this.mInterval;
    }

    public int getTxPowerLevel() {
        return this.mTxPowerLevel;
    }

    private int getMandatoryInterval() {
        return this.mMandatoryInterval;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AdvertisingSetParameters setMandatoryInterval(int mandatoryInterval) {
        this.mMandatoryInterval = mandatoryInterval;
        return this;
    }

    public String toString() {
        return "AdvertisingSetParameters [connectable=" + this.mConnectable + ", isLegacy=" + this.mIsLegacy + ", isAnonymous=" + this.mIsAnonymous + ", includeTxPower=" + this.mIncludeTxPower + ", primaryPhy=" + this.mPrimaryPhy + ", secondaryPhy=" + this.mSecondaryPhy + ", interval=" + this.mInterval + ", txPowerLevel=" + this.mTxPowerLevel + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mConnectable ? 1 : 0);
        dest.writeInt(this.mScannable ? 1 : 0);
        dest.writeInt(this.mIsLegacy ? 1 : 0);
        dest.writeInt(this.mIsAnonymous ? 1 : 0);
        dest.writeInt(this.mIncludeTxPower ? 1 : 0);
        dest.writeInt(this.mPrimaryPhy);
        dest.writeInt(this.mSecondaryPhy);
        dest.writeInt(this.mInterval);
        dest.writeInt(this.mTxPowerLevel);
        dest.writeInt(this.mMandatoryInterval);
    }

    public static final class Builder {
        private boolean mConnectable = false;
        private boolean mIncludeTxPower = false;
        private int mInterval = 160;
        private boolean mIsAnonymous = false;
        private boolean mIsLegacy = false;
        private int mMandatoryInterval = -1;
        private int mPrimaryPhy = 1;
        private boolean mScannable = false;
        private int mSecondaryPhy = 1;
        private int mTxPowerLevel = -7;

        public Builder setConnectable(boolean connectable) {
            this.mConnectable = connectable;
            return this;
        }

        public Builder setScannable(boolean scannable) {
            this.mScannable = scannable;
            return this;
        }

        public Builder setLegacyMode(boolean isLegacy) {
            this.mIsLegacy = isLegacy;
            return this;
        }

        public Builder setAnonymous(boolean isAnonymous) {
            this.mIsAnonymous = isAnonymous;
            return this;
        }

        public Builder setIncludeTxPower(boolean includeTxPower) {
            this.mIncludeTxPower = includeTxPower;
            return this;
        }

        public Builder setPrimaryPhy(int primaryPhy) {
            if (primaryPhy == 1 || primaryPhy == 3) {
                this.mPrimaryPhy = primaryPhy;
                return this;
            }
            throw new IllegalArgumentException("bad primaryPhy " + primaryPhy);
        }

        public Builder setSecondaryPhy(int secondaryPhy) {
            if (secondaryPhy == 1 || secondaryPhy == 2 || secondaryPhy == 3) {
                this.mSecondaryPhy = secondaryPhy;
                return this;
            }
            throw new IllegalArgumentException("bad secondaryPhy " + secondaryPhy);
        }

        public Builder setInterval(int interval) {
            if (interval < 160 || interval > 16777215) {
                throw new IllegalArgumentException("unknown interval " + interval);
            }
            this.mInterval = interval;
            return this;
        }

        public Builder setTxPowerLevel(int txPowerLevel) {
            if (txPowerLevel < -127 || txPowerLevel > 8) {
                throw new IllegalArgumentException("unknown txPowerLevel " + txPowerLevel);
            }
            this.mTxPowerLevel = txPowerLevel;
            return this;
        }

        public AdvertisingSetParameters build() {
            if (this.mIsLegacy) {
                if (this.mIsAnonymous) {
                    throw new IllegalArgumentException("Legacy advertising can't be anonymous");
                } else if (this.mConnectable && !this.mScannable) {
                    throw new IllegalStateException("Legacy advertisement can't be connectable and non-scannable");
                } else if (this.mIncludeTxPower) {
                    throw new IllegalStateException("Legacy advertising can't include TX power level in header");
                }
            } else if (this.mConnectable && this.mScannable) {
                throw new IllegalStateException("Advertising can't be both connectable and scannable");
            } else if (this.mIsAnonymous && this.mConnectable) {
                throw new IllegalStateException("Advertising can't be both connectable and anonymous");
            }
            return new AdvertisingSetParameters(this.mConnectable, this.mScannable, this.mIsLegacy, this.mIsAnonymous, this.mIncludeTxPower, this.mPrimaryPhy, this.mSecondaryPhy, this.mInterval, this.mTxPowerLevel).setMandatoryInterval(this.mMandatoryInterval);
        }
    }
}
