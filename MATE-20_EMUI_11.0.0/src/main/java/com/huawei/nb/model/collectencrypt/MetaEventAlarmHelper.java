package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventAlarmHelper extends AEntityHelper<MetaEventAlarm> {
    private static final MetaEventAlarmHelper INSTANCE = new MetaEventAlarmHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaEventAlarm metaEventAlarm) {
        return null;
    }

    private MetaEventAlarmHelper() {
    }

    public static MetaEventAlarmHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaEventAlarm metaEventAlarm) {
        Integer mId = metaEventAlarm.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaEventAlarm.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mEventID = metaEventAlarm.getMEventID();
        if (mEventID != null) {
            statement.bindLong(3, (long) mEventID.intValue());
        } else {
            statement.bindNull(3);
        }
        Date mAlarmTime = metaEventAlarm.getMAlarmTime();
        if (mAlarmTime != null) {
            statement.bindLong(4, mAlarmTime.getTime());
        } else {
            statement.bindNull(4);
        }
        String mEventInfo = metaEventAlarm.getMEventInfo();
        if (mEventInfo != null) {
            statement.bindString(5, mEventInfo);
        } else {
            statement.bindNull(5);
        }
        String mAddress = metaEventAlarm.getMAddress();
        if (mAddress != null) {
            statement.bindString(6, mAddress);
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = metaEventAlarm.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = metaEventAlarm.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaEventAlarm readObject(Cursor cursor, int i) {
        return new MetaEventAlarm(cursor);
    }

    public void setPrimaryKeyValue(MetaEventAlarm metaEventAlarm, long j) {
        metaEventAlarm.setMId(Integer.valueOf((int) j));
    }
}
