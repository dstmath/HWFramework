package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSHiTouchPhoneTokenHelper extends AEntityHelper<DSHiTouchPhoneToken> {
    private static final DSHiTouchPhoneTokenHelper INSTANCE = new DSHiTouchPhoneTokenHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DSHiTouchPhoneToken dSHiTouchPhoneToken) {
        return null;
    }

    private DSHiTouchPhoneTokenHelper() {
    }

    public static DSHiTouchPhoneTokenHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSHiTouchPhoneToken dSHiTouchPhoneToken) {
        Integer id = dSHiTouchPhoneToken.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Long timestamp = dSHiTouchPhoneToken.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(2, timestamp.longValue());
        } else {
            statement.bindNull(2);
        }
        Integer compat = dSHiTouchPhoneToken.getCompat();
        if (compat != null) {
            statement.bindLong(3, (long) compat.intValue());
        } else {
            statement.bindNull(3);
        }
        String tokencodes = dSHiTouchPhoneToken.getTokencodes();
        if (tokencodes != null) {
            statement.bindString(4, tokencodes);
        } else {
            statement.bindNull(4);
        }
        String reserved0 = dSHiTouchPhoneToken.getReserved0();
        if (reserved0 != null) {
            statement.bindString(5, reserved0);
        } else {
            statement.bindNull(5);
        }
        String reserved1 = dSHiTouchPhoneToken.getReserved1();
        if (reserved1 != null) {
            statement.bindString(6, reserved1);
        } else {
            statement.bindNull(6);
        }
        String reserved2 = dSHiTouchPhoneToken.getReserved2();
        if (reserved2 != null) {
            statement.bindString(7, reserved2);
        } else {
            statement.bindNull(7);
        }
        String reserved3 = dSHiTouchPhoneToken.getReserved3();
        if (reserved3 != null) {
            statement.bindString(8, reserved3);
        } else {
            statement.bindNull(8);
        }
        String reserved4 = dSHiTouchPhoneToken.getReserved4();
        if (reserved4 != null) {
            statement.bindString(9, reserved4);
        } else {
            statement.bindNull(9);
        }
        String reserved5 = dSHiTouchPhoneToken.getReserved5();
        if (reserved5 != null) {
            statement.bindString(10, reserved5);
        } else {
            statement.bindNull(10);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DSHiTouchPhoneToken readObject(Cursor cursor, int i) {
        return new DSHiTouchPhoneToken(cursor);
    }

    public void setPrimaryKeyValue(DSHiTouchPhoneToken dSHiTouchPhoneToken, long j) {
        dSHiTouchPhoneToken.setId(Integer.valueOf((int) j));
    }
}
