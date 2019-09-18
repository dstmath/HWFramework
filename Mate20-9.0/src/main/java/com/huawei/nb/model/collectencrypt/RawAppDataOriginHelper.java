package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class RawAppDataOriginHelper extends AEntityHelper<RawAppDataOrigin> {
    private static final RawAppDataOriginHelper INSTANCE = new RawAppDataOriginHelper();

    private RawAppDataOriginHelper() {
    }

    public static RawAppDataOriginHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawAppDataOrigin object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Long dataSerialNumber = object.getDataSerialNumber();
        if (dataSerialNumber != null) {
            statement.bindLong(2, dataSerialNumber.longValue());
        } else {
            statement.bindNull(2);
        }
        Long timestamp = object.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(3, timestamp.longValue());
        } else {
            statement.bindNull(3);
        }
        String packageName = object.getPackageName();
        if (packageName != null) {
            statement.bindString(4, packageName);
        } else {
            statement.bindNull(4);
        }
        String jsonData = object.getJsonData();
        if (jsonData != null) {
            statement.bindString(5, jsonData);
        } else {
            statement.bindNull(5);
        }
        String column1 = object.getColumn1();
        if (column1 != null) {
            statement.bindString(6, column1);
        } else {
            statement.bindNull(6);
        }
        String column2 = object.getColumn2();
        if (column2 != null) {
            statement.bindString(7, column2);
        } else {
            statement.bindNull(7);
        }
    }

    public RawAppDataOrigin readObject(Cursor cursor, int offset) {
        return new RawAppDataOrigin(cursor);
    }

    public void setPrimaryKeyValue(RawAppDataOrigin object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawAppDataOrigin object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
