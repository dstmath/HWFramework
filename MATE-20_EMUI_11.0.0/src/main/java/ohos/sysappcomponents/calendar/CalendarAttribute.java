package ohos.sysappcomponents.calendar;

import ohos.annotation.SystemApi;
import ohos.utils.net.Uri;

@SystemApi
public class CalendarAttribute {

    private interface AccountColumns {
        public static final String ACCOUNT_ACCESS_LEVEL = "calendar_access_level";
        public static final String ACCOUNT_COLOR = "calendar_color";
        public static final String ACCOUNT_COLOR_KEY = "calendar_color_index";
        public static final String ACCOUNT_DISPLAY_NAME = "calendar_displayName";
        public static final String ACCOUNT_TIME_ZONE = "calendar_timezone";
        public static final int ACC_ACCESS_CONTRIBUTOR = 500;
        public static final int ACC_ACCESS_EDITOR = 600;
        public static final int ACC_ACCESS_FREEBUSY = 100;
        public static final int ACC_ACCESS_NONE = 0;
        public static final int ACC_ACCESS_OVERRIDE = 400;
        public static final int ACC_ACCESS_OWNER = 700;
        public static final int ACC_ACCESS_READ = 200;
        public static final int ACC_ACCESS_RESPOND = 300;
        public static final int ACC_ACCESS_ROOT = 800;
        public static final String ATTENDEE_TYPES = "allowedAttendeeTypes";
        public static final String AVAILABILITY_STATUS = "allowedAvailability";
        public static final String IS_PRIMARY = "isPrimary";
        public static final String MASTER_ACCOUNT = "ownerAccount";
        public static final String MAX_REMINDERS = "maxReminders";
        public static final String MODIFY_TIME_ZONE = "canModifyTimeZone";
        public static final String ORGANIZER_RESPOND = "canOrganizerRespond";
        public static final String REMINDERS_TYPE = "allowedReminders";
        public static final String SYNC_EVENTS = "sync_events";
        public static final String VISIBLE = "visible";
    }

    private interface AccountSyncColumns {
        public static final String ACC_SYNC1 = "cal_sync1";
        public static final String ACC_SYNC10 = "cal_sync10";
        public static final String ACC_SYNC2 = "cal_sync2";
        public static final String ACC_SYNC3 = "cal_sync3";
        public static final String ACC_SYNC4 = "cal_sync4";
        public static final String ACC_SYNC5 = "cal_sync5";
        public static final String ACC_SYNC6 = "cal_sync6";
        public static final String ACC_SYNC7 = "cal_sync7";
        public static final String ACC_SYNC8 = "cal_sync8";
        public static final String ACC_SYNC9 = "cal_sync9";
    }

    private interface BaseColumns {
        public static final String COUNT = "_count";
        public static final String ID = "_id";
    }

    private interface EventsColumns {
        public static final String ACC_ID = "calendar_id";
        public static final String AVAILABLE_STATUS = "availability";
        public static final String CAN_INVITE = "canInviteOthers";
        public static final String CUSTOMIZE_APP_URI = "customAppUri";
        public static final String CUSTOMIZE_PACKAGE_NAME = "customAppPackage";
        public static final String DESCRIPTION = "description";
        public static final String DISPLAY_COLOUR = "displayColor";
        public static final String DURATION = "duration";
        public static final String END_TIMEZONE = "eventEndTimezone";
        public static final String EVENT_COLOUR = "eventColor";
        public static final String EVENT_COLOUR_INDEX = "eventColor_index";
        public static final String EVENT_END_TIME = "dtend";
        public static final String EVENT_POSITION = "eventLocation";
        public static final String EVENT_START_TIME = "dtstart";
        public static final String EVENT_STATUS = "eventStatus";
        public static final int EVENT_STATUS_CANCELED = 2;
        public static final int EVENT_STATUS_CONFIRMED = 1;
        public static final int EVENT_STATUS_TENTATIVE = 0;
        public static final String EXCEPTION_DATE = "exdate";
        public static final String EXCEPTION_RULE = "exrule";
        public static final String EXTEND_DATA1 = "sync_data1";
        public static final String EXTEND_DATA10 = "sync_data10";
        public static final String EXTEND_DATA2 = "sync_data2";
        public static final String EXTEND_DATA3 = "sync_data3";
        public static final String EXTEND_DATA4 = "sync_data4";
        public static final String EXTEND_DATA5 = "sync_data5";
        public static final String EXTEND_DATA6 = "sync_data6";
        public static final String EXTEND_DATA7 = "sync_data7";
        public static final String EXTEND_DATA8 = "sync_data8";
        public static final String EXTEND_DATA9 = "sync_data9";
        public static final String HAS_ALARM = "hasAlarm";
        public static final String HAS_ATTENDEE_INFO = "hasAttendeeData";
        public static final String HAS_EXTENDED_ATTRIBUTES = "hasExtendedProperties";
        public static final String INITIAL_ID = "original_id";
        public static final String INITIAL_SYNC_ID = "original_sync_id";
        public static final String IS_LAST_SYNCED = "lastSynced";
        public static final String LAST_RECUR_DATE = "lastDate";
        public static final String ORGANIZER_EMAIL = "organizer";
        public static final String OWNER_ATTENDEE_STATUS = "selfAttendeeStatus";
        public static final int PERMISSION_CONFIDENTIAL = 1;
        public static final int PERMISSION_DEFAULT = 0;
        public static final String PERMISSION_LEVEL = "accessLevel";
        public static final int PERMISSION_PRIVATE = 2;
        public static final int PERMISSION_PUBLIC = 3;
        public static final String RECUR_DATE = "rdate";
        public static final String RECUR_RULE = "rrule";
        public static final String START_TIMEZONE = "eventTimezone";
        public static final int STATUS_BUSY = 0;
        public static final int STATUS_FREE = 1;
        public static final int STATUS_TENTATIVE = 2;
        public static final String TITLE = "title";
        public static final String WHOLE_DAY = "allDay";
    }

