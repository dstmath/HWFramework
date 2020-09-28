package com.android.internal.telephony.ims;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.ims.RcsEventQueryParams;
import android.telephony.ims.RcsEventQueryResultDescriptor;
import android.telephony.ims.RcsFileTransferCreationParams;
import android.telephony.ims.RcsIncomingMessageCreationParams;
import android.telephony.ims.RcsMessageQueryParams;
import android.telephony.ims.RcsMessageQueryResultParcelable;
import android.telephony.ims.RcsMessageSnippet;
import android.telephony.ims.RcsOutgoingMessageCreationParams;
import android.telephony.ims.RcsParticipantQueryParams;
import android.telephony.ims.RcsParticipantQueryResultParcelable;
import android.telephony.ims.RcsQueryContinuationToken;
import android.telephony.ims.RcsThreadQueryParams;
import android.telephony.ims.RcsThreadQueryResultParcelable;
import android.telephony.ims.aidl.IRcs;
import com.android.internal.annotations.VisibleForTesting;

public class RcsMessageStoreController extends IRcs.Stub {
    private static final String RCS_SERVICE_NAME = "ircs";
    static final String TAG = "RcsMsgStoreController";
    private static RcsMessageStoreController sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final RcsEventQueryHelper mEventQueryHelper = new RcsEventQueryHelper(this.mContentResolver);
    private final RcsMessageQueryHelper mMessageQueryHelper = new RcsMessageQueryHelper(this.mContentResolver);
    private final RcsMessageStoreUtil mMessageStoreUtil = new RcsMessageStoreUtil(this.mContentResolver);
    private final RcsParticipantQueryHelper mParticipantQueryHelper = new RcsParticipantQueryHelper(this.mContentResolver);
    private final RcsThreadQueryHelper mThreadQueryHelper = new RcsThreadQueryHelper(this.mContentResolver, this.mParticipantQueryHelper);

    /* access modifiers changed from: package-private */
    public interface ThrowingRunnable {
        void run() throws RemoteException;
    }

    /* access modifiers changed from: package-private */
    public interface ThrowingSupplier<T> {
        T get() throws RemoteException;
    }

    /* JADX WARN: Type inference failed for: r2v2, types: [com.android.internal.telephony.ims.RcsMessageStoreController, android.os.IBinder] */
    public static RcsMessageStoreController init(Context context) {
        synchronized (RcsMessageStoreController.class) {
            if (sInstance == null) {
                sInstance = new RcsMessageStoreController(context);
                if (ServiceManager.getService(RCS_SERVICE_NAME) == null) {
                    ServiceManager.addService(RCS_SERVICE_NAME, (IBinder) sInstance);
                }
            } else {
                Rlog.e(TAG, "init() called multiple times! sInstance = " + sInstance);
            }
        }
        return sInstance;
    }

