package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorAuditHelper extends AEntityHelper<CoordinatorAudit> {
    private static final CoordinatorAuditHelper INSTANCE = new CoordinatorAuditHelper();

    private CoordinatorAuditHelper() {
    }

    public static CoordinatorAuditHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CoordinatorAudit object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String appPackageName = object.getAppPackageName();
        if (appPackageName != null) {
            statement.bindString(2, appPackageName);
        } else {
            statement.bindNull(2);
        }
        String url = object.getUrl();
        if (url != null) {
            statement.bindString(3, url);
        } else {
            statement.bindNull(3);
        }
        String netWorkState = object.getNetWorkState();
        if (netWorkState != null) {
            statement.bindString(4, netWorkState);
        } else {
            statement.bindNull(4);
        }
        Long timeStamp = object.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(5, timeStamp.longValue());
        } else {
            statement.bindNull(5);
        }
        Long isNeedRetry = object.getIsNeedRetry();
        if (isNeedRetry != null) {
            statement.bindLong(6, isNeedRetry.longValue());
        } else {
            statement.bindNull(6);
        }
        Long successVerifyTime = object.getSuccessVerifyTime();
        if (successVerifyTime != null) {
            statement.bindLong(7, successVerifyTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Long successTransferTime = object.getSuccessTransferTime();
        if (successTransferTime != null) {
            statement.bindLong(8, successTransferTime.longValue());
        } else {
            statement.bindNull(8);
        }
        Long dataSize = object.getDataSize();
        if (dataSize != null) {
            statement.bindLong(9, dataSize.longValue());
        } else {
            statement.bindNull(9);
        }
        String requestDate = object.getRequestDate();
        if (requestDate != null) {
            statement.bindString(10, requestDate);
        } else {
            statement.bindNull(10);
        }
        statement.bindLong(11, object.getIsRequestSuccess() ? 1 : 0);
    }

    public CoordinatorAudit readObject(Cursor cursor, int offset) {
        return new CoordinatorAudit(cursor);
    }

    public void setPrimaryKeyValue(CoordinatorAudit object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, CoordinatorAudit object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
