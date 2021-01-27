package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DBInfoHelper extends AEntityHelper<DBInfo> {
    private static final DBInfoHelper INSTANCE = new DBInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DBInfo dBInfo) {
        return null;
    }

    private DBInfoHelper() {
    }

    public static DBInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DBInfo dBInfo) {
        Integer mId = dBInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String dbName = dBInfo.getDbName();
        if (dbName != null) {
            statement.bindString(2, dbName);
        } else {
            statement.bindNull(2);
        }
        String dbPath = dBInfo.getDbPath();
        if (dbPath != null) {
            statement.bindString(3, dbPath);
        } else {
            statement.bindNull(3);
        }
        Integer dbType = dBInfo.getDbType();
        if (dbType != null) {
            statement.bindLong(4, (long) dbType.intValue());
        } else {
            statement.bindNull(4);
        }
        String description = dBInfo.getDescription();
        if (description != null) {
            statement.bindString(5, description);
        } else {
            statement.bindNull(5);
        }
        String modelXml = dBInfo.getModelXml();
        if (modelXml != null) {
            statement.bindString(6, modelXml);
        } else {
            statement.bindNull(6);
        }
        String dataXml = dBInfo.getDataXml();
        if (dataXml != null) {
            statement.bindString(7, dataXml);
        } else {
            statement.bindNull(7);
        }
        Boolean isEncrypt = dBInfo.getIsEncrypt();
        long j = 1;
        if (isEncrypt != null) {
            statement.bindLong(8, isEncrypt.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(8);
        }
        Integer ownerBusinessId = dBInfo.getOwnerBusinessId();
        if (ownerBusinessId != null) {
            statement.bindLong(9, (long) ownerBusinessId.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer recovery = dBInfo.getRecovery();
        if (recovery != null) {
            statement.bindLong(10, (long) recovery.intValue());
        } else {
            statement.bindNull(10);
        }
        Boolean isCreate = dBInfo.getIsCreate();
        if (isCreate != null) {
            if (!isCreate.booleanValue()) {
                j = 0;
            }
            statement.bindLong(11, j);
        } else {
            statement.bindNull(11);
        }
        Integer initMode = dBInfo.getInitMode();
        if (initMode != null) {
            statement.bindLong(12, (long) initMode.intValue());
        } else {
            statement.bindNull(12);
        }
        Long capacity = dBInfo.getCapacity();
        if (capacity != null) {
            statement.bindLong(13, capacity.longValue());
        } else {
            statement.bindNull(13);
        }
        Integer actionAfterFull = dBInfo.getActionAfterFull();
        if (actionAfterFull != null) {
            statement.bindLong(14, (long) actionAfterFull.intValue());
        } else {
            statement.bindNull(14);
        }
        String config = dBInfo.getConfig();
        if (config != null) {
            statement.bindString(15, config);
        } else {
            statement.bindNull(15);
        }
        Integer shareMode = dBInfo.getShareMode();
        if (shareMode != null) {
            statement.bindLong(16, (long) shareMode.intValue());
        } else {
            statement.bindNull(16);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DBInfo readObject(Cursor cursor, int i) {
        return new DBInfo(cursor);
    }

    public void setPrimaryKeyValue(DBInfo dBInfo, long j) {
        dBInfo.setMId(Integer.valueOf((int) j));
    }
}
