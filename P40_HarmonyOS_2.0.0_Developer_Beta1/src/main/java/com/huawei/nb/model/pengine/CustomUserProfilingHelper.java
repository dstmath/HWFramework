package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CustomUserProfilingHelper extends AEntityHelper<CustomUserProfiling> {
    private static final CustomUserProfilingHelper INSTANCE = new CustomUserProfilingHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CustomUserProfiling customUserProfiling) {
        return null;
    }

    private CustomUserProfilingHelper() {
    }

    public static CustomUserProfilingHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CustomUserProfiling customUserProfiling) {
        Integer id = customUserProfiling.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String parent = customUserProfiling.getParent();
        if (parent != null) {
            statement.bindString(2, parent);
        } else {
            statement.bindNull(2);
        }
        Integer level = customUserProfiling.getLevel();
        if (level != null) {
            statement.bindLong(3, (long) level.intValue());
        } else {
            statement.bindNull(3);
        }
        String uriKey = customUserProfiling.getUriKey();
        if (uriKey != null) {
            statement.bindString(4, uriKey);
        } else {
            statement.bindNull(4);
        }
        String uriValue = customUserProfiling.getUriValue();
        if (uriValue != null) {
            statement.bindString(5, uriValue);
        } else {
            statement.bindNull(5);
        }
        Long timestamp = customUserProfiling.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(6, timestamp.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CustomUserProfiling readObject(Cursor cursor, int i) {
        return new CustomUserProfiling(cursor);
    }

    public void setPrimaryKeyValue(CustomUserProfiling customUserProfiling, long j) {
        customUserProfiling.setId(Integer.valueOf((int) j));
    }
}
