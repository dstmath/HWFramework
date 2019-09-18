package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaTextInfoHelper extends AEntityHelper<MetaTextInfo> {
    private static final MetaTextInfoHelper INSTANCE = new MetaTextInfoHelper();

    private MetaTextInfoHelper() {
    }

    public static MetaTextInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaTextInfo object) {
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
        Integer mType = object.getMType();
        if (mType != null) {
            statement.bindLong(3, (long) mType.intValue());
        } else {
            statement.bindNull(3);
        }
        String mTitle = object.getMTitle();
        if (mTitle != null) {
            statement.bindString(4, mTitle);
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

    public MetaTextInfo readObject(Cursor cursor, int offset) {
        return new MetaTextInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaTextInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaTextInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