    private interface ParticipantsColumns {
        public static final String EVENT_ID = "event_id";
        public static final String PARTICIPANT_EMAIL = "attendeeEmail";
        public static final String PARTICIPANT_IDENTITY = "attendeeIdentity";
        public static final String PARTICIPANT_ID_NAMESPACE = "attendeeIdNamespace";
        public static final String PARTICIPANT_NAME = "attendeeName";
        public static final String PARTICIPANT_ROLE_TYPE = "attendeeRelationship";
        public static final String PARTICIPANT_STATUS = "attendeeStatus";
        public static final int PARTICIPANT_STATUS_ACCEPTED = 1;
        public static final int PARTICIPANT_STATUS_DECLINED = 2;
        public static final int PARTICIPANT_STATUS_INVITED = 3;
        public static final int PARTICIPANT_STATUS_NONE = 0;
        public static final int PARTICIPANT_STATUS_TENTATIVE = 4;
        public static final String PARTICIPANT_TYPE = "attendeeType";
        public static final int ROLE_ATTENDEE = 1;
        public static final int ROLE_NONE = 0;
        public static final int ROLE_ORGANIZER = 2;
        public static final int ROLE_PERFORMER = 3;
        public static final int ROLE_SPEAKER = 4;
        public static final int TYPE_NONE = 0;
        public static final int TYPE_OPTIONAL = 2;
        public static final int TYPE_REQUIRED = 1;
        public static final int TYPE_RESOURCE = 3;
    }

    private interface RemindersColumns {
        public static final String EVENT_ID = "event_id";
        public static final String REMIND_MINUTES = "minutes";
        public static final int REMIND_MINUTES_DEFAULT = -1;
        public static final String REMIND_TYPE = "method";
        public static final int TYPE_ALARM = 4;
        public static final int TYPE_ALERT = 1;
        public static final int TYPE_DEFAULT = 0;
        public static final int TYPE_EMAIL = 2;
        public static final int TYPE_SMS = 3;
    }

    private interface SyncColumns extends AccountSyncColumns {
        public static final String ACC_NAME = "account_name";
        public static final String ACC_TYPE = "account_type";
        public static final String CALLING_PACKAGE_NAME = "mutators";
        public static final String DELETED = "deleted";
        public static final String DIRTY = "dirty";
        public static final String SYNC_ID = "_sync_id";
    }

    public static final class Accounts implements BaseColumns, SyncColumns, AccountColumns {
        public static final Uri DATA_ABILITY_URI = Attribute.CALENDARS_URI;

        private Accounts() {
        }
    }

    public static final class Participants implements BaseColumns, ParticipantsColumns, EventsColumns {
        public static final Uri DATA_ABILITY_URI = Attribute.ATTENDEES_URI;

        private Participants() {
        }
    }

    public static final class Events implements BaseColumns, SyncColumns, EventsColumns {
        public static final Uri DATA_ABILITY_URI = Attribute.EVENTS_URI;

        private Events() {
        }
    }

    public static final class Instances implements BaseColumns, EventsColumns, AccountColumns {
        public static final Uri DATA_ABILITY_URI = Attribute.INSTANCES_URI;

        private Instances() {
        }
    }

    public static final class Reminders implements BaseColumns, RemindersColumns, EventsColumns {
        public static final Uri DATA_ABILITY_URI = Attribute.REMINDERS_URI;

        private Reminders() {
        }
    }
}
