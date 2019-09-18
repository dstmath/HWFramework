package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaHealthDataHelper extends AEntityHelper<MetaHealthData> {
    private static final MetaHealthDataHelper INSTANCE = new MetaHealthDataHelper();

    private MetaHealthDataHelper() {
    }

    public static MetaHealthDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaHealthData object) {
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
        Double mHeartRat = object.getMHeartRat();
        if (mHeartRat != null) {
            statement.bindDouble(3, mHeartRat.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Double mBloodPressure_low = object.getMBloodPressure_low();
        if (mBloodPressure_low != null) {
            statement.bindDouble(4, mBloodPressure_low.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double mBloodPressure_high = object.getMBloodPressure_high();
        if (mBloodPressure_high != null) {
            statement.bindDouble(5, mBloodPressure_high.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Double mBloodSugar = object.getMBloodSugar();
        if (mBloodSugar != null) {
            statement.bindDouble(6, mBloodSugar.doubleValue());
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

    public MetaHealthData readObject(Cursor cursor, int offset) {
        return new MetaHealthData(cursor);
    }

    public void setPrimaryKeyValue(MetaHealthData object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaHealthData object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
