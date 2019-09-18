package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class VisitStatisticHelper extends AEntityHelper<VisitStatistic> {
    private static final VisitStatisticHelper INSTANCE = new VisitStatisticHelper();

    private VisitStatisticHelper() {
    }

    public static VisitStatisticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, VisitStatistic object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer guestId = object.getGuestId();
        if (guestId != null) {
            statement.bindLong(2, (long) guestId.intValue());
        } else {
            statement.bindNull(2);
        }
        Integer hostId = object.getHostId();
        if (hostId != null) {
            statement.bindLong(3, (long) hostId.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer type = object.getType();
        if (type != null) {
            statement.bindLong(4, (long) type.intValue());
        } else {
            statement.bindNull(4);
        }
        Long insertCount = object.getInsertCount();
        if (insertCount != null) {
            statement.bindLong(5, insertCount.longValue());
        } else {
            statement.bindNull(5);
        }
        Long updateCount = object.getUpdateCount();
        if (updateCount != null) {
            statement.bindLong(6, updateCount.longValue());
        } else {
            statement.bindNull(6);
        }
        Long deleteCount = object.getDeleteCount();
        if (deleteCount != null) {
            statement.bindLong(7, deleteCount.longValue());
        } else {
            statement.bindNull(7);
        }
        Long queryCount = object.getQueryCount();
        if (queryCount != null) {
            statement.bindLong(8, queryCount.longValue());
        } else {
            statement.bindNull(8);
        }
        Long subscribeCount = object.getSubscribeCount();
        if (subscribeCount != null) {
            statement.bindLong(9, subscribeCount.longValue());
        } else {
            statement.bindNull(9);
        }
        Long updateTime = object.getUpdateTime();
        if (updateTime != null) {
            statement.bindLong(10, updateTime.longValue());
        } else {
            statement.bindNull(10);
        }
        Long uploadTime = object.getUploadTime();
        if (uploadTime != null) {
            statement.bindLong(11, uploadTime.longValue());
        } else {
            statement.bindNull(11);
        }
    }

    public VisitStatistic readObject(Cursor cursor, int offset) {
        return new VisitStatistic(cursor);
    }

    public void setPrimaryKeyValue(VisitStatistic object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, VisitStatistic object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
