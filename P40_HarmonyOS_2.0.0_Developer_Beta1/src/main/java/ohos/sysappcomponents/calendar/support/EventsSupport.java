package ohos.sysappcomponents.calendar.support;

import java.util.ArrayList;
import java.util.Optional;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.LogUtil;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.entity.Events;
import ohos.utils.net.Uri;

public class EventsSupport extends TableSupport {
    private static final String ACC_ID_RAW_SELECTION = "calendar_id=?";
    private static final String TAG = EventsSupport.class.getSimpleName();

    private EventsSupport(Class<? extends CalendarEntity> cls, Uri uri) {
        super(cls, uri);
    }

    static EventsSupport getInstance() {
        return InnerSingleton.INSTANCE;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public Rule getRule() {
        return new Rule() {
            /* class ohos.sysappcomponents.calendar.support.$$Lambda$EventsSupport$ChZWh6Qxy2s69d_82QZAcHbnPYM */

            @Override // ohos.sysappcomponents.calendar.support.Rule
            public final Optional getEntity(ResultSet resultSet, String[] strArr) {
                return EventsSupport.this.lambda$getRule$0$EventsSupport(resultSet, strArr);
            }
        };
    }

    public /* synthetic */ Optional lambda$getRule$0$EventsSupport(ResultSet resultSet, String[] strArr) {
        Events events = new Events();
        for (String str : strArr) {
            setEntityValue(events, resultSet, str);
        }
        return Optional.of(events);
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public ValuesBucket getValueBucket(CalendarEntity calendarEntity) {
        ValuesBucket valuesBucket = new ValuesBucket();
        if (calendarEntity == null) {
            return valuesBucket;
        }
        if (calendarEntity instanceof Events) {
            setValuesBucket(valuesBucket, (Events) calendarEntity);
        } else {
            LogUtil.warn(TAG, "Invalid input. Input must be events entity objects.");
        }
        return valuesBucket;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public DataAbilityPredicates getPredicate(int i) {
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(ACC_ID_RAW_SELECTION);
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(i));
        dataAbilityPredicates.setWhereArgs(arrayList);
        return dataAbilityPredicates;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public void initColumnMap() {
        initBaseColumnsMap();
        initSyncColumnsMap();
        initEventsColumnsMap();
    }

    /* access modifiers changed from: private */
    public static class InnerSingleton {
        private static final EventsSupport INSTANCE = new EventsSupport(Events.class, TableUri.EVENT_DATA_ABILITY_URI);

        private InnerSingleton() {
        }
    }

    private void setValuesBucket(ValuesBucket valuesBucket, Events events) {
        setValuesBucketSyncColumnsField(valuesBucket, events);
        setValuesBucketEventsColumnsField(valuesBucket, events);
    }

    private void setValuesBucketSyncColumnsField(ValuesBucket valuesBucket, Events events) {
        valuesBucket.putString("_sync_id", events.getSyncId());
        valuesBucket.putLong("dirty", Long.valueOf(events.getDirty()));
        valuesBucket.putString("mutators", events.getCallingBundleName());
        valuesBucket.putBoolean("deleted", Boolean.valueOf(events.isDeleted()));
    }

    private void setValuesBucketEventsColumnsField(ValuesBucket valuesBucket, Events events) {
        valuesBucket.putInteger("calendar_id", Integer.valueOf(events.getAccId()));
        valuesBucket.putString("title", events.getTitle());
        valuesBucket.putString("description", events.getDescription());
        valuesBucket.putString("eventLocation", events.getEventPosition());
        valuesBucket.putInteger("eventColor", Integer.valueOf(events.getEventColour()));
        valuesBucket.putString("eventColor_index", events.getEventColourIndex());
        valuesBucket.putInteger("displayColor", Integer.valueOf(events.getDisplayColour()));
        valuesBucket.putInteger("eventStatus", Integer.valueOf(events.getEventStatus()));
        valuesBucket.putInteger("selfAttendeeStatus", Integer.valueOf(events.getOwnerAttendeeStatus()));
        valuesBucket.putBoolean("lastSynced", Boolean.valueOf(events.isLastSynced()));
        valuesBucket.putLong("dtstart", Long.valueOf(events.getEventStartTime()));
        valuesBucket.putLong("dtend", Long.valueOf(events.getEventEndTime()));
        valuesBucket.putString("duration", events.getDuration());
        valuesBucket.putString("eventTimezone", events.getStartTimezone());
        valuesBucket.putString("eventEndTimezone", events.getEndTimezone());
        valuesBucket.putBoolean("allDay", Boolean.valueOf(events.isWholeDay()));
        valuesBucket.putInteger("accessLevel", Integer.valueOf(events.getPermissionLevel()));
        valuesBucket.putInteger("availability", Integer.valueOf(events.getAvailableStatus()));
        valuesBucket.putBoolean("hasAlarm", Boolean.valueOf(events.hasAlarm()));
        valuesBucket.putBoolean("hasExtendedProperties", Boolean.valueOf(events.hasExtendedAttribute()));
        valuesBucket.putString("rrule", events.getRecurRule());
        valuesBucket.putString("rdate", events.getRecurDate());
        valuesBucket.putString("exrule", events.getExceptionRule());
        valuesBucket.putString("exdate", events.getExceptionDate());
        valuesBucket.putString("original_id", events.getInitialId());
        valuesBucket.putString("original_sync_id", events.getInitialSyncId());
        valuesBucket.putLong("lastDate", Long.valueOf(events.getLastRecurDate()));
        valuesBucket.putBoolean("hasAttendeeData", Boolean.valueOf(events.hasAttendeeInfo()));
        valuesBucket.putBoolean("canInviteOthers", Boolean.valueOf(events.isCanInvite()));
        valuesBucket.putString("organizer", events.getOrganizerEmail());
        valuesBucket.putString("customAppPackage", events.getCustomizeBundleName());
        valuesBucket.putString("customAppUri", events.getCustomizeAppUri());
    }
}
