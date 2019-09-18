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
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("ConditionProviders", 3);
    private static final boolean DEBUG_ATTENDEES = false;
    private static final int EVENT_CHECK_LOOKAHEAD = 86400000;
    private static final String INSTANCE_ORDER_BY = "begin ASC";
    private static final String[] INSTANCE_PROJECTION = {"begin", "end", "title", "visible", "event_id", "calendar_displayName", "ownerAccount", "calendar_id", "availability"};
    private static final String TAG = "ConditionProviders.CT";
    /* access modifiers changed from: private */
    public Callback mCallback;
    private final ContentObserver mObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange, Uri u) {
            if (CalendarTracker.DEBUG) {
                Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange + " uri=" + u + " u=" + CalendarTracker.this.mUserContext.getUserId());
            }
            if (CalendarTracker.this.mCallback != null) {
                CalendarTracker.this.mCallback.onChanged();
            }
        }

        public void onChange(boolean selfChange) {
            if (CalendarTracker.DEBUG) {
                Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange);
            }
        }
    };
    private boolean mRegistered;
    private final Context mSystemContext;
    /* access modifiers changed from: private */
    public final Context mUserContext;

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

    private ArraySet<Long> getPrimaryCalendars() {
        long start = System.currentTimeMillis();
        ArraySet<Long> rt = new ArraySet<>();
        Cursor cursor = null;
        try {
            cursor = this.mUserContext.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, new String[]{"_id", "(account_name=ownerAccount) AS \"primary\""}, "\"primary\" = 1", null, null);
            while (cursor != null && cursor.moveToNext()) {
                rt.add(Long.valueOf(cursor.getLong(0)));
            }
            if (DEBUG) {
                Log.d(TAG, "getPrimaryCalendars took " + (System.currentTimeMillis() - start));
            }
            return rt;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:121:0x0243  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x024a  */
    public CheckEventResult checkEvent(ZenModeConfig.EventInfo filter, long time) {
        Cursor cursor;
        CheckEventResult result;
        int availability;
        int eventId;
        CheckEventResult result2;
        Cursor cursor2;
        Uri.Builder uriBuilder;
        ArraySet<Long> primaryCalendars;
        boolean z;
        String owner;
        String name;
        Object[] objArr;
        ZenModeConfig.EventInfo eventInfo = filter;
        long j = time;
        Uri.Builder uriBuilder2 = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(uriBuilder2, j);
        ContentUris.appendId(uriBuilder2, j + 86400000);
        Uri uri = uriBuilder2.build();
        Cursor cursor3 = this.mUserContext.getContentResolver().query(uri, INSTANCE_PROJECTION, null, null, INSTANCE_ORDER_BY);
        CheckEventResult result3 = new CheckEventResult();
        result3.recheckAt = 86400000 + j;
        try {
            ArraySet<Long> primaryCalendars2 = getPrimaryCalendars();
            while (cursor3 != null) {
                try {
                    if (!cursor3.moveToNext()) {
                        break;
                    }
                    long begin = cursor3.getLong(0);
                    long end = cursor3.getLong(1);
                    String title = cursor3.getString(2);
                    boolean calendarVisible = cursor3.getInt(3) == 1;
                    int eventId2 = cursor3.getInt(4);
                    String name2 = cursor3.getString(5);
                    String owner2 = cursor3.getString(6);
                    long calendarId = cursor3.getLong(7);
                    int availability2 = cursor3.getInt(8);
                    Uri uri2 = uri;
                    long calendarId2 = calendarId;
                    try {
                        boolean calendarPrimary = primaryCalendars2.contains(Long.valueOf(calendarId2));
                        if (DEBUG) {
                            primaryCalendars = primaryCalendars2;
                            uriBuilder = uriBuilder2;
                            cursor2 = cursor3;
                            try {
                                objArr = new Object[10];
                                z = false;
                                objArr[0] = title;
                                result2 = result3;
                            } catch (Exception e) {
                                e = e;
                                result = result3;
                                cursor = cursor2;
                                try {
                                    Slog.w(TAG, "error reading calendar", e);
                                    if (cursor != null) {
                                    }
                                    return result;
                                } catch (Throwable th) {
                                    th = th;
                                    if (cursor != null) {
                                    }
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                CheckEventResult checkEventResult = result3;
                                cursor = cursor2;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                            try {
                                objArr[1] = new Date(begin);
                                objArr[2] = new Date(end);
                                objArr[3] = Boolean.valueOf(calendarVisible);
                                int availability3 = availability2;
                                objArr[4] = availabilityToString(availability3);
                                int eventId3 = eventId2;
                                objArr[5] = Integer.valueOf(eventId3);
                                eventId = eventId3;
                                name = name2;
                                objArr[6] = name;
                                availability = availability3;
                                owner = owner2;
                                objArr[7] = owner;
                                objArr[8] = Long.valueOf(calendarId2);
                                objArr[9] = Boolean.valueOf(calendarPrimary);
                                Log.d(TAG, String.format("%s %s-%s v=%s a=%s eid=%s n=%s o=%s cid=%s p=%s", objArr));
                            } catch (Exception e2) {
                                e = e2;
                                cursor = cursor2;
                                result = result2;
                            } catch (Throwable th3) {
                                th = th3;
                                cursor = cursor2;
                                CheckEventResult checkEventResult2 = result2;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        } else {
                            primaryCalendars = primaryCalendars2;
                            uriBuilder = uriBuilder2;
                            cursor2 = cursor3;
                            result2 = result3;
                            eventId = eventId2;
                            name = name2;
                            owner = owner2;
                            availability = availability2;
                            z = false;
                        }
                        boolean meetsTime = (j < begin || j >= end) ? z : true;
                        boolean meetsCalendar = (!calendarVisible || !calendarPrimary || (eventInfo.calendar != null && !Objects.equals(eventInfo.calendar, owner) && !Objects.equals(eventInfo.calendar, name))) ? z : true;
                        String str = name;
                        if (availability != 1) {
                            z = true;
                        }
                        boolean meetsAvailability = z;
                        if (meetsCalendar && meetsAvailability) {
                            try {
                                if (DEBUG) {
                                    boolean z2 = meetsAvailability;
                                    boolean z3 = meetsCalendar;
                                    Log.d(TAG, "  MEETS CALENDAR & AVAILABILITY");
                                } else {
                                    boolean z4 = meetsCalendar;
                                }
                                int eventId4 = eventId;
                                if (meetsAttendee(eventInfo, eventId4, owner)) {
                                    if (DEBUG) {
                                        Log.d(TAG, "    MEETS ATTENDEE");
                                    }
                                    if (meetsTime) {
                                        if (DEBUG) {
                                            Log.d(TAG, "      MEETS TIME");
                                        }
                                        result = result2;
                                        try {
                                            result.inEvent = true;
                                        } catch (Exception e3) {
                                            e = e3;
                                            cursor = cursor2;
                                            Slog.w(TAG, "error reading calendar", e);
                                            if (cursor != null) {
                                            }
                                            return result;
                                        } catch (Throwable th4) {
                                            th = th4;
                                            cursor = cursor2;
                                            if (cursor != null) {
                                            }
                                            throw th;
                                        }
                                    } else {
                                        result = result2;
                                    }
                                    if (begin > j) {
                                        int i = eventId4;
                                        boolean z5 = calendarVisible;
                                        if (begin < result.recheckAt) {
                                            result.recheckAt = begin;
                                            result3 = result;
                                            uri = uri2;
                                            primaryCalendars2 = primaryCalendars;
                                            uriBuilder2 = uriBuilder;
                                            cursor3 = cursor2;
                                            eventInfo = filter;
                                        }
                                    } else {
                                        int i2 = eventId4;
                                        boolean z6 = calendarVisible;
                                    }
                                    if (end > j && end < result.recheckAt) {
                                        result.recheckAt = end;
                                    }
                                    result3 = result;
                                    uri = uri2;
                                    primaryCalendars2 = primaryCalendars;
                                    uriBuilder2 = uriBuilder;
                                    cursor3 = cursor2;
                                    eventInfo = filter;
                                }
                            } catch (Exception e4) {
                                e = e4;
                                result = result2;
                                cursor = cursor2;
                                Slog.w(TAG, "error reading calendar", e);
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return result;
                            } catch (Throwable th5) {
                                th = th5;
                                CheckEventResult checkEventResult3 = result2;
                                cursor = cursor2;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                        }
                        result = result2;
                        result3 = result;
                        uri = uri2;
                        primaryCalendars2 = primaryCalendars;
                        uriBuilder2 = uriBuilder;
                        cursor3 = cursor2;
                        eventInfo = filter;
                    } catch (Exception e5) {
                        e = e5;
                        Uri.Builder builder = uriBuilder2;
                        result = result3;
                        cursor = cursor3;
                        Slog.w(TAG, "error reading calendar", e);
                        if (cursor != null) {
                        }
                        return result;
                    } catch (Throwable th6) {
                        th = th6;
                        Uri.Builder builder2 = uriBuilder2;
                        CheckEventResult checkEventResult4 = result3;
                        cursor = cursor3;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (Exception e6) {
                    e = e6;
                    Uri.Builder builder3 = uriBuilder2;
                    Uri uri3 = uri;
                    result = result3;
                    cursor = cursor3;
                    Slog.w(TAG, "error reading calendar", e);
                    if (cursor != null) {
                    }
                    return result;
                } catch (Throwable th7) {
                    th = th7;
                    Uri.Builder builder4 = uriBuilder2;
                    Uri uri4 = uri;
                    CheckEventResult checkEventResult5 = result3;
                    cursor = cursor3;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            Uri uri5 = uri;
            Cursor cursor4 = cursor3;
            result = result3;
            if (cursor4 != null) {
                cursor4.close();
            } else {
                Cursor cursor5 = cursor4;
            }
        } catch (Exception e7) {
            e = e7;
            Uri.Builder builder5 = uriBuilder2;
            Uri uri6 = uri;
            cursor = cursor3;
            result = result3;
            Slog.w(TAG, "error reading calendar", e);
            if (cursor != null) {
            }
            return result;
        } catch (Throwable th8) {
            th = th8;
            Uri.Builder builder6 = uriBuilder2;
            Uri uri7 = uri;
            cursor = cursor3;
            CheckEventResult checkEventResult6 = result3;
            if (cursor != null) {
            }
            throw th;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:61:0x013b  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0142  */
    private boolean meetsAttendee(ZenModeConfig.EventInfo filter, int eventId, String email) {
        String selection;
        String[] selectionArgs;
        boolean z;
        boolean eventMeets;
        String str = email;
        long start = System.currentTimeMillis();
        String selection2 = ATTENDEE_SELECTION;
        int i = 2;
        boolean z2 = false;
        int i2 = 1;
        String[] selectionArgs2 = {Integer.toString(eventId), str};
        Cursor cursor = this.mUserContext.getContentResolver().query(CalendarContract.Attendees.CONTENT_URI, ATTENDEE_PROJECTION, selection2, selectionArgs2, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 0) {
                    ZenModeConfig.EventInfo eventInfo = filter;
                    String[] strArr = selectionArgs2;
                    String str2 = selection2;
                    int i3 = eventId;
                } else {
                    boolean rt = false;
                    while (cursor != null && cursor.moveToNext()) {
                        long rowEventId = cursor.getLong(z2 ? 1 : 0);
                        String rowEmail = cursor.getString(i2);
                        int status = cursor.getInt(i);
                        try {
                            boolean meetsReply = meetsReply(filter.reply, status);
                            if (DEBUG) {
                                StringBuilder sb = new StringBuilder();
                                selectionArgs = selectionArgs2;
                                try {
                                    sb.append(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                    selection = selection2;
                                    try {
                                        z = false;
                                        sb.append(String.format("status=%s, meetsReply=%s", new Object[]{attendeeStatusToString(status), Boolean.valueOf(meetsReply)}));
                                        Log.d(TAG, sb.toString());
                                    } catch (Throwable th) {
                                        th = th;
                                        int i4 = eventId;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    String str3 = selection2;
                                    int i5 = eventId;
                                    if (cursor != null) {
                                    }
                                    if (DEBUG) {
                                    }
                                    throw th;
                                }
                            } else {
                                selectionArgs = selectionArgs2;
                                selection = selection2;
                                z = z2;
                                int i6 = i;
                            }
                            if (rowEventId == ((long) eventId)) {
                                try {
                                    if (Objects.equals(rowEmail, str) && meetsReply) {
                                        eventMeets = true;
                                        rt |= eventMeets;
                                        z2 = z;
                                        selectionArgs2 = selectionArgs;
                                        selection2 = selection;
                                        i = 2;
                                        i2 = 1;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    if (cursor != null) {
                                    }
                                    if (DEBUG) {
                                    }
                                    throw th;
                                }
                            }
                            eventMeets = z;
                            rt |= eventMeets;
                            z2 = z;
                            selectionArgs2 = selectionArgs;
                            selection2 = selection;
                            i = 2;
                            i2 = 1;
                        } catch (Throwable th4) {
                            th = th4;
                            String[] strArr2 = selectionArgs2;
                            String str4 = selection2;
                            int i7 = eventId;
                            if (cursor != null) {
                            }
                            if (DEBUG) {
                            }
                            throw th;
                        }
                    }
                    ZenModeConfig.EventInfo eventInfo2 = filter;
                    String[] strArr3 = selectionArgs2;
                    String str5 = selection2;
                    int i8 = eventId;
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (DEBUG) {
                        Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                    }
                    return rt;
                }
            } catch (Throwable th5) {
                th = th5;
                ZenModeConfig.EventInfo eventInfo3 = filter;
                String[] strArr22 = selectionArgs2;
                String str42 = selection2;
                int i72 = eventId;
                if (cursor != null) {
                    cursor.close();
                }
                if (DEBUG) {
                    Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                }
                throw th;
            }
        } else {
            ZenModeConfig.EventInfo eventInfo4 = filter;
            String[] strArr4 = selectionArgs2;
            String str6 = selection2;
            int i9 = eventId;
        }
        if (DEBUG) {
            Log.d(TAG, "No attendees found");
        }
        if (cursor != null) {
            cursor.close();
        }
        if (DEBUG) {
            Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
        }
        return true;
    }

    private void setRegistered(boolean registered) {
        if (this.mRegistered != registered) {
            ContentResolver cr = this.mSystemContext.getContentResolver();
            if (this.mRegistered) {
                if (DEBUG) {
                    Log.d(TAG, "unregister content observer u=0");
                }
                cr.unregisterContentObserver(this.mObserver);
            }
            this.mRegistered = registered;
            if (DEBUG) {
                Log.d(TAG, "mRegistered = " + registered + " u=" + 0);
            }
            if (this.mRegistered) {
                if (DEBUG) {
                    Log.d(TAG, "register content observer u=0");
                }
                cr.registerContentObserver(CalendarContract.Instances.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(CalendarContract.Events.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(CalendarContract.Calendars.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(CalendarContract.Attendees.CONTENT_URI, true, this.mObserver, 0);
            }
        }
    }

    private static String attendeeStatusToString(int status) {
        switch (status) {
            case 0:
                return "ATTENDEE_STATUS_NONE";
            case 1:
                return "ATTENDEE_STATUS_ACCEPTED";
            case 2:
                return "ATTENDEE_STATUS_DECLINED";
            case 3:
                return "ATTENDEE_STATUS_INVITED";
            case 4:
                return "ATTENDEE_STATUS_TENTATIVE";
            default:
                return "ATTENDEE_STATUS_UNKNOWN_" + status;
        }
    }

    private static String availabilityToString(int availability) {
        switch (availability) {
            case 0:
                return "AVAILABILITY_BUSY";
            case 1:
                return "AVAILABILITY_FREE";
            case 2:
                return "AVAILABILITY_TENTATIVE";
            default:
                return "AVAILABILITY_UNKNOWN_" + availability;
        }
    }

    private static boolean meetsReply(int reply, int attendeeStatus) {
        boolean z = false;
        switch (reply) {
            case 0:
                if (attendeeStatus != 2) {
                    z = true;
                }
                return z;
            case 1:
                if (attendeeStatus == 1 || attendeeStatus == 4) {
                    z = true;
                }
                return z;
            case 2:
                if (attendeeStatus == 1) {
                    z = true;
                }
                return z;
            default:
                return false;
        }
    }
}
