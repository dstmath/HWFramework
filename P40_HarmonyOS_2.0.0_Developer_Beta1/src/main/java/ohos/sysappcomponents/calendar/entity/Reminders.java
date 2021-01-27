package ohos.sysappcomponents.calendar.entity;

import ohos.annotation.SystemApi;
import ohos.sysappcomponents.calendar.column.BaseColumns;
import ohos.sysappcomponents.calendar.column.EventsColumns;
import ohos.sysappcomponents.calendar.column.RemindersColumns;

public class Reminders extends CalendarEntity implements BaseColumns, RemindersColumns, EventsColumns {
    @SystemApi
    private boolean deleted;
    private int eventId;
    private int remindMinutes;
    private int remindType;
    private String syncId;

    public int getEventId() {
        return this.eventId;
    }

    public void setEventId(int i) {
        this.eventId = i;
    }

    public int getRemindMinutes() {
        return this.remindMinutes;
    }

    public void setRemindMinutes(int i) {
        this.remindMinutes = i;
    }

    public int getRemindType() {
        return this.remindType;
    }

    public void setRemindType(int i) {
        this.remindType = i;
    }

    @SystemApi
    public boolean isDeleted() {
        return this.deleted;
    }

    @SystemApi
    public void setDeleted(boolean z) {
        this.deleted = z;
    }

    public String getSyncId() {
        return this.syncId;
    }

    public void setSyncId(String str) {
        this.syncId = str;
    }
}
