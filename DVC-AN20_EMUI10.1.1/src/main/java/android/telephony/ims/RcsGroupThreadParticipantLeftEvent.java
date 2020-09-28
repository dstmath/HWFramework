package android.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;

public final class RcsGroupThreadParticipantLeftEvent extends RcsGroupThreadEvent {
    private RcsParticipant mLeavingParticipant;

    public RcsGroupThreadParticipantLeftEvent(long timestamp, RcsGroupThread rcsGroupThread, RcsParticipant originatingParticipant, RcsParticipant leavingParticipant) {
        super(timestamp, rcsGroupThread, originatingParticipant);
        this.mLeavingParticipant = leavingParticipant;
    }

    public RcsParticipant getLeavingParticipant() {
        return this.mLeavingParticipant;
    }

    /* access modifiers changed from: package-private */
    @Override // android.telephony.ims.RcsEvent
    public void persist(RcsControllerCall rcsControllerCall) throws RcsMessageStoreException {
        rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsGroupThreadParticipantLeftEvent$vX6x1bZueUi684uTuoFiWxhgs80 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsGroupThreadParticipantLeftEvent.this.lambda$persist$0$RcsGroupThreadParticipantLeftEvent(iRcs, str);
            }
        });
    }

    public /* synthetic */ Integer lambda$persist$0$RcsGroupThreadParticipantLeftEvent(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.createGroupThreadParticipantLeftEvent(getTimestamp(), getRcsGroupThread().getThreadId(), getOriginatingParticipant().getId(), getLeavingParticipant().getId(), callingPackage));
    }
}
