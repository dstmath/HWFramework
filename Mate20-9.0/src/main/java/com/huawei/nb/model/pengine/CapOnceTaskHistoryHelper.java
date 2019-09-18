package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CapOnceTaskHistoryHelper extends AEntityHelper<CapOnceTaskHistory> {
    private static final CapOnceTaskHistoryHelper INSTANCE = new CapOnceTaskHistoryHelper();

    private CapOnceTaskHistoryHelper() {
    }

    public static CapOnceTaskHistoryHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CapOnceTaskHistory object) {
        Integer hisId = object.getHisId();
        if (hisId != null) {
            statement.bindLong(1, (long) hisId.intValue());
        } else {
            statement.bindNull(1);
        }
        String taskName = object.getTaskName();
        if (taskName != null) {
            statement.bindString(2, taskName);
        } else {
            statement.bindNull(2);
        }
        Integer version = object.getVersion();
        if (version != null) {
            statement.bindLong(3, (long) version.intValue());
        } else {
            statement.bindNull(3);
        }
        String taskType = object.getTaskType();
        if (taskType != null) {
            statement.bindString(4, taskType);
        } else {
            statement.bindNull(4);
        }
        Long executeTime = object.getExecuteTime();
        if (executeTime != null) {
            statement.bindLong(5, executeTime.longValue());
        } else {
            statement.bindNull(5);
        }
    }

    public CapOnceTaskHistory readObject(Cursor cursor, int offset) {
        return new CapOnceTaskHistory(cursor);
    }

    public void setPrimaryKeyValue(CapOnceTaskHistory object, long value) {
        object.setHisId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, CapOnceTaskHistory object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
