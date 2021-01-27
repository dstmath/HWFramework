package ohos.sysappcomponents.calendar;

import android.provider.CalendarContract;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

public class Attribute {
    public static final String ACCOUNT_TYPE_LOCAL = "LOCAL";
    public static final Uri ATTENDEES_URI = UriConverter.convertToZidaneContentUri(CalendarContract.Attendees.CONTENT_URI, "");
    public static final Uri CALENDARS_URI = UriConverter.convertToZidaneContentUri(CalendarContract.Calendars.CONTENT_URI, "");
    public static final String CALENDAR_NAME = "name";
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";
    public static final Uri EVENTS_URI = UriConverter.convertToZidaneContentUri(CalendarContract.Events.CONTENT_URI, "");
    public static final Uri INSTANCES_URI = Uri.parse("dataability:///com.android.calendar/instances/when");
    public static final Uri REMINDERS_URI = UriConverter.convertToZidaneContentUri(CalendarContract.Reminders.CONTENT_URI, "");

    public interface AttendeesColumns {
        public static final String ATTENDEE_EMAIL = "attendeeEmail";
        public static final String ATTENDEE_IDENTITY = "attendeeIdentity";
        public static final String ATTENDEE_ID_NAMESPACE = "attendeeIdNamespace";
        public static final String ATTENDEE_NAME = "attendeeName";
        public static final String ATTENDEE_RELATIONSHIP = "attendeeRelationship";
        public static final String ATTENDEE_STATUS = "attendeeStatus";
        public static final int ATTENDEE_STATUS_ACCEPTED = 1;
        public static final int ATTENDEE_STATUS_DECLINED = 2;
        public static final int ATTENDEE_STATUS_INVITED = 3;
        public static final int ATTENDEE_STATUS_NONE = 0;
        public static final int ATTENDEE_STATUS_TENTATIVE = 4;
        public static final String ATTENDEE_TYPE = "attendeeType";
        public static final String EVENT_ID = "event_id";
        public static final int RELATIONSHIP_ATTENDEE = 1;
        public static final int RELATIONSHIP_NONE = 0;
        public static final int RELATIONSHIP_ORGANIZER = 2;
        public static final int RELATIONSHIP_PERFORMER = 3;
        public static final int RELATIONSHIP_SPEAKER = 4;
        public static final int TYPE_NONE = 0;
        public static final int TYPE_OPTIONAL = 2;
        public static final int TYPE_REQUIRED = 1;
        public static final int TYPE_RESOURCE = 3;
    }

    public interface BaseColumns {
        public static final String _COUNT = "_count";
        public static final String _ID = "_id";
    }

    public interface CalendarColumns {
        public static final String ALLOWED_ATTENDEE_TYPES = "allowedAttendeeTypes";
        public static final String ALLOWED_AVAILABILITY = "allowedAvailability";
        public static final String ALLOWED_REMINDERS = "allowedReminders";
        public static final String CALENDAR_ACCESS_LEVEL = "calendar_access_level";
        public static final String CALENDAR_COLOR = "calendar_color";
        public static final String CALENDAR_COLOR_KEY = "calendar_color_index";
        public static final String CALENDAR_DISPLAY_NAME = "calendar_displayName";
        public static final String CALENDAR_TIME_ZONE = "calendar_timezone";
        public static final int CAL_ACCESS_CONTRIBUTOR = 500;
        public static final int CAL_ACCESS_EDITOR = 600;
        public static final int CAL_ACCESS_FREEBUSY = 100;
        public static final int CAL_ACCESS_NONE = 0;
        public static final int CAL_ACCESS_OVERRIDE = 400;
        public static final int CAL_ACCESS_OWNER = 700;
        public static final int CAL_ACCESS_READ = 200;
        public static final int CAL_ACCESS_RESPOND = 300;
        public static final int CAL_ACCESS_ROOT = 800;
        public static final String CAN_MODIFY_TIME_ZONE = "canModifyTimeZone";
        public static final String CAN_ORGANIZER_RESPOND = "canOrganizerRespond";
        public static final String IS_PRIMARY = "isPrimary";
        public static final String MAX_REMINDERS = "maxReminders";
        public static final String OWNER_ACCOUNT = "ownerAccount";
        public static final String SYNC_EVENTS = "sync_events";
        public static final String VISIBLE = "visible";
    }

