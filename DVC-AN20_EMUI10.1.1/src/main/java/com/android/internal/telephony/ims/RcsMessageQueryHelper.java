package com.android.internal.telephony.ims;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.ims.RcsFileTransferCreationParams;
import android.telephony.ims.RcsMessageCreationParams;
import android.telephony.ims.RcsMessageQueryResultParcelable;
import android.telephony.ims.RcsQueryContinuationToken;
import com.android.ims.RcsTypeIdPair;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class RcsMessageQueryHelper {
    private final ContentResolver mContentResolver;

    RcsMessageQueryHelper(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0062, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0063, code lost:
        if (r2 != null) goto L_0x0065;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0065, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0068, code lost:
        throw r4;
     */
    public RcsMessageQueryResultParcelable performMessageQuery(Bundle bundle) throws RemoteException {
        RcsQueryContinuationToken continuationToken = null;
        List<RcsTypeIdPair> messageTypeIdPairs = new ArrayList<>();
        Cursor cursor = this.mContentResolver.query(Telephony.RcsColumns.RcsUnifiedMessageColumns.UNIFIED_MESSAGE_URI, null, bundle, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int i = 0;
                boolean isIncoming = cursor.getInt(cursor.getColumnIndex("message_type")) == 1;
                int messageId = cursor.getInt(cursor.getColumnIndex("rcs_message_row_id"));
                if (isIncoming) {
                    i = 1;
                }
                messageTypeIdPairs.add(new RcsTypeIdPair(i, messageId));
            }
            Bundle cursorExtras = cursor.getExtras();
            if (cursorExtras != null) {
                continuationToken = (RcsQueryContinuationToken) cursorExtras.getParcelable("query_continuation_token");
            }
            $closeResource(null, cursor);
            return new RcsMessageQueryResultParcelable(continuationToken, messageTypeIdPairs);
        }
        throw new RemoteException("Could not perform message query.");
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* access modifiers changed from: package-private */
    public void createContentValuesForGenericMessage(ContentValues contentValues, int threadId, RcsMessageCreationParams rcsMessageCreationParams) {
        contentValues.put("rcs_message_global_id", rcsMessageCreationParams.getRcsMessageGlobalId());
        contentValues.put("sub_id", Integer.valueOf(rcsMessageCreationParams.getSubId()));
        contentValues.put("status", Integer.valueOf(rcsMessageCreationParams.getMessageStatus()));
        contentValues.put("origination_timestamp", Long.valueOf(rcsMessageCreationParams.getOriginationTimestamp()));
        contentValues.put("rcs_thread_id", Integer.valueOf(threadId));
    }

    /* access modifiers changed from: package-private */
    public Uri getMessageInsertionUri(boolean isIncoming) {
        return isIncoming ? Telephony.RcsColumns.RcsIncomingMessageColumns.INCOMING_MESSAGE_URI : Telephony.RcsColumns.RcsOutgoingMessageColumns.OUTGOING_MESSAGE_URI;
    }

    /* access modifiers changed from: package-private */
    public Uri getMessageDeletionUri(int messageId, boolean isIncoming, int rcsThreadId, boolean isGroup) {
        return Telephony.RcsColumns.CONTENT_AND_AUTHORITY.buildUpon().appendPath(isGroup ? "group_thread" : "p2p_thread").appendPath(Integer.toString(rcsThreadId)).appendPath(isIncoming ? "incoming_message" : "outgoing_message").appendPath(Integer.toString(messageId)).build();
    }

    /* access modifiers changed from: package-private */
    public Uri getMessageUpdateUri(int messageId, boolean isIncoming) {
        return getMessageInsertionUri(isIncoming).buildUpon().appendPath(Integer.toString(messageId)).build();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0047, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        if (r0 != null) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004d, code lost:
        throw r2;
     */
    public int[] getDeliveryParticipantsForMessage(int messageId) throws RemoteException {
        Cursor cursor = this.mContentResolver.query(getMessageDeliveryQueryUri(messageId), null, null, null);
        if (cursor != null) {
            int[] participantIds = new int[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                participantIds[i] = cursor.getInt(cursor.getColumnIndex("rcs_participant_id"));
                i++;
            }
            $closeResource(null, cursor);
            return participantIds;
        }
        throw new RemoteException("Could not query deliveries for message, messageId: " + messageId);
    }

    /* access modifiers changed from: package-private */
    public Uri getMessageDeliveryUri(int messageId, int participantId) {
        return Uri.withAppendedPath(getMessageDeliveryQueryUri(messageId), Integer.toString(participantId));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0040, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0041, code lost:
        if (r0 != null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0043, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0046, code lost:
        throw r2;
     */
    public long getLongValueFromDelivery(int messageId, int participantId, String columnName) throws RemoteException {
        Cursor cursor = this.mContentResolver.query(getMessageDeliveryUri(messageId, participantId), null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                long j = cursor.getLong(cursor.getColumnIndex(columnName));
                $closeResource(null, cursor);
                return j;
            }
        }
        throw new RemoteException("Could not read delivery for message: " + messageId + ", participant: " + participantId);
    }

    private Uri getMessageDeliveryQueryUri(int messageId) {
        return getMessageInsertionUri(false).buildUpon().appendPath(Integer.toString(messageId)).appendPath("delivery").build();
    }

    /* access modifiers changed from: package-private */
    public ContentValues getContentValuesForFileTransfer(RcsFileTransferCreationParams fileTransferCreationParameters) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("session_id", fileTransferCreationParameters.getRcsFileTransferSessionId());
        contentValues.put("content_uri", fileTransferCreationParameters.getContentUri().toString());
        contentValues.put("content_type", fileTransferCreationParameters.getContentMimeType());
        contentValues.put("file_size", Long.valueOf(fileTransferCreationParameters.getFileSize()));
        contentValues.put("transfer_offset", Long.valueOf(fileTransferCreationParameters.getTransferOffset()));
        contentValues.put("transfer_status", Integer.valueOf(fileTransferCreationParameters.getFileTransferStatus()));
        contentValues.put("width", Integer.valueOf(fileTransferCreationParameters.getWidth()));
        contentValues.put("height", Integer.valueOf(fileTransferCreationParameters.getHeight()));
        contentValues.put("duration", Long.valueOf(fileTransferCreationParameters.getMediaDuration()));
        contentValues.put("preview_uri", fileTransferCreationParameters.getPreviewUri().toString());
        contentValues.put("preview_type", fileTransferCreationParameters.getPreviewMimeType());
        return contentValues;
    }

    /* access modifiers changed from: package-private */
    public Uri getFileTransferInsertionUri(int messageId) {
        return Telephony.RcsColumns.RcsUnifiedMessageColumns.UNIFIED_MESSAGE_URI.buildUpon().appendPath(Integer.toString(messageId)).appendPath("file_transfer").build();
    }

    /* access modifiers changed from: package-private */
    public Uri getFileTransferUpdateUri(int partId) {
        return Uri.withAppendedPath(Telephony.RcsColumns.RcsFileTransferColumns.FILE_TRANSFER_URI, Integer.toString(partId));
    }
}
