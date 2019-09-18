package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSSwitchRecordHelper extends AEntityHelper<DSSwitchRecord> {
    private static final DSSwitchRecordHelper INSTANCE = new DSSwitchRecordHelper();

    private DSSwitchRecordHelper() {
    }

    public static DSSwitchRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSSwitchRecord object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Long timeStamp = object.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(2, timeStamp.longValue());
        } else {
            statement.bindNull(2);
        }
        String switchStatus = object.getSwitchStatus();
        if (switchStatus != null) {
            statement.bindString(3, switchStatus);
        } else {
            statement.bindNull(3);
        }
        String switchName = object.getSwitchName();
        if (switchName != null) {
            statement.bindString(4, switchName);
        } else {
            statement.bindNull(4);
        }
        String packageName = object.getPackageName();
        if (packageName != null) {
            statement.bindString(5, packageName);
        } else {
            statement.bindNull(5);
        }
        String reserved1 = object.getReserved1();
        if (reserved1 != null) {
            statement.bindString(6, reserved1);
        } else {
            statement.bindNull(6);
        }
        String reserved2 = object.getReserved2();
        if (reserved2 != null) {
            statement.bindString(7, reserved2);
        } else {
            statement.bindNull(7);
        }
        String reserved3 = object.getReserved3();
        if (reserved3 != null) {
            statement.bindString(8, reserved3);
        } else {
            statement.bindNull(8);
        }
        String reserved4 = object.getReserved4();
        if (reserved4 != null) {
            statement.bindString(9, reserved4);
        } else {
            statement.bindNull(9);
        }
    }

    public DSSwitchRecord readObject(Cursor cursor, int offset) {
        return new DSSwitchRecord(cursor);
    }

    public void setPrimaryKeyValue(DSSwitchRecord object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DSSwitchRecord object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
