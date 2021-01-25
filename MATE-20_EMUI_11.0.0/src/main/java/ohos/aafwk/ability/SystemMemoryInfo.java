package ohos.aafwk.ability;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class SystemMemoryInfo implements Sequenceable {
    public static final Sequenceable.Producer<SystemMemoryInfo> PRODUCER = $$Lambda$SystemMemoryInfo$hKDVNmSDiZ9bFaQBGlpggQe5JY.INSTANCE;
    private long availSysMem;
    private boolean lowSysMemory;
    private long threshold;
    private long totalSysMem;

    public SystemMemoryInfo() {
    }

    static /* synthetic */ SystemMemoryInfo lambda$static$0(Parcel parcel) {
        SystemMemoryInfo systemMemoryInfo = new SystemMemoryInfo();
        systemMemoryInfo.unmarshalling(parcel);
        return systemMemoryInfo;
    }

    public void setAvailSysMem(long j) {
        this.availSysMem = j;
    }

    public void setTotalSysMem(long j) {
        this.totalSysMem = j;
    }

    public void setThreshold(long j) {
        this.threshold = j;
    }

    public void setLowSysMemory(boolean z) {
        this.lowSysMemory = z;
    }

    public long getAvailSysMem() {
        return this.availSysMem;
    }

    public long getTotalSysMem() {
        return this.totalSysMem;
    }

    public long getThreshold() {
        return this.threshold;
    }

    public boolean isLowSysMemory() {
        return this.lowSysMemory;
    }

    public boolean marshalling(Parcel parcel) {
        if (parcel.writeLong(this.availSysMem) && parcel.writeLong(this.totalSysMem) && parcel.writeLong(this.threshold) && parcel.writeInt(this.lowSysMemory ? 1 : 0)) {
            return true;
        }
        return false;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.availSysMem = parcel.readLong();
        this.totalSysMem = parcel.readLong();
        this.threshold = parcel.readLong();
        this.lowSysMemory = parcel.readInt() != 0;
        return true;
    }

    private SystemMemoryInfo(Parcel parcel) {
        unmarshalling(parcel);
    }
}
