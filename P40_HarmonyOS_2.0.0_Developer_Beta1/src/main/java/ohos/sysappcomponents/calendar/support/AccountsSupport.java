package ohos.sysappcomponents.calendar.support;

import java.util.ArrayList;
import java.util.Optional;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.LogUtil;
import ohos.sysappcomponents.calendar.entity.Accounts;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.utils.net.Uri;

public class AccountsSupport extends TableSupport {
    private static final String ID_RAW_SELECTION = "_id=?";
    private static final String TAG = AccountsSupport.class.getSimpleName();

    private AccountsSupport(Class<? extends CalendarEntity> cls, Uri uri) {
        super(cls, uri);
    }

    public static AccountsSupport getInstance() {
        return InnerSingleton.INSTANCE;
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public Rule getRule() {
        return new Rule() {
            /* class ohos.sysappcomponents.calendar.support.$$Lambda$AccountsSupport$kOjtakkx43Fg5clq8Ch66fQydAM */

            @Override // ohos.sysappcomponents.calendar.support.Rule
            public final Optional getEntity(ResultSet resultSet, String[] strArr) {
                return AccountsSupport.this.lambda$getRule$0$AccountsSupport(resultSet, strArr);
            }
        };
    }

    public /* synthetic */ Optional lambda$getRule$0$AccountsSupport(ResultSet resultSet, String[] strArr) {
        Accounts accounts = new Accounts();
        for (String str : strArr) {
            setEntityValue(accounts, resultSet, str);
        }
        return Optional.of(accounts);
    }

    @Override // ohos.sysappcomponents.calendar.support.TableSupport
    public ValuesBucket getValueBucket(CalendarEntity calendarEntity) {
        ValuesBucket valuesBucket = new ValuesBucket();
        if (calendarEntity == null) {
            return valuesBucket;
        }
        if (calendarEntity instanceof Accounts) {
            setValuesBucket(valuesBucket, (Accounts) calendarEntity);
        } else {
            LogUtil.warn(TAG, "Invalid input. Input must be accounts entity objects.");
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
        initSyncColumnsMap();
        initAccountsColumnsMap();
    }

    /* access modifiers changed from: private */
    public static class InnerSingleton {
        private static final AccountsSupport INSTANCE = new AccountsSupport(Accounts.class, TableUri.ACCOUNT_DATA_ABILITY_URI);

        private InnerSingleton() {
        }
    }

    private void setValuesBucket(ValuesBucket valuesBucket, Accounts accounts) {
        setValuesBucketSyncColumnsField(valuesBucket, accounts);
        setValuesBucketAccountsColumnsField(valuesBucket, accounts);
    }

    private void setValuesBucketSyncColumnsField(ValuesBucket valuesBucket, Accounts accounts) {
        valuesBucket.putString("account_name", accounts.getAccName());
        valuesBucket.putString("account_type", accounts.getAccType());
        valuesBucket.putString("_sync_id", accounts.getSyncId());
        valuesBucket.putLong("dirty", Long.valueOf(accounts.getDirty()));
        valuesBucket.putString("mutators", accounts.getCallingBundleName());
        valuesBucket.putBoolean("deleted", Boolean.valueOf(accounts.isDeleted()));
    }

    private void setValuesBucketAccountsColumnsField(ValuesBucket valuesBucket, Accounts accounts) {
        valuesBucket.putInteger("calendar_color", Integer.valueOf(accounts.getAccColour()));
        valuesBucket.putString("calendar_color_index", accounts.getAccColourIndex());
        valuesBucket.putString("calendar_displayName", accounts.getAccDisplayName());
        valuesBucket.putString("calendar_access_level", accounts.getAccPermissionLevel());
        valuesBucket.putBoolean("visible", Boolean.valueOf(accounts.isVisible()));
        valuesBucket.putString("calendar_timezone", accounts.getAccTimezone());
        valuesBucket.putBoolean("sync_events", Boolean.valueOf(accounts.isSyncEvents()));
        valuesBucket.putString("ownerAccount", accounts.getMasterAccount());
        valuesBucket.putBoolean("canOrganizerRespond", Boolean.valueOf(accounts.isOrganizerResponse()));
        valuesBucket.putBoolean("canModifyTimeZone", Boolean.valueOf(accounts.isModifyTimeZone()));
        valuesBucket.putInteger("maxReminders", Integer.valueOf(accounts.getMaxReminders()));
        valuesBucket.putString("allowedReminders", accounts.getRemindersType());
        valuesBucket.putString("allowedAvailability", accounts.getAvailabilityStatus());
        valuesBucket.putString("allowedAttendeeTypes", accounts.getAttendeeTypes());
        valuesBucket.putBoolean("isPrimary", Boolean.valueOf(accounts.isPrimary()));
    }
}
