package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ActionHelper extends AEntityHelper<Action> {
    private static final ActionHelper INSTANCE = new ActionHelper();

    private ActionHelper() {
    }

    public static ActionHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Action object) {
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
        String value = object.getValue();
        if (value != null) {
            statement.bindString(5, value);
        } else {
            statement.bindNull(5);
        }
        String extraInfo = object.getExtraInfo();
        if (extraInfo != null) {
            statement.bindString(6, extraInfo);
        } else {
            statement.bindNull(6);
        }
        Integer actionType = object.getActionType();
        if (actionType != null) {
            statement.bindLong(7, (long) actionType.intValue());
        } else {
            statement.bindNull(7);
        }
    }

    public Action readObject(Cursor cursor, int offset) {
        return new Action(cursor);
    }

    public void setPrimaryKeyValue(Action object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, Action object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
