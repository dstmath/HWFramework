package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class SearchClickHelper extends AEntityHelper<SearchClick> {
    private static final SearchClickHelper INSTANCE = new SearchClickHelper();

    private SearchClickHelper() {
    }

    public static SearchClickHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, SearchClick object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String keyword = object.getKeyword();
        if (keyword != null) {
            statement.bindString(2, keyword);
        } else {
            statement.bindNull(2);
        }
        String searchTime = object.getSearchTime();
        if (searchTime != null) {
            statement.bindString(3, searchTime);
        } else {
            statement.bindNull(3);
        }
        String resultType = object.getResultType();
        if (resultType != null) {
            statement.bindString(4, resultType);
        } else {
            statement.bindNull(4);
        }
        String browseType = object.getBrowseType();
        if (browseType != null) {
            statement.bindString(5, browseType);
        } else {
            statement.bindNull(5);
        }
        String browseDetail = object.getBrowseDetail();
        if (browseDetail != null) {
            statement.bindString(6, browseDetail);
        } else {
            statement.bindNull(6);
        }
    }

    public SearchClick readObject(Cursor cursor, int offset) {
        return new SearchClick(cursor);
    }

    public void setPrimaryKeyValue(SearchClick object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, SearchClick object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
