package android.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;

public final class RcsParticipantAliasChangedEvent extends RcsEvent {
    private final String mNewAlias;
    private final RcsParticipant mParticipant;

    public RcsParticipantAliasChangedEvent(long timestamp, RcsParticipant participant, String newAlias) {
        super(timestamp);
        this.mParticipant = participant;
        this.mNewAlias = newAlias;
    }

    public RcsParticipant getParticipant() {
        return this.mParticipant;
    }

    public String getNewAlias() {
        return this.mNewAlias;
    }

    public /* synthetic */ Integer lambda$persist$0$RcsParticipantAliasChangedEvent(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.createParticipantAliasChangedEvent(getTimestamp(), getParticipant().getId(), getNewAlias(), callingPackage));
    }

    /* access modifiers changed from: package-private */
    @Override // android.telephony.ims.RcsEvent
    public void persist(RcsControllerCall rcsControllerCall) throws RcsMessageStoreException {
        rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsParticipantAliasChangedEvent$iaidodGQwVEX4DZ8FekRuRx3gQ */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsParticipantAliasChangedEvent.this.lambda$persist$0$RcsParticipantAliasChangedEvent(iRcs, str);
            }
        });
    }
}
