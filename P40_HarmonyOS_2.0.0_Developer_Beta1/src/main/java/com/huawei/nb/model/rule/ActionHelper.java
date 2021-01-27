package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ActionHelper extends AEntityHelper<Action> {
    private static final ActionHelper INSTANCE = new ActionHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Action action) {
        return null;
    }

    private ActionHelper() {
    }

    public static ActionHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Action action) {
        Long id = action.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long groupId = action.getGroupId();
        if (groupId != null) {
            statement.bindLong(2, groupId.longValue());
        } else {
            statement.bindNull(2);
        }
        Long operatorId = action.getOperatorId();
        if (operatorId != null) {
            statement.bindLong(3, operatorId.longValue());
        } else {
            statement.bindNull(3);
        }
        Long itemId = action.getItemId();
        if (itemId != null) {
            statement.bindLong(4, itemId.longValue());
        } else {
            statement.bindNull(4);
        }
        String value = action.getValue();
        if (value != null) {
            statement.bindString(5, value);
        } else {
            statement.bindNull(5);
        }
        String extraInfo = action.getExtraInfo();
        if (extraInfo != null) {
            statement.bindString(6, extraInfo);
        } else {
            statement.bindNull(6);
        }
        Integer actionType = action.getActionType();
        if (actionType != null) {
            statement.bindLong(7, (long) actionType.intValue());
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Action readObject(Cursor cursor, int i) {
        return new Action(cursor);
    }

    public void setPrimaryKeyValue(Action action, long j) {
        action.setId(Long.valueOf(j));
    }
}
