package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class GuestInfoHelper extends AEntityHelper<GuestInfo> {
    private static final GuestInfoHelper INSTANCE = new GuestInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, GuestInfo guestInfo) {
        return null;
    }

    private GuestInfoHelper() {
    }

    public static GuestInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, GuestInfo guestInfo) {
        Integer id = guestInfo.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String pkgName = guestInfo.getPkgName();
        if (pkgName != null) {
            statement.bindString(2, pkgName);
        } else {
            statement.bindNull(2);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public GuestInfo readObject(Cursor cursor, int i) {
        return new GuestInfo(cursor);
    }

    public void setPrimaryKeyValue(GuestInfo guestInfo, long j) {
        guestInfo.setId(Integer.valueOf((int) j));
    }
}
