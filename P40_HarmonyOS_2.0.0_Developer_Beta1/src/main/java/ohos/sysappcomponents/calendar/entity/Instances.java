package ohos.sysappcomponents.calendar.entity;

import ohos.annotation.SystemApi;
import ohos.sysappcomponents.calendar.column.AccountColumns;
import ohos.sysappcomponents.calendar.column.BaseColumns;
import ohos.sysappcomponents.calendar.column.EventsColumns;
import ohos.sysappcomponents.calendar.column.InstancesColumns;

public class Instances extends CalendarEntity implements BaseColumns, EventsColumns, AccountColumns, InstancesColumns {
    @SystemApi
    private boolean deleted;
    private int eventId;
    private long instanceBegin;
    private long instanceEnd;
    private long instanceEndDay;
    private long instanceEndMinute;
    private long instanceStartDay;
    private long instanceStartMinute;

    @SystemApi
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean z) {
        this.deleted = z;
    }

    public long getInstanceBegin() {
        return this.instanceBegin;
    }

    public void setInstanceBegin(long j) {
        this.instanceBegin = j;
    }

    public long getInstanceEnd() {
        return this.instanceEnd;
    }

    public void setInstanceEnd(long j) {
        this.instanceEnd = j;
    }

    public int getEventId() {
        return this.eventId;
    }

    public void setEventId(int i) {
        this.eventId = i;
    }

    public long getInstanceStartDay() {
        return this.instanceStartDay;
    }

    public void setInstanceStartDay(long j) {
        this.instanceStartDay = j;
    }

    public long getInstanceEndDay() {
        return this.instanceEndDay;
    }

    public void setInstanceEndDay(long j) {
        this.instanceEndDay = j;
    }

    public long getInstanceStartMinute() {
        return this.instanceStartMinute;
    }

    public void setInstanceStartMinute(long j) {
        this.instanceStartMinute = j;
    }

    public long getInstanceEndMinute() {
        return this.instanceEndMinute;
    }

    public void setInstanceEndMinute(long j) {
        this.instanceEndMinute = j;
    }
}
