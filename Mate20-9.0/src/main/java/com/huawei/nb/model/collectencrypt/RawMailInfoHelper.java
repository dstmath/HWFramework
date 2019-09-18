package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMailInfoHelper extends AEntityHelper<RawMailInfo> {
    private static final RawMailInfoHelper INSTANCE = new RawMailInfoHelper();

    private RawMailInfoHelper() {
    }

    public static RawMailInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawMailInfo object) {
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
        String mMailClientName = object.getMMailClientName();
        if (mMailClientName != null) {
            statement.bindString(3, mMailClientName);
        } else {
            statement.bindNull(3);
        }
        String mMailAddress = object.getMMailAddress();
        if (mMailAddress != null) {
            statement.bindString(4, mMailAddress);
        } else {
            statement.bindNull(4);
        }
        String mMailSubject = object.getMMailSubject();
        if (mMailSubject != null) {
            statement.bindString(5, mMailSubject);
        } else {
            statement.bindNull(5);
        }
        String mMailContent = object.getMMailContent();
        if (mMailContent != null) {
            statement.bindString(6, mMailContent);
        } else {
            statement.bindNull(6);
        }
        Date mMailTime = object.getMMailTime();
        if (mMailTime != null) {
            statement.bindLong(7, mMailTime.getTime());
        } else {
            statement.bindNull(7);
        }
        String mMailFrom = object.getMMailFrom();
        if (mMailFrom != null) {
            statement.bindString(8, mMailFrom);
        } else {
            statement.bindNull(8);
        }
        String mMailTo = object.getMMailTo();
        if (mMailTo != null) {
            statement.bindString(9, mMailTo);
        } else {
            statement.bindNull(9);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(10, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(10);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(11, mReservedText);
        } else {
            statement.bindNull(11);
        }
    }

    public RawMailInfo readObject(Cursor cursor, int offset) {
        return new RawMailInfo(cursor);
    }

    public void setPrimaryKeyValue(RawMailInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawMailInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
