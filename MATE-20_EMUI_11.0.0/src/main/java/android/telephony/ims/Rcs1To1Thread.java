package android.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;

public class Rcs1To1Thread extends RcsThread {
    private int mThreadId;

    public Rcs1To1Thread(RcsControllerCall rcsControllerCall, int threadId) {
        super(rcsControllerCall, threadId);
        this.mThreadId = threadId;
    }

    @Override // android.telephony.ims.RcsThread
    public boolean isGroup() {
        return false;
    }

    public long getFallbackThreadId() throws RcsMessageStoreException {
        return ((Long) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$Rcs1To1Thread$_6gUCvjDS6WXqf0AClQwrZ7ZpSc */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return Rcs1To1Thread.this.lambda$getFallbackThreadId$0$Rcs1To1Thread(iRcs, str);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getFallbackThreadId$0$Rcs1To1Thread(IRcs iRcs, String callingPackage) throws RemoteException {
        return Long.valueOf(iRcs.get1To1ThreadFallbackThreadId(this.mThreadId, callingPackage));
    }

    public void setFallbackThreadId(long fallbackThreadId) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(fallbackThreadId) {
            /* class android.telephony.ims.$$Lambda$Rcs1To1Thread$vx_evSYitgJIMB6lhANvSJpdBE */
            private final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                Rcs1To1Thread.this.lambda$setFallbackThreadId$1$Rcs1To1Thread(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setFallbackThreadId$1$Rcs1To1Thread(long fallbackThreadId, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.set1To1ThreadFallbackThreadId(this.mThreadId, fallbackThreadId, callingPackage);
    }

    public RcsParticipant getRecipient() throws RcsMessageStoreException {
        return new RcsParticipant(this.mRcsControllerCall, ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$Rcs1To1Thread$DlCgifrXUJFouqWWh0GG6hzHs */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return Rcs1To1Thread.this.lambda$getRecipient$2$Rcs1To1Thread(iRcs, str);
            }
        })).intValue());
    }

    public /* synthetic */ Integer lambda$getRecipient$2$Rcs1To1Thread(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.get1To1ThreadOtherParticipantId(this.mThreadId, callingPackage));
    }
}
