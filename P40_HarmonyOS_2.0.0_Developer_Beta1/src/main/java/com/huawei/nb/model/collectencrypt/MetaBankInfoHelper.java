package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaBankInfoHelper extends AEntityHelper<MetaBankInfo> {
    private static final MetaBankInfoHelper INSTANCE = new MetaBankInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaBankInfo metaBankInfo) {
        return null;
    }

    private MetaBankInfoHelper() {
    }

    public static MetaBankInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaBankInfo metaBankInfo) {
        Integer mId = metaBankInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaBankInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mInRange = metaBankInfo.getMInRange();
        if (mInRange != null) {
            statement.bindLong(3, (long) mInRange.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mOutRange = metaBankInfo.getMOutRange();
        if (mOutRange != null) {
            statement.bindLong(4, (long) mOutRange.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = metaBankInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = metaBankInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaBankInfo readObject(Cursor cursor, int i) {
        return new MetaBankInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaBankInfo metaBankInfo, long j) {
        metaBankInfo.setMId(Integer.valueOf((int) j));
    }
}
