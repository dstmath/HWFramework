package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawRecomIntegratedInfoHelper extends AEntityHelper<RawRecomIntegratedInfo> {
    private static final RawRecomIntegratedInfoHelper INSTANCE = new RawRecomIntegratedInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawRecomIntegratedInfo rawRecomIntegratedInfo) {
        return null;
    }

    private RawRecomIntegratedInfoHelper() {
    }

    public static RawRecomIntegratedInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawRecomIntegratedInfo rawRecomIntegratedInfo) {
        Integer mId = rawRecomIntegratedInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawRecomIntegratedInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Date mDateTime = rawRecomIntegratedInfo.getMDateTime();
        if (mDateTime != null) {
            statement.bindLong(3, mDateTime.getTime());
        } else {
            statement.bindNull(3);
        }
        String mApkName = rawRecomIntegratedInfo.getMApkName();
        if (mApkName != null) {
            statement.bindString(4, mApkName);
        } else {
            statement.bindNull(4);
        }
        Integer mArActivityType = rawRecomIntegratedInfo.getMArActivityType();
        if (mArActivityType != null) {
            statement.bindLong(5, (long) mArActivityType.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mHeadset = rawRecomIntegratedInfo.getMHeadset();
        if (mHeadset != null) {
            statement.bindLong(6, (long) mHeadset.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mWeek = rawRecomIntegratedInfo.getMWeek();
        if (mWeek != null) {
            statement.bindLong(7, (long) mWeek.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer mNetworkType = rawRecomIntegratedInfo.getMNetworkType();
        if (mNetworkType != null) {
            statement.bindLong(8, (long) mNetworkType.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer mLocationType = rawRecomIntegratedInfo.getMLocationType();
        if (mLocationType != null) {
            statement.bindLong(9, (long) mLocationType.intValue());
        } else {
            statement.bindNull(9);
        }
        String mBatteryStatus = rawRecomIntegratedInfo.getMBatteryStatus();
        if (mBatteryStatus != null) {
            statement.bindString(10, mBatteryStatus);
        } else {
            statement.bindNull(10);
        }
        Integer mWeatherIcon = rawRecomIntegratedInfo.getMWeatherIcon();
        if (mWeatherIcon != null) {
            statement.bindLong(11, (long) mWeatherIcon.intValue());
        } else {
            statement.bindNull(11);
        }
        Integer mCurrentTemperature = rawRecomIntegratedInfo.getMCurrentTemperature();
        if (mCurrentTemperature != null) {
            statement.bindLong(12, (long) mCurrentTemperature.intValue());
        } else {
            statement.bindNull(12);
        }
        String mLatitude = rawRecomIntegratedInfo.getMLatitude();
        if (mLatitude != null) {
            statement.bindString(13, mLatitude);
        } else {
            statement.bindNull(13);
        }
        String mLongitude = rawRecomIntegratedInfo.getMLongitude();
        if (mLongitude != null) {
            statement.bindString(14, mLongitude);
        } else {
            statement.bindNull(14);
        }
        String mTotalTime = rawRecomIntegratedInfo.getMTotalTime();
        if (mTotalTime != null) {
            statement.bindString(15, mTotalTime);
        } else {
            statement.bindNull(15);
        }
        Integer mCellId = rawRecomIntegratedInfo.getMCellId();
        if (mCellId != null) {
            statement.bindLong(16, (long) mCellId.intValue());
        } else {
            statement.bindNull(16);
        }
        String mWifiBssid = rawRecomIntegratedInfo.getMWifiBssid();
        if (mWifiBssid != null) {
            statement.bindString(17, mWifiBssid);
        } else {
            statement.bindNull(17);
        }
        String mService = rawRecomIntegratedInfo.getMService();
        if (mService != null) {
            statement.bindString(18, mService);
        } else {
            statement.bindNull(18);
        }
        Integer mReservedInt = rawRecomIntegratedInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(19, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(19);
        }
        String mReservedText = rawRecomIntegratedInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(20, mReservedText);
        } else {
            statement.bindNull(20);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawRecomIntegratedInfo readObject(Cursor cursor, int i) {
        return new RawRecomIntegratedInfo(cursor);
    }

    public void setPrimaryKeyValue(RawRecomIntegratedInfo rawRecomIntegratedInfo, long j) {
        rawRecomIntegratedInfo.setMId(Integer.valueOf((int) j));
    }
}
