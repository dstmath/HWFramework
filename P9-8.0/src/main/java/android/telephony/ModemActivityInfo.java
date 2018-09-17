package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class ModemActivityInfo implements Parcelable {
    public static final Creator<ModemActivityInfo> CREATOR = new Creator<ModemActivityInfo>() {
        public ModemActivityInfo createFromParcel(Parcel in) {
            long timestamp = in.readLong();
            int sleepTimeMs = in.readInt();
            int idleTimeMs = in.readInt();
            int[] txTimeMs = new int[5];
            for (int i = 0; i < 5; i++) {
                txTimeMs[i] = in.readInt();
            }
            return new ModemActivityInfo(timestamp, sleepTimeMs, idleTimeMs, txTimeMs, in.readInt(), in.readInt());
        }

        public ModemActivityInfo[] newArray(int size) {
            return new ModemActivityInfo[size];
        }
    };
    public static final int TX_POWER_LEVELS = 5;
    private final int mEnergyUsed;
    private final int mIdleTimeMs;
    private final int mRxTimeMs;
    private final int mSleepTimeMs;
    private final long mTimestamp;
    private final int[] mTxTimeMs = new int[5];

    public ModemActivityInfo(long timestamp, int sleepTimeMs, int idleTimeMs, int[] txTimeMs, int rxTimeMs, int energyUsed) {
        this.mTimestamp = timestamp;
        this.mSleepTimeMs = sleepTimeMs;
        this.mIdleTimeMs = idleTimeMs;
        if (txTimeMs != null) {
            System.arraycopy(txTimeMs, 0, this.mTxTimeMs, 0, Math.min(txTimeMs.length, 5));
        }
        this.mRxTimeMs = rxTimeMs;
        this.mEnergyUsed = energyUsed;
    }

    public String toString() {
        return "ModemActivityInfo{ mTimestamp=" + this.mTimestamp + " mSleepTimeMs=" + this.mSleepTimeMs + " mIdleTimeMs=" + this.mIdleTimeMs + " mTxTimeMs[]=" + Arrays.toString(this.mTxTimeMs) + " mRxTimeMs=" + this.mRxTimeMs + " mEnergyUsed=" + this.mEnergyUsed + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mTimestamp);
        dest.writeInt(this.mSleepTimeMs);
        dest.writeInt(this.mIdleTimeMs);
        for (int i = 0; i < 5; i++) {
            dest.writeInt(this.mTxTimeMs[i]);
        }
        dest.writeInt(this.mRxTimeMs);
        dest.writeInt(this.mEnergyUsed);
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public int[] getTxTimeMillis() {
        return this.mTxTimeMs;
    }

    public int getSleepTimeMillis() {
        return this.mSleepTimeMs;
    }

    public int getIdleTimeMillis() {
        return this.mIdleTimeMs;
    }

    public int getRxTimeMillis() {
        return this.mRxTimeMs;
    }

    public int getEnergyUsed() {
        return this.mEnergyUsed;
    }

    public boolean isValid() {
        boolean z = false;
        for (int txVal : getTxTimeMillis()) {
            if (txVal < 0) {
                return false;
            }
        }
        if (getIdleTimeMillis() >= 0 && getSleepTimeMillis() >= 0 && getRxTimeMillis() >= 0 && getEnergyUsed() >= 0) {
            z = isEmpty() ^ 1;
        }
        return z;
    }

    private boolean isEmpty() {
        boolean z = false;
        for (int txVal : getTxTimeMillis()) {
            if (txVal != 0) {
                return false;
            }
        }
        if (getIdleTimeMillis() == 0 && getSleepTimeMillis() == 0 && getRxTimeMillis() == 0 && getEnergyUsed() == 0) {
            z = true;
        }
        return z;
    }
}
