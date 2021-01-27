package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DicTableCollectPolicyHelper extends AEntityHelper<DicTableCollectPolicy> {
    private static final DicTableCollectPolicyHelper INSTANCE = new DicTableCollectPolicyHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DicTableCollectPolicy dicTableCollectPolicy) {
        return null;
    }

    private DicTableCollectPolicyHelper() {
    }

    public static DicTableCollectPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DicTableCollectPolicy dicTableCollectPolicy) {
        Integer mId = dicTableCollectPolicy.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mTblName = dicTableCollectPolicy.getMTblName();
        if (mTblName != null) {
            statement.bindString(2, mTblName);
        } else {
            statement.bindNull(2);
        }
        Integer mTblType = dicTableCollectPolicy.getMTblType();
        if (mTblType != null) {
            statement.bindLong(3, (long) mTblType.intValue());
        } else {
            statement.bindNull(3);
        }
        String mTriggerPolicy = dicTableCollectPolicy.getMTriggerPolicy();
        if (mTriggerPolicy != null) {
            statement.bindString(4, mTriggerPolicy);
        } else {
            statement.bindNull(4);
        }
        Integer mMaxRecordOneday = dicTableCollectPolicy.getMMaxRecordOneday();
        if (mMaxRecordOneday != null) {
            statement.bindLong(5, (long) mMaxRecordOneday.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mColdDownTime = dicTableCollectPolicy.getMColdDownTime();
        if (mColdDownTime != null) {
            statement.bindLong(6, (long) mColdDownTime.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = dicTableCollectPolicy.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = dicTableCollectPolicy.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DicTableCollectPolicy readObject(Cursor cursor, int i) {
        return new DicTableCollectPolicy(cursor);
    }

    public void setPrimaryKeyValue(DicTableCollectPolicy dicTableCollectPolicy, long j) {
        dicTableCollectPolicy.setMId(Integer.valueOf((int) j));
    }
}
