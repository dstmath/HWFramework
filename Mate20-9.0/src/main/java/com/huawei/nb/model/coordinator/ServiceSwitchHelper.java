package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ServiceSwitchHelper extends AEntityHelper<ServiceSwitch> {
    private static final ServiceSwitchHelper INSTANCE = new ServiceSwitchHelper();

    private ServiceSwitchHelper() {
    }

    public static ServiceSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ServiceSwitch object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Integer _switch = object.getSwitch();
        if (_switch != null) {
            statement.bindLong(2, (long) _switch.intValue());
        } else {
            statement.bindNull(2);
        }
    }

    public ServiceSwitch readObject(Cursor cursor, int offset) {
        return new ServiceSwitch(cursor);
    }

    public void setPrimaryKeyValue(ServiceSwitch object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ServiceSwitch object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
