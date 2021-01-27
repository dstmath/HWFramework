package ohos.sysappcomponents.calendar.entity;

public abstract class CalendarEntity {
    public static final String ACC_TYPE_LOCAL = "LOCAL";
    public static final String IS_SYNC_ADAPTER = "caller_is_syncadapter";
    private int id;

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }
}
