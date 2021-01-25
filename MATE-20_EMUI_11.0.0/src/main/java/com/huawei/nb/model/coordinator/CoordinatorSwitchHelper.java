package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorSwitchHelper extends AEntityHelper<CoordinatorSwitch> {
    private static final CoordinatorSwitchHelper INSTANCE = new CoordinatorSwitchHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CoordinatorSwitch coordinatorSwitch) {
        return null;
    }

    private CoordinatorSwitchHelper() {
    }

    public static CoordinatorSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CoordinatorSwitch coordinatorSwitch) {
        Long id = coordinatorSwitch.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String serviceName = coordinatorSwitch.getServiceName();
        if (serviceName != null) {
            statement.bindString(2, serviceName);
        } else {
            statement.bindNull(2);
        }
        String packageName = coordinatorSwitch.getPackageName();
        if (packageName != null) {
            statement.bindString(3, packageName);
        } else {
            statement.bindNull(3);
        }
        long j = 1;
        statement.bindLong(4, coordinatorSwitch.getIsSwitchOn() ? 1 : 0);
        statement.bindLong(5, coordinatorSwitch.getIsAutoUpdate() ? 1 : 0);
        Long latestTimestamp = coordinatorSwitch.getLatestTimestamp();
        if (latestTimestamp != null) {
            statement.bindLong(6, latestTimestamp.longValue());
        } else {
            statement.bindNull(6);
        }
        if (!coordinatorSwitch.getCanUseFlowData()) {
            j = 0;
        }
        statement.bindLong(7, j);
        statement.bindDouble(8, coordinatorSwitch.getCurrentFlowData());
        statement.bindDouble(9, coordinatorSwitch.getMaxFlowData());
        String reserve1 = coordinatorSwitch.getReserve1();
        if (reserve1 != null) {
            statement.bindString(10, reserve1);
        } else {
            statement.bindNull(10);
        }
        String reserve2 = coordinatorSwitch.getReserve2();
        if (reserve2 != null) {
            statement.bindString(11, reserve2);
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CoordinatorSwitch readObject(Cursor cursor, int i) {
        return new CoordinatorSwitch(cursor);
    }

    public void setPrimaryKeyValue(CoordinatorSwitch coordinatorSwitch, long j) {
        coordinatorSwitch.setId(Long.valueOf(j));
    }
}
