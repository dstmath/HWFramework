package ohos.sysappcomponents.calendar.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.LogUtil;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.exception.CalendarContractException;
import ohos.utils.net.Uri;

public abstract class TableSupport {
    private static final String ID_RAW_SELECTION = "_id=?";
    private static final String TAG = TableSupport.class.getSimpleName();
    private final Map<String, Class<?>> column2ClassMap = new HashMap();
    private final Map<String, String> column2MethodMap = new HashMap();
    private final Map<String, ColumnType> column2TypeMap = new HashMap();
    private Uri dataAbilityUri;
    private Class<? extends CalendarEntity> entityClass;

    /* access modifiers changed from: private */
    public enum ColumnType {
        TYPE_BOOLEAN,
        TYPE_SHORT,
        TYPE_INTEGER,
        TYPE_LONG,
        TYPE_FLOAT,
        TYPE_DOUBLE,
        TYPE_STRING
    }

    public abstract DataAbilityPredicates getPredicate(int i);

    public abstract Rule getRule();

    public abstract ValuesBucket getValueBucket(CalendarEntity calendarEntity);

    /* access modifiers changed from: protected */
    public abstract void initColumnMap();

    TableSupport(Class<? extends CalendarEntity> cls, Uri uri) {
        this.entityClass = cls;
        this.dataAbilityUri = uri;
        initColumnMap();
    }

    public Uri getUri() {
        return this.dataAbilityUri;
    }

    public Uri getSyncUri(String str, String str2) {
        return this.dataAbilityUri.makeBuilder().appendDecodedQueryParam("caller_is_syncadapter", "true").appendDecodedQueryParam("account_name", str).appendDecodedQueryParam("account_type", str2).build();
    }

