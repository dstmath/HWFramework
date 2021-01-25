package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawCalenderConfHelper extends AEntityHelper<RawCalenderConf> {
    private static final RawCalenderConfHelper INSTANCE = new RawCalenderConfHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawCalenderConf rawCalenderConf) {
        return null;
    }

    private RawCalenderConfHelper() {
    }

    public static RawCalenderConfHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawCalenderConf rawCalenderConf) {
        Integer mId = rawCalenderConf.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawCalenderConf.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mConfTopic = rawCalenderConf.getMConfTopic();
        if (mConfTopic != null) {
            statement.bindString(3, mConfTopic);
        } else {
            statement.bindNull(3);
        }
        Date mConfBeginTime = rawCalenderConf.getMConfBeginTime();
        if (mConfBeginTime != null) {
            statement.bindLong(4, mConfBeginTime.getTime());
        } else {
            statement.bindNull(4);
        }
        Date mConfEndTime = rawCalenderConf.getMConfEndTime();
        if (mConfEndTime != null) {
            statement.bindLong(5, mConfEndTime.getTime());
        } else {
            statement.bindNull(5);
        }
        String mConfAddr = rawCalenderConf.getMConfAddr();
        if (mConfAddr != null) {
            statement.bindString(6, mConfAddr);
        } else {
            statement.bindNull(6);
        }
        String mConfSponsor = rawCalenderConf.getMConfSponsor();
        if (mConfSponsor != null) {
            statement.bindString(7, mConfSponsor);
        } else {
            statement.bindNull(7);
        }
        Integer mConfStat = rawCalenderConf.getMConfStat();
        if (mConfStat != null) {
            statement.bindLong(8, (long) mConfStat.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer mReservedInt = rawCalenderConf.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(9, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(9);
        }
        String mReservedText = rawCalenderConf.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(10, mReservedText);
        } else {
            statement.bindNull(10);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawCalenderConf readObject(Cursor cursor, int i) {
        return new RawCalenderConf(cursor);
    }

    public void setPrimaryKeyValue(RawCalenderConf rawCalenderConf, long j) {
        rawCalenderConf.setMId(Integer.valueOf((int) j));
    }
}
