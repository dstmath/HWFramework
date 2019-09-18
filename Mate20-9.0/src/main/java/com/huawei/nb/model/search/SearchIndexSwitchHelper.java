package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class SearchIndexSwitchHelper extends AEntityHelper<SearchIndexSwitch> {
    private static final SearchIndexSwitchHelper INSTANCE = new SearchIndexSwitchHelper();

    private SearchIndexSwitchHelper() {
    }

    public static SearchIndexSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, SearchIndexSwitch object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer userId = object.getUserId();
        if (userId != null) {
            statement.bindLong(2, (long) userId.intValue());
        } else {
            statement.bindNull(2);
        }
        statement.bindLong(3, object.getIsSwitchOn() ? 1 : 0);
    }

    public SearchIndexSwitch readObject(Cursor cursor, int offset) {
        return new SearchIndexSwitch(cursor);
    }

    public void setPrimaryKeyValue(SearchIndexSwitch object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, SearchIndexSwitch object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
