package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class SearchTaskItemHelper extends AEntityHelper<SearchTaskItem> {
    private static final SearchTaskItemHelper INSTANCE = new SearchTaskItemHelper();

    private SearchTaskItemHelper() {
    }

    public static SearchTaskItemHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, SearchTaskItem object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String pkgName = object.getPkgName();
        if (pkgName != null) {
            statement.bindString(2, pkgName);
        } else {
            statement.bindNull(2);
        }
        String ids = object.getIds();
        if (ids != null) {
            statement.bindString(3, ids);
        } else {
            statement.bindNull(3);
        }
        Integer op = object.getOp();
        if (op != null) {
            statement.bindLong(4, (long) op.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer userId = object.getUserId();
        if (userId != null) {
            statement.bindLong(5, (long) userId.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer type = object.getType();
        if (type != null) {
            statement.bindLong(6, (long) type.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer runTimes = object.getRunTimes();
        if (runTimes != null) {
            statement.bindLong(7, (long) runTimes.intValue());
        } else {
            statement.bindNull(7);
        }
        Boolean isCrawlContent = object.getIsCrawlContent();
        if (isCrawlContent != null) {
            statement.bindLong(8, isCrawlContent.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(8);
        }
        Integer priority = object.getPriority();
        if (priority != null) {
            statement.bindLong(9, (long) priority.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer status = object.getStatus();
        if (status != null) {
            statement.bindLong(10, (long) status.intValue());
        } else {
            statement.bindNull(10);
        }
        Long startTime = object.getStartTime();
        if (startTime != null) {
            statement.bindLong(11, startTime.longValue());
        } else {
            statement.bindNull(11);
        }
    }

    public SearchTaskItem readObject(Cursor cursor, int offset) {
        return new SearchTaskItem(cursor);
    }

    public void setPrimaryKeyValue(SearchTaskItem object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, SearchTaskItem object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
