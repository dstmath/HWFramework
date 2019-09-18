package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DBInfoHelper extends AEntityHelper<DBInfo> {
    private static final DBInfoHelper INSTANCE = new DBInfoHelper();

    private DBInfoHelper() {
    }

    public static DBInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DBInfo object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String dbName = object.getDbName();
        if (dbName != null) {
            statement.bindString(2, dbName);
        } else {
            statement.bindNull(2);
        }
        String dbPath = object.getDbPath();
        if (dbPath != null) {
            statement.bindString(3, dbPath);
        } else {
            statement.bindNull(3);
        }
        Integer dbType = object.getDbType();
        if (dbType != null) {
            statement.bindLong(4, (long) dbType.intValue());
        } else {
            statement.bindNull(4);
        }
        String description = object.getDescription();
        if (description != null) {
            statement.bindString(5, description);
        } else {
            statement.bindNull(5);
        }
        String modelXml = object.getModelXml();
        if (modelXml != null) {
            statement.bindString(6, modelXml);
        } else {
            statement.bindNull(6);
        }
        String dataXml = object.getDataXml();
        if (dataXml != null) {
            statement.bindString(7, dataXml);
        } else {
            statement.bindNull(7);
        }
        Boolean isEncrypt = object.getIsEncrypt();
        if (isEncrypt != null) {
            statement.bindLong(8, isEncrypt.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(8);
        }
        Integer ownerBusinessId = object.getOwnerBusinessId();
        if (ownerBusinessId != null) {
            statement.bindLong(9, (long) ownerBusinessId.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer recovery = object.getRecovery();
        if (recovery != null) {
            statement.bindLong(10, (long) recovery.intValue());
        } else {
            statement.bindNull(10);
        }
        Boolean isCreate = object.getIsCreate();
        if (isCreate != null) {
            statement.bindLong(11, isCreate.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(11);
        }
        Integer initMode = object.getInitMode();
        if (initMode != null) {
            statement.bindLong(12, (long) initMode.intValue());
        } else {
            statement.bindNull(12);
        }
        Long capacity = object.getCapacity();
        if (capacity != null) {
            statement.bindLong(13, capacity.longValue());
        } else {
            statement.bindNull(13);
        }
        Integer actionAfterFull = object.getActionAfterFull();
        if (actionAfterFull != null) {
            statement.bindLong(14, (long) actionAfterFull.intValue());
        } else {
            statement.bindNull(14);
        }
        String config = object.getConfig();
        if (config != null) {
            statement.bindString(15, config);
        } else {
            statement.bindNull(15);
        }
        Integer shareMode = object.getShareMode();
        if (shareMode != null) {
            statement.bindLong(16, (long) shareMode.intValue());
        } else {
            statement.bindNull(16);
        }
    }

    public DBInfo readObject(Cursor cursor, int offset) {
        return new DBInfo(cursor);
    }

    public void setPrimaryKeyValue(DBInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DBInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
