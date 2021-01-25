package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class SearchClickHelper extends AEntityHelper<SearchClick> {
    private static final SearchClickHelper INSTANCE = new SearchClickHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, SearchClick searchClick) {
        return null;
    }

    private SearchClickHelper() {
    }

    public static SearchClickHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, SearchClick searchClick) {
        Integer id = searchClick.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String keyword = searchClick.getKeyword();
        if (keyword != null) {
            statement.bindString(2, keyword);
        } else {
            statement.bindNull(2);
        }
        String searchTime = searchClick.getSearchTime();
        if (searchTime != null) {
            statement.bindString(3, searchTime);
        } else {
            statement.bindNull(3);
        }
        String resultType = searchClick.getResultType();
        if (resultType != null) {
            statement.bindString(4, resultType);
        } else {
            statement.bindNull(4);
        }
        String browseType = searchClick.getBrowseType();
        if (browseType != null) {
            statement.bindString(5, browseType);
        } else {
            statement.bindNull(5);
        }
        String browseDetail = searchClick.getBrowseDetail();
        if (browseDetail != null) {
            statement.bindString(6, browseDetail);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public SearchClick readObject(Cursor cursor, int i) {
        return new SearchClick(cursor);
    }

    public void setPrimaryKeyValue(SearchClick searchClick, long j) {
        searchClick.setId(Integer.valueOf((int) j));
    }
}
