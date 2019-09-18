package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessInfoHelper extends AEntityHelper<BusinessInfo> {
    private static final BusinessInfoHelper INSTANCE = new BusinessInfoHelper();

    private BusinessInfoHelper() {
    }

    public static BusinessInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, BusinessInfo object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mBusinessName = object.getMBusinessName();
        if (mBusinessName != null) {
            statement.bindString(2, mBusinessName);
        } else {
            statement.bindNull(2);
        }
        String mDescription = object.getMDescription();
        if (mDescription != null) {
            statement.bindString(3, mDescription);
        } else {
            statement.bindNull(3);
        }
    }

    public BusinessInfo readObject(Cursor cursor, int offset) {
        return new BusinessInfo(cursor);
    }

    public void setPrimaryKeyValue(BusinessInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, BusinessInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
