package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DownGradeTableHelper extends AEntityHelper<DownGradeTable> {
    private static final DownGradeTableHelper INSTANCE = new DownGradeTableHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DownGradeTable downGradeTable) {
        return null;
    }

    private DownGradeTableHelper() {
    }

    public static DownGradeTableHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DownGradeTable downGradeTable) {
        Integer mId = downGradeTable.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mDBName = downGradeTable.getMDBName();
        if (mDBName != null) {
            statement.bindString(2, mDBName);
        } else {
            statement.bindNull(2);
        }
        Integer mFromVersion = downGradeTable.getMFromVersion();
        if (mFromVersion != null) {
            statement.bindLong(3, (long) mFromVersion.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mToVersion = downGradeTable.getMToVersion();
        if (mToVersion != null) {
            statement.bindLong(4, (long) mToVersion.intValue());
        } else {
            statement.bindNull(4);
        }
        String mFileName = downGradeTable.getMFileName();
        if (mFileName != null) {
            statement.bindString(5, mFileName);
        } else {
            statement.bindNull(5);
        }
        String mSqlText = downGradeTable.getMSqlText();
        if (mSqlText != null) {
            statement.bindString(6, mSqlText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DownGradeTable readObject(Cursor cursor, int i) {
        return new DownGradeTable(cursor);
    }

    public void setPrimaryKeyValue(DownGradeTable downGradeTable, long j) {
        downGradeTable.setMId(Integer.valueOf((int) j));
    }
}
