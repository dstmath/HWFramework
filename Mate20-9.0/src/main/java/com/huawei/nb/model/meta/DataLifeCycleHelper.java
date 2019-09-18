package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DataLifeCycleHelper extends AEntityHelper<DataLifeCycle> {
    private static final DataLifeCycleHelper INSTANCE = new DataLifeCycleHelper();

    private DataLifeCycleHelper() {
    }

    public static DataLifeCycleHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DataLifeCycle object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mDBName = object.getMDBName();
        if (mDBName != null) {
            statement.bindString(2, mDBName);
        } else {
            statement.bindNull(2);
        }
        String mTableName = object.getMTableName();
        if (mTableName != null) {
            statement.bindString(3, mTableName);
        } else {
            statement.bindNull(3);
        }
        Integer mMode = object.getMMode();
        if (mMode != null) {
            statement.bindLong(4, (long) mMode.intValue());
        } else {
            statement.bindNull(4);
        }
        String mFieldName = object.getMFieldName();
        if (mFieldName != null) {
            statement.bindString(5, mFieldName);
        } else {
            statement.bindNull(5);
        }
        Integer mCount = object.getMCount();
        if (mCount != null) {
            statement.bindLong(6, (long) mCount.intValue());
        } else {
            statement.bindNull(6);
        }
        Long mDBRekeyTime = object.getMDBRekeyTime();
        if (mDBRekeyTime != null) {
            statement.bindLong(7, mDBRekeyTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Integer mThreshold = object.getMThreshold();
        if (mThreshold != null) {
            statement.bindLong(8, (long) mThreshold.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer mUnit = object.getMUnit();
        if (mUnit != null) {
            statement.bindLong(9, (long) mUnit.intValue());
        } else {
            statement.bindNull(9);
        }
    }

    public DataLifeCycle readObject(Cursor cursor, int offset) {
        return new DataLifeCycle(cursor);
    }

    public void setPrimaryKeyValue(DataLifeCycle object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DataLifeCycle object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
