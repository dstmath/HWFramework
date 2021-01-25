package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ConditionGroupHelper extends AEntityHelper<ConditionGroup> {
    private static final ConditionGroupHelper INSTANCE = new ConditionGroupHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ConditionGroup conditionGroup) {
        return null;
    }

    private ConditionGroupHelper() {
    }

    public static ConditionGroupHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ConditionGroup conditionGroup) {
        Long id = conditionGroup.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long groupId = conditionGroup.getGroupId();
        if (groupId != null) {
            statement.bindLong(2, groupId.longValue());
        } else {
            statement.bindNull(2);
        }
        Long relationId = conditionGroup.getRelationId();
        if (relationId != null) {
            statement.bindLong(3, relationId.longValue());
        } else {
            statement.bindNull(3);
        }
        Integer conditionNumber = conditionGroup.getConditionNumber();
        if (conditionNumber != null) {
            statement.bindLong(4, (long) conditionNumber.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer relationType = conditionGroup.getRelationType();
        if (relationType != null) {
            statement.bindLong(5, (long) relationType.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer conditionType = conditionGroup.getConditionType();
        if (conditionType != null) {
            statement.bindLong(6, (long) conditionType.intValue());
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ConditionGroup readObject(Cursor cursor, int i) {
        return new ConditionGroup(cursor);
    }

    public void setPrimaryKeyValue(ConditionGroup conditionGroup, long j) {
        conditionGroup.setId(Long.valueOf(j));
    }
}
