package android.telephony.ims.aidl;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
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

public interface IRcs extends IInterface {
    int addIncomingMessage(int i, RcsIncomingMessageCreationParams rcsIncomingMessageCreationParams, String str) throws RemoteException;

    int addOutgoingMessage(int i, RcsOutgoingMessageCreationParams rcsOutgoingMessageCreationParams, String str) throws RemoteException;

    void addParticipantToGroupThread(int i, int i2, String str) throws RemoteException;

    int createGroupThread(int[] iArr, String str, Uri uri, String str2) throws RemoteException;

    int createGroupThreadIconChangedEvent(long j, int i, int i2, Uri uri, String str) throws RemoteException;

    int createGroupThreadNameChangedEvent(long j, int i, int i2, String str, String str2) throws RemoteException;

    int createGroupThreadParticipantJoinedEvent(long j, int i, int i2, int i3, String str) throws RemoteException;

    int createGroupThreadParticipantLeftEvent(long j, int i, int i2, int i3, String str) throws RemoteException;

    int createParticipantAliasChangedEvent(long j, int i, String str, String str2) throws RemoteException;

    int createRcs1To1Thread(int i, String str) throws RemoteException;

    int createRcsParticipant(String str, String str2, String str3) throws RemoteException;

    void deleteFileTransfer(int i, String str) throws RemoteException;

    void deleteMessage(int i, boolean z, int i2, boolean z2, String str) throws RemoteException;

    boolean deleteThread(int i, int i2, String str) throws RemoteException;

    long get1To1ThreadFallbackThreadId(int i, String str) throws RemoteException;

    int get1To1ThreadOtherParticipantId(int i, String str) throws RemoteException;

    RcsEventQueryResultDescriptor getEvents(RcsEventQueryParams rcsEventQueryParams, String str) throws RemoteException;

    RcsEventQueryResultDescriptor getEventsWithToken(RcsQueryContinuationToken rcsQueryContinuationToken, String str) throws RemoteException;

    String getFileTransferContentType(int i, String str) throws RemoteException;

    Uri getFileTransferContentUri(int i, String str) throws RemoteException;

    long getFileTransferFileSize(int i, String str) throws RemoteException;

    int getFileTransferHeight(int i, String str) throws RemoteException;

    long getFileTransferLength(int i, String str) throws RemoteException;

    String getFileTransferPreviewType(int i, String str) throws RemoteException;

    Uri getFileTransferPreviewUri(int i, String str) throws RemoteException;

    String getFileTransferSessionId(int i, String str) throws RemoteException;

    int getFileTransferStatus(int i, String str) throws RemoteException;

    long getFileTransferTransferOffset(int i, String str) throws RemoteException;

    int getFileTransferWidth(int i, String str) throws RemoteException;

    int[] getFileTransfersAttachedToMessage(int i, boolean z, String str) throws RemoteException;

    String getGlobalMessageIdForMessage(int i, boolean z, String str) throws RemoteException;

    Uri getGroupThreadConferenceUri(int i, String str) throws RemoteException;

    Uri getGroupThreadIcon(int i, String str) throws RemoteException;

    String getGroupThreadName(int i, String str) throws RemoteException;

    int getGroupThreadOwner(int i, String str) throws RemoteException;

    double getLatitudeForMessage(int i, boolean z, String str) throws RemoteException;

    double getLongitudeForMessage(int i, boolean z, String str) throws RemoteException;

    long getMessageArrivalTimestamp(int i, boolean z, String str) throws RemoteException;

    long getMessageOriginationTimestamp(int i, boolean z, String str) throws RemoteException;

    int[] getMessageRecipients(int i, String str) throws RemoteException;

    long getMessageSeenTimestamp(int i, boolean z, String str) throws RemoteException;

    RcsMessageSnippet getMessageSnippet(int i, String str) throws RemoteException;

    int getMessageStatus(int i, boolean z, String str) throws RemoteException;

    int getMessageSubId(int i, boolean z, String str) throws RemoteException;

    RcsMessageQueryResultParcelable getMessages(RcsMessageQueryParams rcsMessageQueryParams, String str) throws RemoteException;

    RcsMessageQueryResultParcelable getMessagesWithToken(RcsQueryContinuationToken rcsQueryContinuationToken, String str) throws RemoteException;

    long getOutgoingDeliveryDeliveredTimestamp(int i, int i2, String str) throws RemoteException;

    long getOutgoingDeliverySeenTimestamp(int i, int i2, String str) throws RemoteException;

    int getOutgoingDeliveryStatus(int i, int i2, String str) throws RemoteException;

    RcsParticipantQueryResultParcelable getParticipants(RcsParticipantQueryParams rcsParticipantQueryParams, String str) throws RemoteException;

    RcsParticipantQueryResultParcelable getParticipantsWithToken(RcsQueryContinuationToken rcsQueryContinuationToken, String str) throws RemoteException;

    String getRcsParticipantAlias(int i, String str) throws RemoteException;

    String getRcsParticipantCanonicalAddress(int i, String str) throws RemoteException;

    String getRcsParticipantContactId(int i, String str) throws RemoteException;

    RcsThreadQueryResultParcelable getRcsThreads(RcsThreadQueryParams rcsThreadQueryParams, String str) throws RemoteException;

    RcsThreadQueryResultParcelable getRcsThreadsWithToken(RcsQueryContinuationToken rcsQueryContinuationToken, String str) throws RemoteException;

    int getSenderParticipant(int i, String str) throws RemoteException;

    String getTextForMessage(int i, boolean z, String str) throws RemoteException;

    void removeParticipantFromGroupThread(int i, int i2, String str) throws RemoteException;

    void set1To1ThreadFallbackThreadId(int i, long j, String str) throws RemoteException;

    void setFileTransferContentType(int i, String str, String str2) throws RemoteException;

    void setFileTransferContentUri(int i, Uri uri, String str) throws RemoteException;

    void setFileTransferFileSize(int i, long j, String str) throws RemoteException;

    void setFileTransferHeight(int i, int i2, String str) throws RemoteException;

    void setFileTransferLength(int i, long j, String str) throws RemoteException;

