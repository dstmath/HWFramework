package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaExerciseDataHelper extends AEntityHelper<MetaExerciseData> {
    private static final MetaExerciseDataHelper INSTANCE = new MetaExerciseDataHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaExerciseData metaExerciseData) {
        return null;
    }

    private MetaExerciseDataHelper() {
    }

    public static MetaExerciseDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaExerciseData metaExerciseData) {
        Integer mId = metaExerciseData.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaExerciseData.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Double mSportHeat = metaExerciseData.getMSportHeat();
        if (mSportHeat != null) {
            statement.bindDouble(3, mSportHeat.doubleValue());
        } else {
            statement.bindNull(3);
        }
        Integer mClimb = metaExerciseData.getMClimb();
        if (mClimb != null) {
            statement.bindLong(4, (long) mClimb.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mDecline = metaExerciseData.getMDecline();
        if (mDecline != null) {
            statement.bindLong(5, (long) mDecline.intValue());
        } else {
            statement.bindNull(5);
        }
        Double mSportDistance = metaExerciseData.getMSportDistance();
        if (mSportDistance != null) {
            statement.bindDouble(6, mSportDistance.doubleValue());
        } else {
            statement.bindNull(6);
        }
        Double mSleep = metaExerciseData.getMSleep();
        if (mSleep != null) {
            statement.bindDouble(7, mSleep.doubleValue());
        } else {
            statement.bindNull(7);
        }
        Double mSportPaces = metaExerciseData.getMSportPaces();
        if (mSportPaces != null) {
            statement.bindDouble(8, mSportPaces.doubleValue());
        } else {
            statement.bindNull(8);
        }
        Double mHeight = metaExerciseData.getMHeight();
        if (mHeight != null) {
            statement.bindDouble(9, mHeight.doubleValue());
        } else {
            statement.bindNull(9);
        }
        Double mWeight = metaExerciseData.getMWeight();
        if (mWeight != null) {
            statement.bindDouble(10, mWeight.doubleValue());
        } else {
            statement.bindNull(10);
        }
        String mSportAR = metaExerciseData.getMSportAR();
        if (mSportAR != null) {
            statement.bindString(11, mSportAR);
        } else {
            statement.bindNull(11);
        }
        Integer mWalk = metaExerciseData.getMWalk();
        if (mWalk != null) {
            statement.bindLong(12, (long) mWalk.intValue());
        } else {
            statement.bindNull(12);
        }
        Integer mRun = metaExerciseData.getMRun();
        if (mRun != null) {
            statement.bindLong(13, (long) mRun.intValue());
        } else {
            statement.bindNull(13);
        }
        Integer mCycling = metaExerciseData.getMCycling();
        if (mCycling != null) {
            statement.bindLong(14, (long) mCycling.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer mReservedInt = metaExerciseData.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(15, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(15);
        }
        String mReservedText = metaExerciseData.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(16, mReservedText);
        } else {
            statement.bindNull(16);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaExerciseData readObject(Cursor cursor, int i) {
        return new MetaExerciseData(cursor);
    }

    public void setPrimaryKeyValue(MetaExerciseData metaExerciseData, long j) {
        metaExerciseData.setMId(Integer.valueOf((int) j));
    }
}
