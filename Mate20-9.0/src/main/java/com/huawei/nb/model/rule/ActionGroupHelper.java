package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ActionGroupHelper extends AEntityHelper<ActionGroup> {
    private static final ActionGroupHelper INSTANCE = new ActionGroupHelper();

    private ActionGroupHelper() {
    }

    public static ActionGroupHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ActionGroup object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long ruleId = object.getRuleId();
        if (ruleId != null) {
            statement.bindLong(2, ruleId.longValue());
        } else {
            statement.bindNull(2);
        }
        Integer actionNumber = object.getActionNumber();
        if (actionNumber != null) {
            statement.bindLong(3, (long) actionNumber.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer relationType = object.getRelationType();
        if (relationType != null) {
            statement.bindLong(4, (long) relationType.intValue());
        } else {
            statement.bindNull(4);
        }
    }

    public ActionGroup readObject(Cursor cursor, int offset) {
        return new ActionGroup(cursor);
    }

    public void setPrimaryKeyValue(ActionGroup object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ActionGroup object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
