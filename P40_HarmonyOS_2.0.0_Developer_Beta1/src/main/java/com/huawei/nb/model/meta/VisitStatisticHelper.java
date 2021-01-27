package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class VisitStatisticHelper extends AEntityHelper<VisitStatistic> {
    private static final VisitStatisticHelper INSTANCE = new VisitStatisticHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, VisitStatistic visitStatistic) {
        return null;
    }

    private VisitStatisticHelper() {
    }

    public static VisitStatisticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, VisitStatistic visitStatistic) {
        Integer id = visitStatistic.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer guestId = visitStatistic.getGuestId();
        if (guestId != null) {
            statement.bindLong(2, (long) guestId.intValue());
        } else {
            statement.bindNull(2);
        }
        Integer hostId = visitStatistic.getHostId();
        if (hostId != null) {
            statement.bindLong(3, (long) hostId.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer type = visitStatistic.getType();
        if (type != null) {
            statement.bindLong(4, (long) type.intValue());
        } else {
            statement.bindNull(4);
        }
        Long insertCount = visitStatistic.getInsertCount();
        if (insertCount != null) {
            statement.bindLong(5, insertCount.longValue());
        } else {
            statement.bindNull(5);
        }
        Long updateCount = visitStatistic.getUpdateCount();
        if (updateCount != null) {
            statement.bindLong(6, updateCount.longValue());
        } else {
            statement.bindNull(6);
        }
        Long deleteCount = visitStatistic.getDeleteCount();
        if (deleteCount != null) {
            statement.bindLong(7, deleteCount.longValue());
        } else {
            statement.bindNull(7);
        }
        Long queryCount = visitStatistic.getQueryCount();
        if (queryCount != null) {
            statement.bindLong(8, queryCount.longValue());
        } else {
            statement.bindNull(8);
        }
        Long subscribeCount = visitStatistic.getSubscribeCount();
        if (subscribeCount != null) {
            statement.bindLong(9, subscribeCount.longValue());
        } else {
            statement.bindNull(9);
        }
        Long updateTime = visitStatistic.getUpdateTime();
        if (updateTime != null) {
            statement.bindLong(10, updateTime.longValue());
        } else {
            statement.bindNull(10);
        }
        Long uploadTime = visitStatistic.getUploadTime();
        if (uploadTime != null) {
            statement.bindLong(11, uploadTime.longValue());
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public VisitStatistic readObject(Cursor cursor, int i) {
        return new VisitStatistic(cursor);
    }

    public void setPrimaryKeyValue(VisitStatistic visitStatistic, long j) {
        visitStatistic.setId(Integer.valueOf((int) j));
    }
}
