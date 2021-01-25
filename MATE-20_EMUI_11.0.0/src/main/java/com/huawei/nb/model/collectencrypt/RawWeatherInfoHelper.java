package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawWeatherInfoHelper extends AEntityHelper<RawWeatherInfo> {
    private static final RawWeatherInfoHelper INSTANCE = new RawWeatherInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawWeatherInfo rawWeatherInfo) {
        return null;
    }

    private RawWeatherInfoHelper() {
    }

    public static RawWeatherInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawWeatherInfo rawWeatherInfo) {
        Integer mId = rawWeatherInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawWeatherInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Double mLongitude = rawWeatherInfo.getMLongitude();
        if (mLongitude != null) {
            statement.bindDouble(3, mLongitude.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Double mLatitude = rawWeatherInfo.getMLatitude();
        if (mLatitude != null) {
            statement.bindDouble(4, mLatitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Integer mWeatherIcon = rawWeatherInfo.getMWeatherIcon();
        if (mWeatherIcon != null) {
            statement.bindLong(5, (long) mWeatherIcon.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mTemprature = rawWeatherInfo.getMTemprature();
        if (mTemprature != null) {
            statement.bindLong(6, (long) mTemprature.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = rawWeatherInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = rawWeatherInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawWeatherInfo readObject(Cursor cursor, int i) {
        return new RawWeatherInfo(cursor);
    }

    public void setPrimaryKeyValue(RawWeatherInfo rawWeatherInfo, long j) {
        rawWeatherInfo.setMId(Integer.valueOf((int) j));
    }
}
