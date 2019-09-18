package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class IndexFieldHelper extends AEntityHelper<IndexField> {
    private static final IndexFieldHelper INSTANCE = new IndexFieldHelper();

    private IndexFieldHelper() {
    }

    public static IndexFieldHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndexField object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String fieldName = object.getFieldName();
        if (fieldName != null) {
            statement.bindString(2, fieldName);
        } else {
            statement.bindNull(2);
        }
        Boolean isFieldConstants = object.getIsFieldConstants();
        if (isFieldConstants != null) {
            statement.bindLong(3, isFieldConstants.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(3);
        }
        String storeStatus = object.getStoreStatus();
        if (storeStatus != null) {
            statement.bindString(4, storeStatus);
        } else {
            statement.bindNull(4);
        }
        String indexStatus = object.getIndexStatus();
        if (indexStatus != null) {
            statement.bindString(5, indexStatus);
        } else {
            statement.bindNull(5);
        }
        Integer indexType = object.getIndexType();
        if (indexType != null) {
            statement.bindLong(6, (long) indexType.intValue());
        } else {
            statement.bindNull(6);
        }
    }

    public IndexField readObject(Cursor cursor, int offset) {
        return new IndexField(cursor);
    }

    public void setPrimaryKeyValue(IndexField object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, IndexField object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
