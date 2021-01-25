package ohos.bluetooth.ble;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class BleAdvertiseSettings implements Sequenceable {
    public static final int INTERVAL_SLOT_DEFAULT = 1600;
    public static final int INTERVAL_SLOT_MAX = 16777215;
    public static final int INTERVAL_SLOT_MIN = 32;
    public static final int TX_POWER_DEFAULT = -7;
    public static final int TX_POWER_MAX = 1;
    public static final int TX_POWER_MIN = -127;
    private boolean mConnectable;
    private int mInterval;
    private int mTxPower;

    private BleAdvertiseSettings(int i, int i2, boolean z) {
        this.mInterval = i;
        this.mTxPower = i2;
        this.mConnectable = z;
    }

    BleAdvertiseSettings() {
    }

    public boolean isConnectable() {
        return this.mConnectable;
    }

    public static final class Builder {
        private boolean mConnectable = true;
        private int mInterval = 1600;
        private int mTxPower = -7;

        public Builder setInterval(int i) {
            if (i < 32 || i > 16777215) {
                throw new IllegalArgumentException("unknown interval " + i);
            }
            this.mInterval = i;
            return this;
        }

        public Builder setTxPower(int i) {
            if (i < -127 || i > 1) {
                throw new IllegalArgumentException("unknown txPower " + i);
            }
            this.mTxPower = i;
            return this;
        }

        public Builder setConnectable(boolean z) {
            this.mConnectable = z;
            return this;
        }

        public BleAdvertiseSettings build() {
            return new BleAdvertiseSettings(this.mInterval, this.mTxPower, this.mConnectable);
        }
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.mConnectable ? 1 : 0);
        parcel.writeInt(1);
        parcel.writeInt(1);
        parcel.writeInt(0);
        parcel.writeInt(0);
        parcel.writeInt(1);
        parcel.writeInt(1);
        parcel.writeInt(this.mInterval);
        parcel.writeInt(this.mTxPower);
        parcel.writeInt(-1);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.mConnectable = parcel.readInt() != 0;
        parcel.readInt();
        parcel.readInt();
        parcel.readInt();
        parcel.readInt();
        parcel.readInt();
        parcel.readInt();
        this.mInterval = parcel.readInt();
        this.mTxPower = parcel.readInt();
        parcel.readInt();
        return true;
    }
}
