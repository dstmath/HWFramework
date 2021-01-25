package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaHealthDataHelper extends AEntityHelper<MetaHealthData> {
    private static final MetaHealthDataHelper INSTANCE = new MetaHealthDataHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaHealthData metaHealthData) {
        return null;
    }

    private MetaHealthDataHelper() {
    }

    public static MetaHealthDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaHealthData metaHealthData) {
        Integer mId = metaHealthData.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaHealthData.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Double mHeartRat = metaHealthData.getMHeartRat();
        if (mHeartRat != null) {
            statement.bindDouble(3, mHeartRat.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Double mBloodPressure_low = metaHealthData.getMBloodPressure_low();
        if (mBloodPressure_low != null) {
            statement.bindDouble(4, mBloodPressure_low.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double mBloodPressure_high = metaHealthData.getMBloodPressure_high();
        if (mBloodPressure_high != null) {
            statement.bindDouble(5, mBloodPressure_high.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Double mBloodSugar = metaHealthData.getMBloodSugar();
        if (mBloodSugar != null) {
            statement.bindDouble(6, mBloodSugar.doubleValue());
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = metaHealthData.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = metaHealthData.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaHealthData readObject(Cursor cursor, int i) {
        return new MetaHealthData(cursor);
    }

    public void setPrimaryKeyValue(MetaHealthData metaHealthData, long j) {
        metaHealthData.setMId(Integer.valueOf((int) j));
    }
}
