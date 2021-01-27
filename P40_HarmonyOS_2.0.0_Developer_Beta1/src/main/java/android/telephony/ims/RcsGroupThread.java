package android.telephony.ims;

import android.net.Uri;
import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class RcsGroupThread extends RcsThread {
    public RcsGroupThread(RcsControllerCall rcsControllerCall, int threadId) {
        super(rcsControllerCall, threadId);
    }

    @Override // android.telephony.ims.RcsThread
    public boolean isGroup() {
        return true;
    }

    public String getGroupName() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$cwnjgWxIgjmTCKAe7pcICt4Voo0 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsGroupThread.this.lambda$getGroupName$0$RcsGroupThread(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getGroupName$0$RcsGroupThread(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getGroupThreadName(this.mThreadId, callingPackage);
    }

    public void setGroupName(String groupName) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(groupName) {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$ZorE2WcUPTtLCwMm_x5CnWwa7YI */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsGroupThread.this.lambda$setGroupName$1$RcsGroupThread(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setGroupName$1$RcsGroupThread(String groupName, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setGroupThreadName(this.mThreadId, groupName, callingPackage);
    }

    public Uri getGroupIcon() throws RcsMessageStoreException {
        return (Uri) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$4K1iTAEPwdeTAbDd4wTsX1Jl4S4 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsGroupThread.this.lambda$getGroupIcon$2$RcsGroupThread(iRcs, str);
            }
        });
    }

    public /* synthetic */ Uri lambda$getGroupIcon$2$RcsGroupThread(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getGroupThreadIcon(this.mThreadId, callingPackage);
    }

    public void setGroupIcon(Uri groupIcon) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(groupIcon) {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$23X4NWEVE7qw298P70JdcMW6oM */
            private final /* synthetic */ Uri f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsGroupThread.this.lambda$setGroupIcon$3$RcsGroupThread(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setGroupIcon$3$RcsGroupThread(Uri groupIcon, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setGroupThreadIcon(this.mThreadId, groupIcon, callingPackage);
    }

    public RcsParticipant getOwner() throws RcsMessageStoreException {
        return new RcsParticipant(this.mRcsControllerCall, ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$OMEGtapvlm86Yn7pLPBR5He4UoQ */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsGroupThread.this.lambda$getOwner$4$RcsGroupThread(iRcs, str);
            }
        })).intValue());
    }

    public /* synthetic */ Integer lambda$getOwner$4$RcsGroupThread(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.getGroupThreadOwner(this.mThreadId, callingPackage));
    }

    public void setOwner(RcsParticipant participant) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(participant) {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$9QKuv_xqJEallZaE2sSumu3POo */
            private final /* synthetic */ RcsParticipant f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsGroupThread.this.lambda$setOwner$5$RcsGroupThread(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setOwner$5$RcsGroupThread(RcsParticipant participant, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setGroupThreadOwner(this.mThreadId, participant.getId(), callingPackage);
    }

    public void addParticipant(RcsParticipant participant) throws RcsMessageStoreException {
        if (participant != null) {
            this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(participant) {
                /* class android.telephony.ims.$$Lambda$RcsGroupThread$HaJSnZuef49b66N8v9ayzVaOQxQ */
                private final /* synthetic */ RcsParticipant f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
                public final void methodOnIRcs(IRcs iRcs, String str) {
                    RcsGroupThread.this.lambda$addParticipant$6$RcsGroupThread(this.f$1, iRcs, str);
                }
            });
        }
    }

    public /* synthetic */ void lambda$addParticipant$6$RcsGroupThread(RcsParticipant participant, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.addParticipantToGroupThread(this.mThreadId, participant.getId(), callingPackage);
    }

    public void removeParticipant(RcsParticipant participant) throws RcsMessageStoreException {
        if (participant != null) {
            this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(participant) {
                /* class android.telephony.ims.$$Lambda$RcsGroupThread$xvETBJ_gzJJ5zvelRSNsYZBdXKw */
                private final /* synthetic */ RcsParticipant f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
                public final void methodOnIRcs(IRcs iRcs, String str) {
                    RcsGroupThread.this.lambda$removeParticipant$7$RcsGroupThread(this.f$1, iRcs, str);
                }
            });
        }
    }

    public /* synthetic */ void lambda$removeParticipant$7$RcsGroupThread(RcsParticipant participant, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.removeParticipantFromGroupThread(this.mThreadId, participant.getId(), callingPackage);
    }

    public Set<RcsParticipant> getParticipants() throws RcsMessageStoreException {
        return Collections.unmodifiableSet(new LinkedHashSet<>(new RcsParticipantQueryResult(this.mRcsControllerCall, (RcsParticipantQueryResultParcelable) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$X2eY_CkF7PfEGF8QwmaD6Cv0PhI */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getParticipants(RcsParticipantQueryParams.this, str);
            }
        })).getParticipants()));
    }

    public Uri getConferenceUri() throws RcsMessageStoreException {
        return (Uri) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$hYpkX2Z60Pf5FiSb6pvoBpmHfXA */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsGroupThread.this.lambda$getConferenceUri$9$RcsGroupThread(iRcs, str);
            }
        });
    }

    public /* synthetic */ Uri lambda$getConferenceUri$9$RcsGroupThread(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getGroupThreadConferenceUri(this.mThreadId, callingPackage);
    }

    public void setConferenceUri(Uri conferenceUri) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(conferenceUri) {
            /* class android.telephony.ims.$$Lambda$RcsGroupThread$LhWdWS6noezEn0xijClZdbKHOas */
            private final /* synthetic */ Uri f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsGroupThread.this.lambda$setConferenceUri$10$RcsGroupThread(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setConferenceUri$10$RcsGroupThread(Uri conferenceUri, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setGroupThreadConferenceUri(this.mThreadId, conferenceUri, callingPackage);
    }
}