    public interface CalendarSyncColumns {
        public static final String CAL_SYNC1 = "cal_sync1";
        public static final String CAL_SYNC10 = "cal_sync10";
        public static final String CAL_SYNC2 = "cal_sync2";
        public static final String CAL_SYNC3 = "cal_sync3";
        public static final String CAL_SYNC4 = "cal_sync4";
        public static final String CAL_SYNC5 = "cal_sync5";
        public static final String CAL_SYNC6 = "cal_sync6";
        public static final String CAL_SYNC7 = "cal_sync7";
        public static final String CAL_SYNC8 = "cal_sync8";
        public static final String CAL_SYNC9 = "cal_sync9";
    }

    public interface EventsColumns {
        public static final int ACCESS_CONFIDENTIAL = 1;
        public static final int ACCESS_DEFAULT = 0;
        public static final String ACCESS_LEVEL = "accessLevel";
        public static final int ACCESS_PRIVATE = 2;
        public static final int ACCESS_PUBLIC = 3;
        public static final String ALL_DAY = "allDay";
        public static final String AVAILABILITY = "availability";
        public static final int AVAILABILITY_BUSY = 0;
        public static final int AVAILABILITY_FREE = 1;
        public static final int AVAILABILITY_TENTATIVE = 2;
        public static final String CALENDAR_ID = "calendar_id";
        public static final String CAN_INVITE_OTHERS = "canInviteOthers";
        public static final String CUSTOM_APP_PACKAGE = "customAppPackage";
        public static final String CUSTOM_APP_URI = "customAppUri";
        public static final String DESCRIPTION = "description";
        public static final String DISPLAY_COLOR = "displayColor";
        public static final String DTEND = "dtend";
        public static final String DTSTART = "dtstart";
        public static final String DURATION = "duration";
        public static final String EVENT_COLOR = "eventColor";
        public static final String EVENT_COLOR_KEY = "eventColor_index";
        public static final String EVENT_END_TIMEZONE = "eventEndTimezone";
        public static final String EVENT_LOCATION = "eventLocation";
        public static final String EVENT_TIMEZONE = "eventTimezone";
        public static final String EXDATE = "exdate";
        public static final String EXRULE = "exrule";
        public static final String HAS_ALARM = "hasAlarm";
        public static final String HAS_ATTENDEE_DATA = "hasAttendeeData";
        public static final String HAS_EXTENDED_PROPERTIES = "hasExtendedProperties";
        public static final String LAST_DATE = "lastDate";
        public static final String LAST_SYNCED = "lastSynced";
        public static final String ORGANIZER = "organizer";
        public static final String ORIGINAL_ID = "original_id";
        public static final String ORIGINAL_SYNC_ID = "original_sync_id";
        public static final String RDATE = "rdate";
        public static final String RRULE = "rrule";
        public static final String SELF_ATTENDEE_STATUS = "selfAttendeeStatus";
        public static final String STATUS = "eventStatus";
        public static final int STATUS_CANCELED = 2;
        public static final int STATUS_CONFIRMED = 1;
        public static final int STATUS_TENTATIVE = 0;
        public static final String SYNC_DATA1 = "sync_data1";
        public static final String SYNC_DATA10 = "sync_data10";
        public static final String SYNC_DATA2 = "sync_data2";
        public static final String SYNC_DATA3 = "sync_data3";
        public static final String SYNC_DATA4 = "sync_data4";
        public static final String SYNC_DATA5 = "sync_data5";
        public static final String SYNC_DATA6 = "sync_data6";
        public static final String SYNC_DATA7 = "sync_data7";
        public static final String SYNC_DATA8 = "sync_data8";
        public static final String SYNC_DATA9 = "sync_data9";
        public static final String TITLE = "title";
    }

    public interface InstancesColumns {
        public static final String BEGIN = "begin";
        public static final String END = "end";
        public static final String END_DAY = "endDay";
        public static final String END_MINUTE = "endMinute";
        public static final String EVENT_ID = "event_id";
        public static final String START_DAY = "startDay";
        public static final String START_MINUTE = "startMinute";
    }

    public interface RemindersColumns {
        public static final String EVENT_ID = "event_id";
        public static final String METHOD = "method";
        public static final int METHOD_ALARM = 4;
        public static final int METHOD_ALERT = 1;
        public static final int METHOD_DEFAULT = 0;
        public static final int METHOD_EMAIL = 2;
        public static final int METHOD_SMS = 3;
        public static final String MINUTES = "minutes";
        public static final int MINUTES_DEFAULT = -1;
    }

    public interface SyncColumns extends CalendarSyncColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String DELETED = "deleted";
        public static final String DIRTY = "dirty";
        public static final String MUTATORS = "mutators";
        public static final String _SYNC_ID = "_sync_id";
    }
}
