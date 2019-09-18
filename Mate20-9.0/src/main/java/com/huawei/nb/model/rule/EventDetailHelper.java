package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class EventDetailHelper extends AEntityHelper<EventDetail> {
    private static final EventDetailHelper INSTANCE = new EventDetailHelper();

    private EventDetailHelper() {
    }

    public static EventDetailHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, EventDetail object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long operatorId = object.getOperatorId();
        if (operatorId != null) {
            statement.bindLong(2, operatorId.longValue());
        } else {
            statement.bindNull(2);
        }
        Long itemId = object.getItemId();
        if (itemId != null) {
            statement.bindLong(3, itemId.longValue());
        } else {
            statement.bindNull(3);
        }
        Date startTime = object.getStartTime();
        if (startTime != null) {
            statement.bindLong(4, startTime.getTime());
        } else {
            statement.bindNull(4);
        }
    }

    public EventDetail readObject(Cursor cursor, int offset) {
        return new EventDetail(cursor);
    }

    public void setPrimaryKeyValue(EventDetail object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, EventDetail object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
