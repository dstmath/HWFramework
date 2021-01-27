package ohos.sysappcomponents.calendar;

import java.security.InvalidParameterException;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.app.Context;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.support.TableSupport;
import ohos.sysappcomponents.calendar.support.TableSupportFactory;
import ohos.utils.net.Uri;

public class CalendarDataHelper {
    private String accName;
    private String accType;
    private DataAbilityHelper dataAbilityHelper;
    private TableSupport tableSupport;

    private CalendarDataHelper(DataAbilityHelper dataAbilityHelper2, TableSupport tableSupport2) {
        this.dataAbilityHelper = dataAbilityHelper2;
        this.tableSupport = tableSupport2;
    }

    private CalendarDataHelper(DataAbilityHelper dataAbilityHelper2, TableSupport tableSupport2, String str, String str2) {
        this.dataAbilityHelper = dataAbilityHelper2;
        this.tableSupport = tableSupport2;
        this.accName = str;
        this.accType = str2;
    }

    public static CalendarDataHelper creator(Context context, Class<? extends CalendarEntity> cls) throws InvalidParameterException {
        return new CalendarDataHelper(DataAbilityHelper.creator(context), TableSupportFactory.getTableSupport(cls));
    }

    public CalendarCollection query(DataAbilityPredicates dataAbilityPredicates, String[] strArr) throws DataAbilityRemoteException {
        return new CalendarCollection(this.dataAbilityHelper.query(this.tableSupport.getUri(), strArr, dataAbilityPredicates), this.tableSupport.getRule(), strArr);
    }

    public CalendarCollection query(int i, String[] strArr) throws DataAbilityRemoteException {
        return new CalendarCollection(this.dataAbilityHelper.query(this.tableSupport.getUri(), strArr, this.tableSupport.getPredicate(i)), this.tableSupport.getRule(), strArr);
    }

    public boolean insert(ValuesBucket valuesBucket) throws DataAbilityRemoteException {
        String string = valuesBucket.getString("account_name");
        String string2 = valuesBucket.getString("account_type");
        return (string == null || string2 == null) ? this.dataAbilityHelper.insert(this.tableSupport.getUri(), valuesBucket) > 0 : this.dataAbilityHelper.insert(this.tableSupport.getSyncUri(string, string2), valuesBucket) > 0;
    }

    public boolean update(ValuesBucket valuesBucket, DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        String string = valuesBucket.getString("account_name");
        String string2 = valuesBucket.getString("account_type");
        return (string == null || string2 == null) ? this.dataAbilityHelper.update(this.tableSupport.getUri(), valuesBucket, dataAbilityPredicates) > 0 : this.dataAbilityHelper.update(this.tableSupport.getSyncUri(string, string2), valuesBucket, dataAbilityPredicates) > 0;
    }

    public int delete(DataAbilityPredicates dataAbilityPredicates) throws DataAbilityRemoteException {
        return this.dataAbilityHelper.delete(this.tableSupport.getUri(), dataAbilityPredicates);
    }

    public boolean delete(CalendarEntity calendarEntity) throws DataAbilityRemoteException {
        return this.dataAbilityHelper.delete(this.tableSupport.getUri(), this.tableSupport.getPredicate(calendarEntity)) > 0;
    }

    public boolean delete(int i) throws DataAbilityRemoteException {
        return this.dataAbilityHelper.delete(this.tableSupport.getUri(), this.tableSupport.getPredicate(i)) > 0;
    }

    public boolean release() {
        return this.dataAbilityHelper.release();
    }

    public InstancesDataHelper getInstanceDataHelper() {
        return new InstancesDataHelper();
    }

    public class InstancesDataHelper {
        private InstancesDataHelper() {
        }

        public CalendarCollection query(DataAbilityPredicates dataAbilityPredicates, String[] strArr, long j, long j2) throws DataAbilityRemoteException {
            return new CalendarCollection(CalendarDataHelper.this.dataAbilityHelper.query(getQueryUri(j, j2), strArr, dataAbilityPredicates), CalendarDataHelper.this.tableSupport.getRule(), strArr);
        }

        public boolean release() {
            return CalendarDataHelper.this.release();
        }

        private Uri getQueryUri(long j, long j2) {
            return CalendarDataHelper.this.tableSupport.getUri().makeBuilder().appendEncodedPath(String.valueOf(j)).appendEncodedPath(String.valueOf(j2)).build();
        }
    }
}
