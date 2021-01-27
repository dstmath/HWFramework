package com.android.server.notification;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.service.notification.ZenModeConfig;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Objects;

public class CalendarTracker {
    private static final String[] ATTENDEE_PROJECTION = {"event_id", "attendeeEmail", "attendeeStatus"};
    private static final String ATTENDEE_SELECTION = "event_id = ? AND attendeeEmail = ?";
    private static final boolean DEBUG = Log.isLoggable("ConditionProviders", 3);
    private static final boolean DEBUG_ATTENDEES = false;
    private static final int EVENT_CHECK_LOOKAHEAD = 86400000;
    private static final String INSTANCE_ORDER_BY = "begin ASC";
    private static final String[] INSTANCE_PROJECTION = {"begin", "end", "title", "visible", "event_id", "calendar_displayName", "ownerAccount", "calendar_id", "availability"};
    private static final String TAG = "ConditionProviders.CT";
    private Callback mCallback;
    private final ContentObserver mObserver = new ContentObserver(null) {
        /* class com.android.server.notification.CalendarTracker.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri u) {
            if (CalendarTracker.DEBUG) {
                Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange + " uri=" + u + " u=" + CalendarTracker.this.mUserContext.getUserId());
            }
            if (CalendarTracker.this.mCallback != null) {
                CalendarTracker.this.mCallback.onChanged();
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            if (CalendarTracker.DEBUG) {
                Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange);
            }
        }
    };
    private boolean mRegistered;
    private final Context mSystemContext;
    private final Context mUserContext;

    public interface Callback {
        void onChanged();
    }

    public static class CheckEventResult {
        public boolean inEvent;
        public long recheckAt;
    }

    public CalendarTracker(Context systemContext, Context userContext) {
        this.mSystemContext = systemContext;
        this.mUserContext = userContext;
    }

    public void setCallback(Callback callback) {
        if (this.mCallback != callback) {
            this.mCallback = callback;
            setRegistered(this.mCallback != null);
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mCallback=");
        pw.println(this.mCallback);
        pw.print(prefix);
        pw.print("mRegistered=");
        pw.println(this.mRegistered);
        pw.print(prefix);
        pw.print("u=");
        pw.println(this.mUserContext.getUserId());
    }

    private ArraySet<Long> getCalendarsWithAccess() {
        long start = System.currentTimeMillis();
        ArraySet<Long> rt = new ArraySet<>();
        Cursor cursor = null;
        try {
            cursor = this.mUserContext.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, new String[]{"_id"}, "calendar_access_level >= 500 AND sync_events = 1", null, null);
            while (cursor != null && cursor.moveToNext()) {
                rt.add(Long.valueOf(cursor.getLong(0)));
            }
            if (DEBUG) {
                Log.d(TAG, "getCalendarsWithAccess took " + (System.currentTimeMillis() - start));
            }
            return rt;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX INFO: Multiple debug info for r10v5 'owner'  java.lang.String: [D('cursor' android.database.Cursor), D('owner' java.lang.String)] */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00a3: APUT  
      (r6v16 java.lang.Object[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: java.util.Date : 0x009e: CONSTRUCTOR  (r9v6 java.util.Date) = (r12v1 'begin' long A[D('begin' long)]) call: java.util.Date.<init>(long):void type: CONSTRUCTOR)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0142, code lost:
        if (java.util.Objects.equals(r35.calName, r9) == false) goto L_0x014b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01c4, code lost:
        if (r18 != null) goto L_0x01c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01c6, code lost:
        r18.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01df, code lost:
        if (r18 != null) goto L_0x01c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x01e2, code lost:
        return r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x01e6  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0154 A[Catch:{ Exception -> 0x0146 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x015a A[ADDED_TO_REGION, Catch:{ Exception -> 0x0146 }] */
    public CheckEventResult checkEvent(ZenModeConfig.EventInfo filter, long time) {
        Cursor cursor;
        Throwable th;
        Exception e;
        Uri uri;
        Uri.Builder uriBuilder;
        ArraySet<Long> calendars;
        boolean meetsAvailability;
        String owner;
        String name;
        boolean meetsCalendar;
        Object[] objArr;
        CalendarTracker calendarTracker = this;
        Uri.Builder uriBuilder2 = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(uriBuilder2, time);
        ContentUris.appendId(uriBuilder2, time + 86400000);
        Uri uri2 = uriBuilder2.build();
        Cursor cursor2 = calendarTracker.mUserContext.getContentResolver().query(uri2, INSTANCE_PROJECTION, null, null, INSTANCE_ORDER_BY);
        CheckEventResult result = new CheckEventResult();
        result.recheckAt = 86400000 + time;
        try {
            ArraySet<Long> calendars2 = getCalendarsWithAccess();
            while (cursor2 != null && cursor2.moveToNext()) {
                long begin = cursor2.getLong(0);
                long end = cursor2.getLong(1);
                String title = cursor2.getString(2);
                boolean calendarVisible = cursor2.getInt(3) == 1;
                int eventId = cursor2.getInt(4);
                String name2 = cursor2.getString(5);
                String owner2 = cursor2.getString(6);
                long calendarId = cursor2.getLong(7);
                int availability = cursor2.getInt(8);
                boolean canAccessCal = calendars2.contains(Long.valueOf(calendarId));
                if (DEBUG) {
                    calendars = calendars2;
                    uriBuilder = uriBuilder2;
                    try {
                        objArr = new Object[10];
                        meetsAvailability = false;
                        objArr[0] = title;
                        uri = uri2;
                        try {
                            objArr[1] = new Date(begin);
                            objArr[2] = new Date(end);
                            objArr[3] = Boolean.valueOf(calendarVisible);
                            objArr[4] = availabilityToString(availability);
                            objArr[5] = Integer.valueOf(eventId);
                            name = name2;
                            objArr[6] = name;
                            cursor = cursor2;
                            owner = owner2;
                        } catch (Exception e2) {
                            e = e2;
                            cursor = cursor2;
                            try {
                                Slog.w(TAG, "error reading calendar", e);
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            cursor = cursor2;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        cursor = cursor2;
                        Slog.w(TAG, "error reading calendar", e);
                    } catch (Throwable th4) {
                        th = th4;
                        cursor = cursor2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                    try {
                        objArr[7] = owner;
                        objArr[8] = Long.valueOf(calendarId);
                        objArr[9] = Boolean.valueOf(canAccessCal);
                        Log.d(TAG, String.format("title=%s time=%s-%s vis=%s availability=%s eventId=%s name=%s owner=%s calId=%s canAccessCal=%s", objArr));
                    } catch (Exception e4) {
                        e = e4;
                        Slog.w(TAG, "error reading calendar", e);
                    }
                } else {
                    calendars = calendars2;
                    uriBuilder = uriBuilder2;
                    uri = uri2;
                    cursor = cursor2;
                    name = name2;
                    owner = owner2;
                    meetsAvailability = false;
                }
                boolean meetsTime = (time < begin || time >= end) ? meetsAvailability : true;
                if (calendarVisible && canAccessCal) {
                    if (filter.calName == null) {
                        if (filter.calendarId == null) {
                            meetsCalendar = true;
                            if (availability != 1) {
                                meetsAvailability = true;
                            }
                            if (!meetsCalendar && meetsAvailability) {
                                if (DEBUG) {
                                    Log.d(TAG, "  MEETS CALENDAR & AVAILABILITY");
                                }
                                if (calendarTracker.meetsAttendee(filter, eventId, owner)) {
                                    if (DEBUG) {
                                        Log.d(TAG, "    MEETS ATTENDEE");
                                    }
                                    if (meetsTime) {
                                        if (DEBUG) {
                                            Log.d(TAG, "      MEETS TIME");
                                        }
                                        result.inEvent = true;
                                    }
                                    if (begin > time) {
                                        if (begin < result.recheckAt) {
                                            result.recheckAt = begin;
                                        }
                                    }
                                    if (end > time && end < result.recheckAt) {
                                        result.recheckAt = end;
                                    }
                                }
                            }
                            calendarTracker = this;
                            cursor2 = cursor;
                            calendars2 = calendars;
                            uriBuilder2 = uriBuilder;
                            uri2 = uri;
                        }
                    }
                    if (!Objects.equals(filter.calendarId, Long.valueOf(calendarId))) {
                    }
                    meetsCalendar = true;
                    if (availability != 1) {
                    }
                    if (!meetsCalendar) {
                    }
                    calendarTracker = this;
                    cursor2 = cursor;
                    calendars2 = calendars;
                    uriBuilder2 = uriBuilder;
                    uri2 = uri;
                }
                meetsCalendar = meetsAvailability;
                if (availability != 1) {
                }
                if (!meetsCalendar) {
                }
                calendarTracker = this;
                cursor2 = cursor;
                calendars2 = calendars;
                uriBuilder2 = uriBuilder;
                uri2 = uri;
            }
            cursor = cursor2;
        } catch (Exception e5) {
            e = e5;
            cursor = cursor2;
            Slog.w(TAG, "error reading calendar", e);
        } catch (Throwable th5) {
            th = th5;
            cursor = cursor2;
            if (cursor != null) {
            }
            throw th;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v4, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r7v5, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r7v10, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0129  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0130  */
    private boolean meetsAttendee(ZenModeConfig.EventInfo filter, int eventId, String email) {
        Throwable th;
        String selection;
        int i;
        String[] selectionArgs;
        int i2;
        long start = System.currentTimeMillis();
        String selection2 = ATTENDEE_SELECTION;
        int i3 = 2;
        int i4 = 0;
        int i5 = 1;
        String[] selectionArgs2 = {Integer.toString(eventId), email};
        Cursor cursor = this.mUserContext.getContentResolver().query(CalendarContract.Attendees.CONTENT_URI, ATTENDEE_PROJECTION, selection2, selectionArgs2, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    boolean rt = 0;
                    while (cursor.moveToNext()) {
                        long rowEventId = cursor.getLong(i4);
                        String rowEmail = cursor.getString(i5);
                        int status = cursor.getInt(i3);
                        boolean meetsReply = meetsReply(filter.reply, status);
                        if (DEBUG) {
                            selectionArgs = selectionArgs2;
                            try {
                                StringBuilder sb = new StringBuilder();
                                selection = selection2;
                                try {
                                    sb.append("");
                                    i = 0;
                                    sb.append(String.format("status=%s, meetsReply=%s", attendeeStatusToString(status), Boolean.valueOf(meetsReply)));
                                    Log.d(TAG, sb.toString());
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                if (cursor != null) {
                                }
                                if (DEBUG) {
                                }
                                throw th;
                            }
                        } else {
                            selectionArgs = selectionArgs2;
                            selection = selection2;
                            i = 0;
                        }
                        if (rowEventId == ((long) eventId)) {
                            try {
                                if (Objects.equals(rowEmail, email) && meetsReply) {
                                    i2 = 1;
                                    int i6 = rt ? 1 : 0;
                                    boolean rt2 = rt ? 1 : 0;
                                    boolean rt3 = rt ? 1 : 0;
                                    int i7 = i6 | i2;
                                    selectionArgs2 = selectionArgs;
                                    i4 = i;
                                    selection2 = selection;
                                    i3 = 2;
                                    i5 = 1;
                                    rt = i7;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                if (DEBUG) {
                                    Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                                }
                                throw th;
                            }
                        }
                        i2 = i;
                        int i62 = rt ? 1 : 0;
                        boolean rt22 = rt ? 1 : 0;
                        boolean rt32 = rt ? 1 : 0;
                        int i72 = i62 | i2;
                        selectionArgs2 = selectionArgs;
                        i4 = i;
                        selection2 = selection;
                        i3 = 2;
                        i5 = 1;
                        rt = i72;
                    }
                    cursor.close();
                    if (DEBUG) {
                        Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                    }
                    return rt;
                }
            } catch (Throwable th5) {
                th = th5;
                if (cursor != null) {
                }
                if (DEBUG) {
                }
                throw th;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "No attendees found");
        }
        if (cursor != null) {
            cursor.close();
        }
        if (!DEBUG) {
            return true;
        }
        Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
        return true;
    }

    private void setRegistered(boolean registered) {
        if (this.mRegistered != registered) {
            ContentResolver cr = this.mSystemContext.getContentResolver();
            int userId = this.mUserContext.getUserId();
            if (this.mRegistered) {
                if (DEBUG) {
                    Log.d(TAG, "unregister content observer u=" + userId);
                }
                cr.unregisterContentObserver(this.mObserver);
            }
            this.mRegistered = registered;
            if (DEBUG) {
                Log.d(TAG, "mRegistered = " + registered + " u=" + userId);
            }
            if (this.mRegistered) {
                if (DEBUG) {
                    Log.d(TAG, "register content observer u=" + userId);
                }
                cr.registerContentObserver(CalendarContract.Instances.CONTENT_URI, true, this.mObserver, userId);
                cr.registerContentObserver(CalendarContract.Events.CONTENT_URI, true, this.mObserver, userId);
                cr.registerContentObserver(CalendarContract.Calendars.CONTENT_URI, true, this.mObserver, userId);
            }
        }
    }

    private static String attendeeStatusToString(int status) {
        if (status == 0) {
            return "ATTENDEE_STATUS_NONE";
        }
        if (status == 1) {
            return "ATTENDEE_STATUS_ACCEPTED";
        }
        if (status == 2) {
            return "ATTENDEE_STATUS_DECLINED";
        }
        if (status == 3) {
            return "ATTENDEE_STATUS_INVITED";
        }
        if (status == 4) {
            return "ATTENDEE_STATUS_TENTATIVE";
        }
        return "ATTENDEE_STATUS_UNKNOWN_" + status;
    }

    private static String availabilityToString(int availability) {
        if (availability == 0) {
            return "AVAILABILITY_BUSY";
        }
        if (availability == 1) {
            return "AVAILABILITY_FREE";
        }
        if (availability == 2) {
            return "AVAILABILITY_TENTATIVE";
        }
        return "AVAILABILITY_UNKNOWN_" + availability;
    }

    private static boolean meetsReply(int reply, int attendeeStatus) {
        return reply != 0 ? reply != 1 ? reply == 2 && attendeeStatus == 1 : attendeeStatus == 1 || attendeeStatus == 4 : attendeeStatus != 2;
    }
}
