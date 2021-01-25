package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UpOnceTaskHistoryHelper extends AEntityHelper<UpOnceTaskHistory> {
    private static final UpOnceTaskHistoryHelper INSTANCE = new UpOnceTaskHistoryHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, UpOnceTaskHistory upOnceTaskHistory) {
        return null;
    }

    private UpOnceTaskHistoryHelper() {
    }

    public static UpOnceTaskHistoryHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UpOnceTaskHistory upOnceTaskHistory) {
        Integer hisId = upOnceTaskHistory.getHisId();
        if (hisId != null) {
            statement.bindLong(1, (long) hisId.intValue());
        } else {
            statement.bindNull(1);
        }
        String taskName = upOnceTaskHistory.getTaskName();
        if (taskName != null) {
            statement.bindString(2, taskName);
        } else {
            statement.bindNull(2);
        }
        Integer version = upOnceTaskHistory.getVersion();
        if (version != null) {
            statement.bindLong(3, (long) version.intValue());
        } else {
            statement.bindNull(3);
        }
        String taskType = upOnceTaskHistory.getTaskType();
        if (taskType != null) {
            statement.bindString(4, taskType);
        } else {
            statement.bindNull(4);
        }
        Long executeTime = upOnceTaskHistory.getExecuteTime();
        if (executeTime != null) {
            statement.bindLong(5, executeTime.longValue());
        } else {
            statement.bindNull(5);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public UpOnceTaskHistory readObject(Cursor cursor, int i) {
        return new UpOnceTaskHistory(cursor);
    }

    public void setPrimaryKeyValue(UpOnceTaskHistory upOnceTaskHistory, long j) {
        upOnceTaskHistory.setHisId(Integer.valueOf((int) j));
    }
}
