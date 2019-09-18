package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class SearchDictionaryHelper extends AEntityHelper<SearchDictionary> {
    private static final SearchDictionaryHelper INSTANCE = new SearchDictionaryHelper();

    private SearchDictionaryHelper() {
    }

    public static SearchDictionaryHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, SearchDictionary object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String value = object.getValue();
        if (value != null) {
            statement.bindString(3, value);
        } else {
            statement.bindNull(3);
        }
        Integer type = object.getType();
        if (type != null) {
            statement.bindLong(4, (long) type.intValue());
        } else {
            statement.bindNull(4);
        }
    }

    public SearchDictionary readObject(Cursor cursor, int offset) {
        return new SearchDictionary(cursor);
    }

    public void setPrimaryKeyValue(SearchDictionary object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, SearchDictionary object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
