package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DownGradeTableHelper extends AEntityHelper<DownGradeTable> {
    private static final DownGradeTableHelper INSTANCE = new DownGradeTableHelper();

    private DownGradeTableHelper() {
    }

    public static DownGradeTableHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DownGradeTable object) {
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
        Integer mFromVersion = object.getMFromVersion();
        if (mFromVersion != null) {
            statement.bindLong(3, (long) mFromVersion.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mToVersion = object.getMToVersion();
        if (mToVersion != null) {
            statement.bindLong(4, (long) mToVersion.intValue());
        } else {
            statement.bindNull(4);
        }
        String mFileName = object.getMFileName();
        if (mFileName != null) {
            statement.bindString(5, mFileName);
        } else {
            statement.bindNull(5);
        }
        String mSqlText = object.getMSqlText();
        if (mSqlText != null) {
            statement.bindString(6, mSqlText);
        } else {
            statement.bindNull(6);
        }
    }

    public DownGradeTable readObject(Cursor cursor, int offset) {
        return new DownGradeTable(cursor);
    }

    public void setPrimaryKeyValue(DownGradeTable object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DownGradeTable object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
