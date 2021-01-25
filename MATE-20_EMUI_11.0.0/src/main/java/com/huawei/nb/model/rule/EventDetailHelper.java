package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class EventDetailHelper extends AEntityHelper<EventDetail> {
    private static final EventDetailHelper INSTANCE = new EventDetailHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, EventDetail eventDetail) {
        return null;
    }

    private EventDetailHelper() {
    }

    public static EventDetailHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, EventDetail eventDetail) {
        Long id = eventDetail.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long operatorId = eventDetail.getOperatorId();
        if (operatorId != null) {
            statement.bindLong(2, operatorId.longValue());
        } else {
            statement.bindNull(2);
        }
        Long itemId = eventDetail.getItemId();
        if (itemId != null) {
            statement.bindLong(3, itemId.longValue());
        } else {
            statement.bindNull(3);
        }
        Date startTime = eventDetail.getStartTime();
        if (startTime != null) {
            statement.bindLong(4, startTime.getTime());
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public EventDetail readObject(Cursor cursor, int i) {
        return new EventDetail(cursor);
    }

    public void setPrimaryKeyValue(EventDetail eventDetail, long j) {
        eventDetail.setId(Long.valueOf(j));
    }
}