    void setFileTransferPreviewType(int i, String str, String str2) throws RemoteException;

    void setFileTransferPreviewUri(int i, Uri uri, String str) throws RemoteException;

    void setFileTransferSessionId(int i, String str, String str2) throws RemoteException;

    void setFileTransferStatus(int i, int i2, String str) throws RemoteException;

    void setFileTransferTransferOffset(int i, long j, String str) throws RemoteException;

    void setFileTransferWidth(int i, int i2, String str) throws RemoteException;

    void setGlobalMessageIdForMessage(int i, boolean z, String str, String str2) throws RemoteException;

    void setGroupThreadConferenceUri(int i, Uri uri, String str) throws RemoteException;

    void setGroupThreadIcon(int i, Uri uri, String str) throws RemoteException;

    void setGroupThreadName(int i, String str, String str2) throws RemoteException;

    void setGroupThreadOwner(int i, int i2, String str) throws RemoteException;

    void setLatitudeForMessage(int i, boolean z, double d, String str) throws RemoteException;

    void setLongitudeForMessage(int i, boolean z, double d, String str) throws RemoteException;

    void setMessageArrivalTimestamp(int i, boolean z, long j, String str) throws RemoteException;

    void setMessageOriginationTimestamp(int i, boolean z, long j, String str) throws RemoteException;

    void setMessageSeenTimestamp(int i, boolean z, long j, String str) throws RemoteException;

    void setMessageStatus(int i, boolean z, int i2, String str) throws RemoteException;

    void setMessageSubId(int i, boolean z, int i2, String str) throws RemoteException;

    void setOutgoingDeliveryDeliveredTimestamp(int i, int i2, long j, String str) throws RemoteException;

    void setOutgoingDeliverySeenTimestamp(int i, int i2, long j, String str) throws RemoteException;

    void setOutgoingDeliveryStatus(int i, int i2, int i3, String str) throws RemoteException;

    void setRcsParticipantAlias(int i, String str, String str2) throws RemoteException;

    void setRcsParticipantContactId(int i, String str, String str2) throws RemoteException;

    void setTextForMessage(int i, boolean z, String str, String str2) throws RemoteException;

    int storeFileTransfer(int i, boolean z, RcsFileTransferCreationParams rcsFileTransferCreationParams, String str) throws RemoteException;

