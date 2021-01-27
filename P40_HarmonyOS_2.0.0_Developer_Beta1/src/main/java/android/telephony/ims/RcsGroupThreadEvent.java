package android.telephony.ims;

public abstract class RcsGroupThreadEvent extends RcsEvent {
    private final RcsParticipant mOriginatingParticipant;
    private final RcsGroupThread mRcsGroupThread;

    RcsGroupThreadEvent(long timestamp, RcsGroupThread rcsGroupThread, RcsParticipant originatingParticipant) {
        super(timestamp);
        this.mRcsGroupThread = rcsGroupThread;
        this.mOriginatingParticipant = originatingParticipant;
    }

    public RcsGroupThread getRcsGroupThread() {
        return this.mRcsGroupThread;
    }

    public RcsParticipant getOriginatingParticipant() {
        return this.mOriginatingParticipant;
    }
}
