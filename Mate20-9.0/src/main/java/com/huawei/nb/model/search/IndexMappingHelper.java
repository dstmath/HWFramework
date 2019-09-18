package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class IndexMappingHelper extends AEntityHelper<IndexMapping> {
    private static final IndexMappingHelper INSTANCE = new IndexMappingHelper();

    private IndexMappingHelper() {
    }

    public static IndexMappingHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndexMapping object) {
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
        Boolean isColumnNum = object.getIsColumnNum();
        if (isColumnNum != null) {
            statement.bindLong(3, isColumnNum.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(3);
        }
        String columnName = object.getColumnName();
        if (columnName != null) {
            statement.bindString(4, columnName);
        } else {
            statement.bindNull(4);
        }
        String columnNums = object.getColumnNums();
        if (columnNums != null) {
            statement.bindString(5, columnNums);
        } else {
            statement.bindNull(5);
        }
        Integer indexMappingType = object.getIndexMappingType();
        if (indexMappingType != null) {
            statement.bindLong(6, (long) indexMappingType.intValue());
        } else {
            statement.bindNull(6);
        }
    }

    public IndexMapping readObject(Cursor cursor, int offset) {
        return new IndexMapping(cursor);
    }

    public void setPrimaryKeyValue(IndexMapping object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, IndexMapping object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
