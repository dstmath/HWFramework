package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceSyncHelper extends AEntityHelper<ResourceSync> {
    private static final ResourceSyncHelper INSTANCE = new ResourceSyncHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ResourceSync resourceSync) {
        return null;
    }

    private ResourceSyncHelper() {
    }

    public static ResourceSyncHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ResourceSync resourceSync) {
        Integer id = resourceSync.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String tableName = resourceSync.getTableName();
        if (tableName != null) {
            statement.bindString(2, tableName);
        } else {
            statement.bindNull(2);
        }
        String dbName = resourceSync.getDbName();
        if (dbName != null) {
            statement.bindString(3, dbName);
        } else {
            statement.bindNull(3);
        }
        Long syncMode = resourceSync.getSyncMode();
        if (syncMode != null) {
            statement.bindLong(4, syncMode.longValue());
        } else {
            statement.bindNull(4);
        }
        Long syncTime = resourceSync.getSyncTime();
        if (syncTime != null) {
            statement.bindLong(5, syncTime.longValue());
        } else {
            statement.bindNull(5);
        }
        Long syncPoint = resourceSync.getSyncPoint();
        if (syncPoint != null) {
            statement.bindLong(6, syncPoint.longValue());
        } else {
            statement.bindNull(6);
        }
        String remoteUrl = resourceSync.getRemoteUrl();
        if (remoteUrl != null) {
            statement.bindString(7, remoteUrl);
        } else {
            statement.bindNull(7);
        }
        Long dataType = resourceSync.getDataType();
        if (dataType != null) {
            statement.bindLong(8, dataType.longValue());
        } else {
            statement.bindNull(8);
        }
        Long isAllowOverWrite = resourceSync.getIsAllowOverWrite();
        if (isAllowOverWrite != null) {
            statement.bindLong(9, isAllowOverWrite.longValue());
        } else {
            statement.bindNull(9);
        }
        Long networkMode = resourceSync.getNetworkMode();
        if (networkMode != null) {
            statement.bindLong(10, networkMode.longValue());
        } else {
            statement.bindNull(10);
        }
        String syncField = resourceSync.getSyncField();
        if (syncField != null) {
            statement.bindString(11, syncField);
        } else {
            statement.bindNull(11);
        }
        String startTime = resourceSync.getStartTime();
        if (startTime != null) {
            statement.bindString(12, startTime);
        } else {
            statement.bindNull(12);
        }
        Integer electricity = resourceSync.getElectricity();
        if (electricity != null) {
            statement.bindLong(13, (long) electricity.intValue());
        } else {
            statement.bindNull(13);
        }
        Integer tilingTime = resourceSync.getTilingTime();
        if (tilingTime != null) {
            statement.bindLong(14, (long) tilingTime.intValue());
        } else {
            statement.bindNull(14);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ResourceSync readObject(Cursor cursor, int i) {
        return new ResourceSync(cursor);
    }

    public void setPrimaryKeyValue(ResourceSync resourceSync, long j) {
        resourceSync.setId(Integer.valueOf((int) j));
    }
}
