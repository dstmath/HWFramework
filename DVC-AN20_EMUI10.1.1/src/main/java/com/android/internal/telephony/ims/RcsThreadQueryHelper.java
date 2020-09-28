package com.android.internal.telephony.ims;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.ims.RcsQueryContinuationToken;
import android.telephony.ims.RcsThreadQueryResultParcelable;
import com.android.ims.RcsTypeIdPair;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class RcsThreadQueryHelper {
    private static final int THREAD_ID_INDEX_IN_INSERTION_URI = 1;
    private final ContentResolver mContentResolver;
    private final RcsParticipantQueryHelper mParticipantQueryHelper;

    RcsThreadQueryHelper(ContentResolver contentResolver, RcsParticipantQueryHelper participantQueryHelper) {
        this.mContentResolver = contentResolver;
        this.mParticipantQueryHelper = participantQueryHelper;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0072, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0073, code lost:
        if (r2 != null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0079, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007a, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007d, code lost:
        throw r4;
     */
    public RcsThreadQueryResultParcelable performThreadQuery(Bundle bundle) throws RemoteException {
        RcsQueryContinuationToken continuationToken = null;
        List<RcsTypeIdPair> rcsThreadIdList = new ArrayList<>();
        Cursor cursor = this.mContentResolver.query(Telephony.RcsColumns.RcsThreadColumns.RCS_THREAD_URI, null, bundle, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex("thread_type")) == 1) {
                    rcsThreadIdList.add(new RcsTypeIdPair(1, cursor.getInt(cursor.getColumnIndex("rcs_thread_id"))));
                } else {
                    rcsThreadIdList.add(new RcsTypeIdPair(0, cursor.getInt(cursor.getColumnIndex("rcs_thread_id"))));
                }
            }
            Bundle cursorExtras = cursor.getExtras();
            if (cursorExtras != null) {
                continuationToken = (RcsQueryContinuationToken) cursorExtras.getParcelable("query_continuation_token");
            }
            cursor.close();
            return new RcsThreadQueryResultParcelable(continuationToken, rcsThreadIdList);
        }
        throw new RemoteException("Could not perform thread query.");
    }

    /* access modifiers changed from: package-private */
    public int create1To1Thread(int participantId) throws RemoteException {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("rcs_participant_id", Integer.valueOf(participantId));
        Uri insertionUri = this.mContentResolver.insert(Telephony.RcsColumns.Rcs1To1ThreadColumns.RCS_1_TO_1_THREAD_URI, contentValues);
        if (insertionUri != null) {
            int threadId = Integer.parseInt(insertionUri.getLastPathSegment());
            if (threadId > 0) {
                return threadId;
            }
            throw new RemoteException("Rcs1To1Thread creation failed");
        }
        throw new RemoteException("Rcs1To1Thread creation failed");
    }

    /* access modifiers changed from: package-private */
    public int createGroupThread(String groupName, Uri groupIcon) throws RemoteException {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("group_name", groupName);
        if (groupIcon != null) {
            contentValues.put("group_icon", groupIcon.toString());
        }
        Uri groupUri = this.mContentResolver.insert(Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI, contentValues);
        if (groupUri != null) {
            return Integer.parseInt(groupUri.getPathSegments().get(1));
        }
        throw new RemoteException("RcsGroupThread creation failed");
    }

    static Uri get1To1ThreadUri(int rcsThreadId) {
        return Uri.withAppendedPath(Telephony.RcsColumns.Rcs1To1ThreadColumns.RCS_1_TO_1_THREAD_URI, Integer.toString(rcsThreadId));
    }

    static Uri getGroupThreadUri(int rcsThreadId) {
        return Uri.withAppendedPath(Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI, Integer.toString(rcsThreadId));
    }

    static Uri getAllParticipantsInThreadUri(int rcsThreadId) {
        return Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI.buildUpon().appendPath(Integer.toString(rcsThreadId)).appendPath("participant").build();
    }

    static Uri getParticipantInThreadUri(int rcsThreadId, int participantId) {
        return Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI.buildUpon().appendPath(Integer.toString(rcsThreadId)).appendPath("participant").appendPath(Integer.toString(participantId)).build();
    }
}
