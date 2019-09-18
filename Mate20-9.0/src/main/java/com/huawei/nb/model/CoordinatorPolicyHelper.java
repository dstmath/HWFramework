package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorPolicyHelper extends AEntityHelper<CoordinatorPolicy> {
    private static final CoordinatorPolicyHelper INSTANCE = new CoordinatorPolicyHelper();

    private CoordinatorPolicyHelper() {
    }

    public static CoordinatorPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CoordinatorPolicy object) {
        Integer policyNo = object.getPolicyNo();
        if (policyNo != null) {
            statement.bindLong(1, (long) policyNo.intValue());
        } else {
            statement.bindNull(1);
        }
        String tableName = object.getTableName();
        if (tableName != null) {
            statement.bindString(2, tableName);
        } else {
            statement.bindNull(2);
        }
        String dbName = object.getDbName();
        if (dbName != null) {
            statement.bindString(3, dbName);
        } else {
            statement.bindNull(3);
        }
        Long syncMode = object.getSyncMode();
        if (syncMode != null) {
            statement.bindLong(4, syncMode.longValue());
        } else {
            statement.bindNull(4);
        }
        Long syncTime = object.getSyncTime();
        if (syncTime != null) {
            statement.bindLong(5, syncTime.longValue());
        } else {
            statement.bindNull(5);
        }
        Long syncPoint = object.getSyncPoint();
        if (syncPoint != null) {
            statement.bindLong(6, syncPoint.longValue());
        } else {
            statement.bindNull(6);
        }
        String remoteUrl = object.getRemoteUrl();
        if (remoteUrl != null) {
            statement.bindString(7, remoteUrl);
        } else {
            statement.bindNull(7);
        }
        Long dataType = object.getDataType();
        if (dataType != null) {
            statement.bindLong(8, dataType.longValue());
        } else {
            statement.bindNull(8);
        }
        Long isAllowOverWrite = object.getIsAllowOverWrite();
        if (isAllowOverWrite != null) {
            statement.bindLong(9, isAllowOverWrite.longValue());
        } else {
            statement.bindNull(9);
        }
        Long networkMode = object.getNetworkMode();
        if (networkMode != null) {
            statement.bindLong(10, networkMode.longValue());
        } else {
            statement.bindNull(10);
        }
        String syncField = object.getSyncField();
        if (syncField != null) {
            statement.bindString(11, syncField);
        } else {
            statement.bindNull(11);
        }
        String startTime = object.getStartTime();
        if (startTime != null) {
            statement.bindString(12, startTime);
        } else {
            statement.bindNull(12);
        }
        Integer electricity = object.getElectricity();
        if (electricity != null) {
            statement.bindLong(13, (long) electricity.intValue());
        } else {
            statement.bindNull(13);
        }
        Integer tilingTime = object.getTilingTime();
        if (tilingTime != null) {
            statement.bindLong(14, (long) tilingTime.intValue());
        } else {
            statement.bindNull(14);
        }
        Long dataTrafficSyncTime = object.getDataTrafficSyncTime();
        if (dataTrafficSyncTime != null) {
            statement.bindLong(15, dataTrafficSyncTime.longValue());
        } else {
            statement.bindNull(15);
        }
        Integer syncPeriod = object.getSyncPeriod();
        if (syncPeriod != null) {
            statement.bindLong(16, (long) syncPeriod.intValue());
            return;
        }
        statement.bindNull(16);
    }

    public CoordinatorPolicy readObject(Cursor cursor, int offset) {
        return new CoordinatorPolicy(cursor);
    }

    public void setPrimaryKeyValue(CoordinatorPolicy object, long value) {
        object.setPolicyNo(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, CoordinatorPolicy object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
