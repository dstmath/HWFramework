package android.telephony.ims;

import android.content.Context;
import android.net.Uri;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;
import java.util.List;

public class RcsMessageStore {
    RcsControllerCall mRcsControllerCall;

    RcsMessageStore(Context context) {
        this.mRcsControllerCall = new RcsControllerCall(context);
    }

    public RcsThreadQueryResult getRcsThreads(RcsThreadQueryParams queryParameters) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsThreadQueryResult(rcsControllerCall, (RcsThreadQueryResultParcelable) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$z090Zf4wxRrBwUxXanwm4N3vb7w */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getRcsThreads(RcsThreadQueryParams.this, str);
            }
        }));
    }

    public RcsThreadQueryResult getRcsThreads(RcsQueryContinuationToken continuationToken) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsThreadQueryResult(rcsControllerCall, (RcsThreadQueryResultParcelable) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$XArwINUevYoOl_OgZskFwRkGhs */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getRcsThreadsWithToken(RcsQueryContinuationToken.this, str);
            }
        }));
    }

    public RcsParticipantQueryResult getRcsParticipants(RcsParticipantQueryParams queryParameters) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsParticipantQueryResult(rcsControllerCall, (RcsParticipantQueryResultParcelable) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$720PbSnOJzhKXiqHw1UEfx5w6A */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getParticipants(RcsParticipantQueryParams.this, str);
            }
        }));
    }

    public RcsParticipantQueryResult getRcsParticipants(RcsQueryContinuationToken continuationToken) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsParticipantQueryResult(rcsControllerCall, (RcsParticipantQueryResultParcelable) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$tSyQsX68KutSWLEXxfgNSJ47ep0 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getParticipantsWithToken(RcsQueryContinuationToken.this, str);
            }
        }));
    }

    public RcsMessageQueryResult getRcsMessages(RcsMessageQueryParams queryParams) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsMessageQueryResult(rcsControllerCall, (RcsMessageQueryResultParcelable) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$5QXAY7bGFdmsWgLF0pk1tyYYovg */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getMessages(RcsMessageQueryParams.this, str);
            }
        }));
    }

    public RcsMessageQueryResult getRcsMessages(RcsQueryContinuationToken continuationToken) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsMessageQueryResult(rcsControllerCall, (RcsMessageQueryResultParcelable) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$fs2V7Gtqd2gkYR7NanLG2NjZNho */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getMessagesWithToken(RcsQueryContinuationToken.this, str);
            }
        }));
    }

    public RcsEventQueryResult getRcsEvents(RcsEventQueryParams queryParams) throws RcsMessageStoreException {
        return ((RcsEventQueryResultDescriptor) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$IvBKppwBc6MDwzIkAi2XJcVBiI */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getEvents(RcsEventQueryParams.this, str);
            }
        })).getRcsEventQueryResult(this.mRcsControllerCall);
    }

    public RcsEventQueryResult getRcsEvents(RcsQueryContinuationToken continuationToken) throws RcsMessageStoreException {
        return ((RcsEventQueryResultDescriptor) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$RFZerRPNR1WyCuEIu6_yEveDhrk */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getEventsWithToken(RcsQueryContinuationToken.this, str);
            }
        })).getRcsEventQueryResult(this.mRcsControllerCall);
    }

    public void persistRcsEvent(RcsEvent rcsEvent) throws RcsMessageStoreException {
        rcsEvent.persist(this.mRcsControllerCall);
    }

    public Rcs1To1Thread createRcs1To1Thread(RcsParticipant recipient) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new Rcs1To1Thread(rcsControllerCall, ((Integer) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$eOFObBGnN5PMKJvVTBw06iJWQ4 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return Integer.valueOf(iRcs.createRcs1To1Thread(RcsParticipant.this.getId(), str));
            }
        })).intValue());
    }

    public RcsGroupThread createGroupThread(List<RcsParticipant> recipients, String groupName, Uri groupIcon) throws RcsMessageStoreException {
        int[] recipientIds = null;
        if (recipients != null) {
            recipientIds = new int[recipients.size()];
            for (int i = 0; i < recipients.size(); i++) {
                recipientIds[i] = recipients.get(i).getId();
            }
        }
        return new RcsGroupThread(this.mRcsControllerCall, ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall(recipientIds, groupName, groupIcon) {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$g309WUVpYx8N7suWdUAGJXtJOs */
            private final /* synthetic */ int[] f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ Uri f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return Integer.valueOf(iRcs.createGroupThread(this.f$0, this.f$1, this.f$2, str));
            }
        })).intValue());
    }

    public void deleteThread(RcsThread thread) throws RcsMessageStoreException {
        if (thread != null && !((Boolean) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$nbXWLR_ux8VCEHNEyE7JO0J05YI */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                RcsThread rcsThread;
                return Boolean.valueOf(iRcs.deleteThread(rcsThread.getThreadId(), RcsThread.this.getThreadType(), str));
            }
        })).booleanValue()) {
            throw new RcsMessageStoreException("Could not delete RcsThread");
        }
    }

    public RcsParticipant createRcsParticipant(String canonicalAddress, String alias) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsParticipant(rcsControllerCall, ((Integer) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall(canonicalAddress, alias) {
            /* class android.telephony.ims.$$Lambda$RcsMessageStore$d1Om4XlR70Dyh7qD9d6F4NZZkQI */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ String f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return Integer.valueOf(iRcs.createRcsParticipant(this.f$0, this.f$1, str));
            }
        })).intValue());
    }
}
