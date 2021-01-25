package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DeviceTokenHelper extends AEntityHelper<DeviceToken> {
    private static final DeviceTokenHelper INSTANCE = new DeviceTokenHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DeviceToken deviceToken) {
        return null;
    }

    private DeviceTokenHelper() {
    }

    public static DeviceTokenHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DeviceToken deviceToken) {
        Long id = deviceToken.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String token = deviceToken.getToken();
        if (token != null) {
            statement.bindString(2, token);
        } else {
            statement.bindNull(2);
        }
        String reportFlag = deviceToken.getReportFlag();
        if (reportFlag != null) {
            statement.bindString(3, reportFlag);
        } else {
            statement.bindNull(3);
        }
        String reportTime = deviceToken.getReportTime();
        if (reportTime != null) {
            statement.bindString(4, reportTime);
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DeviceToken readObject(Cursor cursor, int i) {
        return new DeviceToken(cursor);
    }

    public void setPrimaryKeyValue(DeviceToken deviceToken, long j) {
        deviceToken.setId(Long.valueOf(j));
    }
}
