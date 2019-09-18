package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawLocationRecordHelper extends AEntityHelper<RawLocationRecord> {
    private static final RawLocationRecordHelper INSTANCE = new RawLocationRecordHelper();

    private RawLocationRecordHelper() {
    }

    public static RawLocationRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawLocationRecord object) {
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
        Character mLocationType = object.getMLocationType();
        if (mLocationType != null) {
            statement.bindString(3, String.valueOf(mLocationType));
        } else {
            statement.bindNull(3);
        }
        Double mLongitude = object.getMLongitude();
        if (mLongitude != null) {
            statement.bindDouble(4, mLongitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double mLatitude = object.getMLatitude();
        if (mLatitude != null) {
            statement.bindDouble(5, mLatitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Double mAltitude = object.getMAltitude();
        if (mAltitude != null) {
            statement.bindDouble(6, mAltitude.doubleValue());
        } else {
            statement.bindNull(6);
        }
        String mCity = object.getMCity();
        if (mCity != null) {
            statement.bindString(7, mCity);
        } else {
            statement.bindNull(7);
        }
        String mCountry = object.getMCountry();
        if (mCountry != null) {
            statement.bindString(8, mCountry);
        } else {
            statement.bindNull(8);
        }
        String mDetailAddress = object.getMDetailAddress();
        if (mDetailAddress != null) {
            statement.bindString(9, mDetailAddress);
        } else {
            statement.bindNull(9);
        }
        String mDistrict = object.getMDistrict();
        if (mDistrict != null) {
            statement.bindString(10, mDistrict);
        } else {
            statement.bindNull(10);
        }
        String mProvince = object.getMProvince();
        if (mProvince != null) {
            statement.bindString(11, mProvince);
        } else {
            statement.bindNull(11);
        }
        Integer mCellID = object.getMCellID();
        if (mCellID != null) {
            statement.bindLong(12, (long) mCellID.intValue());
        } else {
            statement.bindNull(12);
        }
        Integer mCellMCC = object.getMCellMCC();
        if (mCellMCC != null) {
            statement.bindLong(13, (long) mCellMCC.intValue());
        } else {
            statement.bindNull(13);
        }
        Integer mCellMNC = object.getMCellMNC();
        if (mCellMNC != null) {
            statement.bindLong(14, (long) mCellMNC.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer mCellLAC = object.getMCellLAC();
        if (mCellLAC != null) {
            statement.bindLong(15, (long) mCellLAC.intValue());
        } else {
            statement.bindNull(15);
        }
        Integer mCellRSSI = object.getMCellRSSI();
        if (mCellRSSI != null) {
            statement.bindLong(16, (long) mCellRSSI.intValue());
        } else {
            statement.bindNull(16);
        }
        String mWifiBSSID = object.getMWifiBSSID();
        if (mWifiBSSID != null) {
            statement.bindString(17, mWifiBSSID);
        } else {
            statement.bindNull(17);
        }
        Integer mWifiLevel = object.getMWifiLevel();
        if (mWifiLevel != null) {
            statement.bindLong(18, (long) mWifiLevel.intValue());
        } else {
            statement.bindNull(18);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(19, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(19);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(20, mReservedText);
        } else {
            statement.bindNull(20);
        }
        String geodeticSystem = object.getGeodeticSystem();
        if (geodeticSystem != null) {
            statement.bindString(21, geodeticSystem);
        } else {
            statement.bindNull(21);
        }
    }

    public RawLocationRecord readObject(Cursor cursor, int offset) {
        return new RawLocationRecord(cursor);
    }

    public void setPrimaryKeyValue(RawLocationRecord object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawLocationRecord object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
