package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawWeatherInfoHelper extends AEntityHelper<RawWeatherInfo> {
    private static final RawWeatherInfoHelper INSTANCE = new RawWeatherInfoHelper();

    private RawWeatherInfoHelper() {
    }

    public static RawWeatherInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawWeatherInfo object) {
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
        Double mLongitude = object.getMLongitude();
        if (mLongitude != null) {
            statement.bindDouble(3, mLongitude.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Double mLatitude = object.getMLatitude();
        if (mLatitude != null) {
            statement.bindDouble(4, mLatitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Integer mWeatherIcon = object.getMWeatherIcon();
        if (mWeatherIcon != null) {
            statement.bindLong(5, (long) mWeatherIcon.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mTemprature = object.getMTemprature();
        if (mTemprature != null) {
            statement.bindLong(6, (long) mTemprature.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    public RawWeatherInfo readObject(Cursor cursor, int offset) {
        return new RawWeatherInfo(cursor);
    }

    public void setPrimaryKeyValue(RawWeatherInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawWeatherInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
