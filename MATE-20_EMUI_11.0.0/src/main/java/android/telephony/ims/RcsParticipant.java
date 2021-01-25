package android.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;

public class RcsParticipant {
    private final int mId;
    private final RcsControllerCall mRcsControllerCall;

    public RcsParticipant(RcsControllerCall rcsControllerCall, int id) {
        this.mRcsControllerCall = rcsControllerCall;
        this.mId = id;
    }

    public String getCanonicalAddress() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsParticipant$T35onLZnUuRTl7zQ7ZWRFtFvx4 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsParticipant.this.lambda$getCanonicalAddress$0$RcsParticipant(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getCanonicalAddress$0$RcsParticipant(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getRcsParticipantCanonicalAddress(this.mId, callingPackage);
    }

    public String getAlias() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsParticipant$MNtRFbM6hycH3bPEUZgB5f56zs */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsParticipant.this.lambda$getAlias$1$RcsParticipant(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getAlias$1$RcsParticipant(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getRcsParticipantAlias(this.mId, callingPackage);
    }

    public void setAlias(String alias) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(alias) {
            /* class android.telephony.ims.$$Lambda$RcsParticipant$xireNE3auWDac4dOx89mKtRKU */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsParticipant.this.lambda$setAlias$2$RcsParticipant(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setAlias$2$RcsParticipant(String alias, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setRcsParticipantAlias(this.mId, alias, callingPackage);
    }

    public String getContactId() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsParticipant$up5zUlvCkFUru1_1NfgXrzNmBic */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsParticipant.this.lambda$getContactId$3$RcsParticipant(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getContactId$3$RcsParticipant(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getRcsParticipantContactId(this.mId, callingPackage);
    }

    public void setContactId(String contactId) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(contactId) {
            /* class android.telephony.ims.$$Lambda$RcsParticipant$HgHlMU15W2RReyvhkUQ432pfA */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsParticipant.this.lambda$setContactId$4$RcsParticipant(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setContactId$4$RcsParticipant(String contactId, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setRcsParticipantContactId(this.mId, contactId, callingPackage);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RcsParticipant)) {
            return false;
        }
        if (this.mId == ((RcsParticipant) obj).mId) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.mId;
    }

    public int getId() {
        return this.mId;
    }
}
