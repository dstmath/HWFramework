package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class TableSyncHelper extends AEntityHelper<TableSync> {
    private static final TableSyncHelper INSTANCE = new TableSyncHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, TableSync tableSync) {
        return null;
    }

    private TableSyncHelper() {
    }

    public static TableSyncHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, TableSync tableSync) {
        Integer mId = tableSync.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mDBName = tableSync.getMDBName();
        if (mDBName != null) {
            statement.bindString(2, mDBName);
        } else {
            statement.bindNull(2);
        }
        String mTableName = tableSync.getMTableName();
        if (mTableName != null) {
            statement.bindString(3, mTableName);
        } else {
            statement.bindNull(3);
        }
        String mCloudUri = tableSync.getMCloudUri();
        if (mCloudUri != null) {
            statement.bindString(4, mCloudUri);
        } else {
            statement.bindNull(4);
        }
        Integer mTitle = tableSync.getMTitle();
        if (mTitle != null) {
            statement.bindLong(5, (long) mTitle.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mChannel = tableSync.getMChannel();
        if (mChannel != null) {
            statement.bindLong(6, (long) mChannel.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mSyncMode = tableSync.getMSyncMode();
        if (mSyncMode != null) {
            statement.bindLong(7, (long) mSyncMode.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer mSyncTime = tableSync.getMSyncTime();
        if (mSyncTime != null) {
            statement.bindLong(8, (long) mSyncTime.intValue());
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public TableSync readObject(Cursor cursor, int i) {
        return new TableSync(cursor);
    }

    public void setPrimaryKeyValue(TableSync tableSync, long j) {
        tableSync.setMId(Integer.valueOf((int) j));
    }
}
