package android.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;

public final class RcsGroupThreadNameChangedEvent extends RcsGroupThreadEvent {
    private final String mNewName;

    public RcsGroupThreadNameChangedEvent(long timestamp, RcsGroupThread rcsGroupThread, RcsParticipant originatingParticipant, String newName) {
        super(timestamp, rcsGroupThread, originatingParticipant);
        this.mNewName = newName;
    }

    public String getNewName() {
        return this.mNewName;
    }

    public /* synthetic */ Integer lambda$persist$0$RcsGroupThreadNameChangedEvent(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.createGroupThreadNameChangedEvent(getTimestamp(), getRcsGroupThread().getThreadId(), getOriginatingParticipant().getId(), this.mNewName, callingPackage));
    }

    /* access modifiers changed from: package-private */
    @Override // android.telephony.ims.RcsEvent
    public void persist(RcsControllerCall rcsControllerCall) throws RcsMessageStoreException {
        rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsGroupThreadNameChangedEvent$_UcLy20x7aG6AEgcOgmZOeqTok0 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsGroupThreadNameChangedEvent.this.lambda$persist$0$RcsGroupThreadNameChangedEvent(iRcs, str);
            }
        });
    }
}
