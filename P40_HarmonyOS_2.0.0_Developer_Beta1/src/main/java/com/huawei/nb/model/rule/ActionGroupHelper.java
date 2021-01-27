package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ActionGroupHelper extends AEntityHelper<ActionGroup> {
    private static final ActionGroupHelper INSTANCE = new ActionGroupHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ActionGroup actionGroup) {
        return null;
    }

    private ActionGroupHelper() {
    }

    public static ActionGroupHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ActionGroup actionGroup) {
        Long id = actionGroup.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long ruleId = actionGroup.getRuleId();
        if (ruleId != null) {
            statement.bindLong(2, ruleId.longValue());
        } else {
            statement.bindNull(2);
        }
        Integer actionNumber = actionGroup.getActionNumber();
        if (actionNumber != null) {
            statement.bindLong(3, (long) actionNumber.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer relationType = actionGroup.getRelationType();
        if (relationType != null) {
            statement.bindLong(4, (long) relationType.intValue());
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ActionGroup readObject(Cursor cursor, int i) {
        return new ActionGroup(cursor);
    }

    public void setPrimaryKeyValue(ActionGroup actionGroup, long j) {
        actionGroup.setId(Long.valueOf(j));
    }
}
