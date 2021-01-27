package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawLocationRecordHelper extends AEntityHelper<RawLocationRecord> {
    private static final RawLocationRecordHelper INSTANCE = new RawLocationRecordHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawLocationRecord rawLocationRecord) {
        return null;
    }

    private RawLocationRecordHelper() {
    }

    public static RawLocationRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawLocationRecord rawLocationRecord) {
        Integer mId = rawLocationRecord.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawLocationRecord.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Character mLocationType = rawLocationRecord.getMLocationType();
        if (mLocationType != null) {
            statement.bindString(3, String.valueOf(mLocationType));
        } else {
            statement.bindNull(3);
        }
        Double mLongitude = rawLocationRecord.getMLongitude();
        if (mLongitude != null) {
            statement.bindDouble(4, mLongitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double mLatitude = rawLocationRecord.getMLatitude();
        if (mLatitude != null) {
            statement.bindDouble(5, mLatitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Double mAltitude = rawLocationRecord.getMAltitude();
        if (mAltitude != null) {
            statement.bindDouble(6, mAltitude.doubleValue());
        } else {
            statement.bindNull(6);
        }
        String mCity = rawLocationRecord.getMCity();
        if (mCity != null) {
            statement.bindString(7, mCity);
        } else {
            statement.bindNull(7);
        }
        String mCountry = rawLocationRecord.getMCountry();
        if (mCountry != null) {
            statement.bindString(8, mCountry);
        } else {
            statement.bindNull(8);
        }
        String mDetailAddress = rawLocationRecord.getMDetailAddress();
        if (mDetailAddress != null) {
            statement.bindString(9, mDetailAddress);
        } else {
            statement.bindNull(9);
        }
        String mDistrict = rawLocationRecord.getMDistrict();
        if (mDistrict != null) {
            statement.bindString(10, mDistrict);
        } else {
            statement.bindNull(10);
        }
        String mProvince = rawLocationRecord.getMProvince();
        if (mProvince != null) {
            statement.bindString(11, mProvince);
        } else {
            statement.bindNull(11);
        }
        Integer mCellID = rawLocationRecord.getMCellID();
        if (mCellID != null) {
            statement.bindLong(12, (long) mCellID.intValue());
        } else {
            statement.bindNull(12);
        }
        Integer mCellMCC = rawLocationRecord.getMCellMCC();
        if (mCellMCC != null) {
            statement.bindLong(13, (long) mCellMCC.intValue());
        } else {
            statement.bindNull(13);
        }
        Integer mCellMNC = rawLocationRecord.getMCellMNC();
        if (mCellMNC != null) {
            statement.bindLong(14, (long) mCellMNC.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer mCellLAC = rawLocationRecord.getMCellLAC();
        if (mCellLAC != null) {
            statement.bindLong(15, (long) mCellLAC.intValue());
        } else {
            statement.bindNull(15);
        }
        Integer mCellRSSI = rawLocationRecord.getMCellRSSI();
        if (mCellRSSI != null) {
            statement.bindLong(16, (long) mCellRSSI.intValue());
        } else {
            statement.bindNull(16);
        }
        String mWifiBSSID = rawLocationRecord.getMWifiBSSID();
        if (mWifiBSSID != null) {
            statement.bindString(17, mWifiBSSID);
        } else {
            statement.bindNull(17);
        }
        Integer mWifiLevel = rawLocationRecord.getMWifiLevel();
        if (mWifiLevel != null) {
            statement.bindLong(18, (long) mWifiLevel.intValue());
        } else {
            statement.bindNull(18);
        }
        Integer mReservedInt = rawLocationRecord.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(19, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(19);
        }
        String mReservedText = rawLocationRecord.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(20, mReservedText);
        } else {
            statement.bindNull(20);
        }
        String geodeticSystem = rawLocationRecord.getGeodeticSystem();
        if (geodeticSystem != null) {
            statement.bindString(21, geodeticSystem);
        } else {
            statement.bindNull(21);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawLocationRecord readObject(Cursor cursor, int i) {
        return new RawLocationRecord(cursor);
    }

    public void setPrimaryKeyValue(RawLocationRecord rawLocationRecord, long j) {
        rawLocationRecord.setMId(Integer.valueOf((int) j));
    }
}
