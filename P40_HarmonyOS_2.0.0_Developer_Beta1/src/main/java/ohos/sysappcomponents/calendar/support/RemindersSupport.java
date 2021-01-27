package ohos.sysappcomponents.calendar.support;

import java.util.ArrayList;
import java.util.Optional;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.LogUtil;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.entity.Reminders;
import ohos.utils.net.Uri;

public class RemindersSupport extends TableSupport {
    private static final String EVENT_ID_RAW_SELECTION = "event_id=?";
    private static final String TAG = RemindersSupport.class.getSimpleName();

    private RemindersSupport(Class<? extends CalendarEntity> cls, Uri uri) {
        super(cls, uri);
    }

    static RemindersSupport getInstance() {
        return InnerSingleton.INSTANCE;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public Rule getRule() {
        return new Rule() {
            /* class ohos.sysappcomponents.calendar.support.$$Lambda$RemindersSupport$0XI3rX1RKXOSngToC_EpZYh0PF8 */

            @Override // ohos.sysappcomponents.calendar.support.Rule
            public final Optional getEntity(ResultSet resultSet, String[] strArr) {
                return RemindersSupport.this.lambda$getRule$0$RemindersSupport(resultSet, strArr);
            }
        };
    }

    public /* synthetic */ Optional lambda$getRule$0$RemindersSupport(ResultSet resultSet, String[] strArr) {
        Reminders reminders = new Reminders();
        for (String str : strArr) {
            setEntityValue(reminders, resultSet, str);
        }
        return Optional.of(reminders);
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public ValuesBucket getValueBucket(CalendarEntity calendarEntity) {
        ValuesBucket valuesBucket = new ValuesBucket();
        if (calendarEntity == null) {
            return valuesBucket;
        }
        if (calendarEntity instanceof Reminders) {
            setValuesBucket(valuesBucket, (Reminders) calendarEntity);
        } else {
            LogUtil.warn(TAG, "Invalid input. Input must be reminders entity objects.");
        }
        return valuesBucket;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public DataAbilityPredicates getPredicate(int i) {
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(EVENT_ID_RAW_SELECTION);
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(i));
        dataAbilityPredicates.setWhereArgs(arrayList);
        return dataAbilityPredicates;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public void initColumnMap() {
        initBaseColumnsMap();
        initRemindersColumnsMap();
    }

    /* access modifiers changed from: private */
    public static class InnerSingleton {
        private static final RemindersSupport INSTANCE = new RemindersSupport(Reminders.class, TableUri.REMINDER_DATA_ABILITY_URI);

        private InnerSingleton() {
        }
    }

    private void setValuesBucket(ValuesBucket valuesBucket, Reminders reminders) {
        setValuesBucketRemindersColumnsField(valuesBucket, reminders);
        setValuesBucketSyncColumnsField(valuesBucket, reminders);
    }

    private void setValuesBucketSyncColumnsField(ValuesBucket valuesBucket, Reminders reminders) {
        valuesBucket.putBoolean("deleted", Boolean.valueOf(reminders.isDeleted()));
        valuesBucket.putString("_sync_id", reminders.getSyncId());
    }

    private void setValuesBucketRemindersColumnsField(ValuesBucket valuesBucket, Reminders reminders) {
        valuesBucket.putInteger("event_id", Integer.valueOf(reminders.getEventId()));
        valuesBucket.putInteger("minutes", Integer.valueOf(reminders.getRemindMinutes()));
        valuesBucket.putInteger("method", Integer.valueOf(reminders.getRemindType()));
    }
}
