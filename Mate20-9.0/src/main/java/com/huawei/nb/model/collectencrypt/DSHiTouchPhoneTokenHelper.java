package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSHiTouchPhoneTokenHelper extends AEntityHelper<DSHiTouchPhoneToken> {
    private static final DSHiTouchPhoneTokenHelper INSTANCE = new DSHiTouchPhoneTokenHelper();

    private DSHiTouchPhoneTokenHelper() {
    }

    public static DSHiTouchPhoneTokenHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSHiTouchPhoneToken object) {
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
        Integer compat = object.getCompat();
        if (compat != null) {
            statement.bindLong(3, (long) compat.intValue());
        } else {
            statement.bindNull(3);
        }
        String tokencodes = object.getTokencodes();
        if (tokencodes != null) {
            statement.bindString(4, tokencodes);
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

    public DSHiTouchPhoneToken readObject(Cursor cursor, int offset) {
        return new DSHiTouchPhoneToken(cursor);
    }

    public void setPrimaryKeyValue(DSHiTouchPhoneToken object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DSHiTouchPhoneToken object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
