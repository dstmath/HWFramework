package com.android.server.notification;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Handler;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.service.notification.ZenModeConfig.EventInfo;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.usage.UnixCalendar;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Objects;

public class CalendarTracker {
    private static final String[] ATTENDEE_PROJECTION = null;
    private static final String ATTENDEE_SELECTION = "event_id = ? AND attendeeEmail = ?";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_ATTENDEES = false;
    private static final int EVENT_CHECK_LOOKAHEAD = 86400000;
    private static final String INSTANCE_ORDER_BY = "begin ASC";
    private static final String[] INSTANCE_PROJECTION = null;
    private static final String TAG = "ConditionProviders.CT";
    private Callback mCallback;
    private final ContentObserver mObserver;
    private boolean mRegistered;
    private final Context mSystemContext;
    private final Context mUserContext;

    /* renamed from: com.android.server.notification.CalendarTracker.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange, Uri u) {
            if (CalendarTracker.DEBUG) {
                Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange + " uri=" + u + " u=" + CalendarTracker.this.mUserContext.getUserId());
            }
            CalendarTracker.this.mCallback.onChanged();
        }

        public void onChange(boolean selfChange) {
            if (CalendarTracker.DEBUG) {
                Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange);
            }
        }
    }

    public interface Callback {
        void onChanged();
    }

    public static class CheckEventResult {
        public boolean inEvent;
        public long recheckAt;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.CalendarTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.CalendarTracker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.CalendarTracker.<clinit>():void");
    }

    public CalendarTracker(Context systemContext, Context userContext) {
        this.mObserver = new AnonymousClass1(null);
        this.mSystemContext = systemContext;
        this.mUserContext = userContext;
    }

    public void setCallback(Callback callback) {
        if (this.mCallback != callback) {
            this.mCallback = callback;
            setRegistered(this.mCallback != null ? true : DEBUG_ATTENDEES);
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
        ArraySet<Long> rt = new ArraySet();
        String primary = "\"primary\"";
        String selection = "\"primary\" = 1";
        Cursor cursor = null;
        try {
            cursor = this.mUserContext.getContentResolver().query(Calendars.CONTENT_URI, new String[]{"_id", "(account_name=ownerAccount) AS \"primary\""}, "\"primary\" = 1", null, null);
            while (cursor != null && cursor.moveToNext()) {
                rt.add(Long.valueOf(cursor.getLong(0)));
            }
            if (cursor != null) {
                cursor.close();
            }
            if (DEBUG) {
                Log.d(TAG, "getPrimaryCalendars took " + (System.currentTimeMillis() - start));
            }
            return rt;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public CheckEventResult checkEvent(EventInfo filter, long time) {
        Builder uriBuilder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(uriBuilder, time);
        ContentUris.appendId(uriBuilder, UnixCalendar.DAY_IN_MILLIS + time);
        Cursor cursor = this.mUserContext.getContentResolver().query(uriBuilder.build(), INSTANCE_PROJECTION, null, null, INSTANCE_ORDER_BY);
        CheckEventResult result = new CheckEventResult();
        result.recheckAt = UnixCalendar.DAY_IN_MILLIS + time;
        try {
            ArraySet<Long> primaryCalendars = getPrimaryCalendars();
            while (cursor != null && cursor.moveToNext()) {
                boolean meetsCalendar;
                long begin = cursor.getLong(0);
                long end = cursor.getLong(1);
                String title = cursor.getString(2);
                boolean calendarVisible = cursor.getInt(3) == 1 ? true : DEBUG_ATTENDEES;
                int eventId = cursor.getInt(4);
                String name = cursor.getString(5);
                String owner = cursor.getString(6);
                long calendarId = cursor.getLong(7);
                int availability = cursor.getInt(8);
                boolean calendarPrimary = primaryCalendars.contains(Long.valueOf(calendarId));
                if (DEBUG) {
                    Log.d(TAG, String.format("%s %s-%s v=%s a=%s eid=%s n=%s o=%s cid=%s p=%s", new Object[]{title, new Date(begin), new Date(end), Boolean.valueOf(calendarVisible), availabilityToString(availability), Integer.valueOf(eventId), name, owner, Long.valueOf(calendarId), Boolean.valueOf(calendarPrimary)}));
                }
                boolean meetsTime = (time < begin || time >= end) ? DEBUG_ATTENDEES : true;
                if (!calendarVisible || !calendarPrimary) {
                    meetsCalendar = DEBUG_ATTENDEES;
                } else if (filter.calendar == null || Objects.equals(filter.calendar, owner)) {
                    meetsCalendar = true;
                } else {
                    meetsCalendar = Objects.equals(filter.calendar, name);
                }
                boolean meetsAvailability = availability != 1 ? true : DEBUG_ATTENDEES;
                if (meetsCalendar && meetsAvailability) {
                    if (DEBUG) {
                        Log.d(TAG, "  MEETS CALENDAR & AVAILABILITY");
                    }
                    if (meetsAttendee(filter, eventId, owner)) {
                        if (DEBUG) {
                            Log.d(TAG, "    MEETS ATTENDEE");
                        }
                        if (meetsTime) {
                            if (DEBUG) {
                                Log.d(TAG, "      MEETS TIME");
                            }
                            result.inEvent = true;
                        }
                        if (begin > time && begin < result.recheckAt) {
                            result.recheckAt = begin;
                        } else if (end <= time) {
                            continue;
                        } else if (end < result.recheckAt) {
                            result.recheckAt = end;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return result;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean meetsAttendee(EventInfo filter, int eventId, String email) {
        long start = System.currentTimeMillis();
        Cursor cursor = this.mUserContext.getContentResolver().query(Attendees.CONTENT_URI, ATTENDEE_PROJECTION, ATTENDEE_SELECTION, new String[]{Integer.toString(eventId), email}, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 0) {
                    if (DEBUG) {
                        Log.d(TAG, "No attendees found");
                    }
                    return true;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                    if (DEBUG) {
                        Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                    }
                }
            }
        }
        boolean z = DEBUG_ATTENDEES;
        while (cursor != null) {
            if (!cursor.moveToNext()) {
                break;
            }
            long rowEventId = cursor.getLong(0);
            String rowEmail = cursor.getString(1);
            boolean meetsReply = meetsReply(filter.reply, cursor.getInt(2));
            if (DEBUG) {
                Log.d(TAG, "" + String.format("status=%s, meetsReply=%s", new Object[]{attendeeStatusToString(status), Boolean.valueOf(meetsReply)}));
            }
            boolean eventMeets = (rowEventId == ((long) eventId) && Objects.equals(rowEmail, email)) ? meetsReply : DEBUG_ATTENDEES;
            z |= eventMeets;
        }
        if (cursor != null) {
            cursor.close();
            if (DEBUG) {
                Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
            }
        }
        return z;
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
                cr.registerContentObserver(Instances.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(Events.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(Calendars.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(Attendees.CONTENT_URI, true, this.mObserver, 0);
            }
        }
    }

    private static String attendeeStatusToString(int status) {
        switch (status) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "ATTENDEE_STATUS_NONE";
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return "ATTENDEE_STATUS_ACCEPTED";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "ATTENDEE_STATUS_DECLINED";
            case H.REPORT_LOSING_FOCUS /*3*/:
                return "ATTENDEE_STATUS_INVITED";
            case H.DO_TRAVERSAL /*4*/:
                return "ATTENDEE_STATUS_TENTATIVE";
            default:
                return "ATTENDEE_STATUS_UNKNOWN_" + status;
        }
    }

    private static String availabilityToString(int availability) {
        switch (availability) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "AVAILABILITY_BUSY";
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                return "AVAILABILITY_FREE";
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return "AVAILABILITY_TENTATIVE";
            default:
                return "AVAILABILITY_UNKNOWN_" + availability;
        }
    }

    private static boolean meetsReply(int reply, int attendeeStatus) {
        boolean z = true;
        switch (reply) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                if (attendeeStatus == 2) {
                    z = DEBUG_ATTENDEES;
                }
                return z;
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                if (!(attendeeStatus == 1 || attendeeStatus == 4)) {
                    z = DEBUG_ATTENDEES;
                }
                return z;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                if (attendeeStatus != 1) {
                    z = DEBUG_ATTENDEES;
                }
                return z;
            default:
                return DEBUG_ATTENDEES;
        }
    }
}
