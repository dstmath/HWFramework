package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public final class WifiActivityEnergyInfo implements Parcelable {
    public static final Creator<WifiActivityEnergyInfo> CREATOR = null;
    public static final int STACK_STATE_INVALID = 0;
    public static final int STACK_STATE_STATE_ACTIVE = 1;
    public static final int STACK_STATE_STATE_IDLE = 3;
    public static final int STACK_STATE_STATE_SCANNING = 2;
    public long mControllerEnergyUsed;
    public long mControllerIdleTimeMs;
    public long mControllerRxTimeMs;
    public long mControllerTxTimeMs;
    public long[] mControllerTxTimePerLevelMs;
    public int mStackState;
    public long mTimestamp;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiActivityEnergyInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiActivityEnergyInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiActivityEnergyInfo.<clinit>():void");
    }

    public WifiActivityEnergyInfo(long timestamp, int stackState, long txTime, long[] txTimePerLevel, long rxTime, long idleTime, long energyUsed) {
        this.mTimestamp = timestamp;
        this.mStackState = stackState;
        this.mControllerTxTimeMs = txTime;
        this.mControllerTxTimePerLevelMs = txTimePerLevel;
        this.mControllerRxTimeMs = rxTime;
        this.mControllerIdleTimeMs = idleTime;
        this.mControllerEnergyUsed = energyUsed;
    }

    public String toString() {
        return "WifiActivityEnergyInfo{ timestamp=" + this.mTimestamp + " mStackState=" + this.mStackState + " mControllerTxTimeMs=" + this.mControllerTxTimeMs + " mControllerTxTimePerLevelMs=" + Arrays.toString(this.mControllerTxTimePerLevelMs) + " mControllerRxTimeMs=" + this.mControllerRxTimeMs + " mControllerIdleTimeMs=" + this.mControllerIdleTimeMs + " mControllerEnergyUsed=" + this.mControllerEnergyUsed + " }";
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mTimestamp);
        out.writeInt(this.mStackState);
        out.writeLong(this.mControllerTxTimeMs);
        out.writeLongArray(this.mControllerTxTimePerLevelMs);
        out.writeLong(this.mControllerRxTimeMs);
        out.writeLong(this.mControllerIdleTimeMs);
        out.writeLong(this.mControllerEnergyUsed);
    }

    public int describeContents() {
        return STACK_STATE_INVALID;
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
        if (this.mControllerTxTimeMs < 0 || this.mControllerRxTimeMs < 0 || this.mControllerIdleTimeMs < 0) {
            return false;
        }
        return true;
    }
}
