package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZMetaDataHelper extends AEntityHelper<ZMetaData> {
    private static final ZMetaDataHelper INSTANCE = new ZMetaDataHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZMetaData zMetaData) {
        return null;
    }

    private ZMetaDataHelper() {
    }

    public static ZMetaDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZMetaData zMetaData) {
        Integer id = zMetaData.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String name = zMetaData.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String value = zMetaData.getValue();
        if (value != null) {
            statement.bindString(3, value);
        } else {
            statement.bindNull(3);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZMetaData readObject(Cursor cursor, int i) {
        return new ZMetaData(cursor);
    }

    public void setPrimaryKeyValue(ZMetaData zMetaData, long j) {
        zMetaData.setId(Integer.valueOf((int) j));
    }
}
