package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorAuditHelper extends AEntityHelper<CoordinatorAudit> {
    private static final CoordinatorAuditHelper INSTANCE = new CoordinatorAuditHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CoordinatorAudit coordinatorAudit) {
        return null;
    }

    private CoordinatorAuditHelper() {
    }

    public static CoordinatorAuditHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CoordinatorAudit coordinatorAudit) {
        Integer id = coordinatorAudit.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String appPackageName = coordinatorAudit.getAppPackageName();
        if (appPackageName != null) {
            statement.bindString(2, appPackageName);
        } else {
            statement.bindNull(2);
        }
        String url = coordinatorAudit.getUrl();
        if (url != null) {
            statement.bindString(3, url);
        } else {
            statement.bindNull(3);
        }
        String netWorkState = coordinatorAudit.getNetWorkState();
        if (netWorkState != null) {
            statement.bindString(4, netWorkState);
        } else {
            statement.bindNull(4);
        }
        Long timeStamp = coordinatorAudit.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(5, timeStamp.longValue());
        } else {
            statement.bindNull(5);
        }
        Long isNeedRetry = coordinatorAudit.getIsNeedRetry();
        if (isNeedRetry != null) {
            statement.bindLong(6, isNeedRetry.longValue());
        } else {
            statement.bindNull(6);
        }
        Long successVerifyTime = coordinatorAudit.getSuccessVerifyTime();
        if (successVerifyTime != null) {
            statement.bindLong(7, successVerifyTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Long successTransferTime = coordinatorAudit.getSuccessTransferTime();
        if (successTransferTime != null) {
            statement.bindLong(8, successTransferTime.longValue());
        } else {
            statement.bindNull(8);
        }
        Long dataSize = coordinatorAudit.getDataSize();
        if (dataSize != null) {
            statement.bindLong(9, dataSize.longValue());
        } else {
            statement.bindNull(9);
        }
        String requestDate = coordinatorAudit.getRequestDate();
        if (requestDate != null) {
            statement.bindString(10, requestDate);
        } else {
            statement.bindNull(10);
        }
        statement.bindLong(11, coordinatorAudit.getIsRequestSuccess() ? 1 : 0);
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CoordinatorAudit readObject(Cursor cursor, int i) {
        return new CoordinatorAudit(cursor);
    }

    public void setPrimaryKeyValue(CoordinatorAudit coordinatorAudit, long j) {
        coordinatorAudit.setId(Integer.valueOf((int) j));
    }
}
