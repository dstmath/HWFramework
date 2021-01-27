package ohos.sysappcomponents.calendar.support;

import java.util.ArrayList;
import java.util.Optional;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.LogUtil;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.entity.Instances;
import ohos.utils.net.Uri;

public class InstancesSupport extends TableSupport {
    private static final String ID_RAW_SELECTION = "_id=?";
    private static final String TAG = InstancesSupport.class.getSimpleName();

    private InstancesSupport(Class<? extends CalendarEntity> cls, Uri uri) {
        super(cls, uri);
    }

    static InstancesSupport getInstance() {
        return InnerSingleton.INSTANCE;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public Rule getRule() {
        return new Rule() {
            /* class ohos.sysappcomponents.calendar.support.$$Lambda$InstancesSupport$POpIx0IYq0iLHAdyxiFntQxuoEc */

            @Override // ohos.sysappcomponents.calendar.support.Rule
            public final Optional getEntity(ResultSet resultSet, String[] strArr) {
                return InstancesSupport.this.lambda$getRule$0$InstancesSupport(resultSet, strArr);
            }
        };
    }

    public /* synthetic */ Optional lambda$getRule$0$InstancesSupport(ResultSet resultSet, String[] strArr) {
        Instances instances = new Instances();
        for (String str : strArr) {
            setEntityValue(instances, resultSet, str);
        }
        return Optional.of(instances);
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public ValuesBucket getValueBucket(CalendarEntity calendarEntity) {
        ValuesBucket valuesBucket = new ValuesBucket();
        if (calendarEntity == null) {
            return valuesBucket;
        }
        if (calendarEntity instanceof Instances) {
            setValuesBucket(valuesBucket, (Instances) calendarEntity);
        } else {
            LogUtil.warn(TAG, "Invalid input. Input must be instances entity objects.");
        }
        return valuesBucket;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public DataAbilityPredicates getPredicate(int i) {
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(ID_RAW_SELECTION);
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(i));
        dataAbilityPredicates.setWhereArgs(arrayList);
        return dataAbilityPredicates;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public void initColumnMap() {
        initBaseColumnsMap();
        initInstancesColumnsMap();
    }

    /* access modifiers changed from: private */
    public static class InnerSingleton {
        private static final InstancesSupport INSTANCE = new InstancesSupport(Instances.class, TableUri.INSTANCE_DATA_ABILITY_URI);

        private InnerSingleton() {
        }
    }

    private void setValuesBucket(ValuesBucket valuesBucket, Instances instances) {
        setValuesBucketSyncColumnsField(valuesBucket, instances);
        setValuesBucketInstancesColumnsField(valuesBucket, instances);
    }

    private void setValuesBucketSyncColumnsField(ValuesBucket valuesBucket, Instances instances) {
        valuesBucket.putBoolean("deleted", Boolean.valueOf(instances.isDeleted()));
    }

    private void setValuesBucketInstancesColumnsField(ValuesBucket valuesBucket, Instances instances) {
        valuesBucket.putLong("begin", Long.valueOf(instances.getInstanceBegin()));
        valuesBucket.putLong("end", Long.valueOf(instances.getInstanceEnd()));
        valuesBucket.putInteger("event_id", Integer.valueOf(instances.getEventId()));
        valuesBucket.putLong("startDay", Long.valueOf(instances.getInstanceStartDay()));
        valuesBucket.putLong("endDay", Long.valueOf(instances.getInstanceEndDay()));
        valuesBucket.putLong("startMinute", Long.valueOf(instances.getInstanceStartMinute()));
        valuesBucket.putLong("endMinute", Long.valueOf(instances.getInstanceEndMinute()));
    }
}
