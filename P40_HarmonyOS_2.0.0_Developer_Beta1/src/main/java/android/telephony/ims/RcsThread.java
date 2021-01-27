package android.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.RcsMessageQueryParams;
import android.telephony.ims.aidl.IRcs;
import com.android.internal.annotations.VisibleForTesting;

public abstract class RcsThread {
    protected final RcsControllerCall mRcsControllerCall;
    protected int mThreadId;

    public abstract boolean isGroup();

    protected RcsThread(RcsControllerCall rcsControllerCall, int threadId) {
        this.mThreadId = threadId;
        this.mRcsControllerCall = rcsControllerCall;
    }

    public RcsMessageSnippet getSnippet() throws RcsMessageStoreException {
        return (RcsMessageSnippet) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsThread$TwqOqnkLjl05BhB2arTpJkBo73Y */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsThread.this.lambda$getSnippet$0$RcsThread(iRcs, str);
            }
        });
    }

    public /* synthetic */ RcsMessageSnippet lambda$getSnippet$0$RcsThread(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getMessageSnippet(this.mThreadId, callingPackage);
    }

    public RcsIncomingMessage addIncomingMessage(RcsIncomingMessageCreationParams rcsIncomingMessageCreationParams) throws RcsMessageStoreException {
        return new RcsIncomingMessage(this.mRcsControllerCall, ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall(rcsIncomingMessageCreationParams) {
            /* class android.telephony.ims.$$Lambda$RcsThread$9gFw0KtLBczxOxCksL2zOV2xHM */
            private final /* synthetic */ RcsIncomingMessageCreationParams f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsThread.this.lambda$addIncomingMessage$1$RcsThread(this.f$1, iRcs, str);
            }
        })).intValue());
    }

    public /* synthetic */ Integer lambda$addIncomingMessage$1$RcsThread(RcsIncomingMessageCreationParams rcsIncomingMessageCreationParams, IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.addIncomingMessage(this.mThreadId, rcsIncomingMessageCreationParams, callingPackage));
    }

    public RcsOutgoingMessage addOutgoingMessage(RcsOutgoingMessageCreationParams rcsOutgoingMessageCreationParams) throws RcsMessageStoreException {
        return new RcsOutgoingMessage(this.mRcsControllerCall, ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall(rcsOutgoingMessageCreationParams) {
            /* class android.telephony.ims.$$Lambda$RcsThread$_9zfuqUJl6VjAbIMvQwKcAyzUs */
            private final /* synthetic */ RcsOutgoingMessageCreationParams f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsThread.this.lambda$addOutgoingMessage$2$RcsThread(this.f$1, iRcs, str);
            }
        })).intValue());
    }

    public /* synthetic */ Integer lambda$addOutgoingMessage$2$RcsThread(RcsOutgoingMessageCreationParams rcsOutgoingMessageCreationParams, IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.addOutgoingMessage(this.mThreadId, rcsOutgoingMessageCreationParams, callingPackage));
    }

    public void deleteMessage(RcsMessage rcsMessage) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(rcsMessage) {
            /* class android.telephony.ims.$$Lambda$RcsThread$uAkHFwrvypgP5w5y0Uy4uwQ6blY */
            private final /* synthetic */ RcsMessage f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsThread.this.lambda$deleteMessage$3$RcsThread(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$deleteMessage$3$RcsThread(RcsMessage rcsMessage, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.deleteMessage(rcsMessage.getId(), rcsMessage.isIncoming(), this.mThreadId, isGroup(), callingPackage);
    }

    public RcsMessageQueryResult getMessages() throws RcsMessageStoreException {
        RcsMessageQueryParams queryParams = new RcsMessageQueryParams.Builder().setThread(this).build();
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsMessageQueryResult(rcsControllerCall, (RcsMessageQueryResultParcelable) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsThread$A9iPL3bU3iiRv1xCYNUNP76n6Vw */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return iRcs.getMessages(RcsMessageQueryParams.this, str);
            }
        }));
    }

    @VisibleForTesting
    public int getThreadId() {
        return this.mThreadId;
    }

    public int getThreadType() {
        return isGroup() ? 1 : 0;
    }
}
