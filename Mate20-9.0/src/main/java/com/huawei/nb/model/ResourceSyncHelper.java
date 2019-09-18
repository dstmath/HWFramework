package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ResourceSyncHelper extends AEntityHelper<ResourceSync> {
    private static final ResourceSyncHelper INSTANCE = new ResourceSyncHelper();

    private ResourceSyncHelper() {
    }

    public static ResourceSyncHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ResourceSync object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
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
            return;
        }
        statement.bindNull(14);
    }

    public ResourceSync readObject(Cursor cursor, int offset) {
        return new ResourceSync(cursor);
    }

    public void setPrimaryKeyValue(ResourceSync object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, ResourceSync object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
