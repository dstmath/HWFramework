package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessMsgHelper extends AEntityHelper<BusinessMsg> {
    private static final BusinessMsgHelper INSTANCE = new BusinessMsgHelper();

    private BusinessMsgHelper() {
    }

    public static BusinessMsgHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, BusinessMsg object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String service_id = object.getService_id();
        if (service_id != null) {
            statement.bindString(2, service_id);
        } else {
            statement.bindNull(2);
        }
        String msg_type = object.getMsg_type();
        if (msg_type != null) {
            statement.bindString(3, msg_type);
        } else {
            statement.bindNull(3);
        }
        String params = object.getParams();
        if (params != null) {
            statement.bindString(4, params);
        } else {
            statement.bindNull(4);
        }
    }

    public BusinessMsg readObject(Cursor cursor, int offset) {
        return new BusinessMsg(cursor);
    }

    public void setPrimaryKeyValue(BusinessMsg object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, BusinessMsg object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
