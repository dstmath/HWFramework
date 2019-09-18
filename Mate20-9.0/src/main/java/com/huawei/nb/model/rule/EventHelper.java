package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class EventHelper extends AEntityHelper<Event> {
    private static final EventHelper INSTANCE = new EventHelper();

    private EventHelper() {
    }

    public static EventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Event object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        Long operatorId = object.getOperatorId();
        if (operatorId != null) {
            statement.bindLong(3, operatorId.longValue());
        } else {
            statement.bindNull(3);
        }
        Long itemId = object.getItemId();
        if (itemId != null) {
            statement.bindLong(4, itemId.longValue());
        } else {
            statement.bindNull(4);
        }
        String value = object.getValue();
        if (value != null) {
            statement.bindString(5, value);
        } else {
            statement.bindNull(5);
        }
        Long timeout = object.getTimeout();
        if (timeout != null) {
            statement.bindLong(6, timeout.longValue());
        } else {
            statement.bindNull(6);
        }
        Integer continuity = object.getContinuity();
        if (continuity != null) {
            statement.bindLong(7, (long) continuity.intValue());
        } else {
            statement.bindNull(7);
        }
        Date time = object.getTime();
        if (time != null) {
            statement.bindLong(8, time.getTime());
        } else {
            statement.bindNull(8);
        }
        Date lastTime = object.getLastTime();
        if (lastTime != null) {
            statement.bindLong(9, lastTime.getTime());
        } else {
            statement.bindNull(9);
        }
        Integer count = object.getCount();
        if (count != null) {
            statement.bindLong(10, (long) count.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer totalCount = object.getTotalCount();
        if (totalCount != null) {
            statement.bindLong(11, (long) totalCount.intValue());
        } else {
            statement.bindNull(11);
        }
    }

    public Event readObject(Cursor cursor, int offset) {
        return new Event(cursor);
    }

    public void setPrimaryKeyValue(Event object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, Event object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
