package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public final class WifiActivityEnergyInfo implements Parcelable {
    public static final Parcelable.Creator<WifiActivityEnergyInfo> CREATOR = new Parcelable.Creator<WifiActivityEnergyInfo>() {
        public WifiActivityEnergyInfo createFromParcel(Parcel in) {
            WifiActivityEnergyInfo wifiActivityEnergyInfo = new WifiActivityEnergyInfo(in.readLong(), in.readInt(), in.readLong(), in.createLongArray(), in.readLong(), in.readLong(), in.readLong(), in.readLong());
            return wifiActivityEnergyInfo;
        }

        public WifiActivityEnergyInfo[] newArray(int size) {
            return new WifiActivityEnergyInfo[size];
        }
    };
    public static final int STACK_STATE_INVALID = 0;
    public static final int STACK_STATE_STATE_ACTIVE = 1;
    public static final int STACK_STATE_STATE_IDLE = 3;
    public static final int STACK_STATE_STATE_SCANNING = 2;
    public long mControllerEnergyUsed;
    public long mControllerIdleTimeMs;
    public long mControllerRxTimeMs;
    public long mControllerScanTimeMs;
    public long mControllerTxTimeMs;
    public long[] mControllerTxTimePerLevelMs;
    public int mStackState;
    public long mTimestamp;

    public WifiActivityEnergyInfo(long timestamp, int stackState, long txTime, long[] txTimePerLevel, long rxTime, long scanTime, long idleTime, long energyUsed) {
        this.mTimestamp = timestamp;
        this.mStackState = stackState;
        this.mControllerTxTimeMs = txTime;
        this.mControllerTxTimePerLevelMs = txTimePerLevel;
        this.mControllerRxTimeMs = rxTime;
        this.mControllerScanTimeMs = scanTime;
        this.mControllerIdleTimeMs = idleTime;
        this.mControllerEnergyUsed = energyUsed;
    }

    public String toString() {
        return "WifiActivityEnergyInfo{ timestamp=" + this.mTimestamp + " mStackState=" + this.mStackState + " mControllerTxTimeMs=" + this.mControllerTxTimeMs + " mControllerTxTimePerLevelMs=" + Arrays.toString(this.mControllerTxTimePerLevelMs) + " mControllerRxTimeMs=" + this.mControllerRxTimeMs + " mControllerScanTimeMs=" + this.mControllerScanTimeMs + " mControllerIdleTimeMs=" + this.mControllerIdleTimeMs + " mControllerEnergyUsed=" + this.mControllerEnergyUsed + " }";
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mTimestamp);
        out.writeInt(this.mStackState);
        out.writeLong(this.mControllerTxTimeMs);
        out.writeLongArray(this.mControllerTxTimePerLevelMs);
        out.writeLong(this.mControllerRxTimeMs);
        out.writeLong(this.mControllerScanTimeMs);
        out.writeLong(this.mControllerIdleTimeMs);
        out.writeLong(this.mControllerEnergyUsed);
    }

    public int describeContents() {
        return 0;
    }

    public int getStackState() {
        return this.mStackState;
    }

    public long getControllerTxTimeMillis() {
        return this.mControllerTxTimeMs;
    }

    public long getControllerTxTimeMillisAtLevel(int level) {
        if (level < this.mControllerTxTimePerLevelMs.length) {
            return this.mControllerTxTimePerLevelMs[level];
        }
        return 0;
    }

    public long getControllerRxTimeMillis() {
        return this.mControllerRxTimeMs;
    }

    public long getControllerScanTimeMillis() {
        return this.mControllerScanTimeMs;
    }

    public long getControllerIdleTimeMillis() {
        return this.mControllerIdleTimeMs;
    }

    public long getControllerEnergyUsed() {
        return this.mControllerEnergyUsed;
    }

    public long getTimeStamp() {
        return this.mTimestamp;
    }

    public boolean isValid() {
        return this.mControllerTxTimeMs >= 0 && this.mControllerRxTimeMs >= 0 && this.mControllerScanTimeMs >= 0 && this.mControllerIdleTimeMs >= 0;
    }
}
