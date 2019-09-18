package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CapJobInstanceHelper extends AEntityHelper<CapJobInstance> {
    private static final CapJobInstanceHelper INSTANCE = new CapJobInstanceHelper();

    private CapJobInstanceHelper() {
    }

    public static CapJobInstanceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CapJobInstance object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String type = object.getType();
        if (type != null) {
            statement.bindString(2, type);
        } else {
            statement.bindNull(2);
        }
        String status = object.getStatus();
        if (status != null) {
            statement.bindString(3, status);
        } else {
            statement.bindNull(3);
        }
        String tasks = object.getTasks();
        if (tasks != null) {
            statement.bindString(4, tasks);
        } else {
            statement.bindNull(4);
        }
        String result = object.getResult();
        if (result != null) {
            statement.bindString(5, result);
        } else {
            statement.bindNull(5);
        }
        String resultDesc = object.getResultDesc();
        if (resultDesc != null) {
            statement.bindString(6, resultDesc);
        } else {
            statement.bindNull(6);
        }
        Long analyzeTime = object.getAnalyzeTime();
        if (analyzeTime != null) {
            statement.bindLong(7, analyzeTime.longValue());
        } else {
            statement.bindNull(7);
        }
        Long createTime = object.getCreateTime();
        if (createTime != null) {
            statement.bindLong(8, createTime.longValue());
        } else {
            statement.bindNull(8);
        }
        Long lastModifyTime = object.getLastModifyTime();
        if (lastModifyTime != null) {
            statement.bindLong(9, lastModifyTime.longValue());
        } else {
            statement.bindNull(9);
        }
    }

    public CapJobInstance readObject(Cursor cursor, int offset) {
        return new CapJobInstance(cursor);
    }

    public void setPrimaryKeyValue(CapJobInstance object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, CapJobInstance object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
