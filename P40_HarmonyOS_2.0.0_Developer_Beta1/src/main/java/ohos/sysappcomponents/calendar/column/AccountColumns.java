package ohos.sysappcomponents.calendar.column;

import ohos.annotation.SystemApi;

public interface AccountColumns {
    public static final String ACC_COLOR = "calendar_color";
    public static final String ACC_COLOR_INDEX = "calendar_color_index";
    public static final String ACC_DISPLAY_NAME = "calendar_displayName";
    @SystemApi
    public static final int ACC_PERMISSION_CONTRIBUTOR = 500;
    @SystemApi
    public static final int ACC_PERMISSION_EDITOR = 600;
    @SystemApi
    public static final int ACC_PERMISSION_FREEBUSY = 100;
    @SystemApi
    public static final String ACC_PERMISSION_LEVEL = "calendar_access_level";
    @SystemApi
    public static final int ACC_PERMISSION_NONE = 0;
    @SystemApi
    public static final int ACC_PERMISSION_OVERRIDE = 400;
    @SystemApi
    public static final int ACC_PERMISSION_OWNER = 700;
    @SystemApi
    public static final int ACC_PERMISSION_READ = 200;
    @SystemApi
    public static final int ACC_PERMISSION_RESPOND = 300;
    @SystemApi
    public static final int ACC_PERMISSION_ROOT = 800;
    public static final String ACC_TIME_ZONE = "calendar_timezone";
    public static final String ATTENDEE_TYPES = "allowedAttendeeTypes";
    public static final String AVAILABILITY_STATUS = "allowedAvailability";
    public static final String IS_PRIMARY = "isPrimary";
    public static final String IS_SYNC_EVENTS = "sync_events";
    @SystemApi
    public static final String MASTER_ACCOUNT = "ownerAccount";
    @SystemApi
    public static final String MAX_REMINDERS = "maxReminders";
    @SystemApi
    public static final String MODIFY_TIME_ZONE = "canModifyTimeZone";
    @SystemApi
    public static final String ORGANIZER_RESPOND = "canOrganizerRespond";
    public static final String REMINDERS_TYPE = "allowedReminders";
    public static final String VISIBLE = "visible";
}
