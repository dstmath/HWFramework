package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSHiTouchSwitchHelper extends AEntityHelper<DSHiTouchSwitch> {
    private static final DSHiTouchSwitchHelper INSTANCE = new DSHiTouchSwitchHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DSHiTouchSwitch dSHiTouchSwitch) {
        return null;
    }

    private DSHiTouchSwitchHelper() {
    }

    public static DSHiTouchSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSHiTouchSwitch dSHiTouchSwitch) {
        Integer id = dSHiTouchSwitch.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Long timestamp = dSHiTouchSwitch.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(2, timestamp.longValue());
        } else {
            statement.bindNull(2);
        }
        Integer digest = dSHiTouchSwitch.getDigest();
        if (digest != null) {
            statement.bindLong(3, (long) digest.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer express = dSHiTouchSwitch.getExpress();
        if (express != null) {
            statement.bindLong(4, (long) express.intValue());
        } else {
            statement.bindNull(4);
        }
        String reserved0 = dSHiTouchSwitch.getReserved0();
        if (reserved0 != null) {
            statement.bindString(5, reserved0);
        } else {
            statement.bindNull(5);
        }
        String reserved1 = dSHiTouchSwitch.getReserved1();
        if (reserved1 != null) {
            statement.bindString(6, reserved1);
        } else {
            statement.bindNull(6);
        }
        String reserved2 = dSHiTouchSwitch.getReserved2();
        if (reserved2 != null) {
            statement.bindString(7, reserved2);
        } else {
            statement.bindNull(7);
        }
        String reserved3 = dSHiTouchSwitch.getReserved3();
        if (reserved3 != null) {
            statement.bindString(8, reserved3);
        } else {
            statement.bindNull(8);
        }
        String reserved4 = dSHiTouchSwitch.getReserved4();
        if (reserved4 != null) {
            statement.bindString(9, reserved4);
        } else {
            statement.bindNull(9);
        }
        String reserved5 = dSHiTouchSwitch.getReserved5();
        if (reserved5 != null) {
            statement.bindString(10, reserved5);
        } else {
            statement.bindNull(10);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DSHiTouchSwitch readObject(Cursor cursor, int i) {
        return new DSHiTouchSwitch(cursor);
    }

    public void setPrimaryKeyValue(DSHiTouchSwitch dSHiTouchSwitch, long j) {
        dSHiTouchSwitch.setId(Integer.valueOf((int) j));
    }
}
