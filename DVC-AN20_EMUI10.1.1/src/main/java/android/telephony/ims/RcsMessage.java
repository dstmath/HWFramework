package android.telephony.ims;

import android.os.RemoteException;
import android.telephony.ims.RcsControllerCall;
import android.telephony.ims.aidl.IRcs;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class RcsMessage {
    public static final int DRAFT = 1;
    public static final int FAILED = 6;
    public static final double LOCATION_NOT_SET = Double.MIN_VALUE;
    public static final int NOT_SET = 0;
    public static final int QUEUED = 2;
    public static final int RECEIVED = 7;
    public static final int RETRYING = 5;
    public static final int SEEN = 9;
    public static final int SENDING = 3;
    public static final int SENT = 4;
    protected final int mId;
    protected final RcsControllerCall mRcsControllerCall;

    @Retention(RetentionPolicy.SOURCE)
    public @interface RcsMessageStatus {
    }

    public abstract boolean isIncoming();

    RcsMessage(RcsControllerCall rcsControllerCall, int id) {
        this.mRcsControllerCall = rcsControllerCall;
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }

    public int getSubscriptionId() throws RcsMessageStoreException {
        return ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$aRPnqqKzd_0r7d0LyxEGwwqhc */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getSubscriptionId$0$RcsMessage(iRcs, str);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getSubscriptionId$0$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.getMessageSubId(this.mId, isIncoming(), callingPackage));
    }

    public void setSubscriptionId(int subId) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(subId) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$LY9H_5LQIoU4Xq6Om0qdYMVI */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsMessage.this.lambda$setSubscriptionId$1$RcsMessage(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setSubscriptionId$1$RcsMessage(int subId, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setMessageSubId(this.mId, isIncoming(), subId, callingPackage);
    }

    public void setStatus(int rcsMessageStatus) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(rcsMessageStatus) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$HOpRnAgYuj5XzRrkxcAiJKt3Yc */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsMessage.this.lambda$setStatus$2$RcsMessage(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setStatus$2$RcsMessage(int rcsMessageStatus, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setMessageStatus(this.mId, isIncoming(), rcsMessageStatus, callingPackage);
    }

    public int getStatus() throws RcsMessageStoreException {
        return ((Integer) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$ENpJTtPeUTVSc1EYo7vY4el8CTs */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getStatus$3$RcsMessage(iRcs, str);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getStatus$3$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.getMessageStatus(this.mId, isIncoming(), callingPackage));
    }

    public void setOriginationTimestamp(long timestamp) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(timestamp) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$tq1Iu9i2c3B7IAVANp7f9nz6BQI */
            private final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsMessage.this.lambda$setOriginationTimestamp$4$RcsMessage(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setOriginationTimestamp$4$RcsMessage(long timestamp, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setMessageOriginationTimestamp(this.mId, isIncoming(), timestamp, callingPackage);
    }

    public long getOriginationTimestamp() throws RcsMessageStoreException {
        return ((Long) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$g_U1Cuc_BEv4JwISu6moBuf_gk0 */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getOriginationTimestamp$5$RcsMessage(iRcs, str);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getOriginationTimestamp$5$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return Long.valueOf(iRcs.getMessageOriginationTimestamp(this.mId, isIncoming(), callingPackage));
    }

    public void setRcsMessageId(String rcsMessageGlobalId) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(rcsMessageGlobalId) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$g8Us4wB8C4Z6FrAxP2EuVIs7uxg */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsMessage.this.lambda$setRcsMessageId$6$RcsMessage(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setRcsMessageId$6$RcsMessage(String rcsMessageGlobalId, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setGlobalMessageIdForMessage(this.mId, isIncoming(), rcsMessageGlobalId, callingPackage);
    }

    public String getRcsMessageId() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$Q3LSjskzCcY_LjdyGsUpqO_r8VY */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getRcsMessageId$7$RcsMessage(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getRcsMessageId$7$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getGlobalMessageIdForMessage(this.mId, isIncoming(), callingPackage);
    }

    public String getText() throws RcsMessageStoreException {
        return (String) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$jYDUGwQFl9jl0oYVhZlCKVq8rao */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getText$8$RcsMessage(iRcs, str);
            }
        });
    }

    public /* synthetic */ String lambda$getText$8$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getTextForMessage(this.mId, isIncoming(), callingPackage);
    }

    public void setText(String text) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(text) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$OAV9C_4ygCWHuq6dzQZ6ryQxcng */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsMessage.this.lambda$setText$9$RcsMessage(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setText$9$RcsMessage(String text, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setTextForMessage(this.mId, isIncoming(), text, callingPackage);
    }

    public double getLatitude() throws RcsMessageStoreException {
        return ((Double) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$kreYSW19iRp_OhyMXMbvQXAxPUo */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getLatitude$10$RcsMessage(iRcs, str);
            }
        })).doubleValue();
    }

    public /* synthetic */ Double lambda$getLatitude$10$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return Double.valueOf(iRcs.getLatitudeForMessage(this.mId, isIncoming(), callingPackage));
    }

    public void setLatitude(double latitude) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(latitude) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$OWkNB5jXq4SHPkhN01pKQSg5Z0 */
            private final /* synthetic */ double f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsMessage.this.lambda$setLatitude$11$RcsMessage(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setLatitude$11$RcsMessage(double latitude, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setLatitudeForMessage(this.mId, isIncoming(), latitude, callingPackage);
    }

    public double getLongitude() throws RcsMessageStoreException {
        return ((Double) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$x3G08QqJukFKk5K0JbtI4g5JW5o */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getLongitude$12$RcsMessage(iRcs, str);
            }
        })).doubleValue();
    }

    public /* synthetic */ Double lambda$getLongitude$12$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return Double.valueOf(iRcs.getLongitudeForMessage(this.mId, isIncoming(), callingPackage));
    }

    public void setLongitude(double longitude) throws RcsMessageStoreException {
        this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn(longitude) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$LUddD5B3is0XmrdznFFrh7_BWBA */
            private final /* synthetic */ double f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
            public final void methodOnIRcs(IRcs iRcs, String str) {
                RcsMessage.this.lambda$setLongitude$13$RcsMessage(this.f$1, iRcs, str);
            }
        });
    }

    public /* synthetic */ void lambda$setLongitude$13$RcsMessage(double longitude, IRcs iRcs, String callingPackage) throws RemoteException {
        iRcs.setLongitudeForMessage(this.mId, isIncoming(), longitude, callingPackage);
    }

    public RcsFileTransferPart insertFileTransfer(RcsFileTransferCreationParams fileTransferCreationParameters) throws RcsMessageStoreException {
        RcsControllerCall rcsControllerCall = this.mRcsControllerCall;
        return new RcsFileTransferPart(rcsControllerCall, ((Integer) rcsControllerCall.call(new RcsControllerCall.RcsServiceCall(fileTransferCreationParameters) {
            /* class android.telephony.ims.$$Lambda$RcsMessage$b6noI0B_AJvyHWAuKOL2fMkHI4 */
            private final /* synthetic */ RcsFileTransferCreationParams f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$insertFileTransfer$14$RcsMessage(this.f$1, iRcs, str);
            }
        })).intValue());
    }

    public /* synthetic */ Integer lambda$insertFileTransfer$14$RcsMessage(RcsFileTransferCreationParams fileTransferCreationParameters, IRcs iRcs, String callingPackage) throws RemoteException {
        return Integer.valueOf(iRcs.storeFileTransfer(this.mId, isIncoming(), fileTransferCreationParameters, callingPackage));
    }

    public Set<RcsFileTransferPart> getFileTransferParts() throws RcsMessageStoreException {
        Set<RcsFileTransferPart> fileTransferParts = new HashSet<>();
        for (int fileTransfer : (int[]) this.mRcsControllerCall.call(new RcsControllerCall.RcsServiceCall() {
            /* class android.telephony.ims.$$Lambda$RcsMessage$0kBwAJ2w8hy0pyzXvF4qM9OTJY */

            @Override // android.telephony.ims.RcsControllerCall.RcsServiceCall
            public final Object methodOnIRcs(IRcs iRcs, String str) {
                return RcsMessage.this.lambda$getFileTransferParts$15$RcsMessage(iRcs, str);
            }
        })) {
            fileTransferParts.add(new RcsFileTransferPart(this.mRcsControllerCall, fileTransfer));
        }
        return Collections.unmodifiableSet(fileTransferParts);
    }

    public /* synthetic */ int[] lambda$getFileTransferParts$15$RcsMessage(IRcs iRcs, String callingPackage) throws RemoteException {
        return iRcs.getFileTransfersAttachedToMessage(this.mId, isIncoming(), callingPackage);
    }

    public void removeFileTransferPart(RcsFileTransferPart fileTransferPart) throws RcsMessageStoreException {
        if (fileTransferPart != null) {
            this.mRcsControllerCall.callWithNoReturn(new RcsControllerCall.RcsServiceCallWithNoReturn() {
                /* class android.telephony.ims.$$Lambda$RcsMessage$ArUQB5LoWlQIN8Wq6WO2D831MM */

                @Override // android.telephony.ims.RcsControllerCall.RcsServiceCallWithNoReturn
                public final void methodOnIRcs(IRcs iRcs, String str) {
                    RcsMessage.lambda$removeFileTransferPart$16(RcsFileTransferPart.this, iRcs, str);
                }
            });
        }
    }
}
