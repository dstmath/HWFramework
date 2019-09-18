package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PresetJobHelper extends AEntityHelper<PresetJob> {
    private static final PresetJobHelper INSTANCE = new PresetJobHelper();

    private PresetJobHelper() {
    }

    public static PresetJobHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PresetJob object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String jobName = object.getJobName();
        if (jobName != null) {
            statement.bindString(2, jobName);
        } else {
            statement.bindNull(2);
        }
        String parameter = object.getParameter();
        if (parameter != null) {
            statement.bindString(3, parameter);
        } else {
            statement.bindNull(3);
        }
        Integer scheduleType = object.getScheduleType();
        if (scheduleType != null) {
            statement.bindLong(4, (long) scheduleType.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer taskType = object.getTaskType();
        if (taskType != null) {
            statement.bindLong(5, (long) taskType.intValue());
        } else {
            statement.bindNull(5);
        }
        String jobInfo = object.getJobInfo();
        if (jobInfo != null) {
            statement.bindString(6, jobInfo);
        } else {
            statement.bindNull(6);
        }
    }

    public PresetJob readObject(Cursor cursor, int offset) {
        return new PresetJob(cursor);
    }

    public void setPrimaryKeyValue(PresetJob object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, PresetJob object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
