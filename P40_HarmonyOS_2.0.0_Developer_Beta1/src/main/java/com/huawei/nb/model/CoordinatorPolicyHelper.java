package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorPolicyHelper extends AEntityHelper<CoordinatorPolicy> {
    private static final CoordinatorPolicyHelper INSTANCE = new CoordinatorPolicyHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CoordinatorPolicy coordinatorPolicy) {
        return null;
    }

    private CoordinatorPolicyHelper() {
    }

    public static CoordinatorPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CoordinatorPolicy coordinatorPolicy) {
        Integer policyNo = coordinatorPolicy.getPolicyNo();
        if (policyNo != null) {
            statement.bindLong(1, (long) policyNo.intValue());
        } else {
            statement.bindNull(1);
        }
        String tableName = coordinatorPolicy.getTableName();
        if (tableName != null) {
            statement.bindString(2, tableName);
        } else {
            statement.bindNull(2);
        }
        String dbName = coordinatorPolicy.getDbName();
        if (dbName != null) {
            statement.bindString(3, dbName);
        } else {
            statement.bindNull(3);
        }
        Long syncMode = coordinatorPolicy.getSyncMode();
        if (syncMode != null) {
            statement.bindLong(4, syncMode.longValue());
        } else {
            statement.bindNull(4);
        }
        Long syncTime = coordinatorPolicy.getSyncTime();
        if (syncTime != null) {
            statement.bindLong(5, syncTime.longValue());
        } else {
            statement.bindNull(5);
        }
        Long syncPoint = coordinatorPolicy.getSyncPoint();
        if (syncPoint != null) {
            statement.bindLong(6, syncPoint.longValue());
        } else {
            statement.bindNull(6);
        }
        String remoteUrl = coordinatorPolicy.getRemoteUrl();
        if (remoteUrl != null) {
            statement.bindString(7, remoteUrl);
        } else {
            statement.bindNull(7);
        }
        Long dataType = coordinatorPolicy.getDataType();
        if (dataType != null) {
            statement.bindLong(8, dataType.longValue());
        } else {
            statement.bindNull(8);
        }
        Long isAllowOverWrite = coordinatorPolicy.getIsAllowOverWrite();
        if (isAllowOverWrite != null) {
            statement.bindLong(9, isAllowOverWrite.longValue());
        } else {
            statement.bindNull(9);
        }
        Long networkMode = coordinatorPolicy.getNetworkMode();
        if (networkMode != null) {
            statement.bindLong(10, networkMode.longValue());
        } else {
            statement.bindNull(10);
        }
        String syncField = coordinatorPolicy.getSyncField();
        if (syncField != null) {
            statement.bindString(11, syncField);
        } else {
            statement.bindNull(11);
        }
        String startTime = coordinatorPolicy.getStartTime();
        if (startTime != null) {
            statement.bindString(12, startTime);
        } else {
            statement.bindNull(12);
        }
        Integer electricity = coordinatorPolicy.getElectricity();
        if (electricity != null) {
            statement.bindLong(13, (long) electricity.intValue());
        } else {
            statement.bindNull(13);
        }
        Integer tilingTime = coordinatorPolicy.getTilingTime();
        if (tilingTime != null) {
            statement.bindLong(14, (long) tilingTime.intValue());
        } else {
            statement.bindNull(14);
        }
        Long dataTrafficSyncTime = coordinatorPolicy.getDataTrafficSyncTime();
        if (dataTrafficSyncTime != null) {
            statement.bindLong(15, dataTrafficSyncTime.longValue());
        } else {
            statement.bindNull(15);
        }
        Integer syncPeriod = coordinatorPolicy.getSyncPeriod();
        if (syncPeriod != null) {
            statement.bindLong(16, (long) syncPeriod.intValue());
        } else {
            statement.bindNull(16);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CoordinatorPolicy readObject(Cursor cursor, int i) {
        return new CoordinatorPolicy(cursor);
    }

    public void setPrimaryKeyValue(CoordinatorPolicy coordinatorPolicy, long j) {
        coordinatorPolicy.setPolicyNo(Integer.valueOf((int) j));
    }
}
