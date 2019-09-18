package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class GuestInfoHelper extends AEntityHelper<GuestInfo> {
    private static final GuestInfoHelper INSTANCE = new GuestInfoHelper();

    private GuestInfoHelper() {
    }

    public static GuestInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, GuestInfo object) {
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
    }

    public GuestInfo readObject(Cursor cursor, int offset) {
        return new GuestInfo(cursor);
    }

    public void setPrimaryKeyValue(GuestInfo object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, GuestInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
