package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawCalenderTipHelper extends AEntityHelper<RawCalenderTip> {
    private static final RawCalenderTipHelper INSTANCE = new RawCalenderTipHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawCalenderTip rawCalenderTip) {
        return null;
    }

    private RawCalenderTipHelper() {
    }

    public static RawCalenderTipHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawCalenderTip rawCalenderTip) {
        Integer mId = rawCalenderTip.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawCalenderTip.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mTipTitle = rawCalenderTip.getMTipTitle();
        if (mTipTitle != null) {
            statement.bindString(3, mTipTitle);
        } else {
            statement.bindNull(3);
        }
        String mTipContent = rawCalenderTip.getMTipContent();
        if (mTipContent != null) {
            statement.bindString(4, mTipContent);
        } else {
            statement.bindNull(4);
        }
        Date mTipStartTime = rawCalenderTip.getMTipStartTime();
        if (mTipStartTime != null) {
            statement.bindLong(5, mTipStartTime.getTime());
        } else {
            statement.bindNull(5);
        }
        Date mTipEndTime = rawCalenderTip.getMTipEndTime();
        if (mTipEndTime != null) {
            statement.bindLong(6, mTipEndTime.getTime());
        } else {
            statement.bindNull(6);
        }
        Date mTipAlarmTime = rawCalenderTip.getMTipAlarmTime();
        if (mTipAlarmTime != null) {
            statement.bindLong(7, mTipAlarmTime.getTime());
        } else {
            statement.bindNull(7);
        }
        Integer mReservedInt = rawCalenderTip.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(8, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(8);
        }
        String mReservedText = rawCalenderTip.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(9, mReservedText);
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawCalenderTip readObject(Cursor cursor, int i) {
        return new RawCalenderTip(cursor);
    }

    public void setPrimaryKeyValue(RawCalenderTip rawCalenderTip, long j) {
        rawCalenderTip.setMId(Integer.valueOf((int) j));
    }
}
