package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ServiceSwitchHelper extends AEntityHelper<ServiceSwitch> {
    private static final ServiceSwitchHelper INSTANCE = new ServiceSwitchHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ServiceSwitch serviceSwitch) {
        return null;
    }

    private ServiceSwitchHelper() {
    }

    public static ServiceSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ServiceSwitch serviceSwitch) {
        Long id = serviceSwitch.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Integer num = serviceSwitch.getSwitch();
        if (num != null) {
            statement.bindLong(2, (long) num.intValue());
        } else {
            statement.bindNull(2);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ServiceSwitch readObject(Cursor cursor, int i) {
        return new ServiceSwitch(cursor);
    }

    public void setPrimaryKeyValue(ServiceSwitch serviceSwitch, long j) {
        serviceSwitch.setId(Long.valueOf(j));
    }
}
