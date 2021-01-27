package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DataLifeCycleHelper extends AEntityHelper<DataLifeCycle> {
    private static final DataLifeCycleHelper INSTANCE = new DataLifeCycleHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DataLifeCycle dataLifeCycle) {
        return null;
    }

    private DataLifeCycleHelper() {
    }

    public static DataLifeCycleHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DataLifeCycle dataLifeCycle) {
        Integer mId = dataLifeCycle.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mDBName = dataLifeCycle.getMDBName();
        if (mDBName != null) {
            statement.bindString(2, mDBName);
        } else {
            statement.bindNull(2);
        }
        String mTableName = dataLifeCycle.getMTableName();
        if (mTableName != null) {
            statement.bindString(3, mTableName);
        } else {
            statement.bindNull(3);
        }
        Integer mMode = dataLifeCycle.getMMode();
        if (mMode != null) {
            statement.bindLong(4, (long) mMode.intValue());
        } else {
            statement.bindNull(4);
        }
        String mFieldName = dataLifeCycle.getMFieldName();
        if (mFieldName != null) {
            statement.bindString(5, mFieldName);
        } else {
            statement.bindNull(5);
        }
        Integer mCount = dataLifeCycle.getMCount();
        if (mCount != null) {
            statement.bindLong(6, (long) mCount.intValue());
        } else {
            statement.bindNull(6);
        }
        Long mDBRekeyTime = dataLifeCycle.getMDBRekeyTime();
        if (mDBRekeyTime != null) {
            statement.bindLong(7, mDBRekeyTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Integer mThreshold = dataLifeCycle.getMThreshold();
        if (mThreshold != null) {
            statement.bindLong(8, (long) mThreshold.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer mUnit = dataLifeCycle.getMUnit();
        if (mUnit != null) {
            statement.bindLong(9, (long) mUnit.intValue());
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DataLifeCycle readObject(Cursor cursor, int i) {
        return new DataLifeCycle(cursor);
    }

    public void setPrimaryKeyValue(DataLifeCycle dataLifeCycle, long j) {
        dataLifeCycle.setMId(Integer.valueOf((int) j));
    }
}
