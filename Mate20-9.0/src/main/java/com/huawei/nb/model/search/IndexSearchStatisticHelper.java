package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class IndexSearchStatisticHelper extends AEntityHelper<IndexSearchStatistic> {
    private static final IndexSearchStatisticHelper INSTANCE = new IndexSearchStatisticHelper();

    private IndexSearchStatisticHelper() {
    }

    public static IndexSearchStatisticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndexSearchStatistic object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer appId = object.getAppId();
        if (appId != null) {
            statement.bindLong(2, (long) appId.intValue());
        } else {
            statement.bindNull(2);
        }
        Integer eventType = object.getEventType();
        if (eventType != null) {
            statement.bindLong(3, (long) eventType.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer operateType = object.getOperateType();
        if (operateType != null) {
            statement.bindLong(4, (long) operateType.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer eventNum = object.getEventNum();
        if (eventNum != null) {
            statement.bindLong(5, (long) eventNum.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer maxExecTime = object.getMaxExecTime();
        if (maxExecTime != null) {
            statement.bindLong(6, (long) maxExecTime.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer minExecTime = object.getMinExecTime();
        if (minExecTime != null) {
            statement.bindLong(7, (long) minExecTime.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer avgExecTime = object.getAvgExecTime();
        if (avgExecTime != null) {
            statement.bindLong(8, (long) avgExecTime.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer indexDatabaseSize = object.getIndexDatabaseSize();
        if (indexDatabaseSize != null) {
            statement.bindLong(9, (long) indexDatabaseSize.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer lifeTime = object.getLifeTime();
        if (lifeTime != null) {
            statement.bindLong(10, (long) lifeTime.intValue());
        } else {
            statement.bindNull(10);
        }
        String description = object.getDescription();
        if (description != null) {
            statement.bindString(11, description);
        } else {
            statement.bindNull(11);
        }
        Long reportTime = object.getReportTime();
        if (reportTime != null) {
            statement.bindLong(12, reportTime.longValue());
        } else {
            statement.bindNull(12);
        }
        Boolean hasReported = object.getHasReported();
        if (hasReported != null) {
            statement.bindLong(13, hasReported.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(13);
        }
    }

    public IndexSearchStatistic readObject(Cursor cursor, int offset) {
        return new IndexSearchStatistic(cursor);
    }

    public void setPrimaryKeyValue(IndexSearchStatistic object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, IndexSearchStatistic object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
