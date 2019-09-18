package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class RuleConditionHelper extends AEntityHelper<RuleCondition> {
    private static final RuleConditionHelper INSTANCE = new RuleConditionHelper();

    private RuleConditionHelper() {
    }

    public static RuleConditionHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RuleCondition object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long groupId = object.getGroupId();
        if (groupId != null) {
            statement.bindLong(2, groupId.longValue());
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
        Integer priority = object.getPriority();
        if (priority != null) {
            statement.bindLong(5, (long) priority.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer matchMode = object.getMatchMode();
        if (matchMode != null) {
            statement.bindLong(6, (long) matchMode.intValue());
        } else {
            statement.bindNull(6);
        }
        String eventActValue = object.getEventActValue();
        if (eventActValue != null) {
            statement.bindString(7, eventActValue);
        } else {
            statement.bindNull(7);
        }
        String eventActValueShift = object.getEventActValueShift();
        if (eventActValueShift != null) {
            statement.bindString(8, eventActValueShift);
        } else {
            statement.bindNull(8);
        }
        Integer eventActValueType = object.getEventActValueType();
        if (eventActValueType != null) {
            statement.bindLong(9, (long) eventActValueType.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer eventActCompareType = object.getEventActCompareType();
        if (eventActCompareType != null) {
            statement.bindLong(10, (long) eventActCompareType.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer eventActAttribute = object.getEventActAttribute();
        if (eventActAttribute != null) {
            statement.bindLong(11, (long) eventActAttribute.intValue());
        } else {
            statement.bindNull(11);
        }
        Integer type = object.getType();
        if (type != null) {
            statement.bindLong(12, (long) type.intValue());
            return;
        }
        statement.bindNull(12);
    }

    public RuleCondition readObject(Cursor cursor, int offset) {
        return new RuleCondition(cursor);
    }

    public void setPrimaryKeyValue(RuleCondition object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, RuleCondition object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
