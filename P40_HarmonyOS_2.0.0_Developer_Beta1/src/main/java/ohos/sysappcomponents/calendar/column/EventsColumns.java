package ohos.sysappcomponents.calendar.column;

import ohos.annotation.SystemApi;

public interface EventsColumns {
    public static final String ACC_ID = "calendar_id";
    public static final String AVAILABLE_STATUS = "availability";
    @SystemApi
    public static final String CAN_INVITE = "canInviteOthers";
    @SystemApi
    public static final String CUSTOMIZE_APP_URI = "customAppUri";
    @SystemApi
    public static final String CUSTOMIZE_BUNDLE_NAME = "customAppPackage";
    public static final String DESCRIPTION = "description";
    @SystemApi
    public static final String DISPLAY_COLOUR = "displayColor";
    public static final String DURATION = "duration";
    public static final String END_TIMEZONE = "eventEndTimezone";
    @SystemApi
    public static final String EVENT_COLOUR = "eventColor";
    @SystemApi
    public static final String EVENT_COLOUR_INDEX = "eventColor_index";
    public static final String EVENT_END_TIME = "dtend";
    public static final String EVENT_POSITION = "eventLocation";
    public static final String EVENT_START_TIME = "dtstart";
    public static final String EVENT_STATUS = "eventStatus";
    public static final int EVENT_STATUS_CANCELED = 2;
    public static final int EVENT_STATUS_CONFIRMED = 1;
    public static final int EVENT_STATUS_TENTATIVE = 0;
    @SystemApi
    public static final String EXCEPTION_DATE = "exdate";
    @SystemApi
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
    @SystemApi
    public static final String IS_LAST_SYNCED = "lastSynced";
    public static final String IS_WHOLE_DAY = "allDay";
    @SystemApi
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
}
