package com.android.internal.telephony.ims;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.ims.RcsEventDescriptor;
import android.telephony.ims.RcsEventQueryResultDescriptor;
import android.telephony.ims.RcsGroupThreadIconChangedEventDescriptor;
import android.telephony.ims.RcsGroupThreadNameChangedEventDescriptor;
import android.telephony.ims.RcsGroupThreadParticipantJoinedEventDescriptor;
import android.telephony.ims.RcsGroupThreadParticipantLeftEventDescriptor;
import android.telephony.ims.RcsParticipantAliasChangedEventDescriptor;
import android.telephony.ims.RcsQueryContinuationToken;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class RcsEventQueryHelper {
    private final ContentResolver mContentResolver;

    RcsEventQueryHelper(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    /* access modifiers changed from: package-private */
    public Uri getParticipantEventInsertionUri(int participantId) {
        return Telephony.RcsColumns.RcsParticipantColumns.RCS_PARTICIPANT_URI.buildUpon().appendPath(Integer.toString(participantId)).appendPath("alias_change_event").build();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0095, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0096, code lost:
        if (r2 != null) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009d, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a0, code lost:
        throw r4;
     */
    public RcsEventQueryResultDescriptor performEventQuery(Bundle bundle) throws RemoteException {
        RcsQueryContinuationToken continuationToken = null;
        List<RcsEventDescriptor> eventList = new ArrayList<>();
        Cursor cursor = this.mContentResolver.query(Telephony.RcsColumns.RcsUnifiedEventHelper.RCS_EVENT_QUERY_URI, null, bundle, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int eventType = cursor.getInt(cursor.getColumnIndex("event_type"));
                if (eventType == 1) {
                    eventList.add(createNewParticipantAliasChangedEvent(cursor));
                } else if (eventType == 2) {
                    eventList.add(createNewParticipantJoinedEvent(cursor));
                } else if (eventType == 4) {
                    eventList.add(createNewParticipantLeftEvent(cursor));
                } else if (eventType == 8) {
                    eventList.add(createNewGroupIconChangedEvent(cursor));
                } else if (eventType != 16) {
                    Rlog.e("RcsMsgStoreController", "RcsEventQueryHelper: invalid event type: " + eventType);
                } else {
                    eventList.add(createNewGroupNameChangedEvent(cursor));
                }
            }
            Bundle cursorExtras = cursor.getExtras();
            if (cursorExtras != null) {
                continuationToken = (RcsQueryContinuationToken) cursorExtras.getParcelable("query_continuation_token");
            }
            cursor.close();
            return new RcsEventQueryResultDescriptor(continuationToken, eventList);
        }
        throw new RemoteException("Event query failed.");
    }

    /* access modifiers changed from: package-private */
    public int createGroupThreadEvent(int eventType, long timestamp, int threadId, int originationParticipantId, ContentValues eventSpecificValues) throws RemoteException {
        ContentValues values = new ContentValues(eventSpecificValues);
        values.put("event_type", Integer.valueOf(eventType));
        values.put("origination_timestamp", Long.valueOf(timestamp));
        values.put("source_participant", Integer.valueOf(originationParticipantId));
        Uri insertionUri = this.mContentResolver.insert(Telephony.RcsColumns.RcsGroupThreadColumns.RCS_GROUP_THREAD_URI.buildUpon().appendPath(Integer.toString(threadId)).appendPath(getPathForEventType(eventType)).build(), values);
        int eventId = 0;
        if (insertionUri != null) {
            eventId = Integer.parseInt(insertionUri.getLastPathSegment());
        }
        if (eventId > 0) {
            return eventId;
        }
        throw new RemoteException("Could not create event with type: " + eventType + " on thread: " + threadId);
    }

    private String getPathForEventType(int eventType) throws RemoteException {
        if (eventType == 2) {
            return "participant_joined_event";
        }
        if (eventType == 4) {
            return "participant_left_event";
        }
        if (eventType == 8) {
            return "icon_changed_event";
        }
        if (eventType == 16) {
            return "name_changed_event";
        }
        throw new RemoteException("Event type unrecognized: " + eventType);
    }

    private RcsGroupThreadIconChangedEventDescriptor createNewGroupIconChangedEvent(Cursor cursor) {
        String newIcon = cursor.getString(cursor.getColumnIndex("new_icon_uri"));
        return new RcsGroupThreadIconChangedEventDescriptor(cursor.getLong(cursor.getColumnIndex("origination_timestamp")), cursor.getInt(cursor.getColumnIndex("rcs_thread_id")), cursor.getInt(cursor.getColumnIndex("source_participant")), newIcon == null ? null : Uri.parse(newIcon));
    }

    private RcsGroupThreadNameChangedEventDescriptor createNewGroupNameChangedEvent(Cursor cursor) {
        return new RcsGroupThreadNameChangedEventDescriptor(cursor.getLong(cursor.getColumnIndex("origination_timestamp")), cursor.getInt(cursor.getColumnIndex("rcs_thread_id")), cursor.getInt(cursor.getColumnIndex("source_participant")), cursor.getString(cursor.getColumnIndex("new_name")));
    }

    private RcsGroupThreadParticipantLeftEventDescriptor createNewParticipantLeftEvent(Cursor cursor) {
        return new RcsGroupThreadParticipantLeftEventDescriptor(cursor.getLong(cursor.getColumnIndex("origination_timestamp")), cursor.getInt(cursor.getColumnIndex("rcs_thread_id")), cursor.getInt(cursor.getColumnIndex("source_participant")), cursor.getInt(cursor.getColumnIndex("destination_participant")));
    }

    private RcsGroupThreadParticipantJoinedEventDescriptor createNewParticipantJoinedEvent(Cursor cursor) {
        return new RcsGroupThreadParticipantJoinedEventDescriptor(cursor.getLong(cursor.getColumnIndex("origination_timestamp")), cursor.getInt(cursor.getColumnIndex("rcs_thread_id")), cursor.getInt(cursor.getColumnIndex("source_participant")), cursor.getInt(cursor.getColumnIndex("destination_participant")));
    }

    private RcsParticipantAliasChangedEventDescriptor createNewParticipantAliasChangedEvent(Cursor cursor) {
        return new RcsParticipantAliasChangedEventDescriptor(cursor.getLong(cursor.getColumnIndex("origination_timestamp")), cursor.getInt(cursor.getColumnIndex("source_participant")), cursor.getString(cursor.getColumnIndex("new_alias")));
    }
}
