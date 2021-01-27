package ohos.sysappcomponents.calendar.entity;

import ohos.annotation.SystemApi;
import ohos.sysappcomponents.calendar.column.AccountColumns;
import ohos.sysappcomponents.calendar.column.BaseColumns;
import ohos.sysappcomponents.calendar.column.SyncColumns;

public class Accounts extends CalendarEntity implements BaseColumns, SyncColumns, AccountColumns {
    public static final String NAME = "name";
    private int accColour;
    @SystemApi
    private String accColourIndex;
    @SystemApi
    private String accDisplayName;
    private String accName;
    @SystemApi
    private String accPermissionLevel;
    private String accTimezone;
    private String accType;
    private String attendeeTypes;
    @SystemApi
    private String availabilityStatus;
    @SystemApi
    private String callingBundleName;
    @SystemApi
    private boolean deleted;
    @SystemApi
    private long dirty;
    @SystemApi
    private boolean isPrimary;
    private boolean isSyncEvents;
    @SystemApi
    private String masterAccount;
    @SystemApi
    private int maxReminders;
    @SystemApi
    private boolean modifyTimeZone;
    private String name;
    @SystemApi
    private boolean organizerResponse;
    private String remindersType;
    @SystemApi
    private String syncId;
    private boolean visible;

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getAccName() {
        return this.accName;
    }

    public void setAccName(String str) {
        this.accName = str;
    }

    public String getAccType() {
        return this.accType;
    }

    public void setAccType(String str) {
        this.accType = str;
    }

    @SystemApi
    public String getSyncId() {
        return this.syncId;
    }

    @SystemApi
    public void setSyncId(String str) {
        this.syncId = str;
    }

    @SystemApi
    public long getDirty() {
        return this.dirty;
    }

    public void setDirty(long j) {
        this.dirty = j;
    }

    @SystemApi
    public String getCallingBundleName() {
        return this.callingBundleName;
    }

    @SystemApi
    public void setCallingBundleName(String str) {
        this.callingBundleName = str;
    }

    @SystemApi
    public boolean isDeleted() {
        return this.deleted;
    }

    @SystemApi
    public void setDeleted(boolean z) {
        this.deleted = z;
    }

    public int getAccColour() {
        return this.accColour;
    }

    public void setAccColour(int i) {
        this.accColour = i;
    }

    @SystemApi
    public String getAccColourIndex() {
        return this.accColourIndex;
    }

    @SystemApi
    public void setAccColourIndex(String str) {
        this.accColourIndex = str;
    }

    public String getAccDisplayName() {
        return this.accDisplayName;
    }

    public void setAccDisplayName(String str) {
        this.accDisplayName = str;
    }

    @SystemApi
    public String getAccPermissionLevel() {
        return this.accPermissionLevel;
    }

    @SystemApi
    public void setAccPermissionLevel(String str) {
        this.accPermissionLevel = str;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean z) {
        this.visible = z;
    }

    public String getAccTimezone() {
        return this.accTimezone;
    }

    public void setAccTimezone(String str) {
        this.accTimezone = str;
    }

    public boolean isSyncEvents() {
        return this.isSyncEvents;
    }

    public void setSyncEvents(boolean z) {
        this.isSyncEvents = z;
    }

    @SystemApi
    public String getMasterAccount() {
        return this.masterAccount;
    }

    @SystemApi
    public void setMasterAccount(String str) {
        this.masterAccount = str;
    }

    @SystemApi
    public boolean isOrganizerResponse() {
        return this.organizerResponse;
    }

    @SystemApi
    public void setOrganizerResponse(boolean z) {
        this.organizerResponse = z;
    }

    @SystemApi
    public boolean isModifyTimeZone() {
        return this.modifyTimeZone;
    }

    @SystemApi
    public void setModifyTimeZone(boolean z) {
        this.modifyTimeZone = z;
    }

    @SystemApi
    public int getMaxReminders() {
        return this.maxReminders;
    }

    @SystemApi
    public void setMaxReminders(int i) {
        this.maxReminders = i;
    }

    public String getRemindersType() {
        return this.remindersType;
    }

    public void setRemindersType(String str) {
        this.remindersType = str;
    }

    @SystemApi
    public String getAvailabilityStatus() {
        return this.availabilityStatus;
    }

    @SystemApi
    public void setAvailabilityStatus(String str) {
        this.availabilityStatus = str;
    }

    public String getAttendeeTypes() {
        return this.attendeeTypes;
    }

    public void setAttendeeTypes(String str) {
        this.attendeeTypes = str;
    }

    @SystemApi
    public boolean isPrimary() {
        return this.isPrimary;
    }

    @SystemApi
    public void setPrimary(boolean z) {
        this.isPrimary = z;
    }
}
