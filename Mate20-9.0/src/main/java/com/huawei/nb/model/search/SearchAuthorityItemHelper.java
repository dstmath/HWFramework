package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class SearchAuthorityItemHelper extends AEntityHelper<SearchAuthorityItem> {
    private static final SearchAuthorityItemHelper INSTANCE = new SearchAuthorityItemHelper();

    private SearchAuthorityItemHelper() {
    }

    public static SearchAuthorityItemHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, SearchAuthorityItem object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String pkgName = object.getPkgName();
        if (pkgName != null) {
            statement.bindString(2, pkgName);
        } else {
            statement.bindNull(2);
        }
        String callingPkgName = object.getCallingPkgName();
        if (callingPkgName != null) {
            statement.bindString(3, callingPkgName);
        } else {
            statement.bindNull(3);
        }
    }

    public SearchAuthorityItem readObject(Cursor cursor, int offset) {
        return new SearchAuthorityItem(cursor);
    }

    public void setPrimaryKeyValue(SearchAuthorityItem object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, SearchAuthorityItem object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
