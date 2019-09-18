package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DicFieldCollectPolicyHelper extends AEntityHelper<DicFieldCollectPolicy> {
    private static final DicFieldCollectPolicyHelper INSTANCE = new DicFieldCollectPolicyHelper();

    private DicFieldCollectPolicyHelper() {
    }

    public static DicFieldCollectPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DicFieldCollectPolicy object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mTableName = object.getMTableName();
        if (mTableName != null) {
            statement.bindString(2, mTableName);
        } else {
            statement.bindNull(2);
        }
        String mFieldName = object.getMFieldName();
        if (mFieldName != null) {
            statement.bindString(3, mFieldName);
        } else {
            statement.bindNull(3);
        }
        Integer mCollectMethod = object.getMCollectMethod();
        if (mCollectMethod != null) {
            statement.bindLong(4, (long) mCollectMethod.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    public DicFieldCollectPolicy readObject(Cursor cursor, int offset) {
        return new DicFieldCollectPolicy(cursor);
    }

    public void setPrimaryKeyValue(DicFieldCollectPolicy object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DicFieldCollectPolicy object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
