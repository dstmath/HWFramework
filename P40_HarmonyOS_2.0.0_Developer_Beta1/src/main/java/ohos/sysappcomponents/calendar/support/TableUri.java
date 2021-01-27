package ohos.sysappcomponents.calendar.support;

import ohos.sysappcomponents.calendar.Attribute;
import ohos.utils.net.Uri;

interface TableUri {
    public static final Uri ACCOUNT_DATA_ABILITY_URI = Attribute.CALENDARS_URI;
    public static final Uri EVENT_DATA_ABILITY_URI = Attribute.EVENTS_URI;
    public static final Uri INSTANCE_DATA_ABILITY_URI = Attribute.INSTANCES_URI;
    public static final Uri PARTICIPANT_DATA_ABILITY_URI = Attribute.ATTENDEES_URI;
    public static final Uri REMINDER_DATA_ABILITY_URI = Attribute.REMINDERS_URI;
}
