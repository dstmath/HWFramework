package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawMailInfoHelper extends AEntityHelper<RawMailInfo> {
    private static final RawMailInfoHelper INSTANCE = new RawMailInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawMailInfo rawMailInfo) {
        return null;
    }

    private RawMailInfoHelper() {
    }

    public static RawMailInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawMailInfo rawMailInfo) {
        Integer mId = rawMailInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawMailInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mMailClientName = rawMailInfo.getMMailClientName();
        if (mMailClientName != null) {
            statement.bindString(3, mMailClientName);
        } else {
            statement.bindNull(3);
        }
        String mMailAddress = rawMailInfo.getMMailAddress();
        if (mMailAddress != null) {
            statement.bindString(4, mMailAddress);
        } else {
            statement.bindNull(4);
        }
        String mMailSubject = rawMailInfo.getMMailSubject();
        if (mMailSubject != null) {
            statement.bindString(5, mMailSubject);
        } else {
            statement.bindNull(5);
        }
        String mMailContent = rawMailInfo.getMMailContent();
        if (mMailContent != null) {
            statement.bindString(6, mMailContent);
        } else {
            statement.bindNull(6);
        }
        Date mMailTime = rawMailInfo.getMMailTime();
        if (mMailTime != null) {
            statement.bindLong(7, mMailTime.getTime());
        } else {
            statement.bindNull(7);
        }
        String mMailFrom = rawMailInfo.getMMailFrom();
        if (mMailFrom != null) {
            statement.bindString(8, mMailFrom);
        } else {
            statement.bindNull(8);
        }
        String mMailTo = rawMailInfo.getMMailTo();
        if (mMailTo != null) {
            statement.bindString(9, mMailTo);
        } else {
            statement.bindNull(9);
        }
        Integer mReservedInt = rawMailInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(10, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(10);
        }
        String mReservedText = rawMailInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(11, mReservedText);
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawMailInfo readObject(Cursor cursor, int i) {
        return new RawMailInfo(cursor);
    }

    public void setPrimaryKeyValue(RawMailInfo rawMailInfo, long j) {
        rawMailInfo.setMId(Integer.valueOf((int) j));
    }
}