    private void performWriteOperation(String callingPackage, ThrowingRunnable fn) {
        RcsPermissions.checkWritePermissions(this.mContext, callingPackage);
        try {
            fn.run();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T performCreateOperation(String callingPackage, ThrowingSupplier<T> fn) {
        RcsPermissions.checkWritePermissions(this.mContext, callingPackage);
        try {
            return fn.get();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T performReadOperation(String callingPackage, ThrowingSupplier<T> fn) {
        RcsPermissions.checkReadPermissions(this.mContext, callingPackage);
        try {
            return fn.get();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    public RcsMessageStoreController(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public boolean deleteThread(int threadId, int threadType, String callingPackage) {
        return ((Boolean) performCreateOperation(callingPackage, new ThrowingSupplier(threadType, threadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$vA5r4LfpqX3gFPu_m4wW3apM7Q */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$deleteThread$0$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).booleanValue();
    }

    public /* synthetic */ Boolean lambda$deleteThread$0$RcsMessageStoreController(int threadType, int threadId) throws RemoteException {
        boolean z = true;
        if (this.mContentResolver.delete(threadType == 1 ? Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI : Telephony.RcsColumns.Rcs1To1ThreadColumns.RCS_1_TO_1_THREAD_URI, "rcs_thread_id=?", new String[]{Integer.toString(threadId)}) <= 0) {
            z = false;
        }
        return Boolean.valueOf(z);
    }

    public RcsMessageSnippet getMessageSnippet(int threadId, String callingPackage) {
        return (RcsMessageSnippet) performReadOperation(callingPackage, $$Lambda$RcsMessageStoreController$yW1mUOjjSZ_aoCdgW7hX38_lT8.INSTANCE);
    }

    static /* synthetic */ RcsMessageSnippet lambda$getMessageSnippet$1() throws RemoteException {
        return null;
    }

    public RcsThreadQueryResultParcelable getRcsThreads(RcsThreadQueryParams queryParameters, String callingPackage) {
        return (RcsThreadQueryResultParcelable) performReadOperation(callingPackage, new ThrowingSupplier(queryParameters) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$ClTeG1a5315E4yM3I5FjYPv_aqU */
            private final /* synthetic */ RcsThreadQueryParams f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getRcsThreads$2$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsThreadQueryResultParcelable lambda$getRcsThreads$2$RcsMessageStoreController(RcsThreadQueryParams queryParameters) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("thread_query_parameters", queryParameters);
        return this.mThreadQueryHelper.performThreadQuery(bundle);
    }

    public RcsThreadQueryResultParcelable getRcsThreadsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) {
        return (RcsThreadQueryResultParcelable) performReadOperation(callingPackage, new ThrowingSupplier(continuationToken) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$59OQ763FYDG6pocPJGJOTyticw0 */
            private final /* synthetic */ RcsQueryContinuationToken f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getRcsThreadsWithToken$3$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsThreadQueryResultParcelable lambda$getRcsThreadsWithToken$3$RcsMessageStoreController(RcsQueryContinuationToken continuationToken) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("query_continuation_token", continuationToken);
        return this.mThreadQueryHelper.performThreadQuery(bundle);
    }

    public RcsParticipantQueryResultParcelable getParticipants(RcsParticipantQueryParams queryParameters, String callingPackage) {
        return (RcsParticipantQueryResultParcelable) performReadOperation(callingPackage, new ThrowingSupplier(queryParameters) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$ysPz1siZPIdaDSnk6hkNkODHo */
            private final /* synthetic */ RcsParticipantQueryParams f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getParticipants$4$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsParticipantQueryResultParcelable lambda$getParticipants$4$RcsMessageStoreController(RcsParticipantQueryParams queryParameters) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("participant_query_parameters", queryParameters);
        return this.mParticipantQueryHelper.performParticipantQuery(bundle);
    }

    public RcsParticipantQueryResultParcelable getParticipantsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) {
        return (RcsParticipantQueryResultParcelable) performReadOperation(callingPackage, new ThrowingSupplier(continuationToken) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$th762XOIu1OKBMzBiiE5dtAuBnE */
            private final /* synthetic */ RcsQueryContinuationToken f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getParticipantsWithToken$5$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsParticipantQueryResultParcelable lambda$getParticipantsWithToken$5$RcsMessageStoreController(RcsQueryContinuationToken continuationToken) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("query_continuation_token", continuationToken);
        return this.mParticipantQueryHelper.performParticipantQuery(bundle);
    }

    public RcsMessageQueryResultParcelable getMessages(RcsMessageQueryParams queryParams, String callingPackage) {
        return (RcsMessageQueryResultParcelable) performReadOperation(callingPackage, new ThrowingSupplier(queryParams) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$8kZ0Whs2V3H2_A7PZtgThC9cHrM */
            private final /* synthetic */ RcsMessageQueryParams f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessages$6$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsMessageQueryResultParcelable lambda$getMessages$6$RcsMessageStoreController(RcsMessageQueryParams queryParams) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("message_query_parameters", queryParams);
        return this.mMessageQueryHelper.performMessageQuery(bundle);
    }

    public RcsMessageQueryResultParcelable getMessagesWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) {
        return (RcsMessageQueryResultParcelable) performReadOperation(callingPackage, new ThrowingSupplier(continuationToken) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$yDvbl7WUiKHa8mgKb4dC0kTmU */
            private final /* synthetic */ RcsQueryContinuationToken f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessagesWithToken$7$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsMessageQueryResultParcelable lambda$getMessagesWithToken$7$RcsMessageStoreController(RcsQueryContinuationToken continuationToken) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("query_continuation_token", continuationToken);
        return this.mMessageQueryHelper.performMessageQuery(bundle);
    }

    public RcsEventQueryResultDescriptor getEvents(RcsEventQueryParams queryParameters, String callingPackage) {
        return (RcsEventQueryResultDescriptor) performReadOperation(callingPackage, new ThrowingSupplier(queryParameters) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$LbCZDtv0n6njHJFbdiZw0tky6vs */
            private final /* synthetic */ RcsEventQueryParams f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getEvents$8$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsEventQueryResultDescriptor lambda$getEvents$8$RcsMessageStoreController(RcsEventQueryParams queryParameters) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("event_query_parameters", queryParameters);
        return this.mEventQueryHelper.performEventQuery(bundle);
    }

    public RcsEventQueryResultDescriptor getEventsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) {
        return (RcsEventQueryResultDescriptor) performReadOperation(callingPackage, new ThrowingSupplier(continuationToken) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$uY3LpbCjF8RJpCw0F59FfBzKsJo */
            private final /* synthetic */ RcsQueryContinuationToken f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getEventsWithToken$9$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ RcsEventQueryResultDescriptor lambda$getEventsWithToken$9$RcsMessageStoreController(RcsQueryContinuationToken continuationToken) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("query_continuation_token", continuationToken);
        return this.mEventQueryHelper.performEventQuery(bundle);
    }

    public int createRcs1To1Thread(int recipientId, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(recipientId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$gpg3zSTPdicDEJNClha72fjiHY */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createRcs1To1Thread$10$RcsMessageStoreController(this.f$1);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createRcs1To1Thread$10$RcsMessageStoreController(int recipientId) throws RemoteException {
        return Integer.valueOf(this.mThreadQueryHelper.create1To1Thread(recipientId));
    }

    public int createGroupThread(int[] participantIds, String groupName, Uri groupIcon, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(groupName, groupIcon, participantIds, callingPackage) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$wooijl9hUm68TegPWxMOAOSGs8 */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ Uri f$2;
            private final /* synthetic */ int[] f$3;
            private final /* synthetic */ String f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createGroupThread$11$RcsMessageStoreController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createGroupThread$11$RcsMessageStoreController(String groupName, Uri groupIcon, int[] participantIds, String callingPackage) throws RemoteException {
        int groupThreadId = this.mThreadQueryHelper.createGroupThread(groupName, groupIcon);
        if (groupThreadId > 0) {
            if (participantIds != null) {
                for (int participantId : participantIds) {
                    addParticipantToGroupThread(groupThreadId, participantId, callingPackage);
                }
            }
            return Integer.valueOf(groupThreadId);
        }
        throw new RemoteException("Could not create RcsGroupThread.");
    }

    public int createRcsParticipant(String canonicalAddress, String alias, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(canonicalAddress, alias) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$H8kZkoDGWxnaPfrHVAB6QaOOEK4 */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createRcsParticipant$12$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createRcsParticipant$12$RcsMessageStoreController(String canonicalAddress, String alias) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        long canonicalAddressId = Telephony.RcsColumns.RcsCanonicalAddressHelper.getOrCreateCanonicalAddressId(this.mContentResolver, canonicalAddress);
        if (canonicalAddressId != -2147483648L) {
            contentValues.put("canonical_address_id", Long.valueOf(canonicalAddressId));
            contentValues.put("rcs_alias", alias);
            Uri newParticipantUri = this.mContentResolver.insert(Telephony.RcsColumns.RcsParticipantColumns.RCS_PARTICIPANT_URI, contentValues);
            if (newParticipantUri != null) {
                try {
                    return Integer.valueOf(Integer.parseInt(newParticipantUri.getLastPathSegment()));
                } catch (NumberFormatException e) {
                    throw new RemoteException("Uri returned after creating a participant is malformed: " + newParticipantUri);
                }
            } else {
                throw new RemoteException("Error inserting new participant into RcsProvider");
            }
        } else {
            throw new RemoteException("Could not create or make canonical address entry");
        }
    }

    public String getRcsParticipantCanonicalAddress(int participantId, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(participantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$d2kuYkT0m7BtnEDDoFJO8Ff4aik */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getRcsParticipantCanonicalAddress$13$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ String lambda$getRcsParticipantCanonicalAddress$13$RcsMessageStoreController(int participantId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(Telephony.RcsColumns.RcsParticipantColumns.RCS_PARTICIPANT_URI, "address", "rcs_participant_id", participantId);
    }

    public String getRcsParticipantAlias(int participantId, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(participantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$I8TYhXVjUPl2Qbe8Oh1Ed3yhWNw */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getRcsParticipantAlias$14$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ String lambda$getRcsParticipantAlias$14$RcsMessageStoreController(int participantId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(Telephony.RcsColumns.RcsParticipantColumns.RCS_PARTICIPANT_URI, "rcs_alias", "rcs_participant_id", participantId);
    }

    public void setRcsParticipantAlias(int id, String alias, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(id, alias) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$GMu9xFuH6Pwj30sjJjU3zueFCNY */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setRcsParticipantAlias$15$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setRcsParticipantAlias$15$RcsMessageStoreController(int id, String alias) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(RcsParticipantQueryHelper.getUriForParticipant(id), "rcs_alias", alias, "Could not update RCS participant alias");
    }

    public String getRcsParticipantContactId(int participantId, String callingPackage) {
        return (String) performReadOperation(callingPackage, $$Lambda$RcsMessageStoreController$75youkeK6UPDR54BvRLBXlMeuw.INSTANCE);
    }

    static /* synthetic */ String lambda$getRcsParticipantContactId$16() throws RemoteException {
        return null;
    }

    public void setRcsParticipantContactId(int participantId, String contactId, String callingPackage) {
        performWriteOperation(callingPackage, $$Lambda$RcsMessageStoreController$n7YWjkBre8yAm3X4Ma8Y6IJulUU.INSTANCE);
    }

    static /* synthetic */ void lambda$setRcsParticipantContactId$17() throws RemoteException {
    }

    public void set1To1ThreadFallbackThreadId(int rcsThreadId, long fallbackId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(rcsThreadId, fallbackId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$RAd62JxQGCnFeaVRbYvfRMq2nMQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$set1To1ThreadFallbackThreadId$18$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$set1To1ThreadFallbackThreadId$18$RcsMessageStoreController(int rcsThreadId, long fallbackId) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(RcsThreadQueryHelper.get1To1ThreadUri(rcsThreadId), "rcs_fallback_thread_id", fallbackId, "Could not set fallback thread ID");
    }

    public long get1To1ThreadFallbackThreadId(int rcsThreadId, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(rcsThreadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$Kv0WzjSrvy9VSan2OBtU0Bx6ETs */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$get1To1ThreadFallbackThreadId$19$RcsMessageStoreController(this.f$1);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$get1To1ThreadFallbackThreadId$19$RcsMessageStoreController(int rcsThreadId) throws RemoteException {
        return Long.valueOf(this.mMessageStoreUtil.getLongValueFromTableRow(Telephony.RcsColumns.Rcs1To1ThreadColumns.RCS_1_TO_1_THREAD_URI, "rcs_fallback_thread_id", "rcs_thread_id", rcsThreadId));
    }

    public int get1To1ThreadOtherParticipantId(int rcsThreadId, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(rcsThreadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$C_S6XDOl4j0OCArwRwMKCC1AaRc */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$get1To1ThreadOtherParticipantId$20$RcsMessageStoreController(this.f$1);
            }
        })).intValue();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0039, code lost:
        if (r3 != null) goto L_0x003b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003f, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0040, code lost:
        r1.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        throw r4;
     */
    public /* synthetic */ Integer lambda$get1To1ThreadOtherParticipantId$20$RcsMessageStoreController(int rcsThreadId) throws RemoteException {
        Cursor cursor = this.mContentResolver.query(RcsThreadQueryHelper.get1To1ThreadUri(rcsThreadId), new String[]{"rcs_participant_id"}, null, null);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToNext();
                Integer valueOf = Integer.valueOf(cursor.getInt(cursor.getColumnIndex("rcs_participant_id")));
                cursor.close();
                return valueOf;
            }
        }
        throw new RemoteException("Could not get the thread recipient");
    }

    public void setGroupThreadName(int rcsThreadId, String groupName, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(rcsThreadId, groupName) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$TpIpy59FjUIVg96luEg12VkT5aw */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setGroupThreadName$21$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setGroupThreadName$21$RcsMessageStoreController(int rcsThreadId, String groupName) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(RcsThreadQueryHelper.getGroupThreadUri(rcsThreadId), "group_name", groupName, "Could not update group name");
    }

    public String getGroupThreadName(int rcsThreadId, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(rcsThreadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$Aj3odzXvQEQBCSmamvCh9PVCEoo */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getGroupThreadName$22$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ String lambda$getGroupThreadName$22$RcsMessageStoreController(int rcsThreadId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI, "group_name", "rcs_thread_id", rcsThreadId);
    }

    public void setGroupThreadIcon(int rcsThreadId, Uri groupIcon, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(rcsThreadId, groupIcon) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$9wFprf97OtAZJet5n97zbx_SmAw */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ Uri f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setGroupThreadIcon$23$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setGroupThreadIcon$23$RcsMessageStoreController(int rcsThreadId, Uri groupIcon) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(RcsThreadQueryHelper.getGroupThreadUri(rcsThreadId), "group_icon", groupIcon, "Could not update group icon");
    }

    public Uri getGroupThreadIcon(int rcsThreadId, String callingPackage) {
        return (Uri) performReadOperation(callingPackage, new ThrowingSupplier(rcsThreadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$i5gf8gYSpfipcECfLUsMExN6FGE */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getGroupThreadIcon$24$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ Uri lambda$getGroupThreadIcon$24$RcsMessageStoreController(int rcsThreadId) throws RemoteException {
        return this.mMessageStoreUtil.getUriValueFromTableRow(Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI, "group_icon", "rcs_thread_id", rcsThreadId);
    }

    public void setGroupThreadOwner(int rcsThreadId, int participantId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(rcsThreadId, participantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$LKp6ysypBzOb0aWBWdCnqPDcLHA */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setGroupThreadOwner$25$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setGroupThreadOwner$25$RcsMessageStoreController(int rcsThreadId, int participantId) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(RcsThreadQueryHelper.getGroupThreadUri(rcsThreadId), "owner_participant", participantId, "Could not set the group owner");
    }

    public int getGroupThreadOwner(int rcsThreadId, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(rcsThreadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$FZtgkcQORLtmljGumeKt4RYKcDI */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getGroupThreadOwner$26$RcsMessageStoreController(this.f$1);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getGroupThreadOwner$26$RcsMessageStoreController(int rcsThreadId) throws RemoteException {
        return Integer.valueOf(this.mMessageStoreUtil.getIntValueFromTableRow(Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI, "owner_participant", "rcs_thread_id", rcsThreadId));
    }

    public void setGroupThreadConferenceUri(int rcsThreadId, Uri uri, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(rcsThreadId, uri) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$uHotLs093tvDOCQG2PWoegXouxA */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ Uri f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setGroupThreadConferenceUri$27$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setGroupThreadConferenceUri$27$RcsMessageStoreController(int rcsThreadId, Uri uri) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(RcsThreadQueryHelper.getGroupThreadUri(rcsThreadId), "conference_uri", uri, "Could not set the conference URI for group");
    }

    public Uri getGroupThreadConferenceUri(int rcsThreadId, String callingPackage) {
        return (Uri) performReadOperation(callingPackage, new ThrowingSupplier(rcsThreadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$I4OWMlhoGkKEhCcow2wPN5FG9VY */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getGroupThreadConferenceUri$28$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ Uri lambda$getGroupThreadConferenceUri$28$RcsMessageStoreController(int rcsThreadId) throws RemoteException {
        return this.mMessageStoreUtil.getUriValueFromTableRow(Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI, "conference_uri", "rcs_thread_id", rcsThreadId);
    }

    public void addParticipantToGroupThread(int rcsThreadId, int participantId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(rcsThreadId, participantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$aLn59mZ4fSyftj8qGtwnDws4upk */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$addParticipantToGroupThread$29$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$addParticipantToGroupThread$29$RcsMessageStoreController(int rcsThreadId, int participantId) throws RemoteException {
        ContentValues contentValues = new ContentValues(2);
        contentValues.put("rcs_thread_id", Integer.valueOf(rcsThreadId));
        contentValues.put("rcs_participant_id", Integer.valueOf(participantId));
        this.mContentResolver.insert(RcsThreadQueryHelper.getAllParticipantsInThreadUri(rcsThreadId), contentValues);
    }

    public void removeParticipantFromGroupThread(int rcsThreadId, int participantId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(rcsThreadId, participantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$JHzHPFCAseKapBbeL98bJFNAzig */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$removeParticipantFromGroupThread$30$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$removeParticipantFromGroupThread$30$RcsMessageStoreController(int rcsThreadId, int participantId) throws RemoteException {
        this.mContentResolver.delete(RcsThreadQueryHelper.getParticipantInThreadUri(rcsThreadId, participantId), null, null);
    }

    public int addIncomingMessage(int rcsThreadId, RcsIncomingMessageCreationParams rcsIncomingMessageCreationParams, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(rcsIncomingMessageCreationParams, rcsThreadId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$jiseLvnaiI_ZGA6BgQU2fHwubyw */
            private final /* synthetic */ RcsIncomingMessageCreationParams f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$addIncomingMessage$31$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$addIncomingMessage$31$RcsMessageStoreController(RcsIncomingMessageCreationParams rcsIncomingMessageCreationParams, int rcsThreadId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put("arrival_timestamp", Long.valueOf(rcsIncomingMessageCreationParams.getArrivalTimestamp()));
        contentValues.put("seen_timestamp", Long.valueOf(rcsIncomingMessageCreationParams.getSeenTimestamp()));
        contentValues.put("sender_participant", Integer.valueOf(rcsIncomingMessageCreationParams.getSenderParticipantId()));
        this.mMessageQueryHelper.createContentValuesForGenericMessage(contentValues, rcsThreadId, rcsIncomingMessageCreationParams);
        return Integer.valueOf(addMessage(rcsThreadId, true, contentValues));
    }

    public int addOutgoingMessage(int rcsThreadId, RcsOutgoingMessageCreationParams rcsOutgoingMessageCreationParameters, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(rcsThreadId, rcsOutgoingMessageCreationParameters) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$40atfWQEcRbpUIloB6mwL9gyuIc */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ RcsOutgoingMessageCreationParams f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$addOutgoingMessage$32$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$addOutgoingMessage$32$RcsMessageStoreController(int rcsThreadId, RcsOutgoingMessageCreationParams rcsOutgoingMessageCreationParameters) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        this.mMessageQueryHelper.createContentValuesForGenericMessage(contentValues, rcsThreadId, rcsOutgoingMessageCreationParameters);
        return Integer.valueOf(addMessage(rcsThreadId, false, contentValues));
    }

    private int addMessage(int rcsThreadId, boolean isIncoming, ContentValues contentValues) throws RemoteException {
        Uri uri = this.mContentResolver.insert(this.mMessageQueryHelper.getMessageInsertionUri(isIncoming), contentValues);
        if (uri != null) {
            return Integer.parseInt(uri.getLastPathSegment());
        }
        throw new RemoteException("Could not create message on thread, threadId: " + rcsThreadId);
    }

    public void deleteMessage(int messageId, boolean isIncoming, int rcsThreadId, boolean isGroup, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, rcsThreadId, isGroup) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$obNp4lwMT0StmSSFcBaCx4A1s */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ boolean f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$deleteMessage$33$RcsMessageStoreController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$deleteMessage$33$RcsMessageStoreController(int messageId, boolean isIncoming, int rcsThreadId, boolean isGroup) throws RemoteException {
        this.mContentResolver.delete(this.mMessageQueryHelper.getMessageDeletionUri(messageId, isIncoming, rcsThreadId, isGroup), null, null);
    }

    public /* synthetic */ void lambda$setMessageSubId$34$RcsMessageStoreController(int messageId, boolean isIncoming, int subId) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "sub_id", subId, "Could not set subscription ID for message");
    }

    public void setMessageSubId(int messageId, boolean isIncoming, int subId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, subId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$Ydc70CwcfJDOBqlqBcqxTt38Uzo */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setMessageSubId$34$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public int getMessageSubId(int messageId, boolean isIncoming, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$jdfxgyc8mRmghuhEspSRnxyRt4Y */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessageSubId$35$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getMessageSubId$35$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return Integer.valueOf(this.mMessageStoreUtil.getIntValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "sub_id", "rcs_message_row_id", messageId));
    }

    public /* synthetic */ void lambda$setMessageStatus$36$RcsMessageStoreController(int messageId, boolean isIncoming, int status) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "status", status, "Could not set the status for message");
    }

    public void setMessageStatus(int messageId, boolean isIncoming, int status, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, status) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$oYdMpGEerSj76ai9qOJ48sYoY14 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setMessageStatus$36$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public int getMessageStatus(int messageId, boolean isIncoming, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$DenzMTC9mTdhbmGgInCtmmHclY */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessageStatus$37$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getMessageStatus$37$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return Integer.valueOf(this.mMessageStoreUtil.getIntValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "status", "rcs_message_row_id", messageId));
    }

    public /* synthetic */ void lambda$setMessageOriginationTimestamp$38$RcsMessageStoreController(int messageId, boolean isIncoming, long originationTimestamp) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "origination_timestamp", originationTimestamp, "Could not set the origination timestamp for message");
    }

    public void setMessageOriginationTimestamp(int messageId, boolean isIncoming, long originationTimestamp, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, originationTimestamp) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$EUwM1EwT7Lz6sUEnYRpBVA8Q4Yo */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ long f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setMessageOriginationTimestamp$38$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public long getMessageOriginationTimestamp(int messageId, boolean isIncoming, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$nHeOZX6Yex65rTb0Sr3YNhUFNI */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessageOriginationTimestamp$39$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getMessageOriginationTimestamp$39$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return Long.valueOf(this.mMessageStoreUtil.getLongValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "origination_timestamp", "rcs_message_row_id", messageId));
    }

    public /* synthetic */ void lambda$setGlobalMessageIdForMessage$40$RcsMessageStoreController(int messageId, boolean isIncoming, String globalId) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "rcs_message_global_id", globalId, "Could not set the global ID for message");
    }

    public void setGlobalMessageIdForMessage(int messageId, boolean isIncoming, String globalId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, globalId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$emhqhIZb6HnA9QWgvIl6aqRig */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setGlobalMessageIdForMessage$40$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public String getGlobalMessageIdForMessage(int messageId, boolean isIncoming, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$g2STMzf2abyGQ3T02uUssGDUYU */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getGlobalMessageIdForMessage$41$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ String lambda$getGlobalMessageIdForMessage$41$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "rcs_message_global_id", "rcs_message_row_id", messageId);
    }

    public /* synthetic */ void lambda$setMessageArrivalTimestamp$42$RcsMessageStoreController(int messageId, boolean isIncoming, long arrivalTimestamp) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "arrival_timestamp", arrivalTimestamp, "Could not update the arrival timestamp for message");
    }

    public void setMessageArrivalTimestamp(int messageId, boolean isIncoming, long arrivalTimestamp, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, arrivalTimestamp) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$kKfCdoAe6weg7LBn30W5ytjoBmg */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ long f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setMessageArrivalTimestamp$42$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public long getMessageArrivalTimestamp(int messageId, boolean isIncoming, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$rOTFWScZeGqI_FfpdXKvaj_6TaY */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessageArrivalTimestamp$43$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getMessageArrivalTimestamp$43$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return Long.valueOf(this.mMessageStoreUtil.getLongValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "arrival_timestamp", "rcs_message_row_id", messageId));
    }

    public /* synthetic */ void lambda$setMessageSeenTimestamp$44$RcsMessageStoreController(int messageId, boolean isIncoming, long notifiedTimestamp) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "seen_timestamp", notifiedTimestamp, "Could not set the notified timestamp for message");
    }

    public void setMessageSeenTimestamp(int messageId, boolean isIncoming, long notifiedTimestamp, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, notifiedTimestamp) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$ZbAi2Z4Zs7PCzpYK2ddr149tEyQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ long f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setMessageSeenTimestamp$44$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public long getMessageSeenTimestamp(int messageId, boolean isIncoming, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$YipgJUqRvwIQNiAwv0iAJe9jDg */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessageSeenTimestamp$45$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getMessageSeenTimestamp$45$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return Long.valueOf(this.mMessageStoreUtil.getLongValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "seen_timestamp", "rcs_message_row_id", messageId));
    }

    public int[] getMessageRecipients(int messageId, String callingPackage) {
        return (int[]) performReadOperation(callingPackage, new ThrowingSupplier(messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$dtjug27mcIESiWPv1SG5VdrJVNk */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getMessageRecipients$46$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ int[] lambda$getMessageRecipients$46$RcsMessageStoreController(int messageId) throws RemoteException {
        return this.mMessageQueryHelper.getDeliveryParticipantsForMessage(messageId);
    }

    public long getOutgoingDeliveryDeliveredTimestamp(int messageId, int participantId, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(messageId, participantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$cN0QvwaxRss2b1Nol38Q6BWlJb8 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getOutgoingDeliveryDeliveredTimestamp$47$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getOutgoingDeliveryDeliveredTimestamp$47$RcsMessageStoreController(int messageId, int participantId) throws RemoteException {
        return Long.valueOf(this.mMessageQueryHelper.getLongValueFromDelivery(messageId, participantId, "delivered_timestamp"));
    }

    public /* synthetic */ void lambda$setOutgoingDeliveryDeliveredTimestamp$48$RcsMessageStoreController(int messageId, int participantId, long deliveredTimestamp) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageDeliveryUri(messageId, participantId), "delivered_timestamp", deliveredTimestamp, "Could not update the delivered timestamp for outgoing delivery");
    }

    public void setOutgoingDeliveryDeliveredTimestamp(int messageId, int participantId, long deliveredTimestamp, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, participantId, deliveredTimestamp) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$K1FMaXVKrG26Oq2VVJlnciThdc */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ long f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setOutgoingDeliveryDeliveredTimestamp$48$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public long getOutgoingDeliverySeenTimestamp(int messageId, int participantId, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(messageId, participantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$hOq33zEjGZu3QxvphbbEytozff8 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getOutgoingDeliverySeenTimestamp$49$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getOutgoingDeliverySeenTimestamp$49$RcsMessageStoreController(int messageId, int participantId) throws RemoteException {
        return Long.valueOf(this.mMessageQueryHelper.getLongValueFromDelivery(messageId, participantId, "seen_timestamp"));
    }

    public /* synthetic */ void lambda$setOutgoingDeliverySeenTimestamp$50$RcsMessageStoreController(int messageId, int participantId, long seenTimestamp) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageDeliveryUri(messageId, participantId), "seen_timestamp", seenTimestamp, "Could not update the seen timestamp for outgoing delivery");
    }

    public void setOutgoingDeliverySeenTimestamp(int messageId, int participantId, long seenTimestamp, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, participantId, seenTimestamp) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$jHeKnabsIeh0lBHfJyzVOO62F0 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ long f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setOutgoingDeliverySeenTimestamp$50$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public int getOutgoingDeliveryStatus(int messageId, int participantId, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, $$Lambda$RcsMessageStoreController$ql9qCCnj1UThMFRJGcj36kFApKA.INSTANCE)).intValue();
    }

    static /* synthetic */ Integer lambda$getOutgoingDeliveryStatus$51() throws RemoteException {
        return 0;
    }

    public void setOutgoingDeliveryStatus(int messageId, int participantId, int status, String callingPackage) {
        performWriteOperation(callingPackage, $$Lambda$RcsMessageStoreController$Abaq2KJr5w02nAfSHDpHL8cbGCM.INSTANCE);
    }

    static /* synthetic */ void lambda$setOutgoingDeliveryStatus$52() throws RemoteException {
    }

    public /* synthetic */ void lambda$setTextForMessage$53$RcsMessageStoreController(int messageId, boolean isIncoming, String text) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "rcs_text", text, "Could not set the text for message");
    }

    public void setTextForMessage(int messageId, boolean isIncoming, String text, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, text) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$fgKkPLRhFZnK35RO00UEoYUhO3I */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setTextForMessage$53$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public String getTextForMessage(int messageId, boolean isIncoming, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$nCbhDPjKFA0Jm53EFmbQexceNLQ */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getTextForMessage$54$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ String lambda$getTextForMessage$54$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "rcs_text", "rcs_message_row_id", messageId);
    }

    public /* synthetic */ void lambda$setLatitudeForMessage$55$RcsMessageStoreController(int messageId, boolean isIncoming, double latitude) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "latitude", latitude, "Could not update latitude for message");
    }

    public void setLatitudeForMessage(int messageId, boolean isIncoming, double latitude, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, latitude) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$xC3HWhR80aJOt1X6r83yAZM3tUQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ double f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setLatitudeForMessage$55$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public double getLatitudeForMessage(int messageId, boolean isIncoming, String callingPackage) {
        return ((Double) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$Cb4PItW7V1g69LCBQGhBiRRtz4 */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getLatitudeForMessage$56$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).doubleValue();
    }

    public /* synthetic */ Double lambda$getLatitudeForMessage$56$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return Double.valueOf(this.mMessageStoreUtil.getDoubleValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "latitude", "rcs_message_row_id", messageId));
    }

    public /* synthetic */ void lambda$setLongitudeForMessage$57$RcsMessageStoreController(int messageId, boolean isIncoming, double longitude) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getMessageUpdateUri(messageId, isIncoming), "longitude", longitude, "Could not set longitude for message");
    }

    public void setLongitudeForMessage(int messageId, boolean isIncoming, double longitude, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(messageId, isIncoming, longitude) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$rUl7GR0q_Zo6dP_OpjyrnxyNLc4 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ double f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setLongitudeForMessage$57$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public double getLongitudeForMessage(int messageId, boolean isIncoming, String callingPackage) {
        return ((Double) performReadOperation(callingPackage, new ThrowingSupplier(isIncoming, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$DJ1U8cPQGcg90QJamjBaum8g4gc */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getLongitudeForMessage$58$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).doubleValue();
    }

    public /* synthetic */ Double lambda$getLongitudeForMessage$58$RcsMessageStoreController(boolean isIncoming, int messageId) throws RemoteException {
        return Double.valueOf(this.mMessageStoreUtil.getDoubleValueFromTableRow(RcsMessageStoreUtil.getMessageTableUri(isIncoming), "longitude", "rcs_message_row_id", messageId));
    }

    public int[] getFileTransfersAttachedToMessage(int messageId, boolean isIncoming, String callingPackage) {
        return (int[]) performReadOperation(callingPackage, $$Lambda$RcsMessageStoreController$dK7yaLArRjD5DmHyJNMMqtB22C4.INSTANCE);
    }

    static /* synthetic */ int[] lambda$getFileTransfersAttachedToMessage$59() throws RemoteException {
        return new int[0];
    }

    public int getSenderParticipant(int messageId, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$LMhPtb7AbEObwiwot1ObQconTrY */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getSenderParticipant$60$RcsMessageStoreController(this.f$1);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getSenderParticipant$60$RcsMessageStoreController(int messageId) throws RemoteException {
        return Integer.valueOf(this.mMessageStoreUtil.getIntValueFromTableRow(Telephony.RcsColumns.RcsIncomingMessageColumns.INCOMING_MESSAGE_URI, "sender_participant", "rcs_message_row_id", messageId));
    }

    public void deleteFileTransfer(int partId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$cI3HfQAJmekiQsJsCcTNShYosxw */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$deleteFileTransfer$61$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$deleteFileTransfer$61$RcsMessageStoreController(int partId) throws RemoteException {
        this.mContentResolver.delete(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), null, null);
    }

    public int storeFileTransfer(int messageId, boolean isIncoming, RcsFileTransferCreationParams fileTransferCreationParameters, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(fileTransferCreationParameters, messageId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$1jwBeSBvZDdyDtjPAHrOI2_Kp8 */
            private final /* synthetic */ RcsFileTransferCreationParams f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$storeFileTransfer$62$RcsMessageStoreController(this.f$1, this.f$2);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$storeFileTransfer$62$RcsMessageStoreController(RcsFileTransferCreationParams fileTransferCreationParameters, int messageId) throws RemoteException {
        Uri uri = this.mContentResolver.insert(this.mMessageQueryHelper.getFileTransferInsertionUri(messageId), this.mMessageQueryHelper.getContentValuesForFileTransfer(fileTransferCreationParameters));
        if (uri != null) {
            return Integer.valueOf(Integer.parseInt(uri.getLastPathSegment()));
        }
        return Integer.MIN_VALUE;
    }

    public /* synthetic */ void lambda$setFileTransferSessionId$63$RcsMessageStoreController(int partId, String sessionId) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "session_id", sessionId, "Could not set session ID for file transfer");
    }

    public void setFileTransferSessionId(int partId, String sessionId, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, sessionId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$CrJMOfHeqLMbEHM26Tzgd2h0xwc */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferSessionId$63$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public String getFileTransferSessionId(int partId, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$IUphvRVIxp54IApD01IQZxvMU */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferSessionId$64$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ String lambda$getFileTransferSessionId$64$RcsMessageStoreController(int partId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "session_id", "rcs_file_transfer_id", partId);
    }

    public /* synthetic */ void lambda$setFileTransferContentUri$65$RcsMessageStoreController(int partId, Uri contentUri) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "content_uri", contentUri, "Could not set content URI for file transfer");
    }

    public void setFileTransferContentUri(int partId, Uri contentUri, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, contentUri) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$VySBVSH4PKKZDjHYzvMaQ6sXAh4 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ Uri f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferContentUri$65$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public Uri getFileTransferContentUri(int partId, String callingPackage) {
        return (Uri) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$kwvr7eXsIkjI91KRQHY7Ht2JB4 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferContentUri$66$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ Uri lambda$getFileTransferContentUri$66$RcsMessageStoreController(int partId) throws RemoteException {
        return this.mMessageStoreUtil.getUriValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "content_uri", "rcs_file_transfer_id", partId);
    }

    public /* synthetic */ void lambda$setFileTransferContentType$67$RcsMessageStoreController(int partId, String contentType) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "content_type", contentType, "Could not set content type for file transfer");
    }

    public void setFileTransferContentType(int partId, String contentType, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, contentType) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$QuCMfZjaL38JKfdVdaXlhUciYg */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferContentType$67$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public String getFileTransferContentType(int partId, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$HSCl_3xd_STs1XaojnDci914C0 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferContentType$68$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ String lambda$getFileTransferContentType$68$RcsMessageStoreController(int partId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "content_type", "rcs_file_transfer_id", partId);
    }

    public /* synthetic */ void lambda$setFileTransferFileSize$69$RcsMessageStoreController(int partId, long fileSize) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "file_size", fileSize, "Could not set file size for file transfer");
    }

    public void setFileTransferFileSize(int partId, long fileSize, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, fileSize) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$NyzhqlBzK5HgvBBj0yIUKT7n6c */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferFileSize$69$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public long getFileTransferFileSize(int partId, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$tdK9Zp0b0YckcWYCGTylP1Sa4g */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferFileSize$70$RcsMessageStoreController(this.f$1);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getFileTransferFileSize$70$RcsMessageStoreController(int partId) throws RemoteException {
        return Long.valueOf(this.mMessageStoreUtil.getLongValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "file_size", "rcs_file_transfer_id", partId));
    }

    public /* synthetic */ void lambda$setFileTransferTransferOffset$71$RcsMessageStoreController(int partId, long transferOffset) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "transfer_offset", transferOffset, "Could not set transfer offset for file transfer");
    }

    public void setFileTransferTransferOffset(int partId, long transferOffset, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, transferOffset) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$_QH7KyUipgV0PmMsbFbK5_YvipI */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferTransferOffset$71$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public long getFileTransferTransferOffset(int partId, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$yv74ikmFfA7ogwNEv9RwRPIh3w */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferTransferOffset$72$RcsMessageStoreController(this.f$1);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getFileTransferTransferOffset$72$RcsMessageStoreController(int partId) throws RemoteException {
        return Long.valueOf(this.mMessageStoreUtil.getLongValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "transfer_offset", "rcs_file_transfer_id", partId));
    }

    public /* synthetic */ void lambda$setFileTransferStatus$73$RcsMessageStoreController(int partId, int transferStatus) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "transfer_status", transferStatus, "Could not set transfer status for file transfer");
    }

    public void setFileTransferStatus(int partId, int transferStatus, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, transferStatus) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$fyr6oqGnSJUp4Gt59YfyGGlG77Q */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferStatus$73$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public int getFileTransferStatus(int partId, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$aTBxVCDp4645Mr87rYLwljQQ11U */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferStatus$74$RcsMessageStoreController(this.f$1);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getFileTransferStatus$74$RcsMessageStoreController(int partId) throws RemoteException {
        return Integer.valueOf(this.mMessageStoreUtil.getIntValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "status", "rcs_file_transfer_id", partId));
    }

    public /* synthetic */ void lambda$setFileTransferWidth$75$RcsMessageStoreController(int partId, int width) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "width", width, "Could not set width of file transfer");
    }

    public void setFileTransferWidth(int partId, int width, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, width) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$lpHB7xyEmqwVVQoFOttd3ccPVsk */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferWidth$75$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public int getFileTransferWidth(int partId, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$DjIzQFPqrkv3Uo1dzNDROD0ozLs */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferWidth$76$RcsMessageStoreController(this.f$1);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getFileTransferWidth$76$RcsMessageStoreController(int partId) throws RemoteException {
        return Integer.valueOf(this.mMessageStoreUtil.getIntValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "width", "rcs_file_transfer_id", partId));
    }

    public /* synthetic */ void lambda$setFileTransferHeight$77$RcsMessageStoreController(int partId, int height) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "height", height, "Could not set height of file transfer");
    }

    public void setFileTransferHeight(int partId, int height, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, height) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$cOS8pT4JicoW_zRFZJ1cYJTDjoE */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferHeight$77$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public int getFileTransferHeight(int partId, String callingPackage) {
        return ((Integer) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$yS7DrFPmo7WnhiPUTQ_g6NDQjw */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferHeight$78$RcsMessageStoreController(this.f$1);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$getFileTransferHeight$78$RcsMessageStoreController(int partId) throws RemoteException {
        return Integer.valueOf(this.mMessageStoreUtil.getIntValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "height", "rcs_file_transfer_id", partId));
    }

    public /* synthetic */ void lambda$setFileTransferLength$79$RcsMessageStoreController(int partId, long length) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "duration", length, "Could not set length of file transfer");
    }

    public void setFileTransferLength(int partId, long length, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, length) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$MbBnVvQk8PdmMcn1WGDQaVhTok */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferLength$79$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public long getFileTransferLength(int partId, String callingPackage) {
        return ((Long) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$zi0MeLHsTlKqbg6lktWyXcOuEr4 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferLength$80$RcsMessageStoreController(this.f$1);
            }
        })).longValue();
    }

    public /* synthetic */ Long lambda$getFileTransferLength$80$RcsMessageStoreController(int partId) throws RemoteException {
        return Long.valueOf(this.mMessageStoreUtil.getLongValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "duration", "rcs_file_transfer_id", partId));
    }

    public /* synthetic */ void lambda$setFileTransferPreviewUri$81$RcsMessageStoreController(int partId, Uri uri) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "preview_uri", uri, "Could not set preview URI of file transfer");
    }

    public void setFileTransferPreviewUri(int partId, Uri uri, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, uri) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$d0MdM4tqzzQuPmkNRmZ3KJdjlE */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ Uri f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferPreviewUri$81$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public Uri getFileTransferPreviewUri(int partId, String callingPackage) {
        return (Uri) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$fbjeAKvq29PTxOBowvC2GclUNIA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferPreviewUri$82$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ Uri lambda$getFileTransferPreviewUri$82$RcsMessageStoreController(int partId) throws RemoteException {
        return this.mMessageStoreUtil.getUriValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "duration", "rcs_file_transfer_id", partId);
    }

    public /* synthetic */ void lambda$setFileTransferPreviewType$83$RcsMessageStoreController(int partId, String type) throws RemoteException {
        this.mMessageStoreUtil.updateValueOfProviderUri(this.mMessageQueryHelper.getFileTransferUpdateUri(partId), "preview_type", type, "Could not set preview type of file transfer");
    }

    public void setFileTransferPreviewType(int partId, String type, String callingPackage) {
        performWriteOperation(callingPackage, new ThrowingRunnable(partId, type) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$zWLrx_FcTUxnS1nsFt35WphvPII */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingRunnable
            public final void run() {
                RcsMessageStoreController.this.lambda$setFileTransferPreviewType$83$RcsMessageStoreController(this.f$1, this.f$2);
            }
        });
    }

    public String getFileTransferPreviewType(int partId, String callingPackage) {
        return (String) performReadOperation(callingPackage, new ThrowingSupplier(partId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$2vqOScXnTngkQ9yzhAYC8e3AHUU */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$getFileTransferPreviewType$84$RcsMessageStoreController(this.f$1);
            }
        });
    }

    public /* synthetic */ String lambda$getFileTransferPreviewType$84$RcsMessageStoreController(int partId) throws RemoteException {
        return this.mMessageStoreUtil.getStringValueFromTableRow(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, "preview_type", "rcs_file_transfer_id", partId);
    }

    public int createGroupThreadNameChangedEvent(long timestamp, int threadId, int originationParticipantId, String newName, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(newName, timestamp, threadId, originationParticipantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$mQeh9a23oigBT3qH7by7Bi8LlEU */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r5;
                this.f$4 = r6;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createGroupThreadNameChangedEvent$85$RcsMessageStoreController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createGroupThreadNameChangedEvent$85$RcsMessageStoreController(String newName, long timestamp, int threadId, int originationParticipantId) throws RemoteException {
        ContentValues eventSpecificValues = new ContentValues();
        eventSpecificValues.put("new_name", newName);
        return Integer.valueOf(this.mEventQueryHelper.createGroupThreadEvent(16, timestamp, threadId, originationParticipantId, eventSpecificValues));
    }

    public int createGroupThreadIconChangedEvent(long timestamp, int threadId, int originationParticipantId, Uri newIcon, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(newIcon, timestamp, threadId, originationParticipantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$4U3TsrRCd3QMjXYC5EsUpGmVMTw */
            private final /* synthetic */ Uri f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r5;
                this.f$4 = r6;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createGroupThreadIconChangedEvent$86$RcsMessageStoreController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createGroupThreadIconChangedEvent$86$RcsMessageStoreController(Uri newIcon, long timestamp, int threadId, int originationParticipantId) throws RemoteException {
        ContentValues eventSpecificValues = new ContentValues();
        eventSpecificValues.put("new_icon_uri", newIcon == null ? null : newIcon.toString());
        return Integer.valueOf(this.mEventQueryHelper.createGroupThreadEvent(8, timestamp, threadId, originationParticipantId, eventSpecificValues));
    }

    public int createGroupThreadParticipantJoinedEvent(long timestamp, int threadId, int originationParticipantId, int participantId, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(participantId, timestamp, threadId, originationParticipantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$gwncfskzeEFKLz89VwsUiuq7rr4 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r5;
                this.f$4 = r6;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createGroupThreadParticipantJoinedEvent$87$RcsMessageStoreController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createGroupThreadParticipantJoinedEvent$87$RcsMessageStoreController(int participantId, long timestamp, int threadId, int originationParticipantId) throws RemoteException {
        ContentValues eventSpecificValues = new ContentValues();
        eventSpecificValues.put("destination_participant", Integer.valueOf(participantId));
        return Integer.valueOf(this.mEventQueryHelper.createGroupThreadEvent(2, timestamp, threadId, originationParticipantId, eventSpecificValues));
    }

    public int createGroupThreadParticipantLeftEvent(long timestamp, int threadId, int originationParticipantId, int participantId, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(participantId, timestamp, threadId, originationParticipantId) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$sAlZvDr2whzxoiLJXOIiypxEVM */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r5;
                this.f$4 = r6;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createGroupThreadParticipantLeftEvent$88$RcsMessageStoreController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createGroupThreadParticipantLeftEvent$88$RcsMessageStoreController(int participantId, long timestamp, int threadId, int originationParticipantId) throws RemoteException {
        ContentValues eventSpecificValues = new ContentValues();
        eventSpecificValues.put("destination_participant", Integer.valueOf(participantId));
        return Integer.valueOf(this.mEventQueryHelper.createGroupThreadEvent(4, timestamp, threadId, originationParticipantId, eventSpecificValues));
    }

    public int createParticipantAliasChangedEvent(long timestamp, int participantId, String newAlias, String callingPackage) {
        return ((Integer) performCreateOperation(callingPackage, new ThrowingSupplier(timestamp, participantId, newAlias) {
            /* class com.android.internal.telephony.ims.$$Lambda$RcsMessageStoreController$d1hmXh2zI4_jeUX_WBp_HcCjlE */
            private final /* synthetic */ long f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r5;
            }

            @Override // com.android.internal.telephony.ims.RcsMessageStoreController.ThrowingSupplier
            public final Object get() {
                return RcsMessageStoreController.this.lambda$createParticipantAliasChangedEvent$89$RcsMessageStoreController(this.f$1, this.f$2, this.f$3);
            }
        })).intValue();
    }

    public /* synthetic */ Integer lambda$createParticipantAliasChangedEvent$89$RcsMessageStoreController(long timestamp, int participantId, String newAlias) throws RemoteException {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put("origination_timestamp", Long.valueOf(timestamp));
        contentValues.put("source_participant", Integer.valueOf(participantId));
        contentValues.put("new_alias", newAlias);
        Uri uri = this.mContentResolver.insert(this.mEventQueryHelper.getParticipantEventInsertionUri(participantId), contentValues);
        if (uri != null) {
            return Integer.valueOf(Integer.parseInt(uri.getLastPathSegment()));
        }
        throw new RemoteException("Could not create RcsParticipantAliasChangedEvent with participant id: " + participantId);
    }
}
