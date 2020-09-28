package android.telephony.ims;

public abstract class RcsEvent {
    private final long mTimestamp;

    /* access modifiers changed from: package-private */
    public abstract void persist(RcsControllerCall rcsControllerCall) throws RcsMessageStoreException;

    protected RcsEvent(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }
}
