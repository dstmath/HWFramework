package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaBankInfoHelper extends AEntityHelper<MetaBankInfo> {
    private static final MetaBankInfoHelper INSTANCE = new MetaBankInfoHelper();

    private MetaBankInfoHelper() {
    }

    public static MetaBankInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaBankInfo object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = object.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mInRange = object.getMInRange();
        if (mInRange != null) {
            statement.bindLong(3, (long) mInRange.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mOutRange = object.getMOutRange();
        if (mOutRange != null) {
            statement.bindLong(4, (long) mOutRange.intValue());
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

    public MetaBankInfo readObject(Cursor cursor, int offset) {
        return new MetaBankInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaBankInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaBankInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
