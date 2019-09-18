package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSportHealthHelper extends AEntityHelper<RawSportHealth> {
    private static final RawSportHealthHelper INSTANCE = new RawSportHealthHelper();

    private RawSportHealthHelper() {
    }

    public static RawSportHealthHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawSportHealth object) {
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
        Double mHeight = object.getMHeight();
        if (mHeight != null) {
            statement.bindDouble(3, mHeight.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Double mWeight = object.getMWeight();
        if (mWeight != null) {
            statement.bindDouble(4, mWeight.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double mHeartRat = object.getMHeartRat();
        if (mHeartRat != null) {
            statement.bindDouble(5, mHeartRat.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Double mBloodPressureLow = object.getMBloodPressureLow();
        if (mBloodPressureLow != null) {
            statement.bindDouble(6, mBloodPressureLow.doubleValue());
        } else {
            statement.bindNull(6);
        }
        Double mBloodPressureHigh = object.getMBloodPressureHigh();
        if (mBloodPressureHigh != null) {
            statement.bindDouble(7, mBloodPressureHigh.doubleValue());
        } else {
            statement.bindNull(7);
        }
        Double mBloodSugar = object.getMBloodSugar();
        if (mBloodSugar != null) {
            statement.bindDouble(8, mBloodSugar.doubleValue());
        } else {
            statement.bindNull(8);
        }
        Double mSportDistance = object.getMSportDistance();
        if (mSportDistance != null) {
            statement.bindDouble(9, mSportDistance.doubleValue());
        } else {
            statement.bindNull(9);
        }
        Double mSportHeight = object.getMSportHeight();
        if (mSportHeight != null) {
            statement.bindDouble(10, mSportHeight.doubleValue());
        } else {
            statement.bindNull(10);
        }
        Double mSportHeat = object.getMSportHeat();
        if (mSportHeat != null) {
            statement.bindDouble(11, mSportHeat.doubleValue());
        } else {
            statement.bindNull(11);
        }
        Double mSportPaces = object.getMSportPaces();
        if (mSportPaces != null) {
            statement.bindDouble(12, mSportPaces.doubleValue());
        } else {
            statement.bindNull(12);
        }
        Double mSleep = object.getMSleep();
        if (mSleep != null) {
            statement.bindDouble(13, mSleep.doubleValue());
        } else {
            statement.bindNull(13);
        }
        String mSportAR = object.getMSportAR();
        if (mSportAR != null) {
            statement.bindString(14, mSportAR);
        } else {
            statement.bindNull(14);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(15, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(15);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(16, mReservedText);
        } else {
            statement.bindNull(16);
        }
    }

    public RawSportHealth readObject(Cursor cursor, int offset) {
        return new RawSportHealth(cursor);
    }

    public void setPrimaryKeyValue(RawSportHealth object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawSportHealth object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