    public DataAbilityPredicates getPredicate(CalendarEntity calendarEntity) {
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(ID_RAW_SELECTION);
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(calendarEntity.getId()));
        dataAbilityPredicates.setWhereArgs(arrayList);
        return dataAbilityPredicates;
    }

    /* access modifiers changed from: package-private */
    public void setEntityValue(CalendarEntity calendarEntity, ResultSet resultSet, String str) {
        if (!this.column2MethodMap.containsKey(str) || !this.column2TypeMap.containsKey(str) || !this.column2ClassMap.containsKey(str)) {
            LogUtil.warn(TAG, String.format(Locale.ROOT, "%s table doesn't have column %s.", this.entityClass.getSimpleName(), str));
            return;
        }
        try {
            setEntityValue(calendarEntity, this.entityClass.getMethod(this.column2MethodMap.get(str), this.column2ClassMap.get(str)), resultSet, this.column2TypeMap.get(str), resultSet.getColumnIndexForName(str));
        } catch (NoSuchMethodException unused) {
            String format = String.format(Locale.ROOT, "%s doesn't have method %s, parameter %s", this.entityClass.getName(), this.column2MethodMap.get(str), this.column2ClassMap.get(str).getName());
            LogUtil.error(TAG, format);
            throw new CalendarContractException(format);
        } catch (IllegalAccessException | InvocationTargetException unused2) {
            String format2 = String.format(Locale.ROOT, "%s invokes method %s error. (Parameter: %s)", this.entityClass.getName(), this.column2MethodMap.get(str), this.column2ClassMap.get(str).getName());
            LogUtil.error(TAG, format2);
            throw new CalendarContractException(format2);
        }
    }

    /* access modifiers changed from: package-private */
    public void initBaseColumnsMap() {
        initBaseColumnsMethodMap();
        initBaseColumnsTypeMap();
        initBaseColumnsClassMap();
    }

    /* access modifiers changed from: package-private */
    public void initSyncColumnsMap() {
        initSyncColumnsMethodMap();
        initSyncColumnsTypeMap();
        initSyncColumnsClassMap();
    }

    /* access modifiers changed from: package-private */
    public void initEventsColumnsMap() {
        initEventsColumnsMethodMap();
        initEventsColumnsTypeMap();
        initEventsColumnsClassMap();
    }

    /* access modifiers changed from: package-private */
    public void initAccountsColumnsMap() {
        initAccountsColumnsMethodMap();
        initAccountsColumnsTypeMap();
        initAccountsColumnsClassMap();
    }

    /* access modifiers changed from: package-private */
    public void initParticipantsColumnsMap() {
        initParticipantsColumnsMethodMap();
        initParticipantsColumnsTypeMap();
        initParticipantsColumnsClassMap();
    }

    /* access modifiers changed from: package-private */
    public void initRemindersColumnsMap() {
        initRemindersColumnsMethodMap();
        initRemindersColumnsTypeMap();
        initRemindersColumnsClassMap();
    }

    /* access modifiers changed from: package-private */
    public void initInstancesColumnsMap() {
        initInstancesColumnsMethodMap();
        initInstancesColumnsTypeMap();
        initInstancesColumnsClassMap();
    }

    private void setEntityValue(CalendarEntity calendarEntity, Method method, ResultSet resultSet, ColumnType columnType, int i) throws InvocationTargetException, IllegalAccessException {
        switch (columnType) {
            case TYPE_BOOLEAN:
                method.invoke(calendarEntity, Boolean.valueOf(resultSet.getInt(i) == 1));
                return;
            case TYPE_SHORT:
                method.invoke(calendarEntity, Short.valueOf(resultSet.getShort(i)));
                return;
            case TYPE_INTEGER:
                method.invoke(calendarEntity, Integer.valueOf(resultSet.getInt(i)));
                return;
            case TYPE_LONG:
                method.invoke(calendarEntity, Long.valueOf(resultSet.getLong(i)));
                return;
            case TYPE_FLOAT:
                method.invoke(calendarEntity, Float.valueOf(resultSet.getFloat(i)));
                return;
            case TYPE_DOUBLE:
                method.invoke(calendarEntity, Double.valueOf(resultSet.getDouble(i)));
                return;
            case TYPE_STRING:
                method.invoke(calendarEntity, resultSet.getString(i));
                return;
            default:
                return;
        }
    }

    private void initBaseColumnsMethodMap() {
        this.column2MethodMap.put("_id", "setId");
    }

    private void initBaseColumnsTypeMap() {
        this.column2TypeMap.put("_id", ColumnType.TYPE_INTEGER);
    }

    private void initBaseColumnsClassMap() {
        this.column2ClassMap.put("_id", Integer.TYPE);
    }

    private void initSyncColumnsMethodMap() {
        this.column2MethodMap.put("account_name", "setAccName");
        this.column2MethodMap.put("account_type", "setAccType");
        this.column2MethodMap.put("_sync_id", "setSyncId");
        this.column2MethodMap.put("dirty", "setDirty");
        this.column2MethodMap.put("mutators", "setCallingBundleName");
        this.column2MethodMap.put("deleted", "setDeleted");
    }

    private void initSyncColumnsTypeMap() {
        this.column2TypeMap.put("account_name", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("account_type", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("_sync_id", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("dirty", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("mutators", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("deleted", ColumnType.TYPE_BOOLEAN);
    }

    private void initSyncColumnsClassMap() {
        this.column2ClassMap.put("account_name", String.class);
        this.column2ClassMap.put("account_type", String.class);
        this.column2ClassMap.put("_sync_id", String.class);
        this.column2ClassMap.put("dirty", Long.TYPE);
        this.column2ClassMap.put("mutators", String.class);
        this.column2ClassMap.put("deleted", Boolean.TYPE);
    }

    private void initEventsColumnsMethodMap() {
        this.column2MethodMap.put("calendar_id", "setAccId");
        this.column2MethodMap.put("title", "setTitle");
        this.column2MethodMap.put("description", "setDescription");
        this.column2MethodMap.put("eventLocation", "setEventPosition");
        this.column2MethodMap.put("eventColor", "setEventColour");
        this.column2MethodMap.put("eventColor_index", "setEventColourIndex");
        this.column2MethodMap.put("displayColor", "setDisplayColour");
        this.column2MethodMap.put("eventStatus", "setEventStatus");
        this.column2MethodMap.put("selfAttendeeStatus", "setOwnerAttendeeStatus");
        this.column2MethodMap.put("lastSynced", "setLastSynced");
        this.column2MethodMap.put("dtstart", "setEventStartTime");
        this.column2MethodMap.put("dtend", "setEventEndTime");
        this.column2MethodMap.put("duration", "setDuration");
        this.column2MethodMap.put("eventTimezone", "setStartTimezone");
        this.column2MethodMap.put("eventEndTimezone", "setEndTimezone");
        this.column2MethodMap.put("allDay", "setWholeDay");
        this.column2MethodMap.put("accessLevel", "setPermissionLevel");
        this.column2MethodMap.put("availability", "setAvailableStatus");
        this.column2MethodMap.put("hasAlarm", "setAlarm");
        this.column2MethodMap.put("hasExtendedProperties", "setExtendedAttribute");
        this.column2MethodMap.put("rrule", "setRecurRule");
        this.column2MethodMap.put("rdate", "setRecurDate");
        this.column2MethodMap.put("exrule", "setExceptionRule");
        this.column2MethodMap.put("exdate", "setExceptionDate");
        this.column2MethodMap.put("original_id", "setInitialId");
        this.column2MethodMap.put("original_sync_id", "setInitialSyncId");
        this.column2MethodMap.put("lastDate", "setLastRecurDate");
        this.column2MethodMap.put("hasAttendeeData", "setAttendeeInfo");
        this.column2MethodMap.put("canInviteOthers", "setCanInvite");
        this.column2MethodMap.put("organizer", "setOrganizerEmail");
        this.column2MethodMap.put("customAppPackage", "setCustomizeBundleName");
        this.column2MethodMap.put("customAppUri", "setCustomizeAppUri");
    }

    private void initEventsColumnsTypeMap() {
        this.column2TypeMap.put("calendar_id", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("title", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("description", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("eventLocation", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("eventColor", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("eventColor_index", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("displayColor", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("eventStatus", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("selfAttendeeStatus", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("lastSynced", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("dtstart", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("dtend", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("duration", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("eventTimezone", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("eventEndTimezone", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("allDay", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("accessLevel", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("availability", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("hasAlarm", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("hasExtendedProperties", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("rrule", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("rdate", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("exrule", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("exdate", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("original_id", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("original_sync_id", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("lastDate", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("hasAttendeeData", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("canInviteOthers", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("organizer", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("customAppPackage", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("customAppUri", ColumnType.TYPE_STRING);
    }

    private void initEventsColumnsClassMap() {
        this.column2ClassMap.put("calendar_id", Integer.TYPE);
        this.column2ClassMap.put("title", String.class);
        this.column2ClassMap.put("description", String.class);
        this.column2ClassMap.put("eventLocation", String.class);
        this.column2ClassMap.put("eventColor", Integer.TYPE);
        this.column2ClassMap.put("eventColor_index", String.class);
        this.column2ClassMap.put("displayColor", Integer.TYPE);
        this.column2ClassMap.put("eventStatus", Integer.TYPE);
        this.column2ClassMap.put("selfAttendeeStatus", Integer.TYPE);
        this.column2ClassMap.put("lastSynced", Boolean.TYPE);
        this.column2ClassMap.put("dtstart", Long.TYPE);
        this.column2ClassMap.put("dtend", Long.TYPE);
        this.column2ClassMap.put("duration", String.class);
        this.column2ClassMap.put("eventTimezone", String.class);
        this.column2ClassMap.put("eventEndTimezone", String.class);
        this.column2ClassMap.put("allDay", Boolean.TYPE);
        this.column2ClassMap.put("accessLevel", Integer.TYPE);
        this.column2ClassMap.put("availability", Integer.TYPE);
        this.column2ClassMap.put("hasAlarm", Boolean.TYPE);
        this.column2ClassMap.put("hasExtendedProperties", Boolean.TYPE);
        this.column2ClassMap.put("rrule", String.class);
        this.column2ClassMap.put("rdate", String.class);
        this.column2ClassMap.put("exrule", String.class);
        this.column2ClassMap.put("exdate", String.class);
        this.column2ClassMap.put("original_id", String.class);
        this.column2ClassMap.put("original_sync_id", String.class);
        this.column2ClassMap.put("lastDate", Long.TYPE);
        this.column2ClassMap.put("hasAttendeeData", Boolean.TYPE);
        this.column2ClassMap.put("canInviteOthers", Boolean.TYPE);
        this.column2ClassMap.put("organizer", String.class);
        this.column2ClassMap.put("customAppPackage", String.class);
        this.column2ClassMap.put("customAppUri", String.class);
    }

    private void initAccountsColumnsMethodMap() {
        this.column2MethodMap.put("calendar_color", "setAccColour");
        this.column2MethodMap.put("calendar_color_index", "setAccColourIndex");
        this.column2MethodMap.put("calendar_displayName", "setAccDisplayName");
        this.column2MethodMap.put("calendar_access_level", "setAccPermissionLevel");
        this.column2MethodMap.put("visible", "setVisible");
        this.column2MethodMap.put("calendar_timezone", "setAccTimezone");
        this.column2MethodMap.put("sync_events", "setSyncEvents");
        this.column2MethodMap.put("ownerAccount", "setMasterAccount");
        this.column2MethodMap.put("canOrganizerRespond", "setOrganizerResponse");
        this.column2MethodMap.put("canModifyTimeZone", "setModifyTimeZone");
        this.column2MethodMap.put("maxReminders", "setMaxReminders");
        this.column2MethodMap.put("allowedReminders", "setRemindersType");
        this.column2MethodMap.put("allowedAvailability", "setAvailabilityStatus");
        this.column2MethodMap.put("allowedAttendeeTypes", "setAttendeeTypes");
        this.column2MethodMap.put("isPrimary", "setPrimary");
    }

    private void initAccountsColumnsTypeMap() {
        this.column2TypeMap.put("calendar_color", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("calendar_color_index", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("calendar_displayName", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("calendar_access_level", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("visible", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("calendar_timezone", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("sync_events", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("ownerAccount", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("canOrganizerRespond", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("canModifyTimeZone", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("maxReminders", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("allowedReminders", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("allowedAvailability", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("allowedAttendeeTypes", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("isPrimary", ColumnType.TYPE_BOOLEAN);
    }

    private void initAccountsColumnsClassMap() {
        this.column2ClassMap.put("calendar_color", Integer.TYPE);
        this.column2ClassMap.put("calendar_color_index", String.class);
        this.column2ClassMap.put("calendar_displayName", String.class);
        this.column2ClassMap.put("calendar_access_level", String.class);
        this.column2ClassMap.put("visible", Boolean.TYPE);
        this.column2ClassMap.put("calendar_timezone", String.class);
        this.column2ClassMap.put("sync_events", Boolean.TYPE);
        this.column2ClassMap.put("ownerAccount", String.class);
        this.column2ClassMap.put("canOrganizerRespond", Boolean.TYPE);
        this.column2ClassMap.put("canModifyTimeZone", Boolean.TYPE);
        this.column2ClassMap.put("maxReminders", Integer.TYPE);
        this.column2ClassMap.put("allowedReminders", String.class);
        this.column2ClassMap.put("allowedAvailability", String.class);
        this.column2ClassMap.put("allowedAttendeeTypes", String.class);
        this.column2ClassMap.put("isPrimary", Boolean.TYPE);
    }

    private void initParticipantsColumnsMethodMap() {
        this.column2MethodMap.put("event_id", "setEventId");
        this.column2MethodMap.put("attendeeName", "setParticipantName");
        this.column2MethodMap.put("attendeeEmail", "setParticipantEmail");
        this.column2MethodMap.put("attendeeRelationship", "setParticipantRoleType");
        this.column2MethodMap.put("attendeeType", "setParticipantType");
        this.column2MethodMap.put("attendeeStatus", "setParticipantStatus");
        this.column2MethodMap.put("attendeeIdentity", "setParticipantIdentity");
        this.column2MethodMap.put("attendeeIdNamespace", "setParticipantIdNamespace");
        this.column2MethodMap.put("deleted", "setDeleted");
        this.column2MethodMap.put("_sync_id", "setSyncId");
    }

    private void initParticipantsColumnsTypeMap() {
        this.column2TypeMap.put("event_id", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("attendeeName", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("attendeeEmail", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("attendeeRelationship", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("attendeeType", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("attendeeStatus", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("attendeeIdentity", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("attendeeIdNamespace", ColumnType.TYPE_STRING);
        this.column2TypeMap.put("deleted", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("_sync_id", ColumnType.TYPE_STRING);
    }

    private void initParticipantsColumnsClassMap() {
        this.column2ClassMap.put("event_id", Integer.TYPE);
        this.column2ClassMap.put("attendeeName", String.class);
        this.column2ClassMap.put("attendeeEmail", String.class);
        this.column2ClassMap.put("attendeeRelationship", Integer.TYPE);
        this.column2ClassMap.put("attendeeType", Integer.TYPE);
        this.column2ClassMap.put("attendeeStatus", Integer.TYPE);
        this.column2ClassMap.put("attendeeIdentity", String.class);
        this.column2ClassMap.put("attendeeIdNamespace", String.class);
        this.column2ClassMap.put("deleted", Boolean.TYPE);
        this.column2ClassMap.put("_sync_id", String.class);
    }

    private void initRemindersColumnsMethodMap() {
        this.column2MethodMap.put("event_id", "setEventId");
        this.column2MethodMap.put("minutes", "setRemindMinutes");
        this.column2MethodMap.put("method", "setRemindType");
        this.column2MethodMap.put("deleted", "setDeleted");
        this.column2MethodMap.put("_sync_id", "setSyncId");
    }

    private void initRemindersColumnsTypeMap() {
        this.column2TypeMap.put("event_id", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("minutes", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("method", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("deleted", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("_sync_id", ColumnType.TYPE_STRING);
    }

    private void initRemindersColumnsClassMap() {
        this.column2ClassMap.put("event_id", Integer.TYPE);
        this.column2ClassMap.put("minutes", Integer.TYPE);
        this.column2ClassMap.put("method", Integer.TYPE);
        this.column2ClassMap.put("deleted", Boolean.TYPE);
        this.column2ClassMap.put("_sync_id", String.class);
    }

    private void initInstancesColumnsMethodMap() {
        this.column2MethodMap.put("deleted", "setDeleted");
        this.column2MethodMap.put("begin", "setInstanceBegin");
        this.column2MethodMap.put("end", "setInstanceEnd");
        this.column2MethodMap.put("event_id", "setEventId");
        this.column2MethodMap.put("startDay", "setInstanceStartDay");
        this.column2MethodMap.put("endDay", "setInstanceEndDay");
        this.column2MethodMap.put("startMinute", "setInstanceStartMinute");
        this.column2MethodMap.put("endMinute", "setInstanceEndMinute");
    }

    private void initInstancesColumnsTypeMap() {
        this.column2TypeMap.put("deleted", ColumnType.TYPE_BOOLEAN);
        this.column2TypeMap.put("begin", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("end", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("event_id", ColumnType.TYPE_INTEGER);
        this.column2TypeMap.put("startDay", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("endDay", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("startMinute", ColumnType.TYPE_LONG);
        this.column2TypeMap.put("endMinute", ColumnType.TYPE_LONG);
    }

    private void initInstancesColumnsClassMap() {
        this.column2ClassMap.put("deleted", Boolean.TYPE);
        this.column2ClassMap.put("begin", Long.TYPE);
        this.column2ClassMap.put("end", Long.TYPE);
        this.column2ClassMap.put("event_id", Integer.TYPE);
        this.column2ClassMap.put("startDay", Long.TYPE);
        this.column2ClassMap.put("endDay", Long.TYPE);
        this.column2ClassMap.put("startMinute", Long.TYPE);
        this.column2ClassMap.put("endMinute", Long.TYPE);
    }
}
