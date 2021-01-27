package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class RuleConditionHelper extends AEntityHelper<RuleCondition> {
    private static final RuleConditionHelper INSTANCE = new RuleConditionHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RuleCondition ruleCondition) {
        return null;
    }

    private RuleConditionHelper() {
    }

    public static RuleConditionHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RuleCondition ruleCondition) {
        Long id = ruleCondition.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long groupId = ruleCondition.getGroupId();
        if (groupId != null) {
            statement.bindLong(2, groupId.longValue());
        } else {
            statement.bindNull(2);
        }
        Long operatorId = ruleCondition.getOperatorId();
        if (operatorId != null) {
            statement.bindLong(3, operatorId.longValue());
        } else {
            statement.bindNull(3);
        }
        Long itemId = ruleCondition.getItemId();
        if (itemId != null) {
            statement.bindLong(4, itemId.longValue());
        } else {
            statement.bindNull(4);
        }
        Integer priority = ruleCondition.getPriority();
        if (priority != null) {
            statement.bindLong(5, (long) priority.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer matchMode = ruleCondition.getMatchMode();
        if (matchMode != null) {
            statement.bindLong(6, (long) matchMode.intValue());
        } else {
            statement.bindNull(6);
        }
        String eventActValue = ruleCondition.getEventActValue();
        if (eventActValue != null) {
            statement.bindString(7, eventActValue);
        } else {
            statement.bindNull(7);
        }
        String eventActValueShift = ruleCondition.getEventActValueShift();
        if (eventActValueShift != null) {
            statement.bindString(8, eventActValueShift);
        } else {
            statement.bindNull(8);
        }
        Integer eventActValueType = ruleCondition.getEventActValueType();
        if (eventActValueType != null) {
            statement.bindLong(9, (long) eventActValueType.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer eventActCompareType = ruleCondition.getEventActCompareType();
        if (eventActCompareType != null) {
            statement.bindLong(10, (long) eventActCompareType.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer eventActAttribute = ruleCondition.getEventActAttribute();
        if (eventActAttribute != null) {
            statement.bindLong(11, (long) eventActAttribute.intValue());
        } else {
            statement.bindNull(11);
        }
        Integer type = ruleCondition.getType();
        if (type != null) {
            statement.bindLong(12, (long) type.intValue());
        } else {
            statement.bindNull(12);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RuleCondition readObject(Cursor cursor, int i) {
        return new RuleCondition(cursor);
    }

    public void setPrimaryKeyValue(RuleCondition ruleCondition, long j) {
        ruleCondition.setId(Long.valueOf(j));
    }
}
