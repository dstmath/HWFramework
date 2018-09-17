package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class ModemActivityInfo implements Parcelable {
    public static final Creator<ModemActivityInfo> CREATOR = null;
    public static final int TX_POWER_LEVELS = 5;
    private final int mEnergyUsed;
    private final int mIdleTimeMs;
    private final int mRxTimeMs;
    private final int mSleepTimeMs;
    private final long mTimestamp;
    private final int[] mTxTimeMs;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.ModemActivityInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.ModemActivityInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.ModemActivityInfo.<clinit>():void");
    }

    public ModemActivityInfo(long timestamp, int sleepTimeMs, int idleTimeMs, int[] txTimeMs, int rxTimeMs, int energyUsed) {
        this.mTxTimeMs = new int[TX_POWER_LEVELS];
        this.mTimestamp = timestamp;
        this.mSleepTimeMs = sleepTimeMs;
        this.mIdleTimeMs = idleTimeMs;
        if (txTimeMs != null) {
            System.arraycopy(txTimeMs, 0, this.mTxTimeMs, 0, Math.min(txTimeMs.length, TX_POWER_LEVELS));
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
        for (int i = 0; i < TX_POWER_LEVELS; i++) {
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
        if (getIdleTimeMillis() >= 0 && getSleepTimeMillis() >= 0 && getRxTimeMillis() >= 0 && getEnergyUsed() >= 0 && !isEmpty()) {
            z = true;
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
