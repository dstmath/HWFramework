package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DeviceTokenHelper extends AEntityHelper<DeviceToken> {
    private static final DeviceTokenHelper INSTANCE = new DeviceTokenHelper();

    private DeviceTokenHelper() {
    }

    public static DeviceTokenHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DeviceToken object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String token = object.getToken();
        if (token != null) {
            statement.bindString(2, token);
        } else {
            statement.bindNull(2);
        }
        String reportFlag = object.getReportFlag();
        if (reportFlag != null) {
            statement.bindString(3, reportFlag);
        } else {
            statement.bindNull(3);
        }
        String reportTime = object.getReportTime();
        if (reportTime != null) {
            statement.bindString(4, reportTime);
        } else {
            statement.bindNull(4);
        }
    }

    public DeviceToken readObject(Cursor cursor, int offset) {
        return new DeviceToken(cursor);
    }

    public void setPrimaryKeyValue(DeviceToken object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, DeviceToken object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
