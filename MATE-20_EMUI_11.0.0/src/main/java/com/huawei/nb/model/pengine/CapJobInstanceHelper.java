package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CapJobInstanceHelper extends AEntityHelper<CapJobInstance> {
    private static final CapJobInstanceHelper INSTANCE = new CapJobInstanceHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CapJobInstance capJobInstance) {
        return null;
    }

    private CapJobInstanceHelper() {
    }

    public static CapJobInstanceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CapJobInstance capJobInstance) {
        Integer id = capJobInstance.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String type = capJobInstance.getType();
        if (type != null) {
            statement.bindString(2, type);
        } else {
            statement.bindNull(2);
        }
        String status = capJobInstance.getStatus();
        if (status != null) {
            statement.bindString(3, status);
        } else {
            statement.bindNull(3);
        }
        String tasks = capJobInstance.getTasks();
        if (tasks != null) {
            statement.bindString(4, tasks);
        } else {
            statement.bindNull(4);
        }
        String result = capJobInstance.getResult();
        if (result != null) {
            statement.bindString(5, result);
        } else {
            statement.bindNull(5);
        }
        String resultDesc = capJobInstance.getResultDesc();
        if (resultDesc != null) {
            statement.bindString(6, resultDesc);
        } else {
            statement.bindNull(6);
        }
        Long analyzeTime = capJobInstance.getAnalyzeTime();
        if (analyzeTime != null) {
            statement.bindLong(7, analyzeTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Long createTime = capJobInstance.getCreateTime();
        if (createTime != null) {
            statement.bindLong(8, createTime.longValue());
        } else {
            statement.bindNull(8);
        }
        Long lastModifyTime = capJobInstance.getLastModifyTime();
        if (lastModifyTime != null) {
            statement.bindLong(9, lastModifyTime.longValue());
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CapJobInstance readObject(Cursor cursor, int i) {
        return new CapJobInstance(cursor);
    }

    public void setPrimaryKeyValue(CapJobInstance capJobInstance, long j) {
        capJobInstance.setId(Integer.valueOf((int) j));
    }
}
