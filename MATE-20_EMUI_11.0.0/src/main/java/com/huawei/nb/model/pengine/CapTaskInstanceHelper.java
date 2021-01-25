package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CapTaskInstanceHelper extends AEntityHelper<CapTaskInstance> {
    private static final CapTaskInstanceHelper INSTANCE = new CapTaskInstanceHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CapTaskInstance capTaskInstance) {
        return null;
    }

    private CapTaskInstanceHelper() {
    }

    public static CapTaskInstanceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CapTaskInstance capTaskInstance) {
        Integer taskId = capTaskInstance.getTaskId();
        if (taskId != null) {
            statement.bindLong(1, (long) taskId.intValue());
        } else {
            statement.bindNull(1);
        }
        Long jobId = capTaskInstance.getJobId();
        if (jobId != null) {
            statement.bindLong(2, jobId.longValue());
        } else {
            statement.bindNull(2);
        }
        String taskName = capTaskInstance.getTaskName();
        if (taskName != null) {
            statement.bindString(3, taskName);
        } else {
            statement.bindNull(3);
        }
        String status = capTaskInstance.getStatus();
        if (status != null) {
            statement.bindString(4, status);
        } else {
            statement.bindNull(4);
        }
        String result = capTaskInstance.getResult();
        if (result != null) {
            statement.bindString(5, result);
        } else {
            statement.bindNull(5);
        }
        String resultDesc = capTaskInstance.getResultDesc();
        if (resultDesc != null) {
            statement.bindString(6, resultDesc);
        } else {
            statement.bindNull(6);
        }
        Long createTime = capTaskInstance.getCreateTime();
        if (createTime != null) {
            statement.bindLong(7, createTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Long lastModifyTime = capTaskInstance.getLastModifyTime();
        if (lastModifyTime != null) {
            statement.bindLong(8, lastModifyTime.longValue());
        } else {
            statement.bindNull(8);
        }
        String attrs = capTaskInstance.getAttrs();
        if (attrs != null) {
            statement.bindString(9, attrs);
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CapTaskInstance readObject(Cursor cursor, int i) {
        return new CapTaskInstance(cursor);
    }

    public void setPrimaryKeyValue(CapTaskInstance capTaskInstance, long j) {
        capTaskInstance.setTaskId(Integer.valueOf((int) j));
    }
}
