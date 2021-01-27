package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaUserInfoStatisticHelper extends AEntityHelper<MetaUserInfoStatistic> {
    private static final MetaUserInfoStatisticHelper INSTANCE = new MetaUserInfoStatisticHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaUserInfoStatistic metaUserInfoStatistic) {
        return null;
    }

    private MetaUserInfoStatisticHelper() {
    }

    public static MetaUserInfoStatisticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaUserInfoStatistic metaUserInfoStatistic) {
        Integer mId = metaUserInfoStatistic.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaUserInfoStatistic.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mhwid = metaUserInfoStatistic.getMHWID();
        if (mhwid != null) {
            statement.bindString(3, mhwid);
        } else {
            statement.bindNull(3);
        }
        Integer mContactNum = metaUserInfoStatistic.getMContactNum();
        if (mContactNum != null) {
            statement.bindLong(4, (long) mContactNum.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mMusicNum = metaUserInfoStatistic.getMMusicNum();
        if (mMusicNum != null) {
            statement.bindLong(5, (long) mMusicNum.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mVideoNum = metaUserInfoStatistic.getMVideoNum();
        if (mVideoNum != null) {
            statement.bindLong(6, (long) mVideoNum.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mPhotoNum = metaUserInfoStatistic.getMPhotoNum();
        if (mPhotoNum != null) {
            statement.bindLong(7, (long) mPhotoNum.intValue());
        } else {
            statement.bindNull(7);
        }
        Date mFirstAlarmClock = metaUserInfoStatistic.getMFirstAlarmClock();
        if (mFirstAlarmClock != null) {
            statement.bindLong(8, mFirstAlarmClock.getTime());
        } else {
            statement.bindNull(8);
        }
        Integer mCallDialNum = metaUserInfoStatistic.getMCallDialNum();
        if (mCallDialNum != null) {
            statement.bindLong(9, (long) mCallDialNum.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer mCallRecvNum = metaUserInfoStatistic.getMCallRecvNum();
        if (mCallRecvNum != null) {
            statement.bindLong(10, (long) mCallRecvNum.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer mCallDurationTime = metaUserInfoStatistic.getMCallDurationTime();
        if (mCallDurationTime != null) {
            statement.bindLong(11, (long) mCallDurationTime.intValue());
        } else {
            statement.bindNull(11);
        }
        Double mWifiDataTotal = metaUserInfoStatistic.getMWifiDataTotal();
        if (mWifiDataTotal != null) {
            statement.bindDouble(12, mWifiDataTotal.doubleValue());
        } else {
            statement.bindNull(12);
        }
        Double mMobileDataTotal = metaUserInfoStatistic.getMMobileDataTotal();
        if (mMobileDataTotal != null) {
            statement.bindDouble(13, mMobileDataTotal.doubleValue());
        } else {
            statement.bindNull(13);
        }
        Double mMobileDataSurplus = metaUserInfoStatistic.getMMobileDataSurplus();
        if (mMobileDataSurplus != null) {
            statement.bindDouble(14, mMobileDataSurplus.doubleValue());
        } else {
            statement.bindNull(14);
        }
        String mHWIDName = metaUserInfoStatistic.getMHWIDName();
        if (mHWIDName != null) {
            statement.bindString(15, mHWIDName);
        } else {
            statement.bindNull(15);
        }
        Date mHWIDBirthday = metaUserInfoStatistic.getMHWIDBirthday();
        if (mHWIDBirthday != null) {
            statement.bindLong(16, mHWIDBirthday.getTime());
        } else {
            statement.bindNull(16);
        }
        Integer mHWIDGender = metaUserInfoStatistic.getMHWIDGender();
        if (mHWIDGender != null) {
            statement.bindLong(17, (long) mHWIDGender.intValue());
        } else {
            statement.bindNull(17);
        }
        Integer mReservedInt = metaUserInfoStatistic.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(18, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(18);
        }
        String mReservedText = metaUserInfoStatistic.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(19, mReservedText);
        } else {
            statement.bindNull(19);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaUserInfoStatistic readObject(Cursor cursor, int i) {
        return new MetaUserInfoStatistic(cursor);
    }

    public void setPrimaryKeyValue(MetaUserInfoStatistic metaUserInfoStatistic, long j) {
        metaUserInfoStatistic.setMId(Integer.valueOf((int) j));
    }
}
