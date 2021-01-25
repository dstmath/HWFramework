package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PresetJobHelper extends AEntityHelper<PresetJob> {
    private static final PresetJobHelper INSTANCE = new PresetJobHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, PresetJob presetJob) {
        return null;
    }

    private PresetJobHelper() {
    }

    public static PresetJobHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PresetJob presetJob) {
        Integer mId = presetJob.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String jobName = presetJob.getJobName();
        if (jobName != null) {
            statement.bindString(2, jobName);
        } else {
            statement.bindNull(2);
        }
        String parameter = presetJob.getParameter();
        if (parameter != null) {
            statement.bindString(3, parameter);
        } else {
            statement.bindNull(3);
        }
        Integer scheduleType = presetJob.getScheduleType();
        if (scheduleType != null) {
            statement.bindLong(4, (long) scheduleType.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer taskType = presetJob.getTaskType();
        if (taskType != null) {
            statement.bindLong(5, (long) taskType.intValue());
        } else {
            statement.bindNull(5);
        }
        String jobInfo = presetJob.getJobInfo();
        if (jobInfo != null) {
            statement.bindString(6, jobInfo);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public PresetJob readObject(Cursor cursor, int i) {
        return new PresetJob(cursor);
    }

    public void setPrimaryKeyValue(PresetJob presetJob, long j) {
        presetJob.setMId(Integer.valueOf((int) j));
    }
}
