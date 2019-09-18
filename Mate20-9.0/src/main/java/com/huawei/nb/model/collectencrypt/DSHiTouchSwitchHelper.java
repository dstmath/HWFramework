package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSHiTouchSwitchHelper extends AEntityHelper<DSHiTouchSwitch> {
    private static final DSHiTouchSwitchHelper INSTANCE = new DSHiTouchSwitchHelper();

    private DSHiTouchSwitchHelper() {
    }

    public static DSHiTouchSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSHiTouchSwitch object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Long timestamp = object.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(2, timestamp.longValue());
        } else {
            statement.bindNull(2);
        }
        Integer digest = object.getDigest();
        if (digest != null) {
            statement.bindLong(3, (long) digest.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer express = object.getExpress();
        if (express != null) {
            statement.bindLong(4, (long) express.intValue());
        } else {
            statement.bindNull(4);
        }
        String reserved0 = object.getReserved0();
        if (reserved0 != null) {
            statement.bindString(5, reserved0);
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
        String reserved5 = object.getReserved5();
        if (reserved5 != null) {
            statement.bindString(10, reserved5);
        } else {
            statement.bindNull(10);
        }
    }

    public DSHiTouchSwitch readObject(Cursor cursor, int offset) {
        return new DSHiTouchSwitch(cursor);
    }

    public void setPrimaryKeyValue(DSHiTouchSwitch object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DSHiTouchSwitch object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
