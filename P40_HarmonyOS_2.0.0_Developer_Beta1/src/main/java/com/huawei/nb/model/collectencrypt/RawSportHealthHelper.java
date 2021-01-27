package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSportHealthHelper extends AEntityHelper<RawSportHealth> {
    private static final RawSportHealthHelper INSTANCE = new RawSportHealthHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawSportHealth rawSportHealth) {
        return null;
    }

    private RawSportHealthHelper() {
    }

    public static RawSportHealthHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawSportHealth rawSportHealth) {
        Integer mId = rawSportHealth.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawSportHealth.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Double mHeight = rawSportHealth.getMHeight();
        if (mHeight != null) {
            statement.bindDouble(3, mHeight.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Double mWeight = rawSportHealth.getMWeight();
        if (mWeight != null) {
            statement.bindDouble(4, mWeight.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double mHeartRat = rawSportHealth.getMHeartRat();
        if (mHeartRat != null) {
            statement.bindDouble(5, mHeartRat.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Double mBloodPressureLow = rawSportHealth.getMBloodPressureLow();
        if (mBloodPressureLow != null) {
            statement.bindDouble(6, mBloodPressureLow.doubleValue());
        } else {
            statement.bindNull(6);
        }
        Double mBloodPressureHigh = rawSportHealth.getMBloodPressureHigh();
        if (mBloodPressureHigh != null) {
            statement.bindDouble(7, mBloodPressureHigh.doubleValue());
        } else {
            statement.bindNull(7);
        }
        Double mBloodSugar = rawSportHealth.getMBloodSugar();
        if (mBloodSugar != null) {
            statement.bindDouble(8, mBloodSugar.doubleValue());
        } else {
            statement.bindNull(8);
        }
        Double mSportDistance = rawSportHealth.getMSportDistance();
        if (mSportDistance != null) {
            statement.bindDouble(9, mSportDistance.doubleValue());
        } else {
            statement.bindNull(9);
        }
        Double mSportHeight = rawSportHealth.getMSportHeight();
        if (mSportHeight != null) {
            statement.bindDouble(10, mSportHeight.doubleValue());
        } else {
            statement.bindNull(10);
        }
        Double mSportHeat = rawSportHealth.getMSportHeat();
        if (mSportHeat != null) {
            statement.bindDouble(11, mSportHeat.doubleValue());
        } else {
            statement.bindNull(11);
        }
        Double mSportPaces = rawSportHealth.getMSportPaces();
        if (mSportPaces != null) {
            statement.bindDouble(12, mSportPaces.doubleValue());
        } else {
            statement.bindNull(12);
        }
        Double mSleep = rawSportHealth.getMSleep();
        if (mSleep != null) {
            statement.bindDouble(13, mSleep.doubleValue());
        } else {
            statement.bindNull(13);
        }
        String mSportAR = rawSportHealth.getMSportAR();
        if (mSportAR != null) {
            statement.bindString(14, mSportAR);
        } else {
            statement.bindNull(14);
        }
        Integer mReservedInt = rawSportHealth.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(15, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(15);
        }
        String mReservedText = rawSportHealth.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(16, mReservedText);
        } else {
            statement.bindNull(16);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawSportHealth readObject(Cursor cursor, int i) {
        return new RawSportHealth(cursor);
    }

    public void setPrimaryKeyValue(RawSportHealth rawSportHealth, long j) {
        rawSportHealth.setMId(Integer.valueOf((int) j));
    }
}
