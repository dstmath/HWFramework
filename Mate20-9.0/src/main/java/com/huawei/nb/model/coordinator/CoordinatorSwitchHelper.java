package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorSwitchHelper extends AEntityHelper<CoordinatorSwitch> {
    private static final CoordinatorSwitchHelper INSTANCE = new CoordinatorSwitchHelper();

    private CoordinatorSwitchHelper() {
    }

    public static CoordinatorSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CoordinatorSwitch object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String serviceName = object.getServiceName();
        if (serviceName != null) {
            statement.bindString(2, serviceName);
        } else {
            statement.bindNull(2);
        }
        String packageName = object.getPackageName();
        if (packageName != null) {
            statement.bindString(3, packageName);
        } else {
            statement.bindNull(3);
        }
        statement.bindLong(4, object.getIsSwitchOn() ? 1 : 0);
        statement.bindLong(5, object.getIsAutoUpdate() ? 1 : 0);
        Long latestTimestamp = object.getLatestTimestamp();
        if (latestTimestamp != null) {
            statement.bindLong(6, latestTimestamp.longValue());
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, object.getCanUseFlowData() ? 1 : 0);
        statement.bindDouble(8, object.getCurrentFlowData());
        statement.bindDouble(9, object.getMaxFlowData());
        String reserve1 = object.getReserve1();
        if (reserve1 != null) {
            statement.bindString(10, reserve1);
        } else {
            statement.bindNull(10);
        }
        String reserve2 = object.getReserve2();
        if (reserve2 != null) {
            statement.bindString(11, reserve2);
        } else {
            statement.bindNull(11);
        }
    }

    public CoordinatorSwitch readObject(Cursor cursor, int offset) {
        return new CoordinatorSwitch(cursor);
    }

    public void setPrimaryKeyValue(CoordinatorSwitch object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, CoordinatorSwitch object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
