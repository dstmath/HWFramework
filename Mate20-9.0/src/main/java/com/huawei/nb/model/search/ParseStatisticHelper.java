package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ParseStatisticHelper extends AEntityHelper<ParseStatistic> {
    private static final ParseStatisticHelper INSTANCE = new ParseStatisticHelper();

    private ParseStatisticHelper() {
    }

    public static ParseStatisticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ParseStatistic object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String docType = object.getDocType();
        if (docType != null) {
            statement.bindString(2, docType);
        } else {
            statement.bindNull(2);
        }
        Integer fileNum = object.getFileNum();
        if (fileNum != null) {
            statement.bindLong(3, (long) fileNum.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer maxExecTime = object.getMaxExecTime();
        if (maxExecTime != null) {
            statement.bindLong(4, (long) maxExecTime.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer minExecTime = object.getMinExecTime();
        if (minExecTime != null) {
            statement.bindLong(5, (long) minExecTime.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer avgExecTime = object.getAvgExecTime();
        if (avgExecTime != null) {
            statement.bindLong(6, (long) avgExecTime.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer maxFileSize = object.getMaxFileSize();
        if (maxFileSize != null) {
            statement.bindLong(7, (long) maxFileSize.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer avgFileSize = object.getAvgFileSize();
        if (avgFileSize != null) {
            statement.bindLong(8, (long) avgFileSize.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer maxFileDepth = object.getMaxFileDepth();
        if (maxFileDepth != null) {
            statement.bindLong(9, (long) maxFileDepth.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer avgFileDepth = object.getAvgFileDepth();
        if (avgFileDepth != null) {
            statement.bindLong(10, (long) avgFileDepth.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer lifeTime = object.getLifeTime();
        if (lifeTime != null) {
            statement.bindLong(11, (long) lifeTime.intValue());
        } else {
            statement.bindNull(11);
        }
        String description = object.getDescription();
        if (description != null) {
            statement.bindString(12, description);
        } else {
            statement.bindNull(12);
        }
        Long reportTime = object.getReportTime();
        if (reportTime != null) {
            statement.bindLong(13, reportTime.longValue());
        } else {
            statement.bindNull(13);
        }
        Boolean hasReported = object.getHasReported();
        if (hasReported != null) {
            statement.bindLong(14, hasReported.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(14);
        }
    }

    public ParseStatistic readObject(Cursor cursor, int offset) {
        return new ParseStatistic(cursor);
    }

    public void setPrimaryKeyValue(ParseStatistic object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, ParseStatistic object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
