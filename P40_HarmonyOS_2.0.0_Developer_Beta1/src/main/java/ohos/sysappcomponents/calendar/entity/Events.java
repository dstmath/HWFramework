package ohos.sysappcomponents.calendar.entity;

import ohos.annotation.SystemApi;
import ohos.sysappcomponents.calendar.column.BaseColumns;
import ohos.sysappcomponents.calendar.column.EventsColumns;
import ohos.sysappcomponents.calendar.column.SyncColumns;

public class Events extends CalendarEntity implements BaseColumns, SyncColumns, EventsColumns {
    private int accId;
    private int availableStatus;
    @SystemApi
    private String callingBundleName;
    @SystemApi
    private boolean canInvite;
    @SystemApi
    private String customizeAppUri;
    @SystemApi
    private String customizeBundleName;
    @SystemApi
    private boolean deleted;
    private String description;
    @SystemApi
    private long dirty;
    @SystemApi
    private int displayColour;
    private String duration;
    @SystemApi
    private String endTimezone;
    @SystemApi
    private int eventColour;
    @SystemApi
    private String eventColourIndex;
    private long eventEndTime;
    private String eventPosition;
    private long eventStartTime;
    private int eventStatus;
    @SystemApi
    private String exceptionDate;
    @SystemApi
    private String exceptionRule;
    private boolean hasAlarm;
    private boolean hasAttendeeInfo;
    @SystemApi
    private boolean hasExtendedAttribute;
    private String initialId;
    @SystemApi
    private String initialSyncId;
    @SystemApi
    private boolean isLastSynced;
    private boolean isWholeDay;
    @SystemApi
    private long lastRecurDate;
    @SystemApi
    private String organizerEmail;
    @SystemApi
    private int ownerAttendeeStatus;
    @SystemApi
    private int permissionLevel;
    private String recurDate;
    private String recurRule;
    @SystemApi
    private String startTimezone;
    private String syncId;
    private String title;

    public String getSyncId() {
        return this.syncId;
    }

    public void setSyncId(String str) {
        this.syncId = str;
    }

    @SystemApi
    public long getDirty() {
        return this.dirty;
    }

    @SystemApi
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

    public int getAccId() {
        return this.accId;
    }

    public void setAccId(int i) {
        this.accId = i;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public String getEventPosition() {
        return this.eventPosition;
    }

    public void setEventPosition(String str) {
        this.eventPosition = str;
    }

    public int getEventColour() {
        return this.eventColour;
    }

    public void setEventColour(int i) {
        this.eventColour = i;
    }

    public String getEventColourIndex() {
        return this.eventColourIndex;
    }

    public void setEventColourIndex(String str) {
        this.eventColourIndex = str;
    }

    @SystemApi
    public int getDisplayColour() {
        return this.displayColour;
    }

    @SystemApi
    public void setDisplayColour(int i) {
        this.displayColour = i;
    }

    public int getEventStatus() {
        return this.eventStatus;
    }

    public void setEventStatus(int i) {
        this.eventStatus = i;
    }

    @SystemApi
    public int getOwnerAttendeeStatus() {
        return this.ownerAttendeeStatus;
    }

    @SystemApi
    public void setOwnerAttendeeStatus(int i) {
        this.ownerAttendeeStatus = i;
    }

    @SystemApi
    public boolean isLastSynced() {
        return this.isLastSynced;
    }

    @SystemApi
    public void setLastSynced(boolean z) {
        this.isLastSynced = z;
    }

    public long getEventStartTime() {
        return this.eventStartTime;
    }

    public void setEventStartTime(long j) {
        this.eventStartTime = j;
    }

    public long getEventEndTime() {
        return this.eventEndTime;
    }

    public void setEventEndTime(long j) {
        this.eventEndTime = j;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String str) {
        this.duration = str;
    }

    public String getStartTimezone() {
        return this.startTimezone;
    }

    public void setStartTimezone(String str) {
        this.startTimezone = str;
    }

    public String getEndTimezone() {
        return this.endTimezone;
    }

    public void setEndTimezone(String str) {
        this.endTimezone = str;
    }

    public boolean isWholeDay() {
        return this.isWholeDay;
    }

    public void setWholeDay(boolean z) {
        this.isWholeDay = z;
    }

    @SystemApi
    public int getPermissionLevel() {
        return this.permissionLevel;
    }

    @SystemApi
    public void setPermissionLevel(int i) {
        this.permissionLevel = i;
    }

    public int getAvailableStatus() {
        return this.availableStatus;
    }

    public void setAvailableStatus(int i) {
        this.availableStatus = i;
    }

    public boolean hasAlarm() {
        return this.hasAlarm;
    }

    public void setAlarm(boolean z) {
        this.hasAlarm = z;
    }

    @SystemApi
    public boolean hasExtendedAttribute() {
        return this.hasExtendedAttribute;
    }

    @SystemApi
    public void setExtendedAttribute(boolean z) {
        this.hasExtendedAttribute = z;
    }

    public String getRecurRule() {
        return this.recurRule;
    }

    public void setRecurRule(String str) {
        this.recurRule = str;
    }

    public String getRecurDate() {
        return this.recurDate;
    }

    public void setRecurDate(String str) {
        this.recurDate = str;
    }

    @SystemApi
    public String getExceptionRule() {
        return this.exceptionRule;
    }

    @SystemApi
    public void setExceptionRule(String str) {
        this.exceptionRule = str;
    }

    @SystemApi
    public String getExceptionDate() {
        return this.exceptionDate;
    }

    @SystemApi
    public void setExceptionDate(String str) {
        this.exceptionDate = str;
    }

    public String getInitialId() {
        return this.initialId;
    }

    public void setInitialId(String str) {
        this.initialId = str;
    }

    @SystemApi
    public String getInitialSyncId() {
        return this.initialSyncId;
    }

    @SystemApi
    public void setInitialSyncId(String str) {
        this.initialSyncId = str;
    }

    @SystemApi
    public long getLastRecurDate() {
        return this.lastRecurDate;
    }

    @SystemApi
    public void setLastRecurDate(long j) {
        this.lastRecurDate = j;
    }

    public boolean hasAttendeeInfo() {
        return this.hasAttendeeInfo;
    }

    public void setAttendeeInfo(boolean z) {
        this.hasAttendeeInfo = z;
    }

    @SystemApi
    public String getOrganizerEmail() {
        return this.organizerEmail;
    }

    @SystemApi
    public void setOrganizerEmail(String str) {
        this.organizerEmail = str;
    }

    @SystemApi
    public boolean isCanInvite() {
        return this.canInvite;
    }

    @SystemApi
    public void setCanInvite(boolean z) {
        this.canInvite = z;
    }

    @SystemApi
    public String getCustomizeBundleName() {
        return this.customizeBundleName;
    }

    @SystemApi
    public void setCustomizeBundleName(String str) {
        this.customizeBundleName = str;
    }

    @SystemApi
    public String getCustomizeAppUri() {
        return this.customizeAppUri;
    }

    @SystemApi
    public void setCustomizeAppUri(String str) {
        this.customizeAppUri = str;
    }
}
