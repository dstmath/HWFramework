package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UpTaskInstanceHelper extends AEntityHelper<UpTaskInstance> {
    private static final UpTaskInstanceHelper INSTANCE = new UpTaskInstanceHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, UpTaskInstance upTaskInstance) {
        return null;
    }

    private UpTaskInstanceHelper() {
    }

    public static UpTaskInstanceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UpTaskInstance upTaskInstance) {
        Integer taskId = upTaskInstance.getTaskId();
        if (taskId != null) {
            statement.bindLong(1, (long) taskId.intValue());
        } else {
            statement.bindNull(1);
        }
        Long jobId = upTaskInstance.getJobId();
        if (jobId != null) {
            statement.bindLong(2, jobId.longValue());
        } else {
            statement.bindNull(2);
        }
        String taskName = upTaskInstance.getTaskName();
        if (taskName != null) {
            statement.bindString(3, taskName);
        } else {
            statement.bindNull(3);
        }
        String status = upTaskInstance.getStatus();
        if (status != null) {
            statement.bindString(4, status);
        } else {
            statement.bindNull(4);
        }
        String result = upTaskInstance.getResult();
        if (result != null) {
            statement.bindString(5, result);
        } else {
            statement.bindNull(5);
        }
        String resultDesc = upTaskInstance.getResultDesc();
        if (resultDesc != null) {
            statement.bindString(6, resultDesc);
        } else {
            statement.bindNull(6);
        }
        Long createTime = upTaskInstance.getCreateTime();
        if (createTime != null) {
            statement.bindLong(7, createTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Long lastModifyTime = upTaskInstance.getLastModifyTime();
        if (lastModifyTime != null) {
            statement.bindLong(8, lastModifyTime.longValue());
        } else {
            statement.bindNull(8);
        }
        String attrs = upTaskInstance.getAttrs();
        if (attrs != null) {
            statement.bindString(9, attrs);
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public UpTaskInstance readObject(Cursor cursor, int i) {
        return new UpTaskInstance(cursor);
    }

    public void setPrimaryKeyValue(UpTaskInstance upTaskInstance, long j) {
        upTaskInstance.setTaskId(Integer.valueOf((int) j));
    }
}
