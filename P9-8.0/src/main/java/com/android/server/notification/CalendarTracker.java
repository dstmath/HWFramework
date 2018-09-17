package com.android.server.notification;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.service.notification.ZenModeConfig.EventInfo;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.usage.UnixCalendar;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Objects;

public class CalendarTracker {
    private static final String[] ATTENDEE_PROJECTION = new String[]{"event_id", "attendeeEmail", "attendeeStatus"};
    private static final String ATTENDEE_SELECTION = "event_id = ? AND attendeeEmail = ?";
    private static final boolean DEBUG = Log.isLoggable("ConditionProviders", 3);
    private static final boolean DEBUG_ATTENDEES = false;
    private static final int EVENT_CHECK_LOOKAHEAD = 86400000;
    private static final String INSTANCE_ORDER_BY = "begin ASC";
    private static final String[] INSTANCE_PROJECTION = new String[]{"begin", "end", "title", "visible", "event_id", "calendar_displayName", "ownerAccount", "calendar_id", "availability"};
    private static final String TAG = "ConditionProviders.CT";
    private Callback mCallback;
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
                boolean calendarVisible = cursor.getInt(3) == 1;
                int eventId = cursor.getInt(4);
                String name = cursor.getString(5);
                String owner = cursor.getString(6);
                long calendarId = cursor.getLong(7);
                int availability = cursor.getInt(8);
                boolean calendarPrimary = primaryCalendars.contains(Long.valueOf(calendarId));
                if (DEBUG) {
                    Log.d(TAG, String.format("%s %s-%s v=%s a=%s eid=%s n=%s o=%s cid=%s p=%s", new Object[]{title, new Date(begin), new Date(end), Boolean.valueOf(calendarVisible), availabilityToString(availability), Integer.valueOf(eventId), name, owner, Long.valueOf(calendarId), Boolean.valueOf(calendarPrimary)}));
                }
                boolean meetsTime = time >= begin && time < end;
                if (!calendarVisible || !calendarPrimary) {
                    meetsCalendar = false;
                } else if (filter.calendar == null || Objects.equals(filter.calendar, owner)) {
                    meetsCalendar = true;
                } else {
                    meetsCalendar = Objects.equals(filter.calendar, name);
                }
                boolean meetsAvailability = availability != 1;
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
                        } else if (end > time) {
                            if (end < result.recheckAt) {
                                result.recheckAt = end;
                            }
                        }
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
                if (cursor.getCount() != 0) {
                    boolean rt = false;
                    while (cursor != null) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                        int eventMeets;
                        long rowEventId = cursor.getLong(0);
                        String rowEmail = cursor.getString(1);
                        boolean meetsReply = meetsReply(filter.reply, cursor.getInt(2));
                        if (DEBUG) {
                            Log.d(TAG, "" + String.format("status=%s, meetsReply=%s", new Object[]{attendeeStatusToString(status), Boolean.valueOf(meetsReply)}));
                        }
                        if (rowEventId == ((long) eventId) && Objects.equals(rowEmail, email)) {
                            eventMeets = meetsReply;
                        } else {
                            eventMeets = 0;
                        }
                        rt |= eventMeets;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (DEBUG) {
                        Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                    }
                    return rt;
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                if (DEBUG) {
                    Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                }
            }
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
                cr.registerContentObserver(Instances.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(Events.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(Calendars.CONTENT_URI, true, this.mObserver, 0);
                cr.registerContentObserver(Attendees.CONTENT_URI, true, this.mObserver, 0);
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
        boolean z = true;
        switch (reply) {
            case 0:
                if (attendeeStatus == 2) {
                    z = false;
                }
                return z;
            case 1:
                if (!(attendeeStatus == 1 || attendeeStatus == 4)) {
                    z = false;
                }
                return z;
            case 2:
                if (attendeeStatus != 1) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }
}
