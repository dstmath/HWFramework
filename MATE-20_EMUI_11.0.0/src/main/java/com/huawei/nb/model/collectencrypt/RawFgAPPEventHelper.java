package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawFgAPPEventHelper extends AEntityHelper<RawFgAPPEvent> {
    private static final RawFgAPPEventHelper INSTANCE = new RawFgAPPEventHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawFgAPPEvent rawFgAPPEvent) {
        return null;
    }

    private RawFgAPPEventHelper() {
    }

    public static RawFgAPPEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawFgAPPEvent rawFgAPPEvent) {
        Integer mId = rawFgAPPEvent.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawFgAPPEvent.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mPackageName = rawFgAPPEvent.getMPackageName();
        if (mPackageName != null) {
            statement.bindString(3, mPackageName);
        } else {
            statement.bindNull(3);
        }
        Integer mStatus = rawFgAPPEvent.getMStatus();
        if (mStatus != null) {
            statement.bindLong(4, (long) mStatus.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mReservedInt = rawFgAPPEvent.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(5, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(5);
        }
        String mReservedText = rawFgAPPEvent.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(6, mReservedText);
        } else {
            statement.bindNull(6);
        }
        String mActivityName = rawFgAPPEvent.getMActivityName();
        if (mActivityName != null) {
            statement.bindString(7, mActivityName);
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawFgAPPEvent readObject(Cursor cursor, int i) {
        return new RawFgAPPEvent(cursor);
    }

    public void setPrimaryKeyValue(RawFgAPPEvent rawFgAPPEvent, long j) {
        rawFgAPPEvent.setMId(Integer.valueOf((int) j));
    }
}
