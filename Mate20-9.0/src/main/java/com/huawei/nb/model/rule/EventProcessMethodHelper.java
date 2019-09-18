package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class EventProcessMethodHelper extends AEntityHelper<EventProcessMethod> {
    private static final EventProcessMethodHelper INSTANCE = new EventProcessMethodHelper();

    private EventProcessMethodHelper() {
    }

    public static EventProcessMethodHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, EventProcessMethod object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long eventId = object.getEventId();
        if (eventId != null) {
            statement.bindLong(2, eventId.longValue());
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
        Long ruleId = object.getRuleId();
        if (ruleId != null) {
            statement.bindLong(5, ruleId.longValue());
        } else {
            statement.bindNull(5);
        }
        String method = object.getMethod();
        if (method != null) {
            statement.bindString(6, method);
        } else {
            statement.bindNull(6);
        }
        String methodArgs = object.getMethodArgs();
        if (methodArgs != null) {
            statement.bindString(7, methodArgs);
        } else {
            statement.bindNull(7);
        }
        String condition = object.getCondition();
        if (condition != null) {
            statement.bindString(8, condition);
        } else {
            statement.bindNull(8);
        }
        Integer seqId = object.getSeqId();
        if (seqId != null) {
            statement.bindLong(9, (long) seqId.intValue());
        } else {
            statement.bindNull(9);
        }
    }

    public EventProcessMethod readObject(Cursor cursor, int offset) {
        return new EventProcessMethod(cursor);
    }

    public void setPrimaryKeyValue(EventProcessMethod object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, EventProcessMethod object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
