package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class SearchMultiUserConfigHelper extends AEntityHelper<SearchMultiUserConfig> {
    private static final SearchMultiUserConfigHelper INSTANCE = new SearchMultiUserConfigHelper();

    private SearchMultiUserConfigHelper() {
    }

    public static SearchMultiUserConfigHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, SearchMultiUserConfig object) {
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
        statement.bindLong(3, object.getIsIntentIdleFinished() ? 1 : 0);
        String lastBuildIntentIndexTime = object.getLastBuildIntentIndexTime();
        if (lastBuildIntentIndexTime != null) {
            statement.bindString(4, lastBuildIntentIndexTime);
        } else {
            statement.bindNull(4);
        }
    }

    public SearchMultiUserConfig readObject(Cursor cursor, int offset) {
        return new SearchMultiUserConfig(cursor);
    }

    public void setPrimaryKeyValue(SearchMultiUserConfig object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, SearchMultiUserConfig object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
