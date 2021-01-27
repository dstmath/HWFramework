package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class EventTimerHelper extends AEntityHelper<EventTimer> {
    private static final EventTimerHelper INSTANCE = new EventTimerHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, EventTimer eventTimer) {
        return null;
    }

    private EventTimerHelper() {
    }

    public static EventTimerHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, EventTimer eventTimer) {
        Long id = eventTimer.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long length = eventTimer.getLength();
        if (length != null) {
            statement.bindLong(2, length.longValue());
        } else {
            statement.bindNull(2);
        }
        Integer switchOn = eventTimer.getSwitchOn();
        if (switchOn != null) {
            statement.bindLong(3, (long) switchOn.intValue());
        } else {
            statement.bindNull(3);
        }
        Long ruleId = eventTimer.getRuleId();
        if (ruleId != null) {
            statement.bindLong(4, ruleId.longValue());
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public EventTimer readObject(Cursor cursor, int i) {
        return new EventTimer(cursor);
    }

    public void setPrimaryKeyValue(EventTimer eventTimer, long j) {
        eventTimer.setId(Long.valueOf(j));
    }
}
