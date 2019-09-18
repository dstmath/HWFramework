package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaUserInfoStatisticHelper extends AEntityHelper<MetaUserInfoStatistic> {
    private static final MetaUserInfoStatisticHelper INSTANCE = new MetaUserInfoStatisticHelper();

    private MetaUserInfoStatisticHelper() {
    }

    public static MetaUserInfoStatisticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaUserInfoStatistic object) {
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
        String mHWID = object.getMHWID();
        if (mHWID != null) {
            statement.bindString(3, mHWID);
        } else {
            statement.bindNull(3);
        }
        Integer mContactNum = object.getMContactNum();
        if (mContactNum != null) {
            statement.bindLong(4, (long) mContactNum.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mMusicNum = object.getMMusicNum();
        if (mMusicNum != null) {
            statement.bindLong(5, (long) mMusicNum.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mVideoNum = object.getMVideoNum();
        if (mVideoNum != null) {
            statement.bindLong(6, (long) mVideoNum.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mPhotoNum = object.getMPhotoNum();
        if (mPhotoNum != null) {
            statement.bindLong(7, (long) mPhotoNum.intValue());
        } else {
            statement.bindNull(7);
        }
        Date mFirstAlarmClock = object.getMFirstAlarmClock();
        if (mFirstAlarmClock != null) {
            statement.bindLong(8, mFirstAlarmClock.getTime());
        } else {
            statement.bindNull(8);
        }
        Integer mCallDialNum = object.getMCallDialNum();
        if (mCallDialNum != null) {
            statement.bindLong(9, (long) mCallDialNum.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer mCallRecvNum = object.getMCallRecvNum();
        if (mCallRecvNum != null) {
            statement.bindLong(10, (long) mCallRecvNum.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer mCallDurationTime = object.getMCallDurationTime();
        if (mCallDurationTime != null) {
            statement.bindLong(11, (long) mCallDurationTime.intValue());
        } else {
            statement.bindNull(11);
        }
        Double mWifiDataTotal = object.getMWifiDataTotal();
        if (mWifiDataTotal != null) {
            statement.bindDouble(12, mWifiDataTotal.doubleValue());
        } else {
            statement.bindNull(12);
        }
        Double mMobileDataTotal = object.getMMobileDataTotal();
        if (mMobileDataTotal != null) {
            statement.bindDouble(13, mMobileDataTotal.doubleValue());
        } else {
            statement.bindNull(13);
        }
        Double mMobileDataSurplus = object.getMMobileDataSurplus();
        if (mMobileDataSurplus != null) {
            statement.bindDouble(14, mMobileDataSurplus.doubleValue());
        } else {
            statement.bindNull(14);
        }
        String mHWIDName = object.getMHWIDName();
        if (mHWIDName != null) {
            statement.bindString(15, mHWIDName);
        } else {
            statement.bindNull(15);
        }
        Date mHWIDBirthday = object.getMHWIDBirthday();
        if (mHWIDBirthday != null) {
            statement.bindLong(16, mHWIDBirthday.getTime());
        } else {
            statement.bindNull(16);
        }
        Integer mHWIDGender = object.getMHWIDGender();
        if (mHWIDGender != null) {
            statement.bindLong(17, (long) mHWIDGender.intValue());
        } else {
            statement.bindNull(17);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(18, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(18);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(19, mReservedText);
        } else {
            statement.bindNull(19);
        }
    }

    public MetaUserInfoStatistic readObject(Cursor cursor, int offset) {
        return new MetaUserInfoStatistic(cursor);
    }

    public void setPrimaryKeyValue(MetaUserInfoStatistic object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaUserInfoStatistic object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
