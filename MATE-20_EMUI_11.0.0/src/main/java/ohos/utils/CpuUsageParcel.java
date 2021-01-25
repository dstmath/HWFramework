package ohos.utils;

public class CpuUsageParcel implements Sequenceable {
    private long mActive;
    private long mTotal;

    public CpuUsageParcel(long j, long j2) {
        this.mActive = j;
        this.mTotal = j2;
    }

    public CpuUsageParcel(Parcel parcel) {
        unmarshalling(parcel);
    }

    public long getActive() {
        return this.mActive;
    }

    public void setActive(long j) {
        this.mActive = j;
    }

    public long getTotal() {
        return this.mTotal;
    }

    public void setTotal(long j) {
        this.mTotal = j;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return parcel.writeLong(this.mActive) && parcel.writeLong(this.mTotal);
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.mActive = parcel.readLong();
        this.mTotal = parcel.readLong();
        return true;
    }
}
