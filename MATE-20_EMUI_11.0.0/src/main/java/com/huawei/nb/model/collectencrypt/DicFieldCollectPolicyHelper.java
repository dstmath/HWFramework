package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DicFieldCollectPolicyHelper extends AEntityHelper<DicFieldCollectPolicy> {
    private static final DicFieldCollectPolicyHelper INSTANCE = new DicFieldCollectPolicyHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DicFieldCollectPolicy dicFieldCollectPolicy) {
        return null;
    }

    private DicFieldCollectPolicyHelper() {
    }

    public static DicFieldCollectPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DicFieldCollectPolicy dicFieldCollectPolicy) {
        Integer mId = dicFieldCollectPolicy.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mTableName = dicFieldCollectPolicy.getMTableName();
        if (mTableName != null) {
            statement.bindString(2, mTableName);
        } else {
            statement.bindNull(2);
        }
        String mFieldName = dicFieldCollectPolicy.getMFieldName();
        if (mFieldName != null) {
            statement.bindString(3, mFieldName);
        } else {
            statement.bindNull(3);
        }
        Integer mCollectMethod = dicFieldCollectPolicy.getMCollectMethod();
        if (mCollectMethod != null) {
            statement.bindLong(4, (long) mCollectMethod.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = dicFieldCollectPolicy.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = dicFieldCollectPolicy.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DicFieldCollectPolicy readObject(Cursor cursor, int i) {
        return new DicFieldCollectPolicy(cursor);
    }

    public void setPrimaryKeyValue(DicFieldCollectPolicy dicFieldCollectPolicy, long j) {
        dicFieldCollectPolicy.setMId(Integer.valueOf((int) j));
    }
}
