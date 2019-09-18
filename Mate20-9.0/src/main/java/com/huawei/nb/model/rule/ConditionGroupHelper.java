package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ConditionGroupHelper extends AEntityHelper<ConditionGroup> {
    private static final ConditionGroupHelper INSTANCE = new ConditionGroupHelper();

    private ConditionGroupHelper() {
    }

    public static ConditionGroupHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ConditionGroup object) {
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
        Long relationId = object.getRelationId();
        if (relationId != null) {
            statement.bindLong(3, relationId.longValue());
        } else {
            statement.bindNull(3);
        }
        Integer conditionNumber = object.getConditionNumber();
        if (conditionNumber != null) {
            statement.bindLong(4, (long) conditionNumber.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer relationType = object.getRelationType();
        if (relationType != null) {
            statement.bindLong(5, (long) relationType.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer conditionType = object.getConditionType();
        if (conditionType != null) {
            statement.bindLong(6, (long) conditionType.intValue());
        } else {
            statement.bindNull(6);
        }
    }

    public ConditionGroup readObject(Cursor cursor, int offset) {
        return new ConditionGroup(cursor);
    }

    public void setPrimaryKeyValue(ConditionGroup object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ConditionGroup object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
