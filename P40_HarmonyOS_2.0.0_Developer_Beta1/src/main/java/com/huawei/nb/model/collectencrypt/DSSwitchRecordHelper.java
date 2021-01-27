package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSSwitchRecordHelper extends AEntityHelper<DSSwitchRecord> {
    private static final DSSwitchRecordHelper INSTANCE = new DSSwitchRecordHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DSSwitchRecord dSSwitchRecord) {
        return null;
    }

    private DSSwitchRecordHelper() {
    }

    public static DSSwitchRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSSwitchRecord dSSwitchRecord) {
        Integer id = dSSwitchRecord.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Long timeStamp = dSSwitchRecord.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(2, timeStamp.longValue());
        } else {
            statement.bindNull(2);
        }
        String switchStatus = dSSwitchRecord.getSwitchStatus();
        if (switchStatus != null) {
            statement.bindString(3, switchStatus);
        } else {
            statement.bindNull(3);
        }
        String switchName = dSSwitchRecord.getSwitchName();
        if (switchName != null) {
            statement.bindString(4, switchName);
        } else {
            statement.bindNull(4);
        }
        String packageName = dSSwitchRecord.getPackageName();
        if (packageName != null) {
            statement.bindString(5, packageName);
        } else {
            statement.bindNull(5);
        }
        String reserved1 = dSSwitchRecord.getReserved1();
        if (reserved1 != null) {
            statement.bindString(6, reserved1);
        } else {
            statement.bindNull(6);
        }
        String reserved2 = dSSwitchRecord.getReserved2();
        if (reserved2 != null) {
            statement.bindString(7, reserved2);
        } else {
            statement.bindNull(7);
        }
        String reserved3 = dSSwitchRecord.getReserved3();
        if (reserved3 != null) {
            statement.bindString(8, reserved3);
        } else {
            statement.bindNull(8);
        }
        String reserved4 = dSSwitchRecord.getReserved4();
        if (reserved4 != null) {
            statement.bindString(9, reserved4);
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DSSwitchRecord readObject(Cursor cursor, int i) {
        return new DSSwitchRecord(cursor);
    }

    public void setPrimaryKeyValue(DSSwitchRecord dSSwitchRecord, long j) {
        dSSwitchRecord.setId(Integer.valueOf((int) j));
    }
}
