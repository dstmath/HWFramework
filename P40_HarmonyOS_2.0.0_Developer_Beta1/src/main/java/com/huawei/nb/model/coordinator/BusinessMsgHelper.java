package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessMsgHelper extends AEntityHelper<BusinessMsg> {
    private static final BusinessMsgHelper INSTANCE = new BusinessMsgHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, BusinessMsg businessMsg) {
        return null;
    }

    private BusinessMsgHelper() {
    }

    public static BusinessMsgHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, BusinessMsg businessMsg) {
        Long id = businessMsg.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String service_id = businessMsg.getService_id();
        if (service_id != null) {
            statement.bindString(2, service_id);
        } else {
            statement.bindNull(2);
        }
        String msg_type = businessMsg.getMsg_type();
        if (msg_type != null) {
            statement.bindString(3, msg_type);
        } else {
            statement.bindNull(3);
        }
        String params = businessMsg.getParams();
        if (params != null) {
            statement.bindString(4, params);
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public BusinessMsg readObject(Cursor cursor, int i) {
        return new BusinessMsg(cursor);
    }

    public void setPrimaryKeyValue(BusinessMsg businessMsg, long j) {
        businessMsg.setId(Long.valueOf(j));
    }
}
