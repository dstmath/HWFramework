package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class EventHelper extends AEntityHelper<Event> {
    private static final EventHelper INSTANCE = new EventHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Event event) {
        return null;
    }

    private EventHelper() {
    }

    public static EventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Event event) {
        Long id = event.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = event.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        Long operatorId = event.getOperatorId();
        if (operatorId != null) {
            statement.bindLong(3, operatorId.longValue());
        } else {
            statement.bindNull(3);
        }
        Long itemId = event.getItemId();
        if (itemId != null) {
            statement.bindLong(4, itemId.longValue());
        } else {
            statement.bindNull(4);
        }
        String value = event.getValue();
        if (value != null) {
            statement.bindString(5, value);
        } else {
            statement.bindNull(5);
        }
        Long timeout = event.getTimeout();
        if (timeout != null) {
            statement.bindLong(6, timeout.longValue());
        } else {
            statement.bindNull(6);
        }
        Integer continuity = event.getContinuity();
        if (continuity != null) {
            statement.bindLong(7, (long) continuity.intValue());
        } else {
            statement.bindNull(7);
        }
        Date time = event.getTime();
        if (time != null) {
            statement.bindLong(8, time.getTime());
        } else {
            statement.bindNull(8);
        }
        Date lastTime = event.getLastTime();
        if (lastTime != null) {
            statement.bindLong(9, lastTime.getTime());
        } else {
            statement.bindNull(9);
        }
        Integer count = event.getCount();
        if (count != null) {
            statement.bindLong(10, (long) count.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer totalCount = event.getTotalCount();
        if (totalCount != null) {
            statement.bindLong(11, (long) totalCount.intValue());
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Event readObject(Cursor cursor, int i) {
        return new Event(cursor);
    }

    public void setPrimaryKeyValue(Event event, long j) {
        event.setId(Long.valueOf(j));
    }
}
