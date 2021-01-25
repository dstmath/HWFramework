package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class EventProcessMethodHelper extends AEntityHelper<EventProcessMethod> {
    private static final EventProcessMethodHelper INSTANCE = new EventProcessMethodHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, EventProcessMethod eventProcessMethod) {
        return null;
    }

    private EventProcessMethodHelper() {
    }

    public static EventProcessMethodHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, EventProcessMethod eventProcessMethod) {
        Long id = eventProcessMethod.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long eventId = eventProcessMethod.getEventId();
        if (eventId != null) {
            statement.bindLong(2, eventId.longValue());
        } else {
            statement.bindNull(2);
        }
        Long operatorId = eventProcessMethod.getOperatorId();
        if (operatorId != null) {
            statement.bindLong(3, operatorId.longValue());
        } else {
            statement.bindNull(3);
        }
        Long itemId = eventProcessMethod.getItemId();
        if (itemId != null) {
            statement.bindLong(4, itemId.longValue());
        } else {
            statement.bindNull(4);
        }
        Long ruleId = eventProcessMethod.getRuleId();
        if (ruleId != null) {
            statement.bindLong(5, ruleId.longValue());
        } else {
            statement.bindNull(5);
        }
        String method = eventProcessMethod.getMethod();
        if (method != null) {
            statement.bindString(6, method);
        } else {
            statement.bindNull(6);
        }
        String methodArgs = eventProcessMethod.getMethodArgs();
        if (methodArgs != null) {
            statement.bindString(7, methodArgs);
        } else {
            statement.bindNull(7);
        }
        String condition = eventProcessMethod.getCondition();
        if (condition != null) {
            statement.bindString(8, condition);
        } else {
            statement.bindNull(8);
        }
        Integer seqId = eventProcessMethod.getSeqId();
        if (seqId != null) {
            statement.bindLong(9, (long) seqId.intValue());
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public EventProcessMethod readObject(Cursor cursor, int i) {
        return new EventProcessMethod(cursor);
    }

    public void setPrimaryKeyValue(EventProcessMethod eventProcessMethod, long j) {
        eventProcessMethod.setId(Long.valueOf(j));
    }
}
