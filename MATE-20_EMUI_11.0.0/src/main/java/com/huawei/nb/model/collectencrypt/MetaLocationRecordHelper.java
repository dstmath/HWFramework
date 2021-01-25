package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaLocationRecordHelper extends AEntityHelper<MetaLocationRecord> {
    private static final MetaLocationRecordHelper INSTANCE = new MetaLocationRecordHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaLocationRecord metaLocationRecord) {
        return null;
    }

    private MetaLocationRecordHelper() {
    }

    public static MetaLocationRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaLocationRecord metaLocationRecord) {
        Integer mId = metaLocationRecord.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaLocationRecord.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Character mLocationType = metaLocationRecord.getMLocationType();
        if (mLocationType != null) {
            statement.bindString(3, String.valueOf(mLocationType));
        } else {
            statement.bindNull(3);
        }
        Double mLongitude = metaLocationRecord.getMLongitude();
        if (mLongitude != null) {
            statement.bindDouble(4, mLongitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double mLatitude = metaLocationRecord.getMLatitude();
        if (mLatitude != null) {
            statement.bindDouble(5, mLatitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Integer mCellID = metaLocationRecord.getMCellID();
        if (mCellID != null) {
            statement.bindLong(6, (long) mCellID.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mCellLAC = metaLocationRecord.getMCellLAC();
        if (mCellLAC != null) {
            statement.bindLong(7, (long) mCellLAC.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer mCellRSSI = metaLocationRecord.getMCellRSSI();
        if (mCellRSSI != null) {
            statement.bindLong(8, (long) mCellRSSI.intValue());
        } else {
            statement.bindNull(8);
        }
        String mWifiBSSID = metaLocationRecord.getMWifiBSSID();
        if (mWifiBSSID != null) {
            statement.bindString(9, mWifiBSSID);
        } else {
            statement.bindNull(9);
        }
        Integer mWifiLevel = metaLocationRecord.getMWifiLevel();
        if (mWifiLevel != null) {
            statement.bindLong(10, (long) mWifiLevel.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer mReservedInt = metaLocationRecord.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(11, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(11);
        }
        String mReservedText = metaLocationRecord.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(12, mReservedText);
        } else {
            statement.bindNull(12);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaLocationRecord readObject(Cursor cursor, int i) {
        return new MetaLocationRecord(cursor);
    }

    public void setPrimaryKeyValue(MetaLocationRecord metaLocationRecord, long j) {
        metaLocationRecord.setMId(Integer.valueOf((int) j));
    }
}