    public static class Default implements IRcs {
        @Override // android.telephony.ims.aidl.IRcs
        public RcsThreadQueryResultParcelable getRcsThreads(RcsThreadQueryParams queryParams, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsThreadQueryResultParcelable getRcsThreadsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsParticipantQueryResultParcelable getParticipants(RcsParticipantQueryParams queryParams, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsParticipantQueryResultParcelable getParticipantsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsMessageQueryResultParcelable getMessages(RcsMessageQueryParams queryParams, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsMessageQueryResultParcelable getMessagesWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsEventQueryResultDescriptor getEvents(RcsEventQueryParams queryParams, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsEventQueryResultDescriptor getEventsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public boolean deleteThread(int threadId, int threadType, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createRcs1To1Thread(int participantId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createGroupThread(int[] participantIds, String groupName, Uri groupIcon, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int addIncomingMessage(int rcsThreadId, RcsIncomingMessageCreationParams rcsIncomingMessageCreationParams, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int addOutgoingMessage(int rcsThreadId, RcsOutgoingMessageCreationParams rcsOutgoingMessageCreationParams, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void deleteMessage(int rcsMessageId, boolean isIncoming, int rcsThreadId, boolean isGroup, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public RcsMessageSnippet getMessageSnippet(int rcsThreadId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void set1To1ThreadFallbackThreadId(int rcsThreadId, long fallbackId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long get1To1ThreadFallbackThreadId(int rcsThreadId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int get1To1ThreadOtherParticipantId(int rcsThreadId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setGroupThreadName(int rcsThreadId, String groupName, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getGroupThreadName(int rcsThreadId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setGroupThreadIcon(int rcsThreadId, Uri groupIcon, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public Uri getGroupThreadIcon(int rcsThreadId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setGroupThreadOwner(int rcsThreadId, int participantId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getGroupThreadOwner(int rcsThreadId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setGroupThreadConferenceUri(int rcsThreadId, Uri conferenceUri, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public Uri getGroupThreadConferenceUri(int rcsThreadId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void addParticipantToGroupThread(int rcsThreadId, int participantId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void removeParticipantFromGroupThread(int rcsThreadId, int participantId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createRcsParticipant(String canonicalAddress, String alias, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getRcsParticipantCanonicalAddress(int participantId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getRcsParticipantAlias(int participantId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setRcsParticipantAlias(int id, String alias, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getRcsParticipantContactId(int participantId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setRcsParticipantContactId(int participantId, String contactId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setMessageSubId(int messageId, boolean isIncoming, int subId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getMessageSubId(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setMessageStatus(int messageId, boolean isIncoming, int status, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getMessageStatus(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setMessageOriginationTimestamp(int messageId, boolean isIncoming, long originationTimestamp, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getMessageOriginationTimestamp(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setGlobalMessageIdForMessage(int messageId, boolean isIncoming, String globalId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getGlobalMessageIdForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setMessageArrivalTimestamp(int messageId, boolean isIncoming, long arrivalTimestamp, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getMessageArrivalTimestamp(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setMessageSeenTimestamp(int messageId, boolean isIncoming, long seenTimestamp, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getMessageSeenTimestamp(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setTextForMessage(int messageId, boolean isIncoming, String text, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getTextForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setLatitudeForMessage(int messageId, boolean isIncoming, double latitude, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public double getLatitudeForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return 0.0d;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setLongitudeForMessage(int messageId, boolean isIncoming, double longitude, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public double getLongitudeForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return 0.0d;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int[] getFileTransfersAttachedToMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getSenderParticipant(int messageId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int[] getMessageRecipients(int messageId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getOutgoingDeliveryDeliveredTimestamp(int messageId, int participantId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setOutgoingDeliveryDeliveredTimestamp(int messageId, int participantId, long deliveredTimestamp, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getOutgoingDeliverySeenTimestamp(int messageId, int participantId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setOutgoingDeliverySeenTimestamp(int messageId, int participantId, long seenTimestamp, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getOutgoingDeliveryStatus(int messageId, int participantId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setOutgoingDeliveryStatus(int messageId, int participantId, int status, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int storeFileTransfer(int messageId, boolean isIncoming, RcsFileTransferCreationParams fileTransferCreationParams, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void deleteFileTransfer(int partId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferSessionId(int partId, String sessionId, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getFileTransferSessionId(int partId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferContentUri(int partId, Uri contentUri, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public Uri getFileTransferContentUri(int partId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferContentType(int partId, String contentType, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getFileTransferContentType(int partId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferFileSize(int partId, long fileSize, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getFileTransferFileSize(int partId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferTransferOffset(int partId, long transferOffset, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getFileTransferTransferOffset(int partId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferStatus(int partId, int transferStatus, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getFileTransferStatus(int partId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferWidth(int partId, int width, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getFileTransferWidth(int partId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferHeight(int partId, int height, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int getFileTransferHeight(int partId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferLength(int partId, long length, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public long getFileTransferLength(int partId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferPreviewUri(int partId, Uri uri, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public Uri getFileTransferPreviewUri(int partId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public void setFileTransferPreviewType(int partId, String type, String callingPackage) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IRcs
        public String getFileTransferPreviewType(int partId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createGroupThreadNameChangedEvent(long timestamp, int threadId, int originationParticipantId, String newName, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createGroupThreadIconChangedEvent(long timestamp, int threadId, int originationParticipantId, Uri newIcon, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createGroupThreadParticipantJoinedEvent(long timestamp, int threadId, int originationParticipantId, int participantId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createGroupThreadParticipantLeftEvent(long timestamp, int threadId, int originationParticipantId, int participantId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IRcs
        public int createParticipantAliasChangedEvent(long timestamp, int participantId, String newAlias, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRcs {
        private static final String DESCRIPTOR = "android.telephony.ims.aidl.IRcs";
        static final int TRANSACTION_addIncomingMessage = 12;
        static final int TRANSACTION_addOutgoingMessage = 13;
        static final int TRANSACTION_addParticipantToGroupThread = 27;
        static final int TRANSACTION_createGroupThread = 11;
        static final int TRANSACTION_createGroupThreadIconChangedEvent = 87;
        static final int TRANSACTION_createGroupThreadNameChangedEvent = 86;
        static final int TRANSACTION_createGroupThreadParticipantJoinedEvent = 88;
        static final int TRANSACTION_createGroupThreadParticipantLeftEvent = 89;
        static final int TRANSACTION_createParticipantAliasChangedEvent = 90;
        static final int TRANSACTION_createRcs1To1Thread = 10;
        static final int TRANSACTION_createRcsParticipant = 29;
        static final int TRANSACTION_deleteFileTransfer = 63;
        static final int TRANSACTION_deleteMessage = 14;
        static final int TRANSACTION_deleteThread = 9;
        static final int TRANSACTION_get1To1ThreadFallbackThreadId = 17;
        static final int TRANSACTION_get1To1ThreadOtherParticipantId = 18;
        static final int TRANSACTION_getEvents = 7;
        static final int TRANSACTION_getEventsWithToken = 8;
        static final int TRANSACTION_getFileTransferContentType = 69;
        static final int TRANSACTION_getFileTransferContentUri = 67;
        static final int TRANSACTION_getFileTransferFileSize = 71;
        static final int TRANSACTION_getFileTransferHeight = 79;
        static final int TRANSACTION_getFileTransferLength = 81;
        static final int TRANSACTION_getFileTransferPreviewType = 85;
        static final int TRANSACTION_getFileTransferPreviewUri = 83;
        static final int TRANSACTION_getFileTransferSessionId = 65;
        static final int TRANSACTION_getFileTransferStatus = 75;
        static final int TRANSACTION_getFileTransferTransferOffset = 73;
        static final int TRANSACTION_getFileTransferWidth = 77;
        static final int TRANSACTION_getFileTransfersAttachedToMessage = 53;
        static final int TRANSACTION_getGlobalMessageIdForMessage = 42;
        static final int TRANSACTION_getGroupThreadConferenceUri = 26;
        static final int TRANSACTION_getGroupThreadIcon = 22;
        static final int TRANSACTION_getGroupThreadName = 20;
        static final int TRANSACTION_getGroupThreadOwner = 24;
        static final int TRANSACTION_getLatitudeForMessage = 50;
        static final int TRANSACTION_getLongitudeForMessage = 52;
        static final int TRANSACTION_getMessageArrivalTimestamp = 44;
        static final int TRANSACTION_getMessageOriginationTimestamp = 40;
        static final int TRANSACTION_getMessageRecipients = 55;
        static final int TRANSACTION_getMessageSeenTimestamp = 46;
        static final int TRANSACTION_getMessageSnippet = 15;
        static final int TRANSACTION_getMessageStatus = 38;
        static final int TRANSACTION_getMessageSubId = 36;
        static final int TRANSACTION_getMessages = 5;
        static final int TRANSACTION_getMessagesWithToken = 6;
        static final int TRANSACTION_getOutgoingDeliveryDeliveredTimestamp = 56;
        static final int TRANSACTION_getOutgoingDeliverySeenTimestamp = 58;
        static final int TRANSACTION_getOutgoingDeliveryStatus = 60;
        static final int TRANSACTION_getParticipants = 3;
        static final int TRANSACTION_getParticipantsWithToken = 4;
        static final int TRANSACTION_getRcsParticipantAlias = 31;
        static final int TRANSACTION_getRcsParticipantCanonicalAddress = 30;
        static final int TRANSACTION_getRcsParticipantContactId = 33;
        static final int TRANSACTION_getRcsThreads = 1;
        static final int TRANSACTION_getRcsThreadsWithToken = 2;
        static final int TRANSACTION_getSenderParticipant = 54;
        static final int TRANSACTION_getTextForMessage = 48;
        static final int TRANSACTION_removeParticipantFromGroupThread = 28;
        static final int TRANSACTION_set1To1ThreadFallbackThreadId = 16;
        static final int TRANSACTION_setFileTransferContentType = 68;
        static final int TRANSACTION_setFileTransferContentUri = 66;
        static final int TRANSACTION_setFileTransferFileSize = 70;
        static final int TRANSACTION_setFileTransferHeight = 78;
        static final int TRANSACTION_setFileTransferLength = 80;
        static final int TRANSACTION_setFileTransferPreviewType = 84;
        static final int TRANSACTION_setFileTransferPreviewUri = 82;
        static final int TRANSACTION_setFileTransferSessionId = 64;
        static final int TRANSACTION_setFileTransferStatus = 74;
        static final int TRANSACTION_setFileTransferTransferOffset = 72;
        static final int TRANSACTION_setFileTransferWidth = 76;
        static final int TRANSACTION_setGlobalMessageIdForMessage = 41;
        static final int TRANSACTION_setGroupThreadConferenceUri = 25;
        static final int TRANSACTION_setGroupThreadIcon = 21;
        static final int TRANSACTION_setGroupThreadName = 19;
        static final int TRANSACTION_setGroupThreadOwner = 23;
        static final int TRANSACTION_setLatitudeForMessage = 49;
        static final int TRANSACTION_setLongitudeForMessage = 51;
        static final int TRANSACTION_setMessageArrivalTimestamp = 43;
        static final int TRANSACTION_setMessageOriginationTimestamp = 39;
        static final int TRANSACTION_setMessageSeenTimestamp = 45;
        static final int TRANSACTION_setMessageStatus = 37;
        static final int TRANSACTION_setMessageSubId = 35;
        static final int TRANSACTION_setOutgoingDeliveryDeliveredTimestamp = 57;
        static final int TRANSACTION_setOutgoingDeliverySeenTimestamp = 59;
        static final int TRANSACTION_setOutgoingDeliveryStatus = 61;
        static final int TRANSACTION_setRcsParticipantAlias = 32;
        static final int TRANSACTION_setRcsParticipantContactId = 34;
        static final int TRANSACTION_setTextForMessage = 47;
        static final int TRANSACTION_storeFileTransfer = 62;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRcs asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRcs)) {
                return new Proxy(obj);
            }
            return (IRcs) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getRcsThreads";
                case 2:
                    return "getRcsThreadsWithToken";
                case 3:
                    return "getParticipants";
                case 4:
                    return "getParticipantsWithToken";
                case 5:
                    return "getMessages";
                case 6:
                    return "getMessagesWithToken";
                case 7:
                    return "getEvents";
                case 8:
                    return "getEventsWithToken";
                case 9:
                    return "deleteThread";
                case 10:
                    return "createRcs1To1Thread";
                case 11:
                    return "createGroupThread";
                case 12:
                    return "addIncomingMessage";
                case 13:
                    return "addOutgoingMessage";
                case 14:
                    return "deleteMessage";
                case 15:
                    return "getMessageSnippet";
                case 16:
                    return "set1To1ThreadFallbackThreadId";
                case 17:
                    return "get1To1ThreadFallbackThreadId";
                case 18:
                    return "get1To1ThreadOtherParticipantId";
                case 19:
                    return "setGroupThreadName";
                case 20:
                    return "getGroupThreadName";
                case 21:
                    return "setGroupThreadIcon";
                case 22:
                    return "getGroupThreadIcon";
                case 23:
                    return "setGroupThreadOwner";
                case 24:
                    return "getGroupThreadOwner";
                case 25:
                    return "setGroupThreadConferenceUri";
                case 26:
                    return "getGroupThreadConferenceUri";
                case 27:
                    return "addParticipantToGroupThread";
                case 28:
                    return "removeParticipantFromGroupThread";
                case 29:
                    return "createRcsParticipant";
                case 30:
                    return "getRcsParticipantCanonicalAddress";
                case 31:
                    return "getRcsParticipantAlias";
                case 32:
                    return "setRcsParticipantAlias";
                case 33:
                    return "getRcsParticipantContactId";
                case 34:
                    return "setRcsParticipantContactId";
                case 35:
                    return "setMessageSubId";
                case 36:
                    return "getMessageSubId";
                case 37:
                    return "setMessageStatus";
                case 38:
                    return "getMessageStatus";
                case 39:
                    return "setMessageOriginationTimestamp";
                case 40:
                    return "getMessageOriginationTimestamp";
                case 41:
                    return "setGlobalMessageIdForMessage";
                case 42:
                    return "getGlobalMessageIdForMessage";
                case 43:
                    return "setMessageArrivalTimestamp";
                case 44:
                    return "getMessageArrivalTimestamp";
                case 45:
                    return "setMessageSeenTimestamp";
                case 46:
                    return "getMessageSeenTimestamp";
                case 47:
                    return "setTextForMessage";
                case 48:
                    return "getTextForMessage";
                case 49:
                    return "setLatitudeForMessage";
                case 50:
                    return "getLatitudeForMessage";
                case 51:
                    return "setLongitudeForMessage";
                case 52:
                    return "getLongitudeForMessage";
                case 53:
                    return "getFileTransfersAttachedToMessage";
                case 54:
                    return "getSenderParticipant";
                case 55:
                    return "getMessageRecipients";
                case 56:
                    return "getOutgoingDeliveryDeliveredTimestamp";
                case 57:
                    return "setOutgoingDeliveryDeliveredTimestamp";
                case 58:
                    return "getOutgoingDeliverySeenTimestamp";
                case 59:
                    return "setOutgoingDeliverySeenTimestamp";
                case 60:
                    return "getOutgoingDeliveryStatus";
                case 61:
                    return "setOutgoingDeliveryStatus";
                case 62:
                    return "storeFileTransfer";
                case 63:
                    return "deleteFileTransfer";
                case 64:
                    return "setFileTransferSessionId";
                case 65:
                    return "getFileTransferSessionId";
                case 66:
                    return "setFileTransferContentUri";
                case 67:
                    return "getFileTransferContentUri";
                case 68:
                    return "setFileTransferContentType";
                case 69:
                    return "getFileTransferContentType";
                case 70:
                    return "setFileTransferFileSize";
                case 71:
                    return "getFileTransferFileSize";
                case 72:
                    return "setFileTransferTransferOffset";
                case 73:
                    return "getFileTransferTransferOffset";
                case 74:
                    return "setFileTransferStatus";
                case 75:
                    return "getFileTransferStatus";
                case 76:
                    return "setFileTransferWidth";
                case 77:
                    return "getFileTransferWidth";
                case 78:
                    return "setFileTransferHeight";
                case 79:
                    return "getFileTransferHeight";
                case 80:
                    return "setFileTransferLength";
                case 81:
                    return "getFileTransferLength";
                case 82:
                    return "setFileTransferPreviewUri";
                case 83:
                    return "getFileTransferPreviewUri";
                case 84:
                    return "setFileTransferPreviewType";
                case 85:
                    return "getFileTransferPreviewType";
                case 86:
                    return "createGroupThreadNameChangedEvent";
                case 87:
                    return "createGroupThreadIconChangedEvent";
                case 88:
                    return "createGroupThreadParticipantJoinedEvent";
                case 89:
                    return "createGroupThreadParticipantLeftEvent";
                case 90:
                    return "createParticipantAliasChangedEvent";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RcsThreadQueryParams _arg0;
            RcsQueryContinuationToken _arg02;
            RcsParticipantQueryParams _arg03;
            RcsQueryContinuationToken _arg04;
            RcsMessageQueryParams _arg05;
            RcsQueryContinuationToken _arg06;
            RcsEventQueryParams _arg07;
            RcsQueryContinuationToken _arg08;
            Uri _arg2;
            RcsIncomingMessageCreationParams _arg1;
            RcsOutgoingMessageCreationParams _arg12;
            Uri _arg13;
            Uri _arg14;
            RcsFileTransferCreationParams _arg22;
            Uri _arg15;
            Uri _arg16;
            Uri _arg3;
            if (code != 1598968902) {
                boolean _arg17 = false;
                boolean _arg18 = false;
                boolean _arg19 = false;
                boolean _arg110 = false;
                boolean _arg111 = false;
                boolean _arg112 = false;
                boolean _arg113 = false;
                boolean _arg114 = false;
                boolean _arg115 = false;
                boolean _arg116 = false;
                boolean _arg117 = false;
                boolean _arg118 = false;
                boolean _arg119 = false;
                boolean _arg120 = false;
                boolean _arg121 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = RcsThreadQueryParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        RcsThreadQueryResultParcelable _result = getRcsThreads(_arg0, data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = RcsQueryContinuationToken.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        RcsThreadQueryResultParcelable _result2 = getRcsThreadsWithToken(_arg02, data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = RcsParticipantQueryParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        RcsParticipantQueryResultParcelable _result3 = getParticipants(_arg03, data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = RcsQueryContinuationToken.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        RcsParticipantQueryResultParcelable _result4 = getParticipantsWithToken(_arg04, data.readString());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = RcsMessageQueryParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        RcsMessageQueryResultParcelable _result5 = getMessages(_arg05, data.readString());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = RcsQueryContinuationToken.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        RcsMessageQueryResultParcelable _result6 = getMessagesWithToken(_arg06, data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = RcsEventQueryParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        RcsEventQueryResultDescriptor _result7 = getEvents(_arg07, data.readString());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = RcsQueryContinuationToken.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        RcsEventQueryResultDescriptor _result8 = getEventsWithToken(_arg08, data.readString());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deleteThread = deleteThread(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(deleteThread ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = createRcs1To1Thread(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _arg09 = data.createIntArray();
                        String _arg122 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result10 = createGroupThread(_arg09, _arg122, _arg2, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = RcsIncomingMessageCreationParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result11 = addIncomingMessage(_arg010, _arg1, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = RcsOutgoingMessageCreationParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result12 = addOutgoingMessage(_arg011, _arg12, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        deleteMessage(data.readInt(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        RcsMessageSnippet _result13 = getMessageSnippet(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        set1To1ThreadFallbackThreadId(data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        long _result14 = get1To1ThreadFallbackThreadId(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result14);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = get1To1ThreadOtherParticipantId(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        setGroupThreadName(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _result16 = getGroupThreadName(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result16);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        setGroupThreadIcon(_arg012, _arg13, data.readString());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        Uri _result17 = getGroupThreadIcon(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result17 != null) {
                            reply.writeInt(1);
                            _result17.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        setGroupThreadOwner(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = getGroupThreadOwner(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        setGroupThreadConferenceUri(_arg013, _arg14, data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        Uri _result19 = getGroupThreadConferenceUri(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result19 != null) {
                            reply.writeInt(1);
                            _result19.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        addParticipantToGroupThread(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        removeParticipantFromGroupThread(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = createRcsParticipant(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getRcsParticipantCanonicalAddress(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _result22 = getRcsParticipantAlias(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result22);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        setRcsParticipantAlias(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        String _result23 = getRcsParticipantContactId(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result23);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        setRcsParticipantContactId(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg014 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg17 = true;
                        }
                        setMessageSubId(_arg014, _arg17, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg015 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg121 = true;
                        }
                        int _result24 = getMessageSubId(_arg015, _arg121, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg016 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg120 = true;
                        }
                        setMessageStatus(_arg016, _arg120, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg017 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg119 = true;
                        }
                        int _result25 = getMessageStatus(_arg017, _arg119, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        setMessageOriginationTimestamp(data.readInt(), data.readInt() != 0, data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg018 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg118 = true;
                        }
                        long _result26 = getMessageOriginationTimestamp(_arg018, _arg118, data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result26);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg019 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg117 = true;
                        }
                        setGlobalMessageIdForMessage(_arg019, _arg117, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg020 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg116 = true;
                        }
                        String _result27 = getGlobalMessageIdForMessage(_arg020, _arg116, data.readString());
                        reply.writeNoException();
                        reply.writeString(_result27);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        setMessageArrivalTimestamp(data.readInt(), data.readInt() != 0, data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg021 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg115 = true;
                        }
                        long _result28 = getMessageArrivalTimestamp(_arg021, _arg115, data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result28);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        setMessageSeenTimestamp(data.readInt(), data.readInt() != 0, data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg022 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg114 = true;
                        }
                        long _result29 = getMessageSeenTimestamp(_arg022, _arg114, data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result29);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg023 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg113 = true;
                        }
                        setTextForMessage(_arg023, _arg113, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg024 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg112 = true;
                        }
                        String _result30 = getTextForMessage(_arg024, _arg112, data.readString());
                        reply.writeNoException();
                        reply.writeString(_result30);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        setLatitudeForMessage(data.readInt(), data.readInt() != 0, data.readDouble(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg025 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg111 = true;
                        }
                        double _result31 = getLatitudeForMessage(_arg025, _arg111, data.readString());
                        reply.writeNoException();
                        reply.writeDouble(_result31);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        setLongitudeForMessage(data.readInt(), data.readInt() != 0, data.readDouble(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg026 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg110 = true;
                        }
                        double _result32 = getLongitudeForMessage(_arg026, _arg110, data.readString());
                        reply.writeNoException();
                        reply.writeDouble(_result32);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg027 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        int[] _result33 = getFileTransfersAttachedToMessage(_arg027, _arg19, data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result33);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        int _result34 = getSenderParticipant(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result34);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result35 = getMessageRecipients(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result35);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        long _result36 = getOutgoingDeliveryDeliveredTimestamp(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result36);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        setOutgoingDeliveryDeliveredTimestamp(data.readInt(), data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        long _result37 = getOutgoingDeliverySeenTimestamp(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result37);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        setOutgoingDeliverySeenTimestamp(data.readInt(), data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        int _result38 = getOutgoingDeliveryStatus(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result38);
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        setOutgoingDeliveryStatus(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg028 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg18 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = RcsFileTransferCreationParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _result39 = storeFileTransfer(_arg028, _arg18, _arg22, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result39);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        deleteFileTransfer(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferSessionId(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        String _result40 = getFileTransferSessionId(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result40);
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg029 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        setFileTransferContentUri(_arg029, _arg15, data.readString());
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        Uri _result41 = getFileTransferContentUri(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result41 != null) {
                            reply.writeInt(1);
                            _result41.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferContentType(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        String _result42 = getFileTransferContentType(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result42);
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferFileSize(data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        long _result43 = getFileTransferFileSize(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result43);
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferTransferOffset(data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        long _result44 = getFileTransferTransferOffset(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result44);
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferStatus(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        int _result45 = getFileTransferStatus(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result45);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferWidth(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        int _result46 = getFileTransferWidth(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result46);
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferHeight(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        int _result47 = getFileTransferHeight(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result47);
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferLength(data.readInt(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        long _result48 = getFileTransferLength(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result48);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg030 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        setFileTransferPreviewUri(_arg030, _arg16, data.readString());
                        reply.writeNoException();
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        Uri _result49 = getFileTransferPreviewUri(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result49 != null) {
                            reply.writeInt(1);
                            _result49.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        setFileTransferPreviewType(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        String _result50 = getFileTransferPreviewType(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result50);
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        int _result51 = createGroupThreadNameChangedEvent(data.readLong(), data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result51);
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg031 = data.readLong();
                        int _arg123 = data.readInt();
                        int _arg23 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result52 = createGroupThreadIconChangedEvent(_arg031, _arg123, _arg23, _arg3, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result52);
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        int _result53 = createGroupThreadParticipantJoinedEvent(data.readLong(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result53);
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        int _result54 = createGroupThreadParticipantLeftEvent(data.readLong(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result54);
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        int _result55 = createParticipantAliasChangedEvent(data.readLong(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result55);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRcs {
            public static IRcs sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsThreadQueryResultParcelable getRcsThreads(RcsThreadQueryParams queryParams, String callingPackage) throws RemoteException {
                RcsThreadQueryResultParcelable _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queryParams != null) {
                        _data.writeInt(1);
                        queryParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRcsThreads(queryParams, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsThreadQueryResultParcelable.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsThreadQueryResultParcelable getRcsThreadsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
                RcsThreadQueryResultParcelable _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (continuationToken != null) {
                        _data.writeInt(1);
                        continuationToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRcsThreadsWithToken(continuationToken, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsThreadQueryResultParcelable.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsParticipantQueryResultParcelable getParticipants(RcsParticipantQueryParams queryParams, String callingPackage) throws RemoteException {
                RcsParticipantQueryResultParcelable _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queryParams != null) {
                        _data.writeInt(1);
                        queryParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getParticipants(queryParams, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsParticipantQueryResultParcelable.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsParticipantQueryResultParcelable getParticipantsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
                RcsParticipantQueryResultParcelable _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (continuationToken != null) {
                        _data.writeInt(1);
                        continuationToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getParticipantsWithToken(continuationToken, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsParticipantQueryResultParcelable.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsMessageQueryResultParcelable getMessages(RcsMessageQueryParams queryParams, String callingPackage) throws RemoteException {
                RcsMessageQueryResultParcelable _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queryParams != null) {
                        _data.writeInt(1);
                        queryParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessages(queryParams, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsMessageQueryResultParcelable.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsMessageQueryResultParcelable getMessagesWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
                RcsMessageQueryResultParcelable _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (continuationToken != null) {
                        _data.writeInt(1);
                        continuationToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessagesWithToken(continuationToken, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsMessageQueryResultParcelable.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsEventQueryResultDescriptor getEvents(RcsEventQueryParams queryParams, String callingPackage) throws RemoteException {
                RcsEventQueryResultDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queryParams != null) {
                        _data.writeInt(1);
                        queryParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEvents(queryParams, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsEventQueryResultDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsEventQueryResultDescriptor getEventsWithToken(RcsQueryContinuationToken continuationToken, String callingPackage) throws RemoteException {
                RcsEventQueryResultDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (continuationToken != null) {
                        _data.writeInt(1);
                        continuationToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEventsWithToken(continuationToken, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsEventQueryResultDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public boolean deleteThread(int threadId, int threadType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(threadId);
                    _data.writeInt(threadType);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteThread(threadId, threadType, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createRcs1To1Thread(int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createRcs1To1Thread(participantId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createGroupThread(int[] participantIds, String groupName, Uri groupIcon, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(participantIds);
                    _data.writeString(groupName);
                    if (groupIcon != null) {
                        _data.writeInt(1);
                        groupIcon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createGroupThread(participantIds, groupName, groupIcon, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int addIncomingMessage(int rcsThreadId, RcsIncomingMessageCreationParams rcsIncomingMessageCreationParams, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    if (rcsIncomingMessageCreationParams != null) {
                        _data.writeInt(1);
                        rcsIncomingMessageCreationParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addIncomingMessage(rcsThreadId, rcsIncomingMessageCreationParams, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int addOutgoingMessage(int rcsThreadId, RcsOutgoingMessageCreationParams rcsOutgoingMessageCreationParams, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    if (rcsOutgoingMessageCreationParams != null) {
                        _data.writeInt(1);
                        rcsOutgoingMessageCreationParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addOutgoingMessage(rcsThreadId, rcsOutgoingMessageCreationParams, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void deleteMessage(int rcsMessageId, boolean isIncoming, int rcsThreadId, boolean isGroup, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsMessageId);
                    int i = 1;
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeInt(rcsThreadId);
                    if (!isGroup) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteMessage(rcsMessageId, isIncoming, rcsThreadId, isGroup, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public RcsMessageSnippet getMessageSnippet(int rcsThreadId, String callingPackage) throws RemoteException {
                RcsMessageSnippet _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageSnippet(rcsThreadId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RcsMessageSnippet.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void set1To1ThreadFallbackThreadId(int rcsThreadId, long fallbackId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeLong(fallbackId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().set1To1ThreadFallbackThreadId(rcsThreadId, fallbackId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long get1To1ThreadFallbackThreadId(int rcsThreadId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().get1To1ThreadFallbackThreadId(rcsThreadId, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int get1To1ThreadOtherParticipantId(int rcsThreadId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().get1To1ThreadOtherParticipantId(rcsThreadId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setGroupThreadName(int rcsThreadId, String groupName, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(groupName);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGroupThreadName(rcsThreadId, groupName, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getGroupThreadName(int rcsThreadId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGroupThreadName(rcsThreadId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setGroupThreadIcon(int rcsThreadId, Uri groupIcon, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    if (groupIcon != null) {
                        _data.writeInt(1);
                        groupIcon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGroupThreadIcon(rcsThreadId, groupIcon, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public Uri getGroupThreadIcon(int rcsThreadId, String callingPackage) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGroupThreadIcon(rcsThreadId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setGroupThreadOwner(int rcsThreadId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGroupThreadOwner(rcsThreadId, participantId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getGroupThreadOwner(int rcsThreadId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGroupThreadOwner(rcsThreadId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setGroupThreadConferenceUri(int rcsThreadId, Uri conferenceUri, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    if (conferenceUri != null) {
                        _data.writeInt(1);
                        conferenceUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGroupThreadConferenceUri(rcsThreadId, conferenceUri, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public Uri getGroupThreadConferenceUri(int rcsThreadId, String callingPackage) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGroupThreadConferenceUri(rcsThreadId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void addParticipantToGroupThread(int rcsThreadId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addParticipantToGroupThread(rcsThreadId, participantId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void removeParticipantFromGroupThread(int rcsThreadId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rcsThreadId);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeParticipantFromGroupThread(rcsThreadId, participantId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createRcsParticipant(String canonicalAddress, String alias, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(canonicalAddress);
                    _data.writeString(alias);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createRcsParticipant(canonicalAddress, alias, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getRcsParticipantCanonicalAddress(int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRcsParticipantCanonicalAddress(participantId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getRcsParticipantAlias(int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRcsParticipantAlias(participantId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setRcsParticipantAlias(int id, String alias, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeString(alias);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRcsParticipantAlias(id, alias, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getRcsParticipantContactId(int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRcsParticipantContactId(participantId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setRcsParticipantContactId(int participantId, String contactId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(participantId);
                    _data.writeString(contactId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRcsParticipantContactId(participantId, contactId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setMessageSubId(int messageId, boolean isIncoming, int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMessageSubId(messageId, isIncoming, subId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getMessageSubId(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageSubId(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setMessageStatus(int messageId, boolean isIncoming, int status, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeInt(status);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMessageStatus(messageId, isIncoming, status, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getMessageStatus(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageStatus(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setMessageOriginationTimestamp(int messageId, boolean isIncoming, long originationTimestamp, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeLong(originationTimestamp);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMessageOriginationTimestamp(messageId, isIncoming, originationTimestamp, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getMessageOriginationTimestamp(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageOriginationTimestamp(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setGlobalMessageIdForMessage(int messageId, boolean isIncoming, String globalId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(globalId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGlobalMessageIdForMessage(messageId, isIncoming, globalId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getGlobalMessageIdForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGlobalMessageIdForMessage(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setMessageArrivalTimestamp(int messageId, boolean isIncoming, long arrivalTimestamp, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeLong(arrivalTimestamp);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMessageArrivalTimestamp(messageId, isIncoming, arrivalTimestamp, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getMessageArrivalTimestamp(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageArrivalTimestamp(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setMessageSeenTimestamp(int messageId, boolean isIncoming, long seenTimestamp, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeLong(seenTimestamp);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMessageSeenTimestamp(messageId, isIncoming, seenTimestamp, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getMessageSeenTimestamp(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageSeenTimestamp(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setTextForMessage(int messageId, boolean isIncoming, String text, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(text);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTextForMessage(messageId, isIncoming, text, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getTextForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTextForMessage(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setLatitudeForMessage(int messageId, boolean isIncoming, double latitude, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeDouble(latitude);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLatitudeForMessage(messageId, isIncoming, latitude, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public double getLatitudeForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLatitudeForMessage(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    double _result = _reply.readDouble();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setLongitudeForMessage(int messageId, boolean isIncoming, double longitude, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeDouble(longitude);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLongitudeForMessage(messageId, isIncoming, longitude, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public double getLongitudeForMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLongitudeForMessage(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    double _result = _reply.readDouble();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int[] getFileTransfersAttachedToMessage(int messageId, boolean isIncoming, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransfersAttachedToMessage(messageId, isIncoming, callingPackage);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getSenderParticipant(int messageId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSenderParticipant(messageId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int[] getMessageRecipients(int messageId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageRecipients(messageId, callingPackage);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getOutgoingDeliveryDeliveredTimestamp(int messageId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOutgoingDeliveryDeliveredTimestamp(messageId, participantId, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setOutgoingDeliveryDeliveredTimestamp(int messageId, int participantId, long deliveredTimestamp, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(participantId);
                    _data.writeLong(deliveredTimestamp);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOutgoingDeliveryDeliveredTimestamp(messageId, participantId, deliveredTimestamp, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getOutgoingDeliverySeenTimestamp(int messageId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOutgoingDeliverySeenTimestamp(messageId, participantId, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setOutgoingDeliverySeenTimestamp(int messageId, int participantId, long seenTimestamp, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(participantId);
                    _data.writeLong(seenTimestamp);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOutgoingDeliverySeenTimestamp(messageId, participantId, seenTimestamp, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getOutgoingDeliveryStatus(int messageId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(participantId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(60, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOutgoingDeliveryStatus(messageId, participantId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setOutgoingDeliveryStatus(int messageId, int participantId, int status, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(participantId);
                    _data.writeInt(status);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOutgoingDeliveryStatus(messageId, participantId, status, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int storeFileTransfer(int messageId, boolean isIncoming, RcsFileTransferCreationParams fileTransferCreationParams, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(isIncoming ? 1 : 0);
                    if (fileTransferCreationParams != null) {
                        _data.writeInt(1);
                        fileTransferCreationParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().storeFileTransfer(messageId, isIncoming, fileTransferCreationParams, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void deleteFileTransfer(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteFileTransfer(partId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferSessionId(int partId, String sessionId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(sessionId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferSessionId(partId, sessionId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getFileTransferSessionId(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(65, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferSessionId(partId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferContentUri(int partId, Uri contentUri, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    if (contentUri != null) {
                        _data.writeInt(1);
                        contentUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferContentUri(partId, contentUri, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public Uri getFileTransferContentUri(int partId, String callingPackage) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(67, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferContentUri(partId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferContentType(int partId, String contentType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(contentType);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferContentType(partId, contentType, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getFileTransferContentType(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferContentType(partId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferFileSize(int partId, long fileSize, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeLong(fileSize);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferFileSize(partId, fileSize, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getFileTransferFileSize(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(71, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferFileSize(partId, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferTransferOffset(int partId, long transferOffset, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeLong(transferOffset);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferTransferOffset(partId, transferOffset, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getFileTransferTransferOffset(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferTransferOffset(partId, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferStatus(int partId, int transferStatus, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeInt(transferStatus);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(74, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferStatus(partId, transferStatus, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getFileTransferStatus(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferStatus(partId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferWidth(int partId, int width, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeInt(width);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(76, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferWidth(partId, width, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getFileTransferWidth(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(77, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferWidth(partId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferHeight(int partId, int height, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeInt(height);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferHeight(partId, height, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int getFileTransferHeight(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(79, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferHeight(partId, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferLength(int partId, long length, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeLong(length);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(80, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferLength(partId, length, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public long getFileTransferLength(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(81, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferLength(partId, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferPreviewUri(int partId, Uri uri, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(82, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferPreviewUri(partId, uri, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public Uri getFileTransferPreviewUri(int partId, String callingPackage) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferPreviewUri(partId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public void setFileTransferPreviewType(int partId, String type, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(type);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(84, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileTransferPreviewType(partId, type, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public String getFileTransferPreviewType(int partId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(partId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFileTransferPreviewType(partId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createGroupThreadNameChangedEvent(long timestamp, int threadId, int originationParticipantId, String newName, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(timestamp);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(threadId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(originationParticipantId);
                        try {
                            _data.writeString(newName);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int createGroupThreadNameChangedEvent = Stub.getDefaultImpl().createGroupThreadNameChangedEvent(timestamp, threadId, originationParticipantId, newName, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                            return createGroupThreadNameChangedEvent;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createGroupThreadIconChangedEvent(long timestamp, int threadId, int originationParticipantId, Uri newIcon, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(timestamp);
                        try {
                            _data.writeInt(threadId);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(originationParticipantId);
                            if (newIcon != null) {
                                _data.writeInt(1);
                                newIcon.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeString(callingPackage);
                                if (this.mRemote.transact(87, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    int _result = _reply.readInt();
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                }
                                int createGroupThreadIconChangedEvent = Stub.getDefaultImpl().createGroupThreadIconChangedEvent(timestamp, threadId, originationParticipantId, newIcon, callingPackage);
                                _reply.recycle();
                                _data.recycle();
                                return createGroupThreadIconChangedEvent;
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createGroupThreadParticipantJoinedEvent(long timestamp, int threadId, int originationParticipantId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(timestamp);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(threadId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(originationParticipantId);
                        try {
                            _data.writeInt(participantId);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(88, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int createGroupThreadParticipantJoinedEvent = Stub.getDefaultImpl().createGroupThreadParticipantJoinedEvent(timestamp, threadId, originationParticipantId, participantId, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                            return createGroupThreadParticipantJoinedEvent;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createGroupThreadParticipantLeftEvent(long timestamp, int threadId, int originationParticipantId, int participantId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(timestamp);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(threadId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(originationParticipantId);
                        try {
                            _data.writeInt(participantId);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(89, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int createGroupThreadParticipantLeftEvent = Stub.getDefaultImpl().createGroupThreadParticipantLeftEvent(timestamp, threadId, originationParticipantId, participantId, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                            return createGroupThreadParticipantLeftEvent;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.telephony.ims.aidl.IRcs
            public int createParticipantAliasChangedEvent(long timestamp, int participantId, String newAlias, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestamp);
                    _data.writeInt(participantId);
                    _data.writeString(newAlias);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(90, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createParticipantAliasChangedEvent(timestamp, participantId, newAlias, callingPackage);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRcs impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRcs getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
